package com.idealink.vinty.data.model

data class OtpResponse(
    val success: Boolean?,
    val message: String?,
    val expiresIn: Int?,
    val error: String?,
    val retryAfter: Int?
)
