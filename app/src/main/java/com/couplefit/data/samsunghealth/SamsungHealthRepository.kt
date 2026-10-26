package com.couplefit.data.samsunghealth

import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * Common interface for all Samsung Health metrics, abstracting away the SDK details.
 */
interface SamsungHealthRepository {
    
    // Standard-tier metrics (can be fetched normally)
    fun getSteps(startDate: Date, endDate: Date): Flow<Int>
    fun getDistanceMeters(startDate: Date, endDate: Date): Flow<Float>
    fun getActiveCalories(startDate: Date, endDate: Date): Flow<Float>
    fun getHeartRate(startDate: Date, endDate: Date): Flow<List<HeartRateReading>>
    
    // Sensitive-tier metrics (may require partner/privileged access)
    fun getSpO2(startDate: Date, endDate: Date): Flow<HealthResult<List<SpO2Reading>>>
    fun getEcgAlerts(startDate: Date, endDate: Date): Flow<HealthResult<List<EcgAlert>>>
}

// Sealed class to handle the NotYetAuthorized state for sensitive metrics
sealed class HealthResult<out T> {
    data class Success<out T>(val data: T) : HealthResult<T>()
    object NotYetAuthorized : HealthResult<Nothing>()
    data class Error(val exception: Throwable) : HealthResult<Nothing>()
}

// Data models for the SDK responses
data class HeartRateReading(
    val timestamp: Date,
    val bpm: Int
)

data class SpO2Reading(
    val timestamp: Date,
    val percentage: Int
)

data class EcgAlert(
    val timestamp: Date,
    val alertType: String // e.g., "AFib", "Irregular"
)

