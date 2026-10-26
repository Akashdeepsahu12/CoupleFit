package com.couplefit.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.couplefit.ui.theme.ColorSuccess
import com.couplefit.ui.theme.StepsAccent
import com.couplefit.ui.theme.SystemRed
import com.couplefit.ui.theme.VitalsAccent
import kotlin.math.cos
import kotlin.math.sin

/**
 * Animated icon suite inspired by modern motion icon designs:
 * - lucide-animated
 * - movingicons.dev
 * - animateicons.in
 * - itshover.com
 * - heroicons-animated
 *
 * All icons follow standard 24dp grids, 2dp stroke geometry, rounded joins,
 * and high-performance Compose hardware-accelerated spring/physics animations.
 */

// ---------------------------------------------------------------------------
// 1. ANIMATED HEART ICON (Cardiac Pulse / Nudge spring)
// ---------------------------------------------------------------------------
@Composable
fun AnimatedHeartIcon(
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
    isPulsing: Boolean = true,
    filled: Boolean = false,
    reduceMotion: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "heartBeatTransition")
    val beatScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1350
                1.0f at 0
                1.22f at 130 with FastOutSlowInEasing
                0.96f at 260 with FastOutSlowInEasing
                1.12f at 390 with FastOutSlowInEasing
                1.0f at 520 with FastOutSlowInEasing
                1.0f at 1350
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "heartBeatScale"
    )

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "heartPressScale"
    )

    val currentScale = when {
        reduceMotion -> 1.0f
        isPressed -> pressScale
        isPulsing -> beatScale
        else -> 1.0f
    }

    Box(
        modifier = modifier
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer {
                    scaleX = currentScale
                    scaleY = currentScale
                }
        ) {
            val s = size.minDimension / 24f
            val strokeW = 2f * s

            val heartPath = Path().apply {
                moveTo(19f * s, 14f * s)
                cubicTo(20.49f * s, 12.54f * s, 22f * s, 10.79f * s, 22f * s, 8.5f * s)
                cubicTo(22f * s, 5.46f * s, 19.54f * s, 3f * s, 16.5f * s, 3f * s)
                cubicTo(14.74f * s, 3f * s, 13.5f * s, 3.8f * s, 12f * s, 5f * s)
                cubicTo(10.5f * s, 3.8f * s, 9.26f * s, 3f * s, 7.5f * s, 3f * s)
                cubicTo(4.46f * s, 3f * s, 2f * s, 5.46f * s, 2f * s, 8.5f * s)
                cubicTo(2f * s, 10.8f * s, 3.51f * s, 12.55f * s, 5f * s, 14f * s)
                lineTo(12f * s, 21f * s)
                close()
            }

            if (filled) {
                drawPath(path = heartPath, color = tint)
            }

            drawPath(
                path = heartPath,
                color = tint,
                style = Stroke(
                    width = strokeW,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }
    }
}

