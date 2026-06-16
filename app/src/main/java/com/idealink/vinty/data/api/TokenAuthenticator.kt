package com.idealink.vinty.data.api

import com.idealink.vinty.data.DataManager
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator(
    private val dataManager: DataManager,
    private val refreshApi: RefreshApiService
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {

        // Prevent infinite retry loops
        if (responseCount(response) >= 2) {
            dataManager.clearSession()
            return null
        }

        val refreshToken = dataManager.getRefreshToken()
            ?: return null

        val refreshResponse = try {
            refreshApi.refreshToken(
                mapOf("refreshToken" to refreshToken)
            ).execute()
        } catch (_: Exception) {
            return null
        }

        if (!refreshResponse.isSuccessful || refreshResponse.body() == null) {
            dataManager.clearSession()
            return null
        }

        val newTokens = refreshResponse.body()!!

        dataManager.saveTokens(
            newTokens.accessToken,
            newTokens.refreshToken
        )

        return response.request.newBuilder()
            .header("Authorization", "Bearer ${newTokens.accessToken}")
            .build()
    }

    private fun responseCount(response: Response): Int {
        var result = 1
        var prior = response.priorResponse
        while (prior != null) {
            result++
            prior = prior.priorResponse
        }
        return result
    }
}
