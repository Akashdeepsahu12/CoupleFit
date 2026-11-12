package com.couplefit.data.repository

import android.content.Context
import com.couplefit.data.local.CoupleFitPreferencesDataStore
import com.couplefit.data.local.HealthDao
import com.couplefit.data.local.PartnerDailyEntity
import com.couplefit.domain.repository.PartnerRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PartnerRepositoryImpl @Inject constructor(
    private val healthDao: HealthDao,
    private val firestore: FirebaseFirestore,
    @ApplicationContext private val context: Context? = null,
    private val auth: FirebaseAuth? = null,
    private val preferencesDataStore: CoupleFitPreferencesDataStore? = null
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

    override fun getLatestPartnerDailyFlow(partnerId: String): Flow<PartnerDailyEntity?> {
        return healthDao.getLatestPartnerDailyFlow(partnerId)
    }

    override suspend fun getLatestPartnerDaily(partnerId: String): PartnerDailyEntity? {
        return healthDao.getLatestPartnerDaily(partnerId)
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
    ): Result<PartnerDailyEntity?> = withContext(Dispatchers.IO) {
        if (partnerUid.isBlank() || partnerUid == "partner_1") {
            return@withContext Result.success(null)
        }
        try {
            val currentUid = auth?.currentUser?.uid

            // Step 1: Detect Partner's canonical coupleId from users/{partnerUid} using Source.SERVER
            var partnerRemoteCoupleId: String? = null
            try {
                val partnerUserDoc = firestore.collection("users")
                    .document(partnerUid)
                    .get(Source.SERVER)
                    .await()
                if (partnerUserDoc.exists()) {
                    partnerRemoteCoupleId = partnerUserDoc.getString("coupleId")
                    val pDisplayName = partnerUserDoc.getString("displayName")
                    val pBirthday = partnerUserDoc.getString("birthday") ?: partnerUserDoc.getString("birthdate")
                    if (!pDisplayName.isNullOrBlank()) {
                        if (preferencesDataStore != null) {
                            preferencesDataStore.savePartnerName(pDisplayName)
                        } else if (context != null) {
                            try { CoupleFitPreferencesDataStore(context, firestore).savePartnerName(pDisplayName) } catch (_: Exception) {}
                        }
                    }
                    if (!pBirthday.isNullOrBlank()) {
                        if (preferencesDataStore != null) {
                            preferencesDataStore.savePartnerBirthday(pBirthday)
                        } else if (context != null) {
                            try { CoupleFitPreferencesDataStore(context, firestore).savePartnerBirthday(pBirthday) } catch (_: Exception) {}
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.w(e, "Could not fetch users/$partnerUid via Source.SERVER, trying fallback")
                try {
                    val fallbackDoc = firestore.collection("users")
                        .document(partnerUid)
                        .get()
                        .await()
                    if (fallbackDoc.exists()) {
                        partnerRemoteCoupleId = fallbackDoc.getString("coupleId")
                        val pDisplayName = fallbackDoc.getString("displayName")
                        val pBirthday = fallbackDoc.getString("birthday") ?: fallbackDoc.getString("birthdate")
                        if (!pDisplayName.isNullOrBlank()) {
                            if (preferencesDataStore != null) {
                                preferencesDataStore.savePartnerName(pDisplayName)
                            } else if (context != null) {
                                try { CoupleFitPreferencesDataStore(context, firestore).savePartnerName(pDisplayName) } catch (_: Exception) {}
                            }
                        }
                        if (!pBirthday.isNullOrBlank()) {
                            if (preferencesDataStore != null) {
                                preferencesDataStore.savePartnerBirthday(pBirthday)
                            } else if (context != null) {
                                try { CoupleFitPreferencesDataStore(context, firestore).savePartnerBirthday(pBirthday) } catch (_: Exception) {}
                            }
                        }
                    }
                } catch (ignored: Exception) {}
            }

            // Candidate couple rooms to probe:
            // 1) partner's remote coupleId (if known)
            // 2) local coupleId (if provided)
            val candidateRooms = listOfNotNull(partnerRemoteCoupleId, coupleId)
                .filter { it.isNotBlank() && it != "couple_1" }
                .distinct()

            Timber.d("Syncing partner daily for $partnerUid on $dateStr across candidate rooms: $candidateRooms")

            var foundDaily: PartnerDailyEntity? = null
            var matchedRoom: String? = null

            // Step 2: Probe today's document with Source.SERVER across candidate rooms
            for (room in candidateRooms) {
                val docId = "${partnerUid}_$dateStr"
                val docRef = firestore.collection("couples")
                    .document(room)
                    .collection("daily")
                    .document(docId)

                try {
                    val snapshot = withTimeout(15000L) {
                        docRef.get(Source.SERVER).await()
                    }
                    if (snapshot.exists()) {
                        val entity = parsePartnerDaily(docId, partnerUid, dateStr, snapshot.data)
                        foundDaily = entity
                        matchedRoom = room
                        Timber.d("Found partner daily with Source.SERVER in couples/$room/daily/$docId (steps: ${entity.steps})")
                        break
                    }
                } catch (e: Exception) {
                    Timber.w(e, "Source.SERVER fetch failed for couples/$room/daily/$docId, trying cache fallback")
                    try {
                        val cachedSnapshot = docRef.get().await()
                        if (cachedSnapshot.exists()) {
                            val entity = parsePartnerDaily(docId, partnerUid, dateStr, cachedSnapshot.data)
                            foundDaily = entity
                            matchedRoom = room
                            break
                        }
                    } catch (ignored: Exception) {}
                }
            }

            // Step 3: If not found for today, probe yesterday's document
            if (foundDaily == null) {
                val parsedDate = try {
                    LocalDate.parse(dateStr)
                } catch (e: Exception) {
                    LocalDate.now()
                }
                val yesterdayStr = parsedDate.minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
                val yesterdayDocId = "${partnerUid}_$yesterdayStr"

                for (room in candidateRooms) {
                    val yesterdayRef = firestore.collection("couples")
                        .document(room)
                        .collection("daily")
                        .document(yesterdayDocId)

                    try {
                        val snapshot = withTimeout(10000L) {
                            yesterdayRef.get(Source.SERVER).await()
                        }
                        if (snapshot.exists()) {
                            val entity = parsePartnerDaily(yesterdayDocId, partnerUid, yesterdayStr, snapshot.data)
                            foundDaily = entity
                            matchedRoom = room
                            Timber.d("Found partner yesterday daily in couples/$room/daily/$yesterdayDocId (steps: ${entity.steps})")
                            break
                        }
                    } catch (e: Exception) {
                        try {
                            val cachedSnap = yesterdayRef.get().await()
                            if (cachedSnap.exists()) {
                                val entity = parsePartnerDaily(yesterdayDocId, partnerUid, yesterdayStr, cachedSnap.data)
                                foundDaily = entity
                                matchedRoom = room
                                break
                            }
                        } catch (ignored: Exception) {}
                    }
                }
            }

            // Step 4: If still not found, query couples/{room}/daily for userId == partnerUid limit(1)
            if (foundDaily == null) {
                for (room in candidateRooms) {
                    try {
                        val querySnapshot = firestore.collection("couples")
                            .document(room)
                            .collection("daily")
                            .whereEqualTo("userId", partnerUid)
                            .limit(1)
                            .get(Source.SERVER)
                            .await()

                        val doc = querySnapshot.documents.firstOrNull()
                        if (doc != null && doc.exists()) {
                            val docDate = doc.getString("date") ?: doc.getString("dateStr") ?: dateStr
                            val entity = parsePartnerDaily(doc.id, partnerUid, docDate, doc.data)
                            foundDaily = entity
                            matchedRoom = room
                            Timber.d("Found partner daily via query in couples/$room/daily/${doc.id}")
                            break
                        }
                    } catch (e: Exception) {
                        Timber.w(e, "Query fallback failed in couples/$room/daily")
                    }
                }
            }

            // Step 5: If data found, insert into Room and handle auto-convergence
            if (foundDaily != null) {
                healthDao.insertPartnerDaily(foundDaily)

                // AUTO-CONVERGENCE:
                // If matched room differs from local coupleId, converge to matchedRoom
                val targetRoom = matchedRoom ?: partnerRemoteCoupleId
                if (targetRoom != null && targetRoom.isNotBlank() && targetRoom != coupleId && currentUid != null) {
                    Timber.i("Auto-converging couple rooms: local=$coupleId -> remote=$targetRoom")
                    try {
                        // 1. Update local SharedPreferences session cache
                        context?.let { ctx ->
                            val prefs = ctx.getSharedPreferences("couplefit_session_cache", Context.MODE_PRIVATE)
                            prefs.edit().putString("cached_couple_id", targetRoom).apply()
                        }
                        // 2. Update current user document in Firestore
                        firestore.collection("users").document(currentUid).set(
                            mapOf(
                                "coupleId" to targetRoom,
                                "partnerId" to partnerUid,
                                "status" to "LINKED",
                                "updatedAt" to Timestamp.now()
                            ),
                            SetOptions.merge()
                        ).await()

                        // 3. Join target couple room members
                        firestore.collection("couples").document(targetRoom).update(
                            "members",
                            FieldValue.arrayUnion(currentUid)
                        ).await()

                        Timber.i("Successfully auto-converged device $currentUid into couple room $targetRoom")
                    } catch (convEx: Exception) {
                        Timber.w(convEx, "Auto-convergence update had a non-fatal exception")
                    }
                }

                Result.success(foundDaily)
            } else {
                Timber.d("No remote partner daily found for $partnerUid in candidate rooms $candidateRooms")
                Result.success(null)
            }
        } catch (e: Exception) {
            Timber.e(e, "Exception during syncPartnerDailyFromRemote for $partnerUid")
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
