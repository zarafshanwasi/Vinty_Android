package com.idealink.vinty.ui.fragments.mission

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.idealink.vinty.data.model.Mission
import com.idealink.vinty.data.model.MissionsResponse
import com.idealink.vinty.data.repository.VintyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Response

class MissionViewModel(private val repository: VintyRepository) : ViewModel() {

    private val _missions = MutableStateFlow<List<Mission>>(emptyList())
    val missions: StateFlow<List<Mission>> = _missions

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadMissions() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.getMissions()
                handleMissionsResponse(response)
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun handleMissionsResponse(response: Response<MissionsResponse>) {
        if (response.isSuccessful) {
            val missionsBody = response.body()
            missionsBody?.let {
                val dailyList = it.daily ?: emptyList()
                val adWatchingList = it.continuous?.adWatching ?: emptyList()
                val roomEntryList = it.continuous?.roomEntry ?: emptyList()
                _missions.value = dailyList + adWatchingList + roomEntryList
            }
        } else {
            _error.value = "Failed to load missions: ${response.code()}"
        }
    }

    fun claimMission(missionId: String, onResult: (Boolean, String?, Int?, Int?) -> Unit) {
        viewModelScope.launch {
            try {
                val response = repository.claimMission(mapOf("missionProgressId" to missionId))
                if (response.isSuccessful) {
                    _missions.value = _missions.value.map { mission ->
                        if (mission.id == missionId) {
                            mission.copy(claimed = true, completed = true, isClaimed = true, isCompleted = true)
                        } else {
                            mission
                        }
                    }
                    val body = response.body()
                    onResult(true, null, body?.updatedTickets, body?.updatedGems)
                } else {
                    onResult(false, response.errorBody()?.string(), null, null)
                }
            } catch (e: Exception) {
                onResult(false, e.message, null, null)
            }
        }
    }

//    fun recordAdView(adNetwork: String, adType: String) {
////        viewModelScope.launch {
////            try {
////                repository.recordAdView(AdViewRequest(adNetwork, adType))
////            } catch (e: Exception) {
////                // Silent error
////            }
////        }
//    }

//    fun gainXp(xp: Int) {
//        viewModelScope.launch {
//            try {
//                repository.gainXp(com.idealink.vinty.data.model.GainXpRequest(xp))
//            } catch (e: Exception) {
//                // Silent error
//            }
//        }
//    }
}