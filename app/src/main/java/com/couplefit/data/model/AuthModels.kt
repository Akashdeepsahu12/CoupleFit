package com.couplefit.data.model

import java.util.Date

/**
 * Authentication and Session Models
 */
data class UserSession(
    val uid: String,
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String? = null,
    val coupleId: String? = null,
    val partnerId: String? = null
) {
    val isLinked: Boolean
        get() = !coupleId.isNullOrBlank() && !partnerId.isNullOrBlank()
}

data class InviteResult(
    val coupleId: String,
    val inviteCode: String,
    val expiresAt: Long,
    val inviteUrl: String
)

data class RedeemResult(
    val success: Boolean,
    val coupleId: String,
    val partnerId: String,
    val message: String
)

sealed interface AuthFlowStep {
    data object SignIn : AuthFlowStep
    data object InvitePartner : AuthFlowStep
    data object EnterCode : AuthFlowStep
    data class LinkedConfirmation(val partnerId: String, val coupleId: String) : AuthFlowStep
    data object Dashboard : AuthFlowStep
}
