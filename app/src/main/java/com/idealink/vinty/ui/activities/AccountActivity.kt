package com.idealink.vinty.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.idealink.vinty.R
import com.idealink.vinty.data.DataManager

/**
 * AccountActivity handles all authentication and account-related flows:
 * - Login
 * - Registration
 * - Password reset
 * - Email verification
 *
 * Once authenticated, navigates to MainActivity for the main app experience.
 */
class AccountActivity : BaseActivity() {

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_account)

        // Setup NavController
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_account) as NavHostFragment
        navController = navHostFragment.navController

        // Handle onboarding check
        val showOnboarding = intent.getBooleanExtra("SHOW_ONBOARDING", false)
        if (showOnboarding) {
            navController.navigate(R.id.onboardingFragment)
            return
        }

        // Check if user is already logged in
        checkAuthenticationStatus()
    }

    /**
     * Check if user is already authenticated and navigate accordingly
     */
    private fun checkAuthenticationStatus() {
        val dataManager = DataManager.Companion.getInstance()

        if (dataManager.isLoggedIn()) {
            // User is logged in, navigate to MainActivity
            navigateToMainActivity()
        } else {
            // Clear any stale data
            dataManager.clearSession()
            // Navigate to login fragment (default start destination)
        }
    }

    /**
     * Navigate to MainActivity when authentication is complete
     */
    fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    /**
     * Handle logout - clear session and stay in AccountActivity
     */
    fun handleLogout() {
        DataManager.getInstance().clearSession()
        // Navigation will be handled by the fragment
    }
}