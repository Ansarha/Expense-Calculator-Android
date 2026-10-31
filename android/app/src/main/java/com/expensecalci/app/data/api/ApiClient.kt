package com.expensecalci.app.data.api

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    // Cloud Vercel deployment URL (default)
    const val VERCEL_CLOUD_URL = "https://explicitto.vercel.app/api/v1/"
    // Local / Emulator fallback URLs
    const val EMULATOR_URL = "http://10.0.2.2:8000/api/v1/"
    const val LOCALHOST_URL = "http://localhost:8000/api/v1/"

    var baseUrl: String = VERCEL_CLOUD_URL
        private set

    var authToken: String? = null

    fun clearAuth() {
        authToken = null
    }

    private var retrofit: Retrofit? = null
    private var service: ExpenseApiService? = null

    fun updateBaseUrl(newUrl: String) {
        val sanitized = if (!newUrl.endsWith("/")) "$newUrl/" else newUrl
        if (baseUrl != sanitized) {
            baseUrl = sanitized
            retrofit = null
            service = null
        }
    }

    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val builder = original.newBuilder()
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")

        authToken?.let { token ->
            if (token.isNotBlank()) {
                builder.header("Authorization", "Bearer $token")
            }
        }

        chain.proceed(builder.build())
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    fun getService(): ExpenseApiService {
        val existing = service
        if (existing != null) return existing

        val newRetrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val newService = newRetrofit.create(ExpenseApiService::class.java)
        retrofit = newRetrofit
        service = newService
        return newService
    }
}
