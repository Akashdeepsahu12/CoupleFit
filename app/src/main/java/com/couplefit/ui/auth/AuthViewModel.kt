package com.couplefit.ui.auth

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.couplefit.data.model.AuthFlowStep
import com.couplefit.data.model.InviteResult
import com.couplefit.data.model.UserSession
import com.couplefit.data.remote.CoupleLinkManager
import com.couplefit.data.remote.FirebaseAuthManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val currentStep: AuthFlowStep = AuthFlowStep.SignIn,
    val userSession: UserSession? = null,
    val activeInvite: InviteResult? = null,
    val enteredCode: String = "",
    val errorMessage: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authManager: FirebaseAuthManager,
    private val coupleLinkManager: CoupleLinkManager,
    private val preferencesDataStore: com.couplefit.data.local.CoupleFitPreferencesDataStore,
    private val partnerRepository: com.couplefit.domain.repository.PartnerRepository
) : ViewModel() {

    private val initialSession = authManager.getCurrentSession()
    private val _uiState = MutableStateFlow(
        AuthUiState(
            userSession = initialSession,
            currentStep = when {
                initialSession == null -> AuthFlowStep.SignIn
                initialSession.isLinked && initialSession.partnerId != null && initialSession.coupleId != null -> AuthFlowStep.Dashboard
                else -> AuthFlowStep.InvitePartner
            }
        )
    )
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val userSession: StateFlow<UserSession?> = authManager.observeUserSession()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = initialSession
        )

    val customProfilePhotoUri: StateFlow<String?> = preferencesDataStore.customProfilePhotoUriFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val isSamsungConnected: StateFlow<Boolean> = preferencesDataStore.samsungHealthBridgeEnabledFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    fun updateProfilePhoto(uriString: String) {
        viewModelScope.launch {
            preferencesDataStore.saveCustomProfilePhotoUri(uriString)
            val uid = _uiState.value.userSession?.uid
            if (!uid.isNullOrBlank()) {
                try {
                    val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    firestore.collection("users").document(uid)
                        .set(mapOf("photoUrl" to uriString), com.google.firebase.firestore.SetOptions.merge())
                } catch (e: Exception) {
                    Timber.w(e, "Could not mirror custom photoUrl to Firestore")
                }
            }
        }
    }

    fun setSamsungHealthBridge(enabled: Boolean) {
        viewModelScope.launch {
            preferencesDataStore.setSamsungHealthBridgeEnabled(enabled)
        }
    }

    init {
        // Observe user session to automatically update flow step
        viewModelScope.launch {
            userSession.collect { session ->
                _uiState.value = _uiState.value.copy(userSession = session)
                if (session == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        currentStep = AuthFlowStep.SignIn
                    )
                } else if (session.isLinked && session.partnerId != null && session.coupleId != null) {
                    viewModelScope.launch {
                        preferencesDataStore.setLinkedConfirmationShown(true)
                    }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        currentStep = AuthFlowStep.Dashboard
                    )
                } else {
                    // Authenticated, but not yet linked to partner.
                    val pendingCode = _uiState.value.enteredCode
                    if (pendingCode.length == 6 && pendingCode.all { it.isDigit() }) {
                        redeemCode(pendingCode)
                    } else if (_uiState.value.currentStep is AuthFlowStep.SignIn) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            currentStep = AuthFlowStep.InvitePartner
                        )
                    }
                }
            }
        }
    }

    /**
     * Triggers Google Sign-In with Android Credential Manager.
     */
    fun signInWithGoogle(activity: Activity) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val result = authManager.signInWithGoogle(activity)
                result.onSuccess { session ->
                    Timber.d("Signed in as ${session.email}")
                    val pendingCode = _uiState.value.enteredCode
                    if (pendingCode.length == 6 && pendingCode.all { it.isDigit() } && !session.isLinked) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            userSession = session
                        )
                        redeemCode(pendingCode)
                    } else {
                        val nextStep = if (session.isLinked) {
                            AuthFlowStep.Dashboard
                        } else {
                            AuthFlowStep.InvitePartner
                        }
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            userSession = session,
                            currentStep = nextStep
                        )
                    }
                }.onFailure { error ->
                    Timber.e(error, "Sign in failed")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.localizedMessage ?: "Sign in failed. Please try again."
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Exception during signInWithGoogle")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.localizedMessage ?: "Sign in failed. Please try again."
                )
            } finally {
                if (_uiState.value.isLoading && _uiState.value.userSession != null) {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            }
        }
    }

    /**
     * Signs in as a Demo/Guest user for rapid testing without requiring
     * Google Cloud OAuth Web Client ID registration.
     */
    fun signInAsGuest(displayName: String = "Test User") {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val result = authManager.signInAsGuestOrDemo(displayName)
                result.onSuccess { session ->
                    Timber.d("Signed in as Guest/Demo: ${session.uid}")
                    val pendingCode = _uiState.value.enteredCode
                    if (pendingCode.length == 6 && pendingCode.all { it.isDigit() } && !session.isLinked) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            userSession = session
                        )
                        redeemCode(pendingCode)
                    } else {
                        // For Guest/Demo mode, automatically link a demo partner so the user lands
                        // directly on the Dashboard and can immediately explore steps, charts, and rings.
                        val activeSession = if (!session.isLinked) {
                            session.copy(
                                coupleId = "couple_demo",
                                partnerId = "partner_demo"
                            )
                        } else {
                            session
                        }
                        // Seed demo partner telemetry in Room for immediate visibility (PTR-01)
                        viewModelScope.launch {
                            val dateStr = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
                            partnerRepository.seedDemoPartnerDaily(activeSession.partnerId ?: "partner_demo", dateStr)
                        }
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            userSession = activeSession,
                            currentStep = AuthFlowStep.Dashboard
                        )
                    }
                }.onFailure { error ->
                    Timber.e(error, "Guest sign-in failed")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.localizedMessage ?: "Guest sign-in failed."
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Exception during signInAsGuest")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.localizedMessage ?: "Guest sign-in failed."
                )
            } finally {
                if (_uiState.value.isLoading && _uiState.value.userSession != null) {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            }
        }
    }

    /**
     * Immediately transitions into the dashboard with a demo couple session,
     * bypassing the pairing screen. Useful for single-device testing and exploration.
     */
    fun enterDemoDashboard() {
        val current = _uiState.value.userSession ?: com.couplefit.data.model.UserSession(
            uid = "demo_user_${System.currentTimeMillis().toString().takeLast(4)}",
            displayName = "Demo User",
            email = "demo@couplefit.app",
            coupleId = "couple_demo",
            partnerId = "partner_demo"
        )
        val linked = current.copy(
            coupleId = current.coupleId ?: "couple_demo",
            partnerId = current.partnerId ?: "partner_demo"
        )
        // Seed demo partner telemetry in Room for immediate visibility (PTR-01)
        viewModelScope.launch {
            val dateStr = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
            partnerRepository.seedDemoPartnerDaily(linked.partnerId ?: "partner_demo", dateStr)
        }
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            userSession = linked,
            currentStep = AuthFlowStep.Dashboard
        )
    }

    private var partnerLinkJob: Job? = null

    /**
     * Generates a 6-character partner invite code with 24-hour expiration.
     */
    fun generateInviteCode() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )
            val result = coupleLinkManager.createInvite()
            result.onSuccess { inviteResult ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    activeInvite = inviteResult,
                    currentStep = AuthFlowStep.InvitePartner,
                    successMessage = "Invite code generated!"
                )
                startListeningForPartnerLinking(inviteResult.coupleId, inviteResult.inviteCode)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.localizedMessage ?: "Failed to generate invite code"
                )
            }
        }
    }

    /**
     * Forces generation of a brand new 6-digit invite pairing code.
     */
    fun regenerateInviteCode() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = coupleLinkManager.createInvite()
            result.onSuccess { inviteResult ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    activeInvite = inviteResult,
                    currentStep = AuthFlowStep.InvitePartner,
                    successMessage = "New invite code generated!"
                )
                startListeningForPartnerLinking(inviteResult.coupleId, inviteResult.inviteCode)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.localizedMessage ?: "Failed to generate invite code"
                )
            }
        }
    }

    /**
     * Actively listens in real-time across Firestore couples/{coupleId}, invites/{code},
     * and users/{uid} collections to immediately detect when the partner enters and redeems the code.
     */
    fun startListeningForPartnerLinking(coupleId: String, inviteCode: String) {
        if (coupleId.isBlank() && inviteCode.isBlank()) return

        partnerLinkJob?.cancel()
        partnerLinkJob = viewModelScope.launch {
            val currentUid = _uiState.value.userSession?.uid
                ?: authManager.getCurrentSession()?.uid
                ?: ""

            Timber.i("Starting real-time partner linking observation for couple: $coupleId, code: $inviteCode, uid: $currentUid")

            // Immediate one-shot check in case partner redeemed before listener was attached
            checkManualPartnerLinkStatus(coupleId, inviteCode)

            coupleLinkManager.observePartnerLink(
                coupleId = coupleId,
                inviteCode = inviteCode,
                currentUid = currentUid
            ).collect { linkPair ->
                if (linkPair != null) {
                    val (resolvedCoupleId, partnerId) = linkPair
                    handlePartnerLinked(resolvedCoupleId, partnerId)
                }
            }
        }
    }

    /**
     * Completes partner link transition for both creator (Device A) and redeemer (Device B).
     */
    fun handlePartnerLinked(coupleId: String, partnerId: String) {
        val currentUid = _uiState.value.userSession?.uid
            ?: authManager.getCurrentSession()?.uid
            ?: ""

        Timber.i("Partner successfully linked: partnerId=$partnerId, coupleId=$coupleId")
        partnerLinkJob?.cancel()
        partnerLinkJob = null

        val currentSession = _uiState.value.userSession ?: authManager.getCurrentSession()
        val updatedSession = currentSession?.copy(
            coupleId = coupleId,
            partnerId = partnerId
        ) ?: UserSession(
            uid = currentUid.ifBlank { "user_me" },
            displayName = "Me",
            email = "user@couplefit.app",
            coupleId = coupleId,
            partnerId = partnerId
        )

        // Self-persist link bidirectionally in users collection
        viewModelScope.launch {
            coupleLinkManager.persistPartnerLinkLocallyAndRemote(
                myUid = updatedSession.uid,
                coupleId = coupleId,
                partnerId = partnerId
            )
        }

        _uiState.value = _uiState.value.copy(
            isLoading = false,
            userSession = updatedSession,
            currentStep = AuthFlowStep.Dashboard,
            errorMessage = null,
            successMessage = "Partner connected successfully!"
        )
    }

    /**
     * Performs a one-shot query to check if partner has linked,
     * used both by the heartbeat timer and user manual retry button.
     */
    fun checkManualPartnerLinkStatus(
        cplId: String? = _uiState.value.activeInvite?.coupleId,
        code: String? = _uiState.value.activeInvite?.inviteCode
    ) {
        val targetCoupleId = cplId ?: _uiState.value.activeInvite?.coupleId
        val targetCode = code ?: _uiState.value.activeInvite?.inviteCode
        if (targetCoupleId.isNullOrBlank() && targetCode.isNullOrBlank()) return

        val currentUid = _uiState.value.userSession?.uid
            ?: authManager.getCurrentSession()?.uid
            ?: ""

        viewModelScope.launch {
            val result = coupleLinkManager.checkPartnerLinkStatus(
                coupleId = targetCoupleId,
                inviteCode = targetCode,
                currentUid = currentUid
            )
            result.onSuccess { linkPair ->
                if (linkPair != null) {
                    val (resolvedCoupleId, partnerId) = linkPair
                    handlePartnerLinked(resolvedCoupleId, partnerId)
                }
            }
        }
    }

    /**
     * Redeems a 6-digit invite pairing code or direct Couple ID (cpl_...).
     */
    fun redeemCode(code: String) {
        val trimmed = code.trim()
        val isCoupleId = trimmed.startsWith("cpl_")
        val cleanCode = if (isCoupleId) trimmed else trimmed.filter { it.isDigit() }.take(6)
        if (!isCoupleId && cleanCode.length != 6) {
            _uiState.value = _uiState.value.copy(errorMessage = "Pairing code must be 6 digits or a Couple ID (cpl_...)")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = coupleLinkManager.redeemInvite(cleanCode)
            result.onSuccess { redeemResult ->
                val currentSession = _uiState.value.userSession ?: authManager.getCurrentSession()
                val updatedSession = currentSession?.copy(
                    coupleId = redeemResult.coupleId,
                    partnerId = redeemResult.partnerId
                ) ?: com.couplefit.data.model.UserSession(
                    uid = "user_me",
                    displayName = "Me",
                    email = "user@couplefit.app",
                    coupleId = redeemResult.coupleId,
                    partnerId = redeemResult.partnerId
                )

                // Self-persist link bidirectionally in users collection
                viewModelScope.launch {
                    coupleLinkManager.persistPartnerLinkLocallyAndRemote(
                        myUid = updatedSession.uid,
                        coupleId = redeemResult.coupleId,
                        partnerId = redeemResult.partnerId
                    )
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    userSession = updatedSession,
                    currentStep = AuthFlowStep.Dashboard,
                    errorMessage = null,
                    successMessage = "Successfully connected with partner!"
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.localizedMessage ?: "Invalid or expired invite code."
                )
            }
        }
    }

    /**
     * Handles App Link / Custom Scheme input:
     * - https://couplefit.app/invite/{code}
     * - couplefit://invite/{code}
     */
    fun onDeepLinkInviteCodeReceived(code: String) {
        val cleanCode = code.trim().filter { it.isDigit() }.take(6)
        if (cleanCode.length == 6) {
            _uiState.value = _uiState.value.copy(
                enteredCode = cleanCode,
                currentStep = AuthFlowStep.EnterCode
            )
            // Auto redeem if already authenticated
            if (userSession.value != null) {
                redeemCode(cleanCode)
            }
        }
    }

    fun navigateToStep(step: AuthFlowStep) {
        _uiState.value = _uiState.value.copy(currentStep = step, errorMessage = null)
        if (step is AuthFlowStep.Dashboard) {
            viewModelScope.launch {
                preferencesDataStore.setLinkedConfirmationShown(true)
            }
        }
        if (step is AuthFlowStep.InvitePartner) {
            val invite = _uiState.value.activeInvite
            if (invite == null) {
                generateInviteCode()
            } else {
                startListeningForPartnerLinking(invite.coupleId, invite.inviteCode)
            }
        }
    }

    fun updateEnteredCode(code: String) {
        val cleanCode = code.filter { it.isDigit() }.take(6)
        _uiState.value = _uiState.value.copy(enteredCode = cleanCode)
        // Automatically attempt pairing when all 6 digits are typed and user is authenticated
        if (cleanCode.length == 6 && userSession.value != null && !_uiState.value.isLoading) {
            redeemCode(cleanCode)
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }

    fun signOut() {
        authManager.signOut()
        viewModelScope.launch {
            preferencesDataStore.setLinkedConfirmationShown(false)
        }
        _uiState.value = AuthUiState(currentStep = AuthFlowStep.SignIn)
    }

    /**
     * Unlinks current partner and resets to Invite flow.
     */
    fun unlinkPartner(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            preferencesDataStore.setLinkedConfirmationShown(false)
            val result = coupleLinkManager.unlinkCouple()
            result.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    currentStep = AuthFlowStep.InvitePartner,
                    successMessage = "Successfully unlinked from partner."
                )
                onComplete()
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.localizedMessage ?: "Failed to unlink partner."
                )
            }
        }
    }

    /**
     * Complete GDPR/CCPA user data deletion: removes all cloud data, signs out,
     * and resets state back to Sign In.
     */
    fun deleteUserData(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            preferencesDataStore.setLinkedConfirmationShown(false)
            val result = coupleLinkManager.deleteUserData()
            result.onSuccess {
                authManager.signOut()
                _uiState.value = AuthUiState(
                    currentStep = AuthFlowStep.SignIn,
                    successMessage = "All account and fitness data permanently erased."
                )
                onComplete()
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.localizedMessage ?: "Failed to delete account data."
                )
            }
        }
    }
}
