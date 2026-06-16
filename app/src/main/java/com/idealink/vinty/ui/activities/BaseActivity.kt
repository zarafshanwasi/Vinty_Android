package com.idealink.vinty.ui.activities

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        val config = Configuration(newBase.resources.configuration)
        val capped = minOf(config.fontScale, MAX_FONT_SCALE)
        if (config.fontScale != capped) {
            config.fontScale = capped
            super.attachBaseContext(newBase.createConfigurationContext(config))
        } else {
            super.attachBaseContext(newBase)
        }
    }

    companion object {
        private const val MAX_FONT_SCALE = 1.15f
    }
}
