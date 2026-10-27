package com.couplefit.ui.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.couplefit.ui.theme.*
import kotlin.math.roundToInt

data class ChartDataPoint(
    val xLabel: String,
    val value: Float,
    val timestamp: String? = null
)

/**
 * Health Metric Trend Chart optimized to Apple Human Interface Guidelines (HIG) for Charting Data:
 * https://developer.apple.com/design/human-interface-guidelines/charting-data
 *
 * Core HIG Design Pillars:
 * 1. Design with Purpose & Glanceability:
 *    - Prominent leading takeaway (large value readout, clear metric name & units).
 *    - Instant context: Range (Min–Max) and Daily Average visible at a glance.
 *    - Clear semantic badges highlighting key data points (Peak / Low).
 * 2. Visual Hierarchy & Anatomy:
 *    - Clear Plot Area with subtle Apple-style reference gridlines.
 *    - Y-Axis numerical tick labels to ground the scale and provide immediate frame of reference.
 *    - Smooth cubic spline data curve with luminous, non-cluttering gradient area fill.
 *    - Graceful single-point visualization (reference baseline + focal mark).
 * 3. Continuous Scrubbing & Progressive Disclosure:
 *    - Silky smooth touch & continuous drag scrubbing across the entire chart area.
 *    - Vertical tracking hairline with highlighted focus ring on the data curve.
 *    - Responsive haptic ticks as the scrubber crosses each data point boundary.
 *    - Floating Apple-style pill callout + dynamic header readout disclosing exact values and vs-average deltas.
 * 4. Accessibility:
 *    - Full semantic description (Role.Image, contentDescription, dynamic stateDescription).
 *    - Screen reader (TalkBack) custom actions to step through individual readings.
 * 5. Adaptability & Motion:
 *    - Seamless Dark and Light theme adaptation with WCAG-compliant contrast.
 *    - Strict adherence to reduceMotion preferences (bypasses sweeps, disables looping pulses).
 */
