package com.couplefit.service

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.work.WorkerParameters
import com.couplefit.data.healthconnect.HealthConnectManager
import com.couplefit.data.local.CoupleFitPreferencesDataStore
import com.couplefit.data.local.HealthDao
import com.couplefit.data.model.UserSession
import com.couplefit.data.remote.FirebaseAuthManager
import com.couplefit.domain.repository.PartnerRepository
import com.couplefit.domain.repository.SyncRepository
import com.couplefit.domain.repository.SyncResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class BackgroundSyncWorkerTest {

    private lateinit var context: Context
    private lateinit var workerParams: WorkerParameters
    private lateinit var healthConnectManager: HealthConnectManager
    private lateinit var dataStore: CoupleFitPreferencesDataStore
    private lateinit var healthDao: HealthDao
    private lateinit var syncRepository: SyncRepository
    private lateinit var partnerRepository: PartnerRepository
    private lateinit var authManager: FirebaseAuthManager
    private lateinit var worker: SyncWorker

    @Before
    fun setUp() {
        context = mockk(relaxed = true)
        workerParams = mockk(relaxed = true)
        healthConnectManager = mockk(relaxed = true)
        dataStore = mockk(relaxed = true)
        healthDao = mockk(relaxed = true)
        syncRepository = mockk(relaxed = true)
        partnerRepository = mockk(relaxed = true)
        authManager = mockk(relaxed = true)

        worker = SyncWorker(
            appContext = context,
            workerParams = workerParams,
            healthConnectManager = healthConnectManager,
            dataStore = dataStore,
            healthDao = healthDao,
            syncRepository = syncRepository,
            partnerRepository = partnerRepository,
            authManager = authManager
        )
    }

    @Test
    fun `SyncWorker pulls Health Connect changes when SDK is available and permissions granted`() = runBlocking {
        every { healthConnectManager.checkAvailability() } returns HealthConnectClient.SDK_AVAILABLE
        coEvery { healthConnectManager.hasAnyPermissions() } returns true
        coEvery { healthConnectManager.syncViaChangesApi(dataStore, healthDao) } returns Result.success(mockk(relaxed = true))
        every { authManager.getCurrentSession() } returns UserSession(
            uid = "user_123",
            coupleId = "cpl_real",
            partnerId = "partner_456"
        )
        coEvery { syncRepository.uploadMyDailyAggregate(any(), any(), any(), any()) } returns SyncResult.Success(emptyMap())
        coEvery { partnerRepository.syncPartnerDailyFromRemote(any(), any(), any()) } returns Result.success(null)

        val result = worker.doWork()

        assertTrue(result is androidx.work.ListenableWorker.Result.Success)
        coVerify(exactly = 1) { healthConnectManager.syncViaChangesApi(dataStore, healthDao) }
    }

    @Test
    fun `SyncWorker executes bilateral upload and download when linked with real partner`() = runBlocking {
        val todayStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        every { healthConnectManager.checkAvailability() } returns HealthConnectClient.SDK_UNAVAILABLE
        every { authManager.getCurrentSession() } returns UserSession(
            uid = "user_abc",
            displayName = "User",
            email = "user@test.com",
            coupleId = "cpl_789",
            partnerId = "partner_xyz"
        )
        coEvery { syncRepository.uploadMyDailyAggregate("cpl_789", "user_abc", todayStr, force = true) } returns SyncResult.Success(emptyMap())
        coEvery { partnerRepository.syncPartnerDailyFromRemote("cpl_789", "partner_xyz", todayStr) } returns Result.success(null)

        val result = worker.doWork()

        assertTrue(result is androidx.work.ListenableWorker.Result.Success)
        coVerify(exactly = 1) {
            syncRepository.uploadMyDailyAggregate("cpl_789", "user_abc", todayStr, force = true)
        }
        coVerify(exactly = 1) {
            partnerRepository.syncPartnerDailyFromRemote("cpl_789", "partner_xyz", todayStr)
        }
    }

    @Test
    fun `SyncWorker skips remote sync when session is unlinked or dummy couple`() = runBlocking {
        every { healthConnectManager.checkAvailability() } returns HealthConnectClient.SDK_UNAVAILABLE
        every { authManager.getCurrentSession() } returns UserSession(
            uid = "user_guest",
            coupleId = "couple_1", // dummy/unlinked
            partnerId = "partner_1"
        )

        val result = worker.doWork()

        assertTrue(result is androidx.work.ListenableWorker.Result.Success)
        coVerify(exactly = 0) { syncRepository.uploadMyDailyAggregate(any(), any(), any(), any()) }
        coVerify(exactly = 0) { partnerRepository.syncPartnerDailyFromRemote(any(), any(), any()) }
    }
}
