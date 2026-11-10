package com.couplefit.ui.dashboard

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.couplefit.ui.components.NotificationPermissionRationaleDialog
import com.couplefit.ui.challenge.ChallengeViewModel
import com.couplefit.ui.challenge.SideBySideChallengeCard
import com.couplefit.ui.challenge.StartChallengeDialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import com.couplefit.ui.components.NumberPopInCounter
import com.couplefit.ui.components.NumberPopInText
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.health.connect.client.PermissionController
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import com.couplefit.ui.components.ActivityRing
import com.couplefit.ui.components.MotionArcProgressGauge
import com.couplefit.ui.components.MotionPillChip
import com.couplefit.ui.components.WeeklyStreakRow
import com.couplefit.ui.components.ChartDataPoint
import com.couplefit.ui.challenge.ActiveChallengePreviewCard
import com.couplefit.ui.components.InteractiveChart
import com.couplefit.ui.components.ConfettiParticleExplosion
import com.couplefit.ui.components.FloatingHeartCheerCluster
import com.couplefit.ui.components.MedicationDuePill
import com.couplefit.ui.components.QuickNudgeRow
import com.couplefit.ui.components.AnimatedSettingsIcon
import com.couplefit.ui.components.AnimatedHeartIcon
import com.couplefit.ui.components.AnimatedFootstepsIcon
import com.couplefit.ui.components.AnimatedActivityIcon
import com.couplefit.ui.components.NudgeButton
import com.couplefit.ui.components.LiveHeartRateEmptyWaveform
import androidx.compose.ui.text.style.TextOverflow
import com.couplefit.ui.healthconnect.HealthConnectViewModel
import com.couplefit.ui.healthconnect.HealthDashboardUiState
import com.couplefit.ui.theme.*
import com.couplefit.ui.theme.LiquiTokens
import com.couplefit.ui.theme.liquiGlass
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@OptIn(
    ExperimentalMaterialApi::class,
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class
)
@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = false,
    onToggleDarkTheme: () -> Unit = {},
    reduceMotion: Boolean = false,
    onOpenSettings: () -> Unit = {},
    onViewAllChallenges: () -> Unit = {},
    healthConnectViewModel: HealthConnectViewModel = hiltViewModel(),
    challengeViewModel: ChallengeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val healthData by healthConnectViewModel.dashboardState.collectAsState()
    val challengeState by challengeViewModel.uiState.collectAsState()
    var showStartChallengeDialog by remember { mutableStateOf(false) }

    LaunchedEffect(healthData.coupleId, healthData.partnerId) {
        challengeViewModel.observeChallenges(
            coupleId = healthData.coupleId,
            partnerUid = healthData.partnerId
        )
    }

    if (showStartChallengeDialog) {
        StartChallengeDialog(
            onDismiss = { showStartChallengeDialog = false },
            onStartChallenge = { type, goal ->
                challengeViewModel.createChallenge(
                    coupleId = healthData.coupleId,
                    type = type,
                    goal = goal
                )
            }
        )
    }

    var showNotificationRationale by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            } else {
                false
            }
        )
    }

    if (showNotificationRationale) {
        NotificationPermissionRationaleDialog(
            onDismiss = { showNotificationRationale = false },
            onPermissionResult = { _ ->
                showNotificationRationale = false
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract()
    ) { _ ->
        healthConnectViewModel.checkStatusAndFetchData()
    }

    // Unified app-open & resume trigger for transitions.dev Number Pop-In animation (POP-01)
    var popInTrigger by rememberSaveable { mutableIntStateOf(1) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                healthConnectViewModel.onAppForeground()
                popInTrigger++ // Triggers pop-in animation every time the app opens or resumes
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val visibleState = remember {
        MutableTransitionState(false).apply { targetState = true }
    }
    
    var syncTrigger by remember { mutableStateOf(0) }
    var isExplicitRefresh by remember { mutableStateOf(true) } // True on initial load and pull-to-refresh

    // Reset explicit refresh after animations finish (approximate)
    LaunchedEffect(syncTrigger, visibleState.isIdle) {
        if (visibleState.isIdle && isExplicitRefresh) {
            delay(1500) // Wait for animations to finish
            isExplicitRefresh = false
        }
    }

    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val pullRefreshState = rememberPullRefreshState(
        refreshing = healthData.isSyncing,
        onRefresh = {
            isExplicitRefresh = true
            healthConnectViewModel.checkStatusAndFetchData()
            syncTrigger++
        }
    )

    // Foreground fast-refresh loop (simulate 10s polling of local cache if flow wasn't active)
    LaunchedEffect(Unit) {
        while (true) {
            delay(10_000)
            // Just increment sync trigger subtly, but we keep isExplicitRefresh = false 
            // so we don't trigger the "dramatic" entrance animations on background tick
            // In a real app this would trigger the DAO fetch if we weren't using Flow
            syncTrigger++
        }
    }

    val todayFormatted = remember {
        try {
            LocalDate.now().format(
                DateTimeFormatter.ofPattern("EEEE, MMMM d").withLocale(Locale.getDefault())
            ).uppercase()
        } catch (e: Exception) {
            "TODAY"
        }
    }

    Box(modifier = modifier.fillMaxSize().pullRefresh(pullRefreshState)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
        ) {
            // Header with Date Overline, Title & Settings Icon
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.md)
                    .padding(bottom = AppSpacing.md)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = todayFormatted,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.8.sp,
                        maxLines = 1
                    )

                    // Settings Gear Icon (Emancipated Floating Glyph - TST-04)
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier
                            .size(44.dp)
                            .semantics { contentDescription = "Open Settings" }
                    ) {
                        AnimatedSettingsIcon(
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onSurface,
                            isSpinning = isExplicitRefresh,
                            reduceMotion = reduceMotion
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AppSpacing.xxs))

                Text(
                    text = "Today's Pulse",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Contextual "Medication Due" Pill (CSA-03 / Tree Test TS-04)
            // Gated strictly to +/- 1 hour around scheduled dose time (CPX-02)
            var isMedicationDismissed by remember { mutableStateOf(false) }
            val isWithinMedicationWindow = remember {
                com.couplefit.ui.components.isWithinDoseWindow("08:00 AM", 60)
            }
            if (!isMedicationDismissed && isWithinMedicationWindow) {
                MedicationDuePill(
                    medicationName = "Vitamin D3",
                    scheduledTime = "08:00 AM",
                    enforceTimeWindow = true,
                    onLogDose = {
                        // In production, persists to medication log
                    },
                    onDismiss = {
                        isMedicationDismissed = true
                    }
                )
            }

            // Nudge Confirmation Banner
            AnimatedVisibility(
                visible = healthData.nudgeSentMessage != null,
                enter = fadeIn() + slideInVertically(),
                exit = androidx.compose.animation.fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppSpacing.md)
                        .padding(bottom = AppSpacing.sm),
                    shape = RoundedCornerShape(AppSpacing.md),
                    colors = CardDefaults.cardColors(containerColor = SystemGreen.copy(alpha = 0.15f)),
                    border = BorderStroke(1.dp, SystemGreen.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(AppSpacing.sm),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AnimatedHeartIcon(
                            modifier = Modifier.size(20.dp),
                            tint = SystemGreen,
                            isPulsing = true,
                            reduceMotion = reduceMotion
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = healthData.nudgeSentMessage ?: "",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = SystemGreen
                        )
                    }
                }
            }


            // 1. Health Connect Not Ready (Install or Update Needed)
            if (!healthData.isHealthConnectAvailable) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppSpacing.md)
                        .padding(bottom = AppSpacing.sm),
                    shape = RoundedCornerShape(AppSpacing.md),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(AppSpacing.sm),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Health Connect Required",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = "Install or update Health Connect from the Play Store to sync your fitness data.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.85f)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { healthConnectViewModel.openHealthConnect(context) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            shape = RoundedCornerShape(AppSpacing.sm)
                        ) {
                            Text("Get App", fontSize = 12.sp)
                        }
                    }
                }
            }

            // 2. Permission Request Banner (if Health Connect permissions are not yet granted)
            if (healthData.isHealthConnectAvailable && !healthData.hasPermissions) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppSpacing.md)
                        .padding(bottom = AppSpacing.sm),
                    shape = RoundedCornerShape(AppSpacing.md),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(AppSpacing.sm),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Connect Health Connect",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Enable permissions to sync your daily steps & heart rate from Samsung Health.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                try {
                                    permissionLauncher.launch(healthConnectViewModel.permissions)
                                } catch (e: Exception) {
                                    healthConnectViewModel.openHealthConnect(context)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(AppSpacing.sm)
                        ) {
                            Text("Grant", fontSize = 12.sp)
                        }
                    }
                }
            }

            // Sync status with ANM-12 OrbitalSyncPulse kinetic feedback
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.md)
                    .padding(bottom = AppSpacing.xs),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OrbitalSyncPulse(isSyncing = healthData.isSyncing)

                val pName = healthData.partnerName.ifBlank { "Partner" }
                val syncText = if (healthData.isSyncing) {
                    "Syncing couple pulse with Health Connect..."
                } else healthData.lastSyncedAt?.let {
                    val fmt = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withZone(ZoneId.systemDefault())
                    "You & $pName are in sync • Updated ${fmt.format(it)}"
                } ?: "Pull down to refresh your couple pulse"

                Text(
                    text = syncText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                    modifier = Modifier.weight(1f)
                )
            }

            // Warm Day-1 Onboarding / Cold-Start Guidance Card (WF-06, GLS-07, LAY-01)
            if (healthData.steps == 0L) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppSpacing.md)
                        .padding(bottom = AppSpacing.sm)
                        .iosGlass(shape = RoundedCornerShape(AppSpacing.md)),
                    shape = RoundedCornerShape(AppSpacing.md),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    border = null
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("🌱", fontSize = 20.sp)
                            Column {
                                Text(
                                    text = "A fresh day together!",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Take your first morning steps with your partner.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextButton(
                                onClick = { healthConnectViewModel.writeMockData(1000L) },
                                shape = RoundedCornerShape(AppSpacing.sm)
                            ) {
                                Text("+1k Steps", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                            TextButton(
                                onClick = { healthConnectViewModel.addSamplePartnerSteps(1000L) },
                                shape = RoundedCornerShape(AppSpacing.sm)
                            ) {
                                Text("+1k Partner", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Swipeable View Selection Tabs (Side-by-Side vs My Stats vs Partner's Stats)
            val pagerState = rememberPagerState(pageCount = { 3 })

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.md)
                    .padding(bottom = AppSpacing.md)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Tab 0: Side-by-Side
                    FilterChip(
                        selected = pagerState.currentPage == 0,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(0)
                            }
                        },
                        label = { Text("Side-by-Side") },
                        leadingIcon = {
                            AnimatedActivityIcon(
                                modifier = Modifier.size(16.dp),
                                tint = VitalsAccent,
                                reduceMotion = reduceMotion
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = VitalsAccent.copy(alpha = 0.15f),
                            selectedLabelColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = null
                    )

                    // Tab 1: My Stats
                    FilterChip(
                        selected = pagerState.currentPage == 1,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(1)
                            }
                        },
                        label = { Text("My Stats") },
                        leadingIcon = {
                            AnimatedFootstepsIcon(
                                modifier = Modifier.size(16.dp),
                                tint = StepsAccent,
                                reduceMotion = reduceMotion
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StepsAccent.copy(alpha = 0.15f),
                            selectedLabelColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = null
                    )

                    // Tab 2: Partner's Stats
                    FilterChip(
                        selected = pagerState.currentPage == 2,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(2)
                            }
                        },
                        label = {
                            val pName = healthData.partnerName.ifBlank { "Partner" }
                            Text("${pName}'s Stats", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                        },
                        leadingIcon = {
                            AnimatedHeartIcon(
                                modifier = Modifier.size(16.dp),
                                tint = SystemRed,
                                isPulsing = pagerState.currentPage == 2,
                                reduceMotion = reduceMotion
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SystemRed.copy(alpha = 0.15f),
                            selectedLabelColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = null
                    )
                }

                // Pager Indicators
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(3) { index ->
                        val isSelected = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .size(if (isSelected) 8.dp else 6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) VitalsAccent else MaterialTheme.colorScheme.outline
                                )
                                .then(
                                    Modifier.pointerInput(index) {
                                        detectTapGestures {
                                            coroutineScope.launch {
                                                pagerState.animateScrollToPage(index)
                                            }
                                        }
                                    }
                                )
                        )
                    }
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = AppSpacing.md),
                pageSpacing = AppSpacing.md
            ) { page ->
                val isPageActive = pagerState.currentPage == page
                AnimatedVisibility(
                    visibleState = visibleState,
                    enter = if (reduceMotion) {
                        androidx.compose.animation.EnterTransition.None
                    } else {
                        fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow)) +
                                slideInVertically(
                                    initialOffsetY = { 40 },
                                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                                )
                    }
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        if (page == 0) {
                            // --- SIDE-BY-SIDE DASHBOARD TAB (combine(myFlow, partnerFlow)) ---
                            WeeklyStreakRow(
                                todaySteps = healthData.steps,
                                goal = healthData.stepsGoal,
                                modifier = Modifier.padding(bottom = AppSpacing.xs)
                            )

                            Text(
                                text = "ACTIVITY COMPARISON",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 0.5.sp,
                                modifier = Modifier.padding(bottom = AppSpacing.xs)
                            )

                            // Dual Activity Motion Gauges with Double-Tap Cheer & Telemetry Status Pill (FLOW-02, FLOW-03, POP-01)
                            SideBySideActivityCard(
                                healthData = healthData,
                                reduceMotion = reduceMotion,
                                animTrigger = popInTrigger,
                                onSendCheer = { cheerMessage ->
                                    healthConnectViewModel.sendNudgeToPartner(cheerMessage)
                                },
                                onPingPartner = {
                                    healthConnectViewModel.sendNudgeToPartner("Ping! Please sync your activity 📡")
                                    healthConnectViewModel.addSamplePartnerSteps(500L)
                                }
                            )

                            Spacer(modifier = Modifier.height(AppSpacing.xs))

                            // Quick-Action Nudges elevated directly into the Thumb Zone under rings (WF-01, TST-06)
                            QuickNudgeRow(
                                partnerName = healthData.partnerName,
                                mySteps = healthData.steps,
                                partnerSteps = healthData.partnerSteps,
                                reduceMotion = reduceMotion,
                                onSendNudge = { nudgeMessage ->
                                    healthConnectViewModel.sendNudgeToPartner(nudgeMessage)
                                }
                            )

                            Spacer(modifier = Modifier.height(AppSpacing.xs))

                            // Standings Card
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = AppSpacing.xs)
                                    .iosGlass(shape = MaterialTheme.shapes.large),
                                shape = MaterialTheme.shapes.large,
                                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                                border = null
                            ) {
                                Column(modifier = Modifier.padding(AppSpacing.md)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "TODAY'S COMPETITION",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        AnimatedHeartIcon(
                                            modifier = Modifier.size(16.dp),
                                            tint = SystemRed,
                                            isPulsing = true,
                                            reduceMotion = reduceMotion
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(AppSpacing.xs))
                                    val stepDiff = healthData.steps - healthData.partnerSteps
                                    val standingText = when {
                                        stepDiff > 0 -> "🏆 You're leading by ${stepDiff} steps!"
                                        stepDiff < 0 -> "⚡ ${healthData.partnerName} is leading by ${-stepDiff} steps!"
                                        else -> "🤝 Tied at ${healthData.steps} steps! Keep moving!"
                                    }
                                    Text(
                                        text = standingText,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (stepDiff >= 0) SystemGreen else SystemRed
                                    )
                                    val totalSteps = (healthData.steps + healthData.partnerSteps).coerceAtLeast(1L).toFloat()
                                    val userRatio = (healthData.steps.toFloat() / totalSteps).coerceIn(0.05f, 0.95f)
                                    Spacer(modifier = Modifier.height(AppSpacing.xs))
                                    // Playful Tug-of-War Balance Bar (TST-08)
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                    ) {
                                        Row(modifier = Modifier.fillMaxSize()) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxHeight()
                                                    .weight(userRatio)
                                                    .background(CoupleFitIdentityTokens.UserPrimary)
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxHeight()
                                                    .weight(1f - userRatio)
                                                    .background(CoupleFitIdentityTokens.PartnerPrimary)
                                            )
                                        }
                                    }
                                }
                            }

                            // Active Challenge Mini-Preview linking to Tab 2 Challenges (CSA-04 & Wireframe 1)
                            ActiveChallengePreviewCard(
                                challenge = challengeState.activeChallenge,
                                partnerName = healthData.partnerName,
                                onViewAllChallenges = onViewAllChallenges
                            )

                            Spacer(modifier = Modifier.height(AppSpacing.sm))

                            // Harmonized Live Heart Rate & Vitals Waveform Card (Phase 22 / DSN-05)
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = AppSpacing.xs)
                                    .iosGlass(shape = MaterialTheme.shapes.large),
                                shape = MaterialTheme.shapes.large,
                                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                                border = null
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(AppSpacing.md)
                                ) {
                                    // 1. Unified Dashboard Category Header (DSN-05)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f, fill = false),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            AnimatedHeartIcon(
                                                modifier = Modifier.size(16.dp),
                                                tint = VitalsAccent,
                                                isPulsing = healthData.averageHeartRate > 0,
                                                reduceMotion = reduceMotion
                                            )
                                            Text(
                                                text = "LIVE HEART RATE",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                letterSpacing = 0.5.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        // Status Pill matching Activity Gauges and Standings (Two-Material tactile badge)
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(
                                                    color = if (healthData.averageHeartRate > 0) SystemGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                                    shape = RoundedCornerShape(12.dp)
                                                )
                                                .border(
                                                    width = 1.dp,
                                                    if (healthData.averageHeartRate > 0) SystemGreen.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                                    RoundedCornerShape(12.dp)
                                                )
                                        ) {
                                            Text(
                                                text = if (healthData.averageHeartRate > 0) "🟢 Live Telemetry" else "⚪ Health Connect",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                                color = if (healthData.averageHeartRate > 0) SystemGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(AppSpacing.xs))

                                    // 2. Hero Metric Readout & Context Row
                                    val chartPoints = remember(healthData.heartRatePoints, healthData.averageHeartRate) {
                                        if (healthData.heartRatePoints.size > 1) {
                                            healthData.heartRatePoints
                                        } else {
                                            val baseBpm = if (healthData.averageHeartRate > 0) healthData.averageHeartRate.toFloat() else 72f
                                            listOf(
                                                ChartDataPoint("06:00", (baseBpm - 6f).coerceIn(45f, 180f)),
                                                ChartDataPoint("09:00", (baseBpm + 4f).coerceIn(45f, 180f)),
                                                ChartDataPoint("12:00", (baseBpm + 14f).coerceIn(45f, 180f)),
                                                ChartDataPoint("15:00", (baseBpm + 6f).coerceIn(45f, 180f)),
                                                ChartDataPoint("18:00", (baseBpm + 18f).coerceIn(45f, 180f)),
                                                ChartDataPoint("21:00", (baseBpm - 2f).coerceIn(45f, 180f))
                                            )
                                        }
                                    }

                                    val currentBpm = if (healthData.averageHeartRate > 0) {
                                        healthData.averageHeartRate.toInt()
                                    } else {
                                        chartPoints.lastOrNull()?.value?.toInt() ?: 72
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Bottom
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.Bottom,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            NumberPopInCounter(
                                                value = currentBpm.toLong(),
                                                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface,
                                                animTrigger = popInTrigger,
                                                reduceMotion = reduceMotion,
                                                modifier = Modifier.alignByBaseline()
                                            )
                                            Text(
                                                text = "BPM",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = VitalsAccent,
                                                modifier = Modifier.alignByBaseline()
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (healthData.averageHeartRate > 0) "• Resting Avg" else "• Baseline Telemetry",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.alignByBaseline()
                                            )
                                        }

                                        // Peak / Min chips if data is present
                                        if (chartPoints.size > 1) {
                                            val peakVal = chartPoints.maxOf { it.value }.toInt()
                                            val minVal = chartPoints.minOf { it.value }.toInt()
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Surface(
                                                    shape = RoundedCornerShape(AppSpacing.xs),
                                                    color = ColorCritical.copy(alpha = 0.12f)
                                                ) {
                                                    Text(
                                                        text = "▲ $peakVal Peak",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = ColorCritical,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                                if (peakVal != minVal) {
                                                    Surface(
                                                        shape = RoundedCornerShape(AppSpacing.xs),
                                                        color = ColorInfo.copy(alpha = 0.12f)
                                                    ) {
                                                        Text(
                                                            text = "▼ $minVal Min",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = ColorInfo,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(AppSpacing.sm))

                                    // 3. Liqui Glass Waveform Stage (Interactive Spline Graph Restored)
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(145.dp)
                                    ) {
                                        if (chartPoints.isNotEmpty()) {
                                            InteractiveChart(
                                                data = chartPoints,
                                                lineColor = VitalsAccent,
                                                metricTitle = "Heart Rate",
                                                reduceMotion = reduceMotion,
                                                syncTrigger = if (isExplicitRefresh) syncTrigger else 0,
                                                showHeader = false,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        } else {
                                            LiveHeartRateEmptyWaveform(
                                                tint = VitalsAccent,
                                                reduceMotion = reduceMotion,
                                                onSyncClick = { healthConnectViewModel.checkStatusAndFetchData() },
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                    }
                                }
                            }
                        } else if (page == 1) {
                            // --- MY STATS TAB (Live from Health Connect) ---
                            WeeklyStreakRow(
                                todaySteps = healthData.steps,
                                goal = healthData.stepsGoal,
                                modifier = Modifier.padding(bottom = AppSpacing.xs)
                            )

                            Text(
                                text = "ACTIVITY",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 0.5.sp,
                                modifier = Modifier.padding(bottom = AppSpacing.xs)
                            )

                            StatCard(
                                title = "My Stats",
                                subtitle = if (!healthData.hasPermissions) "Permission required • Tap to grant" 
                                           else if (healthData.steps == 0L) "0 steps today • Pull to sync" 
                                           else "Live from Health Connect",
                                stepsProgress = healthData.stepsProgress,
                                exerciseProgress = healthData.exerciseProgress,
                                stepsCount = healthData.steps,
                                activeMinutesCount = healthData.activeMinutes,
                                caloriesCount = healthData.caloriesBurned,
                                onSyncClick = { healthConnectViewModel.checkStatusAndFetchData() },
                                onAddSampleStepsClick = { healthConnectViewModel.writeMockData(1000L) },
                                animTrigger = Pair(popInTrigger, isPageActive && pagerState.currentPage == 1),
                                isExplicitRefresh = isExplicitRefresh,
                                reduceMotion = reduceMotion,
                                staggerIndex = 0,
                                isPartner = false,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight()
                            )

                            Spacer(modifier = Modifier.height(AppSpacing.md))

                            // Heart Rate Section
                            Column(modifier = Modifier.padding(bottom = AppSpacing.xs)) {
                                Text(
                                    text = "MY VITALS",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    letterSpacing = 0.5.sp
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Heart Rate & ECG",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    AnimatedActivityIcon(
                                        modifier = Modifier.size(20.dp),
                                        tint = VitalsAccent,
                                        reduceMotion = reduceMotion
                                    )
                                }
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(260.dp)
                                    .iosGlass(shape = MaterialTheme.shapes.large),
                                shape = MaterialTheme.shapes.large,
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.Transparent
                                ),
                                border = null
                            ) {
                                val myChartData = remember(healthData.heartRatePoints, healthData.averageHeartRate) {
                                    if (healthData.heartRatePoints.size > 1) {
                                        healthData.heartRatePoints
                                    } else {
                                        val baseBpm = if (healthData.averageHeartRate > 0) healthData.averageHeartRate.toFloat() else 72f
                                        listOf(
                                            ChartDataPoint("06:00", (baseBpm - 6f).coerceIn(45f, 180f)),
                                            ChartDataPoint("09:00", (baseBpm + 4f).coerceIn(45f, 180f)),
                                            ChartDataPoint("12:00", (baseBpm + 14f).coerceIn(45f, 180f)),
                                            ChartDataPoint("15:00", (baseBpm + 6f).coerceIn(45f, 180f)),
                                            ChartDataPoint("18:00", (baseBpm + 18f).coerceIn(45f, 180f)),
                                            ChartDataPoint("21:00", (baseBpm - 2f).coerceIn(45f, 180f))
                                        )
                                    }
                                }

                                if (myChartData.isNotEmpty()) {
                                    InteractiveChart(
                                        data = myChartData,
                                        lineColor = VitalsAccent,
                                        metricTitle = "Heart Rate",
                                        reduceMotion = reduceMotion,
                                        syncTrigger = if (isExplicitRefresh) syncTrigger else 0,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(AppSpacing.md)
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(AppSpacing.md),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = if (healthData.hasPermissions) "No Heart Rate data logged today" else "Permissions required",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = if (healthData.hasPermissions) "Records from Samsung Health will appear here once synced" else "Grant Health Connect permissions to view vitals",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                                textAlign = TextAlign.Center
                                            )
                                            if (!healthData.hasPermissions) {
                                                Spacer(modifier = Modifier.height(AppSpacing.sm))
                                                Button(
                                                    onClick = { permissionLauncher.launch(healthConnectViewModel.permissions) },
                                                    shape = RoundedCornerShape(AppSpacing.xs),
                                                    colors = ButtonDefaults.buttonColors(containerColor = VitalsAccent)
                                                ) {
                                                    Text(
                                                        text = "Grant Permissions",
                                                        color = Color.Black,
                                                        fontWeight = FontWeight.Bold,
                                                        style = MaterialTheme.typography.labelMedium
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(AppSpacing.md))

                            // Oxygen Saturation Card
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(260.dp)
                                    .iosGlass(shape = MaterialTheme.shapes.large),
                                shape = MaterialTheme.shapes.large,
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.Transparent
                                ),
                                border = null
                            ) {
                                val o2ChartData = healthData.oxygenSaturationPoints

                                if (o2ChartData.isNotEmpty()) {
                                    InteractiveChart(
                                        data = o2ChartData,
                                        lineColor = VitalsAccent, // You can use a different color if preferred
                                        metricTitle = "Blood Oxygen (%)",
                                        reduceMotion = reduceMotion,
                                        syncTrigger = if (isExplicitRefresh) syncTrigger else 0,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(AppSpacing.md)
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(AppSpacing.md),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = if (healthData.hasPermissions) "No Blood Oxygen data today" else "Permissions required",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = if (healthData.hasPermissions) "Records from Samsung Health will appear here once synced" else "Grant Health Connect permissions to view vitals",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            // --- PARTNER'S STATS TAB ---
                            Text(
                                text = "ACTIVITY",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 0.5.sp,
                                modifier = Modifier.padding(bottom = AppSpacing.xs)
                            )

                            StatCard(
                                title = "${healthData.partnerName}'s Stats",
                                subtitle = if (healthData.partnerHasTodayData) healthData.partnerLastUpdatedLabel else "Awaiting partner sync • Offline ready",
                                stepsProgress = healthData.partnerStepsProgress,
                                exerciseProgress = healthData.partnerExerciseProgress,
                                stepsCount = healthData.partnerSteps,
                                activeMinutesCount = healthData.partnerActiveMinutes,
                                caloriesCount = healthData.partnerCaloriesBurned,
                                onSyncClick = { healthConnectViewModel.checkStatusAndFetchData() },
                                onAddSampleStepsClick = { healthConnectViewModel.addSamplePartnerSteps(1000L) },
                                animTrigger = Pair(popInTrigger, isPageActive && pagerState.currentPage == 2),
                                isExplicitRefresh = isExplicitRefresh,
                                reduceMotion = reduceMotion,
                                staggerIndex = 0,
                                isPartner = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight()
                            )

                            Spacer(modifier = Modifier.height(AppSpacing.md))

                            // Partner Card: displays offline cached values from Room SSOT with last updated label
                            PartnerOfflineCard(
                                partnerName = healthData.partnerName,
                                partnerSteps = healthData.partnerSteps,
                                partnerActiveMinutes = healthData.partnerActiveMinutes,
                                partnerCalories = healthData.partnerCaloriesBurned,
                                partnerAvgHr = healthData.partnerAverageHeartRate,
                                lastUpdatedLabel = healthData.partnerLastUpdatedLabel,
                                animTrigger = Pair(popInTrigger, isPageActive && pagerState.currentPage == 2),
                                reduceMotion = reduceMotion
                            )

                            Spacer(modifier = Modifier.height(AppSpacing.md))

                            // Apple Fitness-style Couple Competition / Standings Card
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = AppSpacing.xs)
                                    .iosGlass(shape = MaterialTheme.shapes.large),
                                shape = MaterialTheme.shapes.large,
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.Transparent
                                ),
                                border = null
                            ) {
                                Column(modifier = Modifier.padding(AppSpacing.md)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "TODAY'S COMPETITION",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        AnimatedHeartIcon(
                                            modifier = Modifier.size(16.dp),
                                            tint = SystemRed,
                                            isPulsing = true,
                                            reduceMotion = reduceMotion
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(AppSpacing.xs))
                                    val stepDiff = healthData.steps - healthData.partnerSteps
                                    val standingText = when {
                                        stepDiff > 0 -> "You're leading by ${stepDiff} steps!"
                                        stepDiff < 0 -> "${healthData.partnerName} is leading by ${-stepDiff} steps!"
                                        else -> "Tied at ${healthData.steps} steps! Keep moving!"
                                    }
                                    Text(
                                        text = standingText,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (stepDiff >= 0) SystemGreen else SystemRed
                                    )
                                    val totalSteps = (healthData.steps + healthData.partnerSteps).coerceAtLeast(1L).toFloat()
                                    val userRatio = (healthData.steps.toFloat() / totalSteps).coerceIn(0.05f, 0.95f)
                                    Spacer(modifier = Modifier.height(AppSpacing.xs))
                                    // Playful Tug-of-War Balance Bar (TST-08)
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                    ) {
                                        Row(modifier = Modifier.fillMaxSize()) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxHeight()
                                                    .weight(userRatio)
                                                    .background(CoupleFitIdentityTokens.UserPrimary)
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxHeight()
                                                    .weight(1f - userRatio)
                                                    .background(CoupleFitIdentityTokens.PartnerPrimary)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(AppSpacing.sm))

                            // Interactive Nudge Button
                            NudgeButton(
                                text = "Send a Cheer to ${healthData.partnerName}",
                                onClick = { healthConnectViewModel.sendNudgeToPartner() },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(AppSpacing.md))

                            Column(modifier = Modifier.padding(bottom = AppSpacing.xs)) {
                                Text(
                                    text = "PARTNER'S VITALS",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    letterSpacing = 0.5.sp
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Daily Summary",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    AnimatedActivityIcon(
                                        modifier = Modifier.size(20.dp),
                                        tint = VitalsAccent,
                                        reduceMotion = reduceMotion
                                    )
                                }
                            }

                            // Partner's Average Heart Rate Card (Daily Aggregate Only)
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .iosGlass(shape = MaterialTheme.shapes.large),
                                shape = MaterialTheme.shapes.large,
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.Transparent
                                ),
                                border = null
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(AppSpacing.md),
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "Average Heart Rate",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        AnimatedHeartIcon(
                                            modifier = Modifier.size(22.dp),
                                            tint = VitalsAccent,
                                            isPulsing = healthData.partnerAverageHeartRate > 0.0,
                                            reduceMotion = reduceMotion
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(AppSpacing.xs))
                                    if (healthData.partnerAverageHeartRate > 0.0) {
                                        NumberPopInCounter(
                                            value = healthData.partnerAverageHeartRate.toLong(),
                                            style = MaterialTheme.typography.headlineMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = VitalsAccent
                                            ),
                                            suffix = " BPM",
                                            animTrigger = Pair(popInTrigger, isPageActive && pagerState.currentPage == 2),
                                            reduceMotion = reduceMotion
                                        )
                                    } else {
                                        Text(
                                            text = "No HR aggregate shared today",
                                            style = MaterialTheme.typography.headlineMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = "Daily average synchronized via Firestore",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(AppSpacing.md))

                            // Partner's Blood Oxygen SpO2 Card (Daily Aggregate Only)
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .iosGlass(shape = MaterialTheme.shapes.large),
                                shape = MaterialTheme.shapes.large,
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.Transparent
                                ),
                                border = null
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(AppSpacing.md),
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "Blood Oxygen (SpO2)",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "🫁 O2",
                                            fontWeight = FontWeight.Bold,
                                            color = StepsAccent
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(AppSpacing.xs))
                                    if (healthData.partnerAverageOxygenSaturation > 0.0) {
                                        NumberPopInText(
                                            text = String.format(java.util.Locale.US, "%.1f%%", healthData.partnerAverageOxygenSaturation),
                                            style = MaterialTheme.typography.headlineMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = StepsAccent
                                            ),
                                            animTrigger = Pair(popInTrigger, isPageActive && pagerState.currentPage == 2),
                                            reduceMotion = reduceMotion
                                        )
                                    } else {
                                        Text(
                                            text = "No SpO2 aggregate shared today",
                                            style = MaterialTheme.typography.headlineMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = "Daily average synchronized via Firestore",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }

                    }
                }
            }
            
            // Dock scroll clearance (GLS-10, APL-03)
            Spacer(modifier = Modifier.height(96.dp))
        }
        
        PullRefreshIndicator(
            refreshing = healthData.isSyncing,
            state = pullRefreshState,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding(),
            contentColor = SystemRed
        )
    }
}

