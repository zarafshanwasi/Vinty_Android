package com.idealink.vinty.data.model

data class ApplyInviteCodeRequest(
    val inviteCode: String
)

data class ApplyInviteCodeResponse(
    val success: Boolean,
    val message: String,
    val ticketsAwarded: Int = 0
)