package com.idealink.vinty.ui.fragments.jackpot

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.BuildConfig
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.fragment.findNavController
import com.idealink.vinty.App
import com.idealink.vinty.R
import com.idealink.vinty.data.model.SpinResult
import com.idealink.vinty.databinding.FragmentJackpotBinding
import com.idealink.vinty.ui.viewmodel.UserSharedViewModel
import com.idealink.vinty.ui.viewmodel.ViewModelFactory
import com.idealink.vinty.utils.SoundManager
import com.idealink.vinty.utils.cancelImageSequenceStreamed
import com.idealink.vinty.utils.playImageSequenceStreamed

/**
 * Jackpot slot machine fragment with frame-by-frame animations.
 * Implements weighted probability outcomes matching iOS logic.
 */
class JackpotFragment : Fragment() {

    private var _binding: FragmentJackpotBinding? = null
    private val binding get() = _binding!!

    private val viewModel: JackpotViewModel by viewModels {
        val app = requireActivity().application as App
        ViewModelFactory(app.repository)
    }

    private val userSharedViewModel: UserSharedViewModel by activityViewModels {
        val app = requireActivity().application as App
        ViewModelFactory(app.repository)
    }

    enum class Symbol(val value: String) {
        GEM("gem"),
        TICKET("ticket"),
        PLAY("play-icon"),
        ANY("any"),
        NO_MATCH("no-match");
    }

    private val slotOptions = listOf(Symbol.GEM, Symbol.PLAY, Symbol.TICKET)

    private var results: MutableList<Symbol> = mutableListOf()
    private var finishedCount = 0
    private var currentState: JackpotState = JackpotState.ReadyToSpin
        set(value) {
            field = value
            updateUI(value)
        }

