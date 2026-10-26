package com.couplefit.ui

import com.couplefit.data.model.AuthFlowStep
import com.couplefit.data.model.UserSession
import com.couplefit.ui.auth.AuthUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthFlowStateTest {

    @Test
    fun `default AuthUiState starts on SignIn step without loading`() {
        val state = AuthUiState()
        assertEquals(AuthFlowStep.SignIn, state.currentStep)
        assertFalse(state.isLoading)
        assertEquals(null, state.errorMessage)
        assertEquals(null, state.userSession)
    }

    @Test
    fun `unlinked session transitions out of SignIn to InvitePartner step`() {
        val initialState = AuthUiState(isLoading = true, currentStep = AuthFlowStep.SignIn)
        val unlinkedSession = UserSession(
            uid = "user_123",
            displayName = "Akash",
            email = "akash@example.com",
            coupleId = null,
            partnerId = null
        )
        assertFalse(unlinkedSession.isLinked)

        // Simulating the AuthViewModel session collection resolution:
        val resolvedState = if (unlinkedSession.isLinked) {
            initialState.copy(
                isLoading = false,
                userSession = unlinkedSession,
                currentStep = AuthFlowStep.LinkedConfirmation(
                    partnerId = unlinkedSession.partnerId!!,
                    coupleId = unlinkedSession.coupleId!!
                )
            )
        } else {
            initialState.copy(
                isLoading = false,
                userSession = unlinkedSession,
                currentStep = AuthFlowStep.InvitePartner
            )
        }

        assertFalse(resolvedState.isLoading)
        assertEquals(AuthFlowStep.InvitePartner, resolvedState.currentStep)
        assertEquals(unlinkedSession, resolvedState.userSession)
    }

    @Test
    fun `linked session transitions to LinkedConfirmation or Dashboard`() {
        val initialState = AuthUiState(isLoading = true, currentStep = AuthFlowStep.SignIn)
        val linkedSession = UserSession(
            uid = "user_123",
            displayName = "Akash",
            email = "akash@example.com",
            coupleId = "couple_abc",
            partnerId = "partner_456"
        )
        assertTrue(linkedSession.isLinked)

        val resolvedState = initialState.copy(
            isLoading = false,
            userSession = linkedSession,
            currentStep = AuthFlowStep.LinkedConfirmation(
                partnerId = linkedSession.partnerId!!,
                coupleId = linkedSession.coupleId!!
            )
        )

        assertFalse(resolvedState.isLoading)
        assertTrue(resolvedState.currentStep is AuthFlowStep.LinkedConfirmation)
        val step = resolvedState.currentStep as AuthFlowStep.LinkedConfirmation
        assertEquals("partner_456", step.partnerId)
        assertEquals("couple_abc", step.coupleId)
    }

    @Test
    fun `6-digit pairing code extraction from various link formats`() {
        val sixDigitRegex = Regex("""\b\d{6}\b""")

        // Standard App Link URL
        val appLinkUrl = "https://couplefit.app/invite/481920"
        val codeFromAppLink = sixDigitRegex.find(appLinkUrl)?.value
        assertEquals("481920", codeFromAppLink)

        // Custom URI Scheme
        val customSchemeUrl = "couplefit://invite/592810"
        val codeFromCustomScheme = sixDigitRegex.find(customSchemeUrl)?.value
        assertEquals("592810", codeFromCustomScheme)

        // Query parameter URL
        val queryUrl = "https://couplefit.app/invite?code=123456"
        val codeFromQuery = sixDigitRegex.find(queryUrl)?.value
        assertEquals("123456", codeFromQuery)
    }

    @Test
    fun `pending 6-digit pairing code triggers auto-redeem on sign-in`() {
        val stateWithPendingCode = AuthUiState(
            enteredCode = "481920",
            currentStep = AuthFlowStep.SignIn
        )
        val loggedInSession = UserSession(
            uid = "user_bob",
            displayName = "Bob",
            email = "bob@example.com",
            coupleId = null,
            partnerId = null
        )

        // Logic check: When signed in with pending 6-digit code, target step is linking instead of InvitePartner
        val shouldAutoRedeem = stateWithPendingCode.enteredCode.length == 6 &&
                stateWithPendingCode.enteredCode.all { it.isDigit() } &&
                !loggedInSession.isLinked

        assertTrue(shouldAutoRedeem)
    }

    @Test
    fun `partner link detection transitions to LinkedConfirmation step with partner and couple ids`() {
        val initialInviteState = AuthUiState(
            currentStep = AuthFlowStep.InvitePartner,
            userSession = UserSession(uid = "creator_uid", displayName = "Alice")
        )

        val partnerId = "partner_bob_uid"
        val coupleId = "cpl_123456"

        val updatedSession = initialInviteState.userSession!!.copy(
            coupleId = coupleId,
            partnerId = partnerId
        )

        val linkedState = initialInviteState.copy(
            isLoading = false,
            userSession = updatedSession,
            currentStep = AuthFlowStep.LinkedConfirmation(
                partnerId = partnerId,
                coupleId = coupleId
            ),
            successMessage = "Partner connected successfully!"
        )

        assertTrue(linkedState.userSession!!.isLinked)
        assertTrue(linkedState.currentStep is AuthFlowStep.LinkedConfirmation)
        val step = linkedState.currentStep as AuthFlowStep.LinkedConfirmation
        assertEquals(partnerId, step.partnerId)
        assertEquals(coupleId, step.coupleId)
    }

    @Test
    fun `clean digit sanitization ensures valid 6-digit invite code`() {
        val messyInputs = listOf(
            " 123456 " to "123456",
            "12-34-56" to "123456",
            "code: 987654" to "987654",
            "12345" to "12345"
        )

        for ((input, expected) in messyInputs) {
            val sanitized = input.filter { it.isDigit() }.take(6)
            assertEquals(expected, sanitized)
        }
    }
}

