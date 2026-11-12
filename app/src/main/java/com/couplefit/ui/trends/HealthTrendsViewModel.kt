package com.couplefit.ui.trends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.couplefit.data.local.CoupleFitPreferencesDataStore
import com.couplefit.data.local.HealthDao
import com.couplefit.data.remote.FirebaseAuthManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

data class TimeHorizonData(
    val horizon: String, // "Day", "Week", "Month", "Year"
    val daysCount: Int,
    val combinedSteps: Long = 0L,
    val combinedGoal: Long = 20_000L,
    val combinedRatio: Float = 0f,
    val userSteps: Long = 0L,
    val userGoal: Long = 10_000L,
    val userRatio: Float = 0f,
    val userDailyAvg: Long = 0L,
    val userActiveMins: Long = 0L,
    val userCalories: Long = 0L,
    val partnerSteps: Long = 0L,
    val partnerGoal: Long = 10_000L,
    val partnerRatio: Float = 0f,
    val partnerDailyAvg: Long = 0L,
    val partnerActiveMins: Long = 0L,
    val partnerCalories: Long = 0L,
    val userSplitPercent: Int = 50,
    val partnerSplitPercent: Int = 50,
    val synchronyPercent: Int = 100,
    val totalActiveMins: Long = 0L,
    val totalCalories: Long = 0L
)

data class HealthTrendsUiState(
    val partnerName: String = "Partner",
    val dayData: TimeHorizonData = TimeHorizonData("Day", 1),
    val weekData: TimeHorizonData = TimeHorizonData("Week", 7, combinedGoal = 140_000L, userGoal = 70_000L, partnerGoal = 70_000L),
    val monthData: TimeHorizonData = TimeHorizonData("Month", 30, combinedGoal = 600_000L, userGoal = 300_000L, partnerGoal = 300_000L),
    val yearData: TimeHorizonData = TimeHorizonData("Year", 365, combinedGoal = 7_300_000L, userGoal = 3_650_000L, partnerGoal = 3_650_000L),
    val isLoading: Boolean = false
)

