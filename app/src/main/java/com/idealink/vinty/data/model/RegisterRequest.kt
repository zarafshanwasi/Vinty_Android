package com.idealink.vinty.data.model

data class RegisterRequest(
    val email: String,
    val password: String,
    val username: String,
    val sendEmailVerification: Boolean = true
)