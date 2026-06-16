package com.idealink.vinty.utils

import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import java.io.IOException
import java.util.WeakHashMap

/**
 * Extension functions for ImageView to play frame-by-frame animations from assets.
 * Supports streaming PNG sequences with custom frame duration and cancellation.
 */

// WeakHashMap to track animation state per ImageView (similar to iOS NSMapTable)
private val animationFlags = WeakHashMap<ImageView, Boolean>()

/**
 * Play a sequence of images from assets folder with streaming approach.
 * 
 * @param basePath Base path in assets folder (e.g., "Slots/slot_1_gem")
 * @param baseName Base name of the image files (e.g., "slot_1_gem")
 * @param frameRange Range of frame numbers to play (e.g., 45..239)
 * @param frameDuration Duration of each frame in milliseconds (default 50ms = 20 FPS)
 * @param completion Callback invoked when animation completes
 */
fun ImageView.playImageSequenceStreamed(
    basePath: String,
    baseName: String,
    frameRange: IntRange,
    frameDuration: Long = 50,
    completion: (() -> Unit)? = null
) {
    val logger = AnimationLogger(baseName)
    logger.start()
    
    // Mark this ImageView as actively animating
    animationFlags[this] = true
    
    val frames = frameRange.toList().toMutableList()
    val handler = Handler(Looper.getMainLooper())
    
    fun playNextFrame() {
        if (frames.isEmpty()) {
            logger.end()
            handler.post { completion?.invoke() }
            return
        }
        
        val frameNumber = frames.removeAt(0)
        val shouldContinue = animationFlags[this] ?: false
        
        if (!shouldContinue) {
            logger.end()
            return
        }
        
        try {
            // Load image from assets
            val fileName = "$basePath/${baseName}_${frameNumber}@2x.png"
            context.assets.open(fileName).use { inputStream ->
                val bitmap = BitmapFactory.decodeStream(inputStream)
                handler.post {
                    if (animationFlags[this] == true) {
                        setImageBitmap(bitmap)
                        logger.logFrame()
                    }
                }
            }
        } catch (e: IOException) {
            android.util.Log.e("ImageViewExtensions", "❌ Failed to load frame: $baseName $frameNumber", e)
        }
        
        // Schedule next frame
        handler.postDelayed({ playNextFrame() }, frameDuration)
    }
    
    playNextFrame()
}

/**
 * Cancel any ongoing image sequence animation on this ImageView.
 */
fun ImageView.cancelImageSequenceStreamed() {
    animationFlags[this] = false
}
