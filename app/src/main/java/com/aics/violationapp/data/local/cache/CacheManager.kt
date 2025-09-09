package com.aics.violationapp.data.local.cache

import android.content.Context
import com.aics.violationapp.data.model.Student
import com.aics.violationapp.data.model.ViolationType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.system.measureTimeMillis

class CacheManager(context: Context) {
    
    private val database = CacheDatabase.getDatabase(context)
    private val violationTypeDao = database.violationTypeDao()
    private val studentDao = database.studentDao()
    private val offenseCountDao = database.offenseCountDao()
    private val syncMetadataDao = database.syncMetadataDao()
    
    // Cache expiration times (in milliseconds)
    companion object {
        const val VIOLATION_TYPES_CACHE_DURATION = 24 * 60 * 60 * 1000L // 24 hours
        const val STUDENT_CACHE_DURATION = 60 * 60 * 1000L // 1 hour
        const val OFFENSE_COUNT_CACHE_DURATION = 30 * 60 * 1000L // 30 minutes
        
        const val SYNC_KEY_VIOLATION_TYPES = "violation_types"
        const val SYNC_KEY_STUDENT_PREFIX = "student_"
        const val SYNC_KEY_OFFENSE_PREFIX = "offense_"
    }
    
    // Violation Types Caching
    suspend fun getCachedViolationTypes(): List<ViolationType>? = withContext(Dispatchers.IO) {
        val metadata = syncMetadataDao.getSyncMetadata(SYNC_KEY_VIOLATION_TYPES)
        
        if (metadata != null && System.currentTimeMillis() < metadata.expires_at) {
            val cached = violationTypeDao.getAllViolationTypes()
            cached.map { 
                ViolationType(
                    id = it.id,
                    violation_name = it.violation_name,
                    category = it.category,
                    penalty_description = it.penalty_description,
                    is_active = it.is_active
                )
            }.takeIf { it.isNotEmpty() }
        } else null
    }
    
    suspend fun cacheViolationTypes(violationTypes: List<ViolationType>) = withContext(Dispatchers.IO) {
        val cached = violationTypes.map { 
            CachedViolationType(
                id = it.id,
                violation_name = it.violation_name,
                category = it.category,
                penalty_description = it.penalty_description,
                is_active = it.is_active
            )
        }
        
        violationTypeDao.clearAll()
        violationTypeDao.insertViolationTypes(cached)
        
        syncMetadataDao.insertSyncMetadata(
            SyncMetadata(
                key = SYNC_KEY_VIOLATION_TYPES,
                last_sync = System.currentTimeMillis(),
                expires_at = System.currentTimeMillis() + VIOLATION_TYPES_CACHE_DURATION
            )
        )
    }
    
    // Student Caching
    suspend fun getCachedStudent(studentId: String): Student? = withContext(Dispatchers.IO) {
        studentDao.getStudent(studentId)?.let { cached ->
            Student(
                id = cached.id,
                student_id = cached.student_id,
                student_name = cached.student_name,
                year_level = cached.year_level,
                course = cached.course,
                section = cached.section,
                image = cached.image
            )
        }
    }
    
    suspend fun cacheStudent(student: Student) = withContext(Dispatchers.IO) {
        val cached = CachedStudent(
            id = student.id,
            student_id = student.student_id,
            student_name = student.student_name,
            year_level = student.year_level,
            course = student.course,
            section = student.section,
            image = student.image,
            expires_at = System.currentTimeMillis() + STUDENT_CACHE_DURATION
        )
        
        studentDao.insertStudent(cached)
    }
    
    // Offense Count Caching
    suspend fun getCachedOffenseCounts(studentId: String): Map<String, Int>? = withContext(Dispatchers.IO) {
        val cached = offenseCountDao.getOffenseCounts(studentId)
        if (cached.isNotEmpty()) {
            cached.associate { it.violation_type to it.offense_count }
        } else null
    }
    
    suspend fun cacheOffenseCounts(studentId: String, offenseCounts: Map<String, Int>) = withContext(Dispatchers.IO) {
        val cached = offenseCounts.map { (violationType, count) ->
            CachedOffenseCount(
                student_id = studentId,
                violation_type = violationType,
                offense_count = count,
                expires_at = System.currentTimeMillis() + OFFENSE_COUNT_CACHE_DURATION
            )
        }
        
        // Remove existing offense counts for this student
        offenseCountDao.removeStudentOffenseCounts(studentId)
        offenseCountDao.insertOffenseCounts(cached)
    }
    
    // Cache Management
    suspend fun cleanupExpiredData() = withContext(Dispatchers.IO) {
        val currentTime = System.currentTimeMillis()
        studentDao.cleanupExpired(currentTime)
        offenseCountDao.cleanupExpired(currentTime)
        syncMetadataDao.cleanupExpired(currentTime)
    }
    
    suspend fun getCacheStats(): CacheStats = withContext(Dispatchers.IO) {
        val violationTypeCount = violationTypeDao.getCount()
        val violationTypesLastSync = syncMetadataDao.getSyncMetadata(SYNC_KEY_VIOLATION_TYPES)?.last_sync
        val recentStudents = studentDao.getRecentStudents(limit = 10)
        
        CacheStats(
            violationTypesCount = violationTypeCount,
            violationTypesLastSync = violationTypesLastSync,
            recentStudentsCount = recentStudents.size,
            cacheSize = estimateCacheSize()
        )
    }
    
    private suspend fun estimateCacheSize(): Long = withContext(Dispatchers.IO) {
        // Rough estimation of cache size in bytes
        val violationTypeCount = violationTypeDao.getCount()
        val studentCount = studentDao.getRecentStudents(limit = Int.MAX_VALUE).size
        // Get total offense count entries by querying with current time (all non-expired entries)
        val offenseCountTotal = try {
            // Since we can't get all offense counts directly, estimate based on recent students
            studentCount * 5 // Estimate 5 offense types per student on average
        } catch (e: Exception) {
            0
        }
        
        // Estimate average sizes
        (violationTypeCount * 100) + (studentCount * 200) + (offenseCountTotal * 50)
    }
    
    suspend fun clearAllCache() = withContext(Dispatchers.IO) {
        violationTypeDao.clearAll()
        studentDao.cleanupExpired(Long.MAX_VALUE) // Clear all
        offenseCountDao.cleanupExpired(Long.MAX_VALUE) // Clear all
        syncMetadataDao.cleanupExpired(Long.MAX_VALUE) // Clear all
    }
}

data class CacheStats(
    val violationTypesCount: Int,
    val violationTypesLastSync: Long?,
    val recentStudentsCount: Int,
    val cacheSize: Long
)