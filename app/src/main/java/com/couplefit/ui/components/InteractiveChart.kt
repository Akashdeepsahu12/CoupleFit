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
import kotlin.math.abs
import kotlin.math.roundToInt

data class ChartDataPoint(
    val xLabel: String,
    val value: Float,
    val timestamp: String? = null
)

/**
 * Health Metric Trend Chart optimized to Apple Human Interface Guidelines (HIG) for Charting Data
 * with Bilateral Telemetry, Delta Shading, and Fluid Spring Kinematics (KRP-03, KRP-04, ANM-06, DTV-01).
 *
 * Core Engineering & Craft Pillars:
 * 1. Directional Touch-Slop Arbitration (KRP-03 / Q1):
 *    Calculates horizontal vs. vertical movement deltas against system touch slop. Only engages scrub mode
 *    when `abs(dx) > abs(dy)`. Vertical drags are left unconsumed, allowing the parent vertical scroll
 *    to glide smoothly without finger trapping or viewport freeze.
 * 2. Bilateral Dual-Series Overlaid Telemetry (KRP-04, DTV-01 / Q4, Q8):
 *    Renders the User curve as a solid 2.5dp Electric Cyan spline and the Partner curve as a dashed 2dp
 *    Vibrant Coral spline with translucent delta shading between curves, enabling synchronous comparison.
 * 3. Fluid Spring Scrub Kinematics (ANM-06 / Q6):
 *    Animates the tracking hairline and floating Apple-style callout pill using [Animatable] spring kinematics
 *    (`dampingRatio = 0.85f, stiffness = 600f`) with boundary haptics on hourly traversal.
 * 4. Progressive Reveal Animation:
 *    Silky draw-in on screen open/sync with full `reduceMotion` accessibility fallback.
 */
