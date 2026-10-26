package com.couplefit.domain.repository

import com.couplefit.data.local.HealthDailyAggregateEntity
import kotlinx.coroutines.flow.Flow

sealed class SyncResult {
    data class Success(val uploadedMetrics: Map<String, Any>) : SyncResult()
    object Throttled : SyncResult()
    data class Failure(val error: Throwable) : SyncResult()
}

interface SyncRepository {
    /**
     * Uploads user's daily aggregate to couples/{coupleId}/daily/{myUid_date}.
     * STRICT CONSTRAINTS:
     * - Only metrics the user has enabled sharing for in MetricSharingPreferences are included.
     * - Throttled to at most once every 15 minutes unless [force] is true.
     */
    suspend fun uploadMyDailyAggregate(
        coupleId: String,
        myUid: String,
        dateStr: String,
        force: Boolean = false
    ): SyncResult

    /**
     * Triggered on app foreground:
     * Uploads user's daily aggregate.
     */
    suspend fun syncOnAppForeground(
        coupleId: String,
        myUid: String,
        dateStr: String
    ): SyncResult

    fun getMyDailyAggregateFlow(dateStr: String): Flow<HealthDailyAggregateEntity?>
    suspend fun getMyDailyAggregate(dateStr: String): HealthDailyAggregateEntity?
}
