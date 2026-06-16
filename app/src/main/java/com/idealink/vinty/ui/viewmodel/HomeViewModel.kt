package com.idealink.vinty.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.idealink.vinty.BuildConfig
import com.idealink.vinty.data.model.SpinStatusResponse
import com.idealink.vinty.data.repository.VintyRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * One-time events for navigation and UI feedback
 */
sealed class HomeEvent {
    object NavigateToJackpot : HomeEvent()
    data class ShowMessage(val message: String) : HomeEvent()
}

class HomeViewModel(private val repository: VintyRepository) : ViewModel() {

    companion object {
        private const val NO_SPINS_MESSAGE = "You've used all your spins for today! Come back tomorrow for another try."
    }

    // Independent UI States
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Badge State
    data class BadgeState(val iconUrl: String?, val message: String?)
    private val _badgeStateProgress = MutableStateFlow<BadgeState?>(null)
    val badgeStateProgress: StateFlow<BadgeState?> = _badgeStateProgress.asStateFlow()

    // Spin Status
    private val _spinStatus = MutableStateFlow<SpinStatusResponse?>(null)
    val spinStatus: StateFlow<SpinStatusResponse?> = _spinStatus.asStateFlow()

    // One-time events
    private val _events = MutableSharedFlow<HomeEvent>()
    val events: SharedFlow<HomeEvent> = _events.asSharedFlow()


    /**
     * Fetch badge progress (ad watching progress)
     */
    fun fetchBadgeProgress() {
        viewModelScope.launch {
            try {
                val response = repository.getBadgeProgress()
                val baseUrl = BuildConfig.BASE_IMAGE_URL
                response.adWatchingProgress?.let { badge ->
                    val fullIconUrl = if (badge.iconUrl.startsWith("http")) {
                        badge.iconUrl
                    } else {
                        "$baseUrl${badge.iconUrl}"
                    }

                    _badgeStateProgress.value = BadgeState(fullIconUrl, badge.message)
                }
            } catch (_: Exception) {
                // Silent fail - home screen should not break
            }
        }
    }

    /**
     * Fetch spin status from API
     */
    fun fetchSpinStatus() {
        viewModelScope.launch {
            try {
                val status = repository.getSpinStatus()
                _spinStatus.value = status
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Handle spin button click
     */
    fun onSpinClicked() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                // Call API directly for fresh status
                val status = repository.getSpinStatus()
                _spinStatus.value = status // Update source of truth
                
                if (status.success) {
                    
                    if (status.spinsRemaining > 0 || status.extraSpins > 0) {
                        _events.emit(HomeEvent.NavigateToJackpot)
                    } else {
                        _events.emit(HomeEvent.ShowMessage(NO_SPINS_MESSAGE))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Optionally show error message
            } finally {
                _isLoading.value = false
            }
        }
    }
}
