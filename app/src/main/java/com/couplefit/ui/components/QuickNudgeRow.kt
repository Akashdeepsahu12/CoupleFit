package com.couplefit.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.couplefit.ui.theme.AppSpacing
import com.couplefit.ui.theme.CoupleFitHaptics
import com.couplefit.ui.theme.CoupleFitIdentityTokens
import com.couplefit.ui.theme.LiquiTokens
import com.couplefit.ui.theme.iosMomentumClickable
import com.couplefit.ui.theme.liquiTactilePill
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sin

/**
 * Quick-action Nudge Pill Row positioned in the Natural Thumb Zone directly below
 * the bilateral activity rings / horseshoe gauges (CSA-08, TST-06, ANM-07).
 *
 * Upgraded with Liquid Frosted Glass styling and Q12 / ANM-07 Parabolic Flying
 * Nudge Particle Trajectory arcing towards the partner's telemetry gauge.
 */
@Composable
fun QuickNudgeRow(
    partnerName: String = "your partner",
    mySteps: Long = 0L,
    partnerSteps: Long = 0L,
    reduceMotion: Boolean = false,
    modifier: Modifier = Modifier,
    onSendNudge: (String) -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current

    // Base couple nudges (preserves exact test contracts for Poke, Keep Going!, Love)
    val baseNudges = remember(partnerName) {
        mutableListOf(
            NudgeAction("👋", "Poke", "Send a quick poke to $partnerName"),
            NudgeAction("🔥", "Keep Going!", "Cheer $partnerName to keep moving"),
            NudgeAction("❤️", "Love", "Send love and encouragement to $partnerName"),
            NudgeAction("💧", "Drink Water", "Remind $partnerName to hydrate together")
        )
    }

    // Contextual couple cheers based on live step relationship (TST-06)
    val displayNudges = remember(partnerSteps, mySteps, baseNudges) {
        val list = baseNudges.toMutableList()
        if (partnerSteps > mySteps && (partnerSteps - mySteps) > 100) {
            list.add(0, NudgeAction("🏃", "Catching Up!", "Tell $partnerName you're right on their heels"))
        } else if (mySteps > partnerSteps && (mySteps - partnerSteps) > 500) {
            list.add(0, NudgeAction("⚡", "You Got This!", "Encourage $partnerName to close the distance"))
        } else if (mySteps > 0 && Math.abs(mySteps - partnerSteps) <= 100) {
            list.add(0, NudgeAction("✨", "In Sync!", "Celebrate being tied stride for stride with $partnerName"))
        }
        list
    }

    // Q12 / ANM-07: Flying Nudge Particle Trajectory State
    var flyingEmoji by remember { mutableStateOf<String?>(null) }
    val flightProgress = remember { Animatable(0f) }

    Box(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.md)
                .padding(top = AppSpacing.sm, bottom = AppSpacing.md)
        ) {
            Text(
                text = "Quick cheers",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.sp,
                modifier = Modifier.padding(bottom = AppSpacing.xs)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
            ) {
                displayNudges.forEach { action ->
                    Box(
                        modifier = Modifier
                            .height(44.dp)
                            .defaultMinSize(minWidth = 44.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                            .border(
                                width = 0.5.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .iosMomentumClickable(
                                onClickLabel = action.description,
                                onClick = {
                                    // Dual-pulse heartbeat sensory haptic (ENG-03, MIC-01)
                                    CoupleFitHaptics.performHeartbeat(context, haptic)
                                    onSendNudge("${action.emoji} ${action.label}")

                                    // Launch Parabolic trajectory flight towards partner ring
                                    if (!reduceMotion) {
                                        flyingEmoji = action.emoji
                                        coroutineScope.launch {
                                            flightProgress.snapTo(0f)
                                            flightProgress.animateTo(
                                                targetValue = 1f,
                                                animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
                                            )
                                            flyingEmoji = null
                                        }
                                    }
                                }
                            )
                            .semantics { contentDescription = action.description },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
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

        // Q12 / ANM-07: Canvas Parabolic Flying Particle & Ripple
        flyingEmoji?.let { emoji ->
            val p = flightProgress.value
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .align(Alignment.BottomCenter)
            ) {
                val startX = size.width * 0.35f
                val targetX = size.width * 0.78f // Partner ring horizontal position
                val startY = size.height * 0.75f
                val targetY = size.height * 0.05f // Elevated toward partner gauge
                val arcApexHeight = with(density) { 60.dp.toPx() }

                // Parabolic trajectory: X linearly interpolates, Y follows quadratic arc
                val currentX = startX + (targetX - startX) * p
                val linearY = startY + (targetY - startY) * p
                val arcOffset = arcApexHeight * sin(p * PI).toFloat()
                val currentY = linearY - arcOffset

                val particleAlpha = ((1f - p.pow(2.5f)) * 255).toInt().coerceIn(0, 255)
                val particleScale = 1.0f + 0.4f * sin(p * PI).toFloat()
                val fontSizePx = with(density) { 26.sp.toPx() } * particleScale

                // Draw luminous trail line
                drawCircle(
                    color = CoupleFitIdentityTokens.PartnerPrimary.copy(alpha = (1f - p) * 0.35f),
                    radius = with(density) { (8 * (1f - p)).dp.toPx() },
                    center = Offset(currentX, currentY)
                )

                // Render flying emoji via native canvas
                drawIntoCanvas { canvas ->
                    val paint = android.graphics.Paint().apply {
                        textSize = fontSizePx
                        isAntiAlias = true
                        alpha = particleAlpha
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                    canvas.nativeCanvas.drawText(
                        emoji,
                        currentX,
                        currentY + fontSizePx / 3f,
                        paint
                    )
                }

                // Partner ring arrival sympathetic ripple
                if (p > 0.75f) {
                    val arrivalProgress = (p - 0.75f) / 0.25f
                    val rippleRadius = with(density) { (15 + arrivalProgress * 25).dp.toPx() }
                    val rippleAlpha = (1f - arrivalProgress) * 0.5f
                    drawCircle(
                        color = CoupleFitIdentityTokens.PartnerPrimary.copy(alpha = rippleAlpha),
                        radius = rippleRadius,
                        center = Offset(targetX, targetY),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = with(density) { 2.dp.toPx() }
                        )
                    )
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
