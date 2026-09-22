package com.couplefit.data.samsunghealth

import com.couplefit.data.model.BiometricReading
import com.couplefit.data.model.CriticalAlert
import com.couplefit.data.model.ECGReading
import com.couplefit.data.model.StressReading
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Interface abstracting Sensitive (Restricted Tier) Samsung Health Data.
 * This requires special Partner/Privileged access approval from Samsung.
 */
interface SensitiveHealthDataProvider {
    fun observeECGReadings(userId: String): Flow<List<ECGReading>>
    fun observeStressReadings(userId: String): Flow<List<StressReading>>
    fun observeSpO2(userId: String): Flow<List<BiometricReading>>
    fun observeIrregularRhythmAlerts(userId: String): Flow<List<CriticalAlert>>
}

/**
 * Stub implementation for the standard app until partner access is granted.
 */
@Singleton
class StubSensitiveHealthDataProvider @Inject constructor() : SensitiveHealthDataProvider {
    override fun observeECGReadings(userId: String): Flow<List<ECGReading>> {
        // TODO: Return actual data once partner SDK access is enabled.
        return emptyFlow()
    }

    override fun observeStressReadings(userId: String): Flow<List<StressReading>> {
        return emptyFlow()
    }

    override fun observeSpO2(userId: String): Flow<List<BiometricReading>> {
        return emptyFlow()
    }

    override fun observeIrregularRhythmAlerts(userId: String): Flow<List<CriticalAlert>> {
        return emptyFlow()
    }
}

