package com.couplefit.domain.repository

import com.couplefit.data.local.CoupleChallengeEntity
import kotlinx.coroutines.flow.Flow

interface ChallengeRepository {
    fun getChallengesFlow(coupleId: String): Flow<List<CoupleChallengeEntity>>
    fun getActiveChallengesFlow(coupleId: String): Flow<List<CoupleChallengeEntity>>
    fun listenToCoupleChallenges(coupleId: String, myUid: String, partnerUid: String): Flow<List<CoupleChallengeEntity>>
    suspend fun createChallenge(
        coupleId: String,
        type: String,
        goal: Long,
        start: String,
        end: String,
        creatorUid: String
    ): Result<String>
}
