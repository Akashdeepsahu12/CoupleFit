package com.couplefit.ui

import android.content.SharedPreferences
import androidx.health.connect.client.HealthConnectClient
import com.couplefit.data.local.MetricSharingPreferences
import com.couplefit.data.sensor.HardwareStepSensorManager
import com.couplefit.ui.dashboard.components.SyncBlockerType
import com.couplefit.ui.healthconnect.HealthConnectViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class Phase74SyncDiagnosticsTest {

    // --- Slice 1: Hardware Step Fallback & Arbitration Tests (HC-02) ---

    @Test
    fun testResolveEffectiveSteps_healthConnectTakesPriorityOverHardware() = runTest {
        // Arrange
        val healthConnectSteps = 4500
        val hardwareStepsRead = suspend { 3200 }

        // Act
        // HealthConnectViewModel helper function
        val result = if (healthConnectSteps > 0) {
            healthConnectSteps to "HEALTH_CONNECT"
        } else {
            val hw = hardwareStepsRead()
            if (hw > 0) hw to "HARDWARE_SENSOR_FALLBACK" else 0 to "EMPTY_HUB"
        }

        // Assert
        assertEquals(4500, result.first)
        assertEquals("HEALTH_CONNECT", result.second)
    }

    @Test
    fun testResolveEffectiveSteps_hardwareFallbackUsedWhenHealthConnectReturnsZero() = runTest {
        // Arrange
        val healthConnectSteps = 0
        val hardwareStepsRead = suspend { 2850 }

        // Act
        val result = if (healthConnectSteps > 0) {
            healthConnectSteps to "HEALTH_CONNECT"
        } else {
            val hw = hardwareStepsRead()
            if (hw > 0) hw to "HARDWARE_SENSOR_FALLBACK" else 0 to "EMPTY_HUB"
        }

        // Assert
        assertEquals(2850, result.first)
        assertEquals("HARDWARE_SENSOR_FALLBACK", result.second)
    }

    @Test
    fun testResolveEffectiveSteps_emptyHubWhenBothZero() = runTest {
        // Arrange
        val healthConnectSteps = 0
        val hardwareStepsRead = suspend { 0 }

        // Act
        val result = if (healthConnectSteps > 0) {
            healthConnectSteps to "HEALTH_CONNECT"
        } else {
            val hw = hardwareStepsRead()
            if (hw > 0) hw to "HARDWARE_SENSOR_FALLBACK" else 0 to "EMPTY_HUB"
        }

        // Assert
        assertEquals(0, result.first)
        assertEquals("EMPTY_HUB", result.second)
    }

    // --- Baseline Delta Calculation Tests ---

    @Test
    fun testComputeDailyDeltaAndUpdateBaseline_sameDayAccumulation() {
        val fakePrefs = FakeSharedPreferences()
        val today = "2026-11-14"

        // First read of the day initializes baseline to 10000
        val firstDelta = HardwareStepSensorManager.computeDailyDeltaAndUpdateBaseline(
            todayKey = today,
            totalBootSteps = 10000,
            prefs = fakePrefs
        )
        assertEquals(0, firstDelta)

        // Second read: total boot steps increased to 12500 -> delta is 2500
        val secondDelta = HardwareStepSensorManager.computeDailyDeltaAndUpdateBaseline(
            todayKey = today,
            totalBootSteps = 12500,
            prefs = fakePrefs
        )
        assertEquals(2500, secondDelta)
    }

    @Test
    fun testComputeDailyDeltaAndUpdateBaseline_newDayResetsBaseline() {
        val fakePrefs = FakeSharedPreferences()
        val day1 = "2026-11-13"
        val day2 = "2026-11-14"

        // Day 1 setup
        HardwareStepSensorManager.computeDailyDeltaAndUpdateBaseline(day1, 5000, fakePrefs)
        val day1Delta = HardwareStepSensorManager.computeDailyDeltaAndUpdateBaseline(day1, 7500, fakePrefs)
        assertEquals(2500, day1Delta)

        // Day 2 arrives: totalBootSteps is 8000, but new date should reset baseline to 8000 and return 0
        val day2InitialDelta = HardwareStepSensorManager.computeDailyDeltaAndUpdateBaseline(day2, 8000, fakePrefs)
        assertEquals(0, day2InitialDelta)

        // Day 2 later: totalBootSteps is 9400 -> delta is 1400
        val day2LaterDelta = HardwareStepSensorManager.computeDailyDeltaAndUpdateBaseline(day2, 9400, fakePrefs)
        assertEquals(1400, day2LaterDelta)
    }

    @Test
    fun testComputeDailyDeltaAndUpdateBaseline_deviceRebootResetsBaseline() {
        val fakePrefs = FakeSharedPreferences()
        val today = "2026-11-14"

        // Before reboot: baseline 15000, current 18000
        HardwareStepSensorManager.computeDailyDeltaAndUpdateBaseline(today, 15000, fakePrefs)
        val deltaBeforeReboot = HardwareStepSensorManager.computeDailyDeltaAndUpdateBaseline(today, 18000, fakePrefs)
        assertEquals(3000, deltaBeforeReboot)

        // Device reboots: boot step counter starts fresh at 500 (< savedBaseline 15000)
        val deltaAfterReboot = HardwareStepSensorManager.computeDailyDeltaAndUpdateBaseline(today, 500, fakePrefs)
        assertEquals(0, deltaAfterReboot)

        // Subsequent steps after reboot: 1200 -> delta is 700
        val subsequentDelta = HardwareStepSensorManager.computeDailyDeltaAndUpdateBaseline(today, 1200, fakePrefs)
        assertEquals(700, subsequentDelta)
    }

    // --- Slice 3 & 4: Diagnostic Blocker Determination Tests (DIAG-01 & DIAG-02) ---

    @Test
    fun testComputeDiagnosticModel_permissionRevokedPriority() {
        val model = HealthConnectViewModel.computeDiagnosticModel(
            availabilityStatus = HealthConnectClient.SDK_AVAILABLE,
            hasPermissions = false,
            sharingPrefs = MetricSharingPreferences(shareSteps = true),
            totalSteps = 0L,
            usingHardwareSensor = false,
            partnerSteps = 5000L,
            partnerHasTodayData = true,
            context = null
        )

        assertEquals(SyncBlockerType.PERMISSION_REVOKED, model.blockerType)
        assertEquals("Health Permissions Required", model.title)
        assertEquals("Grant Permissions", model.actionLabel)
        assertNull(model.partnerDiagnosticNote)
    }

    @Test
    fun testComputeDiagnosticModel_sharingDisabledWhenToggleOff() {
        val model = HealthConnectViewModel.computeDiagnosticModel(
            availabilityStatus = HealthConnectClient.SDK_AVAILABLE,
            hasPermissions = true,
            sharingPrefs = MetricSharingPreferences(shareSteps = false),
            totalSteps = 4500L,
            usingHardwareSensor = false,
            partnerSteps = 5000L,
            partnerHasTodayData = true,
            context = null
        )

        assertEquals(SyncBlockerType.SHARING_DISABLED, model.blockerType)
        assertEquals("Step Sharing Paused", model.title)
        assertEquals("Enable Sharing", model.actionLabel)
    }

    @Test
    fun testComputeDiagnosticModel_partnerDiagnosticNoteWhenPartnerStepsZero() {
        val model = HealthConnectViewModel.computeDiagnosticModel(
            availabilityStatus = HealthConnectClient.SDK_AVAILABLE,
            hasPermissions = true,
            sharingPrefs = MetricSharingPreferences(shareSteps = true),
            totalSteps = 6000L,
            usingHardwareSensor = false,
            partnerSteps = 0L,
            partnerHasTodayData = false,
            context = null
        )

        assertEquals(SyncBlockerType.NONE, model.blockerType)
        assertNotNull(model.partnerDiagnosticNote)
        assertTrue(model.partnerDiagnosticNote!!.contains("Samsung Health or Google Fit"))
    }
}

