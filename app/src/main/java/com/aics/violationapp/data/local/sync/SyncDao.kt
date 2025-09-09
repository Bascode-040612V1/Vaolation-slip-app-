package com.aics.violationapp.data.local.sync

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingViolationDao {
    
    @Query("SELECT * FROM pending_violations WHERE sync_status = :status ORDER BY priority ASC, created_at ASC")
    suspend fun getPendingByStatus(status: SyncStatus): List<PendingViolation>
    
    @Query("SELECT * FROM pending_violations WHERE sync_status IN (:statuses) ORDER BY priority ASC, created_at ASC")
    suspend fun getPendingByStatuses(statuses: List<SyncStatus>): List<PendingViolation>
    
    @Query("SELECT COUNT(*) FROM pending_violations WHERE sync_status = 'PENDING'")
    fun getPendingCountFlow(): Flow<Int>
    
    @Insert
    suspend fun insert(violation: PendingViolation): Long
    
    @Update
    suspend fun update(violation: PendingViolation)
    
    @Query("UPDATE pending_violations SET sync_status = :status WHERE id = :id")
    suspend fun updateStatus(id: Int, status: SyncStatus)
    
    @Query("UPDATE pending_violations SET retry_count = retry_count + 1, last_error = :error WHERE id = :id")
    suspend fun incrementRetryCount(id: Int, error: String)
    
    @Query("DELETE FROM pending_violations WHERE id = :id")
    suspend fun delete(id: Int)
    
    @Query("DELETE FROM pending_violations WHERE sync_status = 'COMPLETED' AND created_at < :olderThan")
    suspend fun cleanupCompleted(olderThan: Long)
}

@Dao
interface PendingUpdateDao {
    
    @Query("SELECT * FROM pending_updates WHERE sync_status = :status ORDER BY created_at ASC")
    suspend fun getPendingByStatus(status: SyncStatus): List<PendingUpdate>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(update: PendingUpdate): Long
    
    @Update
    suspend fun update(update: PendingUpdate)
    
    @Query("DELETE FROM pending_updates WHERE id = :id")
    suspend fun delete(id: Int)
    
    @Query("DELETE FROM pending_updates WHERE sync_status = 'COMPLETED' AND created_at < :olderThan")
    suspend fun cleanupCompleted(olderThan: Long)
}

@Dao
interface SyncLogDao {
    
    @Query("SELECT * FROM sync_log ORDER BY created_at DESC LIMIT :limit")
    suspend fun getRecentLogs(limit: Int = 100): List<SyncLog>
    
    @Query("SELECT COUNT(*) FROM sync_log WHERE status = :status AND created_at > :since")
    suspend fun getStatusCount(status: SyncStatus, since: Long): Int
    
    @Insert
    suspend fun insert(log: SyncLog)
    
    @Query("DELETE FROM sync_log WHERE created_at < :olderThan")
    suspend fun cleanupOldLogs(olderThan: Long)
}