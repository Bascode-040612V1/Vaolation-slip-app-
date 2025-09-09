package com.aics.violationapp.data.local.cache

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "cached_violation_types",
    indices = [Index(value = ["category"])]
)
data class CachedViolationType(
    @PrimaryKey val id: Int,
    val violation_name: String,
    val category: String,
    val penalty_description: String?,
    val is_active: Boolean = true,
    val cached_at: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "cached_students",
    indices = [Index(value = ["student_id"], unique = true)]
)
data class CachedStudent(
    @PrimaryKey val id: Int,
    val student_id: String,
    val student_name: String,
    val year_level: String,
    val course: String,
    val section: String,
    val image: String? = null,
    val cached_at: Long = System.currentTimeMillis(),
    val expires_at: Long = System.currentTimeMillis() + 3600000L // 1 hour default
)

@Entity(
    tableName = "cached_offense_counts",
    indices = [Index(value = ["student_id", "violation_type"], unique = true)]
)
data class CachedOffenseCount(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val student_id: String,
    val violation_type: String,
    val offense_count: Int,
    val cached_at: Long = System.currentTimeMillis(),
    val expires_at: Long = System.currentTimeMillis() + 1800000L // 30 minutes default
)

@Entity(tableName = "sync_metadata")
data class SyncMetadata(
    @PrimaryKey val key: String,
    val last_sync: Long,
    val data_hash: String? = null,
    val expires_at: Long
)