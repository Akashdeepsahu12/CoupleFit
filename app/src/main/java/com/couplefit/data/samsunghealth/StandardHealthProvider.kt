package com.couplefit.data.samsunghealth

import com.couplefit.data.model.HealthSnapshot
import com.couplefit.data.model.WorkoutSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Provider for Standard Tier Samsung Health Data (Steps, Activity, etc.).
 */
interface StandardHealthDataProvider {
    fun observeDailySnapshot(userId: String, date: Date): Flow<HealthSnapshot?>
    fun observeWorkouts(userId: String, startDate: Date, endDate: Date): Flow<List<WorkoutSession>>
}

@Singleton
class DefaultStandardHealthDataProvider @Inject constructor(
    private val healthManager: SamsungHealthManager
) : StandardHealthDataProvider {
    
    override fun observeDailySnapshot(userId: String, date: Date): Flow<HealthSnapshot?> {
        // TODO: Query actual Samsung Health step count, calories, etc.
        // Return a mock for now
        return emptyFlow()
    }

    override fun observeWorkouts(userId: String, startDate: Date, endDate: Date): Flow<List<WorkoutSession>> {
        // TODO: Query actual Samsung Health workouts
        return emptyFlow()
    }
}

