package com.couplefit.ui.healthconnect

import android.content.Context
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.OxygenSaturationRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.couplefit.data.healthconnect.AggregatedHealthData
import com.couplefit.data.healthconnect.HealthConnectManager
import com.couplefit.data.local.HealthDao
import com.couplefit.ui.components.ChartDataPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject

data class HealthDashboardUiState(
    val steps: Long = 0L,
    val stepsGoal: Long = 10000L,
    val stepsProgress: Float = 0f,
    val activeMinutes: Long = 0L,
    val exerciseProgress: Float = 0f,
    val caloriesBurned: Long = 0L,
    val averageHeartRate: Double = 0.0,
    val heartRatePoints: List<ChartDataPoint> = emptyList(),
    val oxygenSaturationPoints: List<ChartDataPoint> = emptyList(),
    val isSyncing: Boolean = false,
    val isHealthConnectAvailable: Boolean = true,
    val healthConnectAvailabilityStatus: Int = HealthConnectClient.SDK_AVAILABLE,
    val hasPermissions: Boolean = false,
    val hasAllPermissions: Boolean = false,
    val lastSyncedAt: Instant? = null,
    val hasTodayData: Boolean = false
)

sealed class HealthConnectUiState {
    object Loading : HealthConnectUiState()
    object Empty : HealthConnectUiState()
    data class Success(val data: AggregatedHealthData, val lastSyncedAt: Instant) : HealthConnectUiState()
    object PermissionRevoked : HealthConnectUiState()
    object NotAvailable : HealthConnectUiState()
}

