package com.idealink.vinty.utils

import com.google.firebase.crashlytics.FirebaseCrashlytics

object CrashlyticsManager {

    private val crashlytics: FirebaseCrashlytics by lazy { FirebaseCrashlytics.getInstance() }

    fun recordError(error: Throwable, extraInfo: Map<String, String>? = null) {
        extraInfo?.forEach { (key, value) ->
            crashlytics.setCustomKey(key, value)
        }
        crashlytics.recordException(error)
    }

    fun log(message: String) {
        crashlytics.log(message)
    }

    fun setCustomValue(value: String, forKey: String) {
        crashlytics.setCustomKey(forKey, value)
    }

    fun setUserID(id: String?) {
        crashlytics.setUserId(id ?: "unknown_user")
    }
}
