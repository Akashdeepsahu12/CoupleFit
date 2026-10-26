package com.couplefit.domain.repository

import com.couplefit.data.local.PartnerDailyEntity
import kotlinx.coroutines.flow.Flow

interface PartnerRepository {
    /**
     * Reads cached partner daily aggregate from Room table 'partnerDaily'.
     * Single Source of Truth for offline dashboard rendering.
     */
    fun getPartnerDailyFlow(partnerId: String, dateStr: String): Flow<PartnerDailyEntity?>
    fun getPartnerDailyFlow(dateStr: String): Flow<PartnerDailyEntity?>
    suspend fun getPartnerDaily(partnerId: String, dateStr: String): PartnerDailyEntity?

    /**
     * Listens to partner's daily document at couples/{coupleId}/daily/{partnerUid_date}
     * in Firestore, automatically writing received snapshots into Room table 'partnerDaily'.
     * Emits the local Room entity reactively.
     */
    fun listenToPartnerDaily(coupleId: String, partnerUid: String, dateStr: String): Flow<PartnerDailyEntity?>

    /**
     * Directly inserts/updates partner daily entity in Room.
     */
    suspend fun savePartnerDaily(entity: PartnerDailyEntity)

    /**
     * Exposes combine(myFlow, partnerFlow) from Room for side-by-side dashboard UI.
     */
    fun getCombinedDashboardFlow(partnerId: String, dateStr: String): Flow<com.couplefit.domain.model.CombinedDailyDashboard>
    fun getCombinedDashboardFlow(dateStr: String): Flow<com.couplefit.domain.model.CombinedDailyDashboard>

    /**
     * Clears cached partner data for a specific partner.
     */
    suspend fun clearPartnerDaily(partnerId: String)

    /**
     * Sends a nudge to the partner by creating a document in couples/{coupleId}/nudges.
     * Rate limiting is enforced strictly on the server in Cloud Functions (not client-side).
     */
    suspend fun sendNudge(
        coupleId: String,
        senderUid: String,
        senderName: String?,
        recipientUid: String,
        message: String = "Time to get active and close your rings! 🏃"
    ): Result<String>
}
