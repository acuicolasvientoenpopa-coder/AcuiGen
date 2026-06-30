package com.nfctags.app.sync

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.nfctags.app.data.remote.SupabaseSyncRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncRepository: SupabaseSyncRepository
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val TAG = "SyncWorker"
    }

    override suspend fun doWork(): Result {
        return try {
            val result = syncRepository.syncAll()

            if (result.success) {
                if (result.syncedCount > 0) {
                    Log.i(TAG, "Sincronización exitosa: ${result.syncedCount} registros")

                    try {
                        syncRepository.pullFromCloud()
                    } catch (e: Exception) {
                        Log.w(TAG, "Pull desde la nube falló (no crítico)", e)
                    }
                }
                Result.success()
            } else {
                Log.w(TAG, "Sincronización parcial, reintentando")
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en sincronización", e)
            Result.retry()
        }
    }
}
