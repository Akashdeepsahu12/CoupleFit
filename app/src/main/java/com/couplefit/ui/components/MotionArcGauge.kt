package com.couplefit.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.couplefit.ui.theme.*
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

/**
 * Modern Motion Arc Progress Gauge inspired by the sleek fitness motion graphics in 558.mp4.
 * Features:
 * - 240° horseshoe arc with smooth FastOutSlowIn entrance animation.
 * - Ambient diffuse glow backdrop behind the active arc.
 * - Glowing leading tip needle that dynamically tracks the sweep progress.
 * - High-precision animated integer counter in Neue Helvetica typography.
 * - Dynamic bottom anchor labels ("0" and Goal).
 * - Floating glassmorphism metric pill chips (km, kcal, active min).
 */
@Composable
fun MotionArcProgressGauge(
    progress: Float,
    currentValue: Long,
    modifier: Modifier = Modifier,
    goalValue: Long = 10000L,
    unitLabel: String = "steps",
    distanceKm: Double = (currentValue * 0.000762).coerceAtLeast(0.0),
    calories: Long = (currentValue * 0.04).toLong(),
    activeMinutes: Long = (currentValue / 110).coerceAtLeast(0L),
    gradientColors: List<Color> = listOf(MotionCyanStart, MotionSkyMid, MotionBlueEnd),
    glowColor: Color = MotionGlowCyan,
    trackColor: Color = if (MaterialTheme.colorScheme.background == BackgroundDark) MotionTrackDark else MotionTrackLight,
    arcThickness: Dp = 18.dp,
    compact: Boolean = false,
    animTrigger: Any? = null,
    reduceMotion: Boolean = false,
    headerTitle: String? = null,
    headerSubtitle: String? = null,
    onCardClick: (() -> Unit)? = null
) {
    val clampedProgress = progress.coerceIn(0f, 1.25f)
    val animatedProgress = remember { Animatable(if (reduceMotion) clampedProgress else 0f) }

    LaunchedEffect(clampedProgress, animTrigger, reduceMotion) {
        if (reduceMotion) {
            animatedProgress.snapTo(clampedProgress)
        } else {
            animatedProgress.snapTo(0f)
            animatedProgress.animateTo(
                targetValue = clampedProgress,
                animationSpec = tween(
                    durationMillis = 1400,
                    easing = FastOutSlowInEasing
                )
            )
        }
    }

    // Dynamic numeric counter animation
    val animCount by animateIntAsState(
        targetValue = currentValue.toInt(),
        animationSpec = if (reduceMotion) snap() else tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "gaugeStepCount"
    )

    // Ambient breathing pulse for the leading tip glow
    val infiniteTransition = rememberInfiniteTransition(label = "arcGlowPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    val density = LocalDensity.current
    val gaugeSize = if (compact) 160.dp else 240.dp
    val actualThickness = if (compact) 12.dp else arcThickness

    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onCardClick != null) Modifier.clickable(onClick = onCardClick) else Modifier),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (headerTitle != null) {
            Text(
                text = headerTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            if (headerSubtitle != null) {
                Text(
                    text = headerSubtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                )
            }
        }

        // --- Center Gauge Container ---
        Box(
            modifier = Modifier
                .size(gaugeSize)
                .padding(horizontal = 12.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokePx = with(density) { actualThickness.toPx() }
                val diameter = size.minDimension - strokePx - 20.dp.toPx()
                val radius = diameter / 2f
                val centerOffset = Offset(size.width / 2f, size.height / 2f + (if (compact) 4.dp.toPx() else 8.dp.toPx()))

                // 240° horseshoe arc: starts bottom-left (150°) and sweeps clockwise over top to bottom-right (390° / 30°)
                val startAngle = 150f
                val totalSweep = 240f
                val currentSweep = (totalSweep * animatedProgress.value.coerceIn(0f, 1f))

                val arcTopLeft = Offset(centerOffset.x - radius, centerOffset.y - radius)
                val arcSize = Size(radius * 2f, radius * 2f)

                // 1. Background Track
                drawArc(
                    color = trackColor.copy(alpha = 0.45f),
                    startAngle = startAngle,
                    sweepAngle = totalSweep,
                    useCenter = false,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round),
                    size = arcSize,
                    topLeft = arcTopLeft
                )

                if (currentSweep > 0.1f) {
                    // 2. Ambient Glow Pass (gentle, translucent diffuse stroke kept close to the arc - CPX-03)
                    val glowBrush = Brush.sweepGradient(
                        colors = listOf(
                            glowColor.copy(alpha = 0.12f * pulseAlpha),
                            gradientColors.last().copy(alpha = 0.08f * pulseAlpha)
                        ),
                        center = centerOffset
                    )
                    drawArc(
                        brush = glowBrush,
                        startAngle = startAngle,
                        sweepAngle = currentSweep,
                        useCenter = false,
                        style = Stroke(
                            width = strokePx + with(density) { if (compact) 3.dp.toPx() else 6.dp.toPx() },
                            cap = StrokeCap.Round
                        ),
                        size = arcSize,
                        topLeft = arcTopLeft
                    )

                    // 3. Main Vivid Active Arc
                    val linearProgressBrush = Brush.linearGradient(
                        colors = gradientColors,
                        start = Offset(centerOffset.x - radius, centerOffset.y + radius),
                        end = Offset(centerOffset.x + radius, centerOffset.y)
                    )
                    drawArc(
                        brush = linearProgressBrush,
                        startAngle = startAngle,
                        sweepAngle = currentSweep,
                        useCenter = false,
                        style = Stroke(width = strokePx, cap = StrokeCap.Round),
                        size = arcSize,
                        topLeft = arcTopLeft
                    )

                    // 4. Leading Tip Indicator Needle (Glowing concentric dot at the current tip)
                    val tipAngleDeg = (startAngle + currentSweep) % 360f
                    val tipAngleRad = Math.toRadians(tipAngleDeg.toDouble())
                    val tipX = centerOffset.x + radius * cos(tipAngleRad).toFloat()
                    val tipY = centerOffset.y + radius * sin(tipAngleRad).toFloat()
                    val tipCenter = Offset(tipX, tipY)

                    // Radial glow halo
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                glowColor.copy(alpha = 0.85f * pulseAlpha),
                                glowColor.copy(alpha = 0.2f),
                                Color.Transparent
                            ),
                            center = tipCenter,
                            radius = with(density) { if (compact) 10.dp.toPx() else 16.dp.toPx() }
                        ),
                        radius = with(density) { if (compact) 10.dp.toPx() else 16.dp.toPx() },
                        center = tipCenter
                    )

                    // Crisp white core
                    drawCircle(
                        color = Color.White,
                        radius = with(density) { if (compact) 3.5.dp.toPx() else 5.5.dp.toPx() },
                        center = tipCenter
                    )

                    // Colored boundary accent
                    drawCircle(
                        color = gradientColors.last(),
                        radius = with(density) { if (compact) 5.dp.toPx() else 7.5.dp.toPx() },
                        center = tipCenter,
                        style = Stroke(width = with(density) { 2.dp.toPx() })
                    )
                }
            }

            // --- Center Typography & Motifs ---
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = if (compact) 8.dp else 16.dp)
            ) {
                // Running footsteps icon inside arch
                AnimatedFootstepsIcon(
                    modifier = Modifier.size(if (compact) 18.dp else 24.dp),
                    tint = gradientColors.first(),
                    reduceMotion = reduceMotion
                )

                Spacer(modifier = Modifier.height(if (compact) 2.dp else 6.dp))

                // Big Animated Step Counter (transitions.dev number-pop-in)
                val counterStyle = (if (compact) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.displayMedium).copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    letterSpacing = (-0.5).sp
                )
                NumberPopInCounter(
                    value = currentValue,
                    style = counterStyle,
                    animTrigger = animTrigger,
                    distanceDp = if (compact) 8.dp else 12.dp,
                    blurDp = if (compact) 2.dp else 2.5.dp,
                    reduceMotion = reduceMotion
                )

                // "steps" Label
                Text(
                    text = unitLabel,
                    style = if (compact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )

                if (!compact) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Daily goal: %,d".format(goalValue),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // --- Bottom Endpoint Anchors ("0" and Goal) ---
        Row(
            modifier = Modifier
                .fillMaxWidth(if (compact) 0.85f else 0.72f)
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "0",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "%,d".format(goalValue),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(if (compact) 8.dp else 14.dp))

        // --- Metric Pill Chips Row (km, kcal, active min) ---
        if (!compact) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                MotionPillChip(
                    icon = "⚡",
                    value = "${"%.2f".format(distanceKm)} km",
                    accentColor = gradientColors.first(),
                    animTrigger = animTrigger,
                    reduceMotion = reduceMotion
                )
                MotionPillChip(
                    icon = "🔥",
                    value = "$calories kcal",
                    accentColor = Color(0xFFFF9500),
                    animTrigger = animTrigger,
                    reduceMotion = reduceMotion
                )
                MotionPillChip(
                    icon = "⏱️",
                    value = "${activeMinutes}m",
                    accentColor = Color(0xFF32D74B),
                    animTrigger = animTrigger,
                    reduceMotion = reduceMotion
                )
            }
        }
    }
}

