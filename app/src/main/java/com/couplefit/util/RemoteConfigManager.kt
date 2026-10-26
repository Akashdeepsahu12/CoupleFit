package com.couplefit.util

import com.couplefit.BuildConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteConfigManager @Inject constructor(
    private val remoteConfig: FirebaseRemoteConfig
) {

    companion object {
        const val KEY_STEPS_RACE_GOAL = "challenge_steps_race_goal"
        const val KEY_ACTIVE_MINUTES_GOAL = "challenge_active_minutes_goal"
        const val KEY_DAILY_STEP_GOAL = "daily_step_goal_default"
        const val KEY_DAILY_ACTIVE_MINUTES_GOAL = "daily_active_minutes_goal_default"
        const val KEY_DAILY_CALORIES_GOAL = "daily_calories_goal_default"
        const val KEY_FEATURE_CHALLENGES = "feature_challenges_enabled"
        const val KEY_FEATURE_GLANCE_WIDGET = "feature_glance_widget_enabled"
        const val KEY_FEATURE_NUDGES = "feature_nudges_enabled"
        const val KEY_NUDGE_COOLDOWN_SECONDS = "nudge_cooldown_seconds"

        val DEFAULTS = mapOf<String, Any>(
            KEY_STEPS_RACE_GOAL to 50_000L,
            KEY_ACTIVE_MINUTES_GOAL to 7L,
            KEY_DAILY_STEP_GOAL to 10_000L,
            KEY_DAILY_ACTIVE_MINUTES_GOAL to 30L,
            KEY_DAILY_CALORIES_GOAL to 500L,
            KEY_FEATURE_CHALLENGES to true,
            KEY_FEATURE_GLANCE_WIDGET to true,
            KEY_FEATURE_NUDGES to true,
            KEY_NUDGE_COOLDOWN_SECONDS to 60L
        )
    }

    init {
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(if (BuildConfig.DEBUG) 0L else 3600L)
            .build()
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(DEFAULTS)
    }

    suspend fun fetchAndActivate(): Boolean {
        return try {
            val updated = remoteConfig.fetchAndActivate().await()
            Timber.d("Firebase Remote Config fetchAndActivate completed (updated=$updated)")
            updated
        } catch (e: Exception) {
            Timber.w(e, "Firebase Remote Config fetch failed, falling back to local defaults")
            false
        }
    }

    val stepsRaceGoal: Long
        get() = remoteConfig.getLong(KEY_STEPS_RACE_GOAL)

    val activeMinutesStreakGoal: Long
        get() = remoteConfig.getLong(KEY_ACTIVE_MINUTES_GOAL)

    val dailyStepGoal: Long
        get() = remoteConfig.getLong(KEY_DAILY_STEP_GOAL)

    val dailyActiveMinutesGoal: Long
        get() = remoteConfig.getLong(KEY_DAILY_ACTIVE_MINUTES_GOAL)

    val dailyCaloriesGoal: Long
        get() = remoteConfig.getLong(KEY_DAILY_CALORIES_GOAL)

    val isChallengesEnabled: Boolean
        get() = remoteConfig.getBoolean(KEY_FEATURE_CHALLENGES)

    val isGlanceWidgetEnabled: Boolean
        get() = remoteConfig.getBoolean(KEY_FEATURE_GLANCE_WIDGET)

    val isNudgesEnabled: Boolean
        get() = remoteConfig.getBoolean(KEY_FEATURE_NUDGES)

    val nudgeCooldownSeconds: Long
        get() = remoteConfig.getLong(KEY_NUDGE_COOLDOWN_SECONDS)
}
