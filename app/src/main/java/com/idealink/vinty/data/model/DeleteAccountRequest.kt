package com.idealink.vinty.data.model

data class DeleteAccountRequest(
    val reason: String = "No longer using the app",
    val feedback: String = "The app was great, just moving on"
)
