package com.idealink.vinty.ui.fragments.leaderboard

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.idealink.vinty.data.api.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.idealink.vinty.data.repository.VintyRepository
import com.idealink.vinty.data.model.LeaderboardResponse

class LeaderboardViewModel(private val repository: VintyRepository) : ViewModel() {

    private val _leaderboard = MutableStateFlow<LeaderboardResponse?>(null)
    val leaderboard: StateFlow<LeaderboardResponse?> = _leaderboard

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private var lastFetchTime = 0L
    private var isRefreshing = false

    fun hasLoadedOnce(): Boolean {
        return _leaderboard.value != null
    }

    fun loadLeaderboard() {
        val now = System.currentTimeMillis()

        // Fetch fresh data if needed
        if (_leaderboard.value == null || now - lastFetchTime > 30_000) {
            fetchLeaderboard(now)
        } else if (!isRefreshing) {
            // Silent background refresh
            viewModelScope.launch {
                isRefreshing = true
                try {
                    val response = repository.getLeaderboard("all-time", 50)
                    _leaderboard.value = response
                    lastFetchTime = now
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                isRefreshing = false
            }
        }
    }

    private fun fetchLeaderboard(now: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.getLeaderboard("all-time", 50)
                _leaderboard.value = response
                lastFetchTime = now
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}