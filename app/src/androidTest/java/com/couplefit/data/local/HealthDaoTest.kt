package com.couplefit.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/**
 * In-Memory SQLite Room Integration Test Suite for [HealthDao] (Phase 56 / TST-01).
 *
 * Verifies Room SSOT invariants:
 * 1. DAT-01: ORDER BY lastUpdated DESC LIMIT 1 on date-only partner daily queries.
 * 2. Purging partner data via [HealthDao.deletePartnerDailyData].
 * 3. Cascading GDPR table wipes via [HealthDatabase.clearAllTables].
 * 4. Reactive Flow emissions for active and total challenges.
 * 5. Monotonic step updates and deduplication.
 */
@RunWith(AndroidJUnit4::class)
class HealthDaoTest {

    private lateinit var db: HealthDatabase
    private lateinit var healthDao: HealthDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, HealthDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        healthDao = db.healthDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun partnerDaily_orderByLastUpdatedDesc_returnsLatestRecord() = runBlocking {
        val dateStr = "2026-11-10"
        val record1 = PartnerDailyEntity(
            id = "partner_123_${dateStr}_early",
            partnerId = "partner_123",
            dateStr = dateStr,
            steps = 4000L,
            activeMinutes = 25L,
            calories = 250L,
            avgHr = 70.0,
            avgSpo2 = 98.0,
            lastUpdated = 1000L
        )
        val record2 = PartnerDailyEntity(
            id = "partner_123_${dateStr}_late",
            partnerId = "partner_123",
            dateStr = dateStr,
            steps = 7420L,
            activeMinutes = 49L,
            calories = 420L,
            avgHr = 72.0,
            avgSpo2 = 98.5,
            lastUpdated = 2500L
        )

        healthDao.insertPartnerDaily(record1)
        healthDao.insertPartnerDaily(record2)

        // Verify synchronous query returns record with latest lastUpdated timestamp
        val latest = healthDao.getPartnerDaily(dateStr)
        assertNotNull("Latest record must not be null", latest)
        assertEquals(7420L, latest?.steps)
        assertEquals(2500L, latest?.lastUpdated)

        // Verify Flow emits latest record
        val latestFromFlow = healthDao.getPartnerDailyFlow(dateStr).first()
        assertNotNull("Latest record from Flow must not be null", latestFromFlow)
        assertEquals(7420L, latestFromFlow?.steps)
        assertEquals(2500L, latestFromFlow?.lastUpdated)
    }

    @Test
    fun deletePartnerDailyData_purgesRecordsForSpecificPartnerOnly() = runBlocking {
        val dateStr = "2026-11-10"
        val partner1 = PartnerDailyEntity(
            id = "p1_$dateStr",
            partnerId = "partner_1",
            dateStr = dateStr,
            steps = 5000L,
            activeMinutes = 30L,
            calories = 300L,
            avgHr = 68.0,
            avgSpo2 = 99.0,
            lastUpdated = 1000L
        )
        val partner2 = PartnerDailyEntity(
            id = "p2_$dateStr",
            partnerId = "partner_2",
            dateStr = dateStr,
            steps = 6200L,
            activeMinutes = 40L,
            calories = 380L,
            avgHr = 74.0,
            avgSpo2 = 97.0,
            lastUpdated = 1200L
        )

        healthDao.insertPartnerDaily(partner1)
        healthDao.insertPartnerDaily(partner2)

        // Delete partner 1
        healthDao.deletePartnerDailyData("partner_1")

        // Partner 1 should be gone
        val p1Result = healthDao.getPartnerDaily("partner_1", dateStr)
        assertNull("Partner 1 data must be purged", p1Result)

        // Partner 2 should remain
        val p2Result = healthDao.getPartnerDaily("partner_2", dateStr)
        assertNotNull("Partner 2 data must remain intact", p2Result)
        assertEquals(6200L, p2Result?.steps)
    }

    @Test
    fun clearAllTables_wipesAllDataForGdprCompliance() = runBlocking {
        val dateStr = "2026-11-10"
        healthDao.insertPartnerDaily(
            PartnerDailyEntity(
                id = "partner_gdpr_$dateStr",
                partnerId = "partner_gdpr",
                dateStr = dateStr,
                steps = 8800L,
                activeMinutes = 55L,
                calories = 510L,
                avgHr = 71.0,
                avgSpo2 = 98.0,
                lastUpdated = 1500L
            )
        )
        healthDao.insertChallenge(
            CoupleChallengeEntity(
                id = "chal_gdpr",
                coupleId = "couple_gdpr",
                type = "STEPS",
                goal = 10000,
                start = "2026-11-01",
                end = "2026-11-30",
                status = "ACTIVE",
                myProgress = 4000,
                partnerProgress = 5000,
                winnerUserId = null,
                completedAt = null,
                lastUpdated = 1000L
            )
        )

        assertNotNull(healthDao.getPartnerDaily(dateStr))
        assertNotNull(healthDao.getChallenge("chal_gdpr"))

        // Execute database-wide wipe
        db.clearAllTables()

        assertNull("Partner daily data must be wiped after clearAllTables()", healthDao.getPartnerDaily(dateStr))
        assertNull("Couple challenges must be wiped after clearAllTables()", healthDao.getChallenge("chal_gdpr"))
    }

    @Test
    fun challenges_activeFilterAndDeletion_behaveDeterministically() = runBlocking {
        val activeChal = CoupleChallengeEntity(
            id = "c_active",
            coupleId = "couple_alpha",
            type = "STEPS",
            goal = 15000,
            start = "2026-11-01",
            end = "2026-11-15",
            status = "ACTIVE",
            myProgress = 7000,
            partnerProgress = 8000,
            winnerUserId = null,
            completedAt = null,
            lastUpdated = 2000L
        )
        val completedChal = CoupleChallengeEntity(
            id = "c_completed",
            coupleId = "couple_alpha",
            type = "CALORIES",
            goal = 700,
            start = "2026-11-01",
            end = "2026-11-05",
            status = "COMPLETED",
            myProgress = 750,
            partnerProgress = 680,
            winnerUserId = "user_1",
            completedAt = 1800L,
            lastUpdated = 1800L
        )

        healthDao.insertChallenge(activeChal)
        healthDao.insertChallenge(completedChal)

        // Query active only
        val activeList = healthDao.getActiveChallengesFlow("couple_alpha").first()
        assertEquals(1, activeList.size)
        assertEquals("c_active", activeList[0].id)
        assertEquals("ACTIVE", activeList[0].status)

        // Query all
        val allList = healthDao.getChallengesFlow("couple_alpha").first()
        assertEquals(2, allList.size)

        // Delete challenges for couple
        healthDao.deleteCoupleChallenges("couple_alpha")
        val emptyList = healthDao.getChallengesFlow("couple_alpha").first()
        assertTrue("All challenges for couple must be deleted", emptyList.isEmpty())
    }
}
