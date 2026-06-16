package com.idealink.vinty.data.model

data class SpinResult(
    val success: Boolean,
    val combination: Combination,
    val reward: RewardResult,
    val userBalance: UserBalance,
    val isJackpot: Boolean,
    val spinResultId: String
)
