package com.couplefit.ui.challenge

import com.couplefit.ui.theme.duelCardPress
import com.couplefit.ui.theme.iosGlass
import com.couplefit.ui.theme.iosClickable
import com.couplefit.ui.theme.CoupleFitHaptics

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.couplefit.data.local.CoupleChallengeEntity
import com.couplefit.ui.theme.AppSpacing
import com.couplefit.ui.theme.SystemGreen
import com.couplefit.ui.theme.SystemRed

enum class ChallengeSubTab(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val badgeCount: Int
) {
    ACTIVE_DUELS(
        label = "Active Duels",
        selectedIcon = Icons.Filled.Bolt,
        unselectedIcon = Icons.Outlined.Bolt,
        badgeCount = 1
    ),
    TROPHIES(
        label = "Trophies & Vault",
        selectedIcon = Icons.Filled.EmojiEvents,
        unselectedIcon = Icons.Outlined.EmojiEvents,
        badgeCount = 6
    )
}

data class QuickDuelFormat(
    val title: String,
    val description: String,
    val icon: String,
    val type: String,
    val goal: Long,
    val durationText: String
)

data class TrophyAchievement(
    val id: String,
    val icon: String,
    val title: String,
    val subtitle: String,
    val category: String, // "STEPS", "STREAKS", "SPEED"
    val isUnlocked: Boolean,
    val progress: Float = 1.0f,
    val progressText: String = "Completed"
)

data class CompletedDuelRecord(
    val title: String,
    val winner: String,
    val dateText: String,
    val scoreDelta: String,
    val isUserWinner: Boolean
)

