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

    /**
     * Dynamically gets or initializes the HealthConnectClient if SDK is available.
     * Prevents caching null permanently if the user installs or updates Health Connect during app lifecycle.
     */
    fun getClient(): HealthConnectClient? {
        if (isHealthConnectAvailable(context) == HealthConnectClient.SDK_AVAILABLE) {
            val current = _cachedClient
            if (current != null) return current
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

    val permissions = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getWritePermission(StepsRecord::class),
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class),
        HealthPermission.getReadPermission(OxygenSaturationRecord::class)
    )

    val readPermissions = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class),
        HealthPermission.getReadPermission(OxygenSaturationRecord::class)
    )

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
                steps.addAll(response.records)
                pageToken = response.pageToken
            } while (pageToken != null)
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
                response.records.forEach { record ->
                    samples.addAll(record.samples)
                }
                pageToken = response.pageToken
            } while (pageToken != null)
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
                records.addAll(response.records)
                pageToken = response.pageToken
            } while (pageToken != null)
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
            val response = client.aggregate(
                AggregateRequest(
                    metrics = setOf(StepsRecord.COUNT_TOTAL, HeartRateRecord.BPM_AVG, TotalCaloriesBurnedRecord.ENERGY_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            
            val totalSteps = response[StepsRecord.COUNT_TOTAL] ?: 0L
            val averageHeartRate = response[HeartRateRecord.BPM_AVG]?.toDouble() ?: 0.0
            val totalCalories = response[TotalCaloriesBurnedRecord.ENERGY_TOTAL]?.inKilocalories?.toLong() ?: 0L
            
            return AggregatedHealthData(totalSteps, averageHeartRate, totalCalories)
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException during aggregation", e)
        } catch (e: RemoteException) {
            Log.e(TAG, "RemoteException during aggregation", e)
        } catch (e: Exception) {
            Log.e(TAG, "Exception during aggregation", e)
        }
        return AggregatedHealthData(0, 0.0)
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

    suspend fun writeSampleSteps(count: Long = 1000L) {
        val client = getClient() ?: return
        try {
            val endTime = Instant.now()
            val startTime = endTime.minus(15, ChronoUnit.MINUTES)
            
            val record = StepsRecord(
                count = count,
                startTime = startTime,
                endTime = endTime,
                startZoneOffset = null,
                endZoneOffset = null
            )
            client.insertRecords(listOf(record))
            Log.d(TAG, "Successfully wrote $count sample steps to Health Connect")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write sample steps", e)
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
