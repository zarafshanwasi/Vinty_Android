package com.idealink.vinty.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.idealink.vinty.data.api.ApiException
import com.idealink.vinty.data.repository.VintyRepository
import com.idealink.vinty.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import org.json.JSONObject

class AuthViewModel(private val repository: VintyRepository) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _loginResult = MutableStateFlow<LoginResponse?>(null)
    val loginResult: StateFlow<LoginResponse?> = _loginResult

    private val _registerResult = MutableStateFlow<RegisterResponse?>(null)
    val registerResult: StateFlow<RegisterResponse?> = _registerResult

    private val _authActionResult = MutableStateFlow<AuthActionResult?>(null)
    val authActionResult: StateFlow<AuthActionResult?> = _authActionResult

    fun login(request: LoginRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = repository.login(request)
                _loginResult.value = response
            } catch (e: Exception) {
                _error.value = handleAuthError(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun googleLogin(request: GoogleLoginRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = repository.googleLogin(request)
                _loginResult.value = response
            } catch (e: Exception) {
                _error.value = handleAuthError(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun register(request: RegisterRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = repository.register(request)
                _registerResult.value = response
            } catch (e: Exception) {
                _error.value = handleAuthError(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun verifyEmailOtp(request: VerifyEmailOtpRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = repository.verifyEmailOtp(request)
                
                if (response.success == true || response.message?.contains("verified", ignoreCase = true) == true) {
                    _authActionResult.value = AuthActionResult.VerifyOtpSuccess(response.message ?: "Verified successfully")
                } else {
                    _error.value = response.error ?: response.message ?: "Invalid or expired OTP"
                }
            } catch (e: Exception) {
                _error.value = handleAuthError(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resendVerificationEmail(request: ResendVerificationEmailRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = repository.resendVerificationEmail(request)
                if (response.success == true) {
                    val message = response.message ?: "OTP resent successfully"
                    _authActionResult.value = AuthActionResult.ResendEmailSuccess(message)
                } else {
                    _error.value = response.error ?: response.message ?: "Failed to resend OTP"
                }
            } catch (e: Exception) {
                _error.value = handleAuthError(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun forgotPassword(request: ForgotPasswordRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = repository.forgotPassword(request)
                _authActionResult.value = AuthActionResult.ForgotPasswordSent(response.message,
                    response.otp)
            } catch (e: Exception) {
                _error.value = handleAuthError(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun forgotPasswordVerifyOtp(request: ForgotPasswordVerifyOtpRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = repository.forgotPasswordVerifyOtp(request)
                if (response.success == true || response.message?.contains("success", ignoreCase = true) == true) {
                    _authActionResult.value = AuthActionResult.ResetPasswordSuccess(response.message ?: "Password changed successfully")
                } else {
                    _error.value = response.error ?: response.message ?: "Failed to verify OTP"
                }

            } catch (e: Exception) {
                _error.value = handleAuthError(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun applyInviteCode(request: ApplyInviteCodeRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = repository.applyInviteCode(request)
                if (response.success) {
                    _authActionResult.value = AuthActionResult.ApplyInviteCodeSuccess(response.message, response.ticketsAwarded)
                } else {
                    _error.value = response.message
                }
            } catch (e: Exception) {
                _error.value = handleAuthError(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun handleAuthError(e: Exception): String {
        val body: String? = when (e) {
            is ApiException -> e.errorBody
            is HttpException -> e.response()?.errorBody()?.string()
            else -> null
        }
        body?.let {
            try {
                val json = JSONObject(it)
                if (json.has("error")) return json.getString("error")
                if (json.has("message")) return json.getString("message")
            } catch (_: Exception) {
                // fall through to generic message
            }
        }
        return "Something went wrong, Please try again later."
    }

    fun clearLoginResult() {
        _loginResult.value = null
    }

    fun clearRegisterResult() {
        _registerResult.value = null
    }

    fun clearAuthActionResult() {
        _authActionResult.value = null
    }

    fun clearError() {
        _error.value = null
    }
}

sealed class AuthActionResult {
    data class VerifyOtpSuccess(val message: String) : AuthActionResult()
    data class ResendEmailSuccess(val message: String) : AuthActionResult()
    data class ForgotPasswordSent(val message: String, val otp: String) : AuthActionResult()
    data class ResetPasswordSuccess(val message: String) : AuthActionResult()
    data class ApplyInviteCodeSuccess(val message: String, val reward: Int) : AuthActionResult()
}
