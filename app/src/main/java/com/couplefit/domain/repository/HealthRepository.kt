package com.couplefit.domain.repository

import com.couplefit.data.model.HealthSnapshot
import kotlinx.coroutines.flow.Flow
import java.util.Date

interface HealthRepository {
    fun getMyDailySnapshot(date: Date): Flow<HealthSnapshot?>
    fun getPartnerDailySnapshot(partnerId: String, date: Date): Flow<HealthSnapshot?>
    
    suspend fun syncWithPartner()
}

