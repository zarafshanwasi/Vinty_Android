package com.idealink.vinty.utils

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.unity3d.ads.*
import com.unity3d.ads.UnityAds

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
        if (!UnityAds.isInitialized) {
            initializeUnityAds {
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
        UnityAds.show(activity, AD_UNIT_ID, object : IUnityAdsShowListener {
            override fun onUnityAdsShowStart(placementId: String) {
                post { listener.onAdShown() }
            }

            override fun onUnityAdsShowClick(placementId: String) {
                Log.d(TAG, "Ad clicked")
            }

            override fun onUnityAdsShowFailure(
                placementId: String,
                error: UnityAds.UnityAdsShowError,
                message: String
            ) {
                Log.e(TAG, "Show failed: [$error] $message")

                state = State.IDLE

                post {
                    listener.onError("Ad show failed: $message")
                }
            }

            override fun onUnityAdsShowComplete(
                placementId: String,
                completionState: UnityAds.UnityAdsShowCompletionState
            ) {
                state = State.IDLE

                post {
                    if (completionState == UnityAds.UnityAdsShowCompletionState.COMPLETED) {
                        listener.onAdCompleted()
                    } else {
                        listener.onAdDismissed()
                    }
                }
            }
        })
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

        UnityAds.load(AD_UNIT_ID, object : IUnityAdsLoadListener {

            override fun onUnityAdsAdLoaded(placementId: String) {
                state = State.LOADED
                loadingCallback?.invoke(false)

                post {
                    listener?.onAdLoaded()
                }
            }

            override fun onUnityAdsFailedToLoad(
                placementId: String,
                error: UnityAds.UnityAdsLoadError,
                message: String
            ) {
                Log.e(TAG, "Load failed: [$error] $message")

                state = State.IDLE
                loadingCallback?.invoke(false)

                post {
                    listener?.onAdFailedToLoad(message)
                }
            }
        })
    }

    // =========================================================
    // Initialization (safe async)
    // =========================================================

    private fun initializeUnityAds(onComplete: (() -> Unit)? = null) {

        if (UnityAds.isInitialized) {
            onComplete?.invoke()
            return
        }

        UnityAds.initialize(
            appContext,
            GAME_ID,
            TEST_MODE,
            object : IUnityAdsInitializationListener {

                override fun onInitializationComplete() {
                    Log.d(TAG, "Unity Ads initialized")
                    onComplete?.invoke()
                }

                override fun onInitializationFailed(
                    error: UnityAds.UnityAdsInitializationError?,
                    message: String?
                ) {
                    Log.e(TAG, "Initialization failed: [$error] $message")
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

        // Unity test IDs
        private const val GAME_ID = "4853271"
        private const val AD_UNIT_ID = "Rewarded_Android"

        private const val TEST_MODE = true
    }
}
