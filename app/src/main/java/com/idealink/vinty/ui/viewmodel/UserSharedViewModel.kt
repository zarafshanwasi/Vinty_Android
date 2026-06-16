package com.idealink.vinty.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.idealink.vinty.data.DataManager
import com.idealink.vinty.data.UserHistoryResponse
import com.idealink.vinty.data.api.ApiException
import com.idealink.vinty.data.model.AdViewRequest
import com.idealink.vinty.data.model.BadgeStatsResponse
import com.idealink.vinty.data.model.DeleteAccountRequest
import com.idealink.vinty.data.model.GainXpRequest
import com.idealink.vinty.data.model.User
import com.idealink.vinty.data.repository.VintyRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import org.json.JSONObject
import retrofit2.HttpException

/**
 * ViewModel for Profile screen
 * Manages user profile data, history, avatar upload, and logout functionality
 * Shared by Activity
 */
class UserSharedViewModel(private val repository: VintyRepository) : ViewModel() {

    companion object {
        private const val TAG = "ProfileViewModel"
    }

    // ========================================================================
    // STATE FLOWS - User Profile
    // ========================================================================

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // ========================================================================
    // STATE FLOWS - User History
    // ========================================================================

    private val _history = MutableStateFlow<UserHistoryResponse?>(null)
    val history: StateFlow<UserHistoryResponse?> = _history.asStateFlow()

    private val _historyLoading = MutableStateFlow(false)
    val historyLoading: StateFlow<Boolean> = _historyLoading.asStateFlow()

    // ========================================================================
    // STATE FLOWS - Avatar Upload
    // ========================================================================

    private val _avatarUploading = MutableStateFlow(false)
    val avatarUploading: StateFlow<Boolean> = _avatarUploading.asStateFlow()

    private val _avatarUploadSuccess = MutableStateFlow(false)
    val avatarUploadSuccess: StateFlow<Boolean> = _avatarUploadSuccess.asStateFlow()

    // ========================================================================
    // STATE FLOWS - Ad Reward
    // ========================================================================

    private val _adReward = MutableStateFlow<Int?>(null)
    val adReward: StateFlow<Int?> = _adReward.asStateFlow()

    // ========================================================================
    // STATE FLOWS - Error Handling
    // ========================================================================

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _badgeStats = MutableStateFlow<BadgeStatsResponse?>(null)
    val badgeStats: StateFlow<BadgeStatsResponse?> = _badgeStats.asStateFlow()

    // ========================================================================
    // STATE FLOWS - Error Handling
    // ========================================================================

    var rewardGain: Int? = null
    var hasShownSpinSheet = false

    // ========================================================================
    // PUBLIC METHODS - User Profile
    // ========================================================================

    fun triggerDataRefresh() {
        CoroutineScope(Dispatchers.Main).launch {
            delay(1000) // 1 second delay
            loadUser()
        }
    }

