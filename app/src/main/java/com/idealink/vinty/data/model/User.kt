package com.idealink.vinty.data.model

data class User(
    val id: String,
    val email: String,
    val balance: Int,
    var tickets: Int,
    val username: String,
    val withdrawalThreshold: Int,
    val isVerified: Boolean,
    val level: Int,
    val levelProgress: Double,
    val totalAds: Int,
    val totalWinnings: Int,
    val avatarUrl: String?,
    val isBanned: Boolean,
    val currentStreak: Int,
    val longestStreak: Int,
    val streakBroken: Boolean
)

data class UserProfileResponse(
    val user: User
)