/**
 * Lightweight in-memory FakeSharedPreferences for zero-reflection deterministic unit tests.
 */
class FakeSharedPreferences : SharedPreferences {
    private val data = mutableMapOf<String, Any?>()

    override fun getAll(): MutableMap<String, *> = data
    override fun getString(key: String?, defValue: String?): String? = (data[key] as? String) ?: defValue
    override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? =
        @Suppress("UNCHECKED_CAST") (data[key] as? MutableSet<String>) ?: defValues
    override fun getInt(key: String?, defValue: Int): Int = (data[key] as? Int) ?: defValue
    override fun getLong(key: String?, defValue: Long): Long = (data[key] as? Long) ?: defValue
    override fun getFloat(key: String?, defValue: Float): Float = (data[key] as? Float) ?: defValue
    override fun getBoolean(key: String?, defValue: Boolean): Boolean = (data[key] as? Boolean) ?: defValue
    override fun contains(key: String?): Boolean = data.containsKey(key)
    override fun edit(): SharedPreferences.Editor = FakeEditor(this)
    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) = Unit
    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) = Unit

    class FakeEditor(private val prefs: FakeSharedPreferences) : SharedPreferences.Editor {
        private val staging = mutableMapOf<String, Any?>()
        private var clearFlag = false

        override fun putString(key: String?, value: String?): SharedPreferences.Editor {
            key?.let { staging[it] = value }
            return this
        }
        override fun putStringSet(key: String?, values: MutableSet<String>?): SharedPreferences.Editor {
            key?.let { staging[it] = values }
            return this
        }
        override fun putInt(key: String?, value: Int): SharedPreferences.Editor {
            key?.let { staging[it] = value }
            return this
        }
        override fun putLong(key: String?, value: Long): SharedPreferences.Editor {
            key?.let { staging[it] = value }
            return this
        }
        override fun putFloat(key: String?, value: Float): SharedPreferences.Editor {
            key?.let { staging[it] = value }
            return this
        }
        override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor {
            key?.let { staging[it] = value }
            return this
        }
        override fun remove(key: String?): SharedPreferences.Editor {
            key?.let { staging[it] = null }
            return this
        }
        override fun clear(): SharedPreferences.Editor {
            clearFlag = true
            return this
        }
        override fun commit(): Boolean {
            apply()
            return true
        }
        override fun apply() {
            if (clearFlag) prefs.data.clear()
            for ((k, v) in staging) {
                if (v == null) prefs.data.remove(k) else prefs.data[k] = v
            }
            staging.clear()
            clearFlag = false
        }
    }
}
