package com.couplefit.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.couplefit.ui.theme.AppSpacing
import com.couplefit.ui.theme.CoupleFitHaptics
import com.couplefit.ui.theme.SystemGreen
import com.couplefit.ui.theme.iosClickable
import com.couplefit.ui.theme.iosGlass
import com.couplefit.ui.theme.readableMeasure
import kotlinx.coroutines.delay

/**
 * Contextual "Medication Due" banner/pill for the Today Dashboard.
 *
 * Implements the design contract specified in `card-sort-analysis.md` §6.3 (Tree Test TS-04)
 * and §7.2 (Wireframe 1) to eliminate 23% friction observed during medication logging (CSA-03).
 *
 * Enhanced with ANM-13: Tactile completion physics, AnimatedContent transitions,
 * anatomical heartbeat haptic feedback (CoupleFitHaptics.performHeartbeat), and spring ejection.
 */
@Composable
fun MedicationDuePill(
    modifier: Modifier = Modifier,
    medicationName: String = "Vitamin D3",
    scheduledTime: String = "08:00 AM",
    isVisible: Boolean = true,
    onLogDose: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var isLogged by remember { mutableStateOf(false) }
    var pillVisible by remember(isVisible) { mutableStateOf(isVisible) }

    val containerAlpha by animateFloatAsState(
        targetValue = if (isLogged) 0.18f else 0.0f,
        animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow),
        label = "pillContainerAlpha"
    )

    AnimatedVisibility(
        visible = pillVisible,
        enter = fadeIn(tween(300)) + expandVertically(spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow)),
        exit = fadeOut(tween(250)) + shrinkVertically(spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessLow)),
        modifier = modifier
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .readableMeasure(maxWidth = 620.dp)
                .padding(horizontal = AppSpacing.md)
                .padding(bottom = AppSpacing.sm)
                .iosGlass(shape = RoundedCornerShape(16.dp))
                .semantics {
                    contentDescription = if (isLogged) {
                        "$medicationName dose logged successfully"
                    } else {
                        "Medication reminder: $scheduledTime $medicationName due. Tap to log dose."
                    }
                },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isLogged) SystemGreen.copy(alpha = containerAlpha) else Color.Transparent
            ),
            border = if (isLogged) BorderStroke(1.dp, SystemGreen.copy(alpha = 0.5f)) else null
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.sm, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Pill emoji + Title & Time with fluid AnimatedContent transitions
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    AnimatedContent(
                        targetState = isLogged,
                        transitionSpec = {
                            (fadeIn(tween(220)) + scaleIn(initialScale = 0.8f, animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium)))
                                .togetherWith(fadeOut(tween(140)) + scaleOut(targetScale = 0.8f))
                        },
                        label = "medicationEmoji"
                    ) { logged ->
                        Text(
                            text = if (logged) "🎉" else "💊",
                            fontSize = 22.sp,
                            modifier = Modifier.padding(end = AppSpacing.sm)
                        )
                    }

                    AnimatedContent(
                        targetState = isLogged,
                        transitionSpec = {
                            (fadeIn(tween(220)) + slideInVertically(initialOffsetY = { -it / 3 }))
                                .togetherWith(fadeOut(tween(140)) + slideOutVertically(targetOffsetY = { it / 3 }))
                        },
                        label = "medicationTitle"
                    ) { logged ->
                        Column {
                            Text(
                                text = if (logged) "Dose Logged!" else "$scheduledTime • $medicationName Due",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (logged) SystemGreen else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (logged) "Recorded for today" else "Tap to log your daily dose",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Right: Action Buttons with spring AnimatedContent swap & tactile haptics
                AnimatedContent(
                    targetState = isLogged,
                    transitionSpec = {
                        (fadeIn(tween(220)) + scaleIn(initialScale = 0.85f, animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium)))
                            .togetherWith(fadeOut(tween(140)) + scaleOut(targetScale = 0.9f))
                    },
                    label = "medicationAction"
                ) { logged ->
                    if (!logged) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Button(
                                onClick = {
                                    CoupleFitHaptics.performHeartbeat(context, haptic)
                                    isLogged = true
                                    onLogDose()
                                },
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier
                                    .height(38.dp)
                                    .iosClickable(pressedScale = 0.95f) {
                                        CoupleFitHaptics.performHeartbeat(context, haptic)
                                        isLogged = true
                                        onLogDose()
                                    }
                                    .semantics {
                                        contentDescription = "Log $medicationName dose as taken"
                                    }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Log [✓]",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }

                            IconButton(
                                onClick = {
                                    CoupleFitHaptics.performSubtleTick(context, haptic)
                                    pillVisible = false
                                    onDismiss()
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .semantics {
                                        contentDescription = "Dismiss medication reminder"
                                    }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    } else {
                        Badge(
                            containerColor = SystemGreen,
                            contentColor = Color.White,
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .semantics { contentDescription = "$medicationName dose completed" }
                        ) {
                            Text(
                                text = "Done ✓",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Auto-dismiss 2.5 seconds after logging with smooth spring ejection
    LaunchedEffect(isLogged) {
        if (isLogged) {
            delay(2500)
            pillVisible = false
        }
    }
}
