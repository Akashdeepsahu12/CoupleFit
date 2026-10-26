package com.couplefit.data.repository

import com.couplefit.data.local.HealthDao
import com.couplefit.data.local.HealthDailyAggregateEntity
import com.couplefit.data.local.PartnerDailyAggregateEntity
import com.couplefit.data.model.HealthSnapshot
import com.couplefit.data.model.SharingPreferences
import com.couplefit.data.remote.FirebaseSyncManager
import com.couplefit.domain.repository.HealthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HealthRepositoryImpl @Inject constructor(
    private val healthDao: HealthDao,
    private val firebaseSyncManager: FirebaseSyncManager
) : HealthRepository {

    private val scope = CoroutineScope(Dispatchers.IO)

    private fun formatDate(date: Date): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date)
    }

    /**
     * Offline-first single source of truth for user data:
     * Reads directly from Room's health_daily_aggregates table as a reactive Flow.
     */
    override fun getMyDailySnapshot(date: Date): Flow<HealthSnapshot?> {
        val dateStr = formatDate(date)
        return healthDao.getDailyAggregateFlow(dateStr).map { entity ->
            entity?.toHealthSnapshot(date)
        }
    }

    /**
     * Offline-first single source of truth for partner data:
     * Reads directly from Room's partner_daily_aggregates table as a reactive Flow.
     * Guaranteed to work completely offline once synced.
     */
    override fun getPartnerDailySnapshot(partnerId: String, date: Date): Flow<HealthSnapshot?> {
        val dateStr = formatDate(date)
        return healthDao.getPartnerDailyAggregateFlow(partnerId, dateStr).map { entity ->
            entity?.toHealthSnapshot(date)
        }
    }

    override suspend fun syncWithPartner() {
        val today = Date()
        val defaultPrefs = SharingPreferences(
            userId = "current_user",
            partnerId = "partner_user"
        )
        syncWithPartner(defaultPrefs.partnerId, today, defaultPrefs)
    }

    /**
     * Executes the bidirectional offline-first sync cycle:
     * 1. Health Connect -> Room (handled in HealthConnectViewModel / Worker)
     * 2. Room -> Firestore: Push daily aggregate to user's sharedView (filtered by SharingPreferences)
     * 3. Firestore -> Room: Observe partner's sharedView snapshot listener and persist to partner Room table
     * 4. Room -> UI: Flow automatically updates without loading spinners
     */
    override suspend fun syncWithPartner(
        partnerId: String,
        date: Date,
        prefs: SharingPreferences
    ) {
        val dateStr = formatDate(date)

        // 1. Upload local aggregate to remote Firestore
        val localAggregate = healthDao.getDailyAggregate(dateStr)
        if (localAggregate != null) {
            firebaseSyncManager.syncDailyAggregate(
                aggregate = localAggregate,
                prefs = prefs,
                userId = prefs.userId
            )
        }

        // 2. Start observing partner updates and write incoming aggregates directly into Room
        if (partnerId.isNotBlank()) {
            scope.launch {
                firebaseSyncManager.observePartnerDailyAggregate(partnerId, dateStr).collect { partnerAggregate ->
                    if (partnerAggregate != null) {
                        healthDao.insertPartnerAggregate(partnerAggregate)
                    }
                }
            }
        }
    }

    private fun HealthDailyAggregateEntity.toHealthSnapshot(date: Date): HealthSnapshot {
        return HealthSnapshot(
            userId = "me",
            date = date,
            steps = totalSteps.toInt(),
            distanceMeters = totalSteps * 0.75, // standard step-to-meter conversion factor
            floorsClimbed = 0,
            activeCalories = totalCalories.toDouble(),
            sedentaryMinutes = 0,
            activeMinutes = totalActiveMinutes.toInt(),
            averageHeartRate = if (averageHeartRate > 0.0) averageHeartRate else null,
            averageOxygenSaturation = if (averageOxygenSaturation > 0.0) averageOxygenSaturation else null
        )
    }

    private fun PartnerDailyAggregateEntity.toHealthSnapshot(date: Date): HealthSnapshot {
        return HealthSnapshot(
            userId = partnerId,
            date = date,
            steps = totalSteps.toInt(),
            distanceMeters = totalSteps * 0.75,
            floorsClimbed = 0,
            activeCalories = totalCalories.toDouble(),
            sedentaryMinutes = 0,
            activeMinutes = totalActiveMinutes.toInt(),
            averageHeartRate = if (averageHeartRate > 0.0) averageHeartRate else null,
            averageOxygenSaturation = if (averageOxygenSaturation > 0.0) averageOxygenSaturation else null
        )
    }
}
