package com.couplefit.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMetrics(metrics: List<HealthMetricEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAggregate(aggregate: HealthDailyAggregateEntity)

    // Deduplication query: Check if metric already exists
    @Query("SELECT EXISTS(SELECT 1 FROM health_metrics WHERE id = :id)")
    suspend fun metricExists(id: String): Boolean

    @Query("SELECT * FROM health_metrics WHERE metricType = :type AND sourceTimestamp >= :startTime AND sourceTimestamp <= :endTime ORDER BY sourceTimestamp ASC")
    fun getMetricsFlow(type: String, startTime: Long, endTime: Long): Flow<List<HealthMetricEntity>>

    @Query("SELECT * FROM health_metrics WHERE metricType = :type AND sourceTimestamp >= :startTime AND sourceTimestamp <= :endTime ORDER BY sourceTimestamp ASC")
    suspend fun getMetrics(type: String, startTime: Long, endTime: Long): List<HealthMetricEntity>

    @Query("SELECT * FROM health_daily_aggregates WHERE dateStr = :dateStr")
    fun getDailyAggregateFlow(dateStr: String): Flow<HealthDailyAggregateEntity?>

    @Query("SELECT * FROM health_daily_aggregates WHERE dateStr = :dateStr")
    suspend fun getDailyAggregate(dateStr: String): HealthDailyAggregateEntity?
    
    @Query("UPDATE health_metrics SET syncedToRemote = 1 WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<String>)
    
    @Query("SELECT * FROM health_metrics WHERE syncedToRemote = 0")
    suspend fun getUnsyncedMetrics(): List<HealthMetricEntity>
    
    // Partner Aggregates (Local SSOT cache of partner data from Firestore)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPartnerAggregate(aggregate: PartnerDailyAggregateEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPartnerAggregates(aggregates: List<PartnerDailyAggregateEntity>)

    @Query("SELECT * FROM partner_daily_aggregates WHERE partnerId = :partnerId AND dateStr = :dateStr")
    fun getPartnerDailyAggregateFlow(partnerId: String, dateStr: String): Flow<PartnerDailyAggregateEntity?>

    @Query("SELECT * FROM partner_daily_aggregates WHERE partnerId = :partnerId AND dateStr = :dateStr")
    suspend fun getPartnerDailyAggregate(partnerId: String, dateStr: String): PartnerDailyAggregateEntity?

    @Query("SELECT * FROM partner_daily_aggregates WHERE partnerId = :partnerId ORDER BY dateStr DESC")
    fun getPartnerDailyAggregatesHistoryFlow(partnerId: String): Flow<List<PartnerDailyAggregateEntity>>

    @Query("DELETE FROM partner_daily_aggregates WHERE partnerId = :partnerId")
    suspend fun deletePartnerData(partnerId: String)

    // partnerDaily Table queries
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPartnerDaily(entity: PartnerDailyEntity)

    @Query("SELECT * FROM partnerDaily WHERE partnerId = :partnerId AND dateStr = :dateStr")
    fun getPartnerDailyFlow(partnerId: String, dateStr: String): Flow<PartnerDailyEntity?>

    @Query("SELECT * FROM partnerDaily WHERE dateStr = :dateStr ORDER BY CASE WHEN partnerId != 'partner_1' THEN 0 ELSE 1 END, lastUpdated DESC LIMIT 1")
    fun getPartnerDailyFlow(dateStr: String): Flow<PartnerDailyEntity?>

    @Query("SELECT * FROM partnerDaily WHERE partnerId = :partnerId AND dateStr = :dateStr")
    suspend fun getPartnerDaily(partnerId: String, dateStr: String): PartnerDailyEntity?

    @Query("SELECT * FROM partnerDaily WHERE dateStr = :dateStr ORDER BY CASE WHEN partnerId != 'partner_1' THEN 0 ELSE 1 END, lastUpdated DESC LIMIT 1")
    suspend fun getPartnerDaily(dateStr: String): PartnerDailyEntity?

    @Query("DELETE FROM partnerDaily WHERE partnerId = :partnerId")
    suspend fun deletePartnerDailyData(partnerId: String)

    // Challenge Queries
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallenge(challenge: CoupleChallengeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallenges(challenges: List<CoupleChallengeEntity>)

    @Query("SELECT * FROM coupleChallenges WHERE coupleId = :coupleId ORDER BY lastUpdated DESC")
    fun getChallengesFlow(coupleId: String): Flow<List<CoupleChallengeEntity>>

    @Query("SELECT * FROM coupleChallenges WHERE coupleId = :coupleId AND status = 'ACTIVE' ORDER BY lastUpdated DESC")
    fun getActiveChallengesFlow(coupleId: String): Flow<List<CoupleChallengeEntity>>

    @Query("SELECT * FROM coupleChallenges WHERE id = :challengeId")
    suspend fun getChallenge(challengeId: String): CoupleChallengeEntity?

    @Query("DELETE FROM coupleChallenges WHERE coupleId = :coupleId")
    suspend fun deleteCoupleChallenges(coupleId: String)

    // Using transaction for safe remote and local sync
    @Transaction
    suspend fun updateLocalAfterRemoteSync(syncedIds: List<String>) {
        markAsSynced(syncedIds)
    }
}

