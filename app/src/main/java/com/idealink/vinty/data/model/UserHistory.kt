package com.idealink.vinty.data.model


data class LotteryHistory(
    val id: String,
    val userId: String,
    val lotteryId: String?,
    val ticketsUsed: Int?,
    val prizeWon: String?,
    val createdAt: String
)

data class RewardHistory(
    val id: String,
    val userId: String,
    val rewardType: String?,
    val amount: Int?,
    val createdAt: String
)