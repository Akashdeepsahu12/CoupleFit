package com.couplefit.data.healthconnect

import android.content.Context
import android.util.Log
import androidx.glance.appwidget.updateAll
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.Instant
import java.time.temporal.ChronoUnit

@HiltWorker
class HealthConnectSyncWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val healthConnectManager: HealthConnectManager,
    private val dataStore: com.couplefit.data.local.CoupleFitPreferencesDataStore,
    private val healthDao: com.couplefit.data.local.HealthDao
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting background sync for Health Connect via Changes API")
        
        // Safety check for SDK availability
        if (healthConnectManager.checkAvailability() != androidx.health.connect.client.HealthConnectClient.SDK_AVAILABLE) {
            Log.w(TAG, "Health Connect is not supported or ready on this device. Aborting sync.")
            return Result.failure()
        }

        // Safety check for permissions
        val hasPermissions = healthConnectManager.hasAnyPermissions()
        if (!hasPermissions) {
            Log.w(TAG, "Missing Health Connect permissions. Aborting background sync.")
            return Result.failure()
        }

        return try {
            // Replaces "last 24 hours" read with the Changes API (handles token expiry with full resync fallback and deduplicates steps via aggregate queries)
            val syncResult = healthConnectManager.syncViaChangesApi(dataStore, healthDao)
            if (syncResult.isSuccess) {
                val aggregatedData = syncResult.getOrNull()
                Log.d(TAG, "Background sync via Changes API completed. Steps: ${aggregatedData?.totalSteps}, Avg HR: ${aggregatedData?.averageHeartRate}")
                try {
                    com.couplefit.widget.CoupleFitAppWidget().updateAll(context)
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to update Glance widget", e)
                }
                Result.success()
            } else {
                Log.w(TAG, "Background sync via Changes API failed with error: ${syncResult.exceptionOrNull()?.message}")
                Result.retry()
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException during background sync (Likely missing background permission)", e)
            Result.failure()
        } catch (e: Exception) {
            Log.e(TAG, "Exception during background sync", e)
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "HealthConnectSyncWorker"
    }
}

