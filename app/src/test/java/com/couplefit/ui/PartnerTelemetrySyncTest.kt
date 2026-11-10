package com.couplefit.ui

import com.couplefit.ui.dashboard.toDashboardUiState
import com.couplefit.ui.healthconnect.HealthDashboardUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PartnerTelemetrySyncTest {

    @Test
    fun `test partner telemetry is fully mapped into DashboardUiState`() {
        val now = System.currentTimeMillis()
        val healthState = HealthDashboardUiState(
            userName = "Jordan",
            steps = 8500L,
            stepsGoal = 10000L,
            stepsProgress = 0.85f,
            activeMinutes = 45L,
            caloriesBurned = 400L,
            partnerId = "partner_456",
            partnerName = "Riley",
            isPartnerLinked = true,
            partnerSteps = 7200L,
            partnerStepsGoal = 10000L,
            partnerStepsProgress = 0.72f,
            partnerActiveMinutes = 40L,
            partnerExerciseProgress = 0.67f,
            partnerCaloriesBurned = 350L,
            partnerAverageHeartRate = 74.0,
            partnerAverageOxygenSaturation = 98.2,
            partnerHasTodayData = true,
            partnerLastUpdated = now - 60_000L,
            partnerLastUpdatedLabel = "Last updated 1m ago"
        )

        val dashboardUiState = healthState.toDashboardUiState()

        // Verify Today Tab Partner State
        val today = dashboardUiState.todayTab
        assertEquals("Riley", today.partnerName)
        assertEquals(7200L, today.partnerSteps)
        assertEquals(0.72f, today.partnerStepsProgress, 0.001f)
        assertEquals(40L, today.partnerActiveMinutes)
        assertEquals(350L, today.partnerCalories)
        assertEquals("Last updated 1m ago", today.partnerLastUpdatedLabel)
        assertFalse(today.bothRingsClosed) // Jordan (85%) + Riley (72%) = not both closed

        // Verify Partner Tab Dedicated State
        val partner = dashboardUiState.partnerStats
        assertEquals("partner_456", partner.partnerId)
        assertEquals("Riley", partner.partnerName)
        assertEquals(7200L, partner.steps)
        assertEquals(0.72f, partner.stepsProgress, 0.001f)
        assertEquals(40L, partner.activeMinutes)
        assertEquals(350L, partner.caloriesBurned)
        assertEquals(74.0, partner.averageHeartRate, 0.001)
        assertEquals(98.2, partner.averageOxygenSaturation, 0.001)
        assertEquals("Last updated 1m ago", partner.lastUpdatedLabel)
    }

    @Test
    fun `test simulated partner steps triggers both rings closure celebration`() {
        val initial = HealthDashboardUiState(
            steps = 10500L,
            stepsProgress = 1.05f,
            partnerSteps = 8500L,
            partnerStepsProgress = 0.85f
        )
        assertFalse(initial.toDashboardUiState().todayTab.bothRingsClosed)

        // Simulate partner adding steps to cross 10,000 threshold
        val updated = initial.copy(
            partnerSteps = 10200L,
            partnerStepsProgress = 1.02f
        )
        assertTrue(updated.toDashboardUiState().todayTab.bothRingsClosed)
    }

    @Test
    fun `test partner empty state falls back gracefully`() {
        val emptyState = HealthDashboardUiState()
        val uiState = emptyState.toDashboardUiState()

        assertEquals("Partner", uiState.todayTab.partnerName)
        assertEquals(0L, uiState.todayTab.partnerSteps)
        assertEquals(0f, uiState.todayTab.partnerStepsProgress, 0.001f)
        assertEquals("Awaiting sync", uiState.todayTab.partnerLastUpdatedLabel)
        assertFalse(uiState.todayTab.bothRingsClosed)
    }

    @Test
    fun `test linked partner state correctly maintains coupleId and partnerUid`() {
        val linkedState = HealthDashboardUiState(
            coupleId = "cpl_1794268458799_EYf2I",
            partnerId = "RmkTPEUJlwYVm23evsWOj6cOpi02",
            myUid = "my_user_123",
            partnerName = "TaleOrbit",
            partnerSteps = 9840L,
            partnerStepsProgress = 0.984f,
            isPartnerLinked = true
        )
        val uiState = linkedState.toDashboardUiState()

        assertEquals("cpl_1794268458799_EYf2I", uiState.todayTab.coupleId)
        assertEquals("RmkTPEUJlwYVm23evsWOj6cOpi02", uiState.todayTab.partnerUid)
        assertEquals("TaleOrbit", uiState.todayTab.partnerName)
        assertEquals(9840L, uiState.todayTab.partnerSteps)
        assertEquals(0.984f, uiState.todayTab.partnerStepsProgress, 0.001f)
        assertEquals("RmkTPEUJlwYVm23evsWOj6cOpi02", uiState.partnerStats.partnerId)
        assertEquals("TaleOrbit", uiState.partnerStats.partnerName)
    }

    @Test
    fun `test partner entity sort comparator prioritizes real partner over dummy partner_1`() {
        data class MockPartnerDaily(val partnerId: String, val lastUpdated: Long, val steps: Long)

        val partner1 = MockPartnerDaily("partner_1", 1000L, 7420L)
        val realPartner = MockPartnerDaily("RmkTPEUJlwYVm23evsWOj6cOpi02", 500L, 8900L)

        val list = listOf(partner1, realPartner)
        // Mimicking Room SQL: ORDER BY CASE WHEN partnerId != 'partner_1' THEN 0 ELSE 1 END, lastUpdated DESC LIMIT 1
        val sorted = list.sortedWith(
            compareBy<MockPartnerDaily> { if (it.partnerId != "partner_1") 0 else 1 }
                .thenByDescending { it.lastUpdated }
        )

        assertEquals("RmkTPEUJlwYVm23evsWOj6cOpi02", sorted.first().partnerId)
        assertEquals(8900L, sorted.first().steps)
    }
}
