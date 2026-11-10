package com.couplefit.data.repository

import com.couplefit.data.local.CoupleFitPreferencesDataStore
import com.couplefit.data.local.HealthDailyAggregateEntity
import com.couplefit.data.local.HealthDao
import com.couplefit.domain.repository.SyncRepository
import com.couplefit.domain.repository.SyncResult
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncRepositoryImpl @Inject constructor(
    private val healthDao: HealthDao,
    private val dataStore: CoupleFitPreferencesDataStore,
    private val firestore: FirebaseFirestore
) : SyncRepository {

    companion object {
        const val FIFTEEN_MINUTES_MS = 15 * 60 * 1000L
    }

    override suspend fun uploadMyDailyAggregate(
        coupleId: String,
        myUid: String,
        dateStr: String,
        force: Boolean
    ): SyncResult = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val lastSync = dataStore.getLastSyncTimestamp()

        // 1. Enforce 15-minute throttle unless forced
        if (!force && (now - lastSync < FIFTEEN_MINUTES_MS)) {
            Timber.d("Sync throttled. Last sync was ${now - lastSync} ms ago (< 15 min)")
            return@withContext SyncResult.Throttled
        }

        try {
            // 2. Fetch local aggregate from Room (SSOT)
            val aggregate = healthDao.getDailyAggregate(dateStr)

            // 3. Fetch user's sharing preferences from DataStore
            val prefs = dataStore.getSharingPreferences()

            // 4. Construct payload strictly filtering only enabled metrics (aggregates only, no raw points)
            val payload = mutableMapOf<String, Any>(
                "userId" to myUid,
                "dateStr" to dateStr,
                "lastUpdated" to now
            )

            if (prefs.shareSteps) {
                payload["steps"] = aggregate?.totalSteps ?: 0L
            }
            if (prefs.shareWorkouts) {
                payload["activeMinutes"] = aggregate?.totalActiveMinutes ?: 0L
            }
            if (prefs.shareCalories || prefs.shareWorkouts) {
                payload["calories"] = aggregate?.totalCalories ?: 0L
            }
            if (prefs.shareHr && (aggregate?.averageHeartRate ?: 0.0) > 0.0) {
                payload["avgHr"] = aggregate!!.averageHeartRate
            }
            if (prefs.shareSpo2 && (aggregate?.averageOxygenSaturation ?: 0.0) > 0.0) {
                payload["avgSpo2"] = aggregate!!.averageOxygenSaturation
            }

            // 5. Upload to couples/{coupleId}/daily/{myUid_date}
            val docId = "${myUid}_$dateStr"
            kotlinx.coroutines.withTimeoutOrNull(5000L) {
                firestore.collection("couples")
                    .document(coupleId)
                    .collection("daily")
                    .document(docId)
                    .set(payload, SetOptions.merge())
                    .await()
            }

            // 6. Record last sync timestamp
            dataStore.updateLastSyncTimestamp(now)
            Timber.d("Successfully synced daily aggregate to Firestore at couples/$coupleId/daily/$docId")
            SyncResult.Success(payload)
        } catch (e: Exception) {
            Timber.e(e, "Failed to upload daily aggregate to Firestore")
            SyncResult.Failure(e)
        }
    }

    /**
     * Executes sync on app foreground (forces upload of latest Room aggregate).
     */
    override suspend fun syncOnAppForeground(
        coupleId: String,
        myUid: String,
        dateStr: String
    ): SyncResult {
        Timber.d("Executing foreground sync for couple: $coupleId, user: $myUid, date: $dateStr")
        return uploadMyDailyAggregate(coupleId, myUid, dateStr, force = true)
    }

    override fun getMyDailyAggregateFlow(dateStr: String): Flow<HealthDailyAggregateEntity?> {
        return healthDao.getDailyAggregateFlow(dateStr)
    }

    override suspend fun getMyDailyAggregate(dateStr: String): HealthDailyAggregateEntity? {
        return healthDao.getDailyAggregate(dateStr)
    }
}
