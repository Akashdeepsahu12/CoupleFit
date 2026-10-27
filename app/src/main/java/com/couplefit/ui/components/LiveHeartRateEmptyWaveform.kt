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
import androidx.compose.ui.unit.sp
import com.couplefit.ui.theme.AppSpacing

/**
 * Living Ambient Cardiogram Waveform (ENG-06 / UI-UX Pro Max).
 *
 * Replaces cavernous, barren empty state rectangles with an elegant, breathing
 * ECG cardiogram baseline canvas and a tactile "Sync Now" quick-action trigger.
 */
@Composable
fun LiveHeartRateEmptyWaveform(
    tint: Color,
    reduceMotion: Boolean,
    onSyncClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
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
    val animatedAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ecgAlpha"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(AppSpacing.sm))
            .background(if (isDark) Color(0x10FFFFFF) else Color(0x08000000)),
        contentAlignment = Alignment.Center
    ) {
        // Ambient ECG Waveform Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val midY = h * 0.55f

            // Faint reference baseline
            drawLine(
                color = tint.copy(alpha = 0.12f),
                start = Offset(0f, midY),
                end = Offset(w, midY),
                strokeWidth = 1.dp.toPx()
            )

            // Stylized rhythmic ECG waveform path: baseline -> P wave -> QRS complex -> T wave -> baseline
            val path = Path().apply {
                moveTo(0f, midY)
                val segmentW = w / 3f
                for (i in 0..2) {
                    val startX = i * segmentW
                    // Flat lead-in
                    lineTo(startX + segmentW * 0.25f, midY)
                    // Small P wave
                    cubicTo(
                        startX + segmentW * 0.28f, midY - h * 0.08f,
                        startX + segmentW * 0.32f, midY - h * 0.08f,
                        startX + segmentW * 0.35f, midY
                    )
                    // Flat segment
                    lineTo(startX + segmentW * 0.40f, midY)
                    // Q dip
                    lineTo(startX + segmentW * 0.43f, midY + h * 0.10f)
                    // R peak
                    lineTo(startX + segmentW * 0.48f, midY - h * 0.38f)
                    // S drop
                    lineTo(startX + segmentW * 0.53f, midY + h * 0.20f)
                    // S to baseline
                    lineTo(startX + segmentW * 0.56f, midY)
                    // ST segment
                    lineTo(startX + segmentW * 0.62f, midY)
                    // T wave
                    cubicTo(
                        startX + segmentW * 0.66f, midY - h * 0.12f,
                        startX + segmentW * 0.72f, midY - h * 0.12f,
                        startX + segmentW * 0.76f, midY
                    )
                    // Flat lead-out
                    lineTo(startX + segmentW, midY)
                }
            }

            // Draw soft glowing stroke
            drawPath(
                path = path,
                color = tint.copy(alpha = if (reduceMotion) 0.28f else animatedAlpha * 0.38f),
                style = Stroke(
                    width = 2.dp.toPx(),
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
                        radius = 14.dp.toPx()
                    ),
                    radius = 14.dp.toPx(),
                    center = Offset(beadX, midY)
                )
                drawCircle(
                    color = tint,
                    radius = 3.dp.toPx(),
                    center = Offset(beadX, midY)
                )
            }
        }

        // Action & Explanatory Overlay
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(horizontal = AppSpacing.md)
        ) {
            Text(
                text = "Awaiting Heart Rate Records",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Sync Health Connect to display live cardiovascular telemetry",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))
            Surface(
                onClick = onSyncClick,
                shape = RoundedCornerShape(12.dp),
                color = tint.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, tint.copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Sync Now",
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
