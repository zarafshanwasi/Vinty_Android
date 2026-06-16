package com.idealink.vinty.ui.fragments.auth

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.idealink.vinty.App
import com.idealink.vinty.R
import com.idealink.vinty.data.DataManager
import com.idealink.vinty.data.model.GoogleLoginRequest
import com.idealink.vinty.data.model.LoginResponse
import com.idealink.vinty.data.model.RegisterRequest
import com.idealink.vinty.databinding.FragmentCreateAccountBinding
import com.idealink.vinty.ui.activities.AccountActivity
import com.idealink.vinty.ui.viewmodel.AuthViewModel
import com.idealink.vinty.ui.viewmodel.ViewModelFactory
import com.idealink.vinty.utils.AnalyticsEvent
import com.idealink.vinty.utils.AnalyticsManager
import com.idealink.vinty.utils.FacebookAnalyticsManager
import com.idealink.vinty.utils.FacebookEvent
import com.idealink.vinty.utils.applyTypingFont
import com.idealink.vinty.utils.refreshTypingFont
import com.idealink.vinty.utils.GoogleSignInHelper
import kotlinx.coroutines.launch

class CreateAccountFragment : Fragment() {

    private var _binding: FragmentCreateAccountBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by viewModels <AuthViewModel> {
        val app = requireActivity().application as App
        ViewModelFactory(app.repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        observeViewModel()
    }

    private fun isValidEmail(email: String): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return email.matches(emailPattern.toRegex())
    }

    private fun isValidUsername(username: String): Boolean {
        // Username must be at least 3 characters and contain only alphanumeric characters and underscores
        return username.length >= 3 && username.matches("^[a-zA-Z0-9_]+$".toRegex())
    }

