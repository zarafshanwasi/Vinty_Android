package com.idealink.vinty.data.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface RefreshApiService {
    @POST("auth/refresh")
    fun refreshToken(
        @Body body: Map<String, String>
    ): Call<TokenResponse>
}

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String
)

