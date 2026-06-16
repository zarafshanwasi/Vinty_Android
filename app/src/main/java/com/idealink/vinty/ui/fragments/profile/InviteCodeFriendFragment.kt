package com.idealink.vinty.ui.fragments.profile

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.idealink.vinty.App
import com.idealink.vinty.R
import com.idealink.vinty.databinding.FragmentInviteFriendCodeBinding
import com.idealink.vinty.ui.viewmodel.UserSharedViewModel
import com.idealink.vinty.ui.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch

class InviteCodeFragment : Fragment() {

    private var _binding: FragmentInviteFriendCodeBinding? = null
    private val binding get() = _binding!!

    private var inviteCode = ""
    private var inviteLink = ""

    private val userSharedViewModel: UserSharedViewModel by activityViewModels {
        val app = requireActivity().application as App
        ViewModelFactory(app.repository)
    }

    // ----------------------------------------------------
    // Lifecycle
    // ----------------------------------------------------

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInviteFriendCodeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupClicks()
        observeViewModels()
        adjustOtpBoxWidths()

        fetchInviteCode()

        binding.backBtn4.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ----------------------------------------------------
    // Setup (NO collectors)
    // ----------------------------------------------------

    private fun setupUI() {
        binding.profileHeader.setOnProfileClickListener {
            findNavController().navigate(R.id.action_inviteCodeFragment_to_profileFragment)
        }
    }

    private fun setupClicks() {
        binding.box7.setOnClickListener {
            if (inviteCode.isNotEmpty()) {
                copyToClipboard("🎁 Join the app! Use my code $inviteCode in the app.")
                Toast.makeText(requireContext(), "Code copied", Toast.LENGTH_SHORT).show()
            }
        }

        binding.copyBtn.setOnClickListener {
            if (inviteLink.isNotEmpty()) {
                copyToClipboard("🎁 Join the app! Use my link $inviteLink in the app.")
                Toast.makeText(requireContext(), "Link copied", Toast.LENGTH_SHORT).show()
            }
        }

        binding.copyCodeBtn.setOnClickListener {
            if (inviteCode.isNotEmpty()) {
                shareText("🎁 Join the app! Use my code $inviteCode in the app.")
            }
        }

        binding.copyLinkBtn.setOnClickListener {
            if (inviteLink.isNotEmpty()) {
                shareText("🎁 Join the app! Use my link $inviteLink in the app.")
            }
        }
    }

    // ----------------------------------------------------
    // Observers (ALL flows here)
    // ----------------------------------------------------

    private fun observeViewModels() {

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                // User (StateFlow replay)
                launch {
                    userSharedViewModel.user.collect { user ->
                        user?.let(binding.profileHeader::updateProfile)
                    }
                }

                // Badge stats
                launch {
                    userSharedViewModel.badgeStats.collect { response ->
                        response?.data?.overall?.let {
                            binding.profileHeader.updateBadgeStats(
                                unlocked = it.unlocked,
                                locked = it.locked
                            )
                        }
                    }
                }

                // Invite Code
                launch {
                    userSharedViewModel.inviteCode.collect { code ->
                        code?.let {
                            inviteCode = it
                            inviteLink = "https://vinty.app/invite/$inviteCode"
                            showCode(inviteCode)
                            binding.copyBtn.text = inviteLink
                        }
                    }
                }
            }
        }
    }

    // ----------------------------------------------------
    // Invite logic (unchanged)
    // ----------------------------------------------------

    private fun fetchInviteCode() {
        userSharedViewModel.fetchInviteCode()
    }

    // ----------------------------------------------------

    private fun showCode(code: String) {
        val boxes = listOf(binding.box1, binding.box2, binding.box3, binding.box4, binding.box5, binding.box6)

        boxes.forEachIndexed { index, editText ->
            editText.setText(code.getOrNull(index)?.toString() ?: "")
            editText.isEnabled = false
        }
    }

    private fun copyToClipboard(text: String) {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("invite", text))
    }

    private fun shareText(text: String) {
        startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }, "Share via"))
    }

    private fun adjustOtpBoxWidths() {
        binding.otpContainer.post {
            val boxCount = 7
            val gapDp = 4
            val density = resources.displayMetrics.density
            val containerWidth = binding.otpContainer.width
            if (containerWidth == 0)
                return@post
            val gapPx = (gapDp * density).toInt() * (boxCount - 1)
            val availableWidth = containerWidth - gapPx
            val boxWidth = availableWidth / boxCount
            val boxes = listOf( binding.box1, binding.box2, binding.box3, binding.box4, binding.box5, binding.box6, binding.box7 )
            boxes.forEach { box ->
                val params = box.layoutParams as LinearLayout.LayoutParams
                params.width = boxWidth
                box.layoutParams = params
            }
        }
    }
}
