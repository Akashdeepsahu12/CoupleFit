package com.couplefit.data.healthconnect

import android.content.Context
import android.util.Log
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
    private val healthConnectManager: HealthConnectManager
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting background sync for Health Connect")
        
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
            val endTime = Instant.now()
            val startTime = endTime.minus(24, ChronoUnit.HOURS)
            
            // Read aggregated data to avoid pulling heavy payloads in background
            val aggregatedData = healthConnectManager.getAggregatedData(startTime, endTime)
            
            Log.d(TAG, "Background sync completed. Steps: ${aggregatedData.totalSteps}, Avg HR: ${aggregatedData.averageHeartRate}")
            
            // Here you would upload this data to Firestore or your backend.
            // For now, we just indicate success.
            Result.success()
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

