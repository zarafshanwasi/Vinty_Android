package com.idealink.vinty.ui.fragments.auth

import android.os.Bundle
import android.util.Patterns
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
import com.idealink.vinty.data.model.ForgotPasswordRequest
import com.idealink.vinty.databinding.FragmentForgotPasswordCodeBinding
import com.idealink.vinty.ui.viewmodel.AuthActionResult
import com.idealink.vinty.ui.viewmodel.AuthViewModel
import com.idealink.vinty.ui.viewmodel.ViewModelFactory
import com.idealink.vinty.utils.applyTypingFont
import kotlinx.coroutines.launch

class ForgotPasswordCodeFragment : Fragment() {

    private var _binding: FragmentForgotPasswordCodeBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by viewModels {
        val app = requireActivity().application as App
        ViewModelFactory(app.repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentForgotPasswordCodeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        applyTypingFont(binding.emailText)
        binding.emailText.setBackgroundResource(0)
        binding.emailText.background = null

        observeViewModel()

        binding.sendBtn.setOnClickListener {
            val email = binding.emailText.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter your email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(requireContext(), "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            authViewModel.forgotPassword(ForgotPasswordRequest(email))
        }

        binding.backBtn3.setOnClickListener {
            findNavController().popBackStack()
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
                if (result is AuthActionResult.ForgotPasswordSent) {
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                    authViewModel.clearAuthActionResult()
                    
                    val email = binding.emailText.text.toString().trim()
                    val bundle = Bundle().apply {
                        putString("email", email)
                        putString("otp", result.otp)
                    }

                    findNavController().navigate(
                        R.id.action_forgotPasswordCodeFragment_to_forgotPasswordFragment,
                        bundle
                    )
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}