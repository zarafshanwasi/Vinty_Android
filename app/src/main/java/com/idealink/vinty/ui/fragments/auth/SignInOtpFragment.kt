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
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.idealink.vinty.App
import com.idealink.vinty.R
import com.idealink.vinty.data.model.ResendVerificationEmailRequest
import com.idealink.vinty.data.model.VerifyEmailOtpRequest
import com.idealink.vinty.databinding.FragmentSignInOtpBinding
import com.idealink.vinty.ui.viewmodel.AuthActionResult
import com.idealink.vinty.ui.viewmodel.AuthViewModel
import com.idealink.vinty.ui.viewmodel.UserSharedViewModel
import com.idealink.vinty.ui.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch

class SignInOtpFragment : Fragment() {

    companion object {
        const val SOURCE_SIGNUP = "signup"
        const val SOURCE_HOME = "home"
    }


    private var _binding: FragmentSignInOtpBinding? = null
    private val binding get() = _binding!!
    private var email: String? = null
    private var source: String = SOURCE_SIGNUP


    private val authViewModel: AuthViewModel by viewModels {
        val app = requireActivity().application as App
        ViewModelFactory(app.repository)
    }
    private val userSharedViewModel: UserSharedViewModel by activityViewModels {
        val app = requireActivity().application as App
        ViewModelFactory(app.repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        email = arguments?.getString("email")
        source = arguments?.getString("source", SOURCE_SIGNUP) ?: SOURCE_SIGNUP
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignInOtpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupOtpInputs()
        observeViewModel()

        binding.backBtn.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.skipBtn.visibility =
            if (source == SOURCE_HOME) View.GONE else View.VISIBLE

        binding.verifyBtn.setOnClickListener {
            val otp = collectOtp()
            if (otp.length == 6) {
                authViewModel.verifyEmailOtp(VerifyEmailOtpRequest(email ?: "", otp))
            } else {
                Toast.makeText(requireContext(), "Please enter all 6 digits", Toast.LENGTH_SHORT).show()
            }
        }

        binding.sendAgainBtn.setOnClickListener {
            email?.let {
                clearOtpBoxes()
                authViewModel.resendVerificationEmail(ResendVerificationEmailRequest(it))
            } ?: Toast.makeText(requireContext(), "Email not found!", Toast.LENGTH_SHORT).show()
        }

        binding.skipBtn.setOnClickListener {
            findNavController().navigate(
                R.id.action_signInOtpFragment_to_inviteFriendRewardFragment
            )
        }

        if (savedInstanceState == null && source == SOURCE_HOME) {
            email?.let {
                authViewModel.resendVerificationEmail(ResendVerificationEmailRequest(it))
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            authViewModel.isLoading.collect { isLoading ->
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                binding.verifyBtn.isEnabled = !isLoading
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
                    is AuthActionResult.VerifyOtpSuccess -> {
                        Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                        authViewModel.clearAuthActionResult()

                        when (source) {
                            SOURCE_HOME -> {
                                userSharedViewModel.loadUser()
                                findNavController().popBackStack()                            }

                            SOURCE_SIGNUP -> {
                                // Existing behavior (DO NOT BREAK)
                                findNavController().navigate(
                                    R.id.action_signInOtpFragment_to_inviteFriendRewardFragment
                                )
                            }
                        }
                    }
                    else -> {
                    }
                }
            }
        }
    }

    private fun setupOtpInputs() {
        val boxes = listOf(
            binding.box1, binding.box2, binding.box3,
            binding.box4, binding.box5, binding.box6
        )

        boxes.forEachIndexed { index, currentBox ->

            //  Forward typing
            currentBox.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s?.length == 1 && index < boxes.lastIndex) {
                        boxes[index + 1].requestFocus()
                    }
                }

                override fun afterTextChanged(s: Editable?) {}
            })

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