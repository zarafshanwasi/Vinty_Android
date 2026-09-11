package com.idealink.vinty.utils

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.idealink.vinty.BuildConfig
import com.unity3d.mediation.LevelPlay
import com.unity3d.mediation.LevelPlayAdError
import com.unity3d.mediation.LevelPlayAdInfo
import com.unity3d.mediation.LevelPlayConfiguration
import com.unity3d.mediation.LevelPlayInitError
import com.unity3d.mediation.LevelPlayInitListener
import com.unity3d.mediation.LevelPlayInitRequest
import com.unity3d.mediation.rewarded.LevelPlayReward
import com.unity3d.mediation.rewarded.LevelPlayRewardedAd
import com.unity3d.mediation.rewarded.LevelPlayRewardedAdListener

class AdManager(context: Context) {

    private val appContext = context.applicationContext
    private val mainHandler = Handler(Looper.getMainLooper())

    // =========================================================
    // State
    // =========================================================

    private enum class State {
        IDLE,
        LOADING,
        LOADED
    }

    private var state = State.IDLE
    private var loadingCallback: ((Boolean) -> Unit)? = null

    @Volatile
    private var isInitialized = false

    private var rewardedAd: LevelPlayRewardedAd? = null
    private var wasRewarded = false

    // =========================================================
    // Listener
    // =========================================================

    interface AdListener {
        fun onAdLoaded()
        fun onAdFailedToLoad(error: String)
        fun onAdShown()
        fun onAdDismissed()
        fun onAdCompleted()
        fun onError(message: String)
    }

    // =========================================================
    // Public API
    // =========================================================

    /**
     * Used by UI to show/hide loader
     */
    fun setLoadingCallback(callback: (Boolean) -> Unit) {
        loadingCallback = callback
        callback(state == State.LOADING)
    }

    /**
     * Show ad on demand.
     * Automatically:
     * 1) initializes if needed
     * 2) loads if needed
     * 3) shows when ready
     */
    fun showRewardedAd(activity: Activity, listener: AdListener) {

        // -----------------------------------------------------
        // Step 1 — Initialize safely (async)
        // -----------------------------------------------------
        if (!isInitialized) {
            initializeLevelPlay {
                showRewardedAd(activity, listener)
            }
            return
        }

        // -----------------------------------------------------
        // Step 2 — Load if not ready
        // -----------------------------------------------------
        if (state != State.LOADED) {
            loadRewardedAd(object : AdListener {

                override fun onAdLoaded() {
                    showRewardedAd(activity, listener)
                }

                override fun onAdFailedToLoad(error: String) {
                    listener.onError(error)
                }

                override fun onAdShown() {}
                override fun onAdDismissed() {}
                override fun onAdCompleted() {}
                override fun onError(message: String) {}
            })
            return
        }

        // -----------------------------------------------------
        // Step 3 — Show
        // -----------------------------------------------------
        wasRewarded = false
        getOrCreateRewardedAd(listener).showAd(activity)
    }

    // =========================================================
    // Load
    // =========================================================

    private fun loadRewardedAd(listener: AdListener? = null) {

        when (state) {
            State.LOADED -> {
                post { listener?.onAdLoaded() }
                return
            }

            State.LOADING -> return

            State.IDLE -> {}
        }

        state = State.LOADING
        loadingCallback?.invoke(true)

        getOrCreateRewardedAd(listener).loadAd()
    }

    // =========================================================
    // Rewarded ad instance
    // =========================================================

    private fun getOrCreateRewardedAd(listener: AdListener?): LevelPlayRewardedAd {
        rewardedAd?.let { return it }

        val ad = LevelPlayRewardedAd(AD_UNIT_ID)
        ad.setListener(object : LevelPlayRewardedAdListener {

            override fun onAdLoaded(adInfo: LevelPlayAdInfo) {
                state = State.LOADED
                loadingCallback?.invoke(false)

                post {
                    listener?.onAdLoaded()
                }
            }

            override fun onAdLoadFailed(error: LevelPlayAdError) {
                Log.e(TAG, "Load failed: [${error.errorCode}] ${error.errorMessage}")

                state = State.IDLE
                loadingCallback?.invoke(false)

                post {
                    listener?.onAdFailedToLoad(error.errorMessage)
                }
            }

            override fun onAdDisplayed(adInfo: LevelPlayAdInfo) {
                post { listener?.onAdShown() }
            }

            override fun onAdDisplayFailed(error: LevelPlayAdError, adInfo: LevelPlayAdInfo) {
                Log.e(TAG, "Show failed: [${error.errorCode}] ${error.errorMessage}")

                state = State.IDLE

                post {
                    listener?.onError("Ad show failed: ${error.errorMessage}")
                }
            }

            override fun onAdRewarded(reward: LevelPlayReward, adInfo: LevelPlayAdInfo) {
                wasRewarded = true
            }

            override fun onAdClicked(adInfo: LevelPlayAdInfo) {
                Log.d(TAG, "Ad clicked")
            }

            override fun onAdClosed(adInfo: LevelPlayAdInfo) {
                state = State.IDLE

                post {
                    if (wasRewarded) {
                        listener?.onAdCompleted()
                    } else {
                        listener?.onAdDismissed()
                    }
                    wasRewarded = false
                }
            }

            override fun onAdInfoChanged(adInfo: LevelPlayAdInfo) {}
        })

        rewardedAd = ad
        return ad
    }

    // =========================================================
    // Initialization (safe async)
    // =========================================================

    private fun initializeLevelPlay(onComplete: (() -> Unit)? = null) {

        if (isInitialized) {
            onComplete?.invoke()
            return
        }

        val initRequest = LevelPlayInitRequest.Builder(APP_KEY).build()

        LevelPlay.init(
            appContext,
            initRequest,
            object : LevelPlayInitListener {

                override fun onInitSuccess(configuration: LevelPlayConfiguration) {
                    Log.d(TAG, "LevelPlay initialized")
                    isInitialized = true
                    onComplete?.invoke()
                }

                override fun onInitFailed(error: LevelPlayInitError) {
                    Log.e(TAG, "Initialization failed: [${error.errorCode}] ${error.errorMessage}")
                }
            }
        )
    }

    // =========================================================
    // Helpers
    // =========================================================

    private fun post(block: () -> Unit) {
        mainHandler.post(block)
    }

    companion object {
        private const val TAG = "AdManager"

        private val APP_KEY = BuildConfig.LEVELPLAY_APP_KEY
        private val AD_UNIT_ID = BuildConfig.LEVELPLAY_REWARDED_AD_UNIT_ID
    }
}
