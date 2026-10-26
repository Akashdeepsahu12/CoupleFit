package com.couplefit.data.remote

import com.couplefit.data.model.InviteResult
import com.couplefit.data.model.RedeemResult
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class CoupleLinkManager @Inject constructor(
    private val functions: FirebaseFunctions,
    private val firestore: FirebaseFirestore? = null,
    private val firebaseAuth: FirebaseAuth? = null
) {
    private val effectiveFirestore: FirebaseFirestore
        get() = firestore ?: FirebaseFirestore.getInstance()

    private val effectiveAuth: FirebaseAuth
        get() = firebaseAuth ?: FirebaseAuth.getInstance()

    companion object {
        @Volatile
        var cachedInvite: InviteResult? = null
    }

    /**
     * Generates a random 6-digit pairing code (100000 - 999999).
     */
    private fun generate6DigitCode(): String {
        return Random.nextInt(100000, 1000000).toString()
    }

    /**
     * Calls Cloud Function 'createInvite' to generate a 6-digit invite code
     * and initialize couples/{coupleId} with 24-hour expiration.
     * Falls back to direct Firestore atomic creation if Cloud Functions are offline or undeployed.
     */
    suspend fun createInvite(): Result<InviteResult> {
        // 1. Try Cloud Function first
        try {
            val callable = functions.getHttpsCallable("createInvite")
            val callResult = withTimeoutOrNull(6000L) {
                callable.call().await()
            }
            val data = callResult?.data as? Map<*, *>
            if (data != null) {
                val coupleId = data["coupleId"] as? String ?: ""
                val inviteCode = (data["inviteCode"] as? String)?.filter { it.isDigit() }?.take(6) ?: ""
                val expiresAt = (data["expiresAt"] as? Number)?.toLong() ?: (System.currentTimeMillis() + 24 * 60 * 60 * 1000L)
                val inviteUrl = data["inviteUrl"] as? String ?: "https://couplefit.app/invite/$inviteCode"

                if (inviteCode.length == 6) {
                    val inviteResult = InviteResult(
                        coupleId = coupleId,
                        inviteCode = inviteCode,
                        expiresAt = expiresAt,
                        inviteUrl = inviteUrl
                    )
                    cachedInvite = inviteResult
                    Timber.d("Generated 6-digit invite code via Cloud Functions: $inviteCode")
                    return Result.success(inviteResult)
                }
            }
        } catch (e: Exception) {
            Timber.w(e, "Cloud Function createInvite unavailable, falling back to direct Firestore creation")
        }

        // 2. Fallback: Direct Firestore atomic generation with local resilience
        return try {
            val callerUid = effectiveAuth.currentUser?.uid
                ?: return Result.failure(IllegalStateException("You must be signed in to create an invite code."))

            val code = generate6DigitCode()
            val coupleId = "cpl_${System.currentTimeMillis()}_${callerUid.take(5)}"
            val now = System.currentTimeMillis()
            val expiresAt = now + 24 * 60 * 60 * 1000L
            val inviteUrl = "https://couplefit.app/invite/$code"

            val inviteResult = InviteResult(
                coupleId = coupleId,
                inviteCode = code,
                expiresAt = expiresAt,
                inviteUrl = inviteUrl
            )

            val batch = effectiveFirestore.batch()

            // Create invite record in invites/{code}
            val inviteRef = effectiveFirestore.collection("invites").document(code)
            batch.set(inviteRef, mapOf(
                "code" to code,
                "coupleId" to coupleId,
                "createdBy" to callerUid,
                "createdAt" to Timestamp.now(),
                "expiresAt" to Timestamp(Date(expiresAt)),
                "status" to "PENDING"
            ))

            // Create couple group with creator as first member
            val coupleRef = effectiveFirestore.collection("couples").document(coupleId)
            batch.set(coupleRef, mapOf(
                "id" to coupleId,
                "members" to listOf(callerUid),
                "status" to "INVITING",
                "inviteCode" to code,
                "createdBy" to callerUid,
                "createdAt" to Timestamp.now()
            ))

            // Update user record
            val userRef = effectiveFirestore.collection("users").document(callerUid)
            batch.set(userRef, mapOf(
                "uid" to callerUid,
                "coupleId" to coupleId,
                "status" to "INVITING",
                "updatedAt" to Timestamp.now()
            ), SetOptions.merge())

            batch.commit().await()
            cachedInvite = inviteResult
            Timber.d("Committed invite code $code to Cloud Firestore successfully")
            Result.success(inviteResult)
        } catch (e: Exception) {
            Timber.e(e, "Failed to create invite in Cloud Firestore")
            val message = if (e.message?.contains("Cloud Firestore API has not been used", ignoreCase = true) == true) {
                "Cloud Firestore database is not activated yet in project couplefit-6053a. Please create the database in the Firebase Console."
            } else if (e.message?.contains("PERMISSION_DENIED", ignoreCase = true) == true ||
                e.message?.contains("insufficient permissions", ignoreCase = true) == true) {
                "Firestore Security Rules are denying writes. Please update rules in Firebase Console to allow authenticated users."
            } else {
                e.localizedMessage ?: "Failed to generate invite code."
            }
            Result.failure(Exception(message, e))
        }
    }

    /**
     * Redeems a 6-digit invite code.
     * Enforces that code is not expired, max 2 members, and atomically connects both partners.
     * Tries Cloud Function first and falls back to Firestore atomic transaction.
     */
    suspend fun redeemInvite(code: String): Result<RedeemResult> {
        val sanitizedCode = code.trim().filter { it.isDigit() }.take(6)
        if (sanitizedCode.length != 6) {
            return Result.failure(IllegalArgumentException("Invite code must be exactly 6 digits."))
        }

        // 1. Try Cloud Function first
        try {
            val callable = functions.getHttpsCallable("redeemInvite")
            val payload = mapOf("code" to sanitizedCode)
            val callResult = withTimeoutOrNull(6000L) {
                callable.call(payload).await()
            }
            val data = callResult?.data as? Map<*, *>
            if (data != null) {
                val success = data["success"] as? Boolean ?: true
                val coupleId = data["coupleId"] as? String ?: ""
                val partnerId = data["partnerId"] as? String ?: ""
                val message = data["message"] as? String ?: "Successfully linked with partner!"

                val redeemResult = RedeemResult(
                    success = success,
                    coupleId = coupleId,
                    partnerId = partnerId,
                    message = message
                )
                Timber.d("Successfully redeemed invite code via Cloud Functions. Partner: $partnerId")
                return Result.success(redeemResult)
            }
        } catch (e: Exception) {
            Timber.w(e, "Cloud Function redeemInvite unavailable, falling back to direct Firestore transaction")
        }

        // 2. Direct Firestore atomic transaction (no fake local bypass)
        return try {
            val callerUid = effectiveAuth.currentUser?.uid
                ?: return Result.failure(IllegalStateException("Please sign in before connecting with your partner."))

            val result = effectiveFirestore.runTransaction { transaction ->
                val inviteRef = effectiveFirestore.collection("invites").document(sanitizedCode)
                val inviteDoc = transaction.get(inviteRef)

                if (!inviteDoc.exists()) {
                    throw IllegalStateException("Invalid invite code. Please check the 6-digit number.")
                }

                val status = inviteDoc.getString("status") ?: "PENDING"
                if (status != "PENDING") {
                    throw IllegalStateException("This invite code has already been redeemed.")
                }

                val creatorUid = inviteDoc.getString("createdBy")
                    ?: throw IllegalStateException("Invite creator information is missing.")

                if (callerUid == creatorUid) {
                    throw IllegalStateException("You cannot redeem your own pairing code.")
                }

                val expiresTimestamp = inviteDoc.getTimestamp("expiresAt")
                if (expiresTimestamp != null && System.currentTimeMillis() > expiresTimestamp.toDate().time) {
                    throw IllegalStateException("This invite code has expired. Please ask your partner to generate a new one.")
                }

                val coupleId = inviteDoc.getString("coupleId")
                    ?: throw IllegalStateException("Associated couple was not found.")

                val coupleRef = effectiveFirestore.collection("couples").document(coupleId)
                val coupleDoc = transaction.get(coupleRef)
                if (!coupleDoc.exists()) {
                    throw IllegalStateException("The couple group associated with this invite no longer exists.")
                }

                val currentMembers = (coupleDoc.get("members") as? List<*>)?.filterIsInstance<String>() ?: listOf(creatorUid)
                if (currentMembers.size >= 2) {
                    throw IllegalStateException("This couple already has 2 members.")
                }

                // Update couple members to [creatorUid, callerUid]
                val updatedMembers = listOf(creatorUid, callerUid)
                transaction.update(coupleRef, mapOf(
                    "members" to updatedMembers,
                    "status" to "LINKED",
                    "linkedAt" to Timestamp.now()
                ))

                // Mark invite REDEEMED
                transaction.update(inviteRef, mapOf(
                    "status" to "REDEEMED",
                    "redeemedBy" to callerUid,
                    "redeemedAt" to Timestamp.now()
                ))

                // Link both users bidirectionally
                val callerUserRef = effectiveFirestore.collection("users").document(callerUid)
                transaction.set(callerUserRef, mapOf(
                    "uid" to callerUid,
                    "coupleId" to coupleId,
                    "partnerId" to creatorUid,
                    "status" to "LINKED",
                    "updatedAt" to Timestamp.now()
                ), SetOptions.merge())

                val creatorUserRef = effectiveFirestore.collection("users").document(creatorUid)
                transaction.set(creatorUserRef, mapOf(
                    "uid" to creatorUid,
                    "coupleId" to coupleId,
                    "partnerId" to callerUid,
                    "status" to "LINKED",
                    "updatedAt" to Timestamp.now()
                ), SetOptions.merge())

                RedeemResult(
                    success = true,
                    coupleId = coupleId,
                    partnerId = creatorUid,
                    message = "Successfully linked with partner!"
                )
            }.await()

            Timber.d("Successfully redeemed invite code via Firestore transaction. Partner: ${result.partnerId}")
            Result.success(result)
        } catch (e: Exception) {
            Timber.e(e, "Error redeeming invite via Firestore transaction")
            val message = if (e.message?.contains("Cloud Firestore API has not been used", ignoreCase = true) == true) {
                "Cloud Firestore database is not activated yet in project couplefit-6053a. Please create the database in the Firebase Console."
            } else if (e.message?.contains("PERMISSION_DENIED", ignoreCase = true) == true ||
                e.message?.contains("insufficient permissions", ignoreCase = true) == true) {
                "Firestore Security Rules are denying access. Please update rules in Firebase Console to allow authenticated users."
            } else {
                e.localizedMessage ?: "Failed to redeem pairing code."
            }
            Result.failure(Exception(message, e))
        }
    }

    /**
     * Calls Cloud Function 'unlinkCouple' to sever existing couple link.
     */
    suspend fun unlinkCouple(): Result<Unit> {
        return try {
            val callable = functions.getHttpsCallable("unlinkCouple")
            callable.call().await()
            Timber.d("Successfully unlinked couple")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error unlinking couple")
            Result.failure(e)
        }
    }

    /**
     * Calls Cloud Function 'deleteUserData' to permanently erase all user data,
     * daily snapshots, nudges, invites, and severed couple links from Firestore.
     */
    suspend fun deleteUserData(): Result<Unit> {
        return try {
            val callable = functions.getHttpsCallable("deleteUserData")
            callable.call().await()
            Timber.d("Successfully deleted user data from Firestore")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error deleting user data")
            Result.failure(e)
        }
    }

    /**
     * Real-time multi-source listener observing partner pairing status.
     * Continuously monitors:
     * 1. couples/{coupleId}: status == "LINKED" or members.size >= 2
     * 2. invites/{inviteCode}: status == "REDEEMED" and redeemedBy
     * 3. users/{myUid}: partnerId != null
     */
    fun observePartnerLink(
        coupleId: String,
        inviteCode: String,
        currentUid: String? = null
    ): Flow<Pair<String, String>?> = callbackFlow {
        val myUid = currentUid?.takeIf { it.isNotBlank() } ?: effectiveAuth.currentUser?.uid ?: ""
        var inviteListener: ListenerRegistration? = null
        var coupleListener: ListenerRegistration? = null
        var userListener: ListenerRegistration? = null

        // 1. Observe couples/{coupleId}
        if (coupleId.isNotBlank()) {
            val coupleRef = effectiveFirestore.collection("couples").document(coupleId)
            coupleListener = coupleRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.w(error, "Error listening to couple doc $coupleId")
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val status = snapshot.getString("status")
                    val members = (snapshot.get("members") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                    val partnerId = members.firstOrNull { it != myUid && it.isNotBlank() }
                    if ((status == "LINKED" || members.size >= 2) && partnerId != null) {
                        Timber.d("Partner linked detected via couples/$coupleId: $partnerId")
                        trySend(Pair(coupleId, partnerId))
                    }
                }
            }
        }

        // 2. Observe invites/{inviteCode}
        if (inviteCode.isNotBlank()) {
            val inviteRef = effectiveFirestore.collection("invites").document(inviteCode)
            inviteListener = inviteRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.w(error, "Error listening to invite doc $inviteCode")
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val status = snapshot.getString("status")
                    val redeemedBy = snapshot.getString("redeemedBy")
                    val resolvedCoupleId = snapshot.getString("coupleId") ?: coupleId
                    if (status == "REDEEMED" && !redeemedBy.isNullOrBlank() && redeemedBy != myUid) {
                        Timber.d("Partner redeemed invite detected via invites/$inviteCode: $redeemedBy")
                        trySend(Pair(resolvedCoupleId, redeemedBy))
                    }
                }
            }
        }

        // 3. Observe users/{myUid}
        if (myUid.isNotBlank()) {
            val userRef = effectiveFirestore.collection("users").document(myUid)
            userListener = userRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.w(error, "Error listening to user doc $myUid")
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val partnerId = snapshot.getString("partnerId")
                    val resolvedCoupleId = snapshot.getString("coupleId") ?: coupleId
                    if (!partnerId.isNullOrBlank() && partnerId != myUid && !resolvedCoupleId.isNullOrBlank()) {
                        Timber.d("Partner linked detected via users/$myUid: $partnerId")
                        trySend(Pair(resolvedCoupleId, partnerId))
                    }
                }
            }
        }

        awaitClose {
            inviteListener?.remove()
            coupleListener?.remove()
            userListener?.remove()
        }
    }

    /**
     * Immediate one-shot query to check if partner has already redeemed the code.
     */
    suspend fun checkPartnerLinkStatus(
        coupleId: String?,
        inviteCode: String?,
        currentUid: String? = null
    ): Result<Pair<String, String>?> {
        return try {
            val myUid = currentUid?.takeIf { it.isNotBlank() } ?: effectiveAuth.currentUser?.uid ?: ""

            // 1. Check users/{myUid}
            if (myUid.isNotBlank()) {
                val userDoc = effectiveFirestore.collection("users").document(myUid).get().await()
                if (userDoc.exists()) {
                    val partnerId = userDoc.getString("partnerId")
                    val cplId = userDoc.getString("coupleId") ?: coupleId
                    if (!partnerId.isNullOrBlank() && partnerId != myUid && !cplId.isNullOrBlank()) {
                        return Result.success(Pair(cplId, partnerId))
                    }
                }
            }

            // 2. Check couples/{coupleId}
            if (!coupleId.isNullOrBlank()) {
                val coupleDoc = effectiveFirestore.collection("couples").document(coupleId).get().await()
                if (coupleDoc.exists()) {
                    val status = coupleDoc.getString("status")
                    val members = (coupleDoc.get("members") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                    val partnerId = members.firstOrNull { it != myUid && it.isNotBlank() }
                    if ((status == "LINKED" || members.size >= 2) && partnerId != null) {
                        return Result.success(Pair(coupleId, partnerId))
                    }
                }
            }

            // 3. Check invites/{inviteCode}
            if (!inviteCode.isNullOrBlank()) {
                val inviteDoc = effectiveFirestore.collection("invites").document(inviteCode).get().await()
                if (inviteDoc.exists()) {
                    val status = inviteDoc.getString("status")
                    val redeemedBy = inviteDoc.getString("redeemedBy")
                    val cplId = inviteDoc.getString("coupleId") ?: coupleId
                    if (status == "REDEEMED" && !redeemedBy.isNullOrBlank() && redeemedBy != myUid && !cplId.isNullOrBlank()) {
                        return Result.success(Pair(cplId, redeemedBy))
                    }
                }
            }

            Result.success(null)
        } catch (e: Exception) {
            Timber.w(e, "Error during checkPartnerLinkStatus")
            Result.failure(e)
        }
    }

    /**
     * Persists the linked coupleId and partnerId to users/{myUid} so both devices
     * are persistently and bidirectionally linked.
     */
    suspend fun persistPartnerLinkLocallyAndRemote(myUid: String, coupleId: String, partnerId: String) {
        if (myUid.isBlank() || coupleId.isBlank() || partnerId.isBlank()) return
        try {
            val userRef = effectiveFirestore.collection("users").document(myUid)
            userRef.set(
                mapOf(
                    "uid" to myUid,
                    "coupleId" to coupleId,
                    "partnerId" to partnerId,
                    "status" to "LINKED",
                    "updatedAt" to Timestamp.now()
                ),
                SetOptions.merge()
            ).await()
            Timber.d("Successfully persisted partner link to users/$myUid (coupleId: $coupleId, partnerId: $partnerId)")
        } catch (e: Exception) {
            Timber.w(e, "Failed to persist partner link to users/$myUid")
        }
    }
}

