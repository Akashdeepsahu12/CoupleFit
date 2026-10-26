package com.couplefit.ui.network

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.couplefit.util.ConnectivityObserver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

/**
 * ViewModel managing reactive network connectivity state across the entire application.
 */
@HiltViewModel
class NetworkViewModel @Inject constructor(
    private val connectivityObserver: ConnectivityObserver
) : ViewModel() {

    // Tracks if the user explicitly dismissed the offline dialog during the current offline session
    private val _isDismissedByUser = MutableStateFlow(false)

    /**
     * Live internet connectivity state.
     * Uses WhileSubscribed(5000) to keep observing active across screen rotations / config changes.
     */
    val isConnected: StateFlow<Boolean> = connectivityObserver.isConnected
        .onEach { connected ->
            if (connected) {
                // When network reconnects, reset user dismiss flag so future drops show alert again
                _isDismissedByUser.value = false
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    /**
     * Determines whether the "No Internet Connection" dialog should be displayed.
     * Automatically becomes true when offline and not yet dismissed by the user.
     */
    val showOfflineDialog: StateFlow<Boolean> = combine(
        isConnected,
        _isDismissedByUser
    ) { connected, dismissed ->
        !connected && !dismissed
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    /**
     * Called when the user clicks "Dismiss" or "OK" on the offline dialog.
     * Allows them to browse cached/offline screens without repeated pop-up interruptions.
     */
    fun dismissOfflineDialog() {
        _isDismissedByUser.value = true
    }
}

