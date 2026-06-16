package com.idealink.vinty.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.idealink.vinty.data.model.Badge
import com.idealink.vinty.data.model.ClaimBadgeResponse
import com.idealink.vinty.data.repository.VintyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BadgeViewModel(private val repository: VintyRepository) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _badges = MutableStateFlow<List<Badge>>(emptyList())
    val badges: StateFlow<List<Badge>> = _badges

    fun loadBadges() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = repository.getBadgeGallery()

                if (response.isSuccessful) {
                    val badgeGallery = response.body()

                    if (badgeGallery?.success == true) {
                        val allBadges = mutableListOf<Badge>()

                        badgeGallery.data.forEach { (_, badges) ->
                            allBadges.addAll(badges)
                        }

                        //  IMPORTANT: Keep API order EXACTLY
                        _badges.value = allBadges
                    } else {
                        _error.value = "Failed to load badges"
                    }
                } else {
                    _error.value = "Server error ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    private val _claimResult = MutableStateFlow<ClaimBadgeResponse?>(null)
    val claimResult: StateFlow<ClaimBadgeResponse?> = _claimResult

    fun claimBadgeReward(badgeId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.claimBadgeReward(badgeId)
                if (response.isSuccessful && response.body()?.success == true) {
                    _claimResult.value = response.body()
                } else {
                    _error.value = response.body()?.message ?: "Failed to claim reward"
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearClaimResult() {
        _claimResult.value = null
    }


    fun clearError() {
        _error.value = null
    }
}
