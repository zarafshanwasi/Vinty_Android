package com.idealink.vinty.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.idealink.vinty.App
import com.idealink.vinty.R
import com.idealink.vinty.data.DataManager
import com.idealink.vinty.databinding.ActivityMainBinding
import com.idealink.vinty.ui.viewmodel.UserSharedViewModel
import com.idealink.vinty.ui.viewmodel.ViewModelFactory
import com.idealink.vinty.utils.AdManager
import com.idealink.vinty.utils.AnalyticsEvent
import com.idealink.vinty.utils.AnalyticsManager
import com.idealink.vinty.utils.FacebookAnalyticsManager
import com.idealink.vinty.utils.FacebookEvent
import com.idealink.vinty.utils.VintyFirebaseMessagingService
import com.idealink.vinty.utils.hideSystemUI
import kotlinx.coroutines.launch

/**
 * MainActivity handles the main app experience after authentication.
 * All login/account logic has been moved to AccountActivity.
 */
class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var adManager: AdManager

    private val userSharedViewModel: UserSharedViewModel by viewModels {
        val app = application as App
        ViewModelFactory(app.repository)
    }

    private val TAG = "MainActivity"

    private var bottomSheetDialog: BottomSheetDialog? = null
    private var earnedTickets = 0

    private enum class BottomTab {
        TROPHY, LEADERBOARD, GIFT, PROFILE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check authentication status
        val dataManager = DataManager.getInstance()
        if (!dataManager.isLoggedIn()) {
            // User not authenticated, redirect to AccountActivity
            navigateToAccountActivity()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup NavController
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Register listeners
        handleBottomMenuVisibility()
        observeActiveTab()
        observeAdReward()
        setupBottomMenu()

        // Navigate to home fragment by default
        navController.navigate(R.id.homeFragment)

        // Handle notification tap if launched from one
        handleNotificationIntent(intent)

        // Initialize Ads
        adManager = AdManager(this)
        adManager.setLoadingCallback { isLoading ->
            runOnUiThread {
                binding.adLoadingProgress.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }
    }


    /**
     * Navigate to AccountActivity for authentication
     */
    private fun navigateToAccountActivity() {
        val intent = Intent(this, AccountActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun setupBottomMenu() {
        binding.trophyBtn.setOnClickListener {
            navController.navigate(R.id.homeFragment)
            setActiveTab(BottomTab.TROPHY)
        }
        binding.leaderboardBtn.setOnClickListener {
            navController.navigate(R.id.missionFragment)
            setActiveTab(BottomTab.LEADERBOARD)
        }
        binding.giftBtn.setOnClickListener {
            navController.navigate(R.id.fragmentGiftStore)
            setActiveTab(BottomTab.GIFT)
        }
        binding.profileBtn.setOnClickListener {
            navController.navigate(R.id.profileFragment)
            setActiveTab(BottomTab.PROFILE)
        }
        binding.adBtn.setOnClickListener {
            showRewardedAd()
        }
    }

    private fun observeActiveTab() {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.homeFragment -> setActiveTab(BottomTab.TROPHY)
                R.id.missionFragment -> setActiveTab(BottomTab.LEADERBOARD)
                R.id.fragmentGiftStore -> setActiveTab(BottomTab.GIFT)
                R.id.profileFragment -> setActiveTab(BottomTab.PROFILE)
            }
        }
    }

    private fun setActiveTab(tab: BottomTab) {
        binding.trophyBtn.setImageResource(R.drawable.tab_trophy)
        binding.leaderboardBtn.setImageResource(R.drawable.tab_btn3_dim_receipt)
        binding.giftBtn.setImageResource(R.drawable.ic_gifts)
        binding.profileBtn.setImageResource(R.drawable.tap_btn1_person_dim)

        when (tab) {
            BottomTab.TROPHY -> binding.trophyBtn.setImageResource(R.drawable.tab2)
            BottomTab.LEADERBOARD -> binding.leaderboardBtn.setImageResource(R.drawable.tab4)
            BottomTab.GIFT -> binding.giftBtn.setImageResource(R.drawable.tab1)
            BottomTab.PROFILE -> binding.profileBtn.setImageResource(R.drawable.tab3)
        }
    }

    private fun handleBottomMenuVisibility() {
        val hiddenFragments = setOf(
            R.id.settingFragment,
            R.id.userHistoryFragment,
            R.id.inviteCodeFragment,
            R.id.onboardingFragment,
            R.id.fragmentJackpot,
            R.id.changePasswordFragment,
            R.id.signInOtpFragment,
            R.id.leaderboardFragment,
            R.id.badgeFragment
        )

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val visibility = if (destination.id in hiddenFragments) View.GONE else View.VISIBLE

            binding.bottomMenuBg.visibility = visibility
            binding.trophyBtn.visibility = visibility
            binding.leaderboardBtn.visibility = visibility
            binding.giftBtn.visibility = visibility
            binding.profileBtn.visibility = visibility
            binding.adBtn.visibility = visibility
        }
    }

    private fun observeAdReward() {
        lifecycleScope.launchWhenStarted {
            userSharedViewModel.adReward.collect { tickets ->
                tickets?.let {
                    if (it > 0) {
                        earnedTickets = it
                        // Track earn ticket event
                        AnalyticsManager.log(AnalyticsEvent.EARN_TICKET)
                        FacebookAnalyticsManager.logEvent(FacebookEvent.EARN_TICKET)
                        showRewardBottomSheet(it)
                        userSharedViewModel.resetAdReward()
                    }
                }
            }
        }
    }

    fun showRewardedAd() {
        // Track ad watch clicked
        AnalyticsManager.log(AnalyticsEvent.AD_WATCH_CLICKED)
        FacebookAnalyticsManager.logEvent(FacebookEvent.AD_WATCH_CLICKED)

        adManager.showRewardedAd(this, object : AdManager.AdListener {

            override fun onAdCompleted() {
                // Track ad reward earned
                AnalyticsManager.log(AnalyticsEvent.AD_REWARD_EARNED)
                FacebookAnalyticsManager.logEvent(FacebookEvent.AD_REWARD_EARNED)

                userSharedViewModel.recordAdView()
            }

            override fun onError(message: String) {
                Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
            }

            override fun onAdLoaded() {}
            override fun onAdFailedToLoad(error: String) {}
            override fun onAdDismissed() {}
            override fun onAdShown() {}
        })
    }

    fun showRewardedAdFromBadge() {
        Log.d(TAG, "🎯 Rewarded ad requested from Badge BottomSheet")
        showRewardedAd()
    }

    fun showRewardBottomSheet(i: Int) {
        val bottomSheetView = LayoutInflater.from(this)
            .inflate(R.layout.ad_bottom_bar, null)

        val adTitle = bottomSheetView.findViewById<TextView>(R.id.ad_title)
        val continueWatchBtn = bottomSheetView.findViewById<MaterialButton>(R.id.continue_watch_btn)
        val goBackBtn = bottomSheetView.findViewById<View>(R.id.push_back)

        adTitle.text = "You earned $earnedTickets ticket${if (earnedTickets > 1) "s" else ""}"

        bottomSheetDialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        bottomSheetDialog?.setContentView(bottomSheetView)
        bottomSheetDialog?.setCancelable(false)

        continueWatchBtn.setOnClickListener {
            bottomSheetDialog?.dismiss()
            showRewardedAd()
        }

        goBackBtn.setOnClickListener {
            bottomSheetDialog?.dismiss()
        }

        bottomSheetDialog?.show()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent) {
        val notificationType = intent.getStringExtra(VintyFirebaseMessagingService.EXTRA_NOTIFICATION_TYPE) ?: return
        val notificationId = intent.getStringExtra(VintyFirebaseMessagingService.EXTRA_NOTIFICATION_ID)

        Log.d(TAG, "Handling notification tap - type: $notificationType, id: $notificationId")

        // Navigate to the correct fragment based on notification type
        val destinationId = when (notificationType) {
            "lottery_win", "lottery_draw", "lottery_reminder" -> R.id.homeFragment
            "mission_complete", "new_mission" -> R.id.missionFragment
            "gift_delivery", "gift_redemption" -> R.id.fragmentGiftStore
            "system_announcement", "user_engagement" -> R.id.homeFragment
            else -> R.id.homeFragment
        }

        navController.navigate(destinationId)

        // Mark notification as clicked on the backend
        notificationId?.let { id ->
            lifecycleScope.launch {
                try {
                    val app = application as App
                    app.repository.markNotificationAsClicked(id)
                    Log.d(TAG, "Notification marked as clicked: $id")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to mark notification as clicked: ${e.message}")
                }
            }
        }

        // Clear the extras so re-creation doesn't re-navigate
        intent.removeExtra(VintyFirebaseMessagingService.EXTRA_NOTIFICATION_TYPE)
        intent.removeExtra(VintyFirebaseMessagingService.EXTRA_NOTIFICATION_ID)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUI()
        }
    }

    override fun onDestroy() {
        bottomSheetDialog?.dismiss()
        super.onDestroy()
    }
}