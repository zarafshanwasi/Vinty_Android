package com.idealink.vinty.ui.fragments.auth

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.idealink.vinty.App
import com.idealink.vinty.R
import com.idealink.vinty.data.model.ForgotPasswordVerifyOtpRequest
import com.idealink.vinty.databinding.FragmentForgotCreatePasswordBinding
import com.idealink.vinty.ui.viewmodel.AuthActionResult
import com.idealink.vinty.ui.viewmodel.AuthViewModel
import com.idealink.vinty.ui.viewmodel.ViewModelFactory
import com.idealink.vinty.utils.applyTypingFont
import com.idealink.vinty.utils.refreshTypingFont
import kotlinx.coroutines.launch

class ForgotCreatePasswordFragment : Fragment() {

    private var _binding: FragmentForgotCreatePasswordBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by viewModels {
        val app = requireActivity().application as App
        ViewModelFactory(app.repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentForgotCreatePasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        applyTypingFont(binding.newPassTxt)
        applyTypingFont(binding.confirmPassTxt)

        binding.newPassTxt.setBackgroundResource(0)
        binding.newPassTxt.background = null
        binding.confirmPassTxt.setBackgroundResource(0)
        binding.confirmPassTxt.background = null

        observeViewModel()

        // ... eye button logic ...
        setupEyeButtons()

        val email = arguments?.getString("email") ?: ""
        val otp = arguments?.getString("otp") ?: ""

        if (email.isEmpty() || otp.isEmpty()) {
            Toast.makeText(requireContext(), "Missing email or OTP. Please try again.", Toast.LENGTH_LONG).show()
            findNavController().popBackStack()
            return
        }

        binding.sendBtn.setOnClickListener {
            val newPass = binding.newPassTxt.text.toString().trim()
            val confirmPass = binding.confirmPassTxt.text.toString().trim()

            when {
                newPass.isEmpty() || confirmPass.isEmpty() -> {
                    Toast.makeText(requireContext(), "All fields are required", Toast.LENGTH_SHORT).show()
                }
                newPass != confirmPass -> {
                    Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
                }
                newPass.length < 6 -> {
                    Toast.makeText(requireContext(), "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    authViewModel.forgotPasswordVerifyOtp(ForgotPasswordVerifyOtpRequest(email, otp, newPass))
                }
            }
        }

        binding.backBtn3.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupEyeButtons() {
        var isPasswordVisible = false
        binding.eyeBtn.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            binding.newPassTxt.inputType = if (isPasswordVisible) {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            binding.eyeBtn.setImageResource(
                if (isPasswordVisible) R.drawable.show_pass_icon else R.drawable.eye_logo
            )
            refreshTypingFont(binding.newPassTxt)
            binding.newPassTxt.setSelection(binding.newPassTxt.text?.length ?: 0)
        }

        var isConfirmPasswordVisible = false
        binding.eyeBtn2.setOnClickListener {
            isConfirmPasswordVisible = !isConfirmPasswordVisible
            binding.confirmPassTxt.inputType = if (isConfirmPasswordVisible) {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            binding.eyeBtn2.setImageResource(
                if (isConfirmPasswordVisible) R.drawable.show_pass_icon else R.drawable.eye_logo
            )
            refreshTypingFont(binding.confirmPassTxt)
            binding.confirmPassTxt.setSelection(binding.confirmPassTxt.text?.length ?: 0)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            authViewModel.isLoading.collect { isLoading ->
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                binding.sendBtn.isEnabled = !isLoading
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
                if (result is AuthActionResult.ResetPasswordSuccess) {
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                    authViewModel.clearAuthActionResult()
                    findNavController().navigate(R.id.action_forgotCreatePasswordFragment_to_loginFragment)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}