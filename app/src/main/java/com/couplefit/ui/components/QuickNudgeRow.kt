package com.couplefit.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.couplefit.ui.theme.AppSpacing
import com.couplefit.ui.theme.CoupleFitHaptics

/**
 * Quick-action Nudge Pill Row positioned in the Natural Thumb Zone directly below
 * the bilateral activity rings / horseshoe gauges (CSA-08 & Wireframe 1).
 */
@Composable
fun QuickNudgeRow(
    partnerName: String = "Partner",
    modifier: Modifier = Modifier,
    onSendNudge: (String) -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val quickNudges = listOf(
        NudgeAction("👋", "Poke", "Send a quick poke to $partnerName"),
        NudgeAction("🔥", "Keep Going!", "Cheer $partnerName to keep moving"),
        NudgeAction("❤️", "Love", "Send heart and encouragement to $partnerName"),
        NudgeAction("💧", "Drink Water", "Remind $partnerName to hydrate")
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.md)
            .padding(top = AppSpacing.sm, bottom = AppSpacing.md)
    ) {
        Text(
            text = "QUICK CHEERS & NUDGES",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(bottom = AppSpacing.xs)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
        ) {
            quickNudges.forEach { action ->
                Surface(
                    onClick = {
                        // Dual-pulse heartbeat sensory haptic (ENG-03, MIC-01)
                        CoupleFitHaptics.performHeartbeat(context, haptic)
                        onSendNudge("${action.emoji} ${action.label}")
                    },
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    tonalElevation = 1.dp,
                    modifier = Modifier
                        .height(44.dp)
                        .semantics { contentDescription = action.description }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(action.emoji, fontSize = 16.sp)
                        Text(
                            text = action.label,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

private data class NudgeAction(
    val emoji: String,
    val label: String,
    val description: String
)
