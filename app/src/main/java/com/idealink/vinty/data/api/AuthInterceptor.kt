package com.idealink.vinty.data.api

import com.idealink.vinty.data.DataManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val dataManager: DataManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = dataManager.getAccessToken()

        val request = if (!token.isNullOrEmpty()) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }

        return chain.proceed(request)
    }
}
