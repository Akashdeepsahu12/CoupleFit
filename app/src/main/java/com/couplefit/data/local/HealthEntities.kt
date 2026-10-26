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
    val lastUpdatedTimestamp: Long // UTC
)
