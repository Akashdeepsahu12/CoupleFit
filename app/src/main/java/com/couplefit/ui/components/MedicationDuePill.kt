package com.couplefit.ui.components

import androidx.compose.animation.*
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.couplefit.ui.theme.AppSpacing
import com.couplefit.ui.theme.SystemGreen
import com.couplefit.ui.theme.iosGlass
import kotlinx.coroutines.delay

/**
 * Contextual "Medication Due" banner/pill for the Today Dashboard.
 *
 * Implements the design contract specified in `card-sort-analysis.md` §6.3 (Tree Test TS-04)
 * and §7.2 (Wireframe 1) to eliminate 23% friction observed during medication logging (CSA-03).
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
    val haptic = LocalHapticFeedback.current
    var isLogged by remember { mutableStateOf(false) }
    var pillVisible by remember(isVisible) { mutableStateOf(isVisible) }

    AnimatedVisibility(
        visible = pillVisible,
        enter = fadeIn(tween(300)) + expandVertically(tween(300)),
        exit = fadeOut(tween(300)) + shrinkVertically(tween(300)),
        modifier = modifier
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
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
                containerColor = if (isLogged) SystemGreen.copy(alpha = 0.15f) else Color.Transparent
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
                // Left: Pill emoji + Title & Time
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = if (isLogged) "🎉" else "💊",
                        fontSize = 22.sp,
                        modifier = Modifier.padding(end = AppSpacing.sm)
                    )
                    Column {
                        Text(
                            text = if (isLogged) "Dose Logged!" else "$scheduledTime • $medicationName Due",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isLogged) SystemGreen else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isLogged) "Recorded for today" else "Tap to log your daily dose",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Right: Action Buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (!isLogged) {
                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
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
                    } else {
                        Badge(
                            containerColor = SystemGreen,
                            contentColor = Color.White,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                text = "Done ✓",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Auto-dismiss 2.5 seconds after logging
    LaunchedEffect(isLogged) {
        if (isLogged) {
            delay(2500)
            pillVisible = false
        }
    }
}
