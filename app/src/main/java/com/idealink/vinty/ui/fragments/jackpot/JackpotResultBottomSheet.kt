package com.idealink.vinty.ui.fragments.jackpot

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.idealink.vinty.R
import com.idealink.vinty.data.model.SpinResult
import com.idealink.vinty.databinding.BottomSheetJackpotResultBinding

/**
 * Bottom sheet dialog to display jackpot spin results.
 */
class JackpotResultBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetJackpotResultBinding? = null
    private val binding get() = _binding!!

    private var spinResult: SpinResult? = null
    private var onDismissCallback: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetJackpotResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
    }

    private fun setupUI() {
        spinResult?.let { result ->
            // Set reward label
            binding.rewardLabel.text = result.reward.label

//            // Set reward amount
//            binding.rewardAmount.text = result.reward.amount.toString()
//
//            // Set reward type
//            binding.rewardType.text = result.reward.type.replaceFirstChar {
//                if (it.isLowerCase()) it.titlecase() else it.toString()
//            }

            // Set balance info
//            binding.totalTickets.text = result.userBalance.tickets.toString()
//            binding.spinsRemaining.text = result.userBalance.spinsRemaining.toString()

            // Set icon based on reward type
            val iconRes = when (result.reward.type.lowercase()) {
                "tickets" -> R.drawable.ticket_big
                "gems", "gem" -> R.drawable.ticket_big
                else -> R.drawable.ticket_big
            }
            binding.rewardIcon.setImageResource(iconRes)
        }

        binding.btnContinue.setOnClickListener {
            onDismissCallback?.invoke()
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(
            spinResult: SpinResult,
            onDismiss: () -> Unit
        ): JackpotResultBottomSheet {
            return JackpotResultBottomSheet().apply {
                this.spinResult = spinResult
                this.onDismissCallback = onDismiss
            }
        }
    }
}
