package com.couplefit.ui.trends

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.couplefit.ui.theme.AppSpacing
import com.couplefit.ui.theme.SystemGreen

enum class TelemetryPerspective(val label: String, val icon: String) {
    COMBINED("Both", "👥"),
    YOU("You", "👤"),
    PARTNER("Partner", "💖")
}

/**
 * Health & Trends Screen (Tab 3).
 * Designed according to `card-sort-analysis.md` §7.2 (Wireframe 3).
 * Upgraded with NAV-04 (tactile haptic feedback & animated segmented controls)
 * and NAV-05 (bilateral telemetry perspective filtering: Both, You, Partner).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthTrendsScreen(
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var selectedTimeHorizon by remember { mutableStateOf("Week") }
    var selectedPerspective by remember { mutableStateOf(TelemetryPerspective.COMBINED) }

    val timeHorizons = remember { listOf("Day", "Week", "Month", "Year") }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Health & Trends",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Local Sub-Navigation Sub-Header (LOC-NAV-01 & NAV-04): Animated M3 Segmented Button Timeframe Row
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.md, vertical = AppSpacing.xs)
            ) {
                timeHorizons.forEachIndexed { index, horizon ->
                    val isSelected = selectedTimeHorizon == horizon
                    SegmentedButton(
                        selected = isSelected,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            selectedTimeHorizon = horizon
                        },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = timeHorizons.size),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            activeContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            inactiveContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text(
                            text = horizon,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }

            // NAV-05 Perspective Selector: Local Segmentation to Unclutter Historical Charts
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.md, vertical = AppSpacing.xs)
            ) {
                TelemetryPerspective.entries.forEachIndexed { index, perspective ->
                    val isSelected = selectedPerspective == perspective
                    SegmentedButton(
                        selected = isSelected,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            selectedPerspective = perspective
                        },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = TelemetryPerspective.entries.size),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            activeContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            inactiveContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(perspective.icon, fontSize = 13.sp)
                            Text(
                                text = perspective.label,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = AppSpacing.md),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
                contentPadding = PaddingValues(vertical = AppSpacing.sm)
            ) {
                item {
                    val perspectiveTitle = when (selectedPerspective) {
                        TelemetryPerspective.COMBINED -> "STEPS & ACTIVITY COMPARISON ($selectedTimeHorizon • Both Partners)"
                        TelemetryPerspective.YOU -> "YOUR PERSONAL TELEMETRY ($selectedTimeHorizon)"
                        TelemetryPerspective.PARTNER -> "PARTNER'S TELEMETRY ($selectedTimeHorizon)"
                    }
                    Text(
                        text = perspectiveTitle,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = AppSpacing.xs)
                    )
                }

                // Dynamic Perspective Telemetry Card (NAV-05)
                item {
                    AnimatedContent(
                        targetState = selectedPerspective,
                        transitionSpec = {
                            fadeIn(tween(180)) togetherWith fadeOut(tween(140))
                        },
                        label = "PerspectiveCardTransition"
                    ) { perspective ->
                        when (perspective) {
                            TelemetryPerspective.COMBINED -> {
                                CombinedTelemetryCard(selectedTimeHorizon = selectedTimeHorizon)
                            }
                            TelemetryPerspective.YOU -> {
                                SoloTelemetryCard(
                                    ownerName = "You",
                                    steps = "48,200",
                                    goal = "50,000",
                                    ratio = 0.96f,
                                    dailyAvg = "6,885",
                                    activeMins = "280",
                                    calories = "2,450",
                                    accentColor = MaterialTheme.colorScheme.primary
                                )
                            }
                            TelemetryPerspective.PARTNER -> {
                                SoloTelemetryCard(
                                    ownerName = "Partner",
                                    steps = "44,200",
                                    goal = "50,000",
                                    ratio = 0.88f,
                                    dailyAvg = "6,314",
                                    activeMins = "252",
                                    calories = "2,180",
                                    accentColor = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "MEDICATION & SUPPLEMENT TRACKER",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier.padding(AppSpacing.md),
                            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                        ) {
                            MedicationRow(name = "Vitamin D3", time = "08:00 AM", taken = true)
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                            MedicationRow(name = "Omega-3", time = "01:00 PM", taken = true)
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                            MedicationRow(name = "Magnesium Glycinate", time = "10:00 PM", taken = false)
                        }
                    }
                }

                item {
                    Text(
                        text = "SENSOR DIAGNOSTICS & SYNC STATUS",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier.padding(AppSpacing.md),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Health Connect Integration", style = MaterialTheme.typography.bodyMedium)
                                Text("🟢 Connected", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Last Background Sync", style = MaterialTheme.typography.bodyMedium)
                                Text("2 min ago", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CombinedTelemetryCard(selectedTimeHorizon: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$selectedTimeHorizon Combined Steps",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                    text = "92,400 / 100k",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            LinearProgressIndicator(
                progress = { 0.92f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Text(
                text = "You: 48,200 steps (52%) • Partner: 44,200 steps (48%)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Couple Synchrony: 94% 🔥",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = SystemGreen
                )
                Text(
                    text = "Total Active: 532 mins",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SoloTelemetryCard(
    ownerName: String,
    steps: String,
    goal: String,
    ratio: Float,
    dailyAvg: String,
    activeMins: String,
    calories: String,
    accentColor: androidx.compose.ui.graphics.Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$ownerName's Total Steps",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                    text = "$steps / $goal",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = accentColor
                )
            }

            LinearProgressIndicator(
                progress = { ratio },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = accentColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Avg: $dailyAvg steps/day",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${(ratio * 100).toInt()}% of Goal",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = accentColor
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "⏱️ $activeMins Active Mins",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "🔥 $calories kcal Burned",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun MedicationRow(
    name: String,
    time: String,
    taken: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = "Scheduled: $time",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Badge(
            containerColor = if (taken) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
        ) {
            Text(
                text = if (taken) "Taken ✓" else "Pending ⏱️",
                color = if (taken) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}
