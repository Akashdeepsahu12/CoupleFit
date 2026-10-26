package com.couplefit.data.healthconnect

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.RemoteException
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.OxygenSaturationRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.couplefit.ui.components.ChartDataPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

data class AggregatedHealthData(
    val totalSteps: Long,
    val averageHeartRate: Double,
    val totalCalories: Long = 0L
)

@Singleton
class HealthConnectManager @Inject constructor(@ApplicationContext private val context: Context) {

    @Volatile
    private var _cachedClient: HealthConnectClient? = null

    // Internal constructor allowing direct injection of HealthConnectClient for testing
    internal constructor(context: Context, testClient: HealthConnectClient?) : this(context) {
        this._cachedClient = testClient
    }

    /**
     * Dynamically gets or initializes the HealthConnectClient if SDK is available.
     * Prevents caching null permanently if the user installs or updates Health Connect during app lifecycle.
     */
    fun getClient(): HealthConnectClient? {
        val current = _cachedClient
        if (current != null) return current
        if (isHealthConnectAvailable(context) == HealthConnectClient.SDK_AVAILABLE) {
            return synchronized(this) {
                _cachedClient ?: try {
                    HealthConnectClient.getOrCreate(context).also { _cachedClient = it }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to getOrCreate HealthConnectClient", e)
                    null
                }
            }
        }
        _cachedClient = null
        return null
    }

    private val _isSupported = MutableStateFlow(isHealthConnectAvailable(context) == HealthConnectClient.SDK_AVAILABLE)
    val isSupported: StateFlow<Boolean> = _isSupported.asStateFlow()

    /**
     * Re-queries availability status and updates isSupported flow.
     * Returns the HealthConnectClient SDK status integer:
     * - HealthConnectClient.SDK_AVAILABLE (3)
     * - HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED (2)
     * - HealthConnectClient.SDK_UNAVAILABLE (1)
     */
    fun checkAvailability(): Int {
        val status = isHealthConnectAvailable(context)
        val available = (status == HealthConnectClient.SDK_AVAILABLE)
        _isSupported.value = available
        if (!available) {
            _cachedClient = null
        }
        return status
    }

