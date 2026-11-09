package com.couplefit.data.repository

import app.cash.turbine.test
import com.couplefit.data.local.HealthDailyAggregateEntity
import com.couplefit.data.local.HealthDao
import com.couplefit.data.local.PartnerDailyEntity
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PartnerRepositoryTest {

    private lateinit var healthDao: HealthDao
    private lateinit var firestore: FirebaseFirestore
    private lateinit var partnerRepository: PartnerRepositoryImpl

    @Before
    fun setUp() {
        healthDao = mockk(relaxed = true)
        firestore = mockk(relaxed = true)
        partnerRepository = PartnerRepositoryImpl(healthDao, firestore)
    }

    @Test
    fun `getPartnerDailyFlow emits cached data from Room for offline-first display`() = runTest {
        val partnerId = "partner_789"
        val dateStr = "2026-10-21"
        val cachedEntity = PartnerDailyEntity(
            id = "${partnerId}_$dateStr",
            partnerId = partnerId,
            dateStr = dateStr,
            steps = 8450L,
            activeMinutes = 45L,
            calories = 420L,
            avgHr = 72.5,
            avgSpo2 = 98.2,
            lastUpdated = 1729500000000L
        )

        every { healthDao.getPartnerDailyFlow(partnerId, dateStr) } returns flowOf(cachedEntity)

        partnerRepository.getPartnerDailyFlow(partnerId, dateStr).test {
            val emission = awaitItem()
            assertNotNull(emission)
            assertEquals(8450L, emission?.steps)
            assertEquals(45L, emission?.activeMinutes)
            assertEquals(420L, emission?.calories)
            assertEquals(72.5, emission?.avgHr ?: 0.0, 0.001)
            assertEquals(98.2, emission?.avgSpo2 ?: 0.0, 0.001)
            awaitComplete()
        }
    }

    @Test
    fun `getCombinedDashboardFlow combines user flow and partner flow from Room`() = runTest {
        val dateStr = "2026-10-21"
        val partnerId = "partner_123"

        val myAggregate = HealthDailyAggregateEntity(
            dateStr = dateStr,
            totalSteps = 10500L,
            totalActiveMinutes = 60L,
            totalCalories = 550L,
            averageHeartRate = 68.0,
            averageOxygenSaturation = 97.5,
            lastUpdatedTimestamp = 1729500000000L
        )
        val partnerEntity = PartnerDailyEntity(
            id = "${partnerId}_$dateStr",
            partnerId = partnerId,
            dateStr = dateStr,
            steps = 9200L,
            activeMinutes = 50L,
            calories = 460L,
            avgHr = 71.0,
            avgSpo2 = 98.0,
            lastUpdated = 1729500000000L
        )

        every { healthDao.getDailyAggregateFlow(dateStr) } returns flowOf(myAggregate)
        every { healthDao.getPartnerDailyFlow(partnerId, dateStr) } returns flowOf(partnerEntity)

        partnerRepository.getCombinedDashboardFlow(partnerId, dateStr).test {
            val combined = awaitItem()
            assertEquals(10500L, combined.mySteps)
            assertEquals(9200L, combined.partnerSteps)
            assertEquals(1300L, combined.stepsDifference) // 10500 - 9200
            assertEquals(60L, combined.myActiveMinutes)
            assertEquals(50L, combined.partnerActiveMinutes)
            assertEquals(68.0, combined.myAvgHr, 0.001)
            assertEquals(71.0, combined.partnerAvgHr, 0.001)
            awaitComplete()
        }
    }

    @Test
    fun `listenToPartnerDaily attaches snapshot listener and inserts into Room on update`() = runTest {
        val coupleId = "couple_456"
        val partnerUid = "partner_456"
        val dateStr = "2026-10-21"
        val docId = "${partnerUid}_$dateStr"

        val mockCouplesCol = mockk<CollectionReference>(relaxed = true)
        val mockCoupleDoc = mockk<DocumentReference>(relaxed = true)
        val mockDailyCol = mockk<CollectionReference>(relaxed = true)
        val mockDocRef = mockk<DocumentReference>(relaxed = true)
        val mockRegistration = mockk<ListenerRegistration>(relaxed = true)

        every { firestore.collection("couples") } returns mockCouplesCol
        every { mockCouplesCol.document(coupleId) } returns mockCoupleDoc
        every { mockCoupleDoc.collection("daily") } returns mockDailyCol
        every { mockDailyCol.document(docId) } returns mockDocRef

        val listenerSlot = slot<EventListener<DocumentSnapshot>>()
        every { mockDocRef.addSnapshotListener(capture(listenerSlot)) } returns mockRegistration

        val cachedEntity = PartnerDailyEntity(
            id = docId,
            partnerId = partnerUid,
            dateStr = dateStr,
            steps = 7500L,
            activeMinutes = 35L,
            calories = 300L,
            avgHr = 75.0,
            avgSpo2 = 99.0,
            lastUpdated = 1729500000000L
        )
        every { healthDao.getPartnerDailyFlow(partnerUid, dateStr) } returns flowOf(cachedEntity)

        partnerRepository.listenToPartnerDaily(coupleId, partnerUid, dateStr).test {
            // Room emits initial cached data
            val initial = awaitItem()
            assertEquals(7500L, initial?.steps)

            // Simulate incoming Firestore snapshot
            val snapshot = mockk<DocumentSnapshot>()
            every { snapshot.exists() } returns true
            every { snapshot.data } returns mapOf(
                "steps" to 8200L,
                "activeMinutes" to 42L,
                "calories" to 380L,
                "avgHr" to 73.5,
                "avgSpo2" to 98.8,
                "lastUpdated" to 1729501000000L
            )

            listenerSlot.captured.onEvent(snapshot, null)

            // Verify written into Room (Single Source of Truth)
            coVerify(timeout = 1000) {
                healthDao.insertPartnerDaily(
                    match {
                        it.steps == 8200L &&
                        it.activeMinutes == 42L &&
                        it.calories == 380L &&
                        it.avgHr == 73.5 &&
                        it.avgSpo2 == 98.8
                    }
                )
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `clearPartnerDaily invokes DAO deletion`() = runTest {
        val partnerId = "partner_to_clear"
        partnerRepository.clearPartnerDaily(partnerId)
        coVerify(exactly = 1) { healthDao.deletePartnerDailyData(partnerId) }
    }

    @Test
    fun `seedDemoPartnerDaily inserts initial entity when Room is empty`() = runTest {
        val partnerId = "partner_demo"
        val dateStr = "2026-10-21"

        coEvery { healthDao.getPartnerDaily(partnerId, dateStr) } returns null

        partnerRepository.seedDemoPartnerDaily(partnerId, dateStr, defaultSteps = 7500L)

        coVerify(exactly = 1) {
            healthDao.insertPartnerDaily(
                match {
                    it.partnerId == partnerId &&
                    it.dateStr == dateStr &&
                    it.steps == 7500L &&
                    it.activeMinutes > 0L &&
                    it.calories > 0L
                }
            )
        }
    }

    @Test
    fun `seedDemoPartnerDaily does not overwrite when Room already has active data`() = runTest {
        val partnerId = "partner_active"
        val dateStr = "2026-10-21"
        val existing = PartnerDailyEntity(
            id = "${partnerId}_$dateStr",
            partnerId = partnerId,
            dateStr = dateStr,
            steps = 9800L,
            activeMinutes = 65L,
            calories = 490L,
            avgHr = 74.0,
            avgSpo2 = 98.0,
            lastUpdated = 1729500000000L
        )

        coEvery { healthDao.getPartnerDaily(partnerId, dateStr) } returns existing

        partnerRepository.seedDemoPartnerDaily(partnerId, dateStr, defaultSteps = 7500L)

        coVerify(exactly = 0) {
            healthDao.insertPartnerDaily(any())
        }
    }

    @Test
    fun `addPartnerSteps increments partner steps and recalculates metrics`() = runTest {
        val partnerId = "partner_123"
        val dateStr = "2026-10-21"
        val existing = PartnerDailyEntity(
            id = "${partnerId}_$dateStr",
            partnerId = partnerId,
            dateStr = dateStr,
            steps = 4000L,
            activeMinutes = 25L,
            calories = 160L,
            avgHr = 72.0,
            avgSpo2 = 98.5,
            lastUpdated = 1729500000000L
        )

        coEvery { healthDao.getPartnerDaily(partnerId, dateStr) } returns existing

        partnerRepository.addPartnerSteps(partnerId, dateStr, count = 1500L)

        coVerify(exactly = 1) {
            healthDao.insertPartnerDaily(
                match {
                    it.partnerId == partnerId &&
                    it.dateStr == dateStr &&
                    it.steps == 5500L && // 4000 + 1500
                    it.activeMinutes >= 25L &&
                    it.calories >= 160L
                }
            )
        }
    }

    @Test
    fun `sendNudge creates document in couples nudges collection with PENDING status`() = runTest {
        val coupleId = "cpl_123"
        val senderUid = "user_me"
        val partnerUid = "user_partner"

        val mockCouplesCol = mockk<CollectionReference>(relaxed = true)
        val mockCoupleDoc = mockk<DocumentReference>(relaxed = true)
        val mockNudgesCol = mockk<CollectionReference>(relaxed = true)
        val mockNudgeDoc = mockk<DocumentReference>(relaxed = true)
        val mockTask = com.google.android.gms.tasks.Tasks.forResult<Void>(null)

        every { firestore.collection("couples") } returns mockCouplesCol
        every { mockCouplesCol.document(coupleId) } returns mockCoupleDoc
        every { mockCoupleDoc.collection("nudges") } returns mockNudgesCol
        every { mockNudgesCol.document() } returns mockNudgeDoc
        every { mockNudgeDoc.id } returns "nudge_abc"
        every { mockNudgeDoc.set(any()) } returns mockTask

        val result = partnerRepository.sendNudge(
            coupleId = coupleId,
            senderUid = senderUid,
            senderName = "Alex",
            recipientUid = partnerUid,
            message = "Let's close our rings!"
        )

        assertTrue(result.isSuccess)
        assertEquals("nudge_abc", result.getOrNull())
        val slot = slot<Map<String, Any>>()
        io.mockk.verify(exactly = 1) {
            mockNudgeDoc.set(capture(slot))
        }

        val captured = slot.captured
        assertEquals(coupleId, captured["coupleId"])
        assertEquals(senderUid, captured["senderUid"])
        assertEquals(partnerUid, captured["recipientUid"])
        assertEquals("PENDING", captured["status"])
        assertEquals("Let's close our rings!", captured["message"])
    }
}
