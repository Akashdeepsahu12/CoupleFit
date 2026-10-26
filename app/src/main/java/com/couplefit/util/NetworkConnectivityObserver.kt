package com.couplefit.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reactive network connectivity observer utilizing Android's ConnectivityManager.
 *
 * It checks both [NetworkCapabilities.NET_CAPABILITY_INTERNET] and [NetworkCapabilities.NET_CAPABILITY_VALIDATED]
 * to ensure that the device not only connects to a network interface (e.g. Wi-Fi router)
 * but actually has working internet routing (e.g. not trapped in a captive portal or offline hotspot).
 */
@Singleton
class NetworkConnectivityObserver @Inject constructor(
    @ApplicationContext private val context: Context
) : ConnectivityObserver {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    override val isConnected: Flow<Boolean> = callbackFlow {
        // 1. Emit current status immediately upon subscription
        trySend(isCurrentlyConnected())

        // 2. Register reactive callback for real-time network capability changes
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                // When a network becomes available, wait for capabilities validation
                // or check current validated connection
                trySend(isCurrentlyConnected())
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                val hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                val isValidated = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                trySend(hasInternet && isValidated)
            }

            override fun onLost(network: Network) {
                // When a network is lost (e.g. Wi-Fi turned off), check if another network
                // is active and validated (e.g. fallback cellular data) to avoid false negatives.
                trySend(isCurrentlyConnected())
            }

            override fun onUnavailable() {
                trySend(false)
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(request, callback)

        // 3. Ensure proper cleanup when flow is cancelled
        awaitClose {
            try {
                connectivityManager.unregisterNetworkCallback(callback)
            } catch (e: Exception) {
                // Ignore if already unregistered
            }
        }
    }.distinctUntilChanged()

    /**
     * Synchronously checks if the active default network has validated internet access.
     */
    private fun isCurrentlyConnected(): Boolean {
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}

