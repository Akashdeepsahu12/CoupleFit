package com.couplefit.data.repository

import com.couplefit.data.local.HealthDao
import com.couplefit.data.local.PartnerDailyEntity
import com.couplefit.domain.repository.PartnerRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PartnerRepositoryImpl @Inject constructor(
    private val healthDao: HealthDao,
    private val firestore: FirebaseFirestore
) : PartnerRepository {

    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    override fun getPartnerDailyFlow(partnerId: String, dateStr: String): Flow<PartnerDailyEntity?> {
        return healthDao.getPartnerDailyFlow(partnerId, dateStr)
    }

    override fun getPartnerDailyFlow(dateStr: String): Flow<PartnerDailyEntity?> {
        return healthDao.getPartnerDailyFlow(dateStr)
    }

    override suspend fun getPartnerDaily(partnerId: String, dateStr: String): PartnerDailyEntity? {
        return healthDao.getPartnerDaily(partnerId, dateStr)
    }

    override suspend fun savePartnerDaily(entity: PartnerDailyEntity) {
        healthDao.insertPartnerDaily(entity)
    }

    override suspend fun clearPartnerDaily(partnerId: String) {
        healthDao.deletePartnerDailyData(partnerId)
    }

    override suspend fun seedDemoPartnerDaily(
        partnerId: String,
        dateStr: String,
        defaultSteps: Long
    ) = kotlinx.coroutines.withContext(Dispatchers.IO) {
        val existing = healthDao.getPartnerDaily(partnerId, dateStr)
        val isDemoPartner = partnerId == "partner_1" || partnerId == "partner_demo" || partnerId.startsWith("demo_")
        if ((existing == null || existing.steps == 0L) && isDemoPartner) {
            val docId = "${partnerId}_$dateStr"
            val activeMins = (defaultSteps / 150).coerceAtLeast(35L)
            val calories = (defaultSteps * 0.042).toLong().coerceAtLeast(310L)
            val entity = PartnerDailyEntity(
                id = docId,
                partnerId = partnerId,
                dateStr = dateStr,
                steps = defaultSteps,
                activeMinutes = activeMins,
                calories = calories,
                avgHr = 72.0,
                avgSpo2 = 98.4,
                lastUpdated = System.currentTimeMillis() - 5 * 60 * 1000L // 5 mins ago (fresh)
            )
            healthDao.insertPartnerDaily(entity)
            Timber.d("Seeded initial partner daily telemetry into Room for $partnerId on $dateStr")
        }
    }

    override suspend fun addPartnerSteps(
        partnerId: String,
        dateStr: String,
        count: Long
    ) = kotlinx.coroutines.withContext(Dispatchers.IO) {
        val existing = healthDao.getPartnerDaily(partnerId, dateStr)
        val currentSteps = existing?.steps ?: 0L
        val updatedSteps = currentSteps + count
        val activeMins = (updatedSteps / 150).coerceAtLeast(existing?.activeMinutes ?: 25L)
        val calories = (updatedSteps * 0.042).toLong().coerceAtLeast(existing?.calories ?: 150L)
        val entity = PartnerDailyEntity(
            id = "${partnerId}_$dateStr",
            partnerId = partnerId,
            dateStr = dateStr,
            steps = updatedSteps,
            activeMinutes = activeMins,
            calories = calories,
            avgHr = if ((existing?.avgHr ?: 0.0) > 0.0) existing!!.avgHr else 72.0,
            avgSpo2 = if ((existing?.avgSpo2 ?: 0.0) > 0.0) existing!!.avgSpo2 else 98.4,
            lastUpdated = System.currentTimeMillis()
        )
        healthDao.insertPartnerDaily(entity)
        Timber.d("Updated partner test steps in Room: $updatedSteps for $partnerId")
    }

    override fun getCombinedDashboardFlow(
        partnerId: String,
        dateStr: String
    ): Flow<com.couplefit.domain.model.CombinedDailyDashboard> {
        val myFlow = healthDao.getDailyAggregateFlow(dateStr)
        val partnerFlow = healthDao.getPartnerDailyFlow(partnerId, dateStr)
        return kotlinx.coroutines.flow.combine(myFlow, partnerFlow) { myDaily, partnerDaily ->
            com.couplefit.domain.model.CombinedDailyDashboard(myDaily, partnerDaily)
        }
    }

    override fun getCombinedDashboardFlow(
        dateStr: String
    ): Flow<com.couplefit.domain.model.CombinedDailyDashboard> {
        val myFlow = healthDao.getDailyAggregateFlow(dateStr)
        val partnerFlow = healthDao.getPartnerDailyFlow(dateStr)
        return kotlinx.coroutines.flow.combine(myFlow, partnerFlow) { myDaily, partnerDaily ->
            com.couplefit.domain.model.CombinedDailyDashboard(myDaily, partnerDaily)
        }
    }

    private fun parseLong(value: Any?, default: Long = 0L): Long {
        return when (value) {
            is Number -> value.toLong()
            is String -> value.toLongOrNull() ?: default
            else -> default
        }
    }

    private fun parseDouble(value: Any?, default: Double = 0.0): Double {
        return when (value) {
            is Number -> value.toDouble()
            is String -> value.toDoubleOrNull() ?: default
            else -> default
        }
    }

    private fun parseTimestamp(value: Any?, fallback: Long = System.currentTimeMillis()): Long {
        return when (value) {
            is com.google.firebase.Timestamp -> value.toDate().time
            is Number -> value.toLong()
            is java.util.Date -> value.time
            is String -> value.toLongOrNull() ?: fallback
            else -> fallback
        }
    }

    private fun parsePartnerDaily(
        docId: String,
        partnerUid: String,
        dateStr: String,
        data: Map<String, Any>?
    ): PartnerDailyEntity {
        val map = data ?: emptyMap()
        val steps = parseLong(map["steps"])
        val activeMinutes = parseLong(map["activeMinutes"])
        val calories = parseLong(map["calories"])
        val avgHr = parseDouble(map["avgHr"] ?: map["averageHeartRate"])
        val avgSpo2 = parseDouble(map["avgSpo2"] ?: map["averageOxygenSaturation"])
        val lastUpdated = parseTimestamp(map["lastUpdated"] ?: map["updatedAt"])

        return PartnerDailyEntity(
            id = docId,
            partnerId = partnerUid,
            dateStr = dateStr,
            steps = steps,
            activeMinutes = activeMinutes,
            calories = calories,
            avgHr = avgHr,
            avgSpo2 = avgSpo2,
            lastUpdated = lastUpdated
        )
    }

    override suspend fun syncPartnerDailyFromRemote(
        coupleId: String,
        partnerUid: String,
        dateStr: String
    ): Result<PartnerDailyEntity?> = kotlinx.coroutines.withContext(Dispatchers.IO) {
        if (coupleId.isBlank() || partnerUid.isBlank() || coupleId == "couple_1" || partnerUid == "partner_1") {
            return@withContext Result.success(null)
        }
        try {
            val docId = "${partnerUid}_$dateStr"
            val docRef = firestore.collection("couples")
                .document(coupleId)
                .collection("daily")
                .document(docId)

            val snapshot = kotlinx.coroutines.withTimeoutOrNull(5000L) {
                docRef.get().await()
            }
            if (snapshot != null && snapshot.exists()) {
                val partnerDaily = parsePartnerDaily(docId, partnerUid, dateStr, snapshot.data)
                healthDao.insertPartnerDaily(partnerDaily)
                Timber.d("Direct remote sync cached partner daily to Room: $docId (steps: ${partnerDaily.steps})")
                Result.success(partnerDaily)
            } else {
                Timber.d("No remote daily document found yet for $docId")
                Result.success(null)
            }
        } catch (e: Exception) {
            Timber.w(e, "Failed direct remote sync for partner daily: couples/$coupleId/daily/${partnerUid}_$dateStr")
            Result.failure(e)
        }
    }

    /**
     * Listens to partner's daily document at couples/{coupleId}/daily/{partnerUid_date}.
     * When remote snapshots arrive, writes directly to Room table 'partnerDaily'.
     * Emits from Room to ensure offline-first SSOT for the dashboard UI.
     */
    override fun listenToPartnerDaily(
        coupleId: String,
        partnerUid: String,
        dateStr: String
    ): Flow<PartnerDailyEntity?> = callbackFlow {
        val docId = "${partnerUid}_$dateStr"
        val docRef = firestore.collection("couples")
            .document(coupleId)
            .collection("daily")
            .document(docId)

        val registration = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Timber.w(error, "Error listening to partner daily doc: couples/$coupleId/daily/$docId")
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val partnerDaily = parsePartnerDaily(docId, partnerUid, dateStr, snapshot.data)
                repositoryScope.launch {
                    healthDao.insertPartnerDaily(partnerDaily)
                    Timber.d("Cached partner daily doc to Room via snapshot: $docId (steps: ${partnerDaily.steps})")
                }
            }
        }

        // Also emit Room flow updates so consumer gets offline data instantly
        val job = launch {
            healthDao.getPartnerDailyFlow(partnerUid, dateStr).collect { entity ->
                trySend(entity)
            }
        }

        awaitClose {
            registration.remove()
            job.cancel()
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun sendNudge(
        coupleId: String,
        senderUid: String,
        senderName: String?,
        recipientUid: String,
        message: String
    ): Result<String> = kotlinx.coroutines.withContext(Dispatchers.IO) {
        try {
            val nudgeRef = firestore.collection("couples").document(coupleId)
                .collection("nudges").document()

            val payload = mapOf(
                "id" to nudgeRef.id,
                "coupleId" to coupleId,
                "senderUid" to senderUid,
                "senderName" to (senderName ?: "Partner"),
                "recipientUid" to recipientUid,
                "message" to message,
                "type" to "NUDGE",
                "status" to "PENDING",
                "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
            )

            nudgeRef.set(payload).await()
            Timber.d("Created nudge document ${nudgeRef.id} in couples/$coupleId/nudges")
            Result.success(nudgeRef.id)
        } catch (e: Exception) {
            Timber.e(e, "Failed to create nudge document in Firestore")
            Result.failure(e)
        }
    }
}
