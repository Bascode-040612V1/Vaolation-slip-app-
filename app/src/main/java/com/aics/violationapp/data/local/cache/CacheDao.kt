package com.aics.violationapp.data.local.cache

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ViolationTypeCacheDao {
    
    @Query("SELECT * FROM cached_violation_types WHERE is_active = 1 ORDER BY category, violation_name")
    suspend fun getAllViolationTypes(): List<CachedViolationType>
    
    @Query("SELECT * FROM cached_violation_types WHERE category = :category AND is_active = 1")
    suspend fun getViolationTypesByCategory(category: String): List<CachedViolationType>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertViolationTypes(violationTypes: List<CachedViolationType>)
    
    @Query("DELETE FROM cached_violation_types")
    suspend fun clearAll()
    
    @Query("SELECT COUNT(*) FROM cached_violation_types")
    suspend fun getCount(): Int
    
    @Query("SELECT MAX(cached_at) FROM cached_violation_types")
    suspend fun getLastCacheTime(): Long?
}

@Dao
interface StudentCacheDao {
    
    @Query("SELECT * FROM cached_students WHERE student_id = :studentId AND expires_at > :currentTime LIMIT 1")
    suspend fun getStudent(studentId: String, currentTime: Long = System.currentTimeMillis()): CachedStudent?
    
    @Query("SELECT * FROM cached_students WHERE expires_at > :currentTime ORDER BY cached_at DESC LIMIT :limit")
    suspend fun getRecentStudents(currentTime: Long = System.currentTimeMillis(), limit: Int = 20): List<CachedStudent>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: CachedStudent)
    
    @Query("DELETE FROM cached_students WHERE expires_at < :currentTime")
    suspend fun cleanupExpired(currentTime: Long = System.currentTimeMillis())
    
    @Query("DELETE FROM cached_students WHERE student_id = :studentId")
    suspend fun removeStudent(studentId: String)
}

@Dao
interface OffenseCountCacheDao {
    
    @Query("SELECT * FROM cached_offense_counts WHERE student_id = :studentId AND expires_at > :currentTime")
    suspend fun getOffenseCounts(studentId: String, currentTime: Long = System.currentTimeMillis()): List<CachedOffenseCount>
    
    @Query("SELECT * FROM cached_offense_counts WHERE student_id = :studentId AND violation_type = :violationType AND expires_at > :currentTime LIMIT 1")
    suspend fun getOffenseCount(studentId: String, violationType: String, currentTime: Long = System.currentTimeMillis()): CachedOffenseCount?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOffenseCounts(offenseCounts: List<CachedOffenseCount>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOffenseCount(offenseCount: CachedOffenseCount)
    
    @Query("DELETE FROM cached_offense_counts WHERE expires_at < :currentTime")
    suspend fun cleanupExpired(currentTime: Long = System.currentTimeMillis())
    
    @Query("DELETE FROM cached_offense_counts WHERE student_id = :studentId")
    suspend fun removeStudentOffenseCounts(studentId: String)
}

@Dao
interface SyncMetadataDao {
    
    @Query("SELECT * FROM sync_metadata WHERE key = :key LIMIT 1")
    suspend fun getSyncMetadata(key: String): SyncMetadata?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyncMetadata(metadata: SyncMetadata)
    
    @Query("DELETE FROM sync_metadata WHERE expires_at < :currentTime")
    suspend fun cleanupExpired(currentTime: Long = System.currentTimeMillis())
}