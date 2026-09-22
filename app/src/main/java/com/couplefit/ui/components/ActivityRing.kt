package com.couplefit.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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

@Composable
fun ActivityRing(
    progress: Float, // 0.0 to 1.0 (or more if overflowing)
    gradientStart: Color,
    gradientEnd: Color,
    modifier: Modifier = Modifier,
    thickness: Dp = 16.dp,
    trackColor: Color = Color.DarkGray.copy(alpha = 0.3f),
    animationDuration: Int = 1200,
    animTrigger: Any? = null
) {
    // Smooth ease-out animation for ring progress
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(progress, animTrigger) {
        animatedProgress.snapTo(0f)
        animatedProgress.animateTo(
            targetValue = progress,
            animationSpec = tween(
                durationMillis = animationDuration,
                easing = FastOutSlowInEasing
            )
        )
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(thickness / 2) // prevent clipping the stroke
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val ringRadius = size.minDimension / 2
            val ringThickness = thickness.toPx()
            
            // Draw background track
            drawCircle(
                color = trackColor,
                radius = ringRadius,
                center = center,
                style = Stroke(width = ringThickness)
            )

            // Draw progress arc
            val sweepAngle = 360f * animatedProgress.value
            if (sweepAngle > 0) {
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

