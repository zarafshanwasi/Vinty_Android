package com.idealink.vinty.data

data class UserHistoryResponse(
    val summary: HistorySummary
)

data class HistorySummary(
    val totalTicketsEarned: Int,
    val totalTicketsSpent: Int,
    val totalWins: Int,
    val totalPrize: Int,
    val totalEntries: Int,
    val totalAdsWatched: Int
)
