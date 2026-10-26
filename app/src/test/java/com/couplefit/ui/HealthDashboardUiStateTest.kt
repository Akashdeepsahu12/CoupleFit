package com.couplefit.ui

import com.couplefit.ui.healthconnect.HealthDashboardUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HealthDashboardUiStateTest {

    @Test
    fun `test HealthDashboardUiState defaults for both user and partner`() {
        val state = HealthDashboardUiState()

        // User defaults
        assertEquals(0L, state.steps)
        assertEquals(10000L, state.stepsGoal)
        assertEquals(0f, state.stepsProgress, 0.001f)
        assertEquals(0L, state.activeMinutes)
        assertEquals(0.0, state.averageHeartRate, 0.001)

        // Partner defaults (Single source of truth)
        assertEquals("partner_1", state.partnerId)
        assertEquals("Partner", state.partnerName)
        assertTrue(state.isPartnerLinked)
        assertEquals(0L, state.partnerSteps)
        assertEquals(10000L, state.partnerStepsGoal)
        assertEquals(0f, state.partnerStepsProgress, 0.001f)
        assertEquals(0L, state.partnerActiveMinutes)
        assertEquals(0.0, state.partnerAverageHeartRate, 0.001)
        assertEquals(0.0, state.partnerAverageOxygenSaturation, 0.001)
        assertFalse(state.partnerHasTodayData)
        assertNull(state.nudgeSentMessage)
    }

    @Test
    fun `test partner state mutation respects unidirectional data flow`() {
        val initial = HealthDashboardUiState()

        val updated = initial.copy(
            partnerSteps = 8200L,
            partnerStepsProgress = 0.82f,
            partnerActiveMinutes = 45L,
            partnerCaloriesBurned = 390L,
            partnerAverageHeartRate = 72.0,
            partnerAverageOxygenSaturation = 98.5,
            partnerHasTodayData = true
        )

        assertEquals(8200L, updated.partnerSteps)
        assertEquals(0.82f, updated.partnerStepsProgress, 0.001f)
        assertEquals(45L, updated.partnerActiveMinutes)
        assertEquals(390L, updated.partnerCaloriesBurned)
        assertEquals(72.0, updated.partnerAverageHeartRate, 0.001)
        assertEquals(98.5, updated.partnerAverageOxygenSaturation, 0.001)
        assertTrue(updated.partnerHasTodayData)
    }

    @Test
    fun `test couple competition standings logic`() {
        // Case 1: User leading
        val userLeading = HealthDashboardUiState(steps = 9500L, partnerSteps = 7200L)
        val diff1 = userLeading.steps - userLeading.partnerSteps
        assertTrue(diff1 > 0)
        assertEquals(2300L, diff1)

        // Case 2: Partner leading
        val partnerLeading = HealthDashboardUiState(steps = 6000L, partnerSteps = 8500L)
        val diff2 = partnerLeading.steps - partnerLeading.partnerSteps
        assertTrue(diff2 < 0)
        assertEquals(-2500L, diff2)

        // Case 3: Tied
        val tied = HealthDashboardUiState(steps = 10000L, partnerSteps = 10000L)
        assertEquals(0L, tied.steps - tied.partnerSteps)
    }

    @Test
    fun `test nudge cheer message trigger`() {
        val state = HealthDashboardUiState()
        assertNull(state.nudgeSentMessage)

        val cheered = state.copy(nudgeSentMessage = "Sent a cheer to Partner!")
        assertEquals("Sent a cheer to Partner!", cheered.nudgeSentMessage)

        val dismissed = cheered.copy(nudgeSentMessage = null)
        assertNull(dismissed.nudgeSentMessage)
    }
}
