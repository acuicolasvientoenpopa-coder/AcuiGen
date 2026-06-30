package com.nfctags.app.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.nfctags.app.data.entities.TagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {

    @Upsert
    suspend fun upsert(tag: TagEntity)

    @Upsert
    suspend fun upsertAll(tags: List<TagEntity>)

    @Query("SELECT * FROM tags WHERE deleted = 0 ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<TagEntity>>

    @Query("SELECT * FROM tags WHERE deleted = 0 ORDER BY updatedAt DESC")
    suspend fun getAll(): List<TagEntity>

    @Query("SELECT * FROM tags WHERE id = :id")
    suspend fun getById(id: String): TagEntity?

    @Query("SELECT * FROM tags WHERE id = :id")
    fun observeById(id: String): Flow<TagEntity?>

    @Query("SELECT * FROM tags WHERE deleted = 0 AND syncedAt IS NULL")
    suspend fun getUnsynced(): List<TagEntity>

    @Query("UPDATE tags SET syncedAt = :timestamp WHERE id = :id")
    suspend fun markSynced(id: String, timestamp: Long)

    @Query("UPDATE tags SET deleted = 1 WHERE id = :id")
    suspend fun softDelete(id: String)

    @Query("DELETE FROM tags WHERE deleted = 1")
    suspend fun purgeDeleted()
}
