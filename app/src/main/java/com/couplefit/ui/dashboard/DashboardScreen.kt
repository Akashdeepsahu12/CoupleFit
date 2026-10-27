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
import com.couplefit.ui.components.ActivityRing
import com.couplefit.ui.components.MotionArcProgressGauge
import com.couplefit.ui.components.MotionPillChip
import com.couplefit.ui.components.SpinConfig
import com.couplefit.ui.components.SpinningCounter
import com.couplefit.ui.components.WeeklyStreakRow
import com.couplefit.ui.components.lenisScroll
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
import com.couplefit.ui.healthconnect.HealthConnectViewModel
import com.couplefit.ui.healthconnect.HealthDashboardUiState
import com.couplefit.ui.theme.*
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

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                healthConnectViewModel.onAppForeground()
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

    Box(modifier = modifier.fillMaxSize().pullRefresh(pullRefreshState).lenisScroll()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .systemBarsPadding()
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
            var isMedicationDismissed by remember { mutableStateOf(false) }
            if (!isMedicationDismissed) {
                MedicationDuePill(
                    medicationName = "Vitamin D3",
                    scheduledTime = "08:00 AM",
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

            // Sync status
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.md)
                    .padding(bottom = AppSpacing.xs),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
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
                        TextButton(
                            onClick = { healthConnectViewModel.writeMockData(1000L) },
                            shape = RoundedCornerShape(AppSpacing.sm)
                        ) {
                            Text("+1k Test Steps", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
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
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = pagerState.currentPage == 0,
                            borderColor = MaterialTheme.colorScheme.outline,
                            selectedBorderColor = VitalsAccent
                        )
                    )

                    // Tab 1: My Stats
                    FilterChip(
                        selected = pagerState.currentPage == 1,
                        onClick = {
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
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = pagerState.currentPage == 1,
                            borderColor = MaterialTheme.colorScheme.outline,
                            selectedBorderColor = StepsAccent
                        )
                    )

                    // Tab 2: Partner's Stats
                    FilterChip(
                        selected = pagerState.currentPage == 2,
                        onClick = {
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
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = pagerState.currentPage == 2,
                            borderColor = MaterialTheme.colorScheme.outline,
                            selectedBorderColor = SystemRed
                        )
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

                            // Dual Activity Motion Gauges with Double-Tap Cheer & Telemetry Status Pill (FLOW-02, FLOW-03)
                            SideBySideActivityCard(
                                healthData = healthData,
                                reduceMotion = reduceMotion,
                                onSendCheer = { cheerMessage ->
                                    healthConnectViewModel.sendNudgeToPartner(cheerMessage)
                                },
                                onPingPartner = {
                                    healthConnectViewModel.sendNudgeToPartner("Ping! Please sync your activity 📡")
                                }
                            )

                            Spacer(modifier = Modifier.height(AppSpacing.xs))

                            // Quick-Action Nudges elevated directly into the Thumb Zone under rings (WF-01, TST-06)
                            QuickNudgeRow(
                                partnerName = healthData.partnerName,
                                mySteps = healthData.steps,
                                partnerSteps = healthData.partnerSteps,
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

                            // Active Challenge Mini-Preview linking to Tab 2 Challenges (CSA-04 & Wireframe 1)
                            ActiveChallengePreviewCard(
                                challenge = challengeState.activeChallenge,
                                partnerName = healthData.partnerName,
                                onViewAllChallenges = onViewAllChallenges
                            )

                            Spacer(modifier = Modifier.height(AppSpacing.sm))

                            // InteractiveChart Section with Progressive Disclosure (WF-01)
                            var isVitalsChartExpanded by remember { mutableStateOf(false) }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateContentSize(animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
                                    .iosGlass(shape = MaterialTheme.shapes.large),
                                shape = MaterialTheme.shapes.large,
                                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                                border = null
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { isVitalsChartExpanded = !isVitalsChartExpanded }
                                            .padding(AppSpacing.md),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text("📈", fontSize = 18.sp)
                                            Column {
                                                Text(
                                                    text = "VITALS PULSE",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    letterSpacing = 0.5.sp
                                                )
                                                val avgBpm = if (healthData.averageHeartRate > 0) "${healthData.averageHeartRate} BPM Avg" else "Listening for pulse"
                                                Text(
                                                    text = "Live Heart Rate • $avgBpm",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                        Text(
                                            text = if (isVitalsChartExpanded) "Collapse ▲" else "Expand ▼",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = VitalsAccent
                                        )
                                    }

                                    if (isVitalsChartExpanded) {
                                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(220.dp)
                                                .padding(AppSpacing.md)
                                        ) {
                                            val chartPoints = if (healthData.heartRatePoints.isNotEmpty()) {
                                                healthData.heartRatePoints
                                            } else if (healthData.averageHeartRate > 0) {
                                                listOf(ChartDataPoint("Today", healthData.averageHeartRate.toFloat()))
                                            } else {
                                                emptyList()
                                            }

                                            if (chartPoints.isNotEmpty()) {
                                                InteractiveChart(
                                                    data = chartPoints,
                                                    lineColor = VitalsAccent,
                                                    metricTitle = "Heart Rate Pulse (BPM)",
                                                    reduceMotion = reduceMotion,
                                                    syncTrigger = if (isExplicitRefresh) syncTrigger else 0,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            } else {
                                                Box(
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = "Sync Health Connect to view interactive charts",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
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
                                animTrigger = if (isExplicitRefresh) (isPageActive && pagerState.currentPage == 1) else null,
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
                                val myChartData = if (healthData.heartRatePoints.isNotEmpty()) {
                                    healthData.heartRatePoints
                                } else if (healthData.averageHeartRate > 0) {
                                    listOf(ChartDataPoint("Today", healthData.averageHeartRate.toFloat()))
                                } else {
                                    emptyList()
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
                                animTrigger = if (isExplicitRefresh) (isPageActive && pagerState.currentPage == 2) else null,
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
                                lastUpdatedLabel = healthData.partnerLastUpdatedLabel
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
                                    Text(
                                        text = if (healthData.partnerAverageHeartRate > 0.0) {
                                            "${healthData.partnerAverageHeartRate.toInt()} BPM"
                                        } else {
                                            "No HR aggregate shared today"
                                        },
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (healthData.partnerAverageHeartRate > 0.0) VitalsAccent else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
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
                                            text = "O2",
                                            fontWeight = FontWeight.Bold,
                                            color = StepsAccent
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(AppSpacing.xs))
                                    Text(
                                        text = if (healthData.partnerAverageOxygenSaturation > 0.0) {
                                            String.format(java.util.Locale.US, "%.1f%%", healthData.partnerAverageOxygenSaturation)
                                        } else {
                                            "No SpO2 aggregate shared today"
                                        },
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (healthData.partnerAverageOxygenSaturation > 0.0) StepsAccent else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
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
) {
    // Staggered slide-up entrance: each card arrives slightly after the previous
    val enterOffsetY = remember { Animatable(if (reduceMotion) 0f else 80f) }
    val enterAlpha  = remember { Animatable(if (reduceMotion) 1f else 0f) }
    LaunchedEffect(animTrigger) {
        val delayMs = (staggerIndex * 80L).coerceAtMost(320L)
        kotlinx.coroutines.delay(delayMs)
        launch {
            enterOffsetY.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        }
        launch {
            enterAlpha.animateTo(
                targetValue = 1f,
                animationSpec = spring(stiffness = Spring.StiffnessLow)
            )
        }
    }
    var showMenu by remember { mutableStateOf(false) }
    var menuOffset by remember { mutableStateOf(DpOffset.Zero) }
    val haptic = LocalHapticFeedback.current
    
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (!reduceMotion && isPressed) 0.95f else 1f,
        animationSpec = if (reduceMotion) androidx.compose.animation.core.snap() else spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "cardScale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (!reduceMotion && isPressed) 0.85f else 1f,
        animationSpec = if (reduceMotion) androidx.compose.animation.core.snap() else spring(dampingRatio = 1f, stiffness = 500f),
        label = "cardAlpha"
    )
    
    // Partial UI Update: Animate only the numeric value itself
    val animStepsCount by androidx.compose.animation.core.animateIntAsState(
        targetValue = (stepsCount ?: (stepsProgress * 10000).toLong()).toInt(),
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 400),
        label = "stepsCount"
    )
    
    val animActiveMinsCount by androidx.compose.animation.core.animateIntAsState(
        targetValue = (activeMinutesCount ?: (exerciseProgress * 60).toLong()).toInt(),
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 400),
        label = "activeMinsCount"
    )
    
    val animCaloriesCount by androidx.compose.animation.core.animateIntAsState(
        targetValue = (caloriesCount ?: ((stepsProgress * 0.75f) * 600).toLong()).toInt(),
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 400),
        label = "caloriesCount"
    )

    Card(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha * enterAlpha.value
                translationY = enterOffsetY.value
            }
            .iosGlass(shape = MaterialTheme.shapes.large)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { 
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onLongPress = { offset ->
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        menuOffset = DpOffset(offset.x.toDp(), offset.y.toDp())
                        showMenu = true
                    }
                )
            },
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        border = null
    ) {
        Box {
            Column(
                modifier = Modifier.fillMaxSize().padding(AppSpacing.md),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                if (subtitle.isNotEmpty()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = AppSpacing.xxs),
                        maxLines = 1
                    )
                }
                Spacer(modifier = Modifier.height(AppSpacing.xs))
                // Motion Arc Progress Gauge inspired by 558.mp4 motion graphics
                MotionArcProgressGauge(
                    progress = stepsProgress,
                    currentValue = stepsCount ?: (stepsProgress * 10000).toLong(),
                    goalValue = 10000L,
                    distanceKm = (animStepsCount * 0.000762).coerceAtLeast(0.0),
                    calories = animCaloriesCount.toLong(),
                    activeMinutes = animActiveMinsCount.toLong(),
                    gradientColors = if (isPartner) listOf(MotionPartnerStart, MotionPartnerMid, MotionPartnerEnd) else listOf(MotionCyanStart, MotionSkyMid, MotionBlueEnd),
                    glowColor = if (isPartner) MotionPartnerGlow else MotionGlowCyan,
                    animTrigger = animTrigger,
                    reduceMotion = reduceMotion
                )
            }
            
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                offset = menuOffset
            ) {
                if (onSyncClick != null) {
                    DropdownMenuItem(
                        text = { Text("Sync with Health Connect") },
                        onClick = {
                            showMenu = false
                            onSyncClick()
                        }
                    )
                }
                if (onAddSampleStepsClick != null) {
                    DropdownMenuItem(
                        text = { Text("Add 1,000 Test Steps") },
                        onClick = {
                            showMenu = false
                            onAddSampleStepsClick()
                        }
                    )
                }
                DropdownMenuItem(
                    text = { Text("Share with Partner") },
                    onClick = { showMenu = false }
                )
            }
        }
    }
}

@Composable
fun SideBySideActivityCard(
    healthData: HealthDashboardUiState,
    reduceMotion: Boolean,
    modifier: Modifier = Modifier,
    onSendCheer: (String) -> Unit = {},
    onPingPartner: () -> Unit = {}
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var showDiagnosticDialog by remember { mutableStateOf(false) }
    var cheerCooldownUntil by remember { mutableLongStateOf(0L) }
    var cheerTriggerCount by remember { mutableIntStateOf(0) }
    var showCheerCelebration by remember { mutableStateOf(false) }
    var userClosureCelebrated by remember { mutableStateOf(false) }
    var partnerClosureCelebrated by remember { mutableStateOf(false) }

    LaunchedEffect(healthData.stepsProgress) {
        if (healthData.stepsProgress >= 1f && !userClosureCelebrated) {
            userClosureCelebrated = true
            CoupleFitHaptics.performHeartbeat(context, haptic)
        }
    }

    LaunchedEffect(healthData.partnerStepsProgress) {
        if (healthData.partnerStepsProgress >= 1f && !partnerClosureCelebrated) {
            partnerClosureCelebrated = true
            CoupleFitHaptics.performHeartbeat(context, haptic)
        }
    }

    val isStale = remember(healthData.partnerLastUpdated) {
        val ts = healthData.partnerLastUpdated
        if (ts == null || ts == 0L) true
        else (System.currentTimeMillis() - ts) > 15 * 60 * 1000L // Stale if older than 15 mins
    }

    AuroraTelemetryMesh(
        modifier = modifier.fillMaxWidth()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .iosGlass(shape = MaterialTheme.shapes.large),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = null
        ) {
            Column(modifier = Modifier.padding(AppSpacing.md)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ACTIVITY GAUGES",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                // Interactive Telemetry Status Pill (FLOW-03)
                Surface(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        showDiagnosticDialog = true
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isStale) SystemOrange.copy(alpha = 0.18f) else SystemGreen.copy(alpha = 0.15f),
                    border = BorderStroke(
                        1.dp,
                        if (isStale) SystemOrange.copy(alpha = 0.45f) else SystemGreen.copy(alpha = 0.4f)
                    )
                ) {
                    Text(
                        text = if (isStale) "🟡 ${healthData.partnerLastUpdatedLabel} • Cached ℹ️"
                               else "🟢 ${healthData.partnerLastUpdatedLabel} • Live ⚡",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = if (isStale) SystemOrange else SystemGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.sm))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // User Column
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "You",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MotionCyanStart
                    )
                    Spacer(modifier = Modifier.height(AppSpacing.xs))
                    Box(contentAlignment = Alignment.Center) {
                        MotionArcProgressGauge(
                            progress = healthData.stepsProgress,
                            currentValue = healthData.steps,
                            compact = true,
                            gradientColors = listOf(MotionCyanStart, MotionSkyMid, MotionBlueEnd),
                            glowColor = MotionGlowCyan,
                            reduceMotion = reduceMotion
                        )
                        ConfettiParticleExplosion(
                            visible = userClosureCelebrated,
                            modifier = Modifier.size(160.dp),
                            radius = 70.dp,
                            reduceMotion = reduceMotion
                        )
                    }
                    Spacer(modifier = Modifier.height(AppSpacing.xs))
                    Text(
                        text = "${healthData.activeMinutes}m • ${healthData.caloriesBurned} kcal",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .height(160.dp)
                        .width(1.dp)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                )

                // Partner Column with Double-Tap Cheer Micro-Interaction (FLOW-02, DSN-02)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = healthData.partnerName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MotionPartnerStart
                    )
                    Spacer(modifier = Modifier.height(AppSpacing.xs))

                    // Direct manipulation double-tap target on partner ring
                    Box(
                        modifier = Modifier
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onDoubleTap = {
                                        val now = System.currentTimeMillis()
                                        if (now > cheerCooldownUntil) {
                                            cheerCooldownUntil = now + 3000L // 3s cooldown limiter
                                            cheerTriggerCount++
                                            showCheerCelebration = true
                                            CoupleFitHaptics.performHeartbeat(context, haptic)
                                            onSendCheer("Cheered your steps! 🔥❤️")
                                        }
                                    }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        MotionArcProgressGauge(
                            progress = healthData.partnerStepsProgress,
                            currentValue = healthData.partnerSteps,
                            compact = true,
                            gradientColors = listOf(MotionPartnerStart, MotionPartnerMid, MotionPartnerEnd),
                            glowColor = MotionPartnerGlow,
                            reduceMotion = reduceMotion
                        )

                        // High-performance Floating Heart & Flame Cluster (FLOW-02, DSN-02)
                        FloatingHeartCheerCluster(
                            visible = showCheerCelebration,
                            modifier = Modifier.size(140.dp),
                            reduceMotion = reduceMotion,
                            onAnimationEnd = { showCheerCelebration = false }
                        )

                        // 100% Daily Ring Closure Confetti (DSN-02, MIC-02)
                        ConfettiParticleExplosion(
                            visible = partnerClosureCelebrated,
                            modifier = Modifier.size(160.dp),
                            radius = 70.dp,
                            reduceMotion = reduceMotion
                        )
                    }

                    Spacer(modifier = Modifier.height(AppSpacing.xs))
                    Text(
                        text = "${healthData.partnerActiveMinutes}m • ${healthData.partnerCaloriesBurned} kcal",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Double-tap ring to cheer 💖",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .clickable {
                                val now = System.currentTimeMillis()
                                if (now > cheerCooldownUntil) {
                                    cheerCooldownUntil = now + 3000L // 3s cooldown limiter
                                    cheerTriggerCount++
                                    showCheerCelebration = true
                                    CoupleFitHaptics.performHeartbeat(context, haptic)
                                    onSendCheer("Cheered your steps! 🔥❤️")
                                }
                            }
                    )
                }
            }
        }
    }
    }

    // Telemetry Diagnostic Dialog (FLOW-03)
    if (showDiagnosticDialog) {
        TelemetryDiagnosticDialog(
            partnerName = healthData.partnerName,
            lastUpdatedLabel = healthData.partnerLastUpdatedLabel,
            isStale = isStale,
            onDismiss = { showDiagnosticDialog = false },
            onPingPartner = onPingPartner
        )
    }
}

