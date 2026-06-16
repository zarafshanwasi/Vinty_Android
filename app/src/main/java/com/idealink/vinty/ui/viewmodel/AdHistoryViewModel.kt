package com.idealink.vinty.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.idealink.vinty.data.repository.VintyRepository
import com.idealink.vinty.data.model.*
import com.idealink.vinty.ui.fragments.ad.AdHistory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AdHistoryViewModel(private val repository: VintyRepository) : ViewModel() {

    private val _adHistory = MutableStateFlow<List<AdHistory>>(emptyList())
    val adHistory: StateFlow<List<AdHistory>> = _adHistory

    private val _lotteryHistory = MutableStateFlow<List<LotteryHistory>>(emptyList())
    val lotteryHistory: StateFlow<List<LotteryHistory>> = _lotteryHistory

    private val _rewardHistory = MutableStateFlow<List<RewardHistory>>(emptyList())
    val rewardHistory: StateFlow<List<RewardHistory>> = _rewardHistory

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadAdHistory() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = repository.getAdHistory()
                if (response.isSuccessful) {
                    _adHistory.value = response.body() ?: emptyList()
                } else {
                    _error.value = "Failed to load ad history"
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadLotteryHistory() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = repository.getLotteryHistory()
                if (response.isSuccessful) {
                    _lotteryHistory.value = response.body() ?: emptyList()
                } else {
                    _error.value = "Failed to load lottery history"
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadRewardHistory() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = repository.getRewardHistory()
                if (response.isSuccessful) {
                    _rewardHistory.value = response.body() ?: emptyList()
                } else {
                    _error.value = "Failed to load reward history"
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
