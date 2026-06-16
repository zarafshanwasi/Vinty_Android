package com.idealink.vinty.data.model

data class SpinStatusResponse(
    val success: Boolean,
    val spinsRemaining: Int,
    val freeSpinUsed: Boolean,
    val extraSpins: Int,
    val totalSpinsUsedToday: Int,
    val resetTime: String
)
