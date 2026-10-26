package com.couplefit.data.repository

import com.couplefit.data.local.CoupleFitPreferencesDataStore
import com.couplefit.data.local.HealthDailyAggregateEntity
import com.couplefit.data.local.HealthDao
import com.couplefit.data.local.MetricSharingPreferences
import com.couplefit.domain.repository.SyncResult
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SyncRepositoryTest {

    private lateinit var healthDao: HealthDao
    private lateinit var dataStore: CoupleFitPreferencesDataStore
    private lateinit var firestore: FirebaseFirestore
    private lateinit var syncRepository: SyncRepositoryImpl

    private val mockCouplesCol = mockk<CollectionReference>(relaxed = true)
    private val mockCoupleDoc = mockk<DocumentReference>(relaxed = true)
    private val mockDailyCol = mockk<CollectionReference>(relaxed = true)
    private val mockDocRef = mockk<DocumentReference>(relaxed = true)

    @Before
    fun setUp() {
        healthDao = mockk(relaxed = true)
        dataStore = mockk(relaxed = true)
        firestore = mockk(relaxed = true)
        syncRepository = SyncRepositoryImpl(healthDao, dataStore, firestore)

        every { firestore.collection("couples") } returns mockCouplesCol
        every { mockCouplesCol.document(any()) } returns mockCoupleDoc
        every { mockCoupleDoc.collection("daily") } returns mockDailyCol
        every { mockDailyCol.document(any()) } returns mockDocRef

        val successTask = Tasks.forResult<Void>(null)
        every { mockDocRef.set(any(), any<SetOptions>()) } returns successTask
    }

    @Test
    fun `uploadMyDailyAggregate returns Throttled when last sync was less than 15 minutes ago`() = runTest {
        val now = System.currentTimeMillis()
        val recentSync = now - (5 * 60 * 1000L) // 5 minutes ago (< 15 min)
        coEvery { dataStore.getLastSyncTimestamp() } returns recentSync

        val result = syncRepository.uploadMyDailyAggregate(
            coupleId = "couple_101",
            myUid = "user_me",
            dateStr = "2026-10-21",
            force = false
        )

        assertEquals(SyncResult.Throttled, result)
        // Verify no Firestore upload occurred
        coVerify(exactly = 0) { mockDocRef.set(any(), any<SetOptions>()) }
    }

    @Test
    fun `uploadMyDailyAggregate executes upload when force is true even if within 15 minutes`() = runTest {
        val now = System.currentTimeMillis()
        val recentSync = now - (2 * 60 * 1000L) // 2 minutes ago
        coEvery { dataStore.getLastSyncTimestamp() } returns recentSync

        coEvery { healthDao.getDailyAggregate("2026-10-21") } returns HealthDailyAggregateEntity(
            dateStr = "2026-10-21",
            totalSteps = 6000L,
            totalActiveMinutes = 30L,
            totalCalories = 250L,
            averageHeartRate = 72.0,
            averageOxygenSaturation = 98.0,
            lastUpdatedTimestamp = now
        )
        coEvery { dataStore.getSharingPreferences() } returns MetricSharingPreferences()

        val result = syncRepository.uploadMyDailyAggregate(
            coupleId = "couple_101",
            myUid = "user_me",
            dateStr = "2026-10-21",
            force = true
        )

        assertTrue(result is SyncResult.Success)
        coVerify(exactly = 1) { mockDocRef.set(any(), any<SetOptions>()) }
        coVerify(exactly = 1) { dataStore.updateLastSyncTimestamp(any()) }
    }

    @Test
    fun `uploadMyDailyAggregate strictly filters metrics according to per-metric sharing toggles`() = runTest {
        val now = System.currentTimeMillis()
        val oldSync = now - (20 * 60 * 1000L) // 20 minutes ago (> 15 min)
        coEvery { dataStore.getLastSyncTimestamp() } returns oldSync

        coEvery { healthDao.getDailyAggregate("2026-10-21") } returns HealthDailyAggregateEntity(
            dateStr = "2026-10-21",
            totalSteps = 8500L,
            totalActiveMinutes = 45L,
            totalCalories = 400L,
            averageHeartRate = 76.5,
            averageOxygenSaturation = 97.8,
            lastUpdatedTimestamp = now
        )

        // Custom sharing preferences: ONLY share steps and workouts; HR and SpO2 disabled
        coEvery { dataStore.getSharingPreferences() } returns MetricSharingPreferences(
            shareSteps = true,
            shareWorkouts = true,
            shareCalories = true,
            shareHr = false,  // DISABLED
            shareSpo2 = false // DISABLED
        )

        val payloadSlot = slot<Map<String, Any>>()
        every { mockDocRef.set(capture(payloadSlot), any<SetOptions>()) } returns Tasks.forResult(null)

        val result = syncRepository.uploadMyDailyAggregate(
            coupleId = "couple_xyz",
            myUid = "user_abc",
            dateStr = "2026-10-21",
            force = false
        )

        assertTrue(result is SyncResult.Success)
        val payload = payloadSlot.captured

        assertEquals("user_abc", payload["userId"])
        assertEquals("2026-10-21", payload["dateStr"])
        assertEquals(8500L, payload["steps"])
        assertEquals(45L, payload["activeMinutes"])
        assertEquals(400L, payload["calories"])

        // Strict verification: avgHr and avgSpo2 must NOT be in payload
        assertFalse("Heart rate must not be uploaded when shareHr is false", payload.containsKey("avgHr"))
        assertFalse("SpO2 must not be uploaded when shareSpo2 is false", payload.containsKey("avgSpo2"))

        // Verifies target path couples/couple_xyz/daily/user_abc_2026-10-21
        coVerify { mockCouplesCol.document("couple_xyz") }
        coVerify { mockDailyCol.document("user_abc_2026-10-21") }
    }

    @Test
    fun `syncOnAppForeground calls upload with force flag true`() = runTest {
        val now = System.currentTimeMillis()
        val recentSync = now - (1 * 60 * 1000L) // 1 minute ago
        coEvery { dataStore.getLastSyncTimestamp() } returns recentSync

        coEvery { healthDao.getDailyAggregate("2026-10-21") } returns HealthDailyAggregateEntity(
            dateStr = "2026-10-21",
            totalSteps = 3000L,
            totalActiveMinutes = 15L,
            totalCalories = 120L,
            averageHeartRate = 70.0,
            averageOxygenSaturation = 98.0,
            lastUpdatedTimestamp = now
        )
        coEvery { dataStore.getSharingPreferences() } returns MetricSharingPreferences()

        val result = syncRepository.syncOnAppForeground(
            coupleId = "couple_101",
            myUid = "user_me",
            dateStr = "2026-10-21"
        )

        assertTrue("Foreground sync must bypass 15-minute throttle", result is SyncResult.Success)
        coVerify(exactly = 1) { mockDocRef.set(any(), any<SetOptions>()) }
    }
}
