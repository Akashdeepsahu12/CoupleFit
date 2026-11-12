package com.couplefit.service

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.couplefit.data.healthconnect.HealthConnectManager
import com.couplefit.data.local.CoupleFitPreferencesDataStore
import com.couplefit.data.local.HealthDao
import com.couplefit.data.remote.FirebaseAuthManager
import com.couplefit.domain.repository.PartnerRepository
import com.couplefit.domain.repository.SyncRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val healthConnectManager: HealthConnectManager,
    private val dataStore: CoupleFitPreferencesDataStore,
    private val healthDao: HealthDao,
    private val syncRepository: SyncRepository,
    private val partnerRepository: PartnerRepository,
    private val authManager: FirebaseAuthManager
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Timber.d("Starting background SyncWorker execution")
        try {
            // 1. Pull latest steps from Health Connect via Changes API into Room (if available & permitted)
            if (healthConnectManager.checkAvailability() == androidx.health.connect.client.HealthConnectClient.SDK_AVAILABLE &&
                healthConnectManager.hasAnyPermissions()
            ) {
                try {
                    val hcResult = healthConnectManager.syncViaChangesApi(dataStore, healthDao)
                    Timber.d("Health Connect background sync result: isSuccess=${hcResult.isSuccess}")
                } catch (e: Exception) {
                    Timber.w(e, "Non-fatal Health Connect Changes API sync issue in background")
                }
            }

            // 2. Fetch current user session
            val session = authManager.getCurrentSession()
            val coupleId = session?.coupleId
            val myUid = session?.uid
            val partnerId = session?.partnerId
            val dateStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

            // 3. If coupled with a real partner, execute bidirectional Firestore sync
            if (session != null && !coupleId.isNullOrBlank() && coupleId != "couple_1" && !myUid.isNullOrBlank()) {
                // A. Upload local aggregate to remote Firestore (force = true to guarantee background freshness)
                val uploadResult = syncRepository.uploadMyDailyAggregate(coupleId, myUid, dateStr, force = true)
                Timber.d("SyncWorker uploadMyDailyAggregate completed: $uploadResult")

                // B. Fetch partner's daily telemetry from remote Firestore into Room
                if (!partnerId.isNullOrBlank() && partnerId != "partner_1") {
                    val partnerResult = partnerRepository.syncPartnerDailyFromRemote(coupleId, partnerId, dateStr)
                    Timber.d("SyncWorker syncPartnerDailyFromRemote completed: isSuccess=${partnerResult.isSuccess}")
                }
            } else {
                Timber.d("SyncWorker skipped remote sync: coupleId='$coupleId', myUid='$myUid'")
            }

            // 4. Update Glance home widget with fresh telemetry
            try {
                com.couplefit.widget.CoupleFitAppWidget().updateAll(appContext)
            } catch (ignored: Exception) {}

            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Error during background SyncWorker execution")
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    companion object {
        const val ONE_TIME_SYNC_TAG = "com.couplefit.ONE_TIME_SYNC"

        /**
         * Enqueues an immediate one-time background sync request with NetworkType.CONNECTED constraints
         * and exponential backoff, ensuring guaranteed delivery even if the foreground app is paused.
         */
        fun enqueueImmediateSync(context: Context) {
            val constraints = androidx.work.Constraints.Builder()
                .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
                .build()

            val syncRequest = androidx.work.OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(constraints)
                .setBackoffCriteria(
                    androidx.work.BackoffPolicy.EXPONENTIAL,
                    10,
                    java.util.concurrent.TimeUnit.SECONDS
                )
                .addTag(ONE_TIME_SYNC_TAG)
                .build()

            androidx.work.WorkManager.getInstance(context).enqueueUniqueWork(
                "OneTimeSyncWork",
                androidx.work.ExistingWorkPolicy.REPLACE,
                syncRequest
            )
            Timber.d("Enqueued immediate OneTimeWorkRequest for SyncWorker with NetworkType.CONNECTED")
        }
    }
}
