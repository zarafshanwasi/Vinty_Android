package com.idealink.vinty.utils

import android.util.Log

/**
 * Debug utility to log animation performance metrics.
 * Tracks frame count, duration, and FPS for frame-by-frame animations.
 */
class AnimationLogger(private val label: String) {
    private var frameCount = 0
    private var startTime: Long = 0
    private var lastFrameTime: Long = 0

    fun start() {
        frameCount = 0
        startTime = System.currentTimeMillis()
        lastFrameTime = startTime
        Log.d("AnimationLogger", "🎬 Animation started for [$label]")
    }

    fun logFrame() {
        frameCount++
        val now = System.currentTimeMillis()
        val frameDuration = now - lastFrameTime
        lastFrameTime = now
        
        // Log every 30 frames to avoid spam
        if (frameCount % 30 == 0) {
            val totalDuration = now - startTime
            val avgFps = if (totalDuration > 0) (frameCount * 1000f / totalDuration) else 0f
            Log.d("AnimationLogger", "📊 [$label] Frame: $frameCount, Avg FPS: ${"%.1f".format(avgFps)}")
        }
    }

    fun end() {
        val totalDuration = System.currentTimeMillis() - startTime
        val avgFps = if (totalDuration > 0) (frameCount * 1000f / totalDuration) else 0f
        Log.d("AnimationLogger", "🏁 [$label] Complete - Total frames: $frameCount, Duration: ${totalDuration}ms, Avg FPS: ${"%.1f".format(avgFps)}")
    }
}
