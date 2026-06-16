package com.idealink.vinty.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.idealink.vinty.data.api.ApiException
import com.idealink.vinty.data.model.ChangePasswordRequest
import com.idealink.vinty.data.repository.VintyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException

class ChangePasswordViewModel(
    private val repository: VintyRepository
) : ViewModel() {

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _result = MutableStateFlow<Result<String>?>(null)
    val result: StateFlow<Result<String>?> = _result

    fun changePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = repository.changePassword(
                    ChangePasswordRequest(
                        currentPassword = currentPassword,
                        newPassword = newPassword
                    )
                )
                _result.value = Result.success(response.message)

            } catch (e: Exception) {
                _result.value = Result.failure(Throwable(extractErrorMessage(e)))
            } finally {
                _loading.value = false
            }
        }
    }

    fun clearResult() {
        _result.value = null
    }

    private fun extractErrorMessage(e: Exception): String {
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
        return "Something went wrong"
    }
}