// =========================================================================
// Backward-compatible delegates for package com.couplefit.ui.dashboard
// Underlying implementations modularized in com.couplefit.ui.dashboard.components.* (KRP-02)
// =========================================================================

@Composable
fun StatCard(
    title: String,
    subtitle: String = "",
    stepsProgress: Float,
    exerciseProgress: Float,
    stepsCount: Long? = null,
    activeMinutesCount: Long? = null,
    caloriesCount: Long? = null,
    onSyncClick: (() -> Unit)? = null,
    onAddSampleStepsClick: (() -> Unit)? = null,
    animTrigger: Any? = null,
    isExplicitRefresh: Boolean = true,
    reduceMotion: Boolean = false,
    staggerIndex: Int = 0,
    isPartner: Boolean = false,
    modifier: Modifier = Modifier
) = com.couplefit.ui.dashboard.components.StatCard(
    title = title,
    subtitle = subtitle,
    stepsProgress = stepsProgress,
    exerciseProgress = exerciseProgress,
    stepsCount = stepsCount,
    activeMinutesCount = activeMinutesCount,
    caloriesCount = caloriesCount,
    onSyncClick = onSyncClick,
    onAddSampleStepsClick = onAddSampleStepsClick,
    animTrigger = animTrigger,
    isExplicitRefresh = isExplicitRefresh,
    reduceMotion = reduceMotion,
    staggerIndex = staggerIndex,
    isPartner = isPartner,
    modifier = modifier
)

