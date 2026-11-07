package com.couplefit.ui.dashboard

import com.couplefit.ui.components.ChartDataPoint
import com.couplefit.ui.healthconnect.HealthDashboardUiState
import java.time.Instant

/**
 * Immutable Single-Source-of-Truth Dashboard UI State (Q7 / KRP-02).
 * Decomposes monolithic dashboard data into targeted tab models, eliminating
 * excessive recompositions and prop drilling across modular sub-components.
 */
data class DashboardUiState(
    val todayTab: TodayTabUiState = TodayTabUiState(),
    val myStats: MyStatsUiState = MyStatsUiState(),
    val partnerStats: PartnerStatsUiState = PartnerStatsUiState(),
    val isSyncing: Boolean = false,
    val lastSyncedAt: Instant? = null,
    val isHealthConnectAvailable: Boolean = true,
    val hasPermissions: Boolean = false,
    val nudgeSentMessage: String? = null
)

/**
 * Tab 0: Side-by-Side Bilateral Telemetry & Couple Synchrony state.
 */
data class TodayTabUiState(
    val userName: String = "You",
    val userSteps: Long = 0L,
    val userStepsProgress: Float = 0f,
    val userActiveMinutes: Long = 0L,
    val userCalories: Long = 0L,
    val partnerName: String = "Partner",
    val partnerSteps: Long = 0L,
    val partnerStepsProgress: Float = 0f,
    val partnerActiveMinutes: Long = 0L,
    val partnerCalories: Long = 0L,
    val partnerLastUpdated: Long? = null,
    val partnerLastUpdatedLabel: String = "Awaiting sync",
    val coupleId: String = "couple_1",
    val partnerUid: String = "partner_1",
    val bothRingsClosed: Boolean = false
)

/**
 * Tab 1: Live Health Connect Telemetry (Solo User).
 */
data class MyStatsUiState(
    val steps: Long = 0L,
    val stepsGoal: Long = 10000L,
    val stepsProgress: Float = 0f,
    val activeMinutes: Long = 0L,
    val exerciseProgress: Float = 0f,
    val caloriesBurned: Long = 0L,
    val averageHeartRate: Double = 0.0,
    val heartRatePoints: List<ChartDataPoint> = emptyList(),
    val oxygenSaturationPoints: List<ChartDataPoint> = emptyList()
)

/**
 * Tab 2: Partner Room SSOT Telemetry Cache.
 */
data class PartnerStatsUiState(
    val partnerId: String = "partner_1",
    val partnerName: String = "Partner",
    val steps: Long = 0L,
    val stepsProgress: Float = 0f,
    val activeMinutes: Long = 0L,
    val caloriesBurned: Long = 0L,
    val averageHeartRate: Double = 0.0,
    val averageOxygenSaturation: Double = 0.0,
    val lastUpdated: Long? = null,
    val lastUpdatedLabel: String = "Awaiting sync"
)

/**
 * Extension mapper from HealthDashboardUiState to DashboardUiState.
 */
fun HealthDashboardUiState.toDashboardUiState(): DashboardUiState {
    return DashboardUiState(
        todayTab = TodayTabUiState(
            userName = userName,
            userSteps = steps,
            userStepsProgress = stepsProgress,
            userActiveMinutes = activeMinutes,
            userCalories = caloriesBurned,
            partnerName = partnerName,
            partnerSteps = partnerSteps,
            partnerStepsProgress = partnerStepsProgress,
            partnerActiveMinutes = partnerActiveMinutes,
            partnerCalories = partnerCaloriesBurned,
            partnerLastUpdated = partnerLastUpdated,
            partnerLastUpdatedLabel = partnerLastUpdatedLabel,
            coupleId = coupleId,
            partnerUid = partnerId,
            bothRingsClosed = stepsProgress >= 1f && partnerStepsProgress >= 1f
        ),
        myStats = MyStatsUiState(
            steps = steps,
            stepsGoal = stepsGoal,
            stepsProgress = stepsProgress,
            activeMinutes = activeMinutes,
            exerciseProgress = exerciseProgress,
            caloriesBurned = caloriesBurned,
            averageHeartRate = averageHeartRate,
            heartRatePoints = heartRatePoints,
            oxygenSaturationPoints = oxygenSaturationPoints
        ),
        partnerStats = PartnerStatsUiState(
            partnerId = partnerId,
            partnerName = partnerName,
            steps = partnerSteps,
            stepsProgress = partnerStepsProgress,
            activeMinutes = partnerActiveMinutes,
            caloriesBurned = partnerCaloriesBurned,
            averageHeartRate = partnerAverageHeartRate,
            averageOxygenSaturation = partnerAverageOxygenSaturation,
            lastUpdated = partnerLastUpdated,
            lastUpdatedLabel = partnerLastUpdatedLabel
        ),
        isSyncing = isSyncing,
        lastSyncedAt = lastSyncedAt,
        isHealthConnectAvailable = isHealthConnectAvailable,
        hasPermissions = hasPermissions,
        nudgeSentMessage = nudgeSentMessage
    )
}
