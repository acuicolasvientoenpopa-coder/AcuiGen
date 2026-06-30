package com.nfctags.app.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.nfctags.app.data.dao.TagDao
import com.nfctags.app.data.dao.ValueHistoryDao
import com.nfctags.app.data.entities.TagEntity
import com.nfctags.app.data.entities.ValueHistoryEntity

@Database(
    entities = [TagEntity::class, ValueHistoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tagDao(): TagDao
    abstract fun valueHistoryDao(): ValueHistoryDao
}
