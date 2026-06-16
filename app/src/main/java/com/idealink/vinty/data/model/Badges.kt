package com.idealink.vinty.data.model

data class BadgeGalleryResponse(
    val success: Boolean,
    val data: Map<String, List<Badge>>
)

data class Badge(
    val id: String,
    val name: String,
    val description: String,
    val category: String,
    val tier: String,
    val requirementType: String,
    val requirementValue: Int,
    val ticketReward: Int,
    val specialReward: String?,
    val iconUrl: String,
    val isHidden: Boolean,
    val progress: Int,
    val isUnlocked: Boolean,
    val unlockedAt: String?,
    var rewardClaimed: Boolean,
    val claimedAt: String?
)
data class BadgeProgressResponse(
    val adWatchingProgress: BadgeProgress?,
    val ticketProgress: BadgeProgress?
)
data class BadgeProgress(
    val badgeId: String,
    val badgeName: String,
    val badgeDescription: String,
    val badgeTier: String,
    val progress: Int,
    val target: Int,
    val remaining: Int,
    val progressPercentage: Int,
    val rewardTickets: Int,
    val iconUrl: String,
    val isUnlocked: Boolean,
    val message: String
)
