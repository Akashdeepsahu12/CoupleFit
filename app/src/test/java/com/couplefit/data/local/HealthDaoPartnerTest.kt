package com.couplefit.data.local

import androidx.sqlite.db.SupportSQLiteDatabase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.reflect.Proxy

class HealthDaoPartnerTest {

    @Test
    fun `test PartnerDailyAggregateEntity creation and integrity`() {
        val partnerEntity = PartnerDailyAggregateEntity(
            id = "partner_123_2026-10-21",
            partnerId = "partner_123",
            dateStr = "2026-10-21",
            totalSteps = 8450L,
            totalActiveMinutes = 45L,
            totalCalories = 420L,
            averageHeartRate = 72.5,
            averageOxygenSaturation = 98.2,
            lastUpdatedTimestamp = 1729500000000L
        )

        assertEquals("partner_123_2026-10-21", partnerEntity.id)
        assertEquals("partner_123", partnerEntity.partnerId)
        assertEquals("2026-10-21", partnerEntity.dateStr)
        assertEquals(8450L, partnerEntity.totalSteps)
        assertEquals(45L, partnerEntity.totalActiveMinutes)
        assertEquals(420L, partnerEntity.totalCalories)
        assertEquals(72.5, partnerEntity.averageHeartRate, 0.001)
        assertEquals(98.2, partnerEntity.averageOxygenSaturation, 0.001)
        assertEquals(1729500000000L, partnerEntity.lastUpdatedTimestamp)
    }

    @Test
    fun `test HealthDailyAggregateEntity includes SpO2 aggregate`() {
        val userEntity = HealthDailyAggregateEntity(
            dateStr = "2026-10-21",
            totalSteps = 10500L,
            totalActiveMinutes = 60L,
            totalCalories = 550L,
            averageHeartRate = 68.0,
            averageOxygenSaturation = 97.5,
            lastUpdatedTimestamp = 1729500000000L
        )

        assertEquals("2026-10-21", userEntity.dateStr)
        assertEquals(10500L, userEntity.totalSteps)
        assertEquals(68.0, userEntity.averageHeartRate, 0.001)
        assertEquals(97.5, userEntity.averageOxygenSaturation, 0.001)
    }

    @Test
    fun `test Room Migration 1 to 2 executes required SQL schema updates`() {
        val executedSql = mutableListOf<String>()

        // Create dynamic proxy for SupportSQLiteDatabase to record SQL calls without Android mock dependencies
        val dbProxy = Proxy.newProxyInstance(
            SupportSQLiteDatabase::class.java.classLoader,
            arrayOf(SupportSQLiteDatabase::class.java)
        ) { _, method, args ->
            if (method.name == "execSQL" && args != null && args.isNotEmpty()) {
                executedSql.add(args[0] as String)
            }
            null
        } as SupportSQLiteDatabase

        HealthDatabase.MIGRATION_1_2.migrate(dbProxy)

        assertTrue(
            "Migration must alter health_daily_aggregates to add averageOxygenSaturation",
            executedSql.any { it.contains("ALTER TABLE health_daily_aggregates ADD COLUMN averageOxygenSaturation") }
        )
        assertTrue(
            "Migration must create partner_daily_aggregates table",
            executedSql.any { it.contains("CREATE TABLE IF NOT EXISTS partner_daily_aggregates") }
        )
    }

    @Test
    fun `test PartnerDailyEntity creation and integrity`() {
        val entity = PartnerDailyEntity(
            id = "partner_abc_2026-10-21",
            partnerId = "partner_abc",
            dateStr = "2026-10-21",
            steps = 9200L,
            activeMinutes = 55L,
            calories = 480L,
            avgHr = 74.0,
            avgSpo2 = 98.5,
            lastUpdated = 1729500000000L
        )

        assertEquals("partner_abc_2026-10-21", entity.id)
        assertEquals("partner_abc", entity.partnerId)
        assertEquals("2026-10-21", entity.dateStr)
        assertEquals(9200L, entity.steps)
        assertEquals(55L, entity.activeMinutes)
        assertEquals(480L, entity.calories)
        assertEquals(74.0, entity.avgHr, 0.001)
        assertEquals(98.5, entity.avgSpo2 ?: 0.0, 0.001)
        assertEquals(1729500000000L, entity.lastUpdated)
    }

    @Test
    fun `test Room Migration 2 to 3 creates partnerDaily table`() {
        val executedSql = mutableListOf<String>()

        val dbProxy = Proxy.newProxyInstance(
            SupportSQLiteDatabase::class.java.classLoader,
            arrayOf(SupportSQLiteDatabase::class.java)
        ) { _, method, args ->
            if (method.name == "execSQL" && args != null && args.isNotEmpty()) {
                executedSql.add(args[0] as String)
            }
            null
        } as SupportSQLiteDatabase

        HealthDatabase.MIGRATION_2_3.migrate(dbProxy)

        assertTrue(
            "Migration 2 to 3 must create partnerDaily table",
            executedSql.any { it.contains("CREATE TABLE IF NOT EXISTS partnerDaily") }
        )
    }

    @Test
    fun `test Room Migration 3 to 4 creates coupleChallenges table`() {
        val executedSql = mutableListOf<String>()

        val dbProxy = Proxy.newProxyInstance(
            SupportSQLiteDatabase::class.java.classLoader,
            arrayOf(SupportSQLiteDatabase::class.java)
        ) { _, method, args ->
            if (method.name == "execSQL" && args != null && args.isNotEmpty()) {
                executedSql.add(args[0] as String)
            }
            null
        } as SupportSQLiteDatabase

        HealthDatabase.MIGRATION_3_4.migrate(dbProxy)

        assertTrue(
            "Migration 3 to 4 must create coupleChallenges table",
            executedSql.any { it.contains("CREATE TABLE IF NOT EXISTS coupleChallenges") }
        )
    }

    @Test
    fun `test CoupleChallengeEntity creation and integrity`() {
        val challenge = CoupleChallengeEntity(
            id = "chal_999",
            coupleId = "couple_pair_1",
            type = "STEPS",
            goal = 12000,
            start = "2026-11-01",
            end = "2026-11-07",
            status = "ACTIVE",
            myProgress = 8500,
            partnerProgress = 9200,
            winnerUserId = null,
            completedAt = null,
            lastUpdated = 1731200000000L
        )

        assertEquals("chal_999", challenge.id)
        assertEquals("couple_pair_1", challenge.coupleId)
        assertEquals("STEPS", challenge.type)
        assertEquals(12000, challenge.goal)
        assertEquals("ACTIVE", challenge.status)
        assertEquals(8500, challenge.myProgress)
        assertEquals(9200, challenge.partnerProgress)
        assertEquals(1731200000000L, challenge.lastUpdated)
    }
}
