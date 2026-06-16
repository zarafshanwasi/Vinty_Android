package com.idealink.vinty.data

import com.google.gson.Gson
import com.idealink.vinty.data.model.User
import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * Thread-safe singleton for managing SharedPreferences and session data.
 * Provides synchronized access to user data, tokens, and preferences.
 */
class DataManager private constructor(context: Context) {
    
    private val sharedPref: SharedPreferences = 
        context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    
    private val lock = ReentrantReadWriteLock()
    private val gson = Gson()
    
    companion object {
        @Volatile
        private var INSTANCE: DataManager? = null
        
        private const val PREF_NAME = "user_pref"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_ONBOARDING_DONE = "onboarding_done"
        private const val KEY_INVITE_CODE = "invite_code"
        private const val KEY_INVITE_CREATED_AT = "invite_created_at"
        private const val KEY_INVITE_EXPIRES_AT = "invite_expires_at"
        private const val KEY_INVITE_USAGE_COUNT = "invite_usage_count"
        private const val KEY_INVITE_LAST_FETCHED_AT = "invite_last_fetched_at"
        private const val KEY_USER_DATA = "user_data"
        private const val KEY_FCM_TOKEN = "fcm_token"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        
        /**
         * Get the singleton instance. Must be initialized with context first.
         * @throws IllegalStateException if not initialized
         */
        fun getInstance(): DataManager {
            return INSTANCE ?: throw IllegalStateException(
                "DataManager not initialized. Call initialize(context) first."
            )
        }
        
        /**
         * Initialize the singleton instance. Should be called in Application class.
         */
        fun initialize(context: Context): DataManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DataManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    // ========== Token Management ==========
    
    /**
     * Save access and refresh tokens thread-safely
     */
    fun saveTokens(accessToken: String?, refreshToken: String?) {
        lock.write {
            sharedPref.edit().apply {
                putString(KEY_ACCESS_TOKEN, accessToken)
                putString(KEY_REFRESH_TOKEN, refreshToken)
                apply()
            }
            android.util.Log.d("DataManager", "💾 Tokens saved successfully")
        }
    }
    
    /**
     * Get access token thread-safely
     */
    fun getAccessToken(): String? {
        return lock.read {
            sharedPref.getString(KEY_ACCESS_TOKEN, null)
        }
    }
    
    /**
     * Get refresh token thread-safely
     */
    fun getRefreshToken(): String? {
        return lock.read {
            sharedPref.getString(KEY_REFRESH_TOKEN, null)
        }
    }
    
    /**
     * Clear tokens thread-safely
     */
    fun clearTokens() {
        lock.write {
            sharedPref.edit { remove(KEY_ACCESS_TOKEN).remove(KEY_REFRESH_TOKEN) }
            android.util.Log.d("DataManager", "🧹 Tokens cleared")
        }
    }
    
    fun isLoggedIn(): Boolean {
        return lock.read {
            !getAccessToken().isNullOrEmpty()
        }
    }
    
    // ========== User Data Management ==========

    /**
     * Save user object thread-safely
     */
    fun saveUser(user: User) {
        lock.write {
            val json = gson.toJson(user)
            sharedPref.edit {
                putString(KEY_USER_DATA, json)
            }
        }
    }

    /**
     * Get cached user object thread-safely
     */
    fun getUser(): User? {
        return lock.read {
            val json = sharedPref.getString(KEY_USER_DATA, null)
            if (json != null) {
                try {
                    gson.fromJson(json, User::class.java)
                } catch (_: Exception) {
                    null
                }
            } else {
                null
            }
        }
    }

    /**
     * Clear cached user data
     */
    fun clearUser() {
        lock.write {
            sharedPref.edit {
                remove(KEY_USER_DATA)
            }
        }
    }
    
    // ========== FCM Token Management ==========

    fun saveFcmToken(token: String) {
        lock.write {
            sharedPref.edit { putString(KEY_FCM_TOKEN, token) }
        }
    }

    fun getFcmToken(): String? {
        return lock.read {
            sharedPref.getString(KEY_FCM_TOKEN, null)
        }
    }

    // ========== Notification Preference ==========

    fun saveNotificationsEnabled(enabled: Boolean) {
        lock.write {
            sharedPref.edit { putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled) }
        }
    }

    fun isNotificationsEnabled(): Boolean {
        return lock.read {
            sharedPref.getBoolean(KEY_NOTIFICATIONS_ENABLED, false)
        }
    }

    // ========== Session Management ==========
    
    /**
     * Clear all session data (logout)
     */
    fun clearSession() {
        lock.write {
            clearTokens()
            clearInviteCode()
            clearUser()
        }
    }
    
    // ========== Onboarding ==========
    
    /**
     * Mark onboarding as completed
     */
    fun setOnboardingCompleted() {
        lock.write {
            sharedPref.edit { putBoolean(KEY_ONBOARDING_DONE, true) }
        }
    }
    
    /**
     * Check if onboarding is completed
     */
    fun isOnboardingCompleted(): Boolean {
        return lock.read {
            sharedPref.getBoolean(KEY_ONBOARDING_DONE, false)
        }
    }
    
    // ========== Invite Code Management ==========
    
    /**
     * Save invite code data thread-safely
     */
    fun saveInviteCode(
        code: String,
        createdAt: String,
        expiresAt: String,
        usageCount: Int
    ) {
        lock.write {
            sharedPref.edit {
                putString(KEY_INVITE_CODE, code)
                putString(KEY_INVITE_CREATED_AT, createdAt)
                putString(KEY_INVITE_EXPIRES_AT, expiresAt)
                putInt(KEY_INVITE_USAGE_COUNT, usageCount)
                putLong(KEY_INVITE_LAST_FETCHED_AT, System.currentTimeMillis())
            }
        }
    }
    
    /**
     * Get invite code thread-safely
     */
    fun getInviteCode(): String? {
        return lock.read {
            sharedPref.getString(KEY_INVITE_CODE, null)
        }
    }
    
    /**
     * Get invite expiration date thread-safely
     */
    fun getInviteExpiresAt(): String? {
        return lock.read {
            sharedPref.getString(KEY_INVITE_EXPIRES_AT, null)
        }
    }
    
    /**
     * Get invite last fetched timestamp thread-safely
     */
    fun getInviteLastFetched(): Long {
        return lock.read {
            sharedPref.getLong(KEY_INVITE_LAST_FETCHED_AT, 0L)
        }
    }
    
    /**
     * Clear invite code data thread-safely
     */
    fun clearInviteCode() {
        lock.write {
            sharedPref.edit {
                remove(KEY_INVITE_CODE)
                remove(KEY_INVITE_CREATED_AT)
                remove(KEY_INVITE_EXPIRES_AT)
                remove(KEY_INVITE_USAGE_COUNT)
                remove(KEY_INVITE_LAST_FETCHED_AT)
            }
        }
    }
}
