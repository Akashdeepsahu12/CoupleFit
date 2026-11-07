package com.couplefit.ui.dashboard.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.couplefit.ui.components.MotionArcProgressGauge
import com.couplefit.ui.components.NumberPopInText
import com.couplefit.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * High-performance Glassmorphic StatCard with Staggered Entrance and Micro-interactions (KRP-02).
 */
@Composable
fun StatCard(
    title: String,
    subtitle: String = "",
    stepsProgress: Float,
    exerciseProgress: Float,
    stepsCount: Long? = null,
    activeMinutesCount: Long? = null,
    caloriesCount: Long? = null,
    onSyncClick: (() -> Unit)? = null,
    onAddSampleStepsClick: (() -> Unit)? = null,
    animTrigger: Any? = null,
    isExplicitRefresh: Boolean = true,
    reduceMotion: Boolean = false,
    staggerIndex: Int = 0,
    isPartner: Boolean = false,
    modifier: Modifier = Modifier
) {
    // Staggered slide-up entrance: each card arrives slightly after the previous
    val enterOffsetY = remember { Animatable(if (reduceMotion) 0f else 80f) }
    val enterAlpha  = remember { Animatable(if (reduceMotion) 1f else 0f) }
    LaunchedEffect(animTrigger) {
        val delayMs = (staggerIndex * 80L).coerceAtMost(320L)
        delay(delayMs)
        launch {
            enterOffsetY.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        }
        launch {
            enterAlpha.animateTo(
                targetValue = 1f,
                animationSpec = spring(stiffness = Spring.StiffnessLow)
            )
        }
    }
    var showMenu by remember { mutableStateOf(false) }
    var menuOffset by remember { mutableStateOf(DpOffset.Zero) }
    val haptic = LocalHapticFeedback.current

    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (!reduceMotion && isPressed) 0.95f else 1f,
        animationSpec = if (reduceMotion) snap() else spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "cardScale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (!reduceMotion && isPressed) 0.85f else 1f,
        animationSpec = if (reduceMotion) snap() else spring(dampingRatio = 1f, stiffness = 500f),
        label = "cardAlpha"
    )

    // Partial UI Update: Animate only the numeric value itself
    val animStepsCount by animateIntAsState(
        targetValue = (stepsCount ?: (stepsProgress * 10000).toLong()).toInt(),
        animationSpec = tween(durationMillis = 400),
        label = "stepsCount"
    )

    val animActiveMinsCount by animateIntAsState(
        targetValue = (activeMinutesCount ?: (exerciseProgress * 60).toLong()).toInt(),
        animationSpec = tween(durationMillis = 400),
        label = "activeMinsCount"
    )

    val animCaloriesCount by animateIntAsState(
        targetValue = (caloriesCount ?: ((stepsProgress * 0.75f) * 600).toLong()).toInt(),
        animationSpec = tween(durationMillis = 400),
        label = "caloriesCount"
    )

    Card(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha * enterAlpha.value
                translationY = enterOffsetY.value
            }
            .iosGlass(shape = MaterialTheme.shapes.large)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onLongPress = { offset ->
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        menuOffset = DpOffset(offset.x.toDp(), offset.y.toDp())
                        showMenu = true
                    }
                )
            },
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        border = null
    ) {
        Box {
            Column(
                modifier = Modifier.fillMaxSize().padding(AppSpacing.md),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                if (subtitle.isNotEmpty()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = AppSpacing.xxs),
                        maxLines = 1
                    )
                }
                Spacer(modifier = Modifier.height(AppSpacing.xs))
                MotionArcProgressGauge(
                    progress = stepsProgress,
                    currentValue = stepsCount ?: (stepsProgress * 10000).toLong(),
                    goalValue = 10000L,
                    distanceKm = (animStepsCount * 0.000762).coerceAtLeast(0.0),
                    calories = animCaloriesCount.toLong(),
                    activeMinutes = animActiveMinsCount.toLong(),
                    gradientColors = if (isPartner) listOf(MotionPartnerStart, MotionPartnerMid, MotionPartnerEnd) else listOf(MotionCyanStart, MotionSkyMid, MotionBlueEnd),
                    glowColor = if (isPartner) MotionPartnerGlow else MotionGlowCyan,
                    animTrigger = animTrigger,
                    reduceMotion = reduceMotion
                )
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                offset = menuOffset
            ) {
                if (onSyncClick != null) {
                    DropdownMenuItem(
                        text = { Text("Sync with Health Connect") },
                        onClick = {
                            showMenu = false
                            onSyncClick()
                        }
                    )
                }
                if (onAddSampleStepsClick != null) {
                    DropdownMenuItem(
                        text = { Text("Add 1,000 Test Steps") },
                        onClick = {
                            showMenu = false
                            onAddSampleStepsClick()
                        }
                    )
                }
                DropdownMenuItem(
                    text = { Text("Share with Partner") },
                    onClick = { showMenu = false }
                )
            }
        }
    }
}

