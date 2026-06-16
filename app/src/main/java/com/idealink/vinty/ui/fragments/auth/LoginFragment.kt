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
import com.idealink.vinty.data.DataManager
import com.idealink.vinty.data.model.GoogleLoginRequest
import com.idealink.vinty.data.model.LoginRequest
import com.idealink.vinty.data.model.LoginResponse
import com.idealink.vinty.databinding.FragmentLoginBinding
import com.idealink.vinty.ui.activities.AccountActivity
import com.idealink.vinty.ui.viewmodel.AuthViewModel
import com.idealink.vinty.ui.viewmodel.ViewModelFactory
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.idealink.vinty.utils.AnalyticsEvent
import com.idealink.vinty.utils.AnalyticsManager
import com.idealink.vinty.utils.FacebookAnalyticsManager
import com.idealink.vinty.utils.FacebookEvent
import com.idealink.vinty.utils.applyTypingFont
import com.idealink.vinty.utils.refreshTypingFont
import com.idealink.vinty.utils.GoogleSignInHelper
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by viewModels<AuthViewModel> {
        val app = requireActivity().application as App
        ViewModelFactory(app.repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeViewModel()
        checkLoginStatus()
    }

    private var isGoogleSignInInProgress = false

    private fun handleGoogleSignIn() {
        if (isGoogleSignInInProgress) return
        isGoogleSignInInProgress = true
        binding.progressBar.visibility = View.VISIBLE
        binding.googleBtn.isEnabled = false
        binding.SignButton.isEnabled = false

        GoogleSignInHelper.signIn(
            fragment = this,
            onSuccess = { idToken ->
                authViewModel.googleLogin(
                    GoogleLoginRequest(idToken = idToken)
                )
            },
            onError = { errorMsg ->
                isGoogleSignInInProgress = false
                binding.progressBar.visibility = View.GONE
                binding.googleBtn.isEnabled = true
                binding.SignButton.isEnabled = true
                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun setupUI() {
        applyTypingFont(binding.editTextTextEmailAddress)
        applyTypingFont(binding.editTextTextPassword)
        binding.editTextTextEmailAddress.setBackgroundResource(0)
        binding.editTextTextEmailAddress.background = null

        var isPasswordVisible = false
        binding.imageButton.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            binding.editTextTextPassword.inputType = if (isPasswordVisible) {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            binding.imageButton.setImageResource(
                if (isPasswordVisible) R.drawable.show_pass_icon else R.drawable.eye_logo
            )
            refreshTypingFont(binding.editTextTextPassword)
            binding.editTextTextPassword.setSelection(binding.editTextTextPassword.text?.length ?: 0)
        }

        binding.SignButton.setOnClickListener {
            if (authViewModel.isLoading.value || isGoogleSignInInProgress) return@setOnClickListener

            val emailOrUsername = binding.editTextTextEmailAddress.text.toString().trim()
            val password = binding.editTextTextPassword.text.toString().trim()

            if (emailOrUsername.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show()
            } else {
                authViewModel.login(LoginRequest(emailOrUsername, password))
            }
        }

        binding.createAccountBtn.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_createAccountFragment)
        }

        binding.fogotPassBtn.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_forgotPasswordCodeFragment)
        }

        binding.googleBtn.setOnClickListener {
            handleGoogleSignIn()
        }
    }

    private fun observeViewModel() {
        // Observe Loading State
        viewLifecycleOwner.lifecycleScope.launch {
            authViewModel.isLoading.collect { isLoading ->
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                binding.SignButton.isEnabled = !isLoading
                binding.googleBtn.isEnabled = !isLoading
                if (!isLoading) isGoogleSignInInProgress = false
            }
        }

        // Observe Error State
        viewLifecycleOwner.lifecycleScope.launch {
            authViewModel.error.collect { error ->
                error?.let {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                    authViewModel.clearError()
                }
            }
        }

        // Observe Login Result
        viewLifecycleOwner.lifecycleScope.launch {
            authViewModel.loginResult.collect { response ->
                response?.let {
                    handleLoginResponse(it)
                    authViewModel.clearLoginResult()
                }
            }
        }
    }

    private fun handleLoginResponse(response: LoginResponse) {
        val user = response.user
        val dataManager = DataManager.getInstance()

        if (user == null) {
            Toast.makeText(requireContext(), "Invalid login response", Toast.LENGTH_SHORT).show()
            return
        }

        // Save tokens once user is valid
        dataManager.saveUser(user)
        dataManager.saveTokens(response.accessToken, response.refreshToken)

        // Track login event
        AnalyticsManager.log(AnalyticsEvent.LOGIN)
        FacebookAnalyticsManager.logEvent(FacebookEvent.LOGIN)

        // Register device for push notifications if enabled
        registerDeviceIfNeeded()

        // Always go to Home
        (requireActivity() as? AccountActivity)?.navigateToMainActivity()
    }


    private fun registerDeviceIfNeeded() {
        val dataManager = DataManager.getInstance()
        if (!dataManager.isNotificationsEnabled()) return

        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            dataManager.saveFcmToken(token)
            val deviceId = Settings.Secure.getString(requireContext().contentResolver, Settings.Secure.ANDROID_ID)
            val appVersion = try {
                requireContext().packageManager.getPackageInfo(requireContext().packageName, 0).versionName ?: "1.0"
            } catch (_: Exception) { "1.0" }

            val request = mapOf(
                "fcmToken" to token,
                "deviceType" to "MOBILE",
                "deviceId" to deviceId,
                "platform" to "Android ${Build.VERSION.RELEASE}",
                "appVersion" to appVersion
            )

            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val app = requireActivity().application as App
                    app.repository.registerDevice(request)
                    Log.d("LoginFragment", "Device registered after login")
                } catch (e: Exception) {
                    Log.e("LoginFragment", "Failed to register device: ${e.message}")
                }
            }
        }
    }

    private fun checkLoginStatus() {
        if (DataManager.getInstance().isLoggedIn()) {
            (requireActivity() as? AccountActivity)?.navigateToMainActivity()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}