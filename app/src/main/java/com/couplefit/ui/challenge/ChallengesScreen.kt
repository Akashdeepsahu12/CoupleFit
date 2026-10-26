package com.couplefit.ui.challenge

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.couplefit.data.local.CoupleChallengeEntity
import com.couplefit.ui.theme.AppSpacing

/**
 * Challenges & Duels Screen (Tab 2).
 * Designed according to `card-sort-analysis.md` §7.2 (Wireframe 2).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengesScreen(
    modifier: Modifier = Modifier,
    viewModel: ChallengeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
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
                        onClick = { showCreateDialog = true },
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
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
        ) {
            item {
                Text(
                    text = "ACTIVE DUELS",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = AppSpacing.sm)
                )
            }

            if (uiState.challenges.isEmpty()) {
                item {
                    // Demo fallback card for immediate interactive experience
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
                        partnerName = "Partner"
                    )
                }
            } else {
                items(uiState.challenges) { challenge ->
                    SideBySideChallengeCard(
                        challenge = challenge,
                        partnerName = "Partner"
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(AppSpacing.sm))
                Text(
                    text = "TROPHY & MILESTONE VAULT",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(AppSpacing.md),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            TrophyBadge(icon = "🥇", title = "100k Steps", subtitle = "Unlocked")
                            TrophyBadge(icon = "🥈", title = "5-Day Sync", subtitle = "Unlocked")
                            TrophyBadge(icon = "🥉", title = "First 10k", subtitle = "Unlocked")
                        }
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
private fun TrophyBadge(
    icon: String,
    title: String,
    subtitle: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = icon, style = MaterialTheme.typography.headlineMedium)
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline
        )
    }
}
