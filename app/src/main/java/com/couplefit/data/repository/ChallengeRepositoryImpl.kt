package com.couplefit.data.repository

import com.couplefit.data.local.CoupleChallengeEntity
import com.couplefit.data.local.HealthDao
import com.couplefit.domain.repository.ChallengeRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChallengeRepositoryImpl @Inject constructor(
    private val healthDao: HealthDao,
    private val firestore: FirebaseFirestore
) : ChallengeRepository {

    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    override fun getChallengesFlow(coupleId: String): Flow<List<CoupleChallengeEntity>> {
        return healthDao.getChallengesFlow(coupleId)
    }

    override fun getActiveChallengesFlow(coupleId: String): Flow<List<CoupleChallengeEntity>> {
        return healthDao.getActiveChallengesFlow(coupleId)
    }

    override fun listenToCoupleChallenges(
        coupleId: String,
        myUid: String,
        partnerUid: String
    ): Flow<List<CoupleChallengeEntity>> = callbackFlow {
        val challengesCol = firestore.collection("couples")
            .document(coupleId)
            .collection("challenges")

        val registration = challengesCol.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Timber.w(error, "Error listening to challenges in couple $coupleId")
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val entities = snapshot.documents.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    val progressMap = (data["progress"] as? Map<*, *>) ?: emptyMap<String, Any>()
                    val myProg = (progressMap[myUid] as? Number)?.toLong() ?: 0L
                    val partnerProg = (progressMap[partnerUid] as? Number)?.toLong() ?: 0L

                    CoupleChallengeEntity(
                        id = doc.id,
                        coupleId = coupleId,
                        type = data["type"] as? String ?: "STEPS_RACE",
                        goal = (data["goal"] as? Number)?.toLong()
                            ?: ((data["targetValue"] as? Number)?.toLong() ?: 50000L),
                        start = data["start"] as? String ?: (data["startDate"] as? String ?: ""),
                        end = data["end"] as? String ?: (data["endDate"] as? String ?: ""),
                        status = data["status"] as? String ?: "ACTIVE",
                        myProgress = myProg,
                        partnerProgress = partnerProg,
                        winnerUserId = data["winnerUserId"] as? String,
                        completedAt = (data["completedAt"] as? com.google.firebase.Timestamp)?.toDate()?.time,
                        lastUpdated = System.currentTimeMillis()
                    )
                }

                repositoryScope.launch {
                    healthDao.insertChallenges(entities)
                    Timber.d("Cached ${entities.size} couple challenges to Room for couple $coupleId")
                }
            }
        }

        // Emit from Room Single Source of Truth
        val job = launch {
            healthDao.getChallengesFlow(coupleId).collect { challenges ->
                trySend(challenges)
            }
        }

        awaitClose {
            registration.remove()
            job.cancel()
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun createChallenge(
        coupleId: String,
        type: String,
        goal: Long,
        start: String,
        end: String,
        creatorUid: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val challengeRef = firestore.collection("couples")
                .document(coupleId)
                .collection("challenges")
                .document()

            val payload = mapOf(
                "id" to challengeRef.id,
                "coupleId" to coupleId,
                "type" to type,
                "goal" to goal,
                "targetValue" to goal,
                "start" to start,
                "end" to end,
                "status" to "ACTIVE",
                "createdBy" to creatorUid,
                "progress" to mapOf(
                    creatorUid to 0L
                ),
                "winnerUserId" to null,
                "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
            )

            challengeRef.set(payload).await()
            Timber.d("Created challenge ${challengeRef.id} in couple $coupleId")
            Result.success(challengeRef.id)
        } catch (e: Exception) {
            Timber.e(e, "Failed to create challenge in Firestore")
            Result.failure(e)
        }
    }
}