    // Debug mode
    private var debugSelectedPattern: List<Symbol>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentJackpotBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupObservers()
        updateUI(currentState)
    }

    private fun setupUI() {
        // Show debug button only in debug builds
        binding.btnDebugOutcome.visibility = if (BuildConfig.DEBUG) View.VISIBLE else View.GONE

        binding.btnSpin.setOnClickListener {
            when (currentState) {
                is JackpotState.Win, is JackpotState.Lose -> {
                    saveSpinResult()
                }
                is JackpotState.ReadyToSpin -> {
                    startSpin()
                }
                is JackpotState.Spinning -> {
                    // Do nothing while spinning
                }
            }
        }

        binding.btnDebugOutcome.setOnClickListener {
            if (currentState !is JackpotState.Spinning) {
                showDebugOutcomeDialog()
            }
        }
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.spinResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                it.onSuccess { spinResult ->
                    // Show bottom sheet with result
                    showResultBottomSheet(spinResult)
                    viewModel.resetSpinResult()
                }
                it.onFailure { error ->
                    Log.e("JackpotFragment", "API Error: ${error.message}")
                    val message = error.message ?: "Something went wrong, please try again."
                    NoSpinsBottomSheet(message) {
                        findNavController().popBackStack()
                    }.show(parentFragmentManager, "NoSpinsBottomSheet")
                    viewModel.resetSpinResult()
                    resetToReady()
                    userSharedViewModel.triggerDataRefresh()
                }
            }
        }
    }

    private fun updateUI(state: JackpotState) {
        binding.btnSpin.isEnabled = state !is JackpotState.Spinning

        when (state) {
            is JackpotState.ReadyToSpin -> {
                binding.jackpotHeder.setImageResource(R.drawable.jackpot_default_text_img)
                binding.btnSpin.text = "Tap to Spin"
                binding.btnSpin.setBackgroundResource(R.drawable.gold_view_spin)
                binding.slotOneImgView.setImageResource(R.drawable.default_slot_1)
                binding.slotTwoImgView.setImageResource(R.drawable.default_slot_2)
                binding.slotThreeImgView.setImageResource(R.drawable.default_slot_3)
                binding.armImgView.setImageResource(R.drawable.arm_sequence_1)
                binding.winBackgroundVideo.visibility = View.GONE
                setSpinButtonWidth(206)
            }

            is JackpotState.Spinning -> {
                binding.jackpotHeder.setImageResource(R.drawable.jackpot_default_text_img)
                binding.btnSpin.text = "Spinning..."
                binding.btnSpin.setBackgroundResource(R.drawable.bg_box)
                setSpinButtonWidth(206)
                binding.btnSpin.setTextColor(
                    ContextCompat.getColor(binding.btnSpin.context, android.R.color.white)
                )
            }

            is JackpotState.Win -> {
                binding.jackpotHeder.setImageResource(R.drawable.jackpot_win_text_img)
                binding.btnSpin.text = "Claim Your Reward"
                binding.btnSpin.setBackgroundResource(R.drawable.gold_view_spin)
                showWinVideo()
                setSpinButtonWidth(298)
                binding.btnSpin.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.blue_txt_color))
                SoundManager.playSound(requireContext(), R.raw.winning_sound)
            }

            is JackpotState.Lose -> {
                binding.jackpotHeder.setImageResource(R.drawable.jackpot_lose_text_img)
                binding.btnSpin.text = "Continue"
                setSpinButtonWidth(206)
                binding.btnSpin.setBackgroundResource(R.drawable.gold_view_spin)
                SoundManager.playSound(requireContext(), R.raw.loose_sound)
            }
        }
    }

    private fun startSpin() {
        currentState = JackpotState.Spinning
        spinSlots()
    }

    // Define outcome probabilities (must total 1.0)
    private val outcomes by lazy {
        listOf(
            listOf(Symbol.TICKET, Symbol.TICKET, Symbol.TICKET) to 0.10,
            listOf(Symbol.GEM, Symbol.GEM, Symbol.GEM) to 0.05,
            listOf(Symbol.PLAY, Symbol.PLAY, Symbol.PLAY) to 0.05,
            listOf(Symbol.TICKET, Symbol.GEM, Symbol.TICKET) to 0.20,
            listOf(Symbol.GEM, Symbol.TICKET, Symbol.GEM) to 0.30,
            listOf(Symbol.PLAY, Symbol.ANY, Symbol.ANY) to 0.20,
            listOf(Symbol.NO_MATCH) to 0.10
        )
    }

    private fun <T> pickWeighted(items: List<Pair<T, Double>>): T {
        val rnd = kotlin.random.Random.nextDouble()
        var cumulative = 0.0

        for ((value, weight) in items) {
            cumulative += weight
            if (rnd < cumulative) return value
        }
        return items.last().first
    }

    private fun spinSlots() {
        val selectedPattern = debugSelectedPattern ?: pickWeighted(outcomes)
        results.clear()

        if (selectedPattern == listOf(Symbol.NO_MATCH)) {
            // Ensure random combination with no identical symbols
            do {
                results = MutableList(3) { slotOptions.random() }
            } while (results.all { it == results.first() })
        } else {
            for (symbol in selectedPattern) {
                if (symbol == Symbol.ANY) {
                    results.add(slotOptions.random())
                } else {
                    results.add(symbol)
                }
            }
        }

        // Start animations
        finishedCount = 0
        SoundManager.playSound(requireContext(), R.raw.slot_arm_sound)
        SoundManager.playSound(requireContext(), R.raw.slot_spinning_sound)

        val totalDuration = 7000L // 7 seconds

        // Calculate frame durations
        val slot1FrameCount = 239 - 45 + 1
        val slot2FrameCount = 259 - 45 + 1
        val slot3FrameCount = 279 - 45 + 1

        val slot1FrameDuration = totalDuration / slot1FrameCount
        val slot2FrameDuration = totalDuration / slot2FrameCount
        val slot3FrameDuration = totalDuration / slot3FrameCount

        // Play arm animation
        binding.armImgView.playImageSequenceStreamed(
            basePath = "Slots/Slot_Arm",
            baseName = "slot_arm",
            frameRange = 45..64,
            frameDuration = 50
        ) {
            // Arm animation complete
        }

        // Play slot animations
        binding.slotOneImgView.playImageSequenceStreamed(
            basePath = "Slots/slot_1_${results[0].value}",
            baseName = "slot_1_${results[0].value}",
            frameRange = 45..239,
            frameDuration = slot1FrameDuration
        ) {
            checkWinnerIfAllFinished()
        }

        binding.slotTwoImgView.playImageSequenceStreamed(
            basePath = "Slots/slot_2_${results[1].value}",
            baseName = "slot_2_${results[1].value}",
            frameRange = 45..259,
            frameDuration = slot2FrameDuration
        ) {
            checkWinnerIfAllFinished()
        }

        binding.slotThreeImgView.playImageSequenceStreamed(
            basePath = "Slots/slot_3_${results[2].value}",
            baseName = "slot_3_${results[2].value}",
            frameRange = 45..279,
            frameDuration = slot3FrameDuration
        ) {
            checkWinnerIfAllFinished()
        }
    }

    private fun checkWinnerIfAllFinished() {
        finishedCount++
        if (finishedCount == 3) {
            finishedCount = 0
            SoundManager.stopSound(R.raw.slot_spinning_sound)

            // Delay slightly before checking winner
            binding.root.postDelayed({
                checkWinner()
            }, 300)
        }
    }

    private fun checkWinner() {
        // For now, always treat as win (matching iOS logic where all outcomes are wins)
        currentState = JackpotState.Win
    }

    private var winPlayer: ExoPlayer? = null

    private fun showWinVideo() {
        // Show PlayerView
        binding.winBackgroundVideo.visibility = View.VISIBLE

        // Create player (only once)
        if (winPlayer == null) {
            winPlayer = ExoPlayer.Builder(requireContext()).build()
            binding.winBackgroundVideo.player = winPlayer
        }

        val mediaItem = MediaItem.fromUri(
            "android.resource://${requireContext().packageName}/${R.raw.win_background}"
        )


        winPlayer?.apply {
            setMediaItem(mediaItem)
            repeatMode = Player.REPEAT_MODE_ALL
            prepare()
            playWhenReady = true
        }
    }



    private fun saveSpinResult() {
        viewModel.saveSpinResult(
            column1 = results[0].value,
            column2 = results[1].value,
            column3 = results[2].value
        )
    }

    private fun showResultBottomSheet(spinResult: SpinResult) {
        val bottomSheet = JackpotResultBottomSheet.newInstance(spinResult) {
            // On dismiss callback
            userSharedViewModel.triggerDataRefresh()
            if (spinResult.userBalance.spinsRemaining > 0) {
                resetToReady()
            } else {
                findNavController().popBackStack()
            }
        }
        bottomSheet.show(childFragmentManager, "JackpotResultBottomSheet")
    }

    private fun resetToReady() {
        cancelAllAnimations()
        SoundManager.stopAll()

        winPlayer?.stop()
        binding.winBackgroundVideo.visibility = View.GONE

        currentState = JackpotState.ReadyToSpin
    }


    private fun cancelAllAnimations() {
        binding.slotOneImgView.cancelImageSequenceStreamed()
        binding.slotTwoImgView.cancelImageSequenceStreamed()
        binding.slotThreeImgView.cancelImageSequenceStreamed()
        binding.armImgView.cancelImageSequenceStreamed()
    }

    private fun showDebugOutcomeDialog() {
        val options = arrayOf(
            "🎟️ Ticket x3",
            "💎 Gem x3",
            "▶️ Play Icon x3",
            "🎟️-💎-🎟️",
            "💎-🎟️-💎",
            "▶️ + Any + Any",
            "❌ No Match",
            "🎲 Random"
        )

        AlertDialog.Builder(requireContext())
            .setTitle("Select Outcome")
            .setItems(options) { _, which ->
                resetToReady()
                debugSelectOutcome(which + 1)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun debugSelectOutcome(index: Int) {
        debugSelectedPattern = when (index) {
            1 -> listOf(Symbol.TICKET, Symbol.TICKET, Symbol.TICKET)
            2 -> listOf(Symbol.GEM, Symbol.GEM, Symbol.GEM)
            3 -> listOf(Symbol.PLAY, Symbol.PLAY, Symbol.PLAY)
            4 -> listOf(Symbol.TICKET, Symbol.GEM, Symbol.TICKET)
            5 -> listOf(Symbol.GEM, Symbol.TICKET, Symbol.GEM)
            6 -> listOf(Symbol.PLAY, Symbol.ANY, Symbol.ANY)
            7 -> listOf(Symbol.NO_MATCH)
            8 -> null // Random outcome
            else -> null
        }
    }
    private fun setSpinButtonWidth(dp: Int) {
        val params = binding.btnSpin.layoutParams
        params.width = dpToPx(dp)
        binding.btnSpin.layoutParams = params
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }


    override fun onDestroyView() {
        super.onDestroyView()

        cancelAllAnimations()
        SoundManager.release()

        winPlayer?.stop()
        winPlayer?.release()
        winPlayer = null

        _binding = null
    }

}

// MARK: - Jackpot States
sealed class JackpotState {
    object ReadyToSpin : JackpotState()
    object Spinning : JackpotState()
    object Win : JackpotState()
    object Lose : JackpotState()
}