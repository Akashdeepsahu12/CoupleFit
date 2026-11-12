package com.couplefit.ui

import com.couplefit.data.local.CoupleFitPreferencesDataStore
import com.couplefit.data.local.HealthDao
import com.couplefit.data.model.UserSession
import com.couplefit.data.remote.FirebaseAuthManager
import com.couplefit.domain.repository.ChallengeRepository
import com.couplefit.ui.challenge.ChallengeViewModel
import com.couplefit.ui.hub.CoupleHubViewModel
import com.couplefit.ui.trends.HealthTrendsViewModel
import com.couplefit.util.RemoteConfigManager
import com.google.firebase.auth.FirebaseAuth
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class Phase71PartnerNameSyncTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var healthDao: HealthDao
    private lateinit var authManager: FirebaseAuthManager
    private lateinit var dataStore: CoupleFitPreferencesDataStore
    private lateinit var challengeRepository: ChallengeRepository
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var remoteConfigManager: RemoteConfigManager

    private val sessionFlow = MutableStateFlow<UserSession?>(null)
    private val partnerNameFlow = MutableStateFlow<String?>(null)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        healthDao = mockk(relaxed = true)
        authManager = mockk(relaxed = true)
        dataStore = mockk(relaxed = true)
        challengeRepository = mockk(relaxed = true)
        firebaseAuth = mockk(relaxed = true)
        remoteConfigManager = mockk(relaxed = true)

        every { authManager.observeUserSession() } returns sessionFlow
        every { authManager.getCurrentSession() } answers { sessionFlow.value }
        every { dataStore.partnerNameFlow } returns partnerNameFlow
        every { dataStore.userNameFlow } returns flowOf("TaleOrbit")
        every { dataStore.userBirthdayFlow } returns flowOf("June 14")
        every { dataStore.partnerBirthdayFlow } returns flowOf("September 28")
        every { dataStore.relationshipTypeFlow } returns flowOf("Life Comrades & Fitness Partners ❤️")
        every { dataStore.pairedDateTimestampFlow } returns flowOf(0L)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun coupleHubViewModel_whenPartnerNameNullAndPartnerLinked_resolvesToAkash() = runTest(testDispatcher) {
        sessionFlow.value = UserSession(
            uid = "EYf2Io9WPDUasjbuqL9wKr1H8lD3",
            displayName = "TaleOrbit",
            email = "thetaleorbit@gmail.com",
            coupleId = "cpl_1793319867725_RmkTP",
            partnerId = "RmkTPEUJlwYVm23evsWOj6cOpi02"
        )

        val viewModel = CoupleHubViewModel(dataStore, authManager, healthDao)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Akash", state.partnerName)
        assertEquals("TaleOrbit", state.userName)
        coVerify { dataStore.savePartnerName("Akash") }
    }

    @Test
    fun coupleHubViewModel_whenCustomPartnerNameProvided_preservesCustomName() = runTest(testDispatcher) {
        partnerNameFlow.value = "Priya"
        sessionFlow.value = UserSession(
            uid = "EYf2Io9WPDUasjbuqL9wKr1H8lD3",
            displayName = "TaleOrbit",
            email = "thetaleorbit@gmail.com",
            coupleId = "cpl_1793319867725_RmkTP",
            partnerId = "RmkTPEUJlwYVm23evsWOj6cOpi02"
        )

        val viewModel = CoupleHubViewModel(dataStore, authManager, healthDao)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Priya", state.partnerName)
    }

    @Test
    fun coupleHubViewModel_updatePartnerName_mirrorsToFirestore() = runTest(testDispatcher) {
        sessionFlow.value = UserSession(
            uid = "EYf2Io9WPDUasjbuqL9wKr1H8lD3",
            displayName = "TaleOrbit",
            email = "thetaleorbit@gmail.com",
            coupleId = "cpl_1793319867725_RmkTP",
            partnerId = "RmkTPEUJlwYVm23evsWOj6cOpi02"
        )

        val viewModel = CoupleHubViewModel(dataStore, authManager, healthDao)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.updatePartnerName("My Sunshine")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { dataStore.savePartnerName("My Sunshine") }
        coVerify {
            dataStore.mirrorPartnerNameToFirestore(
                "EYf2Io9WPDUasjbuqL9wKr1H8lD3",
                "cpl_1793319867725_RmkTP",
                "RmkTPEUJlwYVm23evsWOj6cOpi02",
                "My Sunshine"
            )
        }
    }

    @Test
    fun coupleHubViewModel_updateUserName_mirrorsToFirestore() = runTest(testDispatcher) {
        sessionFlow.value = UserSession(
            uid = "EYf2Io9WPDUasjbuqL9wKr1H8lD3",
            displayName = "TaleOrbit",
            email = "thetaleorbit@gmail.com",
            coupleId = "cpl_1793319867725_RmkTP",
            partnerId = "RmkTPEUJlwYVm23evsWOj6cOpi02"
        )

        val viewModel = CoupleHubViewModel(dataStore, authManager, healthDao)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.updateUserName("Orbit Explorer")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { dataStore.saveUserName("Orbit Explorer") }
        coVerify {
            dataStore.mirrorUserNameToFirestore(
                "EYf2Io9WPDUasjbuqL9wKr1H8lD3",
                "cpl_1793319867725_RmkTP",
                "Orbit Explorer"
            )
        }
    }

    @Test
    fun healthTrendsViewModel_resolvesAkashWhenPartnerNameNullAndLinked() = runTest(testDispatcher) {
        sessionFlow.value = UserSession(
            uid = "EYf2Io9WPDUasjbuqL9wKr1H8lD3",
            displayName = "TaleOrbit",
            email = "thetaleorbit@gmail.com",
            coupleId = "cpl_1793319867725_RmkTP",
            partnerId = "RmkTPEUJlwYVm23evsWOj6cOpi02"
        )

        val viewModel = HealthTrendsViewModel(healthDao, authManager, dataStore)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Akash", viewModel.uiState.value.partnerName)
    }

    @Test
    fun challengeViewModel_resolvesAkashWhenPartnerNameNullAndLinked() = runTest(testDispatcher) {
        sessionFlow.value = UserSession(
            uid = "EYf2Io9WPDUasjbuqL9wKr1H8lD3",
            displayName = "TaleOrbit",
            email = "thetaleorbit@gmail.com",
            coupleId = "cpl_1793319867725_RmkTP",
            partnerId = "RmkTPEUJlwYVm23evsWOj6cOpi02"
        )

        val viewModel = ChallengeViewModel(challengeRepository, firebaseAuth, authManager, remoteConfigManager, dataStore)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Akash", viewModel.uiState.value.partnerName)
    }
}
