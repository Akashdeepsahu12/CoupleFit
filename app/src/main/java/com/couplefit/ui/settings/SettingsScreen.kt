package com.couplefit.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import android.os.PowerManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.health.connect.client.PermissionController
import androidx.hilt.navigation.compose.hiltViewModel
import com.couplefit.ui.components.AnimatedArrowBackIcon
import com.couplefit.ui.components.AnimatedCheckIcon
import com.couplefit.ui.components.AnimatedChevronRightIcon
import com.couplefit.ui.healthconnect.HealthConnectViewModel
import com.couplefit.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 4 Re-architected Settings Hubs per INFORMATION_ARCHITECTURE.md §3 & IA-03.
 */
enum class SettingsHub(val label: String, val icon: ImageVector) {
    PARTNERSHIP("Partnership", Icons.Default.Favorite),
    HEALTH_SYNC("Sync & Sharing", Icons.Default.Refresh),
    EXPERIENCE("Experience", Icons.Default.Settings),
    PRIVACY("Privacy & Safety", Icons.Default.Lock)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    isDarkTheme: Boolean,
    onToggleDarkTheme: (Boolean) -> Unit,
    onToggleDarkThemeWithOrigin: ((Boolean, Offset) -> Unit)? = null,
    reduceMotion: Boolean = false,
    onToggleReduceMotion: (Boolean) -> Unit = {},
    liquidGlassEnabled: Boolean = true,
    onToggleLiquidGlass: (Boolean) -> Unit = {},
    meshGradientEnabled: Boolean = false,
    onToggleMeshGradient: (Boolean) -> Unit = {},
    healthConnectViewModel: HealthConnectViewModel = hiltViewModel(),
    authViewModel: com.couplefit.ui.auth.AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val healthData by healthConnectViewModel.dashboardState.collectAsState()
    val sharingPrefs by healthConnectViewModel.sharingPreferences.collectAsState(initial = com.couplefit.data.local.MetricSharingPreferences())
    val authUiState by authViewModel.uiState.collectAsState()

    var activeHub by remember { mutableStateOf(SettingsHub.PARTNERSHIP) }
    var showPrivacyDetailScreen by remember { mutableStateOf(false) }
    var showUnlinkPartnerDialog by remember { mutableStateOf(false) }
    var unlinkConfirmationText by remember { mutableStateOf("") }
    var showSignOutDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showSamsungBridgeDialog by remember { mutableStateOf(false) }
    var showLicensesDialog by remember { mutableStateOf(false) }

    // Persistent & local UI toggles
    var isSamsungConnected by remember { mutableStateOf(false) }
    var syncFrequency by remember { mutableStateOf("Every 15 min") }
    var notifyCritical by remember { mutableStateOf(true) }
    var notifyNudges by remember { mutableStateOf(true) }
    var notifySummary by remember { mutableStateOf(true) }
    var quietHoursEnabled by remember { mutableStateOf(true) }
    var useMetric by remember { mutableStateOf(true) }
    var biometricLock by remember { mutableStateOf(false) }
    var savedBannerVisible by remember { mutableStateOf(false) }

    fun triggerAutoSaveNotice() {
        coroutineScope.launch {
            savedBannerVisible = true
            delay(1200)
            savedBannerVisible = false
        }
    }

    if (showPrivacyDetailScreen) {
        PrivacyTransparencyScreen(
            onNavigateBack = { showPrivacyDetailScreen = false },
            sharingPrefs = sharingPrefs,
            onUnlinkPartner = {
                authViewModel.unlinkPartner {
                    showPrivacyDetailScreen = false
                    onNavigateBack()
                }
            },
            onDeleteMyData = {
                authViewModel.deleteUserData {
                    showPrivacyDetailScreen = false
                    onNavigateBack()
                }
            },
            isUnlinking = authUiState.isLoading,
            isDeleting = authUiState.isLoading
        )
        return
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract()
    ) { _ ->
        healthConnectViewModel.checkStatusAndFetchData()
    }

