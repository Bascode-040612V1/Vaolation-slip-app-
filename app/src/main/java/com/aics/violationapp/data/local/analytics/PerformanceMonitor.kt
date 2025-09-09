package com.aics.violationapp.data.local.analytics

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.system.measureTimeMillis

class PerformanceMonitor(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("performance_monitor", Context.MODE_PRIVATE)
    
    companion object {
        // Metric keys
        private const val KEY_API_CALL_COUNT = "api_call_count"
        private const val KEY_CACHE_HIT_COUNT = "cache_hit_count"
        private const val KEY_CACHE_MISS_COUNT = "cache_miss_count"
        private const val KEY_TOTAL_API_TIME = "total_api_time"
        private const val KEY_TOTAL_CACHE_TIME = "total_cache_time"
        private const val KEY_SESSION_START = "session_start"
        private const val KEY_VIOLATION_SUBMIT_COUNT = "violation_submit_count"
        private const val KEY_OFFLINE_QUEUE_COUNT = "offline_queue_count"
        
        // Operation types
        const val OP_API_CALL = "api_call"
        const val OP_CACHE_HIT = "cache_hit"
        const val OP_CACHE_MISS = "cache_miss"
        const val OP_VIOLATION_SUBMIT = "violation_submit"
        const val OP_OFFLINE_QUEUE = "offline_queue"
    }
    
    init {
        // Start new session
        if (!prefs.contains(KEY_SESSION_START)) {
            prefs.edit().putLong(KEY_SESSION_START, System.currentTimeMillis()).apply()
        }
    }
    
    suspend fun <T> measureOperation(
        operation: String,
        block: suspend () -> T
    ): T = withContext(Dispatchers.IO) {
        val duration = measureTimeMillis {
            return@measureTimeMillis block()
        }
        
        recordMetric(operation, duration)
        block()
    }
    
    fun recordMetric(operation: String, duration: Long = 0, count: Int = 1) {
        val editor = prefs.edit()
        
        when (operation) {
            OP_API_CALL -> {
                editor.putInt(KEY_API_CALL_COUNT, prefs.getInt(KEY_API_CALL_COUNT, 0) + count)
                editor.putLong(KEY_TOTAL_API_TIME, prefs.getLong(KEY_TOTAL_API_TIME, 0) + duration)
            }
            OP_CACHE_HIT -> {
                editor.putInt(KEY_CACHE_HIT_COUNT, prefs.getInt(KEY_CACHE_HIT_COUNT, 0) + count)
                editor.putLong(KEY_TOTAL_CACHE_TIME, prefs.getLong(KEY_TOTAL_CACHE_TIME, 0) + duration)
            }
            OP_CACHE_MISS -> {
                editor.putInt(KEY_CACHE_MISS_COUNT, prefs.getInt(KEY_CACHE_MISS_COUNT, 0) + count)
            }
            OP_VIOLATION_SUBMIT -> {
                editor.putInt(KEY_VIOLATION_SUBMIT_COUNT, prefs.getInt(KEY_VIOLATION_SUBMIT_COUNT, 0) + count)
            }
            OP_OFFLINE_QUEUE -> {
                editor.putInt(KEY_OFFLINE_QUEUE_COUNT, prefs.getInt(KEY_OFFLINE_QUEUE_COUNT, 0) + count)
            }
        }
        
        editor.apply()
    }
    
    fun getPerformanceMetrics(): PerformanceMetrics {
        val sessionStart = prefs.getLong(KEY_SESSION_START, System.currentTimeMillis())
        val sessionDuration = System.currentTimeMillis() - sessionStart
        
        val apiCallCount = prefs.getInt(KEY_API_CALL_COUNT, 0)
        val cacheHitCount = prefs.getInt(KEY_CACHE_HIT_COUNT, 0)
        val cacheMissCount = prefs.getInt(KEY_CACHE_MISS_COUNT, 0)
        val totalApiTime = prefs.getLong(KEY_TOTAL_API_TIME, 0)
        val totalCacheTime = prefs.getLong(KEY_TOTAL_CACHE_TIME, 0)
        val violationSubmitCount = prefs.getInt(KEY_VIOLATION_SUBMIT_COUNT, 0)
        val offlineQueueCount = prefs.getInt(KEY_OFFLINE_QUEUE_COUNT, 0)
        
        val totalCacheRequests = cacheHitCount + cacheMissCount
        val cacheHitRate = if (totalCacheRequests > 0) {
            (cacheHitCount.toFloat() / totalCacheRequests * 100)
        } else 0f
        
        val avgApiTime = if (apiCallCount > 0) totalApiTime / apiCallCount else 0L
        val avgCacheTime = if (cacheHitCount > 0) totalCacheTime / cacheHitCount else 0L
        
        return PerformanceMetrics(
            sessionDuration = sessionDuration,
            apiCallCount = apiCallCount,
            cacheHitCount = cacheHitCount,
            cacheMissCount = cacheMissCount,
            cacheHitRate = cacheHitRate,
            totalApiTime = totalApiTime,
            totalCacheTime = totalCacheTime,
            averageApiTime = avgApiTime,
            averageCacheTime = avgCacheTime,
            violationSubmitCount = violationSubmitCount,
            offlineQueueCount = offlineQueueCount,
            networkSavings = calculateNetworkSavings(cacheHitCount, avgApiTime)
        )
    }
    
    private fun calculateNetworkSavings(cacheHits: Int, avgApiTime: Long): NetworkSavings {
        val savedRequests = cacheHits
        val savedTime = cacheHits * avgApiTime
        val estimatedDataSaved = cacheHits * 2048L // Estimate 2KB per request saved
        
        return NetworkSavings(
            savedRequests = savedRequests,
            savedTimeMs = savedTime,
            estimatedDataSavedBytes = estimatedDataSaved
        )
    }
    
    fun resetMetrics() {
        prefs.edit().clear().apply()
        prefs.edit().putLong(KEY_SESSION_START, System.currentTimeMillis()).apply()
    }
    
    fun exportMetricsAsString(): String {
        val metrics = getPerformanceMetrics()
        return buildString {
            appendLine("=== Performance Metrics ===")
            appendLine("Session Duration: ${metrics.sessionDuration / 1000}s")
            appendLine("API Calls: ${metrics.apiCallCount}")
            appendLine("Cache Hits: ${metrics.cacheHitCount}")
            appendLine("Cache Misses: ${metrics.cacheMissCount}")
            appendLine("Cache Hit Rate: ${"%.1f".format(metrics.cacheHitRate)}%")
            appendLine("Avg API Time: ${metrics.averageApiTime}ms")
            appendLine("Avg Cache Time: ${metrics.averageCacheTime}ms")
            appendLine("Violations Submitted: ${metrics.violationSubmitCount}")
            appendLine("Offline Queued: ${metrics.offlineQueueCount}")
            appendLine("Saved Requests: ${metrics.networkSavings.savedRequests}")
            appendLine("Saved Time: ${metrics.networkSavings.savedTimeMs / 1000}s")
            appendLine("Estimated Data Saved: ${metrics.networkSavings.estimatedDataSavedBytes / 1024}KB")
        }
    }
}

data class PerformanceMetrics(
    val sessionDuration: Long,
    val apiCallCount: Int,
    val cacheHitCount: Int,
    val cacheMissCount: Int,
    val cacheHitRate: Float,
    val totalApiTime: Long,
    val totalCacheTime: Long,
    val averageApiTime: Long,
    val averageCacheTime: Long,
    val violationSubmitCount: Int,
    val offlineQueueCount: Int,
    val networkSavings: NetworkSavings
)

data class NetworkSavings(
    val savedRequests: Int,
    val savedTimeMs: Long,
    val estimatedDataSavedBytes: Long
)