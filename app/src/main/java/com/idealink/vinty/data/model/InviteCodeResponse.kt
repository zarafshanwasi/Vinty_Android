package com.idealink.vinty.data.model

data class InviteCodeResponse(
    val success: Boolean,
    val code: String,
    val createdAt: String,
    val expiresAt: String,
    val usageCount: Int
)
