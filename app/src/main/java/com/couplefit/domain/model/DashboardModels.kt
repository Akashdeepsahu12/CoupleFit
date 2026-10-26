package com.couplefit.domain.model

import com.couplefit.data.local.HealthDailyAggregateEntity
import com.couplefit.data.local.PartnerDailyEntity

data class CombinedDailyDashboard(
    val myDaily: HealthDailyAggregateEntity?,
    val partnerDaily: PartnerDailyEntity?
) {
    val mySteps: Long get() = myDaily?.totalSteps ?: 0L
    val partnerSteps: Long get() = partnerDaily?.steps ?: 0L
    val stepsDifference: Long get() = mySteps - partnerSteps

    val myActiveMinutes: Long get() = myDaily?.totalActiveMinutes ?: 0L
    val partnerActiveMinutes: Long get() = partnerDaily?.activeMinutes ?: 0L

    val myCalories: Long get() = myDaily?.totalCalories ?: 0L
    val partnerCalories: Long get() = partnerDaily?.calories ?: 0L

    val myAvgHr: Double get() = myDaily?.averageHeartRate ?: 0.0
    val partnerAvgHr: Double get() = partnerDaily?.avgHr ?: 0.0

    val partnerLastUpdated: Long? get() = partnerDaily?.lastUpdated
}
