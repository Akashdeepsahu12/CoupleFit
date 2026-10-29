package com.couplefit.ui.trends

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.couplefit.ui.theme.*

enum class TelemetryPerspective(val label: String, val icon: String) {
    COMBINED("Both", "👥"),
    YOU("You", "👤"),
    PARTNER("Partner", "💖")
}

/**
 * Health & Trends Screen (Tab 3).
 * Designed according to `card-sort-analysis.md` §7.2 (Wireframe 3).
 * Upgraded with NAV-04 (tactile haptic feedback) and NAV-05 (bilateral telemetry perspective).
 *
 * Enhanced with ANM-11: Fluid Timeframe Switcher with spring-driven gliding capsule indicator
 * (`dampingRatio = 0.75f, stiffness = 400f`) and smooth ratio interpolation in telemetry cards.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthTrendsScreen(
    modifier: Modifier = Modifier
) {
    var selectedTimeHorizon by remember { mutableStateOf("Week") }
    var selectedPerspective by remember { mutableStateOf(TelemetryPerspective.COMBINED) }

    val timeHorizons = remember { listOf("Day", "Week", "Month", "Year") }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Health & Trends",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
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
            // ANM-11: Fluid Spring-Driven Timeframe Switcher
            FluidSegmentedSwitcher(
                items = timeHorizons,
                selectedItem = selectedTimeHorizon,
                onItemSelected = { selectedTimeHorizon = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.md, vertical = AppSpacing.xs)
            ) { horizon, isSelected ->
                Text(
                    text = horizon,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    style = MaterialTheme.typography.labelMedium
                )
            }

            // NAV-05 & ANM-11: Fluid Perspective Switcher (Both, You, Partner)
            FluidSegmentedSwitcher(
                items = TelemetryPerspective.entries,
                selectedItem = selectedPerspective,
                onItemSelected = { selectedPerspective = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.md, vertical = AppSpacing.xs)
            ) { perspective, isSelected ->
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
                                    timeHorizon = selectedTimeHorizon,
                                    steps = when (selectedTimeHorizon) {
                                        "Day" -> "8,450"
                                        "Week" -> "48,200"
                                        "Month" -> "198,400"
                                        else -> "2,420,000"
                                    },
                                    goal = when (selectedTimeHorizon) {
                                        "Day" -> "10,000"
                                        "Week" -> "50,000"
                                        "Month" -> "200,000"
                                        else -> "2,500,000"
                                    },
                                    targetRatio = when (selectedTimeHorizon) {
                                        "Day" -> 0.845f
                                        "Week" -> 0.964f
                                        "Month" -> 0.992f
                                        else -> 0.968f
                                    },
                                    dailyAvg = "6,885",
                                    activeMins = when (selectedTimeHorizon) {
                                        "Day" -> "45"
                                        "Week" -> "280"
                                        "Month" -> "1,120"
                                        else -> "13,440"
                                    },
                                    calories = when (selectedTimeHorizon) {
                                        "Day" -> "420"
                                        "Week" -> "2,450"
                                        "Month" -> "10,200"
                                        else -> "122,500"
                                    },
                                    accentColor = CoupleFitIdentityTokens.UserPrimary // Electric Cyan (DSN-01)
                                )
                            }
                            TelemetryPerspective.PARTNER -> {
                                SoloTelemetryCard(
                                    ownerName = "Partner",
                                    timeHorizon = selectedTimeHorizon,
                                    steps = when (selectedTimeHorizon) {
                                        "Day" -> "7,800"
                                        "Week" -> "44,200"
                                        "Month" -> "185,600"
                                        else -> "2,280,000"
                                    },
                                    goal = when (selectedTimeHorizon) {
                                        "Day" -> "10,000"
                                        "Week" -> "50,000"
                                        "Month" -> "200,000"
                                        else -> "2,500,000"
                                    },
                                    targetRatio = when (selectedTimeHorizon) {
                                        "Day" -> 0.780f
                                        "Week" -> 0.884f
                                        "Month" -> 0.928f
                                        else -> 0.912f
                                    },
                                    dailyAvg = "6,314",
                                    activeMins = when (selectedTimeHorizon) {
                                        "Day" -> "40"
                                        "Week" -> "252"
                                        "Month" -> "1,040"
                                        else -> "12,200"
                                    },
                                    calories = when (selectedTimeHorizon) {
                                        "Day" -> "380"
                                        "Week" -> "2,180"
                                        "Month" -> "9,100"
                                        else -> "110,400"
                                    },
                                    accentColor = CoupleFitIdentityTokens.PartnerPrimary // Vibrant Coral (DSN-01)
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .iosGlass(shape = MaterialTheme.shapes.large),
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Transparent
                        ),
                        border = null
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .iosGlass(shape = MaterialTheme.shapes.large),
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Transparent
                        ),
                        border = null
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

                // Dock scroll clearance (GLS-10, APL-03)
                item {
                    Spacer(modifier = Modifier.height(96.dp))
                }
            }
        }
    }
}

/**
 * ANM-11: Fluid Spring-Driven Gliding Segmented Switcher.
 * Replaces rigid segmented borders with an Apple-style gliding capsule pill
 * with calibrated spring physics (`dampingRatio = 0.75f, stiffness = 400f`).
 */
