package com.couplefit.data.repository

import com.couplefit.data.local.HealthDailyAggregateEntity
import com.couplefit.data.local.HealthDao
import com.couplefit.data.local.HealthMetricEntity
import com.couplefit.data.local.PartnerDailyAggregateEntity
import com.couplefit.data.model.SharingPreferences
import com.couplefit.data.remote.FirebaseSyncManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HealthRepositoryImplTest {

    private class FakeHealthDao : HealthDao {
        val userAggregates = mutableMapOf<String, HealthDailyAggregateEntity>()
        val partnerAggregates = mutableMapOf<String, PartnerDailyAggregateEntity>()

        override suspend fun insertMetrics(metrics: List<HealthMetricEntity>) {}
        override suspend fun insertAggregate(aggregate: HealthDailyAggregateEntity) {
            userAggregates[aggregate.dateStr] = aggregate
        }
        override suspend fun metricExists(id: String): Boolean = false
        override fun getMetricsFlow(type: String, startTime: Long, endTime: Long): Flow<List<HealthMetricEntity>> = flowOf(emptyList())
        override suspend fun getMetrics(type: String, startTime: Long, endTime: Long): List<HealthMetricEntity> = emptyList()

        override fun getDailyAggregateFlow(dateStr: String): Flow<HealthDailyAggregateEntity?> {
            return flowOf(userAggregates[dateStr])
        }

        override suspend fun getDailyAggregate(dateStr: String): HealthDailyAggregateEntity? {
            return userAggregates[dateStr]
        }

        override fun getDailyAggregatesBetweenFlow(startDate: String, endDate: String): Flow<List<HealthDailyAggregateEntity>> {
            return flowOf(userAggregates.values.filter { it.dateStr in startDate..endDate })
        }

        override suspend fun getDailyAggregatesBetween(startDate: String, endDate: String): List<HealthDailyAggregateEntity> {
            return userAggregates.values.filter { it.dateStr in startDate..endDate }
        }

        override suspend fun markAsSynced(ids: List<String>) {}
        override suspend fun getUnsyncedMetrics(): List<HealthMetricEntity> = emptyList()

        override suspend fun insertPartnerAggregate(aggregate: PartnerDailyAggregateEntity) {
            partnerAggregates["${aggregate.partnerId}_${aggregate.dateStr}"] = aggregate
        }

        override suspend fun insertPartnerAggregates(aggregates: List<PartnerDailyAggregateEntity>) {
            aggregates.forEach { insertPartnerAggregate(it) }
        }

        override fun getPartnerDailyAggregateFlow(partnerId: String, dateStr: String): Flow<PartnerDailyAggregateEntity?> {
            return flowOf(partnerAggregates["${partnerId}_$dateStr"])
        }

        override suspend fun getPartnerDailyAggregate(partnerId: String, dateStr: String): PartnerDailyAggregateEntity? {
            return partnerAggregates["${partnerId}_$dateStr"]
        }

        override fun getPartnerDailyAggregatesHistoryFlow(partnerId: String): Flow<List<PartnerDailyAggregateEntity>> {
            return flowOf(partnerAggregates.values.filter { it.partnerId == partnerId })
        }

        override suspend fun deletePartnerData(partnerId: String) {
            partnerAggregates.entries.removeIf { it.value.partnerId == partnerId }
        }

        val partnerDailyMap = mutableMapOf<String, com.couplefit.data.local.PartnerDailyEntity>()
        override suspend fun insertPartnerDaily(entity: com.couplefit.data.local.PartnerDailyEntity) {
            partnerDailyMap["${entity.partnerId}_${entity.dateStr}"] = entity
        }
        override fun getPartnerDailyFlow(partnerId: String, dateStr: String): Flow<com.couplefit.data.local.PartnerDailyEntity?> = flowOf(partnerDailyMap["${partnerId}_$dateStr"])
        override fun getPartnerDailyFlow(dateStr: String): Flow<com.couplefit.data.local.PartnerDailyEntity?> = flowOf(partnerDailyMap.values.firstOrNull { it.dateStr == dateStr })
        override suspend fun getPartnerDaily(partnerId: String, dateStr: String): com.couplefit.data.local.PartnerDailyEntity? = partnerDailyMap["${partnerId}_$dateStr"]
        override suspend fun getPartnerDaily(dateStr: String): com.couplefit.data.local.PartnerDailyEntity? = partnerDailyMap.values.firstOrNull { it.dateStr == dateStr }
        override suspend fun deletePartnerDailyData(partnerId: String) {
            partnerDailyMap.entries.removeIf { it.value.partnerId == partnerId }
        }
        override fun getLatestPartnerDailyFlow(partnerId: String): Flow<com.couplefit.data.local.PartnerDailyEntity?> =
            flowOf(partnerDailyMap.values.filter { it.partnerId == partnerId }.maxByOrNull { it.dateStr })
        override suspend fun getLatestPartnerDaily(partnerId: String): com.couplefit.data.local.PartnerDailyEntity? =
            partnerDailyMap.values.filter { it.partnerId == partnerId }.maxByOrNull { it.dateStr }

        override fun getPartnerDailyBetweenFlow(partnerId: String, startDate: String, endDate: String): Flow<List<com.couplefit.data.local.PartnerDailyEntity>> =
            flowOf(partnerDailyMap.values.filter { (partnerId.isEmpty() || it.partnerId == partnerId) && it.dateStr in startDate..endDate })

        override suspend fun getPartnerDailyBetween(partnerId: String, startDate: String, endDate: String): List<com.couplefit.data.local.PartnerDailyEntity> =
            partnerDailyMap.values.filter { (partnerId.isEmpty() || it.partnerId == partnerId) && it.dateStr in startDate..endDate }

        val challengeMap = mutableMapOf<String, com.couplefit.data.local.CoupleChallengeEntity>()
        override suspend fun insertChallenge(challenge: com.couplefit.data.local.CoupleChallengeEntity) {
            challengeMap[challenge.id] = challenge
        }
        override suspend fun insertChallenges(challenges: List<com.couplefit.data.local.CoupleChallengeEntity>) {
            challenges.forEach { challengeMap[it.id] = it }
        }
        override fun getChallengesFlow(coupleId: String): Flow<List<com.couplefit.data.local.CoupleChallengeEntity>> =
            flowOf(challengeMap.values.filter { it.coupleId == coupleId })
        override fun getActiveChallengesFlow(coupleId: String): Flow<List<com.couplefit.data.local.CoupleChallengeEntity>> =
            flowOf(challengeMap.values.filter { it.coupleId == coupleId && it.status == "ACTIVE" })
        override suspend fun getChallenge(challengeId: String): com.couplefit.data.local.CoupleChallengeEntity? =
            challengeMap[challengeId]
        override suspend fun deleteCoupleChallenges(coupleId: String) {
            challengeMap.entries.removeIf { it.value.coupleId == coupleId }
        }
    }

    @Test
    fun `test getMyDailySnapshot maps Room entity to HealthSnapshot`() = runBlocking {
        val fakeDao = FakeHealthDao()
        val syncManager = FirebaseSyncManager()
        val repo = HealthRepositoryImpl(fakeDao, syncManager)

        val today = Date()
        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(today)

        fakeDao.insertAggregate(
            HealthDailyAggregateEntity(
                dateStr = dateStr,
                totalSteps = 7800L,
                totalActiveMinutes = 40L,
                totalCalories = 350L,
                averageHeartRate = 71.5,
                averageOxygenSaturation = 98.4,
                lastUpdatedTimestamp = 1729500000000L
            )
        )

        val snapshot = repo.getMyDailySnapshot(today).first()
        assertNotNull(snapshot)
        assertEquals(7800, snapshot?.steps)
        assertEquals(40, snapshot?.activeMinutes)
        assertEquals(350.0, snapshot?.activeCalories ?: 0.0, 0.001)
        assertEquals(71.5, snapshot?.averageHeartRate ?: 0.0, 0.001)
        assertEquals(98.4, snapshot?.averageOxygenSaturation ?: 0.0, 0.001)
    }

    @Test
    fun `test getPartnerDailySnapshot maps Room partner entity to HealthSnapshot`() = runBlocking {
        val fakeDao = FakeHealthDao()
        val syncManager = FirebaseSyncManager()
        val repo = HealthRepositoryImpl(fakeDao, syncManager)

        val today = Date()
        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(today)
        val partnerId = "partner_xyz"

        fakeDao.insertPartnerAggregate(
            PartnerDailyAggregateEntity(
                id = "${partnerId}_$dateStr",
                partnerId = partnerId,
                dateStr = dateStr,
                totalSteps = 10200L,
                totalActiveMinutes = 65L,
                totalCalories = 500L,
                averageHeartRate = 69.0,
                averageOxygenSaturation = 99.0,
                lastUpdatedTimestamp = 1729500000000L
            )
        )

        val partnerSnapshot = repo.getPartnerDailySnapshot(partnerId, today).first()
        assertNotNull(partnerSnapshot)
        assertEquals(partnerId, partnerSnapshot?.userId)
        assertEquals(10200, partnerSnapshot?.steps)
        assertEquals(65, partnerSnapshot?.activeMinutes)
        assertEquals(500.0, partnerSnapshot?.activeCalories ?: 0.0, 0.001)
        assertEquals(69.0, partnerSnapshot?.averageHeartRate ?: 0.0, 0.001)
        assertEquals(99.0, partnerSnapshot?.averageOxygenSaturation ?: 0.0, 0.001)
    }

    @Test
    fun `test getPartnerDailySnapshot returns null when no partner data cached`() = runBlocking {
        val fakeDao = FakeHealthDao()
        val syncManager = FirebaseSyncManager()
        val repo = HealthRepositoryImpl(fakeDao, syncManager)

        val snapshot = repo.getPartnerDailySnapshot("non_existent_partner", Date()).first()
        assertNull(snapshot)
    }
}
