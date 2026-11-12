package com.couplefit.ui

import com.couplefit.data.local.CoupleFitPreferencesDataStore
import com.couplefit.data.local.HealthDao
import com.couplefit.data.model.UserSession
import com.couplefit.data.remote.FirebaseAuthManager
import com.couplefit.ui.hub.CoupleHubUiState
import com.couplefit.ui.hub.CoupleHubViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import java.util.Base64

@OptIn(ExperimentalCoroutinesApi::class)
class Phase72BirthdayResolutionTest {

    private val testDispatcher = StandardTestDispatcher()

    private val authManager: FirebaseAuthManager = mockk(relaxed = true)
    private val preferencesDataStore: CoupleFitPreferencesDataStore = mockk(relaxed = true)
    private val healthDao: HealthDao = mockk(relaxed = true)
    private val firestore: FirebaseFirestore = mockk(relaxed = true)

    private val realAuthManager = FirebaseAuthManager(
        context = mockk(relaxed = true),
        firebaseAuth = mockk(relaxed = true),
        firestore = firestore,
        preferencesDataStore = preferencesDataStore
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    @Test
    fun `formatBirthday handles diverse international date formats and month name text`() {
        // ISO YYYY-MM-DD
        assertEquals("June 14", realAuthManager.formatBirthday("1995-06-14"))
        assertEquals("September 28", realAuthManager.formatBirthday("1998-09-28"))

        // European / Indian DD-MM-YYYY and DD/MM/YYYY
        assertEquals("June 14", realAuthManager.formatBirthday("14-06-1995"))
        assertEquals("September 28", realAuthManager.formatBirthday("28/09/1998"))

        // US MM/DD/YYYY
        assertEquals("December 25", realAuthManager.formatBirthday("12/25/1990"))

        // Textual month strings
        assertEquals("June 14", realAuthManager.formatBirthday("June 14"))
        assertEquals("September 28", realAuthManager.formatBirthday("28 September"))
        assertEquals("June 14", realAuthManager.formatBirthday("14th June 1995"))
        assertEquals("September 28", realAuthManager.formatBirthday("Sep 28"))
    }

    @Test
    fun `extractBirthdayFromIdToken extracts diverse claim aliases from JWT`() {
        val header = Base64.getUrlEncoder().withoutPadding().encodeToString("""{"alg":"RS256","typ":"JWT"}""".toByteArray())

        // dob claim
        val payloadDob = Base64.getUrlEncoder().withoutPadding().encodeToString("""{"sub":"123","dob":"1995-06-14"}""".toByteArray())
        assertEquals("June 14", realAuthManager.extractBirthdayFromIdToken("$header.$payloadDob.mockSig"))

        // birthDate camelCase claim
        val payloadBirthDate = Base64.getUrlEncoder().withoutPadding().encodeToString("""{"sub":"123","birthDate":"1998-09-28"}""".toByteArray())
        assertEquals("September 28", realAuthManager.extractBirthdayFromIdToken("$header.$payloadBirthDate.mockSig"))

        // dateOfBirth claim
        val payloadDateOfBirth = Base64.getUrlEncoder().withoutPadding().encodeToString("""{"sub":"123","dateOfBirth":"1990-12-25"}""".toByteArray())
        assertEquals("December 25", realAuthManager.extractBirthdayFromIdToken("$header.$payloadDateOfBirth.mockSig"))
    }

    @Test
    fun `CoupleHubViewModel updateUserBirthday formats date and mirrors to cloud`() = runTest(testDispatcher) {
        val session = UserSession(
            uid = "user_123",
            displayName = "TaleOrbit",
            email = "test@example.com",
            coupleId = "cpl_test_123",
            partnerId = "partner_456"
        )
        every { authManager.getCurrentSession() } returns session
        every { authManager.formatBirthday("1995-06-14") } returns "June 14"
        every { preferencesDataStore.userNameFlow } returns flowOf("TaleOrbit")
        every { preferencesDataStore.partnerNameFlow } returns flowOf("Akash")
        every { preferencesDataStore.userBirthdayFlow } returns flowOf("June 14")
        every { preferencesDataStore.partnerBirthdayFlow } returns flowOf("September 28")
        every { preferencesDataStore.relationshipTypeFlow } returns flowOf("Partners")
        every { preferencesDataStore.pairedDateTimestampFlow } returns flowOf(System.currentTimeMillis())

        val viewModel = CoupleHubViewModel(preferencesDataStore, authManager, healthDao)
        testScheduler.advanceUntilIdle()

        viewModel.updateUserBirthday("1995-06-14")
        testScheduler.advanceUntilIdle()

        coVerify { preferencesDataStore.saveUserBirthday("June 14") }
        coVerify { preferencesDataStore.mirrorUserBirthdayToFirestore("user_123", "cpl_test_123", "June 14") }
        assertEquals("June 14", viewModel.uiState.value.userBirthday)
    }

    @Test
    fun `CoupleHubViewModel updatePartnerBirthday formats date and mirrors to cloud`() = runTest(testDispatcher) {
        val session = UserSession(
            uid = "user_123",
            displayName = "TaleOrbit",
            email = "test@example.com",
            coupleId = "cpl_test_123",
            partnerId = "partner_456"
        )
        every { authManager.getCurrentSession() } returns session
        every { authManager.formatBirthday("1998-09-28") } returns "September 28"
        every { preferencesDataStore.userNameFlow } returns flowOf("TaleOrbit")
        every { preferencesDataStore.partnerNameFlow } returns flowOf("Akash")
        every { preferencesDataStore.userBirthdayFlow } returns flowOf("June 14")
        every { preferencesDataStore.partnerBirthdayFlow } returns flowOf("September 28")
        every { preferencesDataStore.relationshipTypeFlow } returns flowOf("Partners")
        every { preferencesDataStore.pairedDateTimestampFlow } returns flowOf(System.currentTimeMillis())

        val viewModel = CoupleHubViewModel(preferencesDataStore, authManager, healthDao)
        testScheduler.advanceUntilIdle()

        viewModel.updatePartnerBirthday("1998-09-28")
        testScheduler.advanceUntilIdle()

        coVerify { preferencesDataStore.savePartnerBirthday("September 28") }
        coVerify { preferencesDataStore.mirrorPartnerBirthdayToFirestore("user_123", "cpl_test_123", "partner_456", "September 28") }
        assertEquals("September 28", viewModel.uiState.value.partnerBirthday)
    }
}
