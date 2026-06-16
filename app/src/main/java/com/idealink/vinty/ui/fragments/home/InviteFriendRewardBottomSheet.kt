package com.idealink.vinty.ui.fragments.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.idealink.vinty.databinding.FragmentInviteFriendRewardBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class InviteFriendRewardBottomSheet(
    private val reward: Int,
    private val onClaim: () -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: FragmentInviteFriendRewardBottomSheetBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInviteFriendRewardBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 🎟 Show reward value
        binding.ticketsReward.text = reward.toString()

        // Claim button
        binding.claimRewardBtn.setOnClickListener {
            dismiss()
            onClaim()
        }

        isCancelable = false // user must claim
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
