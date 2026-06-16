package com.idealink.vinty.ui.fragments.jackpot

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.idealink.vinty.data.model.SpinResult
import com.idealink.vinty.data.repository.VintyRepository
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Response

/**
 * ViewModel for Jackpot fragment.
 * Handles API calls for saving spin results.
 */
class JackpotViewModel(
    private val repository: VintyRepository
) : ViewModel() {

    private val _spinResult = MutableLiveData<Result<SpinResult>?>()
    val spinResult: LiveData<Result<SpinResult>?> = _spinResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    /**
     * Save spin result to backend.
     * @param column1 First slot result
     * @param column2 Second slot result
     * @param column3 Third slot result
     */
    fun saveSpinResult(column1: String, column2: String, column3: String) {
        _isLoading.value = true

        val params = mapOf(
            "column1" to column1,
            "column2" to column2,
            "column3" to column3
        )

        viewModelScope.launch {
            try {
                val response = repository.saveSpinResult(params)

                if (response.isSuccessful && response.body() != null) {
                    _spinResult.value = Result.success(response.body()!!)
                } else {
                    _spinResult.value = Result.failure(Exception(extractErrorMessage(response)))
                }
            } catch (e: Exception) {
                _spinResult.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Reset the spin result state.
     */
    fun resetSpinResult() {
        _spinResult.value = null
    }

    private fun extractErrorMessage(response: Response<SpinResult>): String {
        val body = response.errorBody()?.string()
        body?.let {
            try {
                val json = JSONObject(it)
                if (json.has("error")) return json.getString("error")
                if (json.has("message")) return json.getString("message")
            } catch (_: Exception) {
                // fall through
            }
        }
        return "Something went wrong, please try again."
    }
}