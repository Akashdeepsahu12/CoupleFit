package com.couplefit.ui

import com.couplefit.ui.dashboard.DashboardUiState
import com.couplefit.ui.dashboard.toDashboardUiState
import com.couplefit.ui.healthconnect.HealthDashboardUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DashboardUiStateTest {

    @Test
    fun `test DashboardUiState mapping from HealthDashboardUiState`() {
        val healthState = HealthDashboardUiState(
            userName = "Alex",
            steps = 11000L,
            stepsGoal = 10000L,
            stepsProgress = 1.1f,
            activeMinutes = 55L,
            caloriesBurned = 520L,
            partnerId = "partner_99",
            partnerName = "Taylor",
            partnerSteps = 10500L,
            partnerStepsProgress = 1.05f,
            partnerActiveMinutes = 60L,
            partnerCaloriesBurned = 490L,
            coupleId = "couple_42"
        )

        val uiState: DashboardUiState = healthState.toDashboardUiState()

        // Tab 0 Today Tab state
        assertEquals("Alex", uiState.todayTab.userName)
        assertEquals(11000L, uiState.todayTab.userSteps)
        assertEquals(1.1f, uiState.todayTab.userStepsProgress, 0.001f)
        assertEquals(55L, uiState.todayTab.userActiveMinutes)
        assertEquals(520L, uiState.todayTab.userCalories)

        assertEquals("Taylor", uiState.todayTab.partnerName)
        assertEquals(10500L, uiState.todayTab.partnerSteps)
        assertEquals(1.05f, uiState.todayTab.partnerStepsProgress, 0.001f)
        assertEquals(60L, uiState.todayTab.partnerActiveMinutes)
        assertEquals(490L, uiState.todayTab.partnerCalories)
        assertEquals("couple_42", uiState.todayTab.coupleId)
        assertEquals("partner_99", uiState.todayTab.partnerUid)

        // Q11: Both rings closed harmonic celebration contract
        assertTrue(uiState.todayTab.bothRingsClosed)

        // Tab 1 My Stats state
        assertEquals(11000L, uiState.myStats.steps)
        assertEquals(10000L, uiState.myStats.stepsGoal)
        assertEquals(1.1f, uiState.myStats.stepsProgress, 0.001f)
        assertEquals(55L, uiState.myStats.activeMinutes)
        assertEquals(520L, uiState.myStats.caloriesBurned)

        // Tab 2 Partner Stats state
        assertEquals("Taylor", uiState.partnerStats.partnerName)
        assertEquals(10500L, uiState.partnerStats.steps)
        assertEquals(1.05f, uiState.partnerStats.stepsProgress, 0.001f)
        assertEquals(60L, uiState.partnerStats.activeMinutes)
        assertEquals(490L, uiState.partnerStats.caloriesBurned)
    }

    @Test
    fun `test bothRingsClosed is false when only one partner reaches goal`() {
        // Only user closed
        val userOnly = HealthDashboardUiState(
            stepsProgress = 1.0f,
            partnerStepsProgress = 0.85f
        ).toDashboardUiState()
        assertFalse(userOnly.todayTab.bothRingsClosed)

        // Only partner closed
        val partnerOnly = HealthDashboardUiState(
            stepsProgress = 0.9f,
            partnerStepsProgress = 1.0f
        ).toDashboardUiState()
        assertFalse(partnerOnly.todayTab.bothRingsClosed)

        // Both closed
        val bothClosed = HealthDashboardUiState(
            stepsProgress = 1.0f,
            partnerStepsProgress = 1.0f
        ).toDashboardUiState()
        assertTrue(bothClosed.todayTab.bothRingsClosed)
    }
}
