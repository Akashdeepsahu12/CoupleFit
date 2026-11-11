package com.couplefit.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.couplefit.ui.theme.GlassPrimitives
import com.couplefit.ui.theme.StepsAccent
import com.couplefit.ui.theme.SystemOrangeLight
import com.couplefit.ui.theme.SystemRed
import kotlin.math.cos
import kotlin.math.sin

/**
 * Astryx Custom Animated Refresh Indicator (AST-01).
 *
 * Designed according to Astryx geometric tokens:
 * - Replaces generic Material circular spinner with interlocking Astryx squircle glyphs.
 * - Double-loop continuous curve representing the CoupleFit mutual synchrony pulse.
 * - Frosted pill glass container (GlassPrimitives) with elevation depth and hairline stroke.
 * - Reactive pull rotation (0 -> 180 deg) and infinite spring pulse during active Firebase sync.
 * - Full WCAG 2.2 AA accessibility semantics and TalkBack support.
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AstryxRefreshIndicator(
    refreshing: Boolean,
    state: PullRefreshState,
    modifier: Modifier = Modifier,
    accentColor: Color = StepsAccent,
    secondaryColor: Color = SystemOrangeLight,
    surfaceBackground: Color = GlassPrimitives.SolidSlateCard.copy(alpha = 0.88f)
) {
    // Height threshold for pull completion
    val pullFraction = state.progress.coerceIn(0f, 1.5f)
    val density = LocalDensity.current

    // Animated container translation and scale
    val animatedY = remember { Animatable(0f) }
    val animatedAlpha = remember { Animatable(0f) }

    LaunchedEffect(refreshing, pullFraction) {
        if (refreshing) {
            animatedY.animateTo(
                targetValue = 72f,
                animationSpec = spring(dampingRatio = 0.75f, stiffness = 300f)
            )
            animatedAlpha.animateTo(1f, animationSpec = tween(150))
        } else if (pullFraction > 0.05f) {
            animatedY.snapTo(pullFraction * 56f)
            animatedAlpha.snapTo((pullFraction * 1.4f).coerceIn(0f, 1f))
        } else {
            animatedY.animateTo(0f, animationSpec = tween(220, easing = FastOutSlowInEasing))
            animatedAlpha.animateTo(0f, animationSpec = tween(180))
        }
    }

    // Continuous spin and breathing scale during active sync
    val infiniteTransition = rememberInfiniteTransition(label = "astryxSyncTransition")
    val spinRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "astryxSpinRotation"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "astryxPulseScale"
    )

    val effectiveRotation = if (refreshing) {
        spinRotation
    } else {
        pullFraction * 180f
    }

    val effectiveScale = if (refreshing) {
        pulseScale
    } else {
        (0.7f + pullFraction * 0.3f).coerceIn(0.7f, 1.0f)
    }

    if (animatedAlpha.value > 0.01f || refreshing) {
        Box(
            modifier = modifier
                .graphicsLayer {
                    translationY = with(density) { animatedY.value.dp.toPx() }
                    alpha = animatedAlpha.value
                    scaleX = effectiveScale
                    scaleY = effectiveScale
                }
                .semantics {
                    contentDescription = if (refreshing) {
                        "Syncing couple health data with partner"
                    } else {
                        "Pull down to refresh couple telemetry"
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            // Frosted Squircle Indicator Vessel
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(14.dp),
                        spotColor = accentColor.copy(alpha = 0.35f),
                        ambientColor = Color.Black.copy(alpha = 0.45f)
                    )
                    .clip(RoundedCornerShape(14.dp))
                    .background(surfaceBackground)
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            listOf(
                                Color.White.copy(alpha = 0.30f),
                                Color.White.copy(alpha = 0.08f)
                            )
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                AstryxRefreshGlyph(
                    rotationAngle = effectiveRotation,
                    pullProgress = if (refreshing) 1.0f else pullFraction.coerceAtMost(1.0f),
                    accentColor = accentColor,
                    secondaryColor = secondaryColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/**
 * Vector Geometry for the Astryx Synchrony Refresh Icon.
 * Composes a dual squircle-arc topology with aerodynamic directional terminals.
 */
@Composable
fun AstryxRefreshGlyph(
    rotationAngle: Float,
    pullProgress: Float,
    accentColor: Color,
    secondaryColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val strokeWidth = 2.4.dp.toPx()

        val gradientBrush = Brush.sweepGradient(
            colors = listOf(
                accentColor,
                secondaryColor,
                accentColor
            ),
            center = Offset(width / 2f, height / 2f)
        )

        rotate(rotationAngle) {
            // 1. Primary Top-Right Rounded Arc (Mutual Sync Loop 1)
            val sweep1 = (140f * pullProgress).coerceIn(20f, 140f)
            drawArc(
                brush = gradientBrush,
                startAngle = -45f,
                sweepAngle = sweep1,
                useCenter = false,
                style = Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // 2. Secondary Bottom-Left Rounded Arc (Mutual Sync Loop 2)
            val sweep2 = (140f * pullProgress).coerceIn(20f, 140f)
            drawArc(
                brush = gradientBrush,
                startAngle = 135f,
                sweepAngle = sweep2,
                useCenter = false,
                style = Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // 3. Astryx Directional Arrowheads (Terminals)
            val radius = (width - strokeWidth) / 2f
            val endAngle1Rad = Math.toRadians((-45f + sweep1).toDouble())
            val arrowTip1X = (width / 2f + radius * cos(endAngle1Rad)).toFloat()
            val arrowTip1Y = (height / 2f + radius * sin(endAngle1Rad)).toFloat()

            val arrowHeadPath1 = Path().apply {
                moveTo(arrowTip1X - 3.5.dp.toPx(), arrowTip1Y - 4.5.dp.toPx())
                lineTo(arrowTip1X, arrowTip1Y)
                lineTo(arrowTip1X - 4.5.dp.toPx(), arrowTip1Y + 3.5.dp.toPx())
            }
            drawPath(
                path = arrowHeadPath1,
                brush = gradientBrush,
                style = Stroke(
                    width = strokeWidth * 0.9f,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            val endAngle2Rad = Math.toRadians((135f + sweep2).toDouble())
            val arrowTip2X = (width / 2f + radius * cos(endAngle2Rad)).toFloat()
            val arrowTip2Y = (height / 2f + radius * sin(endAngle2Rad)).toFloat()

            val arrowHeadPath2 = Path().apply {
                moveTo(arrowTip2X + 3.5.dp.toPx(), arrowTip2Y + 4.5.dp.toPx())
                lineTo(arrowTip2X, arrowTip2Y)
                lineTo(arrowTip2X + 4.5.dp.toPx(), arrowTip2Y - 3.5.dp.toPx())
            }
            drawPath(
                path = arrowHeadPath2,
                brush = gradientBrush,
                style = Stroke(
                    width = strokeWidth * 0.9f,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }
    }
}