/**
 * Compact Pill indicator displaying a single metric glyph, value, and label.
 */
@Composable
fun MetricPill(
    label: String,
    value: String,
    color: Color,
    animTrigger: Any? = null,
    reduceMotion: Boolean = false,
    glyph: String? = null
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (glyph != null) {
                Text(
                    text = glyph,
                    style = MaterialTheme.typography.titleSmall
                )
            }
            NumberPopInText(
                text = value,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = color
                ),
                animTrigger = animTrigger,
                reduceMotion = reduceMotion
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * ANM-12: Living Kinetic Orbital Telemetry Sync Pulse.
 * Renders rotating cyan/amber orbital particle ring with expanding radar ripples during active
 * telemetry sync, settling into a calm breathing green pulse when synchronized.
 */
@Composable
fun OrbitalSyncPulse(
    isSyncing: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orbitalSync")

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbitalRotation"
    )

    val rippleProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbitalRipple"
    )

    val idleBreath by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "idleBreath"
    )

    Box(
        modifier = modifier.size(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerOffset = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = 5.dp.toPx()

            if (isSyncing) {
                // Expanding radar pulse wave
                val waveRadius = baseRadius + rippleProgress * 6.dp.toPx()
                val waveAlpha = (1f - rippleProgress).coerceIn(0f, 1f) * 0.7f
                drawCircle(
                    color = CoupleFitIdentityTokens.UserPrimary.copy(alpha = waveAlpha),
                    radius = waveRadius,
                    center = centerOffset,
                    style = Stroke(width = 1.5.dp.toPx())
                )

                // Central glowing nexus
                drawCircle(
                    color = CoupleFitIdentityTokens.SynchronyGold.copy(alpha = 0.9f),
                    radius = 3.dp.toPx(),
                    center = centerOffset
                )

                // Dual orbital particles: User Cyan (top) and Partner Coral (bottom)
                val orbitRadius = 7.5.dp.toPx()
                val rad = Math.toRadians(rotation.toDouble())
                val cyanX = (centerOffset.x + orbitRadius * Math.cos(rad)).toFloat()
                val cyanY = (centerOffset.y + orbitRadius * Math.sin(rad)).toFloat()
                val coralX = (centerOffset.x - orbitRadius * Math.cos(rad)).toFloat()
                val coralY = (centerOffset.y - orbitRadius * Math.sin(rad)).toFloat()

                // Cyan user orbital particle
                drawCircle(
                    color = CoupleFitIdentityTokens.UserPrimary,
                    radius = 2.2.dp.toPx(),
                    center = Offset(cyanX, cyanY)
                )

                // Coral partner orbital particle
                drawCircle(
                    color = CoupleFitIdentityTokens.PartnerPrimary,
                    radius = 2.2.dp.toPx(),
                    center = Offset(coralX, coralY)
                )
            } else {
                // Steady breathing green synchrony dot
                drawCircle(
                    color = SystemGreen.copy(alpha = idleBreath * 0.35f),
                    radius = 5.5.dp.toPx(),
                    center = centerOffset
                )
                drawCircle(
                    color = SystemGreen.copy(alpha = idleBreath),
                    radius = 3.2.dp.toPx(),
                    center = centerOffset
                )
            }
        }
    }
}
