package com.aics.violationapp.data.network

import android.content.Context
import com.aics.violationapp.data.api.ApiService
import com.aics.violationapp.data.local.cache.CacheManager
import com.aics.violationapp.data.local.sync.SyncManager
import com.aics.violationapp.data.local.analytics.PerformanceMonitor
import com.aics.violationapp.data.repository.ViolationRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {
    
    fun provideRetrofit(baseUrl: String): ApiService {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
        
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
    
    fun provideRepository(context: Context, baseUrl: String): ViolationRepository {
        val apiService = provideRetrofit(baseUrl)
        val cacheManager = CacheManager(context)
        val syncManager = SyncManager(context, apiService)
        val performanceMonitor = PerformanceMonitor(context)
        return ViolationRepository(apiService, cacheManager, syncManager, performanceMonitor)
    }
}
