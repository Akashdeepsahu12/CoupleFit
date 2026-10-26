package com.couplefit.data.samsunghealth

import android.app.Activity
import android.content.Context
import com.samsung.android.sdk.health.data.HealthDataService
import com.samsung.android.sdk.health.data.HealthDataStore
import com.samsung.android.sdk.health.data.permission.AccessType
import com.samsung.android.sdk.health.data.permission.Permission
import com.samsung.android.sdk.health.data.request.DataType
import com.samsung.android.sdk.health.data.request.DataTypes
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

enum class HealthConnectionState {
    DISCONNECTED, CONNECTING, CONNECTED, ERROR
}

@Singleton
class SamsungHealthManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val _connectionState = MutableStateFlow(HealthConnectionState.DISCONNECTED)
    val connectionState: StateFlow<HealthConnectionState> = _connectionState

    // Direct access to the Samsung Health Data Store
    val healthDataStore: HealthDataStore by lazy {
        HealthDataService.getStore(context)
    }

    fun connect() {
        _connectionState.value = HealthConnectionState.CONNECTING
        try {
            // Samsung Health Data Store service is initialized via HealthDataService.getStore
            _connectionState.value = HealthConnectionState.CONNECTED
        } catch (e: Exception) {
            e.printStackTrace()
            _connectionState.value = HealthConnectionState.ERROR
        }
    }

    fun disconnect() {
        _connectionState.value = HealthConnectionState.DISCONNECTED
    }

    /**
     * Request incremental permissions for specific data types
     */
    suspend fun requestPermissions(
        activity: Activity,
        dataTypes: Set<DataType>
    ): Set<Permission> {
        val permissions = dataTypes.map { Permission.of(it, AccessType.READ) }.toSet()
        return healthDataStore.requestPermissions(permissions, activity)
    }

    /**
     * Check if specific data types are already authorized
     */
    suspend fun getGrantedPermissions(
        dataTypes: Set<DataType>
    ): Set<Permission> {
        val permissions = dataTypes.map { Permission.of(it, AccessType.READ) }.toSet()
        return healthDataStore.getGrantedPermissions(permissions)
    }
}


