package com.couplefit.data.remote

import com.couplefit.data.model.HealthSnapshot
import com.couplefit.data.model.SharingPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date

class FirebaseSyncManagerTest {

    private val manager = FirebaseSyncManager()

    @Test
    fun `test payload excludes steps when shareSteps is disabled`() {
        val prefs = SharingPreferences(
            userId = "user1",
            partnerId = "partner2",
            shareSteps = false,
            shareWorkouts = true,
            shareHR = true,
            shareSpO2 = true
        )

        val payload = manager.prepareDailyAggregatePayload(
            userId = "user1",
            dateStr = "2026-10-21",
            steps = 9500L,
            activeMinutes = 50L,
            calories = 400L,
            averageHeartRate = 72.0,
            averageOxygenSaturation = 98.0,
            prefs = prefs
        )

        assertFalse("Payload must NOT contain steps when shareSteps is false", payload.containsKey("steps"))
        assertTrue("Payload should contain activeMinutes when shareWorkouts is true", payload.containsKey("activeMinutes"))
        assertTrue("Payload should contain calories when shareWorkouts is true", payload.containsKey("calories"))
        assertTrue("Payload should contain averageHeartRate when shareHR is true", payload.containsKey("averageHeartRate"))
        assertTrue("Payload should contain averageOxygenSaturation when shareSpO2 is true", payload.containsKey("averageOxygenSaturation"))
    }

    @Test
    fun `test payload excludes vitals when shareHR and shareSpO2 are disabled`() {
        val prefs = SharingPreferences(
            userId = "user1",
            partnerId = "partner2",
            shareSteps = true,
            shareWorkouts = true,
            shareHR = false,
            shareSpO2 = false
        )

        val payload = manager.prepareDailyAggregatePayload(
            userId = "user1",
            dateStr = "2026-10-21",
            steps = 11000L,
            activeMinutes = 65L,
            calories = 520L,
            averageHeartRate = 75.0,
            averageOxygenSaturation = 99.0,
            prefs = prefs
        )

        assertTrue(payload.containsKey("steps"))
        assertTrue(payload.containsKey("activeMinutes"))
        assertFalse("Payload must NOT contain heart rate when shareHR is false", payload.containsKey("averageHeartRate"))
        assertFalse("Payload must NOT contain SpO2 when shareSpO2 is false", payload.containsKey("averageOxygenSaturation"))
    }

    @Test
    fun `test payload NEVER includes raw vital samples or points`() {
        val prefs = SharingPreferences(
            userId = "user1",
            partnerId = "partner2",
            shareSteps = true,
            shareWorkouts = true,
            shareHR = true,
            shareSpO2 = true
        )

        val payload = manager.prepareDailyAggregatePayload(
            userId = "user1",
            dateStr = "2026-10-21",
            steps = 8000L,
            activeMinutes = 30L,
            calories = 300L,
            averageHeartRate = 70.0,
            averageOxygenSaturation = 98.0,
            prefs = prefs
        )

        // Strict architectural rule: aggregates ONLY, never raw samples/arrays
        assertFalse(payload.containsKey("rawSamples"))
        assertFalse(payload.containsKey("heartRatePoints"))
        assertFalse(payload.containsKey("oxygenSaturationPoints"))
        assertFalse(payload.containsKey("samples"))
        assertTrue(payload["averageHeartRate"] is Double)
        assertTrue(payload["averageOxygenSaturation"] is Double)
    }

    @Test
    fun `test payload contains only metadata when all toggles are disabled`() {
        val strictPrefs = SharingPreferences(
            userId = "user1",
            partnerId = "partner2",
            shareSteps = false,
            shareWorkouts = false,
            shareSleep = false,
            shareHR = false,
            shareSpO2 = false
        )

        val payload = manager.prepareDailyAggregatePayload(
            userId = "user1",
            dateStr = "2026-10-21",
            steps = 12000L,
            activeMinutes = 90L,
            calories = 700L,
            averageHeartRate = 80.0,
            averageOxygenSaturation = 97.0,
            prefs = strictPrefs
        )

        assertEquals("user1", payload["userId"])
        assertEquals("2026-10-21", payload["dateStr"])
        assertNotNull(payload["updatedAt"])
        assertFalse(payload.containsKey("steps"))
        assertFalse(payload.containsKey("activeMinutes"))
        assertFalse(payload.containsKey("calories"))
        assertFalse(payload.containsKey("averageHeartRate"))
        assertFalse(payload.containsKey("averageOxygenSaturation"))
    }

    @Test
    fun `test syncHealthSnapshot honors SharingPreferences without throwing`() {
        val snapshot = HealthSnapshot(
            userId = "user123",
            date = Date(),
            steps = 5000,
            distanceMeters = 4000.0,
            activeCalories = 300.0,
            activeMinutes = 30,
            averageHeartRate = 71.0,
            averageOxygenSaturation = 98.0
        )

        val strictPrefs = SharingPreferences(
            userId = "user123",
            partnerId = "partner456",
            shareSteps = false,
            shareWorkouts = false,
            shareHR = false,
            shareSpO2 = false
        )
        manager.syncHealthSnapshot(snapshot, strictPrefs)

        val normalPrefs = SharingPreferences(
            userId = "user123",
            partnerId = "partner456",
            shareSteps = true,
            shareWorkouts = true,
            shareHR = true,
            shareSpO2 = true
        )
        manager.syncHealthSnapshot(snapshot, normalPrefs)
    }
}
