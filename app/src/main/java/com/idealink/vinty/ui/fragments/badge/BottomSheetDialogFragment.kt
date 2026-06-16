package com.idealink.vinty.ui.fragments.badge

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.idealink.vinty.App
import com.idealink.vinty.BuildConfig
import com.idealink.vinty.R
import com.idealink.vinty.data.DataManager
import com.idealink.vinty.data.model.Badge
import com.idealink.vinty.databinding.FragmentBottomSheetDailogBinding
import com.idealink.vinty.ui.activities.MainActivity
import com.idealink.vinty.ui.viewmodel.BadgeViewModel
import com.idealink.vinty.ui.viewmodel.UserSharedViewModel
import com.idealink.vinty.ui.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch

class BadgeDetailBottomSheet(
    private val badge: Badge
) : BottomSheetDialogFragment() {

    companion object {
        private const val AD_ENGAGEMENT = "AD_ENGAGEMENT"
    }
    private val badgeViewModel: BadgeViewModel by viewModels {
        val app = requireActivity().application as App
        ViewModelFactory(app.repository)
    }

    private val userSharedViewModel: UserSharedViewModel by activityViewModels {
        val app = requireActivity().application as App
        ViewModelFactory(app.repository)
    }

    private var _binding: FragmentBottomSheetDailogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBottomSheetDailogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Text data
        binding.badgeName.text = badge.name
        binding.badgeDescription.text = badge.description

        if (badge.ticketReward > 0) {
            binding.ticketsReward.text = badge.ticketReward.toString()
            binding.ticketsReward.visibility = View.VISIBLE
        } else {
            binding.ticketsReward.visibility = View.GONE
        }

        loadBadgeImage()
        setupButtonsByCategory()
        observeClaimResult()

        binding.closeBtn.setOnClickListener {
            dismiss()
        }

        binding.adWatchBtn.setOnClickListener {
            (requireActivity() as? MainActivity)
                ?.showRewardedAdFromBadge()
        }
        binding.claimRewardBtn.setOnClickListener {
            badgeViewModel.claimBadgeReward(badge.id)
        }
        binding.inviteBtn.setOnClickListener {
            shareInviteCode()
        }
    }

    private fun observeClaimResult() {
        viewLifecycleOwner.lifecycleScope.launch {
            badgeViewModel.claimResult.collect { result ->
                result?.let {
                    if (it.success) {

                        // Update missions / tickets
                        badgeViewModel.loadBadges()
                        userSharedViewModel.triggerDataRefresh()

                        // Close badge bottom sheet
                        dismiss()

                        // Clear state
                        badgeViewModel.clearClaimResult()
                    }
                }
            }
        }
    }


    private fun setupButtonsByCategory() {

        // Hide all action buttons first
        binding.claimRewardBtn.visibility = View.GONE
        binding.adWatchBtn.visibility = View.GONE
        binding.inviteBtn.visibility = View.GONE
        binding.closeBtn.visibility = View.VISIBLE

        when {

            // 🔹 SOCIAL REFERRAL LOGIC
            badge.category == "SOCIAL_REFERRALS" && !badge.isUnlocked -> {
                binding.inviteBtn.visibility = View.VISIBLE
            }

            // 🔹 AD ENGAGEMENT LOGIC
            badge.category == AD_ENGAGEMENT && !badge.isUnlocked -> {
                binding.adWatchBtn.visibility = View.VISIBLE
            }

            // 🔹 CLAIM AVAILABLE (for all categories)
            badge.isUnlocked && !badge.rewardClaimed -> {
                binding.claimRewardBtn.visibility = View.VISIBLE
            }

            // 🔹 Default → Continue only
            else -> {
                // Nothing extra (Continue already visible)
            }
        }
    }


    private fun shareInviteCode() {

        val inviteCode = DataManager.getInstance().getInviteCode()

        if (inviteCode.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Invite code not available", Toast.LENGTH_SHORT).show()
            return
        }

        val message = """
        🎁 Join the app! Use my code $inviteCode in the app.""".trimIndent()

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
        }

        startActivity(Intent.createChooser(shareIntent, "Share via"))
    }



    private fun loadBadgeImage() {
        val baseUrl = BuildConfig.BASE_IMAGE_URL
        val imageUrl = if (badge.iconUrl.startsWith("http")) {
            badge.iconUrl
        } else {
            "$baseUrl${badge.iconUrl}"
        }

        Glide.with(requireContext())
            .load(imageUrl)
            .placeholder(R.drawable.profile_icon)
            .into(binding.imageView23)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
