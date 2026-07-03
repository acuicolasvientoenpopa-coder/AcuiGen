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
            Log.i(TAG, "Sync result: success=${result.success}, count=${result.syncedCount}")

            val pulled = syncRepository.pullFromCloud()
            Log.i(TAG, "Pull result: $pulled tags descargados")

            if (result.success) Result.success() else Result.retry()
        } catch (e: Exception) {
            Log.e(TAG, "Error en sincronización", e)
            Result.retry()
        }
    }
}
