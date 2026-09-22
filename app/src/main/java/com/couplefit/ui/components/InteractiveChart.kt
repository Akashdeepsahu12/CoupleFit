package com.couplefit.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.couplefit.ui.theme.SystemBlue
import com.couplefit.ui.theme.SystemGreen
import com.couplefit.ui.theme.SystemRed

data class ChartDataPoint(val xLabel: String, val value: Float)

@Composable
fun InteractiveChart(
    data: List<ChartDataPoint>,
    lineColor: Color,
    modifier: Modifier = Modifier,
    highThreshold: Float = 75f,
    lowThreshold: Float = 55f
) {
    if (data.isEmpty()) return

    val haptic = LocalHapticFeedback.current
    var scrubbedIndex by remember { mutableStateOf<Int?>(null) }

    val minDataValue = data.minOf { it.value }
    val maxDataValue = data.maxOf { it.value }

    val lowestPoint = data.minByOrNull { it.value }
    val highestPoint = data.maxByOrNull { it.value }

    // Infinite transition for continuous ECG sweep animation
    val infiniteTransition = rememberInfiniteTransition(label = "ecgSweep")
    val sweepProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweepX"
    )

    // Pulse animation for pinpoint alert indicators
    val pulseTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by pulseTransition.animateFloat(
        initialValue = 1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val pulseAlpha by pulseTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Column(modifier = modifier) {
        // High / Low Summary Badges at Top
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            highestPoint?.let { high ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(SystemRed.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(SystemRed)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Highest: ${high.value.toInt()} BPM (${high.xLabel})",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = SystemRed
                    )
                }
            }

            lowestPoint?.let { low ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(SystemBlue.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(SystemBlue)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Lowest: ${low.value.toInt()} BPM (${low.xLabel})",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = SystemBlue
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val index = (offset.x / (size.width / (data.size - 1).coerceAtLeast(1)))
                                .toInt().coerceIn(0, data.size - 1)
                            scrubbedIndex = index
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        },
                        onDragEnd = { scrubbedIndex = null },
                        onDragCancel = { scrubbedIndex = null },
                        onDrag = { change, _ ->
                            val newIndex = (change.position.x / (size.width / (data.size - 1).coerceAtLeast(1)))
                                .toInt().coerceIn(0, data.size - 1)
                            if (newIndex != scrubbedIndex) {
                                scrubbedIndex = newIndex
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = { offset ->
                            val index = (offset.x / (size.width / (data.size - 1).coerceAtLeast(1)))
                                .toInt().coerceIn(0, data.size - 1)
                            scrubbedIndex = index
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            tryAwaitRelease()
                            scrubbedIndex = null
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val stepX = width / (data.size - 1).coerceAtLeast(1)

                val minVal = (minDataValue - 10f).coerceAtLeast(0f)
                val maxVal = (maxDataValue + 10f).coerceAtLeast(minVal + 1f)
                val range = maxVal - minVal

                // Subtle transparent ECG grid lines
                val gridLines = 4
                for (i in 0..gridLines) {
                    val y = height * (i.toFloat() / gridLines)
                    drawLine(
                        color = Color.White.copy(alpha = 0.06f),
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                    )
                }

                // Generate points
                val points = data.mapIndexed { index, point ->
                    val x = index * stepX
                    val y = height - ((point.value - minVal) / range * height)
                    Offset(x, y)
                }

                // Smooth cubic/linear wave path
                val mainPath = Path()
                val fillPath = Path()

                points.forEachIndexed { index, point ->
                    if (index == 0) {
                        mainPath.moveTo(point.x, point.y)
                        fillPath.moveTo(point.x, height)
                        fillPath.lineTo(point.x, point.y)
                    } else {
                        val prev = points[index - 1]
                        val cx = (prev.x + point.x) / 2f
                        mainPath.cubicTo(cx, prev.y, cx, point.y, point.x, point.y)
                        fillPath.cubicTo(cx, prev.y, cx, point.y, point.x, point.y)
                    }
                }
                fillPath.lineTo(width, height)
                fillPath.close()

                // Draw translucent gradient underneath chart line
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            lineColor.copy(alpha = 0.25f),
                            lineColor.copy(alpha = 0.02f)
                        )
                    )
                )

                // Draw standard ECG base curve
                drawPath(
                    path = mainPath,
                    color = lineColor.copy(alpha = 0.35f),
                    style = Stroke(
                        width = 3f,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                // Dynamic ECG Scan / Sweep Animation
                val sweepX = sweepProgress * width
                val sweepGlowRadius = 60f

                // Draw bright glowing segment around sweep head
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            lineColor.copy(alpha = 0.6f),
                            Color.Transparent
                        ),
                        center = Offset(sweepX, height / 2),
                        radius = sweepGlowRadius
                    ),
                    radius = sweepGlowRadius,
                    center = Offset(sweepX, height / 2)
                )

                // Draw ECG sweep beacon line
                drawLine(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            lineColor.copy(alpha = 0.7f),
                            Color.Transparent
                        )
                    ),
                    start = Offset(sweepX, 0f),
                    end = Offset(sweepX, height),
                    strokeWidth = 2.5f
                )

                // High and Low pinpoint markers with pulse animation
                data.forEachIndexed { index, point ->
                    val offset = points[index]
                    val isHigh = point.value == maxDataValue
                    val isLow = point.value == minDataValue
                    val isNearSweep = kotlin.math.abs(offset.x - sweepX) < 30f

                    if (isHigh || isLow) {
                        val markerColor = if (isHigh) SystemRed else SystemBlue

                        // Pulsing outer ripple
                        drawCircle(
                            color = markerColor.copy(alpha = pulseAlpha * (if (isNearSweep) 1.5f else 1f)),
                            radius = (10f * pulseScale) * (if (isNearSweep) 1.3f else 1f),
                            center = offset
                        )

                        // Central pin-point dot
                        drawCircle(
                            color = markerColor,
                            radius = 6f,
                            center = offset
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 2.5f,
                            center = offset
                        )

                        // Pin-point pointer line
                        val tagYOffset = if (isHigh) -22f else 22f
                        drawLine(
                            color = markerColor.copy(alpha = 0.7f),
                            start = offset,
                            end = Offset(offset.x, offset.y + tagYOffset),
                            strokeWidth = 2f
                        )
                    }
                }

                // Interactive Scrubber when user touches/drags
                scrubbedIndex?.let { index ->
                    val point = points[index]
                    val dataItem = data[index]

                    drawLine(
                        color = Color.White.copy(alpha = 0.8f),
                        start = Offset(point.x, 0f),
                        end = Offset(point.x, height),
                        strokeWidth = 2f
                    )

                    drawCircle(
                        color = lineColor,
                        radius = 9f,
                        center = point
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 4.5f,
                        center = point
                    )
                }
            }

            // Scrubber Tooltip overlay
            scrubbedIndex?.let { index ->
                val point = data[index]
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${point.xLabel}: ${point.value.toInt()} BPM",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
