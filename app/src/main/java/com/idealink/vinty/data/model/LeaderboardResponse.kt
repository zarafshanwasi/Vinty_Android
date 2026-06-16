package com.idealink.vinty.data.model

data class LeaderboardResponse(
    val period: String,
    val totalUsers: Int,
    val leaderboard: List<LeaderboardUser>,
    val currentUser: LeaderboardUser?
)

data class LeaderboardUser(
    val rank: Int,
    val username: String,
    val avatarUrl: String?,
    val level: Int,
    val stats: Stats
)

data class Stats(
    val adViews: Int,
    val ticketsSpent: Int,
    val gemsEarned: Int,
    val totalScore: Int
)