@Composable
fun <T> FluidSegmentedSwitcher(
    items: List<T>,
    selectedItem: T,
    onItemSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    itemContent: @Composable (item: T, isSelected: Boolean) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val selectedIndex = items.indexOf(selectedItem).coerceAtLeast(0)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .border(
                BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
                RoundedCornerShape(22.dp)
            )
            .padding(3.dp)
    ) {
        val segmentWidth = maxWidth / items.size
        val animatedIndicatorOffset by animateDpAsState(
            targetValue = segmentWidth * selectedIndex,
            animationSpec = spring(
                dampingRatio = 0.75f,
                stiffness = 400f
            ),
            label = "fluidSwitcherIndicatorOffset"
        )

        // Gliding indicator capsule
        Box(
            modifier = Modifier
                .offset(x = animatedIndicatorOffset)
                .width(segmentWidth)
                .fillMaxHeight()
                .clip(RoundedCornerShape(19.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .border(
                    BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                    RoundedCornerShape(19.dp)
                )
        )

        // Clickable interactive segments
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = index == selectedIndex
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (!isSelected) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onItemSelected(item)
                            }
                        }
                        .semantics {
                            role = Role.Tab
                            selected = isSelected
                        },
                    contentAlignment = Alignment.Center
                ) {
                    itemContent(item, isSelected)
                }
            }
        }
    }
}

@Composable
private fun CombinedTelemetryCard(selectedTimeHorizon: String) {
    val targetRatio = when (selectedTimeHorizon) {
        "Day" -> 0.81f
        "Week" -> 0.92f
        "Month" -> 0.96f
        else -> 0.94f
    }
    val animatedRatio by animateFloatAsState(
        targetValue = targetRatio,
        animationSpec = spring(dampingRatio = 0.80f, stiffness = 450f),
        label = "combinedRatioAnimation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .iosGlass(shape = MaterialTheme.shapes.large),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        border = null
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
                    text = when (selectedTimeHorizon) {
                        "Day" -> "16,250 / 20k"
                        "Week" -> "92,400 / 100k"
                        "Month" -> "384,000 / 400k"
                        else -> "4,700,000 / 5M"
                    },
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Bilateral Two-Tone Split Indicator (DSN-01) with smooth ratio spring
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(modifier = Modifier.fillMaxWidth(animatedRatio).fillMaxHeight()) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(0.52f)
                            .background(CoupleFitIdentityTokens.UserPrimary)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(0.48f)
                            .background(CoupleFitIdentityTokens.PartnerPrimary)
                    )
                }
            }

            Text(
                text = "You: 48,200 steps (52%) • Partner: 44,200 steps (48%)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⚡ 532 active mins",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "🔥 4,630 kcal burned",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Synchrony: 94% ✨",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = SystemGreen
                )
            }
        }
    }
}

@Composable
private fun SoloTelemetryCard(
    ownerName: String,
    timeHorizon: String,
    steps: String,
    goal: String,
    targetRatio: Float,
    dailyAvg: String,
    activeMins: String,
    calories: String,
    accentColor: Color
) {
    val animatedRatio by animateFloatAsState(
        targetValue = targetRatio,
        animationSpec = spring(dampingRatio = 0.80f, stiffness = 450f),
        label = "soloRatioAnimation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .iosGlass(
                shape = MaterialTheme.shapes.large,
                borderBrush = Brush.verticalGradient(listOf(accentColor.copy(alpha = 0.5f), accentColor.copy(alpha = 0.15f)))
            ),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        border = null
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
                    text = "$ownerName's $timeHorizon Steps",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                    text = "$steps / $goal",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = accentColor
                )
            }

            LinearProgressIndicator(
                progress = { animatedRatio },
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
                    text = "${(targetRatio * 100).toInt()}% of Goal",
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
