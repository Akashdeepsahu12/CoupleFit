package com.couplefit.ui.challenge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.couplefit.data.local.CoupleChallengeEntity
import com.couplefit.data.remote.FirebaseAuthManager
import com.couplefit.domain.repository.ChallengeRepository
import com.couplefit.util.RemoteConfigManager
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class ChallengeUiState(
    val challenges: List<CoupleChallengeEntity> = emptyList(),
    val activeChallenge: CoupleChallengeEntity? = null,
    val partnerName: String = "Partner",
    val coupleId: String = "couple_1",
    val partnerUid: String = "partner_1",
    val isLoading: Boolean = false,
    val userMessage: String? = null
)

@HiltViewModel
class ChallengeViewModel @Inject constructor(
    private val challengeRepository: ChallengeRepository,
    private val auth: FirebaseAuth,
    private val authManager: FirebaseAuthManager,
    private val remoteConfigManager: RemoteConfigManager,
    private val preferencesDataStore: com.couplefit.data.local.CoupleFitPreferencesDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChallengeUiState())
    val uiState: StateFlow<ChallengeUiState> = _uiState.asStateFlow()

    init {
        val initialSession = authManager.getCurrentSession()
        val initCoupleId = initialSession?.coupleId?.ifBlank { "couple_1" } ?: "couple_1"
        val initPartnerId = initialSession?.partnerId?.ifBlank { "partner_1" } ?: "partner_1"
        _uiState.value = _uiState.value.copy(coupleId = initCoupleId, partnerUid = initPartnerId)
        observeChallenges(initCoupleId, initPartnerId)

        viewModelScope.launch {
            authManager.observeUserSession().collectLatest { session ->
                if (session != null) {
                    val cId = session.coupleId?.ifBlank { "couple_1" } ?: "couple_1"
                    val pId = session.partnerId?.ifBlank { "partner_1" } ?: "partner_1"
                    if (cId != _uiState.value.coupleId || pId != _uiState.value.partnerUid) {
                        _uiState.value = _uiState.value.copy(coupleId = cId, partnerUid = pId)
                        observeChallenges(cId, pId)
                    }
                }
            }
        }

        viewModelScope.launch {
            preferencesDataStore.partnerNameFlow.collectLatest { name ->
                _uiState.value = _uiState.value.copy(partnerName = name?.ifBlank { "Partner" } ?: "Partner")
            }
        }
    }

    fun observeChallenges(coupleId: String = _uiState.value.coupleId, partnerUid: String = _uiState.value.partnerUid) {
        val myUid = auth.currentUser?.uid ?: "me"
        viewModelScope.launch {
            challengeRepository.listenToCoupleChallenges(coupleId, myUid, partnerUid)
                .collectLatest { list ->
                    val active = list.firstOrNull { it.status == "ACTIVE" }
                    _uiState.value = _uiState.value.copy(
                        challenges = list,
                        activeChallenge = active
                    )
                }
        }
    }

    fun createChallenge(
        coupleId: String = _uiState.value.coupleId,
        type: String = "STEPS_RACE",
        goal: Long? = null,
        durationDays: Long = 7
    ) {
        viewModelScope.launch {
            val myUid = auth.currentUser?.uid ?: "me"
            val targetGoal = goal ?: if (type == "STEPS_RACE") {
                remoteConfigManager.stepsRaceGoal
            } else {
                remoteConfigManager.activeMinutesStreakGoal
            }

            val today = LocalDate.now()
            val start = today.format(DateTimeFormatter.ISO_LOCAL_DATE)
            val end = today.plusDays(durationDays).format(DateTimeFormatter.ISO_LOCAL_DATE)

            val result = challengeRepository.createChallenge(
                coupleId = coupleId,
                type = type,
                goal = targetGoal,
                start = start,
                end = end,
                creatorUid = myUid
            )

            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    userMessage = "New ${if (type == "STEPS_RACE") "Steps Race" else "Active Streak"} challenge started! 🎯"
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    userMessage = "Could not start challenge. Please try again."
                )
            }
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(userMessage = null)
    }
}
