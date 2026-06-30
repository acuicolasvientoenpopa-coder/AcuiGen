package com.nfctags.app.data.remote

import android.util.Log
import com.nfctags.app.data.entities.TagEntity
import com.nfctags.app.data.entities.ValueHistoryEntity
import com.nfctags.app.data.repository.TagRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseSyncRepository @Inject constructor(
    private val api: SupabaseApiService,
    private val localRepo: TagRepository
) {

    companion object {
        private const val TAG = "SupabaseSync"
    }

    suspend fun syncAll(): SyncResult {
        var tagsOk = true
        var historyOk = true
        var syncedCount = 0

        try {
            val unsyncedTags = localRepo.getUnsyncedTags()
            if (unsyncedTags.isNotEmpty()) {
                val dtos = ArrayList(unsyncedTags.map { it.toSupabaseDto() })
                val response = api.upsertTags(dtos)
                if (response.isSuccessful) {
                    val now = System.currentTimeMillis()
                    unsyncedTags.forEach { tag ->
                        localRepo.markTagSynced(tag.id, now)
                    }
                    syncedCount += unsyncedTags.size
                    Log.i(TAG, "Tags sync OK: ${unsyncedTags.size}")
                } else {
                    tagsOk = false
                    Log.e(TAG, "Tags sync error: ${response.code()} ${response.errorBody()?.string()}")
                }
            }
        } catch (e: Exception) {
            tagsOk = false
            Log.e(TAG, "Tags sync exception", e)
        }

        try {
            val unsyncedHistory = localRepo.getUnsyncedHistory()
            if (unsyncedHistory.isNotEmpty()) {
                val dtos = ArrayList(unsyncedHistory.map { it.toSupabaseDto() })
                val response = api.upsertHistory(dtos)
                if (response.isSuccessful) {
                    val ids = unsyncedHistory.map { it.id }
                    localRepo.markHistorySynced(ids)
                    syncedCount += unsyncedHistory.size
                    Log.i(TAG, "History sync OK: ${unsyncedHistory.size}")
                } else {
                    historyOk = false
                    Log.e(TAG, "History sync error: ${response.code()} ${response.errorBody()?.string()}")
                }
            }
        } catch (e: Exception) {
            historyOk = false
            Log.e(TAG, "History sync exception", e)
        }

        return SyncResult(
            success = tagsOk && historyOk,
            syncedCount = syncedCount,
            hasMore = tagsOk && historyOk && syncedCount > 0
        )
    }

    suspend fun pullFromCloud(): Int {
        var count = 0
        try {
            val response = api.getTags()
            if (response.isSuccessful) {
                val remoteTags = response.body() ?: emptyList()
                for (dto in remoteTags) {
                    val localTag = localRepo.getTag(dto.id)
                    if (localTag == null || localTag.updatedAt < dto.updatedAt) {
                        val entity = TagEntity(
                            id = dto.id,
                            nombre = dto.nombre,
                            valor1 = dto.valor1,
                            valor2 = dto.valor2,
                            valor3 = dto.valor3,
                            valor4 = dto.valor4,
                            valor5 = dto.valor5,
                            valor6 = dto.valor6,
                            valor7 = dto.valor7,
                            valor8 = dto.valor8,
                            valor9 = dto.valor9,
                            valor10 = dto.valor10,
                            createdAt = dto.createdAt,
                            updatedAt = dto.updatedAt,
                            syncedAt = dto.syncedAt ?: System.currentTimeMillis(),
                            deleted = dto.deleted
                        )
                        localRepo.saveTag(entity)
                        count++
                    }
                }
                Log.i(TAG, "Pull from cloud: $count tags")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Pull error", e)
        }
        return count
    }

    data class SyncResult(
        val success: Boolean,
        val syncedCount: Int,
        val hasMore: Boolean
    )
}
