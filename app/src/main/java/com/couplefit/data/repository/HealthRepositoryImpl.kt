package com.couplefit.data.repository

import com.couplefit.data.model.HealthSnapshot
import com.couplefit.domain.repository.HealthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HealthRepositoryImpl @Inject constructor() : HealthRepository {
    override fun getMyDailySnapshot(date: Date): Flow<HealthSnapshot?> {
        // TODO: Implement actual logic
        return emptyFlow()
    }

    override fun getPartnerDailySnapshot(partnerId: String, date: Date): Flow<HealthSnapshot?> {
        // TODO: Implement actual logic
        return emptyFlow()
    }

    override suspend fun syncWithPartner() {
        // TODO: Implement sync logic
    }
}

