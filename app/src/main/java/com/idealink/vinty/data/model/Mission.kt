package com.idealink.vinty.data.model

// -----------------------------------------------------
// MAIN MISSIONS RESPONSE (MATCHES YOUR EXACT API JSON)
// -----------------------------------------------------
data class MissionsResponse(
    val continuous: ContinuousMissions?,
    val daily: List<Mission>?
)

// -----------------------------------------------------
// CONTINUOUS MISSIONS
// -----------------------------------------------------
data class ContinuousMissions(
    val adWatching: List<Mission>?,
    val roomEntry: List<Mission>?
)

// -----------------------------------------------------
// MISSION MODEL — matches your JSON fields 100%
// -----------------------------------------------------
data class Mission(
    val id: String,
    val missionId: String,

    val title: String,
    val description: String,

    val action: String,
    val target: Int,
    val progress: Int,
    val lifetimeProgress: Int,

    val completed: Boolean,
    val claimed: Boolean,

    val rewardTickets: Int,
    val rewardGems: Int,

    val sequenceOrder: Int?,
    val expiresAt: String?,

    val isAvailable: Boolean,
    val isInProgress: Boolean,
    val isCompleted: Boolean,
    val isClaimed: Boolean
)
data class MissionClaimResponse(
    val message: String,
    val updatedTickets: Int,
    val updatedGems: Int
)
