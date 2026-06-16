package com.idealink.vinty.data.model

data class LoginRequest(
    val emailOrUsername: String,
    val password: String
)
data class TokenResponse(
    val accessToken: String,
    val refreshToken: String
)
