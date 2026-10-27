package com.couplefit.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.couplefit.ui.theme.AppSpacing

/**
 * Harmonized Living Ambient Cardiogram Waveform (Phase 22 / DSN-05).
 *
 * Integrates directly with the CoupleFit Liquid Frosted Glass design system:
 * - Fluid P-Q-R-S-T rhythmic cardiogram baseline path in VitalsAccent
 * - Ambient travelling pulse highlight bead across the baseline
 * - Reassuring typography matching the dashboard type scale
 * - Frosted glass "Sync Health Connect ↻" pill button matching QuickNudgeRow
 */
@Composable
fun LiveHeartRateEmptyWaveform(
    tint: Color,
    reduceMotion: Boolean,
    onSyncClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()

    // Continuous smooth scanning phase for traveling pulse bead
    val infiniteTransition = rememberInfiniteTransition(label = "ecgWaveform")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ecgPhase"
    )

    // Gentle, calm breathing pulse for ECG baseline
    val animatedAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.70f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ecgAlpha"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isDark) 0.20f else 0.35f)),
        contentAlignment = Alignment.Center
    ) {
        // Ambient ECG Cardiogram Waveform
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val midY = h * 0.52f

            // Faint reference baseline
            drawLine(
                color = tint.copy(alpha = 0.12f),
                start = Offset(0f, midY),
                end = Offset(w, midY),
                strokeWidth = 1.dp.toPx()
            )

            // Precision cardiogram rhythm path (P-Q-R-S-T sequence)
            val path = Path().apply {
                moveTo(0f, midY)
                val segmentW = w / 3f
                for (i in 0..2) {
                    val startX = i * segmentW
                    lineTo(startX + segmentW * 0.22f, midY)
                    // P wave
                    cubicTo(
                        startX + segmentW * 0.25f, midY - h * 0.07f,
                        startX + segmentW * 0.29f, midY - h * 0.07f,
                        startX + segmentW * 0.32f, midY
                    )
                    lineTo(startX + segmentW * 0.38f, midY)
                    // Q dip
                    lineTo(startX + segmentW * 0.41f, midY + h * 0.08f)
                    // R peak
                    lineTo(startX + segmentW * 0.46f, midY - h * 0.34f)
                    // S drop
                    lineTo(startX + segmentW * 0.51f, midY + h * 0.18f)
                    lineTo(startX + segmentW * 0.54f, midY)
                    lineTo(startX + segmentW * 0.60f, midY)
                    // T wave
                    cubicTo(
                        startX + segmentW * 0.64f, midY - h * 0.10f,
                        startX + segmentW * 0.70f, midY - h * 0.10f,
                        startX + segmentW * 0.74f, midY
                    )
                    lineTo(startX + segmentW, midY)
                }
            }

            // Draw calm, glowing ECG line
            drawPath(
                path = path,
                color = tint.copy(alpha = if (reduceMotion) 0.30f else animatedAlpha),
                style = Stroke(
                    width = 1.75.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // Draw traveling pulse highlight bead along the rhythm line
            if (!reduceMotion) {
                val beadX = phase * w
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(tint.copy(alpha = 0.85f), Color.Transparent),
                        center = Offset(beadX, midY),
                        radius = 16.dp.toPx()
                    ),
                    radius = 16.dp.toPx(),
                    center = Offset(beadX, midY)
                )
                drawCircle(
                    color = tint,
                    radius = 3.5.dp.toPx(),
                    center = Offset(beadX, midY)
                )
            }
        }

        // Actionable Overlay (Glassmorphic Pill Button matching QuickNudgeRow)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(horizontal = AppSpacing.md)
        ) {
            Text(
                text = "Awaiting Heart Rate Records",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Wear your watch or sync Health Connect to display telemetry",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))
            Surface(
                onClick = onSyncClick,
                shape = RoundedCornerShape(12.dp),
                color = tint.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, tint.copy(alpha = 0.45f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Sync Health Connect",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = tint
                    )
                    Text(
                        text = "↻",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = tint
                    )
                }
            }
        }
    }
}
