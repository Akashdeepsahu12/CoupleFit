package com.couplefit.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

/**
 * Native Jetpack Compose equivalent of the FloatingPathsBackground component.
 * Animates 36 delicate cubic Bézier paths across the screen.
 * - Lifecycle-aware: pauses rendering loops when the app is backgrounded.
 * - Accessibility-aware: respects reduceMotion system/user preference.
 */
@Composable
fun FloatingPathsBackground(
    modifier: Modifier = Modifier,
    position: Float = -1f,
    reduceMotion: Boolean = false,
    strokeColor: Color = if (isSystemInDarkTheme()) Color.White else Color(0xFF0F172A),
    content: @Composable () -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var isAppForeground by remember { mutableStateOf(true) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            isAppForeground = event == Lifecycle.Event.ON_RESUME
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val phase: Float
    val opacityPulse: Float

    if (!reduceMotion && isAppForeground) {
        val infiniteTransition = rememberInfiniteTransition(label = "FloatingPathsTransition")
        
        val animatedPhase by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 24000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "FloatingPathsPhase"
        )

        val animatedPulse by infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 0.6f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 8000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "FloatingPathsPulse"
        )
        phase = animatedPhase
        opacityPulse = animatedPulse
    } else {
        phase = 0f
        opacityPulse = 0.45f
    }

    // Precompute the 36 cubic paths based on SVG viewBox (696 x 316)
    val pathDataList = remember(position) {
        List(36) { i ->
            val p = position
            val startX = -(380f - i * 5f * p)
            val startY = -(189f + i * 6f)
            val c1x = startX
            val c1y = startY
            val c2x = -(312f - i * 5f * p)
            val c2y = 216f - i * 6f
            val end1x = 152f - i * 5f * p
            val end1y = 343f - i * 6f

            val c3x = 616f - i * 5f * p
            val c3y = 470f - i * 6f
            val c4x = 684f - i * 5f * p
            val c4y = 875f - i * 6f
            val end2x = c4x
            val end2y = c4y

            val path = Path().apply {
                moveTo(startX, startY)
                cubicTo(c1x, c1y, c2x, c2y, end1x, end1y)
                cubicTo(c3x, c3y, c4x, c4y, end2x, end2y)
            }

            val strokeWidth = 0.5f + i * 0.03f
            val baseOpacity = 0.08f + (i * 0.02f)

            Triple(path, strokeWidth, baseOpacity)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (isAppForeground) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val scaleX = size.width / 696f
                val scaleY = size.height / 316f
                val uniformScale = maxOf(scaleX, scaleY)

                scale(scale = uniformScale) {
                    pathDataList.forEachIndexed { index, (path, strokeWidth, baseOpacity) ->
                        val pathPhase = (phase + (index * 0.028f)) % 1f
                        val effectiveAlpha = (baseOpacity * opacityPulse * 1.5f).coerceIn(0.04f, 0.45f)
                        
                        val dashLength = 220f + index * 8f
                        val gapLength = 300f + index * 6f
                        val phaseOffset = pathPhase * (dashLength + gapLength)

                        drawPath(
                            path = path,
                            color = strokeColor.copy(alpha = effectiveAlpha),
                            style = Stroke(
                                width = strokeWidth,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round,
                                pathEffect = PathEffect.dashPathEffect(
                                    intervals = floatArrayOf(dashLength, gapLength),
                                    phase = phaseOffset
                                )
                            )
                        )
                    }
                }
            }
        }
        content()
    }
}