/**
 * Sleek glassmorphism pill metric chip inspired by the floating bottom metrics in 558.mp4.
 */
@Composable
fun MotionPillChip(
    icon: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    animTrigger: Any? = null,
    reduceMotion: Boolean = false
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 14.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = icon,
                fontSize = 13.sp
            )
            NumberPopInText(
                text = value,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                animTrigger = animTrigger,
                reduceMotion = reduceMotion
            )
        }
    }
}

/**
 * 7-Day Weekly Streak Row inspired by the top day selector in 558.mp4.
 * Features animated active day glow and flame indicators.
 */
@Composable
fun WeeklyStreakRow(
    currentDayStreak: Int = 5,
    todaySteps: Long = 8420L,
    goal: Long = 10000L,
    modifier: Modifier = Modifier
) {
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val today = LocalDate.now().dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        days.forEachIndexed { index, day ->
            val isToday = day.equals(today, ignoreCase = true)
            val isCompleted = index < currentDayStreak || (isToday && todaySteps >= goal)

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .then(
                        if (isToday) Modifier
                            .background(MotionCyanStart.copy(alpha = 0.14f), shape = RoundedCornerShape(16.dp))
                            .border(
                                width = 0.5.dp,
                                color = MotionCyanStart.copy(alpha = 0.35f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                        else Modifier.padding(horizontal = 6.dp, vertical = 6.dp)
                    )
            ) {
                Text(
                    text = day,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                    color = if (isToday) MotionCyanStart else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isCompleted) "🔥" else "⚪",
                    fontSize = if (isCompleted) 12.sp else 8.sp,
                    modifier = Modifier.scale(if (isToday) 1.15f else 1f)
                )
            }
        }
    }
}
