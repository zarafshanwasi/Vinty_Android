package com.idealink.vinty.data.model

data class BadgeStatsResponse(
    val success: Boolean,
    val data: BadgeStatsData
)

data class BadgeStatsData(
    val overall: OverallStats,
    val tickets: TicketStats,
    val byCategory: Map<String, CategoryStats>,
    val byTier: Map<String, TierStats>
)

data class OverallStats(
    val total: Int,
    val unlocked: Int,
    val locked: Int,
    val claimed: Int,
    val unclaimed: Int
)

data class TicketStats(
    val earned: Int,
    val pending: Int,
    val potential: Int
)

data class CategoryStats(
    val total: Int,
    val unlocked: Int,
    val claimed: Int
)

data class TierStats(
    val total: Int,
    val unlocked: Int,
    val claimed: Int
)