// ---------------------------------------------------------------------------
// 2. ANIMATED SETTINGS GEAR ICON (Mechanical Spin on Click & Ambient Rotation)
// ---------------------------------------------------------------------------
@Composable
fun AnimatedSettingsIcon(
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
    isSpinning: Boolean = false,
    reduceMotion: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    var rotationTrigger by remember { mutableStateOf(0f) }

    // When clicked, spin 90 degrees with snappy spring physics
    val clickRotation by animateFloatAsState(
        targetValue = rotationTrigger,
        animationSpec = spring(
            dampingRatio = 0.65f,
            stiffness = Spring.StiffnessLow
        ),
        label = "gearClickRotation"
    )

    // Optional continuous ambient spin
    val infiniteTransition = rememberInfiniteTransition(label = "gearSpinTransition")
    val continuousRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gearContinuousRotation"
    )

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "gearPressScale"
    )

    val finalRotation = when {
        reduceMotion -> 0f
        isSpinning -> (continuousRotation + clickRotation) % 360f
        else -> clickRotation
    }

    Box(
        modifier = modifier
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) {
                        rotationTrigger += 90f
                        onClick()
                    }
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer {
                    scaleX = if (reduceMotion) 1.0f else pressScale
                    scaleY = if (reduceMotion) 1.0f else pressScale
                    rotationZ = finalRotation
                }
        ) {
            val s = size.minDimension / 24f
            val strokeW = 2f * s
            val cx = 12f * s
            val cy = 12f * s

            // Draw procedural 8-teeth gear with rounded caps & joins (Lucide geometry)
            val numTeeth = 8
            val rOuter = 9.4f * s
            val rInner = 6.4f * s
            val toothAngleHalf = (Math.PI / 18).toFloat()
            val valleyAngleHalf = (Math.PI / 16).toFloat()

            val gearPath = Path()
            for (i in 0 until numTeeth) {
                val centerAngle = (i * 2 * Math.PI / numTeeth).toFloat()
                val a1 = centerAngle - toothAngleHalf
                val a2 = centerAngle + toothAngleHalf
                val a3 = centerAngle + toothAngleHalf + valleyAngleHalf
                val a0 = centerAngle - toothAngleHalf - valleyAngleHalf

                val pOuter1 = Offset(cx + rOuter * cos(a1), cy + rOuter * sin(a1))
                val pOuter2 = Offset(cx + rOuter * cos(a2), cy + rOuter * sin(a2))
                val pInnerValley = Offset(cx + rInner * cos(a3), cy + rInner * sin(a3))

                if (i == 0) {
                    val pStart = Offset(cx + rInner * cos(a0), cy + rInner * sin(a0))
                    gearPath.moveTo(pStart.x, pStart.y)
                }
                gearPath.lineTo(pOuter1.x, pOuter1.y)
                gearPath.lineTo(pOuter2.x, pOuter2.y)
                gearPath.lineTo(pInnerValley.x, pInnerValley.y)
            }
            gearPath.close()

            drawPath(
                path = gearPath,
                color = tint,
                style = Stroke(
                    width = strokeW,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // Inner Center Circle
            drawCircle(
                color = tint,
                radius = 3f * s,
                center = Offset(cx, cy),
                style = Stroke(
                    width = strokeW,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }
    }
}

// ---------------------------------------------------------------------------
// 3. ANIMATED ARROW BACK ICON (Smooth Left Glide on Click / Press)
// ---------------------------------------------------------------------------
@Composable
fun AnimatedArrowBackIcon(
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
    reduceMotion: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val translationX by animateFloatAsState(
        targetValue = if (isPressed && !reduceMotion) -4f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "arrowBackTranslation"
    )

    Box(
        modifier = modifier
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer {
                    this.translationX = translationX * density
                }
        ) {
            val s = size.minDimension / 24f
            val strokeW = 2f * s

            // Shaft: (19, 12) -> (5, 12)
            drawLine(
                color = tint,
                start = Offset(19f * s, 12f * s),
                end = Offset(5f * s, 12f * s),
                strokeWidth = strokeW,
                cap = StrokeCap.Round
            )

            // Head: (12, 19) -> (5, 12) -> (12, 5)
            val headPath = Path().apply {
                moveTo(12f * s, 19f * s)
                lineTo(5f * s, 12f * s)
                lineTo(12f * s, 5f * s)
            }
            drawPath(
                path = headPath,
                color = tint,
                style = Stroke(
                    width = strokeW,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }
    }
}

// ---------------------------------------------------------------------------
// 4. ANIMATED WIFI OFF ICON (Pulse Offline Indicator & Slash Geometry)
// ---------------------------------------------------------------------------
@Composable
fun AnimatedWifiOffIcon(
    modifier: Modifier = Modifier,
    tint: Color = SystemRed,
    reduceMotion: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wifiOffTransition")
    val waveAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wifiOffWaveAlpha"
    )

    val currentAlpha = if (reduceMotion) 0.8f else waveAlpha

    Canvas(modifier = modifier) {
        val s = size.minDimension / 24f
        val strokeW = 2f * s
        val waveColor = tint.copy(alpha = currentAlpha)

        // Outer Arc right segment
        val topArcRight = Path().apply {
            moveTo(10.71f * s, 5.05f * s)
            cubicTo(14.5f * s, 5.5f * s, 19.5f * s, 6.8f * s, 22.58f * s, 9f * s)
        }
        drawPath(
            path = topArcRight,
            color = waveColor,
            style = Stroke(strokeW, cap = StrokeCap.Round)
        )

        // Outer Arc left segment
        val topArcLeft = Path().apply {
            moveTo(1.42f * s, 9f * s)
            cubicTo(2.8f * s, 7.8f * s, 4.4f * s, 6.8f * s, 6.12f * s, 6.12f * s)
        }
        drawPath(
            path = topArcLeft,
            color = waveColor,
            style = Stroke(strokeW, cap = StrokeCap.Round)
        )

        // Middle Arc
        val midArc = Path().apply {
            moveTo(8.53f * s, 16.11f * s)
            cubicTo(9.5f * s, 15.2f * s, 10.7f * s, 14.8f * s, 12f * s, 14.8f * s)
            cubicTo(13.3f * s, 14.8f * s, 14.5f * s, 15.2f * s, 15.48f * s, 16.11f * s)
        }
        drawPath(
            path = midArc,
            color = waveColor,
            style = Stroke(strokeW, cap = StrokeCap.Round)
        )

        // Bottom Dot
        drawCircle(
            color = waveColor,
            radius = strokeW * 0.75f,
            center = Offset(12f * s, 20f * s)
        )

        // Diagonal Strike Slash: (2, 2) -> (22, 22)
        drawLine(
            color = tint,
            start = Offset(2f * s, 2f * s),
            end = Offset(22f * s, 22f * s),
            strokeWidth = strokeW,
            cap = StrokeCap.Round
        )
    }
}

// ---------------------------------------------------------------------------
// 5. ANIMATED CHEVRON ICON (Accordion Expand/Collapse Smooth 180° Flip)
// ---------------------------------------------------------------------------
@Composable
fun AnimatedChevronIcon(
    isExpanded: Boolean,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
    reduceMotion: Boolean = false
) {
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = spring(
            dampingRatio = 0.72f,
            stiffness = Spring.StiffnessLow
        ),
        label = "chevronRotation"
    )

    Canvas(
        modifier = modifier.graphicsLayer {
            rotationZ = if (reduceMotion) (if (isExpanded) 180f else 0f) else rotation
        }
    ) {
        val s = size.minDimension / 24f
        val strokeW = 2f * s

        val path = Path().apply {
            moveTo(6f * s, 9f * s)
            lineTo(12f * s, 15f * s)
            lineTo(18f * s, 9f * s)
        }
        drawPath(
            path = path,
            color = tint,
            style = Stroke(
                width = strokeW,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}

// ---------------------------------------------------------------------------
// 6. ANIMATED CHEVRON RIGHT ICON (Interactive Row Nudge)
// ---------------------------------------------------------------------------
@Composable
fun AnimatedChevronRightIcon(
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
    isPressed: Boolean = false,
    reduceMotion: Boolean = false
) {
    val offsetX by animateFloatAsState(
        targetValue = if (isPressed && !reduceMotion) 3f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "chevronRightOffset"
    )

    Canvas(
        modifier = modifier.graphicsLayer {
            this.translationX = offsetX * density
        }
    ) {
        val s = size.minDimension / 24f
        val strokeW = 2f * s

        val path = Path().apply {
            moveTo(9f * s, 18f * s)
            lineTo(15f * s, 12f * s)
            lineTo(9f * s, 6f * s)
        }
        drawPath(
            path = path,
            color = tint,
            style = Stroke(
                width = strokeW,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}

// ---------------------------------------------------------------------------
// 7. ANIMATED CHECK ICON (Scale Pop & Checkmark Geometry)
// ---------------------------------------------------------------------------
@Composable
fun AnimatedCheckIcon(
    modifier: Modifier = Modifier,
    tint: Color = ColorSuccess,
    reduceMotion: Boolean = false
) {
    val scale by animateFloatAsState(
        targetValue = 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "checkScale"
    )

    Canvas(
        modifier = modifier.graphicsLayer {
            scaleX = if (reduceMotion) 1f else scale
            scaleY = if (reduceMotion) 1f else scale
        }
    ) {
        val s = size.minDimension / 24f
        val strokeW = 2.4f * s

        val path = Path().apply {
            moveTo(4f * s, 12f * s)
            lineTo(9f * s, 17f * s)
            lineTo(20f * s, 6f * s)
        }
        drawPath(
            path = path,
            color = tint,
            style = Stroke(
                width = strokeW,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}

// ---------------------------------------------------------------------------
// 8. ANIMATED ACTIVITY ICON (ECG Heart Rhythm Pulse)
// ---------------------------------------------------------------------------
@Composable
fun AnimatedActivityIcon(
    modifier: Modifier = Modifier,
    tint: Color = VitalsAccent,
    isPulsing: Boolean = true,
    reduceMotion: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "activityTransition")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1500
                1.0f at 0
                1.16f at 200 with FastOutSlowInEasing
                0.97f at 350 with FastOutSlowInEasing
                1.0f at 500 with FastOutSlowInEasing
                1.0f at 1500
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "activityScale"
    )

    val currentScale = if (reduceMotion || !isPulsing) 1.0f else pulseScale

    Canvas(
        modifier = modifier.graphicsLayer {
            scaleX = currentScale
            scaleY = currentScale
        }
    ) {
        val s = size.minDimension / 24f
        val strokeW = 2f * s

        val path = Path().apply {
            moveTo(2f * s, 12f * s)
            lineTo(6f * s, 12f * s)
            lineTo(9f * s, 3f * s)
            lineTo(15f * s, 21f * s)
            lineTo(18f * s, 12f * s)
            lineTo(22f * s, 12f * s)
        }
        drawPath(
            path = path,
            color = tint,
            style = Stroke(
                width = strokeW,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}

// ---------------------------------------------------------------------------
// 9. ANIMATED FOOTSTEPS ICON (Alternating Step Rhythm)
// ---------------------------------------------------------------------------
@Composable
fun AnimatedFootstepsIcon(
    modifier: Modifier = Modifier,
    tint: Color = StepsAccent,
    reduceMotion: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "footstepsTransition")
    val stepPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "footstepsPhase"
    )

    val leftAlpha = when {
        reduceMotion -> 1.0f
        stepPhase < 1f -> 1.0f
        else -> 0.45f
    }
    val rightAlpha = when {
        reduceMotion -> 1.0f
        stepPhase >= 1f -> 1.0f
        else -> 0.45f
    }

    Canvas(modifier = modifier) {
        val s = size.minDimension / 24f
        val strokeW = 2f * s

        // Left Footprint
        val leftPath = Path().apply {
            moveTo(5.5f * s, 13f * s)
            cubicTo(4f * s, 13f * s, 3f * s, 11f * s, 3f * s, 8.5f * s)
            cubicTo(3f * s, 5.5f * s, 5f * s, 3.5f * s, 7.5f * s, 3.5f * s)
            cubicTo(8.5f * s, 3.5f * s, 9f * s, 5f * s, 9f * s, 7.5f * s)
            cubicTo(9f * s, 10f * s, 7f * s, 13f * s, 5.5f * s, 13f * s)
            close()
        }
        drawPath(
            path = leftPath,
            color = tint.copy(alpha = leftAlpha),
            style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Right Footprint
        val rightPath = Path().apply {
            moveTo(18.5f * s, 20.5f * s)
            cubicTo(17f * s, 20.5f * s, 15f * s, 18.5f * s, 15f * s, 16f * s)
            cubicTo(15f * s, 13f * s, 17f * s, 11f * s, 19.5f * s, 11f * s)
            cubicTo(20.5f * s, 11f * s, 21f * s, 12.5f * s, 21f * s, 15f * s)
            cubicTo(21f * s, 17.5f * s, 20f * s, 20.5f * s, 18.5f * s, 20.5f * s)
            close()
        }
        drawPath(
            path = rightPath,
            color = tint.copy(alpha = rightAlpha),
            style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}
