package com.idealink.vinty.ui.fragments.auth


import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.idealink.vinty.App
import com.idealink.vinty.R
import com.idealink.vinty.data.model.ForgotPasswordRequest
import com.idealink.vinty.databinding.FragmentForgotPasswordOtpBinding
import com.idealink.vinty.ui.viewmodel.AuthActionResult
import com.idealink.vinty.ui.viewmodel.AuthViewModel
import com.idealink.vinty.ui.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch


class ForgotPasswordFragment : Fragment() {

    private var _binding: FragmentForgotPasswordOtpBinding? = null
    private val binding get() = _binding!!
    private var email: String = ""
    private var otpReceived: String = ""

    private val authViewModel: AuthViewModel by viewModels {
        val app = requireActivity().application as App
        ViewModelFactory(app.repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentForgotPasswordOtpBinding.inflate(inflater, container, false)
        email = arguments?.getString("email") ?: ""
        otpReceived = arguments?.getString("otp") ?: ""

        setupOtpInputs()
        observeViewModel()

        binding.continueBtn.setOnClickListener {
            val otp = collectOtp()
            if (otp.length != 6) {
                Toast.makeText(requireContext(), "Enter valid 6-digit OTP", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (email.isEmpty()) {
                Toast.makeText(requireContext(), "Email is missing", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // manually verify
            if (otpReceived == otp) {
                val bundle = Bundle().apply {
                    putString("email", email)
                    putString("otp", otp)
                }
                findNavController().navigate(
                    R.id.action_forgotPasswordFragment_to_forgotCreatePasswordFragment,
                    bundle
                )
            } else {
                Toast.makeText(requireContext(), "The otp is invalid!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.sendAgainBtn.setOnClickListener {
            if (email.isEmpty()) {
                Toast.makeText(requireContext(), "Email is missing", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            clearOtpBoxes()
            authViewModel.forgotPassword(ForgotPasswordRequest(email))
        }

        binding.backBtn.setOnClickListener {
            findNavController().popBackStack()
        }

        return binding.root
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
                    is AuthActionResult.ForgotPasswordSent -> {
                        otpReceived = result.otp
                        clearOtpBoxes()
                        Toast.makeText(requireContext(), "OTP resent successfully", Toast.LENGTH_SHORT).show()
                        authViewModel.clearAuthActionResult()
                    }
                    else -> {}
                }
            }
        }
    }

    /** Smart OTP typing: Auto move, auto delete, and paste support */
    private fun setupOtpInputs() {
        val boxes = listOf(
            binding.box1, binding.box2, binding.box3,
            binding.box4, binding.box5, binding.box6
        )

        boxes.forEachIndexed { index, currentBox ->

            currentBox.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s?.length == 1 && index < boxes.lastIndex) {
                        boxes[index + 1].requestFocus()
                    }
                }

                override fun afterTextChanged(s: Editable?) {}
            })

            //  Backspace handling (KEY FIX)
            currentBox.setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_DEL &&
                    event.action == KeyEvent.ACTION_DOWN &&
                    currentBox.text.isNullOrEmpty()
                ) {
                    handleBackspace(index, boxes)
                } else {
                    false
                }
            }

            // Paste support
            currentBox.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus && (currentBox.text?.length ?: 0) > 1) {
                    val pastedText = currentBox.text.toString()
                    if (pastedText.length == boxes.size) {
                        pastedText.forEachIndexed { i, c ->
                            boxes[i].setText(c.toString())
                        }
                        boxes.last().clearFocus()
                    }
                }
            }
        }
    }


    private fun collectOtp(): String {
        val boxes = listOf(
            binding.box1, binding.box2, binding.box3,
            binding.box4, binding.box5, binding.box6
        )
        return boxes.joinToString("") { it.text?.toString()?.trim() ?: "" }
    }
    private fun clearOtpBoxes() {
        val boxes = listOf(
            binding.box1, binding.box2, binding.box3,
            binding.box4, binding.box5, binding.box6
        )

        boxes.forEach { it.setText("") }
        boxes.first().requestFocus()
    }
    private fun handleBackspace(
        currentIndex: Int,
        boxes: List<EditText>
    ): Boolean {
        if (currentIndex > 0) {
            boxes[currentIndex - 1].apply {
                requestFocus()
                setSelection(text.length)
            }
        }
        return true
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
