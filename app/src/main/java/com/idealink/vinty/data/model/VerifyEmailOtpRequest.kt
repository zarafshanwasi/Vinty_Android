package com.idealink.vinty.data.model


data class VerifyEmailOtpRequest(
    val email: String,
    val otp: String
)

data class ForgotPasswordVerifyOtpRequest(
    val email: String,
    val otp: String,
    val newPassword: String
)
data class ForgotPasswordRequest(
    val email: String
)
data class ResendVerificationEmailRequest(
    val email: String
)

data class ForgotPasswordResponse(
    val message: String,
    val otp: String
)
