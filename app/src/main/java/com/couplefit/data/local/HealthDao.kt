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
    
    // Using transaction for safe remote and local sync
    @Transaction
    suspend fun updateLocalAfterRemoteSync(syncedIds: List<String>) {
        markAsSynced(syncedIds)
    }
}
