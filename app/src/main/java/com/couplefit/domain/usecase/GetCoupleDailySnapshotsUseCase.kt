package com.couplefit.domain.usecase

import com.couplefit.data.model.HealthSnapshot
import com.couplefit.domain.repository.HealthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.Date
import javax.inject.Inject

class GetCoupleDailySnapshotsUseCase @Inject constructor(
    private val healthRepository: HealthRepository
) {
    operator fun invoke(partnerId: String, date: Date): Flow<Pair<HealthSnapshot?, HealthSnapshot?>> {
        val mySnapshot = healthRepository.getMyDailySnapshot(date)
        val partnerSnapshot = healthRepository.getPartnerDailySnapshot(partnerId, date)
        
        return combine(mySnapshot, partnerSnapshot) { mine, partner ->
            Pair(mine, partner)
        }
    }
}