    fun loadUser() {
        viewModelScope.launch {
            _isLoading.value = true
            
            // Try to load from cache first
            val cachedUser = DataManager.getInstance().getUser()
            if (cachedUser != null) {
                _user.value = cachedUser
            }

            try {
                val response = repository.getUserProfile()
                _user.value = response.user
                
                // Save to cache
                DataManager.getInstance().saveUser(response.user)
            } catch (e: Exception) {
                _error.value = handleAuthError(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Load user history from repository
     * Only loads once per session unless force refresh is requested
     * 
     * @param forceRefresh If true, reloads history even if already loaded
     */
    fun loadUserHistory(forceRefresh: Boolean = false) {
        if (_history.value != null && !forceRefresh) return

        viewModelScope.launch {
            _historyLoading.value = true
            try {
                _history.value = repository.getUserHistory()
            } catch (e: Exception) {
                _error.value = handleAuthError(e)
            } finally {
                _historyLoading.value = false
            }
        }
    }

    // ========================================================================
    // PUBLIC METHODS - Avatar Upload
    // ========================================================================

    /**
     * Upload user avatar image
     * Automatically refreshes user profile on success
     */
    fun uploadAvatar(avatarPart: MultipartBody.Part) {
        viewModelScope.launch {
            _avatarUploading.value = true
            _avatarUploadSuccess.value = false
            _error.value = null

            try {
                val response = repository.uploadAvatar(avatarPart)

                if (response.isSuccessful) {
                    val avatarResponse = response.body()
                    Log.d(TAG, "New avatar URL: ${avatarResponse?.avatarUrl}")

                    // Refresh user profile to get updated avatar
                    refreshUserProfile()
                    _avatarUploadSuccess.value = true
                } else {
                    val errorMsg = "Upload failed: ${response.code()}"
                    _error.value = errorMsg
                }
            } catch (e: Exception) {
                _error.value = handleAuthError(e)
            } finally {
                _avatarUploading.value = false
            }
        }
    }
    fun fetchBadgeStats() {
        if (_badgeStats.value != null) return // avoid re-fetch

        viewModelScope.launch {
            try {
                _badgeStats.value = repository.getBadgeStats()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Record ad view and grant reward
     */
    fun recordAdView() {
        viewModelScope.launch {
            try {
                val adId = "ad_${System.currentTimeMillis()}"
                val response = repository.recordAdView(AdViewRequest(adId))

                if (response.isSuccessful && response.body()?.success == true) {
                    val tickets = response.body()?.ticketsGiven ?: 0

                    // Update local tickets
                    refreshUserProfile()

                    // Gain XP
                    repository.gainXp(GainXpRequest(xp = 100))

                    // Emit reward
                    _adReward.emit(tickets)
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                    _error.value = "Ad reward failed: $errorMsg"
                }
            } catch (e: Exception) {
                _error.value = handleAuthError(e)
            }
        }
    }

    /**
     * Reset avatar upload success state
     */
    fun resetAvatarUploadSuccess() {
        _avatarUploadSuccess.value = false
    }

    /**
     * Reset ad reward state
     */
    fun resetAdReward() {
        _adReward.value = null
    }

    // ========================================================================
    // STATE FLOWS - Invite Code
    // ========================================================================

    private val _inviteCode = MutableStateFlow<String?>(null)
    val inviteCode: StateFlow<String?> = _inviteCode.asStateFlow()

    // ========================================================================
    // PUBLIC METHODS - Invite Code
    // ========================================================================

    fun fetchInviteCode() {
        // Check cache first
        val dataManager = DataManager.getInstance()
        val cachedCode = dataManager.getInviteCode()
        val cachedExpiresAt = dataManager.getInviteExpiresAt()
        val lastFetched = dataManager.getInviteLastFetched()

        val now = System.currentTimeMillis()
        val oneDayMs = 24 * 60 * 60 * 1000

        val isCacheValid = !cachedCode.isNullOrEmpty() &&
                !cachedExpiresAt.isNullOrEmpty() &&
                lastFetched > 0 &&
                (now - lastFetched) < oneDayMs

        if (isCacheValid) {
            _inviteCode.value = cachedCode
            return
        }

        // Fetch from API
        viewModelScope.launch {
            try {
                val response = repository.getMyInviteCode()
                if (response.isSuccessful && response.body()?.success == true) {
                    val body = response.body()!!
                    val code = body.code
                    
                    _inviteCode.value = code

                    dataManager.saveInviteCode(
                        code = body.code,
                        createdAt = body.createdAt,
                        expiresAt = body.expiresAt,
                        usageCount = body.usageCount
                    )
                } else {
                    val errorBody = response.errorBody()?.string()
                    errorBody?.let {
                        val json = JSONObject(it)
                        if (json.has("error")) {
                            _error.value = json.getString("error")
                            return@launch
                        }
                        if (json.has("message")) {
                            _error.value = json.getString("message")
                            return@launch
                        }
                    }
                    _error.value = "Failed to fetch invite code: ${response.code()}"
                }
            } catch (e: Exception) {
                // Fail silently or handle error if needed
                _error.value = "Error fetching invite code: ${e.message}"
            }
        }
    }

    // ========================================================================
    // PUBLIC METHODS - Authentication
    // ========================================================================

    /**
     * Logout user and clear session
     */
    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.logout()
                if (response.isSuccessful) {
                    DataManager.getInstance().clearSession()
                    onSuccess()
                } else {
                    _error.value = "Logout failed"
                }
            } catch (e: Exception) {
                _error.value = handleAuthError(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Delete user account permanently
     */
    fun deleteAccount(request: DeleteAccountRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = repository.deleteAccount(request)
                if (response.isSuccessful) {
                    DataManager.getInstance().clearSession()
                    onSuccess()
                } else {
                    _error.value = "Failed to delete account. Please try again."
                }
            } catch (e: Exception) {
                _error.value = handleAuthError(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ========================================================================
    // PRIVATE METHODS
    // ========================================================================

    /**
     * Refresh user profile from repository
     */
    private suspend fun refreshUserProfile() {
        try {
            val userProfile = repository.getUserProfile()
            _user.value = userProfile.user
            DataManager.getInstance().saveUser(userProfile.user)
        } catch (_: Exception) {
        }
    }

    private fun handleAuthError(e: Exception): String {
        val body: String? = when (e) {
            is ApiException -> e.errorBody
            is HttpException -> e.response()?.errorBody()?.string()
            else -> null
        }
        body?.let {
            try {
                val json = JSONObject(it)
                if (json.has("error")) return json.getString("error")
                if (json.has("message")) return json.getString("message")
            } catch (_: Exception) {
                // fall through
            }
        }
        return "Something went wrong, Please try again later."
    }

    fun clearError() {
        _error.value = null
    }
}
