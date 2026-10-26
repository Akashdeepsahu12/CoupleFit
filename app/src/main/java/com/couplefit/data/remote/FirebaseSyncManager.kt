package com.couplefit.data.remote

import android.util.Log
import com.couplefit.data.local.HealthDailyAggregateEntity
import com.couplefit.data.local.PartnerDailyAggregateEntity
import com.couplefit.data.model.HealthSnapshot
import com.couplefit.data.model.SharingPreferences
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emptyFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseSyncManager(
    private val firestore: FirebaseFirestore? = null
) {
    @Inject
    constructor() : this(
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            null
        }
    )

    /**
     * Builds a sanitized, aggregate-only Firestore payload.
     * STRICT PRIVACY RULES:
     * - Every metric respects its per-metric toggle in SharingPreferences.
     * - Only daily aggregated metrics are included.
     * - Raw biometric/vital samples (heart rate or SpO2 points) are NEVER uploaded.
     */
    fun prepareDailyAggregatePayload(
        userId: String,
        dateStr: String,
        steps: Long,
        activeMinutes: Long,
        calories: Long,
        averageHeartRate: Double,
        averageOxygenSaturation: Double,
        prefs: SharingPreferences
    ): Map<String, Any> {
        val payload = mutableMapOf<String, Any>()
        payload["userId"] = userId
        payload["dateStr"] = dateStr
        payload["updatedAt"] = System.currentTimeMillis()

        // 1. Steps
        if (prefs.shareSteps) {
            payload["steps"] = steps
        }

        // 2. Active minutes & Calories (Workouts)
        if (prefs.shareWorkouts) {
            payload["activeMinutes"] = activeMinutes
            payload["calories"] = calories
        }

        // 3. Heart Rate (Daily Aggregate ONLY - never raw points)
        if (prefs.shareHR && averageHeartRate > 0.0) {
            payload["averageHeartRate"] = averageHeartRate
        }

        // 4. Oxygen Saturation SpO2 (Daily Aggregate ONLY - never raw points)
        if (prefs.shareSpO2 && averageOxygenSaturation > 0.0) {
            payload["averageOxygenSaturation"] = averageOxygenSaturation
        }

        return payload
    }

    /**
     * Backwards-compatible overload for HealthSnapshot
     */
    fun syncHealthSnapshot(snapshot: HealthSnapshot, prefs: SharingPreferences) {
        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(snapshot.date)
        val payload = prepareDailyAggregatePayload(
            userId = snapshot.userId,
            dateStr = dateStr,
            steps = snapshot.steps.toLong(),
            activeMinutes = snapshot.activeMinutes.toLong(),
            calories = snapshot.activeCalories.toLong(),
            averageHeartRate = snapshot.averageHeartRate ?: 0.0,
            averageOxygenSaturation = snapshot.averageOxygenSaturation ?: 0.0,
            prefs = prefs
        )

        // Only push if there are shared metrics beyond the metadata
        if (payload.keys.any { it !in setOf("userId", "dateStr", "updatedAt") }) {
            uploadPayload(snapshot.userId, dateStr, payload)
        }
    }

    /**
     * Pushes a local Room daily aggregate to Firestore under users/{userId}/sharedView/{dateStr}
     */
    fun syncDailyAggregate(
        aggregate: HealthDailyAggregateEntity,
        prefs: SharingPreferences,
        userId: String
    ) {
        val payload = prepareDailyAggregatePayload(
            userId = userId,
            dateStr = aggregate.dateStr,
            steps = aggregate.totalSteps,
            activeMinutes = aggregate.totalActiveMinutes,
            calories = aggregate.totalCalories,
            averageHeartRate = aggregate.averageHeartRate,
            averageOxygenSaturation = aggregate.averageOxygenSaturation,
            prefs = prefs
        )

        uploadPayload(userId, aggregate.dateStr, payload)
    }

    private fun uploadPayload(userId: String, dateStr: String, payload: Map<String, Any>) {
        val db = firestore ?: run {
            try {
                Log.w(TAG, "Firestore is not available (mock/testing environment)")
            } catch (_: Exception) {}
            return
        }

        db.collection("users")
            .document(userId)
            .collection("sharedView")
            .document(dateStr)
            .set(payload, SetOptions.merge())
            .addOnSuccessListener {
                try {
                    Log.d(TAG, "Successfully synced daily aggregate to Firestore for date: $dateStr")
                } catch (_: Exception) {}
            }
            .addOnFailureListener { e ->
                try {
                    Log.e(TAG, "Failed to sync daily aggregate to Firestore", e)
                } catch (_: Exception) {}
            }
    }


    /**
     * Observes real-time updates from partner's sharedView document.
     * Emits PartnerDailyAggregateEntity for writing into local Room table.
     */
    fun observePartnerDailyAggregate(
        partnerId: String,
        dateStr: String
    ): Flow<PartnerDailyAggregateEntity?> {
        val db = firestore ?: return emptyFlow()

        return callbackFlow {
            val docRef = db.collection("users")
                .document(partnerId)
                .collection("sharedView")
                .document(dateStr)

            val registration = docRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to partner snapshot", error)
                    trySend(null)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val data = snapshot.data ?: emptyMap<String, Any>()
                    val steps = (data["steps"] as? Number)?.toLong() ?: 0L
                    val activeMins = (data["activeMinutes"] as? Number)?.toLong() ?: 0L
                    val calories = (data["calories"] as? Number)?.toLong() ?: 0L
                    val avgHr = (data["averageHeartRate"] as? Number)?.toDouble() ?: 0.0
                    val avgO2 = (data["averageOxygenSaturation"] as? Number)?.toDouble() ?: 0.0
                    val updatedAt = (data["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()

                    val partnerAggregate = PartnerDailyAggregateEntity(
                        id = "${partnerId}_$dateStr",
                        partnerId = partnerId,
                        dateStr = dateStr,
                        totalSteps = steps,
                        totalActiveMinutes = activeMins,
                        totalCalories = calories,
                        averageHeartRate = avgHr,
                        averageOxygenSaturation = avgO2,
                        lastUpdatedTimestamp = updatedAt
                    )
                    trySend(partnerAggregate)
                } else {
                    trySend(null)
                }
            }

            awaitClose {
                registration.remove()
            }
        }
    }

    fun observePartnerSnapshot(partnerId: String): Flow<HealthSnapshot> {
        return emptyFlow()
    }

    companion object {
        private const val TAG = "FirebaseSyncManager"
    }
}
