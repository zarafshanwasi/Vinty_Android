package com.idealink.vinty.ui.fragments.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.idealink.vinty.App
import com.idealink.vinty.R
import com.idealink.vinty.databinding.FragmentHomeBinding
import com.idealink.vinty.ui.fragments.auth.SignInOtpFragment
import com.idealink.vinty.ui.fragments.jackpot.BottomSheetJackpot
import com.idealink.vinty.ui.fragments.jackpot.NoSpinsBottomSheet
import com.idealink.vinty.ui.viewmodel.HomeViewModel
import com.idealink.vinty.ui.viewmodel.HomeEvent
import com.idealink.vinty.ui.viewmodel.UserSharedViewModel
import com.idealink.vinty.ui.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.load.DataSource
import android.graphics.drawable.Drawable
import android.widget.Toast


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val homeViewModel: HomeViewModel by activityViewModels {
        val app = requireActivity().application as App
        ViewModelFactory(app.repository)
    }

    private val userSharedViewModel: UserSharedViewModel by activityViewModels {
        val app = requireActivity().application as App
        ViewModelFactory(app.repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeViewModel()
        observeEvents()
        observeErrors()
        setupClickListeners()
        
        // Check for invite rewards
        userSharedViewModel.rewardGain?.let {
            InviteFriendRewardBottomSheet(
                reward = it,
                onClaim = {}
            ).show(parentFragmentManager, "InviteReward")
        }

        // Fetch initial data
        userSharedViewModel.user.value?.let {
            // user exists → do nothing
        } ?: run {
            userSharedViewModel.loadUser()
            homeViewModel.fetchBadgeProgress()
            homeViewModel.fetchSpinStatus()
            userSharedViewModel.fetchBadgeStats()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Observe centralized UI state from ViewModel
     */
    /**
     * Observe centralized UI state from ViewModel
     */
    private fun observeViewModel() {
        // Observe Loading
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                homeViewModel.isLoading.collect { isLoading ->
                    binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                }
            }
        }

        // Observe User Data (from ProfileViewModel now)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                userSharedViewModel.user.collect { user ->
                    user?.let {
                        binding.profileHeader.updateProfile(user)
                        val welcomeMessage = "Welcome, ${user.username}"
                        binding.welcomeText.text = welcomeMessage
                        
                        // Verification logic
                        val showVerification = !user.isVerified
                        val verificationVisibility = if (showVerification) View.VISIBLE else View.GONE
                        binding.verifyBox.visibility = verificationVisibility
                        binding.verificationMessageText.visibility = verificationVisibility
                        binding.verificationBtn.visibility = verificationVisibility
                        binding.verifyNowText.visibility = verificationVisibility
                    }
                }
            }
        }

        // Observe Badge State Progress
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                homeViewModel.badgeStateProgress.collect { badgeStateProgress ->
                    badgeStateProgress?.let {
                        it.iconUrl?.let { url ->

                            // Show loader first
                            binding.badgeLoader.visibility = View.VISIBLE

                            Glide.with(this@HomeFragment)
                                .load(url)
                                .listener(object : RequestListener<Drawable> {

                                    override fun onLoadFailed(
                                        e: GlideException?,
                                        model: Any?,
                                        target: Target<Drawable>,
                                        isFirstResource: Boolean
                                    ): Boolean {
                                        binding.badgeLoader.visibility = View.GONE
                                        return false
                                    }

                                    override fun onResourceReady(
                                        resource: Drawable,
                                        model: Any,
                                        target: Target<Drawable>?,
                                        dataSource: DataSource,
                                        isFirstResource: Boolean
                                    ): Boolean {
                                        binding.badgeLoader.visibility = View.GONE
                                        return false
                                    }
                                })

                                .into(binding.badgeIconImage)
                        }

                        it.message?.let { message ->
                            binding.badgeDescriptionHome.text = message
                        }
                    }
                }
            }
        }

        // Observe Badge Stats from ProfileViewModel
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                userSharedViewModel.badgeStats.collect { response ->
                    response?.data?.overall?.let {
                        binding.profileHeader.updateBadgeStats(
                            unlocked = it.unlocked,
                            locked = it.locked
                        )
                    }
                }
            }
        }

        // Observe Spin Status
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                homeViewModel.spinStatus.collect { status ->
                    status?.let {
                        if (it.success && it.spinsRemaining > 0 && !userSharedViewModel.hasShownSpinSheet) {
                            userSharedViewModel.hasShownSpinSheet = true
                            BottomSheetJackpot().show(parentFragmentManager, "JackpotSheet")
                        }
                    }
                }
            }
        }
    }

    /**
     * Observe one-time events for navigation and UI feedback
     */
    private fun observeEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                homeViewModel.events.collect { event ->
                    when (event) {
                        is HomeEvent.NavigateToJackpot -> {
                            findNavController().navigate(R.id.action_homeFragment_to_fragmentJackpot)
                        }
                        is HomeEvent.ShowMessage -> {
                            NoSpinsBottomSheet(event.message)
                                .show(parentFragmentManager, "NoSpinsBottomSheet")
                        }
                    }
                }
            }
        }
    }

    // ========================================================================
    // OBSERVERS - Error Handling
    // ========================================================================

    private fun observeErrors() {
        viewLifecycleOwner.lifecycleScope.launch {
            userSharedViewModel.error.collect { error ->
                error?.let {
                    showToast(it)
                    userSharedViewModel.clearError()
                }
            }
        }
    }

    /**
     * Setup click listeners for navigation
     */
    private fun setupClickListeners() {
        binding.seeAllMissionBtn.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_missionFragment)
        }
        
        binding.viewBadgeBtn.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_badgeFragment)
        }
        
        binding.leaderboardBtn2.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_leaderboardFragment)
        }
        
        binding.inviteBtn.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_inviteCodeFragment)
        }
        
        binding.profileHeader.setOnProfileClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_profileFragment)
        }
        
        binding.spinBtn.setOnClickListener {
            homeViewModel.onSpinClicked()
        }
        
        binding.verifyNowText.setOnClickListener {
            val user = userSharedViewModel.user.value
            val bundle = Bundle().apply {
                putString("source", SignInOtpFragment.SOURCE_HOME)
                putString("email", user?.email)
            }
            findNavController().navigate(
                R.id.action_homeFragment_to_signInOtpFragment2,
                bundle
            )
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}