@HiltViewModel
class HealthConnectViewModel @Inject constructor(
    private val healthConnectManager: HealthConnectManager,
    private val healthDao: HealthDao
) : ViewModel() {

    private val _uiState = MutableStateFlow<HealthConnectUiState>(HealthConnectUiState.Loading)
    val uiState: StateFlow<HealthConnectUiState> = _uiState.asStateFlow()

    private val _dashboardState = MutableStateFlow(HealthDashboardUiState())
    val dashboardState: StateFlow<HealthDashboardUiState> = _dashboardState.asStateFlow()

    val permissions = healthConnectManager.permissions

    private var dbObservationJob: Job? = null
    
    init {
        checkStatusAndFetchData()
        startObservingLocalDb()
    }

    private fun startObservingLocalDb() {
        dbObservationJob?.cancel()
        dbObservationJob = viewModelScope.launch {
            val dateStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            
            // Observe Daily Aggregates from Room
            healthDao.getDailyAggregateFlow(dateStr).collectLatest { aggregate ->
                if (aggregate != null) {
                    val steps = aggregate.totalSteps
                    val goal = 10000L
                    val progress = (steps.toFloat() / goal.toFloat()).coerceIn(0f, 1f)
                    val activeMins = aggregate.totalActiveMinutes
                    val exProgress = (activeMins.toFloat() / 60f).coerceIn(0f, 1f)
                    val calories = aggregate.totalCalories
                    val hasData = steps > 0L || aggregate.averageHeartRate > 0.0
                    
                    _dashboardState.value = _dashboardState.value.copy(
                        steps = steps,
                        stepsProgress = progress,
                        activeMinutes = activeMins,
                        exerciseProgress = exProgress,
                        caloriesBurned = calories,
                        averageHeartRate = aggregate.averageHeartRate,
                        hasTodayData = hasData
                    )
                }
            }
        }
        
        viewModelScope.launch {
            val now = Instant.now()
            val start = now.minus(24, ChronoUnit.HOURS).toEpochMilli()
            val end = now.toEpochMilli()
            healthDao.getMetricsFlow("HEART_RATE", start, end).collectLatest { metrics ->
                val formatter = DateTimeFormatter.ofPattern("h a").withZone(ZoneId.systemDefault())
                val points = metrics.map { 
                    ChartDataPoint(formatter.format(Instant.ofEpochMilli(it.sourceTimestamp)), it.normalizedValue.toFloat())
                }
                _dashboardState.value = _dashboardState.value.copy(heartRatePoints = points)
            }
        }
        
        viewModelScope.launch {
            val now = Instant.now()
            val start = now.minus(24, ChronoUnit.HOURS).toEpochMilli()
            val end = now.toEpochMilli()
            healthDao.getMetricsFlow("OXYGEN_SATURATION", start, end).collectLatest { metrics ->
                val formatter = DateTimeFormatter.ofPattern("h a").withZone(ZoneId.systemDefault())
                val points = metrics.map { 
                    ChartDataPoint(formatter.format(Instant.ofEpochMilli(it.sourceTimestamp)), it.normalizedValue.toFloat())
                }
                _dashboardState.value = _dashboardState.value.copy(oxygenSaturationPoints = points)
            }
        }
    }

    fun checkStatusAndFetchData() {
        viewModelScope.launch {
            _dashboardState.value = _dashboardState.value.copy(isSyncing = true)
            _uiState.value = HealthConnectUiState.Loading
            
            val status = healthConnectManager.checkAvailability()
            val isSupported = (status == HealthConnectClient.SDK_AVAILABLE)
            _dashboardState.value = _dashboardState.value.copy(
                isHealthConnectAvailable = isSupported,
                healthConnectAvailabilityStatus = status
            )

            if (!isSupported) {
                _uiState.value = HealthConnectUiState.NotAvailable
                _dashboardState.value = _dashboardState.value.copy(isSyncing = false, hasPermissions = false)
                return@launch
            }

            val hasAnyPerms = healthConnectManager.hasAnyPermissions()
            val hasAllPerms = healthConnectManager.hasAllPermissions()
            _dashboardState.value = _dashboardState.value.copy(
                hasPermissions = hasAnyPerms,
                hasAllPermissions = hasAllPerms
            )

            if (!hasAnyPerms) {
                _uiState.value = HealthConnectUiState.PermissionRevoked
                _dashboardState.value = _dashboardState.value.copy(isSyncing = false)
                return@launch
            }

            syncFromHealthConnectToRoom()
        }
    }

    private suspend fun syncFromHealthConnectToRoom() {
        try {
            val now = Instant.now()
            val startOfDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()
            
            Log.d("Sync", "Starting Sync Cycle: Fetching from SDK. $startOfDay to $now")
            
            val granted = healthConnectManager.getGrantedPermissions()
            val hasStepsRead = granted.contains(HealthPermission.getReadPermission(StepsRecord::class))
            val hasHeartRateRead = granted.contains(HealthPermission.getReadPermission(HeartRateRecord::class))
            val hasO2Read = granted.contains(HealthPermission.getReadPermission(OxygenSaturationRecord::class))

            // Fetch aggregated data
            val aggregatedData = if (hasStepsRead || hasHeartRateRead) {
                healthConnectManager.getAggregatedData(startOfDay, now)
            } else {
                AggregatedHealthData(0L, 0.0, 0L)
            }

            val heartRatePoints = if (hasHeartRateRead) {
                healthConnectManager.readHeartRatePointsRaw(
                    startTime = now.minus(24, ChronoUnit.HOURS),
                    endTime = now
                )
            } else {
                emptyList()
            }
            
            val steps = aggregatedData.totalSteps
            val activeMins = (steps / 100).coerceAtMost(180)
            val calories = aggregatedData.totalCalories
            val dateStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            
            // Update Room Aggregate
            healthDao.insertAggregate(
                com.couplefit.data.local.HealthDailyAggregateEntity(
                    dateStr = dateStr,
                    totalSteps = steps,
                    totalActiveMinutes = activeMins,
                    totalCalories = calories,
                    averageHeartRate = aggregatedData.averageHeartRate,
                    lastUpdatedTimestamp = now.toEpochMilli()
                )
            )
            
            val oxygenSaturationPoints = if (hasO2Read) {
                healthConnectManager.readOxygenSaturationPointsRaw(
                    startTime = now.minus(24, ChronoUnit.HOURS),
                    endTime = now
                )
            } else {
                emptyList()
            }
            
            val hrEntities = heartRatePoints.map { sample ->
                val timestamp = sample.time.toEpochMilli()
                val id = "HEART_RATE_${timestamp}"
                com.couplefit.data.local.HealthMetricEntity(
                    id = id,
                    metricType = "HEART_RATE",
                    sourceTimestamp = timestamp,
                    rawValue = sample.beatsPerMinute.toDouble(),
                    normalizedValue = sample.beatsPerMinute.toDouble(),
                    syncedToRemote = false
                )
            }
            val oxEntities = oxygenSaturationPoints.map { record ->
                val timestamp = record.time.toEpochMilli()
                val id = "OXYGEN_SATURATION_${timestamp}"
                com.couplefit.data.local.HealthMetricEntity(
                    id = id,
                    metricType = "OXYGEN_SATURATION",
                    sourceTimestamp = timestamp,
                    rawValue = record.percentage.value,
                    normalizedValue = record.percentage.value,
                    syncedToRemote = false
                )
            }
            if (hrEntities.isNotEmpty() || oxEntities.isNotEmpty()) {
                healthDao.insertMetrics(hrEntities + oxEntities)
            }
            
            Log.d("Sync", "Sync Cycle Complete: Saved ${hrEntities.size} HR records and ${oxEntities.size} O2 records to Room.")

            _dashboardState.value = _dashboardState.value.copy(
                isSyncing = false,
                lastSyncedAt = now
            )
            
            _uiState.value = HealthConnectUiState.Success(data = aggregatedData, lastSyncedAt = now)
            
        } catch (e: Exception) {
            Log.e("Sync", "Partial/failed sync", e)
            _dashboardState.value = _dashboardState.value.copy(isSyncing = false)
            _uiState.value = HealthConnectUiState.Empty
        }
    }

    fun writeMockData(count: Long = 1000L) {
        viewModelScope.launch {
            healthConnectManager.writeSampleSteps(count)
            checkStatusAndFetchData()
        }
    }

    fun openHealthConnect(context: Context) {
        healthConnectManager.openHealthConnect(context)
    }

    fun openHealthConnectSettings(context: Context) {
        healthConnectManager.openSettings(context)
    }
}
