package com.idealink.vinty.utils

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

object AnalyticsManager {

    private val analytics: FirebaseAnalytics by lazy { Firebase.analytics }

    fun log(event: AnalyticsEvent, params: Bundle? = null) {
        analytics.logEvent(event.eventName, params)
    }

    fun setUserProperty(value: String?, forName: String) {
        analytics.setUserProperty(forName, value)
    }

    fun setUserID(id: String?) {
        analytics.setUserId(id)
    }
}

enum class AnalyticsEvent(val eventName: String) {
    AD_REWARD_EARNED("ad_reward_earned"),
    EARN_TICKET("earn_ticket"),
    REDEEM_ATTEMPT("redeem_attempt"),
    MISSION_PROGRESS_UP("mission_progress_up"),
    REGISTER("register"),
    LOGIN("login"),
    AD_WATCH_CLICKED("ad_watch_clicked")
}
