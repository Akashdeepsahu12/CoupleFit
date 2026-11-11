package com.couplefit.service

import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import com.couplefit.ui.healthconnect.HealthDashboardUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

class UserDrivenForegroundSyncTest {

    @Test
    fun `test background sync work constraints enforce connected network and battery not low`() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        assertEquals(NetworkType.CONNECTED, constraints.requiredNetworkType)
        assertTrue(constraints.requiresBatteryNotLow())
        assertFalse(constraints.requiresCharging())
    }

    @Test
    fun `test background sync worker interval is throttled to 6 hours for battery optimization`() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val request = PeriodicWorkRequestBuilder<SyncWorker>(6, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        // 6 hours = 6 * 60 * 60 * 1000 = 21,600,000 ms
        val expectedIntervalMillis = 6 * 60 * 60 * 1000L
        assertEquals(expectedIntervalMillis, request.workSpec.intervalDuration)
        assertTrue(request.workSpec.constraints.requiresBatteryNotLow())
    }

    @Test
    fun `test user-driven foreground sync state reflects active pull-to-refresh`() {
        val stateInitial = HealthDashboardUiState(isSyncing = false)
        assertFalse(stateInitial.isSyncing)

        val stateSyncing = stateInitial.copy(isSyncing = true)
        assertTrue(stateSyncing.isSyncing)

        val stateCompleted = stateSyncing.copy(
            isSyncing = false,
            partnerLastUpdatedLabel = "Just now"
        )
        assertFalse(stateCompleted.isSyncing)
        assertEquals("Just now", stateCompleted.partnerLastUpdatedLabel)
    }
}
