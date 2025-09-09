package com.aics.violationapp.data.local.cache

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.aics.violationapp.data.local.sync.*

@Database(
    entities = [
        CachedViolationType::class,
        CachedStudent::class,
        CachedOffenseCount::class,
        SyncMetadata::class,
        PendingViolation::class,
        PendingUpdate::class,
        SyncLog::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class CacheDatabase : RoomDatabase() {
    
    abstract fun violationTypeDao(): ViolationTypeCacheDao
    abstract fun studentDao(): StudentCacheDao
    abstract fun offenseCountDao(): OffenseCountCacheDao
    abstract fun syncMetadataDao(): SyncMetadataDao
    abstract fun pendingViolationDao(): PendingViolationDao
    abstract fun pendingUpdateDao(): PendingUpdateDao
    abstract fun syncLogDao(): SyncLogDao
    
    companion object {
        @Volatile
        private var INSTANCE: CacheDatabase? = null
        
        fun getDatabase(context: Context): CacheDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CacheDatabase::class.java,
                    "violation_cache_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}