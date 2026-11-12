package com.couplefit.ui

import com.couplefit.data.local.CoupleFitPreferencesDataStore
import com.couplefit.data.local.HealthDailyAggregateEntity
import com.couplefit.data.local.HealthDao
import com.couplefit.data.local.PartnerDailyEntity
import com.couplefit.data.model.UserSession
import com.couplefit.data.remote.FirebaseAuthManager
import com.couplefit.domain.repository.ChallengeRepository
import com.couplefit.ui.challenge.ChallengeViewModel
import com.couplefit.ui.hub.CoupleHubViewModel
import com.couplefit.ui.trends.HealthTrendsViewModel
import com.couplefit.util.RemoteConfigManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalCoroutinesApi::class)
class Phase70TelemetryConvergenceTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var healthDao: HealthDao
    private lateinit var authManager: FirebaseAuthManager
    private lateinit var dataStore: CoupleFitPreferencesDataStore
    private lateinit var challengeRepository: ChallengeRepository
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var remoteConfigManager: RemoteConfigManager

    private val sessionFlow = MutableStateFlow<UserSession?>(null)
    private val partnerNameFlow = MutableStateFlow<String?>("Sweetheart")
    private val pairedDateTsFlow = MutableStateFlow<Long>(0L)

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
        every { dataStore.userNameFlow } returns flowOf("Akash")
        every { dataStore.userBirthdayFlow } returns flowOf("June 14")
        every { dataStore.partnerBirthdayFlow } returns flowOf("September 28")
        every { dataStore.relationshipTypeFlow } returns flowOf("Fitness Partners ❤️")
        every { dataStore.pairedDateTimestampFlow } returns pairedDateTsFlow

        val mockUser = mockk<FirebaseUser>(relaxed = true)
        every { mockUser.uid } returns "user_test_uid"
        every { firebaseAuth.currentUser } returns mockUser
        every { remoteConfigManager.stepsRaceGoal } returns 50000L
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testHealthTrendsViewModelCalculatesDynamicAggregatesAccurately() = runTest(testDispatcher) {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val yesterday = LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE)

        val myDailyAggs = listOf(
            HealthDailyAggregateEntity(dateStr = yesterday, totalSteps = 4000L, totalActiveMinutes = 30L, totalCalories = 300L, averageHeartRate = 72.0, averageOxygenSaturation = 98.0, lastUpdatedTimestamp = 1000L),
            HealthDailyAggregateEntity(dateStr = today, totalSteps = 6000L, totalActiveMinutes = 40L, totalCalories = 450L, averageHeartRate = 74.0, averageOxygenSaturation = 98.5, lastUpdatedTimestamp = 2000L)
        )
        val partnerDailyAggs = listOf(
            PartnerDailyEntity(id = "p_1", partnerId = "partner_real", dateStr = yesterday, steps = 3000L, activeMinutes = 25L, calories = 250L, avgHr = 70.0, avgSpo2 = 98.0, lastUpdated = 1000L),
            PartnerDailyEntity(id = "p_2", partnerId = "partner_real", dateStr = today, steps = 7000L, activeMinutes = 45L, calories = 500L, avgHr = 75.0, avgSpo2 = 99.0, lastUpdated = 2000L)
        )

        every { healthDao.getDailyAggregatesBetweenFlow(any(), any()) } returns flowOf(myDailyAggs)
        every { healthDao.getPartnerDailyBetweenFlow(any(), any(), any()) } returns flowOf(partnerDailyAggs)

        sessionFlow.value = UserSession(
            uid = "user_test_uid",
            partnerId = "partner_real",
            coupleId = "cpl_1793319867725_RmkTP"
        )

        val trendsVm = HealthTrendsViewModel(healthDao, authManager, dataStore)
        testScheduler.advanceUntilIdle()

        val state = trendsVm.uiState.value
        assertEquals("Sweetheart", state.partnerName)

        // Day Data (today)
        val dayData = state.dayData
        assertEquals(6000L, dayData.userSteps)
        assertEquals(7000L, dayData.partnerSteps)
        assertEquals(13000L, dayData.combinedSteps)
        assertEquals(40L + 45L, dayData.totalActiveMins)
        assertEquals(450L + 500L, dayData.totalCalories)
        // 6000 / 13000 = 46%, partner = 54%
        assertEquals(46, dayData.userSplitPercent)
        assertEquals(54, dayData.partnerSplitPercent)
        // Synchrony: 6000 / 7000 = 85%
        assertEquals(85, dayData.synchronyPercent)

        // Week Data (2 days within range)
        val weekData = state.weekData
        assertEquals(10000L, weekData.userSteps)
        assertEquals(10000L, weekData.partnerSteps)
        assertEquals(20000L, weekData.combinedSteps)
        assertEquals(50, weekData.userSplitPercent)
        assertEquals(50, weekData.partnerSplitPercent)
        assertEquals(100, weekData.synchronyPercent) // perfectly in sync!
    }

    @Test
    fun testChallengeViewModelBindsRealSessionAndAvoidsCouple1() = runTest(testDispatcher) {
        sessionFlow.value = UserSession(
            uid = "user_test_uid",
            partnerId = "partner_real_uid",
            coupleId = "cpl_1793319867725_RmkTP"
        )

        coEvery {
            challengeRepository.createChallenge(any(), any(), any(), any(), any(), any())
        } returns Result.success("challenge_new_id")

        val challengeVm = ChallengeViewModel(
            challengeRepository,
            firebaseAuth,
            authManager,
            remoteConfigManager,
            dataStore
        )
        testScheduler.advanceUntilIdle()

        assertEquals("cpl_1793319867725_RmkTP", challengeVm.uiState.value.coupleId)
        assertEquals("partner_real_uid", challengeVm.uiState.value.partnerUid)

        // Create challenge uses the dynamic authenticated coupleId
        challengeVm.createChallenge(type = "STEPS_RACE", goal = 50000L)
        testScheduler.advanceUntilIdle()

        coVerify(exactly = 1) {
            challengeRepository.createChallenge(
                coupleId = "cpl_1793319867725_RmkTP",
                type = "STEPS_RACE",
                goal = 50000L,
                start = any(),
                end = any(),
                creatorUid = "user_test_uid"
            )
        }
    }

    @Test
    fun testCoupleHubViewModelDerivesTimestampAndCalculatesStreak() = runTest(testDispatcher) {
        val coupleCreatedTs = 1793319867725L // Nov 10, 2026
        sessionFlow.value = UserSession(
            uid = "user_test_uid",
            partnerId = "partner_real_uid",
            coupleId = "cpl_${coupleCreatedTs}_RmkTP"
        )

        // Mock recent daily data to simulate 2-day shared streak
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val yesterday = LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE)

        coEvery { healthDao.getDailyAggregatesBetween(any(), any()) } returns listOf(
            HealthDailyAggregateEntity(dateStr = yesterday, totalSteps = 2000L, totalActiveMinutes = 20L, totalCalories = 100L, averageHeartRate = 70.0, averageOxygenSaturation = 98.0, lastUpdatedTimestamp = 100L),
            HealthDailyAggregateEntity(dateStr = today, totalSteps = 1500L, totalActiveMinutes = 15L, totalCalories = 80L, averageHeartRate = 72.0, averageOxygenSaturation = 98.0, lastUpdatedTimestamp = 200L)
        )
        coEvery { healthDao.getPartnerDailyBetween(any(), any(), any()) } returns listOf(
            PartnerDailyEntity(id = "p1", partnerId = "partner_real_uid", dateStr = yesterday, steps = 3000L, activeMinutes = 20L, calories = 150L, avgHr = 70.0, avgSpo2 = 98.0, lastUpdated = 10L),
            PartnerDailyEntity(id = "p2", partnerId = "partner_real_uid", dateStr = today, steps = 1800L, activeMinutes = 18L, calories = 100L, avgHr = 72.0, avgSpo2 = 98.0, lastUpdated = 20L)
        )

        val hubVm = CoupleHubViewModel(dataStore, authManager, healthDao)
        testScheduler.advanceUntilIdle()

        val state = hubVm.uiState.value
        assertEquals("Akash", state.userName)
        assertEquals("Sweetheart", state.partnerName)
        // Streak is 2 consecutive days
        assertEquals(2, state.sharedGoalStreak)
        // totalDaysPaired calculated from real timestamp (~14 days, NOT 540 days)
        assertTrue("Days paired should be around 14 days", state.totalDaysPaired in 10..20)
    }
}