@Composable
fun InteractiveChart(
    data: List<ChartDataPoint>,
    lineColor: Color,
    modifier: Modifier = Modifier,
    metricUnit: String = "BPM",
    metricTitle: String = "Heart Rate",
    highThreshold: Float = 75f,
    lowThreshold: Float = 55f,
    strokeWidth: Dp = 1.75.dp,
    reduceMotion: Boolean = false,
    syncTrigger: Any? = null,
    showHeader: Boolean = true
) {
    if (data.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No chart data available",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val isDark = isSystemInDarkTheme()
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current
    var scrubbedIndex by remember { mutableStateOf<Int?>(null) }

    // Statistical Takeaways
    val minDataValue = remember(data) { data.minOf { it.value } }
    val maxDataValue = remember(data) { data.maxOf { it.value } }
    val avgValue = remember(data) { data.map { it.value }.average().toFloat() }
    val latestValue = remember(data) { data.lastOrNull()?.value ?: avgValue }

    val highestPoint = remember(data) { data.maxByOrNull { it.value } }
    val lowestPoint = remember(data) { data.minByOrNull { it.value } }

    // Progressive Reveal Animation (Apple-style draw-in on load/sync)
    val revealProgress = remember { Animatable(if (reduceMotion) 1f else 0f) }

    LaunchedEffect(syncTrigger, reduceMotion) {
        if (reduceMotion || syncTrigger == 0) {
            revealProgress.snapTo(1f)
        } else {
            revealProgress.snapTo(0f)
            revealProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 1100,
                    easing = FastOutSlowInEasing
                )
            )
        }
    }

    // Dynamic Live Counter Value synced with reveal or scrub state
    val displayCounterValue = remember(scrubbedIndex, revealProgress.value, reduceMotion, data) {
        if (scrubbedIndex != null) {
            data[scrubbedIndex!!].value.toInt()
        } else if (reduceMotion) {
            latestValue.toInt()
        } else {
            val progress = revealProgress.value.coerceIn(0f, 1f)
            (minDataValue + (latestValue - minDataValue) * progress).toInt()
        }
    }

    // Subtle breathing pulse for pinpoint indicators (disabled when reduceMotion = true)
    val infiniteTransition = rememberInfiniteTransition(label = "pulseTransition")
    val animatedPulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val pulseScale = if (reduceMotion) 1f else animatedPulseScale

    val animatedPulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )
    val pulseAlpha = if (reduceMotion) 0.4f else animatedPulseAlpha

    // Y-Axis Paint for native hardware-accelerated text rendering along gridlines
    val yAxisPaint = remember(isDark, density) {
        Paint().apply {
            color = if (isDark) {
                android.graphics.Color.argb(160, 160, 171, 190) // TextSecondaryDark
            } else {
                android.graphics.Color.argb(180, 90, 102, 122)   // TextSecondaryLight
            }
            textSize = with(density) { 10.sp.toPx() }
            isAntiAlias = true
            textAlign = Paint.Align.RIGHT
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
    }

    // Accessibility Narrative (Apple HIG: Essential labels that describe values & components)
    val accessibilityDescription = remember(data, latestValue, avgValue, highestPoint, lowestPoint) {
        buildString {
            append("$metricTitle trend chart. ")
            if (data.size == 1) {
                append("Single reading today: ${latestValue.toInt()} $metricUnit. ")
            } else {
                append("Latest reading: ${latestValue.toInt()} $metricUnit. ")
                append("Daily average: ${avgValue.toInt()} $metricUnit. ")
                append("Range: ${minDataValue.toInt()} to ${maxDataValue.toInt()} $metricUnit. ")
                highestPoint?.let { append("Peak: ${it.value.toInt()} $metricUnit at ${it.xLabel}. ") }
                lowestPoint?.let { append("Lowest: ${it.value.toInt()} $metricUnit at ${it.xLabel}. ") }
                append("${data.size} total readings logged. Drag horizontally to scrub through readings.")
            }
        }
    }

    val yAxisWidthDp = 32.dp

    Column(
        modifier = modifier
            .semantics {
                contentDescription = accessibilityDescription
                stateDescription = scrubbedIndex?.let {
                    val pt = data[it]
                    "Selected ${pt.xLabel}, ${pt.value.toInt()} $metricUnit"
                } ?: "Showing full trend"
                role = Role.Image
                customActions = listOf(
                    CustomAccessibilityAction("Inspect Next Reading") {
                        val next = ((scrubbedIndex ?: -1) + 1).coerceAtMost(data.size - 1)
                        scrubbedIndex = next
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        true
                    },
                    CustomAccessibilityAction("Inspect Previous Reading") {
                        val prev = ((scrubbedIndex ?: data.size) - 1).coerceAtLeast(0)
                        scrubbedIndex = prev
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        true
                    },
                    CustomAccessibilityAction("Reset Chart View") {
                        scrubbedIndex = null
                        true
                    }
                )
            }
    ) {
        if (showHeader) {
            // --- 1. GLANCEABLE SUMMARY HEADER ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Main Value Readout
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "$displayCounterValue",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (scrubbedIndex != null) lineColor else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.alignByBaseline()
                    )
                    Spacer(modifier = Modifier.width(AppSpacing.xxs))
                    Column(modifier = Modifier.padding(bottom = 2.dp)) {
                        Text(
                            text = metricUnit,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = lineColor
                        )
                        Text(
                            text = if (scrubbedIndex != null) "SCRUBBING" else metricTitle.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (scrubbedIndex != null) lineColor else MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.5.sp,
                            fontWeight = if (scrubbedIndex != null) FontWeight.Bold else FontWeight.SemiBold,
                            fontSize = 9.sp
                        )
                    }
                }

                // Key Milestone Badges (Peak & Min)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    highestPoint?.let { high ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(AppSpacing.xs))
                                .background(ColorCritical.copy(alpha = 0.12f))
                                .padding(horizontal = AppSpacing.xs, vertical = 3.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(ColorCritical)
                            )
                            Spacer(modifier = Modifier.width(AppSpacing.xxs))
                            Text(
                                text = "${high.value.toInt()} Peak",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = ColorCritical
                            )
                        }
                    }

                    lowestPoint?.let { low ->
                        if (highestPoint?.value != lowestPoint.value) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(AppSpacing.xs))
                                    .background(ColorInfo.copy(alpha = 0.12f))
                                    .padding(horizontal = AppSpacing.xs, vertical = 3.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(ColorInfo)
                                )
                                Spacer(modifier = Modifier.width(AppSpacing.xxs))
                                Text(
                                    text = "${low.value.toInt()} Min",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ColorInfo
                                )
                            }
                        }
                    }
                }
            }

            // --- 2. SECONDARY CONTEXT ROW (Average, Range, or Scrubbed Delta) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = AppSpacing.xs),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (scrubbedIndex == null) {
                    val avgInt = avgValue.toInt()
                    Text(
                        text = if (data.size > 1) {
                            "Daily Avg $avgInt $metricUnit • Range ${minDataValue.toInt()}–${maxDataValue.toInt()} $metricUnit"
                        } else {
                            "Latest reading logged today"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                        fontSize = 11.sp
                    )
                } else {
                    val pt = data[scrubbedIndex!!]
                    val diff = (pt.value - avgValue).roundToInt()
                    val diffStr = if (diff > 0) "+$diff vs avg" else if (diff < 0) "$diff vs avg" else "Equal to avg"
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(lineColor)
                        )
                        Spacer(modifier = Modifier.width(AppSpacing.xxs))
                        Text(
                            text = "${pt.xLabel} • $diffStr",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = lineColor,
                            fontSize = 11.sp
                        )
                    }
                    Text(
                        text = "Release to dismiss",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        fontSize = 10.sp
                    )
                }
            }
        }

        // --- 3. PLOT AREA (Interactive Canvas with Continuous Scrubbing) ---
        val plotModifier = if (showHeader) {
            Modifier.fillMaxWidth().weight(1f)
        } else {
            Modifier.fillMaxWidth().fillMaxHeight()
        }

        Box(
            modifier = plotModifier
                .pointerInput(data) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val yAxisPx = yAxisWidthDp.toPx()
                        val plotWidth = (size.width - yAxisPx).coerceAtLeast(1f)
                        val stepX = if (data.size > 1) plotWidth / (data.size - 1) else 1f

                        val updateIndex = { pos: Offset ->
                            if (data.isNotEmpty()) {
                                val relX = pos.x.coerceIn(0f, plotWidth)
                                val targetIndex = if (data.size > 1) {
                                    ((relX + stepX / 2f) / stepX).toInt().coerceIn(0, data.size - 1)
                                } else {
                                    0
                                }
                                if (scrubbedIndex != targetIndex) {
                                    scrubbedIndex = targetIndex
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                            }
                        }

                        try {
                            updateIndex(down.position)
                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull() ?: break
                                if (change.pressed) {
                                    updateIndex(change.position)
                                    change.consume()
                                } else {
                                    break
                                }
                            }
                        } finally {
                            scrubbedIndex = null
                        }
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val totalWidth = size.width
                val totalHeight = size.height

                val yAxisPx = yAxisWidthDp.toPx()
                val plotWidth = (totalWidth - yAxisPx).coerceAtLeast(1f)

                // Top & Bottom Breathing Room
                val topPadding = 10.dp.toPx()
                val bottomPadding = 10.dp.toPx()
                val plotHeight = (totalHeight - topPadding - bottomPadding).coerceAtLeast(1f)

                // Scale computation: focused range with padding to prevent clipping
                val span = (maxDataValue - minDataValue).coerceAtLeast(0f)
                val pad = if (span == 0f) 12f else (span * 0.15f).coerceAtLeast(4f)
                val minVal = (minDataValue - pad).coerceAtLeast(0f)
                val maxVal = maxDataValue + pad
                val range = (maxVal - minVal).coerceAtLeast(1f)

                // Subtle Reference Grid & Y-Axis Numerical Labels (Apple HIG)
                val gridLinesCount = 3
                val gridColor = if (isDark) Color(0xFF26324A).copy(alpha = 0.55f) else Color(0xFFE2E6EE)
                val dashEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 6.dp.toPx()), 0f)

                for (i in 0..gridLinesCount) {
                    val fraction = i.toFloat() / gridLinesCount
                    val y = topPadding + fraction * plotHeight

                    // Gridline across the plot area
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(plotWidth, y),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = dashEffect
                    )

                    // Y-Axis Tick Value Label
                    val lineVal = (maxVal - fraction * range).roundToInt()
                    drawIntoCanvas { canvas ->
                        canvas.nativeCanvas.drawText(
                            "$lineVal",
                            totalWidth - 2.dp.toPx(),
                            y + 3.5.dp.toPx(),
                            yAxisPaint
                        )
                    }
                }

                // --- DATA RENDERING ---
                if (data.size == 1) {
                    // Graceful Single-Point Visualization
                    val singlePoint = data[0]
                    val y = topPadding + (1f - (singlePoint.value - minVal) / range) * plotHeight
                    val centerX = plotWidth / 2f

                    // Soft horizontal reference baseline
                    drawLine(
                        color = lineColor.copy(alpha = 0.35f),
                        start = Offset(0f, y),
                        end = Offset(plotWidth, y),
                        strokeWidth = (strokeWidth * 0.75f).toPx(),
                        pathEffect = dashEffect
                    )

                    // Area fill below baseline
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                lineColor.copy(alpha = if (isDark) 0.15f else 0.22f),
                                Color.Transparent
                            ),
                            startY = y,
                            endY = topPadding + plotHeight
                        ),
                        topLeft = Offset(0f, y),
                        size = androidx.compose.ui.geometry.Size(plotWidth, topPadding + plotHeight - y)
                    )

                    // Refined center mark
                    drawCircle(
                        color = lineColor.copy(alpha = 0.25f * pulseScale),
                        radius = 8.dp.toPx() * pulseScale,
                        center = Offset(centerX, y)
                    )
                    drawCircle(
                        color = lineColor,
                        radius = 4.dp.toPx(),
                        center = Offset(centerX, y)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 1.8.dp.toPx(),
                        center = Offset(centerX, y)
                    )
                } else {
                    // Multi-point Smooth Waveform
                    val stepX = plotWidth / (data.size - 1)
                    val points = data.mapIndexed { index, point ->
                        val x = index * stepX
                        val y = topPadding + (1f - (point.value - minVal) / range) * plotHeight
                        Offset(x, y)
                    }

                    // Cubic Bézier Spline for organic health waveforms
                    val fullPath = Path()
                    points.forEachIndexed { index, point ->
                        if (index == 0) {
                            fullPath.moveTo(point.x, point.y)
                        } else {
                            val prev = points[index - 1]
                            val cx = (prev.x + point.x) / 2f
                            fullPath.cubicTo(cx, prev.y, cx, point.y, point.x, point.y)
                        }
                    }

                    // Animated segment measurement
                    val pathMeasure = PathMeasure()
                    pathMeasure.setPath(fullPath, false)
                    val totalLength = pathMeasure.length
                    val currentDrawProgress = revealProgress.value.coerceIn(0f, 1f)
                    val currentLength = totalLength * currentDrawProgress

                    val animatedPath = Path()
                    if (currentLength > 0f) {
                        pathMeasure.getSegment(0f, currentLength, animatedPath, true)
                    }

                    // Luminous Gradient Fill under curve
                    if (currentLength > 0f) {
                        val fillPath = Path().apply {
                            addPath(animatedPath)
                            val tipPos = pathMeasure.getPosition(currentLength)
                            lineTo(tipPos.x, topPadding + plotHeight)
                            lineTo(points.first().x, topPadding + plotHeight)
                            close()
                        }

                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    lineColor.copy(alpha = if (isDark) 0.22f else 0.28f),
                                    lineColor.copy(alpha = 0.01f)
                                ),
                                startY = topPadding,
                                endY = topPadding + plotHeight
                            )
                        )
                    }

                    // Curve Stroke with Desaturated OLED-safe Gradient in Dark Mode
                    val gradientBrush = if (isDark) {
                        Brush.horizontalGradient(
                            colors = listOf(
                                VitalsGradientStartDark,
                                lineColor,
                                VitalsGradientEndDark
                            ),
                            startX = 0f,
                            endX = plotWidth
                        )
                    } else {
                        Brush.horizontalGradient(
                            colors = listOf(
                                VitalsGradientStartLight,
                                lineColor,
                                VitalsGradientEndLight
                            ),
                            startX = 0f,
                            endX = plotWidth
                        )
                    }

                    if (currentLength > 0f) {
                        drawPath(
                            path = animatedPath,
                            brush = gradientBrush,
                            style = Stroke(
                                width = strokeWidth.toPx(),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }

                    // Peak and Minimum Pinpoint Indicators
                    data.forEachIndexed { index, point ->
                        val offset = points[index]
                        val isHigh = point == highestPoint
                        val isLow = point == lowestPoint && highestPoint?.value != lowestPoint.value
                        val isRevealed = offset.x <= (currentDrawProgress * plotWidth) + 8f

                        if ((isHigh || isLow) && isRevealed) {
                            val markerColor = if (isHigh) ColorCritical else ColorInfo

                            if (!reduceMotion) {
                                drawCircle(
                                    color = markerColor.copy(alpha = pulseAlpha.coerceIn(0f, 1f)),
                                    radius = 5.5.dp.toPx() * pulseScale,
                                    center = offset
                                )
                            }
                            drawCircle(
                                color = markerColor,
                                radius = 3.5.dp.toPx(),
                                center = offset
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 1.5.dp.toPx(),
                                center = offset
                            )
                        }
                    }

                    // --- SCRUBBER INDICATORS (When User Touches & Drags) ---
                    scrubbedIndex?.let { index ->
                        val activePoint = points[index]

                        // Vertical Guideline Hairline
                        drawLine(
                            color = if (isDark) Color.White.copy(alpha = 0.75f) else Color.Black.copy(alpha = 0.45f),
                            start = Offset(activePoint.x, topPadding),
                            end = Offset(activePoint.x, topPadding + plotHeight),
                            strokeWidth = 1.dp.toPx()
                        )

                        // Focused Target Dot on Curve
                        drawCircle(
                            color = lineColor.copy(alpha = 0.35f),
                            radius = 7.dp.toPx(),
                            center = activePoint
                        )
                        drawCircle(
                            color = lineColor,
                            radius = 4.dp.toPx(),
                            center = activePoint
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 1.8.dp.toPx(),
                            center = activePoint
                        )
                    }
                }
            }

            // Floating Apple-Style Pill Callout (Horizontally follows finger, clamped within plot bounds)
            scrubbedIndex?.let { index ->
                val pt = data[index]
                val stepX = if (data.size > 1) {
                    (density.run { 1.dp.toPx() } * 0f) // Computed dynamically below
                } else 0f

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp)
                ) {
                    val yAxisPx = with(density) { yAxisWidthDp.toPx() }
                    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                        val availableWidth = constraints.maxWidth.toFloat()
                        val plotWidth = (availableWidth - yAxisPx).coerceAtLeast(1f)
                        val ptX = if (data.size > 1) {
                            index * (plotWidth / (data.size - 1))
                        } else {
                            plotWidth / 2f
                        }

                        val pillWidthPx = with(density) { 110.dp.toPx() }
                        val clampedOffset = (ptX - pillWidthPx / 2f)
                            .coerceIn(4f, (plotWidth - pillWidthPx).coerceAtLeast(4f))
                            .toInt()

                        Box(
                            modifier = Modifier
                                .offset { IntOffset(clampedOffset, 0) }
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isDark) SurfaceElevatedDark else SurfaceElevatedLight)
                                .border(
                                    1.dp,
                                    if (isDark) BorderSubtleDark else BorderSubtleLight,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = AppSpacing.sm, vertical = 3.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = pt.xLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = " • ",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${pt.value.toInt()} $metricUnit",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = lineColor,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- 4. X-AXIS MILESTONE LABELS ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = yAxisWidthDp, top = AppSpacing.xs),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (data.size == 1) {
                Text(
                    text = data[0].xLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            } else {
                val milestoneIndices = remember(data) {
                    when {
                        data.size <= 3 -> data.indices.toList()
                        data.size <= 6 -> listOf(0, data.size / 2, data.size - 1)
                        else -> listOf(0, data.size / 3, (2 * data.size) / 3, data.size - 1)
                    }
                }

                milestoneIndices.forEach { idx ->
                    val pt = data[idx]
                    val isScrubbed = scrubbedIndex == idx
                    Text(
                        text = pt.xLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isScrubbed) lineColor else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (isScrubbed) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
