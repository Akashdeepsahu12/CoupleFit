package com.couplefit.data.remote

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.couplefit.data.model.UserSession
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber
import com.couplefit.data.local.CoupleFitPreferencesDataStore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val preferencesDataStore: CoupleFitPreferencesDataStore
) {
    private val authScope = CoroutineScope(Dispatchers.IO)
    private val credentialManager by lazy { CredentialManager.create(context) }

    companion object {
        // Default Web Client ID for Credential Manager Google Sign-In (Firebase project couplefit-6053a)
        const val DEFAULT_WEB_CLIENT_ID = "574898513514-3p2kef3mqbr6qmcgefb4dfmtfsd8ce0o.apps.googleusercontent.com"
    }

    /**
     * Decodes and extracts date of birth / birthday claim from Google ID Token JWT payload.
     */
    fun extractBirthdayFromIdToken(idToken: String): String? {
        return try {
            val parts = idToken.split(".")
            if (parts.size >= 2) {
                val tokenPayload = parts[1]
                val decodedBytes = try {
                    java.util.Base64.getUrlDecoder().decode(tokenPayload)
                } catch (_: Exception) {
                    try {
                        java.util.Base64.getDecoder().decode(tokenPayload)
                    } catch (_: Exception) {
                        android.util.Base64.decode(tokenPayload, android.util.Base64.URL_SAFE or android.util.Base64.NO_PADDING or android.util.Base64.NO_WRAP)
                    }
                }
                val payloadString = String(decodedBytes, Charsets.UTF_8)
                // Use regex claim matcher first for seamless JVM testing and Android runtime parsing
                val regexMatch = Regex(""""(?:birthdate|birthday|dob)"\s*:\s*"([^"]+)"""")
                    .find(payloadString)?.groupValues?.get(1)
                val raw = if (!regexMatch.isNullOrBlank()) {
                    regexMatch
                } else {
                    try {
                        val json = org.json.JSONObject(payloadString)
                        when {
                            json.has("birthdate") -> json.optString("birthdate")
                            json.has("birthday") -> json.optString("birthday")
                            json.has("dob") -> json.optString("dob")
                            else -> null
                        }
                    } catch (_: Throwable) {
                        null
                    }
                }

                if (!raw.isNullOrBlank()) {
                    formatBirthday(raw)
                } else null
            } else null
        } catch (e: Exception) {
            Timber.w(e, "Could not extract birthday from Google ID token JWT payload")
            null
        }
    }

    /**
     * Formats raw birthdate into standardized human-readable format (e.g., "June 14").
     */
    fun formatBirthday(raw: String): String {
        val trimmed = raw.trim()
        return try {
            val normalized = trimmed.removePrefix("--")
            val parts = normalized.split("-", "/")
            when (parts.size) {
                3 -> {
                    val (month, day) = if (parts[0].length == 4) {
                        Pair(parts[1].toInt(), parts[2].toInt())
                    } else {
                        Pair(parts[0].toInt(), parts[1].toInt())
                    }
                    val monthName = java.time.Month.of(month).name.lowercase().replaceFirstChar { it.uppercase() }
                    "$monthName $day"
                }
                2 -> {
                    val month = parts[0].toInt()
                    val day = parts[1].toInt()
                    val monthName = java.time.Month.of(month).name.lowercase().replaceFirstChar { it.uppercase() }
                    "$monthName $day"
                }
                else -> trimmed
            }
        } catch (e: Exception) {
            trimmed
        }
    }

    /**
     * Resolves the Web Client ID dynamically:
     * 1. Checks R.string.default_web_client_id (from strings.xml or google-services.json)
     * 2. Falls back to DEFAULT_WEB_CLIENT_ID
     */
    fun getEffectiveWebClientId(): String {
        val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
        if (resId != 0) {
            val resClientId = context.getString(resId).trim()
            if (resClientId.isNotBlank() && !resClientId.contains("couplefit-android")) {
                return resClientId
            }
        }
        return DEFAULT_WEB_CLIENT_ID
    }

    /**
     * Signs in with Google using modern Android Credential Manager and connects to Firebase Auth.
     * Uses GetSignInWithGoogleOption for explicit button clicks, and falls back gracefully with
     * clear diagnostic advice if credentials are unavailable.
     */
    suspend fun signInWithGoogle(
        activityContext: Context,
        webClientId: String? = null
    ): Result<UserSession> {
        val effectiveWebClientId = webClientId?.takeIf { it.isNotBlank() } ?: getEffectiveWebClientId()

        return try {
            // Android Credential Manager requires GetSignInWithGoogleOption to be the SOLE option in the request.
            // We first try GetSignInWithGoogleOption (the standard explicit button sign-in option).
            // If that fails on the device or older Play Services, we fall back to GetGoogleIdOption alone.
            val credential = try {
                val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = effectiveWebClientId)
                    .build()
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(signInOption)
                    .build()
                val response = credentialManager.getCredential(activityContext, request)
                response.credential
            } catch (e: GetCredentialCancellationException) {
                throw e
            } catch (e: Exception) {
                Timber.w(e, "GetSignInWithGoogleOption failed, falling back to GetGoogleIdOption")
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(effectiveWebClientId)
                    .setAutoSelectEnabled(false)
                    .build()
                val fallbackRequest = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()
                val response = credentialManager.getCredential(activityContext, fallbackRequest)
                response.credential
            }

            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken

                // Authenticate with Firebase Auth
                val authCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = firebaseAuth.signInWithCredential(authCredential).await()
                val firebaseUser = authResult.user
                    ?: return Result.failure(IllegalStateException("Firebase user is null after sign in"))

                // Sync or retrieve user profile in Firestore with a timeout so network delays never block sign-in
                var coupleId: String? = null
                var partnerId: String? = null

                val extractedBirthday = extractBirthdayFromIdToken(idToken)
                    ?: credential.data.getString("birthdate")
                    ?: credential.data.getString("birthday")

                try {
                    withTimeoutOrNull(4000L) {
                        val userRef = firestore.collection("users").document(firebaseUser.uid)
                        val userDoc = userRef.get().await()

                        coupleId = userDoc.getString("coupleId")
                        partnerId = userDoc.getString("partnerId")

                        val profilePayload = mutableMapOf<String, Any>(
                            "uid" to firebaseUser.uid,
                            "displayName" to (firebaseUser.displayName ?: ""),
                            "email" to (firebaseUser.email ?: ""),
                            "photoUrl" to (firebaseUser.photoUrl?.toString() ?: ""),
                            "lastLoginAt" to System.currentTimeMillis()
                        )

                        if (!extractedBirthday.isNullOrBlank()) {
                            val formattedBday = formatBirthday(extractedBirthday)
                            profilePayload["birthday"] = formattedBday
                            profilePayload["birthdate"] = formattedBday
                            preferencesDataStore.saveUserBirthday(formattedBday)
                        } else {
                            val existingBday = userDoc.getString("birthday") ?: userDoc.getString("birthdate")
                            if (!existingBday.isNullOrBlank()) {
                                preferencesDataStore.saveUserBirthday(existingBday)
                            }
                        }

                        userRef.set(profilePayload, SetOptions.merge()).await()
                    }
                } catch (e: Exception) {
                    Timber.w(e, "Firestore user profile sync timed out or offline; proceeding with Firebase Auth session")
                }

                val session = UserSession(
                    uid = firebaseUser.uid,
                    displayName = firebaseUser.displayName ?: "",
                    email = firebaseUser.email ?: "",
                    photoUrl = firebaseUser.photoUrl?.toString(),
                    coupleId = coupleId,
                    partnerId = partnerId
                )

                Timber.d("Successfully signed in with Google: ${session.email}")
                Result.success(session)
            } else {
                Result.failure(IllegalArgumentException("Unsupported credential type: ${credential.type}"))
            }
        } catch (e: GetCredentialCancellationException) {
            Timber.i("Google Sign-In was cancelled by user")
            Result.failure(e)
        } catch (e: NoCredentialException) {
            Timber.w(e, "No credentials available on device")
            Result.failure(
                IllegalStateException(
                    "No Google credentials available on this device. Ensure a Google account is logged into device Settings > Accounts and that Google Sign-in is enabled in Firebase Console. You can also tap 'Continue as Demo'.",
                    e
                )
            )
        } catch (e: GetCredentialException) {
            Timber.e(e, "CredentialManager exception during sign in")
            val msg = if (e.message?.contains("no credential", ignoreCase = true) == true) {
                "No Google credentials available on this device. Ensure a Google account is added to your device/emulator in Settings > Accounts, or tap 'Continue as Demo'."
            } else {
                e.localizedMessage ?: "Google Sign-In failed. Please try again."
            }
            Result.failure(IllegalStateException(msg, e))
        } catch (e: Exception) {
            Timber.e(e, "General exception during sign in with Google")
            Result.failure(e)
        }
    }

    /**
     * Development and testing sign-in bypassing Google Play Services OAuth setup.
     * Uses Firebase Anonymous Auth or mock UID so all features (pairing, syncing,
     * challenges, widgets) work seamlessly out of the box.
     */
    suspend fun signInAsGuestOrDemo(
        displayName: String = "Test User"
    ): Result<UserSession> {
        return try {
            val authResult = try {
                withTimeoutOrNull(4000L) {
                    firebaseAuth.signInAnonymously().await()
                }
            } catch (e: Exception) {
                Timber.w(e, "Firebase anonymous auth failed, falling back to local guest session")
                null
            }

            val uid = authResult?.user?.uid ?: "demo_user_${System.currentTimeMillis().toString().takeLast(6)}"
            val email = authResult?.user?.email ?: "demo@couplefit.app"

            var coupleId: String? = null
            var partnerId: String? = null
            var resolvedName = displayName

            try {
                withTimeoutOrNull(4000L) {
                    val userRef = firestore.collection("users").document(uid)
                    val userDoc = userRef.get().await()

                    coupleId = userDoc.getString("coupleId")
                    partnerId = userDoc.getString("partnerId")
                    resolvedName = userDoc.getString("displayName") ?: displayName

                    val profilePayload = mutableMapOf<String, Any>(
                        "uid" to uid,
                        "displayName" to resolvedName,
                        "email" to (userDoc.getString("email") ?: email),
                        "photoUrl" to "",
                        "isDemo" to true,
                        "lastLoginAt" to System.currentTimeMillis()
                    )

                    userRef.set(profilePayload, SetOptions.merge()).await()
                }
            } catch (e: Exception) {
                Timber.w(e, "Firestore sync skipped for demo user")
            }

            val session = UserSession(
                uid = uid,
                displayName = resolvedName,
                email = email,
                photoUrl = null,
                coupleId = coupleId,
                partnerId = partnerId
            )

            Timber.d("Signed in as Demo User: $uid")
            Result.success(session)
        } catch (e: Exception) {
            Timber.e(e, "Error during guest/demo sign in")
            Result.failure(e)
        }
    }

    /**
     * Observes real-time authentication and user session state changes.
     */
    fun observeUserSession(): Flow<UserSession?> = callbackFlow {
        var firestoreListener: com.google.firebase.firestore.ListenerRegistration? = null
        val sessionPrefs = context.getSharedPreferences("couplefit_session_cache", Context.MODE_PRIVATE)

        val authListener = FirebaseAuth.AuthStateListener { auth ->
            val firebaseUser = auth.currentUser
            firestoreListener?.remove()
            firestoreListener = null

            if (firebaseUser == null) {
                trySend(null)
            } else {
                // Immediately emit base session pre-populated with locally cached couple linking state
                // to eliminate split-second routing flashes (CPX-01).
                val cachedCoupleId = sessionPrefs.getString("cached_couple_id", null)
                val cachedPartnerId = sessionPrefs.getString("cached_partner_id", null)
                val cachedDisplayName = sessionPrefs.getString("cached_display_name", null)
                val effectiveDisplayName = (firebaseUser.displayName ?: cachedDisplayName).orEmpty()

                trySend(
                    UserSession(
                        uid = firebaseUser.uid,
                        displayName = effectiveDisplayName,
                        email = firebaseUser.email ?: "",
                        photoUrl = firebaseUser.photoUrl?.toString(),
                        coupleId = cachedCoupleId,
                        partnerId = cachedPartnerId
                    )
                )

                // Then attach snapshot listener to get live coupleId and partnerId from Firestore
                firestoreListener = firestore.collection("users").document(firebaseUser.uid)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            Timber.w(error, "Error listening to user doc in Firestore")
                            return@addSnapshotListener
                        }
                        if (snapshot != null) {
                            val remoteCoupleId = snapshot.getString("coupleId")
                            val remotePartnerId = snapshot.getString("partnerId")
                            val cachedCoupleId = sessionPrefs.getString("cached_couple_id", null)
                            val cachedPartnerId = sessionPrefs.getString("cached_partner_id", null)
                            val effectiveCoupleId = remoteCoupleId?.takeIf { it.isNotBlank() } ?: cachedCoupleId
                            val effectivePartnerId = remotePartnerId?.takeIf { it.isNotBlank() } ?: cachedPartnerId
                            val displayName = snapshot.getString("displayName") ?: firebaseUser.displayName ?: ""
                            val partnerName = snapshot.getString("partnerName")
                            val userBday = snapshot.getString("birthday") ?: snapshot.getString("birthdate")

                            // Persist latest state into local cache
                            sessionPrefs.edit().apply {
                                putString("cached_couple_id", effectiveCoupleId)
                                putString("cached_partner_id", effectivePartnerId)
                                putString("cached_display_name", displayName)
                                putString("cached_partner_name", partnerName)
                                if (userBday != null) putString("cached_user_birthday", userBday)
                                apply()
                            }

                            if (displayName.isNotBlank()) {
                                authScope.launch {
                                    preferencesDataStore.saveUserName(displayName)
                                }
                            }
                            if (!userBday.isNullOrBlank()) {
                                authScope.launch {
                                    preferencesDataStore.saveUserBirthday(userBday)
                                }
                            }

                            // If partnerId exists, fetch partner's Firestore profile to get their authenticated displayName and birthday
                            if (!effectivePartnerId.isNullOrBlank() && effectivePartnerId != "partner_1") {
                                firestore.collection("users").document(effectivePartnerId).get()
                                    .addOnSuccessListener { pDoc ->
                                        if (pDoc != null && pDoc.exists()) {
                                            val pDisplayName = pDoc.getString("displayName")
                                            val pBday = pDoc.getString("birthday") ?: pDoc.getString("birthdate")
                                            if (!pDisplayName.isNullOrBlank()) {
                                                sessionPrefs.edit().putString("cached_partner_name", pDisplayName).apply()
                                                authScope.launch {
                                                    preferencesDataStore.savePartnerName(pDisplayName)
                                                }
                                            }
                                            if (!pBday.isNullOrBlank()) {
                                                authScope.launch {
                                                    preferencesDataStore.savePartnerBirthday(pBday)
                                                }
                                            }
                                        }
                                    }
                            }

                            trySend(
                                UserSession(
                                    uid = firebaseUser.uid,
                                    displayName = displayName,
                                    email = snapshot.getString("email") ?: firebaseUser.email ?: "",
                                    photoUrl = snapshot.getString("photoUrl") ?: firebaseUser.photoUrl?.toString(),
                                    coupleId = effectiveCoupleId,
                                    partnerId = effectivePartnerId
                                )
                            )
                        }
                    }
            }
        }

        firebaseAuth.addAuthStateListener(authListener)
        awaitClose {
            firestoreListener?.remove()
            firebaseAuth.removeAuthStateListener(authListener)
        }
    }

    fun getCurrentSession(): UserSession? {
        val firebaseUser = firebaseAuth.currentUser ?: return null
        val sessionPrefs = context.getSharedPreferences("couplefit_session_cache", Context.MODE_PRIVATE)
        return UserSession(
            uid = firebaseUser.uid,
            displayName = firebaseUser.displayName ?: sessionPrefs.getString("cached_display_name", "") ?: "",
            email = firebaseUser.email ?: "",
            photoUrl = firebaseUser.photoUrl?.toString(),
            coupleId = sessionPrefs.getString("cached_couple_id", null),
            partnerId = sessionPrefs.getString("cached_partner_id", null)
        )
    }

    fun signOut() {
        try {
            val sessionPrefs = context.getSharedPreferences("couplefit_session_cache", Context.MODE_PRIVATE)
            sessionPrefs.edit().clear().apply()
            firebaseAuth.signOut()
            Timber.d("User signed out and session cache cleared")
        } catch (e: Exception) {
            Timber.e(e, "Error signing out")
        }
    }
}
