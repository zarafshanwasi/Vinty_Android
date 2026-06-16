package com.idealink.vinty.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.idealink.vinty.R
import com.idealink.vinty.data.DataManager
import com.idealink.vinty.databinding.ActivitySplashBinding

/**
 * SplashActivity - Entry point of the application.
 * Determines whether to show onboarding, login flow, or main app.
 */
@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize DataManager
        DataManager.Companion.initialize(applicationContext)

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.lottieAnimation.apply {
            setAnimation(R.raw.splash_animation)
            playAnimation()
        }

        Handler(Looper.getMainLooper()).postDelayed({
            navigateNext()
        }, 2000)
    }

//    private fun navigateNext() {
//
//        //  TESTING ONLY: Skip onboarding + login
//        startActivity(Intent(this, MainActivity::class.java))
//        finish()
//    }

    private fun navigateNext() {
        val dataManager = DataManager.Companion.getInstance()

        // STEP 1: Check onboarding
        if (!dataManager.isOnboardingCompleted()) {
            // Navigate to AccountActivity with onboarding flag
            startActivity(Intent(this, AccountActivity::class.java).apply {
                putExtra("SHOW_ONBOARDING", true)
            })
            finish()
            return
        }

        // STEP 2: Check authentication status
        if (dataManager.isLoggedIn()) {
            // User is authenticated, navigate to MainActivity
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            // User not authenticated, navigate to AccountActivity
            dataManager.clearSession() // Clear any stale data
            startActivity(Intent(this, AccountActivity::class.java))
        }

        finish()
    }
}