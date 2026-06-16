package com.idealink.vinty.data.model


data class AdViewRequest(val adId: String)

data class AdViewResponse(
    val success: Boolean,
    val ticketsGiven: Int
)

// Gain XP Request & Response
data class GainXpRequest(val xp: Int)

data class GainXpResponse(val success: Boolean)