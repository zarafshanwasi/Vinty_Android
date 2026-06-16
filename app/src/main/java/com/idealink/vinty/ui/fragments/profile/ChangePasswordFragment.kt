package com.idealink.vinty.ui.fragments.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.idealink.vinty.App
import com.idealink.vinty.databinding.FragmentChangePasswordBinding
import com.idealink.vinty.ui.viewmodel.ChangePasswordViewModel
import com.idealink.vinty.ui.viewmodel.ViewModelFactory
import android.widget.ImageButton
import com.idealink.vinty.R
import com.idealink.vinty.utils.applyTypingFont
import com.idealink.vinty.utils.refreshTypingFont
import kotlinx.coroutines.launch

class ChangePasswordFragment : Fragment() {

    private var _binding: FragmentChangePasswordBinding? = null
    private val binding get() = _binding!!
    private var isOldPasswordVisible = false
    private var isNewPasswordVisible = false


    private val viewModel: ChangePasswordViewModel by viewModels {
        val app = requireActivity().application as App
        ViewModelFactory(app.repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChangePasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClicks()
        observeViewModel()
    }

    private fun setupClicks() {

        applyTypingFont(binding.newPassTxt)
        applyTypingFont(binding.newPasswordTxt)

        // Back button
        binding.backBtn3.setOnClickListener {
            findNavController().navigateUp()
        }

        // Old password eye
        binding.eyeBtn.setOnClickListener {
            isOldPasswordVisible = !isOldPasswordVisible
            togglePasswordVisibility(binding.newPassTxt, binding.eyeBtn, isOldPasswordVisible)
        }

        // New password eye
        binding.eyeBtn2.setOnClickListener {
            isNewPasswordVisible = !isNewPasswordVisible
            togglePasswordVisibility(binding.newPasswordTxt, binding.eyeBtn2, isNewPasswordVisible)
        }

        // Submit
        binding.sendBtn.setOnClickListener {

            val oldPass = binding.newPassTxt.text.toString().trim()
            val newPass = binding.newPasswordTxt.text.toString().trim()

            if (oldPass.isEmpty()) {
                toast("Enter current password")
                return@setOnClickListener
            }

            if (newPass.length < 6) {
                toast("Password must be at least 6 characters")
                return@setOnClickListener
            }

            viewModel.changePassword(oldPass, newPass)
        }
    }

    private fun observeViewModel() {

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loading.collect { loading ->
                binding.progressBar.visibility =
                    if (loading) View.VISIBLE else View.GONE
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.result.collect { result ->
                result ?: return@collect

                result
                    .onSuccess {
                        toast(it)
                        findNavController().navigateUp()
                    }
                    .onFailure {
                        toast(it.message ?: "Something went wrong")
                    }
            }
        }
    }

    private fun toast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }
    private fun togglePasswordVisibility(
        editText: android.widget.EditText,
        eyeButton: ImageButton,
        isVisible: Boolean
    ) {
        editText.inputType =
            if (isVisible)
                android.text.InputType.TYPE_CLASS_TEXT or
                        android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            else
                android.text.InputType.TYPE_CLASS_TEXT or
                        android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD

        eyeButton.setImageResource(
            if (isVisible) R.drawable.show_pass_icon else R.drawable.eye_logo
        )
        refreshTypingFont(editText)
        editText.setSelection(editText.text.length)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