    private fun isValidPassword(password: String): Boolean {
        // Password must be at least 8 characters and contain at least one uppercase, one lowercase, and one digit
        if (password.length < 8) return false
        val hasUpperCase = password.any { it.isUpperCase() }
        val hasLowerCase = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }
        return hasUpperCase && hasLowerCase && hasDigit
    }

    private fun setupUI() {
        binding.SignButton.isEnabled = false
        binding.SignButton.alpha = 0.5f

        applyTypingFont(binding.usernameTextView)
        applyTypingFont(binding.emailTxtView)
        applyTypingFont(binding.passwrdTxtView)
        applyTypingFont(binding.confirmPasswordTxtView)

        binding.usernameTextView.setBackgroundResource(0)
        binding.usernameTextView.background = null
        binding.emailTxtView.setBackgroundResource(0)
        binding.emailTxtView.background = null
        binding.passwrdTxtView.setBackgroundResource(0)
        binding.passwrdTxtView.background = null
        binding.confirmPasswordTxtView.setBackgroundResource(0)
        binding.confirmPasswordTxtView.background = null

        // Setup focus listeners for scrolling
        setupScrollOnFocus()

        var isChecked = false
        binding.tickBox.setOnClickListener {
            isChecked = !isChecked
            updateSignButtonState(isChecked)
        }
        binding.txtPrivacy.setOnClickListener {
            isChecked = !isChecked
            updateSignButtonState(isChecked)
        }

        setupTermsAndPrivacyLinks()
        setupPasswordVisibility()

        binding.SignButton.setOnClickListener {
            if (authViewModel.isLoading.value || isGoogleSignInInProgress) return@setOnClickListener

            val username = binding.usernameTextView.text.toString().trim()
            val email = binding.emailTxtView.text.toString().trim()
            val password = binding.passwrdTxtView.text.toString().trim()
            val confirmPassword = binding.confirmPasswordTxtView.text.toString().trim()

            // Check if fields are empty
            if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validate email format
            if (!isValidEmail(email)) {
                Toast.makeText(requireContext(), "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validate username
            if (!isValidUsername(username)) {
                Toast.makeText(requireContext(), "Username must be at least 3 characters and contain only letters, numbers, and underscores", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // Validate password strength
            if (!isValidPassword(password)) {
                Toast.makeText(requireContext(), "Password must be at least 8 characters with uppercase, lowercase, and a digit", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // Check if passwords match
            if (password != confirmPassword) {
                Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // All validations passed, proceed with API call
            authViewModel.register(
                RegisterRequest(
                    email = email,
                    password = password,
                    username = username,
                    sendEmailVerification = true
                )
            )
        }


        binding.backBtn.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.haveAnAccountBtn.setOnClickListener {
            findNavController().navigate(R.id.action_createAccountFragment_to_loginFragment)
        }
        
        binding.googleBtn.setOnClickListener {
            handleGoogleSignIn()
        }
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
                binding.SignButton.isEnabled = binding.tickBox.tag == "checked"
                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show()
            }
        )
    }


    private fun observeViewModel() {
        // Observe Loading State
        viewLifecycleOwner.lifecycleScope.launch {
            authViewModel.isLoading.collect { isLoading ->
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                binding.SignButton.isEnabled = !isLoading && binding.tickBox.tag == "checked"
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

        // Observe Register Result
        viewLifecycleOwner.lifecycleScope.launch {
            authViewModel.registerResult.collect { response ->
                response?.let {
                    if (!it.accessToken.isNullOrEmpty() || it.success == true) {
                        val dataManager = DataManager.getInstance()
                        dataManager.saveTokens(response.accessToken, response.refreshToken)

                        // Track register event
                        AnalyticsManager.log(AnalyticsEvent.REGISTER)
                        FacebookAnalyticsManager.logEvent(FacebookEvent.REGISTER)

                        Toast.makeText(requireContext(), "OTP sent to your email!", Toast.LENGTH_SHORT).show()
                        val bundle = Bundle().apply { putString("email", binding.emailTxtView.text.toString().trim()) }
                        findNavController().navigate(R.id.action_createAccountFragment_to_signInOtpFragment, bundle)
                    } else {
                        Toast.makeText(requireContext(), "Registration failed", Toast.LENGTH_SHORT).show()
                    }
                    authViewModel.clearRegisterResult()
                }
            }
        }
        
        // Observe Login Result (for Google Sign In)
        viewLifecycleOwner.lifecycleScope.launch {
            authViewModel.loginResult.collect { response ->
                response?.let {
                    handleLoginResponse(it)
                    authViewModel.clearLoginResult()
                }
            }
        }
    }
    
    private fun handleLoginResponse(response: com.idealink.vinty.data.model.LoginResponse) {
        val user = response.user
        val dataManager = DataManager.getInstance()

        if (user == null) {
            Toast.makeText(requireContext(), "Invalid login response", Toast.LENGTH_SHORT).show()
            return
        }

        // Save tokens once user is valid
        dataManager.saveUser(user)
        dataManager.saveTokens(response.accessToken, response.refreshToken)

        // Track register event (Google Sign-In from registration screen)
        AnalyticsManager.log(AnalyticsEvent.REGISTER)
        FacebookAnalyticsManager.logEvent(FacebookEvent.REGISTER)

        // Navigate to Main Activity
        (requireActivity() as? com.idealink.vinty.ui.activities.AccountActivity)?.navigateToMainActivity()
    }

    private fun setupTermsAndPrivacyLinks() {
        val termsText = "Terms and Conditions"
        val privacyText = "Privacy Policy"
        val fullText = "I have read and agree to the $termsText and $privacyText"
        val spannable = SpannableString(fullText)

        val termsStart = fullText.indexOf(termsText)
        val termsEnd = termsStart + termsText.length
        val privacyStart = fullText.indexOf(privacyText)
        val privacyEnd = privacyStart + privacyText.length

        val termsClickable = object : ClickableSpan() {
            override fun onClick(widget: View) {
                startActivity(Intent(Intent.ACTION_VIEW, "https://www.vinty.win/terms.html".toUri()))
            }
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = true
                ds.color = Color.WHITE
            }
        }

        val privacyClickable = object : ClickableSpan() {
            override fun onClick(widget: View) {
                startActivity(Intent(Intent.ACTION_VIEW, "https://www.vinty.win/privacy.html".toUri()))
            }
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = true
                ds.color = Color.WHITE
            }
        }

        spannable.setSpan(termsClickable, termsStart, termsEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(privacyClickable, privacyStart, privacyEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        binding.txtPrivacy.text = spannable
        binding.txtPrivacy.movementMethod = LinkMovementMethod.getInstance()
        binding.txtPrivacy.highlightColor = Color.TRANSPARENT
    }

    private fun setupPasswordVisibility() {
        var isPasswordVisible = false
        binding.eyeBtn.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            binding.passwrdTxtView.inputType = if (isPasswordVisible) {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            binding.eyeBtn.setImageResource(
                if (isPasswordVisible) R.drawable.show_pass_icon else R.drawable.eye_logo
            )
            refreshTypingFont(binding.passwrdTxtView)
            binding.passwrdTxtView.setSelection(binding.passwrdTxtView.text?.length ?: 0)
        }

        var isConfirmPasswordVisible = false
        binding.eyeBtn2.setOnClickListener {
            isConfirmPasswordVisible = !isConfirmPasswordVisible
            binding.confirmPasswordTxtView.inputType = if (isConfirmPasswordVisible) {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            binding.eyeBtn2.setImageResource(
                if (isConfirmPasswordVisible) R.drawable.show_pass_icon else R.drawable.eye_logo
            )
            refreshTypingFont(binding.confirmPasswordTxtView)
            binding.confirmPasswordTxtView.setSelection(binding.confirmPasswordTxtView.text?.length ?: 0)
        }
    }

    private fun setupScrollOnFocus() {
        val scrollView = view?.findViewById<android.widget.ScrollView>(R.id.scrollView)
        val extraScrollPx = (10 * resources.displayMetrics.density).toInt() // 10dp to pixels

        val focusListener = View.OnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                view.post {
                    scrollView?.smoothScrollTo(0, view.bottom + extraScrollPx)
                }
            }
        }

        binding.usernameTextView.onFocusChangeListener = focusListener
        binding.emailTxtView.onFocusChangeListener = focusListener
        binding.passwrdTxtView.onFocusChangeListener = focusListener
        binding.confirmPasswordTxtView.onFocusChangeListener = focusListener
    }

    private fun updateSignButtonState(isChecked: Boolean) {
        if (isChecked) {
            binding.tickBox.setImageResource(R.drawable.tick_fill_box)
            binding.tickBox.tag = "checked"
            binding.SignButton.isEnabled = true
            binding.SignButton.alpha = 1f
        } else {
            binding.tickBox.setImageResource(R.drawable.tick_box)
            binding.tickBox.tag = "unchecked"
            binding.SignButton.isEnabled = false
            binding.SignButton.alpha = 0.5f
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}