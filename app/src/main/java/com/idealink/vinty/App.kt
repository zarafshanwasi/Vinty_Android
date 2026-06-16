package com.idealink.vinty

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.idealink.vinty.data.DataManager
import com.idealink.vinty.data.api.RetrofitInstance
import com.idealink.vinty.data.repository.VintyRepository
import com.idealink.vinty.utils.FacebookAnalyticsManager

class App : Application() {

    val repository: VintyRepository by lazy {
        RetrofitInstance.getRepository()
    }

    override fun onCreate() {
        super.onCreate()

        RetrofitInstance.init(applicationContext)

        // Force LIGHT Mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        try {
            DataManager.initialize(applicationContext)
        } catch (_: Exception) {
            // optional: log this
        }

        // Facebook SDK
        FacebookSdk.sdkInitialize(applicationContext)
        AppEventsLogger.activateApp(this)
        FacebookAnalyticsManager.initialize(applicationContext)
    }
}
