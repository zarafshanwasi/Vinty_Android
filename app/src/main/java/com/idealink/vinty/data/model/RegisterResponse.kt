package com.idealink.vinty.data.model

data class RegisterResponse(
    val success: Boolean? = true,
    val message: String? = null,
    val accessToken: String?,
    val refreshToken: String?,
    val user: User?,
    val emailVerificationSent: Boolean? = false
)