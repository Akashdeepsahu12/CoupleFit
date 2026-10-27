package com.couplefit.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
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
import com.couplefit.ui.theme.LiquiTokens

/**
 * Liqui Design & Antislop-UI Ambient Heart Rate Waveform Stage.
 *
 * Adheres to:
 * 1. Liqui Design Two-Material Philosophy:
 *    - Glass for surface lens and waveform stage
 *    - Solid fill for actionable controls ("Sync Health Connect")
 * 2. Antislop-UI:
 *    - No decorative endless pulsing dots or fake studio recording lights
 *    - Real, calm cardiogram trace with hospital-grade baseline and purposeful contrast
 */
@Composable
fun LiveHeartRateEmptyWaveform(
    tint: Color,
    reduceMotion: Boolean,
    onSyncClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()

    // Gentle, calm breathing pulse for ECG baseline (calm medical monitor rhythm)
    val infiniteTransition = rememberInfiniteTransition(label = "liquiEcgPulse")
    val animatedAlpha by infiniteTransition.animateFloat(
        initialValue = 0.30f,
        targetValue = 0.55f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "liquiEcgAlpha"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(LiquiTokens.CardRadius - 6.dp))
            .background(if (isDark) LiquiTokens.DarkTintDeep.copy(alpha = 0.45f) else Color(0x0A000000)),
        contentAlignment = Alignment.Center
    ) {
        // Ambient ECG Cardiogram Waveform
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val midY = h * 0.52f

            // Faint reference baseline
            drawLine(
                color = tint.copy(alpha = 0.15f),
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
                color = tint.copy(alpha = if (reduceMotion) 0.35f else animatedAlpha),
                style = Stroke(
                    width = 1.75.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }

        // Actionable Overlay (Two-Material Philosophy: Solid tactile button for value/action)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(horizontal = AppSpacing.md)
        ) {
            Text(
                text = "Awaiting Heart Rate Stream",
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
            Button(
                onClick = onSyncClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = tint,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Sync Health Connect",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "↻",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
