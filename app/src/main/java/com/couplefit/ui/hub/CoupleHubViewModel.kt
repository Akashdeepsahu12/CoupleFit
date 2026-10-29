package com.couplefit.ui.hub

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.couplefit.data.local.CoupleFitPreferencesDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CoupleHubUiState(
    val userName: String = "Akash",
    val partnerName: String = "Partner",
    val userBirthday: String = "June 14",
    val partnerBirthday: String = "September 28",
    val relationshipType: String = "Life Comrades & Fitness Partners ❤️",
    val pairedDateTimestamp: Long = System.currentTimeMillis() - 540L * 86_400_000L,
    val totalDaysPaired: Long = 540L
)

@HiltViewModel
class CoupleHubViewModel @Inject constructor(
    private val preferencesDataStore: CoupleFitPreferencesDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(CoupleHubUiState())
    val uiState: StateFlow<CoupleHubUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                preferencesDataStore.userNameFlow,
                preferencesDataStore.partnerNameFlow,
                preferencesDataStore.userBirthdayFlow,
                preferencesDataStore.partnerBirthdayFlow,
                preferencesDataStore.relationshipTypeFlow
            ) { user, partner, uBday, pBday, rel ->
                Triple(user, partner, Triple(uBday, pBday, rel))
            }.combine(preferencesDataStore.pairedDateTimestampFlow) { (user, partner, details), pairedTs ->
                val (uBday, pBday, rel) = details
                val days = ((System.currentTimeMillis() - pairedTs) / 86_400_000L).coerceAtLeast(1L)
                CoupleHubUiState(
                    userName = user?.ifBlank { "Akash" } ?: "Akash",
                    partnerName = partner?.ifBlank { "Partner" } ?: "Partner",
                    userBirthday = uBday.ifBlank { "June 14" },
                    partnerBirthday = pBday.ifBlank { "September 28" },
                    relationshipType = rel.ifBlank { "Life Comrades & Fitness Partners ❤️" },
                    pairedDateTimestamp = pairedTs,
                    totalDaysPaired = days
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun updateUserName(name: String) {
        viewModelScope.launch { preferencesDataStore.saveUserName(name) }
    }

    fun updatePartnerName(name: String) {
        viewModelScope.launch { preferencesDataStore.savePartnerName(name) }
    }

    fun updateUserBirthday(bday: String) {
        viewModelScope.launch { preferencesDataStore.saveUserBirthday(bday) }
    }

    fun updatePartnerBirthday(bday: String) {
        viewModelScope.launch { preferencesDataStore.savePartnerBirthday(bday) }
    }

    fun updateRelationshipType(type: String) {
        viewModelScope.launch { preferencesDataStore.saveRelationshipType(type) }
    }
}