@HiltViewModel
class HealthTrendsViewModel @Inject constructor(
    private val healthDao: HealthDao,
    private val authManager: FirebaseAuthManager,
    private val preferencesDataStore: CoupleFitPreferencesDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(HealthTrendsUiState())
    val uiState: StateFlow<HealthTrendsUiState> = _uiState.asStateFlow()

    init {
        observePartnerName()
        observeTelemetryAggregates()
    }

    private fun observePartnerName() {
        viewModelScope.launch {
            preferencesDataStore.partnerNameFlow.collectLatest { name ->
                _uiState.value = _uiState.value.copy(
                    partnerName = name?.ifBlank { "Partner" } ?: "Partner"
                )
            }
        }
    }

    private fun observeTelemetryAggregates() {
        viewModelScope.launch {
            authManager.observeUserSession().collectLatest { session ->
                val partnerId = session?.partnerId ?: "partner_1"
                loadAllHorizons(partnerId)
            }
        }
    }

    private fun loadAllHorizons(partnerId: String) {
        val today = LocalDate.now()
        val iso = DateTimeFormatter.ISO_LOCAL_DATE

        // Day: today
        val dayStart = today.format(iso)
        val dayEnd = dayStart

        // Week: last 7 days
        val weekStart = today.minusDays(6).format(iso)
        val weekEnd = dayStart

        // Month: last 30 days
        val monthStart = today.minusDays(29).format(iso)
        val monthEnd = dayStart

        // Year: last 365 days
        val yearStart = today.minusDays(364).format(iso)
        val yearEnd = dayStart

        viewModelScope.launch {
            combine(
                healthDao.getDailyAggregatesBetweenFlow(yearStart, yearEnd),
                healthDao.getPartnerDailyBetweenFlow(partnerId, yearStart, yearEnd)
            ) { myHistory, partnerHistory ->
                val day = computeHorizonData("Day", 1, dayStart, dayEnd, myHistory, partnerHistory, 10_000L)
                val week = computeHorizonData("Week", 7, weekStart, weekEnd, myHistory, partnerHistory, 70_000L)
                val month = computeHorizonData("Month", 30, monthStart, monthEnd, myHistory, partnerHistory, 300_000L)
                val year = computeHorizonData("Year", 365, yearStart, yearEnd, myHistory, partnerHistory, 3_650_000L)

                _uiState.value = _uiState.value.copy(
                    dayData = day,
                    weekData = week,
                    monthData = month,
                    yearData = year
                )
            }.collect()
        }
    }

    private fun computeHorizonData(
        horizon: String,
        daysCount: Int,
        startDate: String,
        endDate: String,
        myHistory: List<com.couplefit.data.local.HealthDailyAggregateEntity>,
        partnerHistory: List<com.couplefit.data.local.PartnerDailyEntity>,
        userGoal: Long
    ): TimeHorizonData {
        val filteredMy = myHistory.filter { it.dateStr in startDate..endDate }
        val filteredPartner = partnerHistory.filter { it.dateStr in startDate..endDate }

        val userSteps = filteredMy.sumOf { it.totalSteps }
        val userActiveMins = filteredMy.sumOf { it.totalActiveMinutes }
        val userCalories = filteredMy.sumOf { it.totalCalories }

        val partnerSteps = filteredPartner.sumOf { it.steps }
        val partnerActiveMins = filteredPartner.sumOf { it.activeMinutes }
        val partnerCalories = filteredPartner.sumOf { it.calories }

        val combinedSteps = userSteps + partnerSteps
        val combinedGoal = userGoal * 2L

        val userDailyAvg = if (daysCount > 0) userSteps / daysCount else userSteps
        val partnerDailyAvg = if (daysCount > 0) partnerSteps / daysCount else partnerSteps

        val userRatio = (userSteps.toFloat() / userGoal.toFloat()).coerceIn(0f, 1.5f)
        val partnerRatio = (partnerSteps.toFloat() / userGoal.toFloat()).coerceIn(0f, 1.5f)
        val combinedRatio = (combinedSteps.toFloat() / combinedGoal.toFloat()).coerceIn(0f, 1.5f)

        val totalForSplit = combinedSteps
        val userSplitPercent = if (totalForSplit > 0L) {
            ((userSteps.toDouble() / totalForSplit.toDouble()) * 100).toInt().coerceIn(1, 99)
        } else {
            50
        }
        val partnerSplitPercent = if (totalForSplit > 0L) {
            100 - userSplitPercent
        } else {
            50
        }

        val synchronyPercent = if (userSteps > 0L && partnerSteps > 0L) {
            val r = min(userSteps, partnerSteps).toDouble() / max(userSteps, partnerSteps).toDouble()
            (r * 100).toInt().coerceIn(1, 100)
        } else if (userSteps == 0L && partnerSteps == 0L) {
            100
        } else {
            // One partner has walked, other has not yet
            val larger = max(userSteps, partnerSteps).toDouble()
            val penalty = ((larger / userGoal.toDouble()).coerceAtMost(1.0) * 50.0).toInt()
            (100 - penalty).coerceIn(15, 95)
        }

        return TimeHorizonData(
            horizon = horizon,
            daysCount = daysCount,
            combinedSteps = combinedSteps,
            combinedGoal = combinedGoal,
            combinedRatio = combinedRatio,
            userSteps = userSteps,
            userGoal = userGoal,
            userRatio = userRatio,
            userDailyAvg = userDailyAvg,
            userActiveMins = userActiveMins,
            userCalories = userCalories,
            partnerSteps = partnerSteps,
            partnerGoal = userGoal,
            partnerRatio = partnerRatio,
            partnerDailyAvg = partnerDailyAvg,
            partnerActiveMins = partnerActiveMins,
            partnerCalories = partnerCalories,
            userSplitPercent = userSplitPercent,
            partnerSplitPercent = partnerSplitPercent,
            synchronyPercent = synchronyPercent,
            totalActiveMins = userActiveMins + partnerActiveMins,
            totalCalories = userCalories + partnerCalories
        )
    }
}
