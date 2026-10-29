package com.couplefit.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

/**
 * Activity Ring component with Emil Kowalski Design Engineering craft (ENG-02, ENG-04, ANM-01, VIS-05, TST-03):
 * - Fluid physics spring retargeting (dampingRatio = 0.85f, stiffness = 160f)
 * - Zero snap eliminated: continuous uninterrupted momentum on updates and refresh
 * - Luminescent track glow (12% tint of gradientEnd) replacing flat dark gray
 * - Dynamic optical depth: ambient cap drop shadow and subtle track aura on OLED black
 * - 0-step breathing ring pulse dot at 12 o'clock cap denoting live sensor connection
 */
@Composable
fun ActivityRing(
    progress: Float, // 0.0 to 1.0 (or more if overflowing)
    gradientStart: Color,
    gradientEnd: Color,
    modifier: Modifier = Modifier,
    thickness: Dp = 16.dp,
    trackColor: Color = Color.Unspecified,
    animationDuration: Int = 1200,
    animTrigger: Any? = null,
    reduceMotion: Boolean = false
) {
    val resolvedTrackColor = if (trackColor != Color.Unspecified) {
        trackColor
    } else {
        gradientEnd.copy(alpha = 0.12f)
    }

    // Smooth physics spring animation for ring progress
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(progress, animTrigger, reduceMotion) {
        if (reduceMotion) {
            animatedProgress.snapTo(progress)
        } else {
            // Fluid physics spring retargeting from current position without jarring zero snap (ENG-02, ANM-01)
            animatedProgress.animateTo(
                targetValue = progress,
                animationSpec = spring(
                    dampingRatio = 0.85f, // Apple Fitness liquid momentum settling (ANM-04)
                    stiffness = 160f // Fluid Apple Fitness-grade sweep (response ≈ 0.4s)
                )
            )
        }
    }

    // 0-step breathing pulse animation (VIS-05 / TST-03 / FEL-01)
    val breathingAlpha = if (reduceMotion) {
        0.65f
    } else {
        val infiniteTransition = rememberInfiniteTransition(label = "RingBreathingPulse")
        infiniteTransition.animateFloat(
            initialValue = 0.35f,
            targetValue = 0.85f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "BreathingPulseAlpha"
        ).value
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(thickness / 2) // prevent clipping the stroke
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val ringRadius = size.minDimension / 2
            val ringThickness = thickness.toPx()

            // 1. Ambient outer aura for the track (ENG-04)
            drawCircle(
                color = gradientEnd.copy(alpha = 0.04f),
                radius = ringRadius,
                center = center,
                style = Stroke(width = ringThickness + 4.dp.toPx())
            )

            // 2. Luminescent background track
            drawCircle(
                color = resolvedTrackColor,
                radius = ringRadius,
                center = center,
                style = Stroke(width = ringThickness)
            )

            // 3. 0-step breathing pulse dot at 12 o'clock position (VIS-05, TST-03)
            if (animatedProgress.value <= 0.005f) {
                val dotRadius = ringThickness / 2
                val dotCenter = Offset(center.x, center.y - ringRadius)
                // Outer breathing aura
                drawCircle(
                    color = gradientStart.copy(alpha = breathingAlpha * 0.35f),
                    radius = dotRadius * 1.5f,
                    center = dotCenter
                )
                // Core luminescent indicator dot
                drawCircle(
                    color = gradientStart.copy(alpha = breathingAlpha),
                    radius = dotRadius * 0.85f,
                    center = dotCenter
                )
            }

            // 4. Progress arc with optical depth cap shadow (ENG-04)
            val sweepAngle = 360f * animatedProgress.value
            if (sweepAngle > 0) {
                // Soft ambient drop shadow under the leading cap when progress is noticeable
                if (sweepAngle > 15f) {
                    val angleRad = Math.toRadians((sweepAngle - 90.0)).toFloat()
                    val capX = center.x + ringRadius * cos(angleRad)
                    val capY = center.y + ringRadius * sin(angleRad)
                    drawCircle(
                        color = Color.Black.copy(alpha = 0.35f),
                        radius = ringThickness / 2 + 1.dp.toPx(),
                        center = Offset(capX, capY + 1.dp.toPx())
                    )
                }

                drawArc(
                    brush = Brush.linearGradient(
                        colors = listOf(gradientStart, gradientEnd),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, size.height)
                    ),
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(
                        width = ringThickness,
                        cap = StrokeCap.Round
                    ),
                    size = Size(ringRadius * 2, ringRadius * 2),
                    topLeft = Offset(center.x - ringRadius, center.y - ringRadius)
                )
            }
        }
    }
}
