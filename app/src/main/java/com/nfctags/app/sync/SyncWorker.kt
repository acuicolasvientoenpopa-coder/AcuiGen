package com.nfctags.app.sync

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.nfctags.app.data.repository.TagRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: TagRepository
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val TAG = "SyncWorker"
    }

    override suspend fun doWork(): Result {
        return try {
            val unsyncedTags = repository.getUnsyncedTags()
            val unsyncedHistory = repository.getUnsyncedHistory()

            if (unsyncedTags.isEmpty() && unsyncedHistory.isEmpty()) {
                Log.i(TAG, "No hay datos pendientes de sincronizar")
                return Result.success()
            }

            Log.i(TAG, "Sincronizando ${unsyncedTags.size} tags y ${unsyncedHistory.size} historiales")

            var success = true

            for (tag in unsyncedTags) {
                try {
                    val respuesta = enviarTagAlServidor(tag)
                    if (respuesta) {
                        repository.markTagSynced(tag.id)
                    } else {
                        success = false
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error sync tag ${tag.id}", e)
                    success = false
                }
            }

            val historyIds = mutableListOf<Long>()
            for (entry in unsyncedHistory) {
                try {
                    val respuesta = enviarHistorialAlServidor(entry)
                    if (respuesta) {
                        historyIds.add(entry.id)
                    } else {
                        success = false
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error sync history ${entry.id}", e)
                    success = false
                }
            }

            if (historyIds.isNotEmpty()) {
                repository.markHistorySynced(historyIds)
            }

            if (success) Result.success() else Result.retry()
        } catch (e: Exception) {
            Log.e(TAG, "Error en sincronización", e)
            Result.retry()
        }
    }

    private suspend fun enviarTagAlServidor(tag: com.nfctags.app.data.entities.TagEntity): Boolean {
        // TODO: Reemplazar con llamada HTTP real a tu API
        // val response = apiService.syncTag(tag.toDto())
        // return response.isSuccessful
        Log.d(TAG, "Tag ${tag.id} listo para enviar al servidor")
        return false
    }

    private suspend fun enviarHistorialAlServidor(
        entry: com.nfctags.app.data.entities.ValueHistoryEntity
    ): Boolean {
        // TODO: Reemplazar con llamada HTTP real a tu API
        // val response = apiService.syncHistory(entry.toDto())
        // return response.isSuccessful
        Log.d(TAG, "History ${entry.id} listo para enviar al servidor")
        return false
    }
}
