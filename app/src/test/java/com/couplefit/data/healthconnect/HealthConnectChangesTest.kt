package com.couplefit.data.healthconnect

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.aggregate.AggregationResult
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ChangesTokenRequest
import androidx.health.connect.client.response.ChangesResponse
import androidx.health.connect.client.units.Energy
import com.couplefit.data.local.CoupleFitPreferencesDataStore
import com.couplefit.data.local.HealthDao
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class HealthConnectChangesTest {

    private lateinit var context: Context
    private lateinit var client: HealthConnectClient
    private lateinit var permissionController: PermissionController
    private lateinit var dataStore: CoupleFitPreferencesDataStore
    private lateinit var healthDao: HealthDao
    private lateinit var manager: HealthConnectManager

    @Before
    fun setUp() {
        context = mockk(relaxed = true)
        client = mockk(relaxed = true)
        permissionController = mockk(relaxed = true)
        dataStore = mockk(relaxed = true)
        healthDao = mockk(relaxed = true)

        every { client.permissionController } returns permissionController
        coEvery { permissionController.getGrantedPermissions() } returns setOf(
            androidx.health.connect.client.permission.HealthPermission.getReadPermission(StepsRecord::class),
            androidx.health.connect.client.permission.HealthPermission.getReadPermission(HeartRateRecord::class),
            androidx.health.connect.client.permission.HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class)
        )
        coEvery { client.readRecords(any<androidx.health.connect.client.request.ReadRecordsRequest<*>>()) } returns androidx.health.connect.client.response.ReadRecordsResponse(
            records = emptyList(),
            pageToken = null
        )

        manager = HealthConnectManager(context, client)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `syncViaChangesApi requests fresh token and runs aggregate deduplication on initial sync`() = runTest {
        // No stored token
        coEvery { dataStore.getChangesToken() } returns null
        coEvery { client.getChangesToken(any()) } returns "fresh_token_123"

        // Mock aggregate response for phone + watch deduplicated steps
        val mockAggResult = mockk<AggregationResult>(relaxed = true)
        every { mockAggResult.get<Long>(StepsRecord.COUNT_TOTAL) } returns 10250L
        every { mockAggResult.get<Long>(HeartRateRecord.BPM_AVG) } returns 70L
        every { mockAggResult.get<Energy>(TotalCaloriesBurnedRecord.ENERGY_TOTAL) } returns Energy.kilocalories(480.0)
        coEvery { client.aggregate(any()) } returns mockAggResult

        val result = manager.syncViaChangesApi(dataStore, healthDao)

        assertTrue(result.isSuccess)
        val data = result.getOrNull()
        assertEquals(10250L, data?.totalSteps)
        assertEquals(70.0, data?.averageHeartRate ?: 0.0, 0.001)

        // Verifies token saved and aggregate persisted to Room
        coVerify(exactly = 1) { dataStore.saveChangesToken("fresh_token_123") }
        coVerify(exactly = 1) {
            healthDao.insertAggregate(
                match { it.totalSteps == 10250L && it.totalCalories == 480L && it.averageHeartRate == 70.0 }
            )
        }
    }

    @Test
    fun `syncViaChangesApi handles token expiry with full resync fallback and acquires new token`() = runTest {
        // Stored token exists but is expired
        coEvery { dataStore.getChangesToken() } returns "expired_token_999"

        val expiredResponse = mockk<ChangesResponse>(relaxed = true)
        every { expiredResponse.changesTokenExpired } returns true
        coEvery { client.getChanges("expired_token_999") } returns expiredResponse

        // Fresh token returned on resync fallback
        coEvery { client.getChangesToken(any()) } returns "newly_minted_token_456"

        val mockAggResult = mockk<AggregationResult>(relaxed = true)
        every { mockAggResult.get<Long>(StepsRecord.COUNT_TOTAL) } returns 7500L
        every { mockAggResult.get<Long>(HeartRateRecord.BPM_AVG) } returns 75L
        every { mockAggResult.get<Energy>(TotalCaloriesBurnedRecord.ENERGY_TOTAL) } returns Energy.kilocalories(320.0)
        coEvery { client.aggregate(any()) } returns mockAggResult

        val result = manager.syncViaChangesApi(dataStore, healthDao)

        assertTrue("Must succeed through fallback resync", result.isSuccess)
        assertEquals(7500L, result.getOrNull()?.totalSteps)

        // Verifies fresh token saved after token expiry
        coVerify(exactly = 1) { dataStore.saveChangesToken("newly_minted_token_456") }
        coVerify(exactly = 1) {
            healthDao.insertAggregate(
                match { it.totalSteps == 7500L && it.totalCalories == 320L }
            )
        }
    }

    @Test
    fun `syncViaChangesApi queries StepsRecord COUNT_TOTAL for phone and watch step deduplication`() = runTest {
        coEvery { dataStore.getChangesToken() } returns null
        coEvery { client.getChangesToken(any()) } returns "token_abc"

        val mockAggResult = mockk<AggregationResult>(relaxed = true)
        every { mockAggResult.get<Long>(StepsRecord.COUNT_TOTAL) } returns 8800L
        every { mockAggResult.get<Long>(HeartRateRecord.BPM_AVG) } returns 68L
        every { mockAggResult.get<Energy>(TotalCaloriesBurnedRecord.ENERGY_TOTAL) } returns Energy.kilocalories(400.0)
        coEvery { client.aggregate(any()) } returns mockAggResult

        val result = manager.syncViaChangesApi(dataStore, healthDao)

        assertTrue(result.isSuccess)
        assertEquals(8800L, result.getOrNull()?.totalSteps)

        // Verify that client.aggregate was invoked and deduplicated steps were saved into Room
        coVerify(atLeast = 1) { client.aggregate(any()) }
        coVerify(exactly = 1) {
            healthDao.insertAggregate(
                match { it.totalSteps == 8800L }
            )
        }
    }
}
