package com.couplefit.ui.settings

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Intent
import android.net.Uri
import com.couplefit.ui.theme.*
import com.couplefit.ui.components.AnimatedArrowBackIcon
import com.couplefit.ui.components.AnimatedChevronIcon
import com.couplefit.ui.components.AnimatedChevronRightIcon
import com.couplefit.ui.components.AnimatedCheckIcon
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.health.connect.client.PermissionController
import androidx.hilt.navigation.compose.hiltViewModel
import com.couplefit.ui.healthconnect.HealthConnectViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    isDarkTheme: Boolean,
    onToggleDarkTheme: (Boolean) -> Unit,
    onToggleDarkThemeWithOrigin: ((Boolean, Offset) -> Unit)? = null,
    reduceMotion: Boolean = false,
    onToggleReduceMotion: (Boolean) -> Unit = {},
    healthConnectViewModel: HealthConnectViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val healthData by healthConnectViewModel.dashboardState.collectAsState()
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract()
    ) { _ ->
        healthConnectViewModel.checkStatusAndFetchData()
    }
    var savedBannerVisible by remember { mutableStateOf(false) }

    // Auto-save feedback trigger
    fun triggerAutoSaveNotice() {
        coroutineScope.launch {
            savedBannerVisible = true
            delay(1200)
            savedBannerVisible = false
        }
    }

    // --- State variables for all 9 sections ---
    // 1. Account
    var showSignOutDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var isPartnerLinked by remember { mutableStateOf(true) }
    var isSamsungConnected by remember { mutableStateOf(false) }
    var showSamsungBridgeDialog by remember { mutableStateOf(false) }

    // 2. Data Sharing Preferences
    var shareSteps by remember { mutableStateOf(true) }
    var shareWorkouts by remember { mutableStateOf(true) }
    var shareVitals by remember { mutableStateOf(true) }
    var shareECG by remember { mutableStateOf(true) }
    var shareStress by remember { mutableStateOf(false) }
    var shareCycleData by remember { mutableStateOf(false) }
    var shareMedications by remember { mutableStateOf(false) }

    // 3. Notifications
    var notifyCritical by remember { mutableStateOf(true) }
    var notifyNudges by remember { mutableStateOf(true) }
    var notifyMeds by remember { mutableStateOf(true) }
    var notifySync by remember { mutableStateOf(false) }
    var notifySummary by remember { mutableStateOf(true) }
    var quietHoursEnabled by remember { mutableStateOf(true) }


    // 5. Samsung Health Sync
    var syncFrequency by remember { mutableStateOf("Every 15 min") }
    var isSyncing by remember { mutableStateOf(false) }

    // 6. Medication Reminders Management
    var activeMeds by remember {
        mutableStateOf(
            listOf("Omega-3 (Morning)", "Vitamin D3 (8:00 AM)", "Magnesium (Night)")
        )
    }

    // 7. Units & Format
    var useMetric by remember { mutableStateOf(true) }
    var use24Hour by remember { mutableStateOf(false) }

    // 8. Privacy & Security
    var biometricLock by remember { mutableStateOf(false) }

    // Expandable Section Tracking
    var expandedAccount by remember { mutableStateOf(true) }
    var expandedSharing by remember { mutableStateOf(true) }
    var expandedNotifications by remember { mutableStateOf(false) }
    var expandedAppearance by remember { mutableStateOf(true) }
    var expandedSamsungSync by remember { mutableStateOf(false) }
    var expandedMeds by remember { mutableStateOf(false) }
    var expandedUnits by remember { mutableStateOf(false) }
    var expandedPrivacy by remember { mutableStateOf(false) }
    var expandedAbout by remember { mutableStateOf(false) }

    Scaffold(
        contentWindowInsets = WindowInsets.systemBars,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
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
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = AppSpacing.md, vertical = AppSpacing.xs)
            ) {
                // Subtle auto-save status pill
                AnimatedVisibility(
                    visible = savedBannerVisible,
                    enter = fadeIn(tween(150)),
                    exit = fadeOut(tween(150))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = AppSpacing.xs)
                            .clip(RoundedCornerShape(AppSpacing.xs))
                            .background(ColorSuccess.copy(alpha = 0.15f))
                            .padding(vertical = AppSpacing.xs),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            AnimatedCheckIcon(
                                modifier = Modifier.size(13.dp),
                                tint = ColorSuccess,
                                reduceMotion = reduceMotion
                            )
                            Text(
                                text = "Changes saved automatically",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = ColorSuccess
                            )
                        }
                    }
                }

                // 1. ACCOUNT SECTION
                SettingsGroup(
                    title = "ACCOUNT",
                    isExpanded = expandedAccount,
                    onToggle = { expandedAccount = !expandedAccount }
                ) {
                    // Profile Info
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "A",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 20.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Alex & Partner",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                if (isPartnerLinked) "Linked with Partner (Active Sync)" else "No partner linked",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isPartnerLinked) SystemGreen else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                    if (isPartnerLinked) {
                        SettingsClickableRow(
                            text = "Unlink Partner",
                            textColor = SystemRed,
                            onClick = {
                                isPartnerLinked = false
                                triggerAutoSaveNotice()
                            }
                        )
                    } else {
                        SettingsClickableRow(
                            text = "Pair / Link Partner",
                            textColor = MaterialTheme.colorScheme.primary,
                            onClick = {
                                isPartnerLinked = true
                                triggerAutoSaveNotice()
                            }
                        )
                    }

                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                    SettingsClickableRow(
                        text = "Sign Out",
                        textColor = MaterialTheme.colorScheme.onSurface,
                        onClick = { showSignOutDialog = true }
                    )

                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                    SettingsClickableRow(
                        text = "Delete Account",
                        textColor = SystemRed,
                        onClick = { showDeleteAccountDialog = true }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Connect with Samsung Account Button
                Button(
                    onClick = {
                        try {
                            // Try launching Health Connect Settings or Samsung Health
                            val intent = Intent("androidx.health.ACTION_HEALTH_CONNECT_SETTINGS").apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            try {
                                val fallbackIntent = Intent(Intent.ACTION_VIEW).apply {
                                    data = Uri.parse("market://details?id=com.google.android.apps.healthdata")
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(fallbackIntent)
                            } catch (e2: Exception) {
                                // If Play Store is unavailable, open via browser
                                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.healthdata"))
                                context.startActivity(webIntent)
                            }
                        }
                        // Mark connected and show the pop-up
                        isSamsungConnected = true
                        showSamsungBridgeDialog = true
                        triggerAutoSaveNotice()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSamsungConnected) SystemGreen.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primaryContainer,
                        contentColor = if (isSamsungConnected) SystemGreen else MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    shape = RoundedCornerShape(AppSpacing.md),
                    border = BorderStroke(
                        1.dp,
                        if (isSamsungConnected) SystemGreen.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (isSamsungConnected) "✓ Connected to Samsung Account" else "Connect with your Samsung account",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.3.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2. DATA SHARING PREFERENCES
                SettingsGroup(
                    title = "DATA SHARING PREFERENCES",
                    isExpanded = expandedSharing,
                    onToggle = { expandedSharing = !expandedSharing }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(onClick = {
                            shareSteps = true; shareWorkouts = true; shareVitals = true; shareECG = true; shareStress = true; shareMedications = true
                            triggerAutoSaveNotice()
                        }) {
                            Text("Share All", fontSize = 12.sp)
                        }
                        TextButton(onClick = {
                            shareSteps = false; shareWorkouts = false; shareVitals = false; shareECG = false; shareStress = false; shareCycleData = false; shareMedications = false
                            triggerAutoSaveNotice()
                        }) {
                            Text("Share None", fontSize = 12.sp, color = SystemRed)
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                    SettingsSwitchRow("Steps & Distance", shareSteps) { shareSteps = it; triggerAutoSaveNotice() }
                    SettingsSwitchRow("Workouts & Active Time", shareWorkouts) { shareWorkouts = it; triggerAutoSaveNotice() }
                    SettingsSwitchRow("Heart Rate & Vitals", shareVitals) { shareVitals = it; triggerAutoSaveNotice() }
                    SettingsSwitchRow("ECG Data", shareECG) { shareECG = it; triggerAutoSaveNotice() }
                    SettingsSwitchRow("Stress Levels", shareStress) { shareStress = it; triggerAutoSaveNotice() }
                    SettingsSwitchRow("Medications Routine", shareMedications) { shareMedications = it; triggerAutoSaveNotice() }
                    SettingsSwitchRow(
                        title = "Menstrual Cycle Data",
                        subtitle = "Strict opt-in (Female users)",
                        checked = shareCycleData
                    ) { shareCycleData = it; triggerAutoSaveNotice() }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 3. NOTIFICATIONS
                SettingsGroup(
                    title = "NOTIFICATIONS",
                    isExpanded = expandedNotifications,
                    onToggle = { expandedNotifications = !expandedNotifications }
                ) {
                    SettingsSwitchRow("Critical Vitals Alerts", notifyCritical) { notifyCritical = it; triggerAutoSaveNotice() }
                    SettingsSwitchRow("Partner Nudges & Activity Alerts", notifyNudges) { notifyNudges = it; triggerAutoSaveNotice() }
                    SettingsSwitchRow("Medication Reminders", notifyMeds) { notifyMeds = it; triggerAutoSaveNotice() }
                    SettingsSwitchRow("Sync Status Alerts", notifySync) { notifySync = it; triggerAutoSaveNotice() }
                    SettingsSwitchRow("Daily Evening Summary", notifySummary) { notifySummary = it; triggerAutoSaveNotice() }
                    SettingsSwitchRow(
                        title = "Quiet Hours (10:00 PM - 7:00 AM)",
                        subtitle = "Only emergency vitals break through",
                        checked = quietHoursEnabled
                    ) { quietHoursEnabled = it; triggerAutoSaveNotice() }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 4. APPEARANCE
                SettingsGroup(
                    title = "APPEARANCE",
                    isExpanded = expandedAppearance,
                    onToggle = { expandedAppearance = !expandedAppearance }
                ) {
                    SettingsSwitchRow(
                        title = "Dark Theme",
                        subtitle = if (isDarkTheme) "OLED pure black mode active" else "Clean daylight theme active",
                        checked = isDarkTheme,
                        onCheckedChangeWithOrigin = { newTheme, origin ->
                            if (onToggleDarkThemeWithOrigin != null) {
                                onToggleDarkThemeWithOrigin(newTheme, origin)
                            } else {
                                onToggleDarkTheme(newTheme)
                            }
                            triggerAutoSaveNotice()
                        }
                    ) {
                        onToggleDarkTheme(it)
                        triggerAutoSaveNotice()
                    }

                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                    SettingsSwitchRow(
                        title = "Reduce Motion",
                        subtitle = if (reduceMotion) "All animations disabled" else "Animations enabled",
                        checked = reduceMotion
                    ) {
                        onToggleReduceMotion(it)
                        triggerAutoSaveNotice()
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 5. SAMSUNG HEALTH SYNC
                SettingsGroup(
                    title = "HEALTH CONNECT / SAMSUNG HEALTH SYNC",
                    isExpanded = expandedSamsungSync,
                    onToggle = { expandedSamsungSync = !expandedSamsungSync }
                ) {
                    val statusText = when {
                        !healthData.isHealthConnectAvailable -> "Health Connect Not Available"
                        !healthData.hasPermissions -> "Permissions Needed"
                        else -> "Connected • Health Connect"
                    }
                    val statusColor = when {
                        !healthData.isHealthConnectAvailable -> SystemRed
                        !healthData.hasPermissions -> SystemOrange
                        else -> SystemGreen
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Connection Status", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                statusText,
                                style = MaterialTheme.typography.bodySmall,
                                color = statusColor
                            )
                        }

                        when {
                            !healthData.isHealthConnectAvailable -> {
                                Button(
                                    onClick = { healthConnectViewModel.openHealthConnect(context) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Get App", fontSize = 12.sp)
                                }
                            }
                            !healthData.hasPermissions -> {
                                Button(
                                    onClick = {
                                        try {
                                            permissionLauncher.launch(healthConnectViewModel.permissions)
                                        } catch (e: Exception) {
                                            healthConnectViewModel.openHealthConnect(context)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Connect", fontSize = 12.sp)
                                }
                            }
                            else -> {
                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            isSyncing = true
                                            healthConnectViewModel.checkStatusAndFetchData()
                                            delay(800)
                                            isSyncing = false
                                            triggerAutoSaveNotice()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(if (isSyncing) "Syncing..." else "Sync Now", fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                    SettingsClickableRow(
                        text = "Manage in Health Connect",
                        textColor = MaterialTheme.colorScheme.primary,
                        onClick = {
                            healthConnectViewModel.openHealthConnectSettings(context)
                        }
                    )

                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                    SettingsClickableRow(
                        text = "Add 1,000 Test Steps (Health Connect)",
                        textColor = MaterialTheme.colorScheme.primary,
                        onClick = {
                            coroutineScope.launch {
                                healthConnectViewModel.writeMockData(1000L)
                                triggerAutoSaveNotice()
                            }
                        }
                    )

                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Sync Frequency", style = MaterialTheme.typography.bodyMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("15 min", "Hourly", "Manual").forEach { freq ->
                                FilterChip(
                                    selected = syncFrequency.contains(freq),
                                    onClick = {
                                        syncFrequency = freq
                                        triggerAutoSaveNotice()
                                    },
                                    label = { Text(freq, fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 6. MEDICATION REMINDERS MANAGEMENT
                SettingsGroup(
                    title = "MEDICATION REMINDERS",
                    isExpanded = expandedMeds,
                    onToggle = { expandedMeds = !expandedMeds }
                ) {
                    activeMeds.forEachIndexed { index, med ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(med, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "Delete",
                                color = SystemRed,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.clickable {
                                    activeMeds = activeMeds.toMutableList().also { it.removeAt(index) }
                                    triggerAutoSaveNotice()
                                }
                            )
                        }
                        if (index < activeMeds.size - 1) {
                            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 7. UNITS & FORMAT
                SettingsGroup(
                    title = "UNITS & FORMAT",
                    isExpanded = expandedUnits,
                    onToggle = { expandedUnits = !expandedUnits }
                ) {
                    SettingsSwitchRow(
                        title = "Metric Units (km, kg)",
                        subtitle = if (useMetric) "Metric selected" else "Imperial (miles, lbs) selected",
                        checked = useMetric
                    ) { useMetric = it; triggerAutoSaveNotice() }

                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                    SettingsSwitchRow(
                        title = "24-Hour Time Format",
                        subtitle = if (use24Hour) "14:00" else "2:00 PM",
                        checked = use24Hour
                    ) { use24Hour = it; triggerAutoSaveNotice() }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 8. PRIVACY & SECURITY
                SettingsGroup(
                    title = "PRIVACY & SECURITY",
                    isExpanded = expandedPrivacy,
                    onToggle = { expandedPrivacy = !expandedPrivacy }
                ) {
                    SettingsSwitchRow(
                        title = "App Lock",
                        subtitle = "Require Biometric / PIN to open CoupleFit",
                        checked = biometricLock
                    ) { biometricLock = it; triggerAutoSaveNotice() }

                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                    SettingsClickableRow("Export Health Data (JSON/CSV)", MaterialTheme.colorScheme.primary) {
                        triggerAutoSaveNotice()
                    }

                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                    SettingsClickableRow("Privacy Policy", MaterialTheme.colorScheme.onSurface) {}
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 9. ABOUT
                SettingsGroup(
                    title = "ABOUT",
                    isExpanded = expandedAbout,
                    onToggle = { expandedAbout = !expandedAbout }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("CoupleFit Version", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "v1.2.0 (Build 42)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    SettingsClickableRow("What's New / Changelog", MaterialTheme.colorScheme.primary) {}
                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    SettingsClickableRow("Support & Contact Us", MaterialTheme.colorScheme.primary) {}
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Confirmation Dialog for Sign Out
    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text("Sign Out") },
            text = { Text("Are you sure you want to sign out of CoupleFit?") },
            confirmButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text("Sign Out", color = SystemRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Confirmation Dialog for Account Deletion
    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = { Text("Delete Account") },
            text = { Text("This will permanently delete all your health snapshots, couple connection, and preferences. This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = { showDeleteAccountDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = SystemRed)
                ) {
                    Text("Delete Forever")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Success Pop-up for Samsung Account Bridge
    if (showSamsungBridgeDialog) {
        AlertDialog(
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
                Text(
                    text = "Bridge Initiated",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Text(
                    text = "Bridge initiated with Samsung Account. Health Connect and Samsung Health permissions are now synced with CoupleFit.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            },
            confirmButton = {
                Button(
                    onClick = { showSamsungBridgeDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(AppSpacing.sm)
                ) {
                    Text("Done")
                }
            }
        )
    }
}

// --- Reusable Settings UI Components with Custom Styling ---

@Composable
fun SettingsGroup(
    title: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() }
                .padding(horizontal = 4.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                letterSpacing = 0.8.sp
            )
            AnimatedChevronIcon(
                isExpanded = isExpanded,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(tween(200)) + fadeIn(tween(150)),
            exit = shrinkVertically(tween(200)) + fadeOut(tween(150))
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(AppSpacing.md),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    content()
                }
            }
        }
    }
}

@Composable
fun SettingsSwitchRow(
    title: String,
    checked: Boolean,
    subtitle: String? = null,
    onCheckedChangeWithOrigin: ((Boolean, Offset) -> Unit)? = null,
    onCheckedChange: (Boolean) -> Unit
) {
    var switchOrigin by remember { mutableStateOf(Offset.Zero) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { 
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
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = checked,
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
            .padding(horizontal = 14.dp, vertical = 13.dp),
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

