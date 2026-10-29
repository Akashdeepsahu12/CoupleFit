package com.couplefit.ui.hub

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.couplefit.ui.network.NetworkViewModel
import com.couplefit.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Couple Hub Screen (Tab 4).
 * Designed according to `card-sort-analysis.md` §7.2 (Wireframe 4).
 *
 * Enhanced with ANM-14: Interactive couple avatar touch-heartbeat haptic ripple,
 * dual-pulse anatomical heartbeat rhythm, and expanding radiant aura.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoupleHubScreen(
    modifier: Modifier = Modifier,
    onNavigateToPrivacySettings: () -> Unit = {},
    networkViewModel: NetworkViewModel = hiltViewModel()
) {
    val isConnected by networkViewModel.isConnected.collectAsState()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    // ANM-14: Heartbeat pulse scale & ripple animation states
    val heartScale = remember { Animatable(1f) }
    val rippleProgress = remember { Animatable(0f) }
    var heartbeatBannerVisible by remember { mutableStateOf(false) }

    fun triggerHeartbeat() {
        CoupleFitHaptics.performHeartbeat(context, haptic)
        heartbeatBannerVisible = true
        coroutineScope.launch {
            // Dual anatomical heartbeat pump: "Lub-Dub"
            // Lub (Systolic)
            heartScale.animateTo(1.24f, tween(90, easing = FastOutSlowInEasing))
            heartScale.animateTo(1.06f, tween(60, easing = LinearEasing))
            // Dub (Ventricular)
            heartScale.animateTo(1.32f, tween(110, easing = FastOutSlowInEasing))
            heartScale.animateTo(1.0f, spring(dampingRatio = 0.68f, stiffness = Spring.StiffnessMedium))
        }
        coroutineScope.launch {
            rippleProgress.snapTo(0f)
            rippleProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 850, easing = FastOutSlowInEasing)
            )
        }
        coroutineScope.launch {
            delay(2200)
            heartbeatBannerVisible = false
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Couple Hub",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
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
                    text = "PARTNER PROFILE & STATUS",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = AppSpacing.sm)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(AppSpacing.md),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)
                        ) {
                            // ANM-14: Interactive Partner Avatar with Heartbeat Ripple Aura
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(76.dp)
                                    .semantics(mergeDescendants = true) {
                                        contentDescription = "Partner profile avatar. Tap to send live heartbeat ripple."
                                        onClick(label = "Send heartbeat to Partner") {
                                            triggerHeartbeat()
                                            true
                                        }
                                    }
                            ) {
                                // Concentric glowing ripple aura waves
                                if (rippleProgress.value > 0f && rippleProgress.value < 1f) {
                                    val r = rippleProgress.value
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        val maxRadius = size.minDimension / 2f
                                        // Outer Wave (Partner Coral / Rose Glow)
                                        drawCircle(
                                            color = CoupleFitIdentityTokens.PartnerPrimary.copy(alpha = (1f - r) * 0.75f),
                                            radius = maxRadius * (0.65f + r * 0.35f),
                                            style = Stroke(width = (3.5f * (1f - r)).dp.toPx())
                                        )
                                        // Inner Wave (User Cyan Glow)
                                        if (r > 0.15f) {
                                            val r2 = (r - 0.15f) / 0.85f
                                            drawCircle(
                                                color = CoupleFitIdentityTokens.UserPrimary.copy(alpha = (1f - r2) * 0.55f),
                                                radius = maxRadius * (0.55f + r2 * 0.40f),
                                                style = Stroke(width = (2.5f * (1f - r2)).dp.toPx())
                                            )
                                        }
                                    }
                                }

                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .graphicsLayer {
                                            scaleX = heartScale.value
                                            scaleY = heartScale.value
                                        }
                                        .iosClickable(pressedScale = 0.94f) {
                                            triggerHeartbeat()
                                        }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "❤️",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontSize = 24.sp
                                        )
                                    }
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Alex & Partner",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Paired for 540 Days",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                val statusText = if (isConnected) "🟢 Partner Online • Live ⚡" else "🟡 Offline Mode • Cached Profile"
                                Text(
                                    text = statusText,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                )

                                // Heartbeat confirmation toast
                                AnimatedVisibility(
                                    visible = heartbeatBannerVisible,
                                    enter = fadeIn(tween(180)) + expandVertically(tween(180)),
                                    exit = fadeOut(tween(220)) + shrinkVertically(tween(220))
                                ) {
                                    Text(
                                        text = "❤️ Heartbeat sent to Partner!",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = CoupleFitIdentityTokens.PartnerPrimary,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "SHARED STREAKS & MEMORIES",
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
                        Text(
                            text = "🔥 42 Days Shared Goal Streak",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = "🎉 12 Days till 1-Year Fitness Anniversary",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Text(
                    text = "PARTNER VISIBILITY & SHARING RULES",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(AppSpacing.md),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Steps & Activity Rings", style = MaterialTheme.typography.bodyMedium)
                            Text("Visible", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        }
                        HorizontalDivider()
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Live Heart Rate & Vitals", style = MaterialTheme.typography.bodyMedium)
                            Text("Hidden", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.error)
                        }
                        HorizontalDivider()
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Medication & Sleep", style = MaterialTheme.typography.bodyMedium)
                            Text("Visible", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedButton(
                            onClick = onNavigateToPrivacySettings,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = "Manage Privacy", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Manage Privacy Rules")
                        }
                    }
                }
            }
        }
    }
}
