package com.couplefit.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.couplefit.ui.theme.iosGlass

/**
 * Quick-action Nudge Pill Row positioned in the Natural Thumb Zone directly below
 * the bilateral activity rings / horseshoe gauges (CSA-08 & Wireframe 1).
 *
 * Upgraded with Liquid Frosted Glass styling (TST-04, TST-06) and contextual
 * dynamic couple prompts for genuine human connection.
 */
@Composable
fun QuickNudgeRow(
    partnerName: String = "Partner",
    mySteps: Long = 0L,
    partnerSteps: Long = 0L,
    modifier: Modifier = Modifier,
    onSendNudge: (String) -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    // Base couple nudges (preserves exact test contracts for Poke, Keep Going!, Love)
    val baseNudges = mutableListOf(
        NudgeAction("👋", "Poke", "Send a quick poke to $partnerName"),
        NudgeAction("🔥", "Keep Going!", "Cheer $partnerName to keep moving"),
        NudgeAction("❤️", "Love", "Send love and encouragement to $partnerName"),
        NudgeAction("💧", "Drink Water", "Remind $partnerName to hydrate together")
    )

    // Contextual couple cheers based on live step relationship (TST-06)
    if (partnerSteps > mySteps && (partnerSteps - mySteps) > 100) {
        baseNudges.add(0, NudgeAction("🏃", "Catching Up!", "Tell $partnerName you're right on their heels"))
    } else if (mySteps > partnerSteps && (mySteps - partnerSteps) > 500) {
        baseNudges.add(0, NudgeAction("⚡", "You Got This!", "Encourage $partnerName to close the distance"))
    } else if (mySteps > 0 && mySteps == partnerSteps) {
        baseNudges.add(0, NudgeAction("✨", "In Sync!", "Celebrate being tied stride for stride with $partnerName"))
    }

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
            baseNudges.forEach { action ->
                Surface(
                    onClick = {
                        // Dual-pulse heartbeat sensory haptic (ENG-03, MIC-01)
                        CoupleFitHaptics.performHeartbeat(context, haptic)
                        onSendNudge("${action.emoji} ${action.label}")
                    },
                    shape = RoundedCornerShape(20.dp),
                    color = Color.Transparent,
                    modifier = Modifier
                        .height(44.dp)
                        .iosGlass(shape = RoundedCornerShape(20.dp))
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
