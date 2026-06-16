package com.idealink.vinty.utils

import android.content.Context
import com.facebook.appevents.AppEventsLogger

object FacebookAnalyticsManager {

    private var logger: AppEventsLogger? = null

    fun initialize(context: Context) {
        logger = AppEventsLogger.newLogger(context)
    }

    fun logEvent(event: FacebookEvent) {
        logger?.logEvent(event.eventName)
    }
}
