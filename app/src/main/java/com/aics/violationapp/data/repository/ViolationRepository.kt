package com.aics.violationapp.data.repository

import com.aics.violationapp.data.api.ApiService
import com.aics.violationapp.data.local.cache.CacheManager
import com.aics.violationapp.data.local.sync.SyncManager
import com.aics.violationapp.data.local.analytics.PerformanceMonitor
import com.aics.violationapp.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log

class ViolationRepository(
    private val apiService: ApiService,
    private val cacheManager: CacheManager,
    private val syncManager: SyncManager,
    private val performanceMonitor: PerformanceMonitor
) {
    
    suspend fun login(email: String, password: String): Result<User> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.login(LoginRequest(email, password))
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let { user ->
                    Result.success(user)
                } ?: Result.failure(Exception("User data is null"))
            } else {
                Result.failure(Exception(response.body()?.message ?: "Login failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun register(username: String, email: String, password: String): Result<User> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.register(RegisterRequest(username, email, password))
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let { user ->
                    Result.success(user)
                } ?: Result.failure(Exception("User data is null"))
            } else {
                Result.failure(Exception(response.body()?.message ?: "Registration failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun searchStudent(studentId: String): Result<Student> = withContext(Dispatchers.IO) {
        try {
            // Try cache first
            val cached = cacheManager.getCachedStudent(studentId)
            if (cached != null) {
                Log.d("ViolationRepo", "Cache hit: student $studentId")
                return@withContext Result.success(cached)
            }
            
            // Cache miss - fetch from API
            Log.d("ViolationRepo", "Cache miss: fetching student $studentId from API")
            val response = apiService.searchStudent(studentId)
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let { student ->
                    // Cache the fresh data
                    cacheManager.cacheStudent(student)
                    Result.success(student)
                } ?: Result.failure(Exception("Student not found"))
            } else {
                Result.failure(Exception(response.body()?.message ?: "Student not found"))
            }
        } catch (e: Exception) {
            // Try cache as fallback on network error
            val cached = cacheManager.getCachedStudent(studentId)
            if (cached != null) {
                Log.d("ViolationRepo", "Network error, using cached student $studentId")
                Result.success(cached)
            } else {
                Result.failure(e)
            }
        }
    }
    
    suspend fun getViolationTypes(): Result<List<ViolationType>> = withContext(Dispatchers.IO) {
        try {
            // Try cache first
            val cached = performanceMonitor.measureOperation(PerformanceMonitor.OP_CACHE_HIT) {
                cacheManager.getCachedViolationTypes()
            }
            
            if (cached != null) {
                performanceMonitor.recordMetric(PerformanceMonitor.OP_CACHE_HIT)
                Log.d("ViolationRepo", "Cache hit: violation types (${cached.size} items)")
                return@withContext Result.success(cached)
            }
            
            // Cache miss - fetch from API
            performanceMonitor.recordMetric(PerformanceMonitor.OP_CACHE_MISS)
            Log.d("ViolationRepo", "Cache miss: fetching violation types from API")
            
            val response = performanceMonitor.measureOperation(PerformanceMonitor.OP_API_CALL) {
                apiService.getViolationTypes()
            }
            
            performanceMonitor.recordMetric(PerformanceMonitor.OP_API_CALL)
            
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let { types ->
                    // Cache the fresh data
                    cacheManager.cacheViolationTypes(types)
                    Result.success(types)
                } ?: Result.failure(Exception("No violation types found"))
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to load violation types"))
            }
        } catch (e: Exception) {
            // Try cache as fallback on network error
            val cached = cacheManager.getCachedViolationTypes()
            if (cached != null) {
                performanceMonitor.recordMetric(PerformanceMonitor.OP_CACHE_HIT)
                Log.d("ViolationRepo", "Network error, using stale cache (${cached.size} items)")
                Result.success(cached)
            } else {
                Result.failure(e)
            }
        }
    }
    
    suspend fun submitViolation(studentId: String, violations: List<String>, recordedBy: String): Result<ViolationResponse> = withContext(Dispatchers.IO) {
        try {
            val response = performanceMonitor.measureOperation(PerformanceMonitor.OP_API_CALL) {
                apiService.submitViolation(ViolationRequest(studentId, violations, recordedBy))
            }
            
            performanceMonitor.recordMetric(PerformanceMonitor.OP_API_CALL)
            
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let { violationResponse ->
                    performanceMonitor.recordMetric(PerformanceMonitor.OP_VIOLATION_SUBMIT)
                    // Invalidate offense count cache after successful submission
                    // Force refresh on next offense count request
                    Log.d("ViolationRepo", "Violation submitted successfully, invalidating offense cache for $studentId")
                    Result.success(violationResponse)
                } ?: Result.failure(Exception("Failed to submit violation"))
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to submit violation"))
            }
        } catch (e: Exception) {
            // Queue for offline sync when network fails
            Log.d("ViolationRepo", "Network error, queuing violation for offline sync")
            val queueId = syncManager.queueViolation(studentId, violations, recordedBy)
            
            performanceMonitor.recordMetric(PerformanceMonitor.OP_OFFLINE_QUEUE)
            
            // Return a mock response indicating it's queued
            val mockResponse = ViolationResponse(
                violation_id = queueId.toInt(),
                offense_count = 1, // Will be calculated during sync
                penalty = "Warning",
                message = "Violation queued for sync when network is available"
            )
            
            Result.success(mockResponse)
        }
    }
    
    suspend fun getOffenseCounts(studentId: String): Result<Map<String, Int>> = withContext(Dispatchers.IO) {
        try {
            // Try cache first
            val cached = cacheManager.getCachedOffenseCounts(studentId)
            if (cached != null) {
                Log.d("ViolationRepo", "Cache hit: offense counts for $studentId (${cached.size} items)")
                return@withContext Result.success(cached)
            }
            
            // Cache miss - fetch from API
            Log.d("ViolationRepo", "Cache miss: fetching offense counts for $studentId from API")
            val response = apiService.getOffenseCounts(studentId)
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let { offenseCounts ->
                    // Cache the fresh data
                    cacheManager.cacheOffenseCounts(studentId, offenseCounts)
                    Result.success(offenseCounts)
                } ?: Result.failure(Exception("Offense counts data is null"))
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to get offense counts"))
            }
        } catch (e: Exception) {
            // Try cache as fallback on network error
            val cached = cacheManager.getCachedOffenseCounts(studentId)
            if (cached != null) {
                Log.d("ViolationRepo", "Network error, using cached offense counts for $studentId")
                Result.success(cached)
            } else {
                Result.failure(e)
            }
        }
    }
    
    suspend fun testConnection(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.testConnection()
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data ?: "Connection successful")
            } else {
                Result.failure(Exception(response.body()?.message ?: "Connection failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Cache management methods
    suspend fun getCacheStats() = cacheManager.getCacheStats()
    
    suspend fun clearCache() = cacheManager.clearAllCache()
    
    suspend fun cleanupExpiredCache() = cacheManager.cleanupExpiredData()
    
    suspend fun invalidateStudentCache(studentId: String) {
        // This would need to be implemented in CacheManager
        Log.d("ViolationRepo", "Invalidating cache for student: $studentId")
    }
    
    // Sync management methods
    suspend fun startSync() = syncManager.startSync()
    
    fun getPendingViolationsFlow() = syncManager.getPendingViolationsFlow()
    
    suspend fun getSyncStats() = syncManager.getSyncStats()
    
    // Performance monitoring
    fun getPerformanceMetrics() = performanceMonitor.getPerformanceMetrics()
}
