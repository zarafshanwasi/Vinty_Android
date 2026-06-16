package com.idealink.vinty.ui.fragments.auth

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.idealink.vinty.App
import com.idealink.vinty.data.DataManager
import com.idealink.vinty.data.model.ApplyInviteCodeRequest
import com.idealink.vinty.databinding.FragmentInviteFriendRewardBinding
import com.idealink.vinty.ui.activities.AccountActivity
import com.idealink.vinty.ui.viewmodel.AuthActionResult
import com.idealink.vinty.ui.viewmodel.AuthViewModel
import com.idealink.vinty.ui.viewmodel.UserSharedViewModel
import com.idealink.vinty.ui.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch
import kotlin.getValue

class InviteApplyCodeFragment : Fragment() {

    private var _binding: FragmentInviteFriendRewardBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by viewModels {
        val app = requireActivity().application as App
        ViewModelFactory(app.repository)
    }

    private val userSharedViewModel: UserSharedViewModel by activityViewModels {
        val app = requireActivity().application as App
        ViewModelFactory(app.repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentInviteFriendRewardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClicks()
        setupOtpInputs()
        observeViewModel()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            authViewModel.isLoading.collect { isLoading ->
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                binding.continueBtn.isEnabled = !isLoading
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            authViewModel.error.collect { error ->
                error?.let {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                    authViewModel.clearError()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            authViewModel.authActionResult.collect { result ->
                when (result) {
                    is AuthActionResult.ApplyInviteCodeSuccess -> {
                        Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()

                        userSharedViewModel.rewardGain = result.reward
                        authViewModel.clearAuthActionResult()
                        checkLoginStatus()
                    }
                    else -> {}
                }
            }
        }
    }

    private fun checkLoginStatus() {
        if (DataManager.getInstance().isLoggedIn()) {
            (requireActivity() as? AccountActivity)?.navigateToMainActivity()
        }
    }

    // CLICK HANDLERS
    private fun setupClicks() {

        // Back → navigate up
        binding.backBtn.setOnClickListener {
            findNavController().navigateUp()
        }

        // Skip → Home
        binding.skipBtn.setOnClickListener {
            checkLoginStatus()
        }

        // Continue → Apply Invite Code
        binding.continueBtn.setOnClickListener {
            applyInviteCode()
        }
    }

    /** Smart OTP typing: Auto move, auto delete, and paste support */
    private fun setupOtpInputs() {
        val boxes = listOf(
            binding.box1, binding.box2, binding.box3,
            binding.box4, binding.box5, binding.box6
        )

        for (i in boxes.indices) {
            val currentBox = boxes[i]

            currentBox.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s?.length == 1 && i < boxes.size - 1) {
                        boxes[i + 1].requestFocus()
                    } else if (s?.isEmpty() == true && i > 0) {
                        boxes[i - 1].requestFocus()
                    }
                }

                override fun afterTextChanged(s: Editable?) {}
            })

            //  Paste full OTP
            currentBox.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus && (currentBox.text?.length ?: 0) > 1) {
                    val pastedText = currentBox.text.toString()
                    if (pastedText.length == boxes.size) {
                        pastedText.forEachIndexed { index, c ->
                            boxes[index].setText(c.toString())
                        }
                        boxes.last().clearFocus()
                    }
                }
            }
        }
    }


    private fun getInviteCode(): String {
        return listOf(
            binding.box1,
            binding.box2,
            binding.box3,
            binding.box4,
            binding.box5,
            binding.box6
        ).joinToString("") { it.text.toString().trim() }
    }
    private fun applyInviteCode() {
        val code = getInviteCode()

        if (code.length != 6) {
            Toast.makeText(requireContext(), "Enter valid invite code", Toast.LENGTH_SHORT).show()
            return
        }

        authViewModel.applyInviteCode(ApplyInviteCodeRequest(inviteCode = code))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
