package com.idealink.vinty.data.model

data class LoginResponse(
    val accessToken: String?,
    val refreshToken: String?,
    val message: String?,
    val user: User?
)
data class LogoutResponse(
    val message: String
)