/**
 * Telemetry Diagnostic Dialog providing transparent insight into partner sync status
 * and an immediate, high-priority "Ping Partner Device 📡" action (FLOW-03).
 */
@Composable
fun TelemetryDiagnosticDialog(
    partnerName: String,
    lastUpdatedLabel: String,
    isStale: Boolean,
    onDismiss: () -> Unit,
    onPingPartner: () -> Unit
) {
    var pingSent by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.iosGlass(
            shape = RoundedCornerShape(28.dp),
            tintColor = GlassPrimitives.SolidSlateModal,
            blurRadius = GlassPrimitives.BlurModal
        ),
        containerColor = Color.Transparent,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = if (isStale) "🟡 Partner Sync Diagnostics" else "🟢 Partner Telemetry Live",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (isStale)
                        "$partnerName's device has not synced recently ($lastUpdatedLabel). You are viewing cached offline Room SSOT records."
                    else
                        "$partnerName's telemetry is currently in live synchronization with Health Connect and Firestore.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "• Room SSOT cache remains available offline\n• Tap Ping to send high-priority sync wake-up to $partnerName",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (pingSent) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SystemGreen.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, SystemGreen.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = "✓ High-priority sync ping sent to $partnerName!",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = SystemGreen,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            FilledTonalButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onPingPartner()
                    pingSent = true
                }
            ) {
                Text("Ping Partner Device 📡", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun PartnerOfflineCard(
    partnerName: String,
    partnerSteps: Long,
    partnerActiveMinutes: Long,
    partnerCalories: Long,
    partnerAvgHr: Double,
    lastUpdatedLabel: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .iosGlass(shape = MaterialTheme.shapes.large),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = null
    ) {
        Column(modifier = Modifier.padding(AppSpacing.md)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "$partnerName's Telemetry",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Room SSOT cache • Available offline",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SystemOrange.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, SystemOrange.copy(alpha = 0.35f))
                ) {
                    Text(
                        text = lastUpdatedLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = SystemOrange,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.sm))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    SpinningCounter(
                        value = partnerSteps,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = StepsAccent
                        ),
                        config = SpinConfig(
                            cell = 20.dp,
                            cellWidth = 11.dp,
                            spinBlur = 1.5.dp
                        ),
                        animateOnFirstComposition = false
                    )
                    Text(
                        text = "Steps",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                MetricPill("Active", "${partnerActiveMinutes}m", ExerciseAccent)
                MetricPill("Calories", "%,d kcal".format(partnerCalories), CaloriesAccent)
                if (partnerAvgHr > 0.0) {
                    MetricPill("Avg HR", "${partnerAvgHr.toInt()} BPM", VitalsAccent)
                }
            }
        }
    }
}

@Composable
fun MetricPill(
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