/**
 * Challenges & Duels Screen (Tab 2).
 * Designed according to `card-sort-analysis.md` §7.2 (Wireframe 2)
 * with dedicated, accessible Material 3 sub-navigation (LOC-NAV-02).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengesScreen(
    modifier: Modifier = Modifier,
    viewModel: ChallengeViewModel = hiltViewModel()
) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val uiState by viewModel.uiState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(ChallengeSubTab.ACTIVE_DUELS) }
    var selectedTrophyCategory by remember { mutableStateOf("ALL") }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Challenges & Duels",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                actions = {
                    FilledTonalButton(
                        onClick = {
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                            showCreateDialog = true
                        },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New Challenge",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New Challenge")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Local Sub-Navigation Control (LOC-NAV-02 & NAV-04): M3 SecondaryTabRow with Haptics
            SecondaryTabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth()
            ) {
                ChallengeSubTab.entries.forEach { tab ->
                    val isSelected = selectedTab == tab
                    Tab(
                        selected = isSelected,
                        onClick = {
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                            selectedTab = tab
                        },
                        text = {
                            val activeChallengesCount = uiState.challenges.count { it.status == "ACTIVE" }
                            val dynamicBadgeCount = when (tab) {
                                ChallengeSubTab.ACTIVE_DUELS -> activeChallengesCount
                                ChallengeSubTab.TROPHIES -> 6
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = tab.label,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                                if (dynamicBadgeCount > 0) {
                                    Badge(
                                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                    ) {
                                        Text(text = "$dynamicBadgeCount")
                                    }
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        modifier = Modifier.semantics {
                            val activeChallengesCount = uiState.challenges.count { it.status == "ACTIVE" }
                            val dynamicBadgeCount = when (tab) {
                                ChallengeSubTab.ACTIVE_DUELS -> activeChallengesCount
                                ChallengeSubTab.TROPHIES -> 6
                            }
                            contentDescription = "${tab.label} tab, $dynamicBadgeCount items"
                        }
                    )
                }
            }

            // Smooth animated content transition between sub-navigation surfaces
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    if (targetState.ordinal > initialState.ordinal) {
                        (slideInHorizontally(animationSpec = tween(220)) { width -> width } + fadeIn(tween(220)))
                            .togetherWith(slideOutHorizontally(animationSpec = tween(180)) { width -> -width } + fadeOut(tween(180)))
                    } else {
                        (slideInHorizontally(animationSpec = tween(220)) { width -> -width } + fadeIn(tween(220)))
                            .togetherWith(slideOutHorizontally(animationSpec = tween(180)) { width -> width } + fadeOut(tween(180)))
                    }
                },
                label = "ChallengesSubNavTransition",
                modifier = Modifier.fillMaxSize()
            ) { targetSubTab ->
                when (targetSubTab) {
                    ChallengeSubTab.ACTIVE_DUELS -> {
                        ActiveDuelsSection(
                            challenges = uiState.challenges,
                            partnerName = uiState.partnerName,
                            onStartFormat = { _, _ ->
                                showCreateDialog = true
                            }
                        )
                    }
                    ChallengeSubTab.TROPHIES -> {
                        TrophiesVaultSection(
                            selectedCategory = selectedTrophyCategory,
                            onCategorySelected = { selectedTrophyCategory = it }
                        )
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        StartChallengeDialog(
            onDismiss = { showCreateDialog = false },
            onStartChallenge = { type, goal ->
                viewModel.createChallenge(coupleId = "couple_1", type = type, goal = goal)
            }
        )
    }
}

@Composable
private fun ActiveDuelsSection(
    challenges: List<CoupleChallengeEntity>,
    partnerName: String = "Partner",
    onStartFormat: (type: String, goal: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val quickFormats = remember {
        listOf(
            QuickDuelFormat("50k Steps Race", "7-Day showdown", "🏃", "STEPS_RACE", 50_000L, "7 Days"),
            QuickDuelFormat("Active Streak", "30 mins/day consistency", "🔥", "ACTIVE_MINUTES_STREAK", 7L, "7 Days"),
            QuickDuelFormat("Weekend Sprint", "Fast 48-hour challenge", "⚡", "STEPS_RACE", 30_000L, "2 Days"),
            QuickDuelFormat("Century Duel", "100k ultimate endurance", "🏆", "STEPS_RACE", 100_000L, "14 Days")
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = AppSpacing.md),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        contentPadding = PaddingValues(vertical = AppSpacing.md)
    ) {
        item {
            Text(
                text = "ACTIVE HEAD-TO-HEAD DUELS",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
        }

        if (challenges.isEmpty()) {
            item {
                // Interactive Demo Card
                val sampleChallenge = remember {
                    CoupleChallengeEntity(
                        id = "demo_duel_1",
                        coupleId = "couple_demo",
                        type = "STEPS_RACE",
                        goal = 50_000L,
                        start = "2026-10-21",
                        end = "2026-10-28",
                        myProgress = 24_500L,
                        partnerProgress = 28_200L,
                        status = "ACTIVE"
                    )
                }
                SideBySideChallengeCard(
                    challenge = sampleChallenge,
                    partnerName = partnerName
                )
            }
        } else {
            items(challenges) { challenge ->
                SideBySideChallengeCard(
                    challenge = challenge,
                    partnerName = partnerName
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(AppSpacing.xs))
            Text(
                text = "AVAILABLE DUEL FORMATS",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
        }

        items(quickFormats) { format ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .duelCardPress(onClickLabel = "Start ${format.title}") {
                        onStartFormat(format.type, format.goal)
                    }
                    .iosGlass(shape = RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                ),
                border = null
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppSpacing.md),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(format.icon, fontSize = 22.sp)
                            }
                        }
                        Column {
                            Text(
                                text = format.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "${format.description} • ${format.durationText}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    FilledTonalButton(
                        onClick = {
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                            onStartFormat(format.type, format.goal)
                        },
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Start ⚡", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun TrophiesVaultSection(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val allAchievements = remember {
        listOf(
            TrophyAchievement("1", "🥇", "100k Steps", "Weekly total milestone", "STEPS", true),
            TrophyAchievement("2", "🥈", "5-Day Sync", "Consecutive telemetry", "STREAKS", true),
            TrophyAchievement("3", "🥉", "First 10k", "Single-day distance", "STEPS", true),
            TrophyAchievement("4", "🔥", "Week Streak", "5 of 7 days logged", "STREAKS", false, 0.71f, "5 / 7 Days"),
            TrophyAchievement("5", "⚡", "Speed Demon", "10k in <60 minutes", "SPEED", true),
            TrophyAchievement("6", "❤️", "Sync Master", "99% couple synchrony", "STREAKS", true)
        )
    }

    val pastDuels = remember {
        listOf(
            CompletedDuelRecord("Weekend Step Showdown", "You Won", "Oct 20, 2026", "+2,400 steps", true),
            CompletedDuelRecord("Autumn Calorie Burn", "Partner Won", "Oct 14, 2026", "+350 kcal", false),
            CompletedDuelRecord("First Couple 10k Run", "You Won", "Oct 07, 2026", "+1,200 steps", true)
        )
    }

    val filteredAchievements = remember(selectedCategory) {
        if (selectedCategory == "ALL") allAchievements
        else allAchievements.filter { it.category == selectedCategory }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = AppSpacing.md),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        contentPadding = PaddingValues(vertical = AppSpacing.md)
    ) {
        item {
            Text(
                text = "TROPHY & MILESTONE VAULT",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Head-to-Head Record Hero Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .duelCardPress(onClickLabel = "View Couple Head-to-Head Record")
                    .iosGlass(shape = RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                ),
                border = null
            ) {
                Column(
                    modifier = Modifier.padding(AppSpacing.md),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                ) {
                    Text(
                        text = "COUPLE HEAD-TO-HEAD RECORD",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "8 Wins",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "You (57%)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("VS", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "6 Wins",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Partner (43%)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "14 Total Duels Played",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Current Streak: 3 🔥 Wins",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = SystemGreen
                        )
                    }
                }
            }
        }

        // Category Filter Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf(
                    "ALL" to "All (6)",
                    "STEPS" to "Steps",
                    "STREAKS" to "Streaks",
                    "SPEED" to "Speed"
                ).forEach { (catKey, label) ->
                    val isSelected = selectedCategory == catKey
                    item {
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                                onCategorySelected(catKey)
                            },
                            label = {
                                Text(
                                    text = label,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }
        }

        // Trophy Showcase Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .duelCardPress(onClickLabel = "View Trophy & Milestone Showcase"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier.padding(AppSpacing.md),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
                ) {
                    filteredAchievements.chunked(3).forEachIndexed { rowIndex, rowItems ->
                        if (rowIndex > 0) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            rowItems.forEach { trophy ->
                                TrophyBadge(
                                    icon = trophy.icon,
                                    title = trophy.title,
                                    subtitle = trophy.subtitle,
                                    isUnlocked = trophy.isUnlocked,
                                    progress = trophy.progress,
                                    progressText = trophy.progressText
                                )
                            }
                        }
                    }
                }
            }
        }

        // Completed Duels History
        item {
            Spacer(modifier = Modifier.height(AppSpacing.xs))
            Text(
                text = "PAST DUEL ARCHIVE",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
        }

        items(pastDuels) { duel ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .duelCardPress(onClickLabel = "View ${duel.title} record"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppSpacing.md),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = duel.title,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "${duel.dateText} • Lead: ${duel.scoreDelta}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (duel.isUserWinner) Color(0x2232D74B) else Color(0x22FF4568)
                    ) {
                        Text(
                            text = duel.winner,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (duel.isUserWinner) SystemGreen else SystemRed,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TrophyBadge(
    icon: String,
    title: String,
    subtitle: String,
    isUnlocked: Boolean = true,
    progress: Float = 1.0f,
    progressText: String = "Completed"
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    var isTapped by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isTapped) 1.15f else 1.0f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMedium),
        finishedListener = { isTapped = false },
        label = "trophyScale"
    )

    // ANM-08: Apple Watch-style 3D specular shine sweep
    val infiniteTransition = rememberInfiniteTransition(label = "trophyShine")
    val shineOffset by infiniteTransition.animateFloat(
        initialValue = -1.2f,
        targetValue = 2.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shineOffset"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .width(96.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .semantics {
                contentDescription = "$title: $subtitle ($progressText)"
            }
            .then(
                if (isUnlocked) {
                    Modifier.iosClickable(pressedScale = 0.92f) {
                        isTapped = true
                        CoupleFitHaptics.performCelebration(context, haptic)
                    }
                } else Modifier
            )
    ) {
        Surface(
            shape = CircleShape,
            color = if (isUnlocked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .size(52.dp)
                .then(
                    if (isUnlocked) {
                        Modifier.background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.White.copy(alpha = 0.28f),
                                    Color.Transparent
                                ),
                                start = Offset(shineOffset * 100f, 0f),
                                end = Offset(shineOffset * 100f + 50f, 100f)
                            ),
                            shape = CircleShape
                        )
                    } else Modifier
                )
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = icon,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.then(
                        if (!isUnlocked) Modifier.background(Color.Transparent) else Modifier
                    )
                )
            }
        }
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = if (isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Text(
            text = if (isUnlocked) "Unlocked ✓" else progressText,
            style = MaterialTheme.typography.labelSmall,
            color = if (isUnlocked) SystemGreen else MaterialTheme.colorScheme.outline
        )
        if (!isUnlocked) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .width(64.dp)
                    .height(3.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.outlineVariant
            )
        }
    }
}
