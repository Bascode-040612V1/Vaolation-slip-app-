package com.aics.violationapp.data.local.sync

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "pending_violations",
    indices = [Index(value = ["sync_status"]), Index(value = ["created_at"])]
)
data class PendingViolation(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val student_id: String,
    val violations: String, // JSON array of violation types
    val recorded_by: String,
    val created_at: Long = System.currentTimeMillis(),
    val sync_status: SyncStatus = SyncStatus.PENDING,
    val retry_count: Int = 0,
    val last_error: String? = null,
    val priority: Int = 1 // 1 = high, 2 = medium, 3 = low
)

@Entity(
    tableName = "pending_updates",
    indices = [Index(value = ["entity_type", "entity_id"], unique = true)]
)
data class PendingUpdate(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val entity_type: String, // "student", "offense_count", etc.
    val entity_id: String,
    val action: String, // "CREATE", "UPDATE", "DELETE"
    val data: String, // JSON data
    val created_at: Long = System.currentTimeMillis(),
    val sync_status: SyncStatus = SyncStatus.PENDING,
    val retry_count: Int = 0
)

@Entity(tableName = "sync_log")
data class SyncLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val operation: String,
    val entity_type: String,
    val entity_id: String?,
    val status: SyncStatus,
    val error_message: String?,
    val sync_duration: Long,
    val created_at: Long = System.currentTimeMillis()
)

enum class SyncStatus {
    PENDING,
    SYNCING,
    COMPLETED,
    FAILED,
    CANCELLED
}