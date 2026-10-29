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
import kotlinx.coroutines.flow.combine
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
    val hasTodayData: Boolean = false,
    // Connected Couple Profile Names (CPX-06)
    val userName: String = "You",
    // Partner Health Telemetry (Single Source of Truth: Room partnerDaily table)
    val partnerId: String = "partner_1",
    val partnerName: String = "Partner",
    val isPartnerLinked: Boolean = true,
    val partnerSteps: Long = 0L,
    val partnerStepsGoal: Long = 10000L,
    val partnerStepsProgress: Float = 0f,
    val partnerActiveMinutes: Long = 0L,
    val partnerExerciseProgress: Float = 0f,
    val partnerCaloriesBurned: Long = 0L,
    val partnerAverageHeartRate: Double = 0.0,
    val partnerAverageOxygenSaturation: Double = 0.0,
    val partnerHasTodayData: Boolean = false,
    val partnerLastUpdated: Long? = null,
    val partnerLastUpdatedLabel: String = "Awaiting sync",
    val coupleId: String = "couple_1",
    val myUid: String = "me",
    val nudgeSentMessage: String? = null
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
    private val healthDao: HealthDao,
    private val healthRepository: com.couplefit.domain.repository.HealthRepository,
    private val partnerRepository: com.couplefit.domain.repository.PartnerRepository,
    private val syncRepository: com.couplefit.domain.repository.SyncRepository,
    private val preferencesDataStore: com.couplefit.data.local.CoupleFitPreferencesDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow<HealthConnectUiState>(HealthConnectUiState.Loading)
    val uiState: StateFlow<HealthConnectUiState> = _uiState.asStateFlow()

    private val _dashboardState = MutableStateFlow(HealthDashboardUiState())
    val dashboardState: StateFlow<HealthDashboardUiState> = _dashboardState.asStateFlow()

    val permissions = healthConnectManager.permissions
    val sharingPreferences = preferencesDataStore.sharingPreferencesFlow

    private var dbObservationJob: Job? = null
    
    init {
        checkStatusAndFetchData()
        startObservingLocalDb()
        observeProfileNames()
    }

    private fun observeProfileNames() {
        viewModelScope.launch {
            combine(
                preferencesDataStore.userNameFlow,
                preferencesDataStore.partnerNameFlow
            ) { user, partner ->
                Pair(user?.ifBlank { "You" } ?: "You", partner?.ifBlank { "Partner" } ?: "Partner")
            }.collect { (user, partner) ->
                _dashboardState.value = _dashboardState.value.copy(
                    userName = user,
                    partnerName = partner
                )
            }
        }
    }

    private fun formatLastUpdated(timestamp: Long?): String {
        if (timestamp == null || timestamp == 0L) return "Awaiting sync"
        val diff = System.currentTimeMillis() - timestamp
        val seconds = (diff / 1000).coerceAtLeast(0)
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        return when {
            minutes < 1 -> "Last updated just now"
            minutes < 60 -> "Last updated ${minutes}m ago"
            hours < 24 -> "Last updated ${hours}h ago"
            else -> "Last updated ${days}d ago"
        }
    }

    private fun startObservingLocalDb() {
        dbObservationJob?.cancel()
        dbObservationJob = viewModelScope.launch {
            val dateStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            
            // Expose combine(myFlow, partnerFlow) from Room repository
            partnerRepository.getCombinedDashboardFlow(dateStr).collectLatest { combined ->
                val myAggregate = combined.myDaily
                val partnerDaily = combined.partnerDaily

                val steps = myAggregate?.totalSteps ?: 0L
                val goal = 10000L
                val progress = (steps.toFloat() / goal.toFloat()).coerceIn(0f, 1f)
                val activeMins = myAggregate?.totalActiveMinutes ?: 0L
                val exProgress = (activeMins.toFloat() / 60f).coerceIn(0f, 1f)
                val calories = myAggregate?.totalCalories ?: 0L
                val hasData = steps > 0L || (myAggregate?.averageHeartRate ?: 0.0) > 0.0

                val pSteps = partnerDaily?.steps ?: 0L
                val pProgress = (pSteps.toFloat() / goal.toFloat()).coerceIn(0f, 1f)
                val pActiveMins = partnerDaily?.activeMinutes ?: 0L
                val pExProgress = (pActiveMins.toFloat() / 60f).coerceIn(0f, 1f)
                val pCalories = partnerDaily?.calories ?: 0L
                val pAvgHr = partnerDaily?.avgHr ?: 0.0
                val pAvgSpo2 = partnerDaily?.avgSpo2 ?: 0.0
                val pHasData = partnerDaily != null && (pSteps > 0L || pAvgHr > 0.0)
                val pLastUpdated = partnerDaily?.lastUpdated
                val pLastUpdatedLabel = formatLastUpdated(pLastUpdated)

                _dashboardState.value = _dashboardState.value.copy(
                    steps = steps,
                    stepsProgress = progress,
                    activeMinutes = activeMins,
                    exerciseProgress = exProgress,
                    caloriesBurned = calories,
                    averageHeartRate = myAggregate?.averageHeartRate ?: 0.0,
                    hasTodayData = hasData,
                    partnerSteps = pSteps,
                    partnerStepsProgress = pProgress,
                    partnerActiveMinutes = pActiveMins,
                    partnerExerciseProgress = pExProgress,
                    partnerCaloriesBurned = pCalories,
                    partnerAverageHeartRate = pAvgHr,
                    partnerAverageOxygenSaturation = pAvgSpo2,
                    partnerHasTodayData = pHasData,
                    partnerLastUpdated = pLastUpdated,
                    partnerLastUpdatedLabel = pLastUpdatedLabel
                )
            }
        }

        // Listen to partner's Firestore document and stream to Room
        viewModelScope.launch {
            val dateStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            val coupleId = _dashboardState.value.coupleId
            val partnerId = _dashboardState.value.partnerId
            partnerRepository.listenToPartnerDaily(coupleId, partnerId, dateStr).collect {
                // PartnerRepository writes incoming snapshots into Room partnerDaily table
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
            val dateStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            
            Log.d("Sync", "Starting Sync Cycle: Fetching from SDK. $startOfDay to $now")
            
            val granted = healthConnectManager.getGrantedPermissions()
            val hasStepsRead = granted.contains(HealthPermission.getReadPermission(StepsRecord::class))
            val hasHeartRateRead = granted.contains(HealthPermission.getReadPermission(HeartRateRecord::class))
            val hasO2Read = granted.contains(HealthPermission.getReadPermission(OxygenSaturationRecord::class))

            // 1. Changes API sync: handles token expiry with full resync fallback and deduplicates steps via aggregate queries
            val changesSyncResult = healthConnectManager.syncViaChangesApi(preferencesDataStore, healthDao)
            val aggregatedData = if (changesSyncResult.isSuccess) {
                changesSyncResult.getOrNull() ?: AggregatedHealthData(0L, 0.0, 0L)
            } else {
                Log.w("Sync", "Changes API fallback to direct aggregate: ${changesSyncResult.exceptionOrNull()?.message}")
                if (hasStepsRead || hasHeartRateRead) {
                    healthConnectManager.getAggregatedData(startOfDay, now)
                } else {
                    AggregatedHealthData(0L, 0.0, 0L)
                }
            }

            // 2. Upload MY daily aggregate (steps, activeMinutes, calories, avgHr) to couples/{coupleId}/daily/{myUid_date}
            // strictly filtering for metrics enabled in sharing preferences, throttled to 15 minutes
            val coupleId = _dashboardState.value.coupleId
            val myUid = _dashboardState.value.myUid
            syncRepository.uploadMyDailyAggregate(coupleId, myUid, dateStr, force = false)

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

    /**
     * Called on app foreground:
     * Triggers syncOnAppForeground to upload MY aggregate and refresh Health Connect
     */
    fun onAppForeground() {
        viewModelScope.launch {
            val dateStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            val coupleId = _dashboardState.value.coupleId
            val myUid = _dashboardState.value.myUid
            syncRepository.syncOnAppForeground(coupleId, myUid, dateStr)
            checkStatusAndFetchData()
        }
    }

    // Per-metric sharing toggle handlers (DataStore + Firestore mirroring)
    fun setShareSteps(enabled: Boolean) {
        viewModelScope.launch { preferencesDataStore.setShareSteps(enabled, _dashboardState.value.myUid) }
    }
    fun setShareWorkouts(enabled: Boolean) {
        viewModelScope.launch { preferencesDataStore.setShareWorkouts(enabled, _dashboardState.value.myUid) }
    }
    fun setShareCalories(enabled: Boolean) {
        viewModelScope.launch { preferencesDataStore.setShareCalories(enabled, _dashboardState.value.myUid) }
    }
    fun setShareHr(enabled: Boolean) {
        viewModelScope.launch { preferencesDataStore.setShareHr(enabled, _dashboardState.value.myUid) }
    }
    fun setShareSpo2(enabled: Boolean) {
        viewModelScope.launch { preferencesDataStore.setShareSpo2(enabled, _dashboardState.value.myUid) }
    }

    fun sendNudgeToPartner(message: String = "Time to get active and close your rings! 🏃") {
        viewModelScope.launch {
            val state = _dashboardState.value
            val coupleId = state.coupleId
            val partnerUid = state.partnerId
            val myUid = state.myUid

            if (coupleId.isBlank() || partnerUid.isBlank()) {
                _dashboardState.value = _dashboardState.value.copy(
                    nudgeSentMessage = "Please link with your partner first!"
                )
                delay(2500)
                _dashboardState.value = _dashboardState.value.copy(nudgeSentMessage = null)
                return@launch
            }

            val result = partnerRepository.sendNudge(
                coupleId = coupleId,
                senderUid = myUid,
                senderName = "You",
                recipientUid = partnerUid,
                message = message
            )

            if (result.isSuccess) {
                _dashboardState.value = _dashboardState.value.copy(
                    nudgeSentMessage = "Sent a cheer to ${state.partnerName}! ❤️"
                )
            } else {
                _dashboardState.value = _dashboardState.value.copy(
                    nudgeSentMessage = "Could not deliver nudge right now."
                )
            }
            delay(2500)
            _dashboardState.value = _dashboardState.value.copy(nudgeSentMessage = null)
        }
    }


    fun writeMockData(count: Long = 1000L) {
        viewModelScope.launch {
            val now = Instant.now()
            val dateStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

            // 1. Read existing local Room aggregate or initialize
            val existing = healthDao.getDailyAggregate(dateStr)
            val updatedSteps = (existing?.totalSteps ?: 0L) + count
            val updatedActiveMinutes = (updatedSteps / 100).coerceAtMost(180)
            val updatedCalories = (existing?.totalCalories ?: 0L) + (count * 0.04).toLong()
            val avgHr = if ((existing?.averageHeartRate ?: 0.0) > 0.0) existing!!.averageHeartRate else 72.0
            val avgO2 = if ((existing?.averageOxygenSaturation ?: 0.0) > 0.0) existing!!.averageOxygenSaturation else 98.0

            val aggregateEntity = com.couplefit.data.local.HealthDailyAggregateEntity(
                dateStr = dateStr,
                totalSteps = updatedSteps,
                totalActiveMinutes = updatedActiveMinutes,
                totalCalories = updatedCalories,
                averageHeartRate = avgHr,
                averageOxygenSaturation = avgO2,
                lastUpdatedTimestamp = now.toEpochMilli()
            )
            healthDao.insertAggregate(aggregateEntity)

            // 2. Also insert a HealthMetricEntity for step records
            val metricEntity = com.couplefit.data.local.HealthMetricEntity(
                id = "STEPS_${now.toEpochMilli()}",
                metricType = "STEPS",
                sourceTimestamp = now.toEpochMilli(),
                rawValue = count.toDouble(),
                normalizedValue = count.toDouble(),
                syncedToRemote = false
            )
            healthDao.insertMetrics(listOf(metricEntity))

            // 3. Immediately reflect in UI State so user sees instant feedback
            val goal = _dashboardState.value.stepsGoal.coerceAtLeast(1L)
            _dashboardState.value = _dashboardState.value.copy(
                steps = updatedSteps,
                stepsProgress = (updatedSteps.toFloat() / goal.toFloat()).coerceIn(0f, 1f),
                activeMinutes = updatedActiveMinutes,
                exerciseProgress = (updatedActiveMinutes.toFloat() / 60f).coerceIn(0f, 1f),
                caloriesBurned = updatedCalories,
                averageHeartRate = avgHr,
                hasTodayData = true,
                lastSyncedAt = now
            )
            _uiState.value = HealthConnectUiState.Success(
                data = AggregatedHealthData(
                    totalSteps = updatedSteps,
                    averageHeartRate = avgHr,
                    totalCalories = updatedCalories
                ),
                lastSyncedAt = now
            )

            // 4. Try to write to Health Connect SDK if available & permitted (non-blocking)
            try {
                healthConnectManager.writeSampleSteps(count)
            } catch (e: Exception) {
                Log.w("HealthConnectVM", "Health Connect SDK write skipped or failed: ${e.message}")
            }

            // 5. Upload to remote partner sync so partner can see the mock steps too
            try {
                val coupleId = _dashboardState.value.coupleId
                val myUid = _dashboardState.value.myUid
                syncRepository.uploadMyDailyAggregate(coupleId, myUid, dateStr, force = true)
            } catch (e: Exception) {
                Log.w("HealthConnectVM", "Remote aggregate sync skipped: ${e.message}")
            }
        }
    }

    fun openHealthConnect(context: Context) {
        healthConnectManager.openHealthConnect(context)
    }

    fun openHealthConnectSettings(context: Context) {
        healthConnectManager.openSettings(context)
    }

    fun updateShareSteps(enabled: Boolean) {
        viewModelScope.launch {
            preferencesDataStore.setShareSteps(enabled, _dashboardState.value.myUid)
        }
    }

    fun updateShareWorkouts(enabled: Boolean) {
        viewModelScope.launch {
            preferencesDataStore.setShareWorkouts(enabled, _dashboardState.value.myUid)
        }
    }

    fun updateShareCalories(enabled: Boolean) {
        viewModelScope.launch {
            preferencesDataStore.setShareCalories(enabled, _dashboardState.value.myUid)
        }
    }

    fun updateShareHr(enabled: Boolean) {
        viewModelScope.launch {
            preferencesDataStore.setShareHr(enabled, _dashboardState.value.myUid)
        }
    }

    fun updateShareSpo2(enabled: Boolean) {
        viewModelScope.launch {
            preferencesDataStore.setShareSpo2(enabled, _dashboardState.value.myUid)
        }
    }
}
