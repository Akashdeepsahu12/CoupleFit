package com.couplefit.ui

import com.couplefit.data.remote.FirebaseAuthManager
import com.couplefit.ui.hub.CoupleHubUiState
import com.couplefit.ui.healthconnect.HealthDashboardUiState
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Base64

/**
 * Phase 69 Unit Test Suite:
 * - Google OAuth Birthday extraction and formatting
 * - Couple Hub UI state decluttering (Fitness Identity excision)
 * - Dynamic Partner Name mapping
 * - Real-Time Permission & Wearable Bridge state verification
 */
class Phase69FeatureRefinementTest {

    private val authManager = FirebaseAuthManager(
        context = mockk(relaxed = true),
        firebaseAuth = mockk(relaxed = true),
        firestore = mockk(relaxed = true),
        preferencesDataStore = mockk(relaxed = true)
    )

    @Test
    fun `formatBirthday correctly standardizes various ISO and OIDC date formats`() {
        assertEquals("June 14", authManager.formatBirthday("1995-06-14"))
        assertEquals("September 28", authManager.formatBirthday("1998-09-28"))
        assertEquals("June 14", authManager.formatBirthday("--06-14"))
        assertEquals("December 25", authManager.formatBirthday("12/25/1990"))
        assertEquals("January 1", authManager.formatBirthday("January 1"))
    }

    @Test
    fun `extractBirthdayFromIdToken extracts birthdate claim from JWT payload`() {
        // Construct a mock JWT: header.payload.signature
        val header = Base64.getUrlEncoder().withoutPadding().encodeToString("""{"alg":"RS256","typ":"JWT"}""".toByteArray())
        val payload = Base64.getUrlEncoder().withoutPadding().encodeToString("""{"sub":"12345","email":"test@example.com","birthdate":"1995-06-14"}""".toByteArray())
        val mockJwt = "$header.$payload.mockSignature"

        val extracted = authManager.extractBirthdayFromIdToken(mockJwt)
        assertEquals("June 14", extracted)
    }

    @Test
    fun `extractBirthdayFromIdToken extracts birthday claim from JWT payload`() {
        val header = Base64.getUrlEncoder().withoutPadding().encodeToString("""{"alg":"RS256","typ":"JWT"}""".toByteArray())
        val payload = Base64.getUrlEncoder().withoutPadding().encodeToString("""{"sub":"12345","email":"test@example.com","birthday":"1998-09-28"}""".toByteArray())
        val mockJwt = "$header.$payload.mockSignature"

        val extracted = authManager.extractBirthdayFromIdToken(mockJwt)
        assertEquals("September 28", extracted)
    }

    @Test
    fun `extractBirthdayFromIdToken returns null when no birthdate claim exists`() {
        val header = Base64.getUrlEncoder().withoutPadding().encodeToString("""{"alg":"RS256","typ":"JWT"}""".toByteArray())
        val payload = Base64.getUrlEncoder().withoutPadding().encodeToString("""{"sub":"12345","email":"test@example.com"}""".toByteArray())
        val mockJwt = "$header.$payload.mockSignature"

        val extracted = authManager.extractBirthdayFromIdToken(mockJwt)
        assertNull(extracted)
    }

    @Test
    fun `CoupleHubUiState holds decluttered personal and partner dossiers without fitness identity`() {
        val state = CoupleHubUiState(
            userName = "Akash",
            partnerName = "Priya",
            userBirthday = "June 14",
            partnerBirthday = "September 28",
            totalDaysPaired = 540L
        )

        assertEquals("Akash", state.userName)
        assertEquals("Priya", state.partnerName)
        assertEquals("June 14", state.userBirthday)
        assertEquals("September 28", state.partnerBirthday)
        assertEquals(540L, state.totalDaysPaired)

        // Verify that fields represent pure intimacy and connection
        assertNotNull(state.relationshipType)
    }

    @Test
    fun `HealthDashboardUiState supports dynamic partner name and real-time permission states`() {
        val dashboardState = HealthDashboardUiState(
            partnerName = "Priya",
            isPartnerLinked = true,
            hasPermissions = true,
            hasAllPermissions = true
        )

        assertEquals("Priya", dashboardState.partnerName)
        assertTrue(dashboardState.isPartnerLinked)
        assertTrue(dashboardState.hasPermissions)
        assertTrue(dashboardState.hasAllPermissions)
    }
}
