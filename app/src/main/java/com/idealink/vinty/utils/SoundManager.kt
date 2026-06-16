package com.idealink.vinty.utils

import android.content.Context
import android.media.MediaPlayer
import androidx.annotation.RawRes

/**
 * Sound manager for playing game sound effects.
 * Manages MediaPlayer instances for jackpot sounds.
 */
object SoundManager {
    
    private val players = mutableMapOf<Int, MediaPlayer>()
    
    /**
     * Play a sound from raw resources.
     * @param context Application context
     * @param resId Raw resource ID (e.g., R.raw.slot_arm_sound)
     * @param loop Whether to loop the sound
     */
    fun playSound(context: Context, @RawRes resId: Int, loop: Boolean = false) {
        try {
            // Stop and release existing player for this resource if any
            stopSound(resId)
            
            val mediaPlayer = MediaPlayer.create(context, resId)
            mediaPlayer?.apply {
                isLooping = loop
                setOnCompletionListener {
                    if (!loop) {
                        release()
                        players.remove(resId)
                    }
                }
                start()
                players[resId] = this
            }
        } catch (e: Exception) {
            android.util.Log.e("SoundManager", "Failed to play sound: $resId", e)
        }
    }
    
    /**
     * Stop a specific sound.
     */
    fun stopSound(@RawRes resId: Int) {
        players[resId]?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        players.remove(resId)
    }
    
    /**
     * Stop all currently playing sounds.
     */
    fun stopAll() {
        players.values.forEach { player ->
            try {
                if (player.isPlaying) {
                    player.stop()
                }
                player.release()
            } catch (e: Exception) {
                android.util.Log.e("SoundManager", "Error stopping sound", e)
            }
        }
        players.clear()
    }
    
    /**
     * Release all resources.
     */
    fun release() {
        stopAll()
    }
}
