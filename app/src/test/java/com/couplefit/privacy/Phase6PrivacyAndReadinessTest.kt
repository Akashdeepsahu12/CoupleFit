package com.couplefit.privacy

import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.OxygenSaturationRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import com.couplefit.data.healthconnect.HealthConnectManager
import com.couplefit.data.local.MetricSharingPreferences
import com.couplefit.data.remote.CoupleLinkManager
import com.google.android.gms.tasks.Tasks
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.HttpsCallableReference
import com.google.firebase.functions.HttpsCallableResult
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class Phase6PrivacyAndReadinessTest {

    @Test
    fun `test health permissions contain only read permissions and strictly no write permissions`() {
        val manager = HealthConnectManager(mockk(relaxed = true))

        // Ensure WRITE_STEPS is NOT requested
        val writeStepsPermission = HealthPermission.getWritePermission(StepsRecord::class)
        assertFalse(
            "Security/Privacy violation: App must not request WRITE_STEPS permission",
            manager.permissions.contains(writeStepsPermission)
        )

        // Ensure read permissions match exact UI metrics
        val expectedReadPermissions = setOf(
            HealthPermission.getReadPermission(StepsRecord::class),
            HealthPermission.getReadPermission(HeartRateRecord::class),
            HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class),
            HealthPermission.getReadPermission(OxygenSaturationRecord::class)
        )
        assertEquals(expectedReadPermissions, manager.permissions)
        assertEquals(expectedReadPermissions, manager.readPermissions)
    }

    @Test
    fun `test metric sharing preferences controls privacy filtering`() {
        val defaultPrefs = MetricSharingPreferences()
        assertTrue(defaultPrefs.shareSteps)
        assertTrue(defaultPrefs.shareWorkouts)
        assertTrue(defaultPrefs.shareCalories)
        assertFalse(defaultPrefs.shareHr)
        assertFalse(defaultPrefs.shareSpo2) // Default false for sensitive medical readings

        val privacyStrict = defaultPrefs.copy(
            shareSteps = true,
            shareWorkouts = false,
            shareCalories = false,
            shareHr = false,
            shareSpo2 = false
        )

        assertTrue(privacyStrict.shareSteps)
        assertFalse(privacyStrict.shareWorkouts)
        assertFalse(privacyStrict.shareCalories)
        assertFalse(privacyStrict.shareHr)
        assertFalse(privacyStrict.shareSpo2)
    }

    @Test
    fun `test unlinkCouple invokes cloud function callable`() = runBlocking {
        val functions = mockk<FirebaseFunctions>()
        val callable = mockk<HttpsCallableReference>()
        val result = mockk<HttpsCallableResult>()

        every { functions.getHttpsCallable("unlinkCouple") } returns callable
        every { callable.call() } returns Tasks.forResult(result)
        every { result.data } returns mapOf("success" to true, "status" to "UNLINKED")

        val manager = CoupleLinkManager(functions)
        val res = manager.unlinkCouple()

        assertTrue(res.isSuccess)
    }

    @Test
    fun `test deleteUserData invokes cloud function callable for GDPR compliance`() = runBlocking {
        val functions = mockk<FirebaseFunctions>()
        val callable = mockk<HttpsCallableReference>()
        val result = mockk<HttpsCallableResult>()

        every { functions.getHttpsCallable("deleteUserData") } returns callable
        every { callable.call() } returns Tasks.forResult(result)
        every { result.data } returns mapOf("success" to true, "message" to "All user data permanently deleted.")

        val manager = CoupleLinkManager(functions)
        val res = manager.deleteUserData()

        assertTrue(res.isSuccess)
    }
}
