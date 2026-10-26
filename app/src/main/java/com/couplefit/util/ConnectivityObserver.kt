package com.couplefit.util

import kotlinx.coroutines.flow.Flow

/**
 * Interface representing a reactive connectivity observer.
 */
interface ConnectivityObserver {
    /**
     * Flow emitting true when a validated, working internet connection is present,
     * and false otherwise.
     */
    val isConnected: Flow<Boolean>
}