@Composable
fun InteractiveChart(
    data: List<ChartDataPoint>,
    lineColor: Color,
    modifier: Modifier = Modifier,
    partnerData: List<ChartDataPoint>? = null,
    partnerLineColor: Color = Color(0xFFFF3366), // Vibrant Coral
    userLabel: String = "You",
    partnerLabel: String = "Partner",
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

    // Combined Scale Computation (Bilateral Awareness)
    val allValues = remember(data, partnerData) {
        val userVals = data.map { it.value }
        val partnerVals = partnerData?.map { it.value } ?: emptyList()
        (userVals + partnerVals).ifEmpty { listOf(70f) }
    }
    val minDataValue = remember(allValues) { allValues.minOrNull() ?: 50f }
    val maxDataValue = remember(allValues) { allValues.maxOrNull() ?: 120f }

    // User Telemetry Statistics
    val userAvgValue = remember(data) { data.map { it.value }.average().toFloat() }
    val userLatestValue = remember(data) { data.lastOrNull()?.value ?: userAvgValue }
    val userHighestPoint = remember(data) { data.maxByOrNull { it.value } }
    val userLowestPoint = remember(data) { data.minByOrNull { it.value } }

    // Partner Telemetry Statistics
    val partnerLatestValue = remember(partnerData) { partnerData?.lastOrNull()?.value }

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
            userLatestValue.toInt()
        } else {
            val progress = revealProgress.value.coerceIn(0f, 1f)
            (minDataValue + (userLatestValue - minDataValue) * progress).toInt()
        }
    }

    // Subtle breathing pulse for pinpoint indicators
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

    // Accessibility Narrative
    val accessibilityDescription = remember(data, partnerData, userLatestValue, userAvgValue) {
        buildString {
            append("$metricTitle bilateral trend chart. ")
            append("$userLabel latest: ${userLatestValue.toInt()} $metricUnit. ")
            partnerLatestValue?.let { append("$partnerLabel latest: ${it.toInt()} $metricUnit. ") }
            append("Daily average: ${userAvgValue.toInt()} $metricUnit. ")
            append("Range: ${minDataValue.toInt()} to ${maxDataValue.toInt()} $metricUnit. ")
            append("${data.size} readings. Drag horizontally to scrub through synchronized readings.")
        }
    }

    val yAxisWidthDp = 32.dp

    Column(
        modifier = modifier
            .semantics {
                contentDescription = accessibilityDescription
                stateDescription = scrubbedIndex?.let {
                    val pt = data[it]
                    val partnerPt = partnerData?.getOrNull(it)
                    if (partnerPt != null) {
                        "Selected ${pt.xLabel}: $userLabel ${pt.value.toInt()}, $partnerLabel ${partnerPt.value.toInt()} $metricUnit"
                    } else {
                        "Selected ${pt.xLabel}, ${pt.value.toInt()} $metricUnit"
                    }
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
                // Main Value Readout (With Partner Bilateral Pill if Available)
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

                    // Bilateral Partner Badge (If Partner Series Present)
                    if (partnerData != null && partnerLatestValue != null) {
                        Spacer(modifier = Modifier.width(AppSpacing.sm))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(bottom = 4.dp)
                                .clip(RoundedCornerShape(AppSpacing.xs))
                                .background(partnerLineColor.copy(alpha = 0.14f))
                                .padding(horizontal = AppSpacing.xs, vertical = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(partnerLineColor)
                            )
                            Spacer(modifier = Modifier.width(AppSpacing.xxs))
                            val scrubbedPartnerVal = scrubbedIndex?.let { partnerData.getOrNull(it)?.value?.toInt() }
                            Text(
                                text = "${scrubbedPartnerVal ?: partnerLatestValue.toInt()} $partnerLabel",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = partnerLineColor,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Key Milestone Badges (Peak & Min)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    userHighestPoint?.let { high ->
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

                    userLowestPoint?.let { low ->
                        if (userHighestPoint?.value != low.value) {
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

            // --- 2. SECONDARY CONTEXT ROW (Average, Range, or Bilateral Differential) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = AppSpacing.xs),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (scrubbedIndex == null) {
                    val avgInt = userAvgValue.toInt()
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
                    val partnerPt = partnerData?.getOrNull(scrubbedIndex!!)
                    if (partnerPt != null) {
                        val delta = (pt.value - partnerPt.value).roundToInt()
                        val deltaStr = when {
                            delta > 0 -> "+$delta vs $partnerLabel"
                            delta < 0 -> "$delta vs $partnerLabel"
                            else -> "In Synchrony (Equal)"
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(if (delta >= 0) lineColor else partnerLineColor)
                            )
                            Spacer(modifier = Modifier.width(AppSpacing.xxs))
                            Text(
                                text = "${pt.xLabel} • $deltaStr",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (delta >= 0) lineColor else partnerLineColor,
                                fontSize = 11.sp
                            )
                        }
                    } else {
                        val diff = (pt.value - userAvgValue).roundToInt()
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

        // --- 3. PLOT AREA (Interactive Canvas with Directional Touch-Slop Arbitration) ---
        val plotModifier = if (showHeader) {
            Modifier.fillMaxWidth().weight(1f)
        } else {
            Modifier.fillMaxWidth().fillMaxHeight()
        }

        // Kinematic animated X position for the tracking hairline (ANM-06)
        val animatedHairlineX = remember { Animatable(0f) }

        Box(
            modifier = plotModifier
                .pointerInput(data, partnerData) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val touchSlop = viewConfiguration.touchSlop
                        var totalDx = 0f
                        var totalDy = 0f
                        var isHorizontalDrag = false

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
                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull() ?: break
                                if (!change.pressed) break

                                val delta = change.position - change.previousPosition
                                totalDx += delta.x
                                totalDy += delta.y

                                if (!isHorizontalDrag) {
                                    // Touch-Slop Arbitration (KRP-03 / Q1):
                                    // Only engage scrub mode when horizontal intent is established!
                                    if (abs(totalDx) > touchSlop && abs(totalDx) > abs(totalDy)) {
                                        isHorizontalDrag = true
                                        updateIndex(change.position)
                                        change.consume()
                                    } else if (abs(totalDy) > touchSlop) {
                                        // Vertical scroll intent detected: break out without consuming!
                                        break
                                    }
                                } else {
                                    updateIndex(change.position)
                                    change.consume()
                                }
                            }
                        } finally {
                            scrubbedIndex = null
                        }
                    }
                }
        ) {
            // PERF-02: Hoist Path, PathMeasure, and Brush allocations out of Canvas draw loop to prevent GC allocation churn
            val userFullPath = remember { Path() }
            val userAnimatedPath = remember { Path() }
            val partnerFullPath = remember { Path() }
            val partnerAnimatedPath = remember { Path() }
            val deltaPolygon = remember { Path() }
            val fillPath = remember { Path() }
            val userPathMeasure = remember { PathMeasure() }
            val partnerPathMeasure = remember { PathMeasure() }

            val deltaGradientBrush = remember {
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF00F5D4).copy(alpha = 0.08f),
                        Color(0xFFFF3366).copy(alpha = 0.08f)
                    )
                )
            }

            val userFillBrush = remember(isDark, lineColor) {
                Brush.verticalGradient(
                    colors = listOf(
                        lineColor.copy(alpha = if (isDark) 0.22f else 0.28f),
                        lineColor.copy(alpha = 0.01f)
                    )
                )
            }

            val userStrokeGradientBrush = remember(isDark, lineColor) {
                Brush.horizontalGradient(
                    colors = listOf(
                        if (isDark) VitalsGradientStartDark else VitalsGradientStartLight,
                        lineColor,
                        if (isDark) VitalsGradientEndDark else VitalsGradientEndLight
                    )
                )
            }

            Canvas(modifier = Modifier.fillMaxSize()) {
                val totalWidth = size.width
                val totalHeight = size.height

                val yAxisPx = yAxisWidthDp.toPx()
                val plotWidth = (totalWidth - yAxisPx).coerceAtLeast(1f)

                // Top & Bottom Breathing Room
                val topPadding = 10.dp.toPx()
                val bottomPadding = 10.dp.toPx()
                val plotHeight = (totalHeight - topPadding - bottomPadding).coerceAtLeast(1f)

                // Scale computation
                val span = (maxDataValue - minDataValue).coerceAtLeast(0f)
                val pad = if (span == 0f) 12f else (span * 0.15f).coerceAtLeast(4f)
                val minVal = (minDataValue - pad).coerceAtLeast(0f)
                val maxVal = maxDataValue + pad
                val range = (maxVal - minVal).coerceAtLeast(1f)

                // Subtle Reference Gridlines & Y-Axis Labels
                val gridLinesCount = 3
                val gridColor = if (isDark) Color(0xFF26324A).copy(alpha = 0.55f) else Color(0xFFE2E6EE)
                val dashEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 6.dp.toPx()), 0f)

                for (i in 0..gridLinesCount) {
                    val fraction = i.toFloat() / gridLinesCount
                    val y = topPadding + fraction * plotHeight

                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(plotWidth, y),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = dashEffect
                    )

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

                val currentDrawProgress = revealProgress.value.coerceIn(0f, 1f)

                // --- DATA RENDERING: USER SERIES ---
                if (data.size == 1) {
                    val singlePoint = data[0]
                    val y = topPadding + (1f - (singlePoint.value - minVal) / range) * plotHeight
                    val centerX = plotWidth / 2f

                    drawLine(
                        color = lineColor.copy(alpha = 0.35f),
                        start = Offset(0f, y),
                        end = Offset(plotWidth, y),
                        strokeWidth = (strokeWidth * 0.75f).toPx(),
                        pathEffect = dashEffect
                    )

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
                } else {
                    val stepX = plotWidth / (data.size - 1)
                    val points = data.mapIndexed { index, point ->
                        val x = index * stepX
                        val y = topPadding + (1f - (point.value - minVal) / range) * plotHeight
                        Offset(x, y)
                    }

                    // User Cubic Spline
                    userFullPath.rewind()
                    points.forEachIndexed { index, point ->
                        if (index == 0) {
                            userFullPath.moveTo(point.x, point.y)
                        } else {
                            val prev = points[index - 1]
                            val cx = (prev.x + point.x) / 2f
                            userFullPath.cubicTo(cx, prev.y, cx, point.y, point.x, point.y)
                        }
                    }

                    userPathMeasure.setPath(userFullPath, false)
                    val totalLength = userPathMeasure.length
                    val currentLength = totalLength * currentDrawProgress

                    userAnimatedPath.rewind()
                    if (currentLength > 0f) {
                        userPathMeasure.getSegment(0f, currentLength, userAnimatedPath, true)
                    }

                    // --- OPTIONAL PARTNER SERIES (Bilateral Telemetry Overlaid Spline) ---
                    var partnerPoints: List<Offset>? = null
                    partnerAnimatedPath.rewind()
                    if (partnerData != null && partnerData.size > 1) {
                        val pStepX = plotWidth / (partnerData.size - 1)
                        val pPoints = partnerData.mapIndexed { index, point ->
                            val x = index * pStepX
                            val y = topPadding + (1f - (point.value - minVal) / range) * plotHeight
                            Offset(x, y)
                        }
                        partnerPoints = pPoints

                        partnerFullPath.rewind()
                        pPoints.forEachIndexed { index, point ->
                            if (index == 0) {
                                partnerFullPath.moveTo(point.x, point.y)
                            } else {
                                val prev = pPoints[index - 1]
                                val cx = (prev.x + point.x) / 2f
                                partnerFullPath.cubicTo(cx, prev.y, cx, point.y, point.x, point.y)
                            }
                        }

                        partnerPathMeasure.setPath(partnerFullPath, false)
                        val pTotalLength = partnerPathMeasure.length
                        val pCurrentLength = pTotalLength * currentDrawProgress
                        if (pCurrentLength > 0f) {
                            partnerPathMeasure.getSegment(0f, pCurrentLength, partnerAnimatedPath, true)
                        }

                        // --- DELTA REGION SHADING BETWEEN USER & PARTNER (DTV-01 / Q8) ---
                        if (currentLength > 0f && pCurrentLength > 0f && points.size == pPoints.size) {
                            deltaPolygon.rewind()
                            deltaPolygon.addPath(userAnimatedPath)
                            for (i in points.size - 1 downTo 0) {
                                val progressRatio = (i.toFloat() / (points.size - 1)).coerceIn(0f, 1f)
                                if (progressRatio <= currentDrawProgress) {
                                    deltaPolygon.lineTo(pPoints[i].x, pPoints[i].y)
                                }
                            }
                            deltaPolygon.close()
                            drawPath(
                                path = deltaPolygon,
                                brush = deltaGradientBrush
                            )
                        }
                    }

                    // User Ambient Gradient Area Fill
                    if (currentLength > 0f && partnerData == null) {
                        fillPath.rewind()
                        fillPath.addPath(userAnimatedPath)
                        val tipPos = userPathMeasure.getPosition(currentLength)
                        fillPath.lineTo(tipPos.x, topPadding + plotHeight)
                        fillPath.lineTo(points.first().x, topPadding + plotHeight)
                        fillPath.close()
                        drawPath(
                            path = fillPath,
                            brush = userFillBrush
                        )
                    }

                    // Draw Partner Curve (Dashed Vibrant Coral Stroke)
                    if (partnerPoints != null && currentLength > 0f) {
                        drawPath(
                            path = partnerAnimatedPath,
                            color = partnerLineColor,
                            style = Stroke(
                                width = (strokeWidth * 1.15f).toPx(),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round,
                                pathEffect = PathEffect.dashPathEffect(
                                    floatArrayOf(10.dp.toPx(), 5.dp.toPx()),
                                    0f
                                )
                            )
                        )
                    }

                    // Draw User Curve (Solid Electric Cyan Stroke)
                    if (currentLength > 0f) {
                        drawPath(
                            path = userAnimatedPath,
                            brush = userStrokeGradientBrush,
                            style = Stroke(
                                width = (strokeWidth * 1.35f).toPx(),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }

                    // Peak and Minimum Pinpoint Indicators
                    data.forEachIndexed { index, point ->
                        val offset = points[index]
                        val isHigh = point == userHighestPoint
                        val isLow = point == userLowestPoint && userHighestPoint?.value != userLowestPoint.value
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

                    // --- SCRUBBER INDICATORS WITH FLUID SPRING KINEMATICS (ANM-06) ---
                    scrubbedIndex?.let { index ->
                        val activePoint = points[index]
                        val targetX = activePoint.x

                        // Hairline Guideline
                        drawLine(
                            color = if (isDark) Color.White.copy(alpha = 0.75f) else Color.Black.copy(alpha = 0.45f),
                            start = Offset(targetX, topPadding),
                            end = Offset(targetX, topPadding + plotHeight),
                            strokeWidth = 1.dp.toPx()
                        )

                        // Focused User Target Dot
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

                        // Focused Partner Target Dot (If present)
                        partnerPoints?.getOrNull(index)?.let { pPoint ->
                            drawCircle(
                                color = partnerLineColor.copy(alpha = 0.35f),
                                radius = 7.dp.toPx(),
                                center = pPoint
                            )
                            drawCircle(
                                color = partnerLineColor,
                                radius = 4.dp.toPx(),
                                center = pPoint
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 1.8.dp.toPx(),
                                center = pPoint
                            )
                        }
                    }
                }
            }

            // Floating Apple-Style Pill Callout with Kinematic Spring Motion (ANM-06)
            scrubbedIndex?.let { index ->
                val pt = data[index]
                val partnerPt = partnerData?.getOrNull(index)

                val yAxisPx = with(density) { yAxisWidthDp.toPx() }
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp)
                ) {
                    val availableWidth = constraints.maxWidth.toFloat()
                    val plotWidth = (availableWidth - yAxisPx).coerceAtLeast(1f)
                    val ptX = if (data.size > 1) {
                        index * (plotWidth / (data.size - 1))
                    } else {
                        plotWidth / 2f
                    }

                    // Spring animation towards target index X position
                    LaunchedEffect(ptX) {
                        if (reduceMotion) {
                            animatedHairlineX.snapTo(ptX)
                        } else {
                            animatedHairlineX.animateTo(
                                targetValue = ptX,
                                animationSpec = spring(
                                    dampingRatio = 0.85f,
                                    stiffness = 600f
                                )
                            )
                        }
                    }

                    val pillWidthPx = with(density) { if (partnerPt != null) 165.dp.toPx() else 110.dp.toPx() }
                    val currentX = if (reduceMotion) ptX else animatedHairlineX.value
                    val clampedOffset = (currentX - pillWidthPx / 2f)
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
                            .padding(horizontal = AppSpacing.sm, vertical = 4.dp)
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

                            // User Reading
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(lineColor)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${pt.value.toInt()}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = lineColor,
                                fontSize = 11.sp
                            )

                            // Partner Reading (If Present)
                            if (partnerPt != null) {
                                Text(
                                    text = "  vs  ",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    fontSize = 10.sp
                                )
                                Box(
                                    modifier = Modifier
                                        .size(5.dp)
                                        .clip(CircleShape)
                                        .background(partnerLineColor)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "${partnerPt.value.toInt()}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = partnerLineColor,
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
