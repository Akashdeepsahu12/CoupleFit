package com.couplefit.data.model

import java.util.Date

enum class AuthProvider { GOOGLE, EMAIL }

data class User(
    val id: String,
    val name: String,
    val gender: String, // e.g. "M", "F", "Other", "Prefer not to say"
    val partnerId: String?,
    val authProvider: AuthProvider
)

enum class ConnectionStatus { PENDING, LINKED, UNLINKED }

data class Couple(
    val id: String,
    val userAId: String,
    val userBId: String,
    val connectionStatus: ConnectionStatus,
    val createdAt: Date
)

data class HealthSnapshot(
    val userId: String,
    val date: Date,
    val steps: Int,
    val distanceMeters: Double,
    val floorsClimbed: Int = 0,
    val activeCalories: Double = 0.0,
    val sedentaryMinutes: Int = 0,
    val activeMinutes: Int = 0,
    val averageHeartRate: Double? = null,
    val averageOxygenSaturation: Double? = null
)


data class WorkoutSession(
    val id: String,
    val userId: String,
    val type: String,
    val startTime: Date,
    val endTime: Date,
    val durationMinutes: Int,
    val caloriesBurned: Double
)

enum class VitalType { RESTING_HR, WALKING_HR, CARDIO_FITNESS }

data class VitalReading(
    val id: String,
    val userId: String,
    val type: VitalType,
    val value: Double,
    val timestamp: Date
)

enum class AlertType { HIGH_HR, LOW_HR, IRREGULAR_RHYTHM }

data class CriticalAlert(
    val id: String,
    val userId: String,
    val type: AlertType,
    val value: String,
    val timestamp: Date,
    val acknowledged: Boolean
)

// SENSITIVE DATA
data class ECGReading(
    val id: String,
    val userId: String,
    val timestamp: Date,
    val classification: String,
    val waveformDataRef: String? // URL or file path to waveform
)

data class StressReading(
    val id: String,
    val userId: String,
    val level: Int, // 0-100
    val timestamp: Date
)

enum class BiometricType { SPO2, BODY_TEMP, RESPIRATORY_RATE }

data class BiometricReading(
    val id: String,
    val userId: String,
    val type: BiometricType,
    val value: Double,
    val timestamp: Date
)

// HIGH PRIVACY DATA
data class CycleLog(
    val id: String,
    val userId: String,
    val startDate: Date,
    val endDate: Date?,
    val symptoms: List<String>,
    val flowIntensity: String, // e.g., "Light", "Medium", "Heavy"
    val predictedNextStart: Date?
)

enum class ReminderStatus { PENDING, DELIVERED, COMPLETED, DISMISSED }

data class Reminder(
    val id: String,
    val createdByUserId: String,
    val targetUserId: String,
    val title: String,
    val message: String,
    val scheduledTime: Date,
    val recurrence: String?, // Cron or predefined like "DAILY"
    val status: ReminderStatus
)

data class SharingPreferences(
    val userId: String,
    val partnerId: String,
    val shareSteps: Boolean = true,
    val shareWorkouts: Boolean = true,
    val shareSleep: Boolean = true,
    val shareHR: Boolean = false,
    val shareSpO2: Boolean = false, // Strictly opt-in per-metric sharing toggle
    val shareCriticalAlerts: Boolean = true,
    val shareCycle: Boolean = false, // Strictly opt-in
    val shareMedications: Boolean = false // Strictly opt-in
)

enum class ChallengeType { TOTAL_STEPS, ACTIVE_MINUTES, CALORIES_BURNED, STREAK }
enum class ChallengeStatus { ACTIVE, COMPLETED, EXPIRED }

/**
 * Challenge state is managed and scored exclusively by Cloud Functions.
 * Clients have read-only access.
 */
data class Challenge(
    val id: String,
    val coupleId: String,
    val title: String,
    val description: String,
    val type: ChallengeType,
    val targetValue: Double,
    val userAProgress: Double = 0.0,
    val userBProgress: Double = 0.0,
    val status: ChallengeStatus = ChallengeStatus.ACTIVE,
    val startDate: Date,
    val endDate: Date,
    val winnerUserId: String? = null
)

enum class MilestoneType { COMBINED_STEPS, STREAK_DAYS, TOTAL_CALORIES }

/**
 * Milestones are evaluated and awarded exclusively on the server by Cloud Functions.
 * Clients have read-only access.
 */
data class Milestone(
    val id: String,
    val coupleId: String,
    val title: String,
    val description: String,
    val type: MilestoneType,
    val threshold: Double,
    val achievedAt: Date,
    val celebratoryMessage: String
)



