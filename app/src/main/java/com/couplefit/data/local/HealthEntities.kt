package com.couplefit.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "health_metrics")
data class HealthMetricEntity(
    @PrimaryKey val id: String, // format: "{metricType}_{timestamp}"
    val metricType: String, // e.g., "STEPS", "HEART_RATE", "ACTIVE_MINUTES", "CALORIES"
    val sourceTimestamp: Long, // Stored in UTC (Instant.toEpochMilli())
    val rawValue: Double,
    val normalizedValue: Double, // Normalized (e.g., proper units, deduplicated)
    val syncedToRemote: Boolean = false
)

@Entity(tableName = "health_daily_aggregates")
data class HealthDailyAggregateEntity(
    @PrimaryKey val dateStr: String, // format: "YYYY-MM-DD" in local time zone
    val totalSteps: Long = 0,
    val totalActiveMinutes: Long = 0,
    val totalCalories: Long = 0,
    val averageHeartRate: Double = 0.0,
    val averageOxygenSaturation: Double = 0.0,
    val lastUpdatedTimestamp: Long // UTC
)

@Entity(tableName = "partner_daily_aggregates")
data class PartnerDailyAggregateEntity(
    @PrimaryKey val id: String, // format: "{partnerId}_{dateStr}"
    val partnerId: String,
    val dateStr: String, // format: "YYYY-MM-DD"
    val totalSteps: Long = 0,
    val totalActiveMinutes: Long = 0,
    val totalCalories: Long = 0,
    val averageHeartRate: Double = 0.0,
    val averageOxygenSaturation: Double = 0.0,
    val lastUpdatedTimestamp: Long // UTC
)

@Entity(tableName = "partnerDaily")
data class PartnerDailyEntity(
    @PrimaryKey val id: String, // format: "{partnerId}_{dateStr}"
    val partnerId: String,
    val dateStr: String, // format: "YYYY-MM-DD"
    val steps: Long = 0,
    val activeMinutes: Long = 0,
    val calories: Long = 0,
    val avgHr: Double = 0.0,
    val avgSpo2: Double? = null,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "coupleChallenges")
data class CoupleChallengeEntity(
    @PrimaryKey val id: String,
    val coupleId: String,
    val type: String, // "STEPS_RACE", "ACTIVE_MINUTES_STREAK"
    val goal: Long,
    val start: String,
    val end: String,
    val status: String, // "ACTIVE", "COMPLETED", "EXPIRED"
    val myProgress: Long = 0L,
    val partnerProgress: Long = 0L,
    val winnerUserId: String? = null,
    val completedAt: Long? = null,
    val lastUpdated: Long = System.currentTimeMillis()
)