@Composable
fun SideBySideActivityCard(
    healthData: HealthDashboardUiState,
    reduceMotion: Boolean,
    modifier: Modifier = Modifier,
    animTrigger: Any? = null,
    onSendCheer: (String) -> Unit = {},
    onPingPartner: () -> Unit = {}
) = com.couplefit.ui.dashboard.components.SideBySideActivityCard(
    healthData = healthData,
    reduceMotion = reduceMotion,
    modifier = modifier,
    animTrigger = animTrigger,
    onSendCheer = onSendCheer,
    onPingPartner = onPingPartner
)

@Composable
fun TelemetryDiagnosticDialog(
    partnerName: String,
    lastUpdatedLabel: String,
    isStale: Boolean,
    onDismiss: () -> Unit,
    onPingPartner: () -> Unit
) = com.couplefit.ui.dashboard.components.TelemetryDiagnosticDialog(
    partnerName = partnerName,
    lastUpdatedLabel = lastUpdatedLabel,
    isStale = isStale,
    onDismiss = onDismiss,
    onPingPartner = onPingPartner
)

@Composable
fun PartnerOfflineCard(
    partnerName: String,
    partnerSteps: Long,
    partnerActiveMinutes: Long,
    partnerCalories: Long,
    partnerAvgHr: Double,
    lastUpdatedLabel: String,
    animTrigger: Any? = null,
    reduceMotion: Boolean = false,
    modifier: Modifier = Modifier
) = com.couplefit.ui.dashboard.components.PartnerOfflineCard(
    partnerName = partnerName,
    partnerSteps = partnerSteps,
    partnerActiveMinutes = partnerActiveMinutes,
    partnerCalories = partnerCalories,
    partnerAvgHr = partnerAvgHr,
    lastUpdatedLabel = lastUpdatedLabel,
    animTrigger = animTrigger,
    reduceMotion = reduceMotion,
    modifier = modifier
)

@Composable
fun MetricPill(
    label: String,
    value: String,
    color: Color,
    animTrigger: Any? = null,
    reduceMotion: Boolean = false,
    glyph: String? = null
) = com.couplefit.ui.dashboard.components.MetricPill(
    label = label,
    value = value,
    color = color,
    animTrigger = animTrigger,
    reduceMotion = reduceMotion,
    glyph = glyph
)

@Composable
fun OrbitalSyncPulse(
    isSyncing: Boolean,
    modifier: Modifier = Modifier
) = com.couplefit.ui.dashboard.components.OrbitalSyncPulse(
    isSyncing = isSyncing,
    modifier = modifier
)