    Scaffold(
        contentWindowInsets = WindowInsets.systemBars,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Settings",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        AnimatedVisibility(
                            visible = savedBannerVisible,
                            enter = fadeIn(tween(150)),
                            exit = fadeOut(tween(150))
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = SystemGreen.copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, SystemGreen.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    "Saved ✓",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SystemGreen,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        AnimatedArrowBackIcon(
                            modifier = Modifier.size(22.dp),
                            tint = MaterialTheme.colorScheme.onSurface,
                            reduceMotion = reduceMotion
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- 4-Hub Segmented Navigation Bar (IA-03) ---
            SettingsHubBar(
                activeHub = activeHub,
                onHubSelected = { activeHub = it },
                modifier = Modifier.readableMeasure(640.dp)
            )

            // --- Scrollable Hub Content Area ---
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .readableMeasure(640.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = AppSpacing.md, vertical = AppSpacing.xs),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
            ) {
                AnimatedContent(
                    targetState = activeHub,
                    transitionSpec = {
                        fadeIn(tween(200)) togetherWith fadeOut(tween(150))
                    },
                    label = "SettingsHubTransition"
                ) { hub ->
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
                    ) {
                        when (hub) {
                            SettingsHub.PARTNERSHIP -> {
                                PartnershipHubContent(
                                    authUiState = authUiState,
                                    partnerName = healthData.partnerName,
                                    isPartnerLinked = healthData.isPartnerLinked,
                                    onUnlinkClicked = { showUnlinkPartnerDialog = true },
                                    onSignOutClicked = { showSignOutDialog = true }
                                )
                            }
                            SettingsHub.HEALTH_SYNC -> {
                                HealthSyncHubContent(
                                    hasPermissions = healthData.hasPermissions,
                                    onManagePermissions = { permissionLauncher.launch(healthConnectViewModel.permissions) },
                                    isSamsungConnected = isSamsungConnected,
                                    onToggleSamsung = {
                                        isSamsungConnected = it
                                        if (it) showSamsungBridgeDialog = true
                                        triggerAutoSaveNotice()
                                    },
                                    syncFrequency = syncFrequency,
                                    onSyncFrequencyChange = {
                                        syncFrequency = it
                                        triggerAutoSaveNotice()
                                    },
                                    sharingPrefs = sharingPrefs,
                                    onToggleSteps = {
                                        healthConnectViewModel.updateShareSteps(it)
                                        triggerAutoSaveNotice()
                                    },
                                    onToggleWorkouts = {
                                        healthConnectViewModel.updateShareWorkouts(it)
                                        triggerAutoSaveNotice()
                                    },
                                    onToggleCalories = {
                                        healthConnectViewModel.updateShareCalories(it)
                                        triggerAutoSaveNotice()
                                    },
                                    onToggleHr = {
                                        healthConnectViewModel.updateShareHr(it)
                                        triggerAutoSaveNotice()
                                    },
                                    onToggleSpo2 = {
                                        healthConnectViewModel.updateShareSpo2(it)
                                        triggerAutoSaveNotice()
                                    }
                                )
                            }
                            SettingsHub.EXPERIENCE -> {
                                ExperienceHubContent(
                                    isDarkTheme = isDarkTheme,
                                    onToggleDarkTheme = onToggleDarkTheme,
                                    onToggleDarkThemeWithOrigin = onToggleDarkThemeWithOrigin,
                                    reduceMotion = reduceMotion,
                                    onToggleReduceMotion = {
                                        onToggleReduceMotion(it)
                                        triggerAutoSaveNotice()
                                    },
                                    liquidGlassEnabled = liquidGlassEnabled,
                                    onToggleLiquidGlass = {
                                        onToggleLiquidGlass(it)
                                        triggerAutoSaveNotice()
                                    },
                                    meshGradientEnabled = meshGradientEnabled,
                                    onToggleMeshGradient = {
                                        onToggleMeshGradient(it)
                                        triggerAutoSaveNotice()
                                    },
                                    notifyCritical = notifyCritical,
                                    onToggleNotifyCritical = {
                                        notifyCritical = it
                                        triggerAutoSaveNotice()
                                    },
                                    notifyNudges = notifyNudges,
                                    onToggleNotifyNudges = {
                                        notifyNudges = it
                                        triggerAutoSaveNotice()
                                    },
                                    notifySummary = notifySummary,
                                    onToggleNotifySummary = {
                                        notifySummary = it
                                        triggerAutoSaveNotice()
                                    },
                                    quietHoursEnabled = quietHoursEnabled,
                                    onToggleQuietHours = {
                                        quietHoursEnabled = it
                                        triggerAutoSaveNotice()
                                    },
                                    useMetric = useMetric,
                                    onToggleUseMetric = {
                                        useMetric = it
                                        triggerAutoSaveNotice()
                                    }
                                )
                            }
                            SettingsHub.PRIVACY -> {
                                PrivacyHubContent(
                                    biometricLock = biometricLock,
                                    onToggleBiometricLock = {
                                        biometricLock = it
                                        triggerAutoSaveNotice()
                                    },
                                    onOpenTransparency = { showPrivacyDetailScreen = true },
                                    onExportData = {
                                        triggerAutoSaveNotice()
                                    },
                                    onDeleteAccountClicked = { showDeleteAccountDialog = true }
                                )
                            }
                        }
                    }
                }

                // --- Section 5: Lean About & Support Footer (Always Accessible) ---
                AboutAndSupportFooter(
                    context = context,
                    onOpenLicenses = { showLicensesDialog = true }
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // --- Modal Dialogs ---

    // 2-Step Strict Unlink Partner Dialog (CSA-07)
    if (showUnlinkPartnerDialog) {
        AlertDialog(
            modifier = Modifier.dialogMeasure(),
            onDismissRequest = {
                showUnlinkPartnerDialog = false
                unlinkConfirmationText = ""
            },
            title = {
                Text(
                    "Unlink Partner?",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                    Text(
                        "Unlinking stops real-time telemetry sharing, closes all active duels, and freezes your couple streak.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "To confirm this permanent action, type \"UNLINK\" below:",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    OutlinedTextField(
                        value = unlinkConfirmationText,
                        onValueChange = { unlinkConfirmationText = it },
                        placeholder = { Text("Type UNLINK") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.error,
                            focusedLabelColor = MaterialTheme.colorScheme.error
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showUnlinkPartnerDialog = false
                        val text = unlinkConfirmationText
                        unlinkConfirmationText = ""
                        if (text.trim().uppercase() == "UNLINK") {
                            authViewModel.unlinkPartner {
                                onNavigateBack()
                            }
                        }
                    },
                    enabled = unlinkConfirmationText.trim().uppercase() == "UNLINK",
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        disabledContainerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
                    )
                ) {
                    Text("Permanently Unlink", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showUnlinkPartnerDialog = false
                        unlinkConfirmationText = ""
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Sign Out Dialog
    if (showSignOutDialog) {
        AlertDialog(
            modifier = Modifier.dialogMeasure(),
            onDismissRequest = { showSignOutDialog = false },
            title = { Text("Sign Out of CoupleFit") },
            text = { Text("Are you sure you want to sign out? Your local health telemetry will remain stored on this device.") },
            confirmButton = {
                Button(
                    onClick = {
                        showSignOutDialog = false
                        authViewModel.signOut()
                        onNavigateBack()
                    }
                ) {
                    Text("Sign Out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Permanent Data & Account Deletion Dialog
    if (showDeleteAccountDialog) {
        AlertDialog(
            modifier = Modifier.dialogMeasure(),
            onDismissRequest = { showDeleteAccountDialog = false },
            title = { Text("Delete Account & Cloud Telemetry") },
            text = { Text("This will permanently purge your health aggregates, cloud connection, and couple streaks from Firestore. This action cannot be reversed.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteAccountDialog = false
                        authViewModel.deleteUserData {
                            onNavigateBack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SystemRed)
                ) {
                    Text("Delete Forever", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Samsung Health Bridge Confirmation Dialog
    if (showSamsungBridgeDialog) {
        AlertDialog(
            modifier = Modifier.dialogMeasure(),
            onDismissRequest = { showSamsungBridgeDialog = false },
            icon = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(SystemGreen.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedCheckIcon(
                        modifier = Modifier.size(24.dp),
                        tint = SystemGreen,
                        reduceMotion = reduceMotion
                    )
                }
            },
            title = {
                Text("Samsung Bridge Active", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("Samsung Health and Health Connect telemetry are bridged with CoupleFit.")
            },
            confirmButton = {
                Button(onClick = { showSamsungBridgeDialog = false }) {
                    Text("Done")
                }
            }
        )
    }

    // Open Source Licenses Dialog
    if (showLicensesDialog) {
        AlertDialog(
            modifier = Modifier.dialogMeasure(minWidth = 340.dp, maxWidth = 580.dp),
            onDismissRequest = { showLicensesDialog = false },
            title = { Text("Open Source Acknowledgments") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("• AndroidX Jetpack Compose (Apache 2.0)")
                    Text("• Health Connect Client SDK (Apache 2.0)")
                    Text("• Google Firebase SDKs (Apache 2.0)")
                    Text("• Kotlin Coroutines & Flow (Apache 2.0)")
                    Text("• Hilt Dependency Injection (Apache 2.0)")
                }
            },
            confirmButton = {
                TextButton(onClick = { showLicensesDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

// ==========================================
// 4 COMPOSABLE HUBS IMPLEMENTATION (IA-03)
// ==========================================

@Composable
internal fun SettingsHubBar(
    activeHub: SettingsHub,
    onHubSelected: (SettingsHub) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = AppSpacing.md, vertical = AppSpacing.xs),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SettingsHub.values().forEach { hub ->
            val isSelected = activeHub == hub
            val haptic = LocalHapticFeedback.current
            FilterChip(
                selected = isSelected,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onHubSelected(hub)
                },
                leadingIcon = {
                    Icon(
                        imageVector = hub.icon,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                label = {
                    Text(
                        text = hub.label,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 13.sp
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

@Composable
internal fun PartnershipHubContent(
    authUiState: com.couplefit.ui.auth.AuthUiState,
    partnerName: String,
    isPartnerLinked: Boolean,
    onUnlinkClicked: () -> Unit,
    onSignOutClicked: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .iosGlass(shape = RoundedCornerShape(AppSpacing.md)),
        shape = RoundedCornerShape(AppSpacing.md),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = null
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            Text(
                "USER PROFILE",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Column {
                    Text(
                        authUiState.userSession?.displayName?.ifBlank { "CoupleFit User" } ?: "CoupleFit User",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        authUiState.userSession?.email?.ifBlank { "Synced with Firebase Auth" } ?: "Synced with Firebase Auth",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .iosGlass(shape = RoundedCornerShape(AppSpacing.md)),
        shape = RoundedCornerShape(AppSpacing.md),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = null
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            Text(
                "PARTNER CONNECTION",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Linked with $partnerName",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        if (isPartnerLinked) "Mutual bi-directional telemetry active" else "Awaiting partner connection",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = (if (isPartnerLinked) SystemGreen else SystemOrange).copy(alpha = 0.2f)
                ) {
                    Text(
                        if (isPartnerLinked) "Active 🟢" else "Pending 🟡",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isPartnerLinked) SystemGreen else SystemOrange,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            OutlinedButton(
                onClick = onSignOutClicked,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign Out")
            }
        }
    }

    // Danger Zone
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppSpacing.md),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            Text(
                "DANGER ZONE",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
            Text(
                "Disconnecting unlinks your accounts and halts all shared metrics.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = onUnlinkClicked,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Unlink Partner (2-Step Verification)", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
internal fun HealthSyncHubContent(
    hasPermissions: Boolean,
    onManagePermissions: () -> Unit,
    isSamsungConnected: Boolean,
    onToggleSamsung: (Boolean) -> Unit,
    syncFrequency: String,
    onSyncFrequencyChange: (String) -> Unit,
    sharingPrefs: com.couplefit.data.local.MetricSharingPreferences,
    onToggleSteps: (Boolean) -> Unit,
    onToggleWorkouts: (Boolean) -> Unit,
    onToggleCalories: (Boolean) -> Unit,
    onToggleHr: (Boolean) -> Unit,
    onToggleSpo2: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val powerManager = remember { context.getSystemService(android.content.Context.POWER_SERVICE) as? PowerManager }
    var isIgnoringBattery by remember {
        mutableStateOf(powerManager?.isIgnoringBatteryOptimizations(context.packageName) ?: false)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .iosGlass(shape = RoundedCornerShape(AppSpacing.md)),
        shape = RoundedCornerShape(AppSpacing.md),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = null
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            Text(
                "CORE INGESTION PROVIDER",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Google Health Connect",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        if (hasPermissions) "Read permissions active" else "Action needed: Grant permissions",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = (if (hasPermissions) SystemGreen else SystemOrange).copy(alpha = 0.2f)
                ) {
                    Text(
                        if (hasPermissions) "Connected ✓" else "Action Required",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (hasPermissions) SystemGreen else SystemOrange,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            OutlinedButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onManagePermissions()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Manage Health Permissions (10 Metrics)")
            }

            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Background Running & Sync",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        if (isIgnoringBattery) "Unrestricted background sync active" else "Allow background telemetry sync when app is closed",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        try {
                            val intent = Intent(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                data = Uri.parse("package:${context.packageName}")
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            try {
                                val intent = Intent(android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isIgnoringBattery) SystemGreen.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primaryContainer,
                        contentColor = if (isIgnoringBattery) SystemGreen else MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (isIgnoringBattery) "Granted ✓" else "Request", fontSize = 12.sp)
                }
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .iosGlass(shape = RoundedCornerShape(AppSpacing.md)),
        shape = RoundedCornerShape(AppSpacing.md),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = null
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            Text(
                "WEARABLE BRIDGES & SCHEDULE",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            SettingsSwitchRow(
                title = "Samsung Health Bridge",
                subtitle = "Sync Galaxy Watch privileged biometric telemetry",
                checked = isSamsungConnected,
                onCheckedChange = onToggleSamsung
            )
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            Text(
                "Background Sync Schedule",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("Every 15 min", "Hourly", "Manual").forEach { freq ->
                    val isFreqSelected = syncFrequency == freq
                    FilterChip(
                        selected = isFreqSelected,
                        onClick = { onSyncFrequencyChange(freq) },
                        label = { Text(freq, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
        }
    }

    // Granular Metric Sharing (DataStore-backed!)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .iosGlass(shape = RoundedCornerShape(AppSpacing.md)),
        shape = RoundedCornerShape(AppSpacing.md),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = null
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
        ) {
            Text(
                "GRANULAR PARTNER METRIC SHARING",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            SettingsSwitchRow(
                title = "Share Daily Step Count",
                subtitle = "Appears on partner dual activity ring",
                checked = sharingPrefs.shareSteps,
                onCheckedChange = onToggleSteps
            )
            SettingsSwitchRow(
                title = "Share Active Workouts",
                subtitle = "Workout duration and exercise progress",
                checked = sharingPrefs.shareWorkouts,
                onCheckedChange = onToggleWorkouts
            )
            SettingsSwitchRow(
                title = "Share Caloric Burn",
                subtitle = "Active energy expenditure",
                checked = sharingPrefs.shareCalories,
                onCheckedChange = onToggleCalories
            )
            SettingsSwitchRow(
                title = "Share Resting Heart Rate",
                subtitle = "Average pulse and intraday trends",
                checked = sharingPrefs.shareHr,
                onCheckedChange = onToggleHr
            )
            SettingsSwitchRow(
                title = "Share Blood Oxygen (SpO2)",
                subtitle = "Oxygen saturation percentage",
                checked = sharingPrefs.shareSpo2,
                onCheckedChange = onToggleSpo2
            )
        }
    }
}

@Composable
internal fun ExperienceHubContent(
    isDarkTheme: Boolean,
    onToggleDarkTheme: (Boolean) -> Unit,
    onToggleDarkThemeWithOrigin: ((Boolean, Offset) -> Unit)?,
    reduceMotion: Boolean,
    onToggleReduceMotion: (Boolean) -> Unit,
    liquidGlassEnabled: Boolean = true,
    onToggleLiquidGlass: (Boolean) -> Unit = {},
    meshGradientEnabled: Boolean = false,
    onToggleMeshGradient: (Boolean) -> Unit = {},
    notifyCritical: Boolean,
    onToggleNotifyCritical: (Boolean) -> Unit,
    notifyNudges: Boolean,
    onToggleNotifyNudges: (Boolean) -> Unit,
    notifySummary: Boolean,
    onToggleNotifySummary: (Boolean) -> Unit,
    quietHoursEnabled: Boolean,
    onToggleQuietHours: (Boolean) -> Unit,
    useMetric: Boolean,
    onToggleUseMetric: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .iosGlass(shape = RoundedCornerShape(AppSpacing.md)),
        shape = RoundedCornerShape(AppSpacing.md),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = null
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
        ) {
            Text(
                "APPEARANCE & MOTION",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            SettingsSwitchRow(
                title = "Dark Theme",
                subtitle = "OLED Obsidian high-contrast palette",
                checked = isDarkTheme,
                onCheckedChangeWithOrigin = onToggleDarkThemeWithOrigin,
                onCheckedChange = onToggleDarkTheme
            )
            SettingsSwitchRow(
                title = "Reduce Motion",
                subtitle = "Disable intensive spring sweeps and continuous pulses",
                checked = reduceMotion,
                onCheckedChange = onToggleReduceMotion
            )
            val isLiquidGlassSupported = remember { android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S }
            SettingsSwitchRow(
                title = "Liquid Glass Navigation Bar",
                subtitle = if (isLiquidGlassSupported) {
                    "Frosted glass effect on the floating bottom navigation bar"
                } else {
                    "Requires Android 12 or later"
                },
                checked = liquidGlassEnabled,
                onCheckedChange = { if (isLiquidGlassSupported) onToggleLiquidGlass(it) },
                enabled = isLiquidGlassSupported
            )
            SettingsSwitchRow(
                title = "Expo Mesh Gradient Background",
                subtitle = if (meshGradientEnabled) {
                    "Fluid living mesh gradient canvas (expo-mesh-gradient)"
                } else {
                    "Ambient video wallpaper"
                },
                checked = meshGradientEnabled,
                onCheckedChange = onToggleMeshGradient
            )
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .iosGlass(shape = RoundedCornerShape(AppSpacing.md)),
        shape = RoundedCornerShape(AppSpacing.md),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = null
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
        ) {
            Text(
                "NOTIFICATIONS & ALERTS",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            SettingsSwitchRow(
                title = "Partner Nudges & Cheers",
                subtitle = "High-priority push notifications",
                checked = notifyNudges,
                onCheckedChange = onToggleNotifyNudges
            )
            SettingsSwitchRow(
                title = "Milestone Celebrations",
                subtitle = "Ring closure and 10k step alerts",
                checked = notifySummary,
                onCheckedChange = onToggleNotifySummary
            )
            SettingsSwitchRow(
                title = "Critical Biometric Alerts",
                subtitle = "Urgent high/low pulse notifications",
                checked = notifyCritical,
                onCheckedChange = onToggleNotifyCritical
            )
            SettingsSwitchRow(
                title = "Quiet Hours (22:00 – 07:00)",
                subtitle = "Mute non-critical alerts during sleep",
                checked = quietHoursEnabled,
                onCheckedChange = onToggleQuietHours
            )
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .iosGlass(shape = RoundedCornerShape(AppSpacing.md)),
        shape = RoundedCornerShape(AppSpacing.md),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = null
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
        ) {
            Text(
                "MEASUREMENT SYSTEM",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            SettingsSwitchRow(
                title = "Metric System (km, kg)",
                subtitle = "Switch OFF for Imperial units (mi, lbs)",
                checked = useMetric,
                onCheckedChange = onToggleUseMetric
            )
        }
    }
}

@Composable
internal fun PrivacyHubContent(
    biometricLock: Boolean,
    onToggleBiometricLock: (Boolean) -> Unit,
    onOpenTransparency: () -> Unit,
    onExportData: () -> Unit,
    onDeleteAccountClicked: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .iosGlass(shape = RoundedCornerShape(AppSpacing.md)),
        shape = RoundedCornerShape(AppSpacing.md),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = null
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            Text(
                "DATA RIGHTS & TRANSPARENCY",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            SettingsClickableRow(
                text = "Partner Data Transparency & Shared Payloads [Inspect →]",
                onClick = onOpenTransparency
            )
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            SettingsSwitchRow(
                title = "Biometric App Lock",
                subtitle = "Require fingerprint/face to open CoupleFit",
                checked = biometricLock,
                onCheckedChange = onToggleBiometricLock
            )
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            SettingsClickableRow(
                text = "Export My Telemetry Data (GDPR/CCPA JSON)",
                onClick = onExportData
            )
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppSpacing.md),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            Text(
                "PERMANENT ACCOUNT PURGE",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
            Text(
                "Erase your account, local Room database, and all cloud telemetry.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = onDeleteAccountClicked,
                colors = ButtonDefaults.buttonColors(containerColor = SystemRed),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Delete My Account & Telemetry", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
internal fun AboutAndSupportFooter(
    context: android.content.Context,
    onOpenLicenses: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppSpacing.md),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                "ABOUT & SUPPORT",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "CoupleFit v2.2.0 • Build 20261027",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Offline-First Dual Telemetry Platform",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Privacy Policy",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable {
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://couplefit.app/privacy"))
                        context.startActivity(browserIntent)
                    }
                )
                Text(
                    "Open Source",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onOpenLicenses() }
                )
            }
        }
    }
}

// ==========================================
// REUSABLE ROW UTILITIES
// ==========================================

@Composable
fun SettingsSwitchRow(
    title: String,
    checked: Boolean,
    subtitle: String? = null,
    enabled: Boolean = true,
    onCheckedChangeWithOrigin: ((Boolean, Offset) -> Unit)? = null,
    onCheckedChange: (Boolean) -> Unit
) {
    var switchOrigin by remember { mutableStateOf(Offset.Zero) }

    val haptic = LocalHapticFeedback.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                if (onCheckedChangeWithOrigin != null) {
                    onCheckedChangeWithOrigin(!checked, switchOrigin)
                } else {
                    onCheckedChange(!checked)
                }
            }
            .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = checked,
            enabled = enabled,
            onCheckedChange = { newValue ->
                if (onCheckedChangeWithOrigin != null) {
                    onCheckedChangeWithOrigin(newValue, switchOrigin)
                } else {
                    onCheckedChange(newValue)
                }
            },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = SystemGreen,
                uncheckedTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
            ),
            modifier = Modifier.onGloballyPositioned { coordinates ->
                val bounds = coordinates.boundsInWindow()
                switchOrigin = Offset(bounds.center.x, bounds.center.y)
            }
        )
    }
}

@Composable
fun SettingsClickableRow(
    text: String,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
        AnimatedChevronRightIcon(
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )
    }
}
