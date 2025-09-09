package com.aics.violationapp.data.local.sync

import android.content.Context
import com.aics.violationapp.data.api.ApiService
import com.aics.violationapp.data.local.cache.CacheDatabase
import com.aics.violationapp.data.model.ViolationRequest
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import android.util.Log
import java.util.concurrent.atomic.AtomicBoolean

class SyncManager(
    private val context: Context,
    private val apiService: ApiService
) {
    private val database = CacheDatabase.getDatabase(context)
    private val pendingViolationDao = database.pendingViolationDao()
    private val pendingUpdateDao = database.pendingUpdateDao()
    private val syncLogDao = database.syncLogDao()
    
    private val gson = Gson()
    private val isSyncing = AtomicBoolean(false)
    private val syncScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    companion object {
        const val MAX_RETRY_COUNT = 3
        const val SYNC_BATCH_SIZE = 10
        const val SYNC_TIMEOUT_MS = 30000L
    }
    
    // Queue violation for offline submission
    suspend fun queueViolation(
        studentId: String,
        violations: List<String>,
        recordedBy: String,
        priority: Int = 1
    ): Long {
        val pendingViolation = PendingViolation(
            student_id = studentId,
            violations = gson.toJson(violations),
            recorded_by = recordedBy,
            priority = priority
        )
        
        val id = pendingViolationDao.insert(pendingViolation)
        Log.d("SyncManager", "Queued violation for student $studentId (ID: $id)")
        
        // Try immediate sync if network is available
        tryImmediateSync()
        
        return id
    }
    
    // Get pending violations count as Flow for UI
    fun getPendingViolationsFlow(): Flow<Int> {
        return pendingViolationDao.getPendingCountFlow()
    }
    
    // Start sync process
    suspend fun startSync(): Boolean {
        if (!isSyncing.compareAndSet(false, true)) {
            Log.d("SyncManager", "Sync already in progress")
            return false
        }
        
        return try {
            Log.d("SyncManager", "Starting sync process")
            val startTime = System.currentTimeMillis()
            
            withTimeout(SYNC_TIMEOUT_MS) {
                syncPendingViolations()
                syncPendingUpdates()
                cleanupOldData()
            }
            
            val duration = System.currentTimeMillis() - startTime
            Log.d("SyncManager", "Sync completed in ${duration}ms")
            
            logSyncOperation("FULL_SYNC", "ALL", null, SyncStatus.COMPLETED, null, duration)
            true
            
        } catch (e: Exception) {
            Log.e("SyncManager", "Sync failed: ${e.message}", e)
            logSyncOperation("FULL_SYNC", "ALL", null, SyncStatus.FAILED, e.message, 0)
            false
        } finally {
            isSyncing.set(false)
        }
    }
    
    private suspend fun syncPendingViolations() {
        val pendingViolations = pendingViolationDao.getPendingByStatuses(
            listOf(SyncStatus.PENDING, SyncStatus.FAILED)
        ).take(SYNC_BATCH_SIZE)
        
        for (violation in pendingViolations) {
            if (violation.retry_count >= MAX_RETRY_COUNT) {
                Log.w("SyncManager", "Max retries reached for violation ${violation.id}")
                pendingViolationDao.updateStatus(violation.id, SyncStatus.CANCELLED)
                continue
            }
            
            try {
                pendingViolationDao.updateStatus(violation.id, SyncStatus.SYNCING)
                
                val violations: List<String> = gson.fromJson(
                    violation.violations,
                    object : TypeToken<List<String>>() {}.type
                )
                
                val response = apiService.submitViolation(
                    ViolationRequest(
                        student_id = violation.student_id,
                        violations = violations,
                        recorded_by = violation.recorded_by
                    )
                )
                
                if (response.isSuccessful && response.body()?.success == true) {
                    pendingViolationDao.updateStatus(violation.id, SyncStatus.COMPLETED)
                    Log.d("SyncManager", "Successfully synced violation ${violation.id}")
                    
                    logSyncOperation(
                        "VIOLATION_SUBMIT", 
                        "violation", 
                        violation.id.toString(),
                        SyncStatus.COMPLETED,
                        null,
                        0
                    )
                } else {
                    val error = response.body()?.message ?: "Unknown error"
                    pendingViolationDao.incrementRetryCount(violation.id, error)
                    pendingViolationDao.updateStatus(violation.id, SyncStatus.FAILED)
                    
                    Log.w("SyncManager", "Failed to sync violation ${violation.id}: $error")
                }
                
            } catch (e: Exception) {
                pendingViolationDao.incrementRetryCount(violation.id, e.message ?: "Exception")
                pendingViolationDao.updateStatus(violation.id, SyncStatus.FAILED)
                
                Log.e("SyncManager", "Error syncing violation ${violation.id}: ${e.message}", e)
            }
            
            // Add small delay between requests to avoid overwhelming server
            delay(100)
        }
    }
    
    private suspend fun syncPendingUpdates() {
        val pendingUpdates = pendingUpdateDao.getPendingByStatus(SyncStatus.PENDING)
            .take(SYNC_BATCH_SIZE)
        
        for (update in pendingUpdates) {
            try {
                pendingUpdateDao.update(update.copy(sync_status = SyncStatus.SYNCING))
                
                // Process different types of updates
                when (update.entity_type) {
                    "student_cache_invalidate" -> {
                        // Invalidate student cache
                        Log.d("SyncManager", "Invalidating cache for ${update.entity_id}")
                        pendingUpdateDao.update(update.copy(sync_status = SyncStatus.COMPLETED))
                    }
                    // Add more update types as needed
                }
                
            } catch (e: Exception) {
                pendingUpdateDao.update(update.copy(sync_status = SyncStatus.FAILED))
                Log.e("SyncManager", "Error syncing update ${update.id}: ${e.message}", e)
            }
        }
    }
    
    private suspend fun cleanupOldData() {
        val oneWeekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
        val oneDayAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000L)
        
        pendingViolationDao.cleanupCompleted(oneWeekAgo)
        pendingUpdateDao.cleanupCompleted(oneWeekAgo)
        syncLogDao.cleanupOldLogs(oneDayAgo)
    }
    
    private suspend fun logSyncOperation(
        operation: String,
        entityType: String,
        entityId: String?,
        status: SyncStatus,
        errorMessage: String?,
        duration: Long
    ) {
        val log = SyncLog(
            operation = operation,
            entity_type = entityType,
            entity_id = entityId,
            status = status,
            error_message = errorMessage,
            sync_duration = duration
        )
        syncLogDao.insert(log)
    }
    
    private fun tryImmediateSync() {
        syncScope.launch {
            try {
                // Quick sync attempt for immediate feedback
                val pendingCount = pendingViolationDao.getPendingByStatus(SyncStatus.PENDING).size
                if (pendingCount > 0) {
                    startSync()
                }
            } catch (e: Exception) {
                Log.d("SyncManager", "Immediate sync failed, will retry later: ${e.message}")
            }
        }
    }
    
    // Get sync statistics
    suspend fun getSyncStats(): SyncStats {
        val oneDayAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000L)
        
        return SyncStats(
            pendingViolations = pendingViolationDao.getPendingByStatus(SyncStatus.PENDING).size,
            failedViolations = pendingViolationDao.getPendingByStatus(SyncStatus.FAILED).size,
            completedToday = syncLogDao.getStatusCount(SyncStatus.COMPLETED, oneDayAgo),
            failedToday = syncLogDao.getStatusCount(SyncStatus.FAILED, oneDayAgo),
            lastSyncTime = getLastSyncTime(),
            isSyncing = isSyncing.get()
        )
    }
    
    private suspend fun getLastSyncTime(): Long? {
        return syncLogDao.getRecentLogs(1).firstOrNull()?.created_at
    }
    
    fun cleanup() {
        syncScope.cancel()
    }
}

data class SyncStats(
    val pendingViolations: Int,
    val failedViolations: Int,
    val completedToday: Int,
    val failedToday: Int,
    val lastSyncTime: Long?,
    val isSyncing: Boolean
)