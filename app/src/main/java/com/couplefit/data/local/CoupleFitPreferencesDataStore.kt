package com.couplefit.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

val Context.coupleFitDataStore: DataStore<Preferences> by preferencesDataStore(name = "couplefit_user_prefs")

data class MetricSharingPreferences(
    val shareSteps: Boolean = true,
    val shareWorkouts: Boolean = true,
    val shareCalories: Boolean = true,
    val shareHr: Boolean = false,
    val shareSpo2: Boolean = false
) {
    fun toFirestoreMap(): Map<String, Boolean> {
        return mapOf(
            "shareSteps" to shareSteps,
            "shareWorkouts" to shareWorkouts,
            "shareCalories" to shareCalories,
            "shareHr" to shareHr,
            "shareSpo2" to shareSpo2
        )
    }
}

@Singleton
class CoupleFitPreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firestore: FirebaseFirestore
) {
    companion object {
        val KEY_DARK_THEME = booleanPreferencesKey("is_dark_theme")
        val KEY_REDUCE_MOTION = booleanPreferencesKey("reduce_motion")
        val KEY_LIQUID_GLASS = booleanPreferencesKey("liquid_glass_enabled")
        val KEY_BIOMETRIC_LOCK = booleanPreferencesKey("biometric_lock")

        // Per-Metric Sharing Toggles (Phase 3)
        val KEY_SHARE_STEPS = booleanPreferencesKey("share_steps")
        val KEY_SHARE_WORKOUTS = booleanPreferencesKey("share_workouts")
        val KEY_SHARE_CALORIES = booleanPreferencesKey("share_calories")
        val KEY_SHARE_HR = booleanPreferencesKey("share_hr")
        val KEY_SHARE_SPO2 = booleanPreferencesKey("share_spo2")

        // Health Connect Changes API token
        val KEY_CHANGES_TOKEN = stringPreferencesKey("hc_changes_token")

        // FCM Device Token (Phase 4)
        val KEY_FCM_TOKEN = stringPreferencesKey("fcm_token")

        // Last remote sync timestamp (15-min throttle)
        val KEY_LAST_SYNC_TIMESTAMP = longPreferencesKey("last_sync_timestamp")

        // Tracks whether LinkedConfirmationScreen was already completed/seen
        val KEY_LINKED_CONFIRMATION_SHOWN = booleanPreferencesKey("linked_confirmation_shown")

        // Couple Profile & Intimacy Details (CPX-06, CPX-07)
        val KEY_USER_NAME = stringPreferencesKey("user_display_name")
        val KEY_PARTNER_NAME = stringPreferencesKey("partner_display_name")
        val KEY_PAIRED_DATE_TIMESTAMP = longPreferencesKey("paired_date_timestamp")
        val KEY_USER_BIRTHDAY = stringPreferencesKey("user_birthday")
        val KEY_PARTNER_BIRTHDAY = stringPreferencesKey("partner_birthday")
        val KEY_RELATIONSHIP_TYPE = stringPreferencesKey("relationship_type")
    }

    val isLinkedConfirmationShown: Flow<Boolean> = context.coupleFitDataStore.data.map { preferences ->
        preferences[KEY_LINKED_CONFIRMATION_SHOWN] ?: false
    }

    suspend fun setLinkedConfirmationShown(shown: Boolean) {
        context.coupleFitDataStore.edit { preferences ->
            preferences[KEY_LINKED_CONFIRMATION_SHOWN] = shown
        }
    }

    // App Preferences
    val isDarkTheme: Flow<Boolean?> = context.coupleFitDataStore.data.map { preferences ->
        preferences[KEY_DARK_THEME]
    }

    suspend fun setDarkTheme(enabled: Boolean) {
        context.coupleFitDataStore.edit { preferences ->
            preferences[KEY_DARK_THEME] = enabled
        }
    }

    val isReduceMotion: Flow<Boolean> = context.coupleFitDataStore.data.map { preferences ->
        preferences[KEY_REDUCE_MOTION] ?: false
    }

    suspend fun setReduceMotion(enabled: Boolean) {
        context.coupleFitDataStore.edit { preferences ->
            preferences[KEY_REDUCE_MOTION] = enabled
        }
    }

    val isLiquidGlassEnabled: Flow<Boolean> = context.coupleFitDataStore.data.map { preferences ->
        preferences[KEY_LIQUID_GLASS] ?: true
    }

    suspend fun setLiquidGlassEnabled(enabled: Boolean) {
        context.coupleFitDataStore.edit { preferences ->
            preferences[KEY_LIQUID_GLASS] = enabled
        }
    }

    // Per-Metric Sharing Preferences Flow
    val sharingPreferencesFlow: Flow<MetricSharingPreferences> = context.coupleFitDataStore.data.map { preferences ->
        MetricSharingPreferences(
            shareSteps = preferences[KEY_SHARE_STEPS] ?: true,
            shareWorkouts = preferences[KEY_SHARE_WORKOUTS] ?: true,
            shareCalories = preferences[KEY_SHARE_CALORIES] ?: true,
            shareHr = preferences[KEY_SHARE_HR] ?: false,
            shareSpo2 = preferences[KEY_SHARE_SPO2] ?: false
        )
    }

    suspend fun getSharingPreferences(): MetricSharingPreferences {
        return sharingPreferencesFlow.first()
    }

    /**
     * Updates local DataStore and mirrors the toggles to users/{uid}.sharingPrefs in Firestore
     */
    suspend fun updateSharingPreference(
        key: Preferences.Key<Boolean>,
        enabled: Boolean,
        uid: String? = null
    ) {
        context.coupleFitDataStore.edit { preferences ->
            preferences[key] = enabled
        }

        // Mirror to Firestore: users/{uid}.sharingPrefs
        if (!uid.isNullOrBlank()) {
            mirrorSharingPreferencesToFirestore(uid)
        }
    }    suspend fun setShareSteps(enabled: Boolean, uid: String? = null) {
        updateSharingPreference(KEY_SHARE_STEPS, enabled, uid)
    }

    suspend fun setShareWorkouts(enabled: Boolean, uid: String? = null) {
        updateSharingPreference(KEY_SHARE_WORKOUTS, enabled, uid)
    }

    suspend fun setShareCalories(enabled: Boolean, uid: String? = null) {
        updateSharingPreference(KEY_SHARE_CALORIES, enabled, uid)
    }

    suspend fun setShareHr(enabled: Boolean, uid: String? = null) {
        updateSharingPreference(KEY_SHARE_HR, enabled, uid)
    }

    suspend fun setShareSpo2(enabled: Boolean, uid: String? = null) {
        updateSharingPreference(KEY_SHARE_SPO2, enabled, uid)
    }

    suspend fun updateAllSharingPreferences(prefs: MetricSharingPreferences, uid: String? = null) {
        context.coupleFitDataStore.edit { preferences ->
            preferences[KEY_SHARE_STEPS] = prefs.shareSteps
            preferences[KEY_SHARE_WORKOUTS] = prefs.shareWorkouts
            preferences[KEY_SHARE_CALORIES] = prefs.shareCalories
            preferences[KEY_SHARE_HR] = prefs.shareHr
            preferences[KEY_SHARE_SPO2] = prefs.shareSpo2
        }
        if (!uid.isNullOrBlank()) {
            mirrorSharingPreferencesToFirestore(uid)
        }
    }

    suspend fun mirrorSharingPreferencesToFirestore(uid: String) {
        try {
            val currentPrefs = getSharingPreferences()
            val payload = mapOf("sharingPrefs" to currentPrefs.toFirestoreMap())
            firestore.collection("users").document(uid)
                .set(payload, SetOptions.merge())
            Timber.d("Successfully mirrored sharingPrefs to Firestore for user $uid")
        } catch (e: Exception) {
            Timber.w(e, "Failed to mirror sharing preferences to Firestore")
        }
    }

    // Changes API Token
    val changesTokenFlow: Flow<String?> = context.coupleFitDataStore.data.map { preferences ->
        preferences[KEY_CHANGES_TOKEN]
    }

    suspend fun getChangesToken(): String? {
        return changesTokenFlow.first()
    }

    suspend fun saveChangesToken(token: String?) {
        context.coupleFitDataStore.edit { preferences ->
            if (token != null) {
                preferences[KEY_CHANGES_TOKEN] = token
            } else {
                preferences.remove(KEY_CHANGES_TOKEN)
            }
        }
    }

    // Last Sync Timestamp (15 min throttle)
    suspend fun getLastSyncTimestamp(): Long {
        return context.coupleFitDataStore.data.map { it[KEY_LAST_SYNC_TIMESTAMP] ?: 0L }.first()
    }

    suspend fun updateLastSyncTimestamp(timestamp: Long = System.currentTimeMillis()) {
        context.coupleFitDataStore.edit { preferences ->
            preferences[KEY_LAST_SYNC_TIMESTAMP] = timestamp
        }
    }

    // FCM Device Token Handling (Phase 4)
    val fcmTokenFlow: Flow<String?> = context.coupleFitDataStore.data.map { preferences ->
        preferences[KEY_FCM_TOKEN]
    }

    suspend fun getFcmToken(): String? {
        return fcmTokenFlow.first()
    }

    suspend fun saveFcmToken(token: String) {
        context.coupleFitDataStore.edit { preferences ->
            preferences[KEY_FCM_TOKEN] = token
        }
    }

    suspend fun mirrorFcmTokenToFirestore(uid: String, token: String) {
        try {
            saveFcmToken(token)
            val payload = mapOf(
                "fcmToken" to token,
                "fcmTokenUpdatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
            )
            firestore.collection("users").document(uid)
                .set(payload, SetOptions.merge())
            Timber.d("Successfully mirrored fcmToken to users/$uid")
        } catch (e: Exception) {
            Timber.w(e, "Failed to mirror fcmToken to users/$uid")
        }
    }

    // Couple Details & Profile Management (CPX-06, CPX-07)
    val userNameFlow: Flow<String?> = context.coupleFitDataStore.data.map { it[KEY_USER_NAME] }
    val partnerNameFlow: Flow<String?> = context.coupleFitDataStore.data.map { it[KEY_PARTNER_NAME] }
    val pairedDateTimestampFlow: Flow<Long> = context.coupleFitDataStore.data.map {
        it[KEY_PAIRED_DATE_TIMESTAMP] ?: (System.currentTimeMillis() - 540L * 86_400_000L) // Default 540 days
    }
    val userBirthdayFlow: Flow<String> = context.coupleFitDataStore.data.map {
        it[KEY_USER_BIRTHDAY] ?: "June 14"
    }
    val partnerBirthdayFlow: Flow<String> = context.coupleFitDataStore.data.map {
        it[KEY_PARTNER_BIRTHDAY] ?: "September 28"
    }
    val relationshipTypeFlow: Flow<String> = context.coupleFitDataStore.data.map {
        it[KEY_RELATIONSHIP_TYPE] ?: "Fitness Partners & Life Comrades ❤️"
    }

    suspend fun getUserName(): String? = userNameFlow.first()
    suspend fun saveUserName(name: String) {
        context.coupleFitDataStore.edit { it[KEY_USER_NAME] = name }
    }

    suspend fun getPartnerName(): String? = partnerNameFlow.first()
    suspend fun savePartnerName(name: String) {
        context.coupleFitDataStore.edit { it[KEY_PARTNER_NAME] = name }
    }

    suspend fun savePairedDateTimestamp(timestamp: Long) {
        context.coupleFitDataStore.edit { it[KEY_PAIRED_DATE_TIMESTAMP] = timestamp }
    }

    suspend fun saveUserBirthday(bday: String) {
        context.coupleFitDataStore.edit { it[KEY_USER_BIRTHDAY] = bday }
    }

    suspend fun savePartnerBirthday(bday: String) {
        context.coupleFitDataStore.edit { it[KEY_PARTNER_BIRTHDAY] = bday }
    }

    suspend fun saveRelationshipType(type: String) {
        context.coupleFitDataStore.edit { it[KEY_RELATIONSHIP_TYPE] = type }
    }

    suspend fun clearAll() {
        context.coupleFitDataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
