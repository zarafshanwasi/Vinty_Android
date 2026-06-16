package com.idealink.vinty.data.api

import android.content.Context
import com.idealink.vinty.BuildConfig
import com.idealink.vinty.data.DataManager
import com.idealink.vinty.data.repository.VintyRepository
import com.idealink.vinty.data.repository.VintyRepositoryImpl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {

    private const val BASE_URL = BuildConfig.API_URL

    private lateinit var dataManager: DataManager

    fun init(context: Context) {
        DataManager.initialize(context)
        dataManager = DataManager.getInstance()
    }

    // ---------------------------------------
    // Cloudflare Header Interceptor
    // ---------------------------------------
    private class CloudflareHeaderInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request().newBuilder()
                .header("User-Agent", "Vinty-Android-App")
                .header("Accept", "application/json")
                .header("Connection", "keep-alive")
                .build()

            return chain.proceed(request)
        }
    }

    // ---------------------------------------
    // REFRESH CLIENT (NO auth, NO authenticator)
    // ---------------------------------------
    private val refreshClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(CloudflareHeaderInterceptor())
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private val refreshRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(refreshClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val refreshApi: RefreshApiService by lazy {
        refreshRetrofit.create(RefreshApiService::class.java)
    }

    // ---------------------------------------
    // MAIN OKHTTP CLIENT
    // ---------------------------------------
    private val okHttpClient: OkHttpClient by lazy {

        val builder = OkHttpClient.Builder()
            .addInterceptor(CloudflareHeaderInterceptor())
            .addInterceptor(AuthInterceptor(dataManager))
            .authenticator(
                TokenAuthenticator(
                    dataManager,
                    refreshApi
                )
            )
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .callTimeout(60, TimeUnit.SECONDS)

        if (BuildConfig.DEBUG) {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            builder.addInterceptor(logging)
        }

        builder.build()
    }

    // ---------------------------------------
    // MAIN RETROFIT
    // ---------------------------------------
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: VintyApiService by lazy {
        retrofit.create(VintyApiService::class.java)
    }

    fun getRepository(): VintyRepository {
        return VintyRepositoryImpl(api)
    }
}
