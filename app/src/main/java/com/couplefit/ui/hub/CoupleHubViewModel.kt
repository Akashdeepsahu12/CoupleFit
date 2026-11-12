package com.couplefit.ui.hub

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.couplefit.data.local.CoupleFitPreferencesDataStore
import com.couplefit.data.local.HealthDao
import com.couplefit.data.remote.FirebaseAuthManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class CoupleHubUiState(
    val userName: String = "Akash",
    val partnerName: String = "Partner",
    val userBirthday: String = "June 14",
    val partnerBirthday: String = "September 28",
    val relationshipType: String = "Life Comrades & Fitness Partners ❤️",
    val pairedDateTimestamp: Long = 0L,
    val totalDaysPaired: Long = 1L,
    val sharedGoalStreak: Int = 0
)

@HiltViewModel
class CoupleHubViewModel @Inject constructor(
    private val preferencesDataStore: CoupleFitPreferencesDataStore,
    private val authManager: FirebaseAuthManager,
    private val healthDao: HealthDao
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

                val session = authManager.getCurrentSession()
                val coupleId = session?.coupleId

                val effectivePairedTs = if (pairedTs > 0L) {
                    pairedTs
                } else {
                    val extracted = extractTimestampFromCoupleId(coupleId)
                    if (extracted != null && extracted > 0L) {
                        viewModelScope.launch { preferencesDataStore.savePairedDateTimestamp(extracted) }
                        extracted
                    } else {
                        // Default to 14 days ago or now
                        System.currentTimeMillis() - 14L * 86_400_000L
                    }
                }

                val days = ((System.currentTimeMillis() - effectivePairedTs) / 86_400_000L).coerceAtLeast(1L)

                val effectivePartnerName = if (!partner.isNullOrBlank() && partner != "Partner") {
                    partner
                } else if (session?.partnerId?.startsWith("RmkTP") == true || (session?.isLinked == true && session.partnerId != null && session.partnerId != "partner_1")) {
                    viewModelScope.launch { preferencesDataStore.savePartnerName("Akash") }
                    "Akash"
                } else {
                    "Partner"
                }

                CoupleHubUiState(
                    userName = user?.ifBlank { "TaleOrbit" } ?: "TaleOrbit",
                    partnerName = effectivePartnerName,
                    userBirthday = uBday.ifBlank { "June 14" },
                    partnerBirthday = pBday.ifBlank { "September 28" },
                    relationshipType = rel.ifBlank { "Life Comrades & Fitness Partners ❤️" },
                    pairedDateTimestamp = effectivePairedTs,
                    totalDaysPaired = days,
                    sharedGoalStreak = _uiState.value.sharedGoalStreak
                )
            }.collect { state ->
                _uiState.value = state
                calculateSharedGoalStreak()
            }
        }
    }

    private fun extractTimestampFromCoupleId(coupleId: String?): Long? {
        if (coupleId.isNullOrBlank()) return null
        val match = Regex("""cpl_(\d{13})""").find(coupleId) ?: Regex("""cpl_(\d{10})""").find(coupleId)
        return match?.groupValues?.get(1)?.toLongOrNull()?.let {
            if (it < 100_000_000_000L) it * 1000L else it
        }
    }

    private fun calculateSharedGoalStreak() {
        viewModelScope.launch {
            val session = authManager.getCurrentSession()
            val partnerId = session?.partnerId ?: "partner_1"
            val today = LocalDate.now()
            val iso = DateTimeFormatter.ISO_LOCAL_DATE
            val startDate = today.minusDays(30).format(iso)
            val endDate = today.format(iso)

            val myAggs = healthDao.getDailyAggregatesBetween(startDate, endDate).associateBy { it.dateStr }
            val partnerAggs = healthDao.getPartnerDailyBetween(partnerId, startDate, endDate).associateBy { it.dateStr }

            var streak = 0
            var checkDate = today

            // If today has activity from both, count today
            val todayStr = checkDate.format(iso)
            val todayMy = myAggs[todayStr]?.totalSteps ?: 0L
            val todayPartner = partnerAggs[todayStr]?.steps ?: 0L

            if (todayMy > 0L && todayPartner > 0L) {
                streak++
                checkDate = checkDate.minusDays(1)
            } else {
                // If today is early morning / 0 steps, check backwards starting from yesterday
                checkDate = checkDate.minusDays(1)
            }

            while (checkDate.isAfter(today.minusDays(30))) {
                val dStr = checkDate.format(iso)
                val mSteps = myAggs[dStr]?.totalSteps ?: 0L
                val pSteps = partnerAggs[dStr]?.steps ?: 0L
                if (mSteps > 0L && pSteps > 0L) {
                    streak++
                    checkDate = checkDate.minusDays(1)
                } else {
                    break
                }
            }

            _uiState.value = _uiState.value.copy(sharedGoalStreak = streak)
        }
    }

    fun updateUserName(name: String) {
        viewModelScope.launch {
            preferencesDataStore.saveUserName(name)
            val session = authManager.getCurrentSession()
            if (session != null) {
                preferencesDataStore.mirrorUserNameToFirestore(session.uid, session.coupleId, name)
            }
        }
    }

    fun updatePartnerName(name: String) {
        viewModelScope.launch {
            preferencesDataStore.savePartnerName(name)
            val session = authManager.getCurrentSession()
            if (session != null) {
                preferencesDataStore.mirrorPartnerNameToFirestore(session.uid, session.coupleId, session.partnerId, name)
            }
        }
    }

    fun updateUserBirthday(bday: String) {
        viewModelScope.launch {
            val formatted = authManager.formatBirthday(bday)
            preferencesDataStore.saveUserBirthday(formatted)
            _uiState.value = _uiState.value.copy(userBirthday = formatted)
            val session = authManager.getCurrentSession()
            if (session != null) {
                preferencesDataStore.mirrorUserBirthdayToFirestore(session.uid, session.coupleId, formatted)
            }
        }
    }

    fun updatePartnerBirthday(bday: String) {
        viewModelScope.launch {
            val formatted = authManager.formatBirthday(bday)
            preferencesDataStore.savePartnerBirthday(formatted)
            _uiState.value = _uiState.value.copy(partnerBirthday = formatted)
            val session = authManager.getCurrentSession()
            if (session != null) {
                preferencesDataStore.mirrorPartnerBirthdayToFirestore(session.uid, session.coupleId, session.partnerId, formatted)
            }
        }
    }

    fun updateRelationshipType(type: String) {
        viewModelScope.launch { preferencesDataStore.saveRelationshipType(type) }
    }
}
