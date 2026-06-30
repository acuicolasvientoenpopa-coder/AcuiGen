package com.nfctags.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.nfctags.app.data.entities.ValueHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ValueHistoryDao {

    @Insert
    suspend fun insert(entry: ValueHistoryEntity)

    @Query("SELECT * FROM value_history WHERE tagId = :tagId ORDER BY timestamp DESC")
    fun observeByTagId(tagId: String): Flow<List<ValueHistoryEntity>>

    @Query("SELECT * FROM value_history WHERE tagId = :tagId ORDER BY timestamp DESC")
    suspend fun getByTagId(tagId: String): List<ValueHistoryEntity>

    @Query("SELECT * FROM value_history WHERE synced = 0")
    suspend fun getUnsynced(): List<ValueHistoryEntity>

    @Query("UPDATE value_history SET synced = 1 WHERE id = :id")
    suspend fun markSynced(id: Long)

    @Query("DELETE FROM value_history WHERE tagId IN (SELECT id FROM tags WHERE deleted = 1)")
    suspend fun purgeDeleted()
}