    val readPermissions = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class),
        HealthPermission.getReadPermission(OxygenSaturationRecord::class)
    )

    val permissions = readPermissions

    /**
     * Checks if all requested permissions (read + write) are granted.
     */
    suspend fun hasAllPermissions(): Boolean {
        val client = getClient() ?: return false
        return try {
            val granted = client.permissionController.getGrantedPermissions()
            granted.containsAll(permissions)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check permissions", e)
            false
        }
    }

    /**
     * Checks if ANY health permission is granted.
     * Allows syncing whatever data types the user agreed to grant rather than blocking completely.
     */
    suspend fun hasAnyPermissions(): Boolean {
        val client = getClient() ?: return false
        return try {
            val granted = client.permissionController.getGrantedPermissions()
            permissions.any { it in granted }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check permissions", e)
            false
        }
    }

    /**
     * Checks if essential read permissions (such as steps or heart rate) are granted.
     */
    suspend fun hasReadPermissions(): Boolean {
        val client = getClient() ?: return false
        return try {
            val granted = client.permissionController.getGrantedPermissions()
            granted.contains(HealthPermission.getReadPermission(StepsRecord::class)) ||
            granted.contains(HealthPermission.getReadPermission(HeartRateRecord::class))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check read permissions", e)
            false
        }
    }

    suspend fun getGrantedPermissions(): Set<String> {
        val client = getClient() ?: return emptySet()
        return try {
            client.permissionController.getGrantedPermissions()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get granted permissions", e)
            emptySet()
        }
    }

    suspend fun readSteps(startTime: Instant, endTime: Instant): List<StepsRecord> {
        val client = getClient() ?: return emptyList()
        val steps = mutableListOf<StepsRecord>()
        try {
            var pageToken: String? = null
            do {
                val request = ReadRecordsRequest(
                    recordType = StepsRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime),
                    pageToken = pageToken
                )
                val response = client.readRecords(request)
                if (response.records.isEmpty()) break
                steps.addAll(response.records)
                pageToken = response.pageToken
            } while (!pageToken.isNullOrBlank())
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException reading steps", e)
        } catch (e: RemoteException) {
            Log.e(TAG, "RemoteException reading steps", e)
        } catch (e: Exception) {
            Log.e(TAG, "Exception reading steps", e)
        }
        return steps
    }

    suspend fun readHeartRatePointsRaw(startTime: Instant, endTime: Instant): List<androidx.health.connect.client.records.HeartRateRecord.Sample> {
        val client = getClient() ?: return emptyList()
        val samples = mutableListOf<androidx.health.connect.client.records.HeartRateRecord.Sample>()
        try {
            var pageToken: String? = null
            do {
                val request = ReadRecordsRequest(
                    recordType = HeartRateRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime),
                    pageToken = pageToken
                )
                val response = client.readRecords(request)
                if (response.records.isEmpty()) break
                response.records.forEach { record ->
                    samples.addAll(record.samples)
                }
                pageToken = response.pageToken
            } while (!pageToken.isNullOrBlank())
        } catch (e: Exception) {
            Log.e(TAG, "Exception reading raw heart rate points", e)
        }
        return samples
    }

    suspend fun readOxygenSaturationPointsRaw(startTime: Instant, endTime: Instant): List<OxygenSaturationRecord> {
        val client = getClient() ?: return emptyList()
        val records = mutableListOf<OxygenSaturationRecord>()
        try {
            var pageToken: String? = null
            do {
                val request = ReadRecordsRequest(
                    recordType = OxygenSaturationRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime),
                    pageToken = pageToken
                )
                val response = client.readRecords(request)
                if (response.records.isEmpty()) break
                records.addAll(response.records)
                pageToken = response.pageToken
            } while (!pageToken.isNullOrBlank())
        } catch (e: Exception) {
            Log.e(TAG, "Exception reading oxygen saturation points", e)
        }
        return records
    }

    suspend fun readHeartRatePoints(startTime: Instant, endTime: Instant): List<ChartDataPoint> {
        val samples = readHeartRatePointsRaw(startTime, endTime)
        val formatter = DateTimeFormatter.ofPattern("h a").withZone(ZoneId.systemDefault())
        return samples.map { sample ->
            ChartDataPoint(formatter.format(sample.time), sample.beatsPerMinute.toFloat())
        }
    }
    
    suspend fun getAggregatedData(startTime: Instant, endTime: Instant): AggregatedHealthData {
        val client = getClient() ?: return AggregatedHealthData(0, 0.0, 0L)
        try {
            val granted = try {
                client.permissionController.getGrantedPermissions()
            } catch (e: Exception) {
                emptySet()
            }

            val filterByGranted = granted.isNotEmpty()
            val hasSteps = !filterByGranted || HealthPermission.getReadPermission(StepsRecord::class) in granted
            val hasHr = !filterByGranted || HealthPermission.getReadPermission(HeartRateRecord::class) in granted
            val hasCal = !filterByGranted || HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class) in granted

            val metricsToQuery = mutableSetOf<androidx.health.connect.client.aggregate.AggregateMetric<*>>()
            if (hasSteps) metricsToQuery.add(StepsRecord.COUNT_TOTAL)
            if (hasHr) metricsToQuery.add(HeartRateRecord.BPM_AVG)
            if (hasCal) metricsToQuery.add(TotalCaloriesBurnedRecord.ENERGY_TOTAL)

            if (metricsToQuery.isEmpty()) {
                return AggregatedHealthData(0, 0.0, 0L)
            }

            val response = client.aggregate(
                AggregateRequest(
                    metrics = metricsToQuery,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            
            val totalSteps = if (hasSteps) response[StepsRecord.COUNT_TOTAL] ?: 0L else 0L
            val averageHeartRate = if (hasHr) response[HeartRateRecord.BPM_AVG]?.toDouble() ?: 0.0 else 0.0
            val totalCalories = if (hasCal) {
                try {
                    response[TotalCaloriesBurnedRecord.ENERGY_TOTAL]?.inKilocalories?.toLong() ?: 0L
                } catch (e: Exception) {
                    0L
                }
            } else 0L
            
            return AggregatedHealthData(totalSteps, averageHeartRate, totalCalories)
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException during aggregation", e)
        } catch (e: RemoteException) {
            Log.e(TAG, "RemoteException during aggregation", e)
        } catch (e: Exception) {
            Log.e(TAG, "Exception during aggregation", e)
        }
        return AggregatedHealthData(0, 0.0, 0L)
    }

    suspend fun getChangesToken(
        recordTypes: Set<kotlin.reflect.KClass<out androidx.health.connect.client.records.Record>> = setOf(
            StepsRecord::class,
            HeartRateRecord::class,
            TotalCaloriesBurnedRecord::class,
            OxygenSaturationRecord::class
        )
    ): String? {
        val client = getClient() ?: return null
        return try {
            client.getChangesToken(
                androidx.health.connect.client.request.ChangesTokenRequest(recordTypes = recordTypes)
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get changes token", e)
            null
        }
    }

    suspend fun getChanges(changesToken: String): androidx.health.connect.client.response.ChangesResponse? {
        val client = getClient() ?: return null
        return try {
            client.getChanges(changesToken)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get changes for token", e)
            null
        }
    }

    /**
     * Replaces legacy last-24-hour sync with the Changes API:
     * - Uses getChangesToken / getChanges
     * - Handles token expiry with a full resync fallback
     * - Uses aggregate queries (StepsRecord.COUNT_TOTAL) to deduplicate phone and watch steps
     */
    suspend fun syncViaChangesApi(
        dataStore: com.couplefit.data.local.CoupleFitPreferencesDataStore,
        healthDao: com.couplefit.data.local.HealthDao
    ): Result<AggregatedHealthData> {
        val client = getClient() ?: return Result.failure(IllegalStateException("Health Connect is unavailable"))
        val now = Instant.now()
        val startOfDay = java.time.LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()
        val dateStr = java.time.LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

        try {
            var currentToken = dataStore.getChangesToken()
            var shouldFullResync = false

            if (currentToken == null) {
                Log.d(TAG, "Changes API: No token stored, performing initial full resync")
                shouldFullResync = true
            } else {
                val changesResponse = getChanges(currentToken)
                if (changesResponse == null || changesResponse.changesTokenExpired) {
                    Log.w(TAG, "Changes API: Token expired or invalid, triggering full resync")
                    shouldFullResync = true
                } else {
                    Log.d(TAG, "Changes API: Incremental update received (${changesResponse.changes.size} changes)")
                    dataStore.saveChangesToken(changesResponse.nextChangesToken)
                    shouldFullResync = changesResponse.changes.isNotEmpty()
                }
            }

            if (shouldFullResync) {
                val freshToken = getChangesToken()
                if (freshToken != null) {
                    dataStore.saveChangesToken(freshToken)
                }
            }

            // Deduplicate phone and watch step counts via aggregate query
            val aggregateData = getAggregatedData(startOfDay, now)
            val existingLocalAggregate = healthDao.getDailyAggregate(dateStr)
            val effectiveSteps = if (aggregateData.totalSteps == 0L && existingLocalAggregate != null && existingLocalAggregate.totalSteps > 0L) {
                existingLocalAggregate.totalSteps
            } else {
                aggregateData.totalSteps
            }
            val activeMins = (effectiveSteps / 100).coerceAtMost(180)
            val calories = if (aggregateData.totalCalories == 0L && existingLocalAggregate != null && existingLocalAggregate.totalCalories > 0L) {
                existingLocalAggregate.totalCalories
            } else {
                aggregateData.totalCalories
            }
            val avgHr = if (aggregateData.averageHeartRate == 0.0 && existingLocalAggregate != null && existingLocalAggregate.averageHeartRate > 0.0) {
                existingLocalAggregate.averageHeartRate
            } else {
                aggregateData.averageHeartRate
            }

            val hrPoints = readHeartRatePointsRaw(now.minus(24, ChronoUnit.HOURS), now)
            val o2Points = readOxygenSaturationPointsRaw(now.minus(24, ChronoUnit.HOURS), now)
            val avgO2 = if (o2Points.isNotEmpty()) {
                o2Points.map { it.percentage.value }.average()
            } else if (existingLocalAggregate != null && existingLocalAggregate.averageOxygenSaturation > 0.0) {
                existingLocalAggregate.averageOxygenSaturation
            } else 0.0

            // Persist deduplicated aggregate to Room
            healthDao.insertAggregate(
                com.couplefit.data.local.HealthDailyAggregateEntity(
                    dateStr = dateStr,
                    totalSteps = effectiveSteps,
                    totalActiveMinutes = activeMins,
                    totalCalories = calories,
                    averageHeartRate = avgHr,
                    averageOxygenSaturation = avgO2,
                    lastUpdatedTimestamp = now.toEpochMilli()
                )
            )

            val hrEntities = hrPoints.map { sample ->
                com.couplefit.data.local.HealthMetricEntity(
                    id = "HEART_RATE_${sample.time.toEpochMilli()}",
                    metricType = "HEART_RATE",
                    sourceTimestamp = sample.time.toEpochMilli(),
                    rawValue = sample.beatsPerMinute.toDouble(),
                    normalizedValue = sample.beatsPerMinute.toDouble()
                )
            }
            val o2Entities = o2Points.map { record ->
                com.couplefit.data.local.HealthMetricEntity(
                    id = "OXYGEN_SATURATION_${record.time.toEpochMilli()}",
                    metricType = "OXYGEN_SATURATION",
                    sourceTimestamp = record.time.toEpochMilli(),
                    rawValue = record.percentage.value,
                    normalizedValue = record.percentage.value
                )
            }
            if (hrEntities.isNotEmpty() || o2Entities.isNotEmpty()) {
                healthDao.insertMetrics(hrEntities + o2Entities)
            }

            return Result.success(aggregateData)
        } catch (e: Exception) {
            Log.e(TAG, "Error in syncViaChangesApi", e)
            return Result.failure(e)
        }
    }


    suspend fun diagnoseHealthConnect(timeRangeDays: Long = 7) {
        val client = getClient()
        if (client == null) {
            Log.d(TAG, "Diagnostic: Health Connect is NOT available.")
            return
        }
        
        Log.d(TAG, "Diagnostic: Health Connect is available.")
        
        val granted = getGrantedPermissions()
        Log.d(TAG, "Diagnostic: Granted Permissions: $granted")
        
        val endTime = Instant.now()
        val startTime = endTime.minus(timeRangeDays, ChronoUnit.DAYS)
        
        Log.d(TAG, "Diagnostic: Querying from $startTime to $endTime (UTC)")
        
        val steps = readSteps(startTime, endTime)
        Log.d(TAG, "Diagnostic: Found ${steps.size} step records.")
        
        if (steps.isEmpty()) {
            Log.w(TAG, "Diagnostic: No step records found. Please use the Health Connect Toolbox app to insert mock data and verify sync.")
        } else {
            steps.take(5).forEach { record ->
                Log.d(TAG, "Diagnostic: Step record - count: ${record.count}, start: ${record.startTime}, source: ${record.metadata.dataOrigin.packageName}")
            }
        }
    }

    suspend fun writeSampleSteps(count: Long = 1000L): Result<Unit> {
        val client = getClient() ?: return Result.failure(IllegalStateException("Health Connect is not available"))
        return try {
            val endTime = Instant.now()
            val startTime = endTime.minus(15, ChronoUnit.MINUTES)
            val zoneOffset = ZoneId.systemDefault().rules.getOffset(endTime)
            
            val record = StepsRecord(
                count = count,
                startTime = startTime,
                endTime = endTime,
                startZoneOffset = zoneOffset,
                endZoneOffset = zoneOffset,
                metadata = androidx.health.connect.client.records.metadata.Metadata.manualEntry()
            )
            client.insertRecords(listOf(record))
            Log.d(TAG, "Successfully wrote $count sample steps to Health Connect")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write sample steps to Health Connect", e)
            Result.failure(e)
        }
    }

    /**
     * Navigates to Health Connect settings or Google Play Store if an install/update is required.
     */
    fun openHealthConnect(activityContext: Context) {
        val status = checkAvailability()
        if (status == HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED || status == HealthConnectClient.SDK_UNAVAILABLE) {
            openPlayStore(activityContext)
        } else {
            openSettings(activityContext)
        }
    }

    /**
     * Opens the Health Connect settings screen.
     */
    fun openSettings(activityContext: Context) {
        try {
            val intent = Intent(HealthConnectClient.ACTION_HEALTH_CONNECT_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            activityContext.startActivity(intent)
        } catch (e: Exception) {
            Log.w(TAG, "ACTION_HEALTH_CONNECT_SETTINGS failed, trying generic intent", e)
            try {
                val intent = Intent("android.settings.HEALTH_CONNECT_SETTINGS").apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                activityContext.startActivity(intent)
            } catch (e2: Exception) {
                try {
                    val intent = Intent(android.provider.Settings.ACTION_SETTINGS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    activityContext.startActivity(intent)
                } catch (e3: Exception) {
                    Log.e(TAG, "Failed to open settings", e3)
                }
            }
        }
    }

    /**
     * Opens Google Play Store to install or update Health Connect with browser fallback.
     */
    fun openPlayStore(activityContext: Context) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("market://details?id=com.google.android.apps.healthdata")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            activityContext.startActivity(intent)
        } catch (e: Exception) {
            try {
                val webIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.healthdata")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                activityContext.startActivity(webIntent)
            } catch (e2: Exception) {
                Log.e(TAG, "Failed to open Play Store link", e2)
            }
        }
    }

    companion object {
        private const val TAG = "HealthConnectManager"
        fun isHealthConnectAvailable(context: Context): Int {
            return HealthConnectClient.getSdkStatus(context)
        }
    }
}
