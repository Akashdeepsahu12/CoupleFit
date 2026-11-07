package com.couplefit.ui.dashboard.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.couplefit.ui.components.ConfettiParticleExplosion
import com.couplefit.ui.components.FloatingHeartCheerCluster
import com.couplefit.ui.components.MotionArcProgressGauge
import com.couplefit.ui.components.NumberPopInText
import com.couplefit.ui.healthconnect.HealthDashboardUiState
import com.couplefit.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Side-by-Side Bilateral Activity Card (FLOW-02, FLOW-03, POP-01, ANM-05).
 * Features bilateral user/partner activity gauges, direct double-tap cheers,
 * telemetry status pill with diagnostic link, and synchronized CoupleRingClosureHarmonicBurst.
 */
@Composable
fun SideBySideActivityCard(
    healthData: HealthDashboardUiState,
    reduceMotion: Boolean,
    modifier: Modifier = Modifier,
    animTrigger: Any? = null,
    onSendCheer: (String) -> Unit = {},
    onPingPartner: () -> Unit = {}
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var showDiagnosticDialog by remember { mutableStateOf(false) }
    var cheerCooldownUntil by remember { mutableLongStateOf(0L) }
    var cheerTriggerCount by remember { mutableIntStateOf(0) }
    var showCheerCelebration by remember { mutableStateOf(false) }
    var userClosureCelebrated by remember { mutableStateOf(false) }
    var partnerClosureCelebrated by remember { mutableStateOf(false) }
    var bothClosureCelebrated by remember { mutableStateOf(false) }

    val bothClosed = healthData.stepsProgress >= 1f && healthData.partnerStepsProgress >= 1f

    // User solo ring closure
    LaunchedEffect(healthData.stepsProgress) {
        if (healthData.stepsProgress >= 1f && !userClosureCelebrated) {
            userClosureCelebrated = true
            CoupleFitHaptics.performHeartbeat(context, haptic)
        }
    }

    // Partner solo ring closure
    LaunchedEffect(healthData.partnerStepsProgress) {
        if (healthData.partnerStepsProgress >= 1f && !partnerClosureCelebrated) {
            partnerClosureCelebrated = true
            CoupleFitHaptics.performHeartbeat(context, haptic)
        }
    }

    // Q11 / ANM-05: Couple Synchronized Ring Closure Harmonic Burst
    LaunchedEffect(bothClosed) {
        if (bothClosed && !bothClosureCelebrated) {
            bothClosureCelebrated = true
            CoupleFitHaptics.performCelebration(context, haptic)
        }
    }

    val isStale = remember(healthData.partnerLastUpdated) {
        val ts = healthData.partnerLastUpdated
        if (ts == null || ts == 0L) true
        else (System.currentTimeMillis() - ts) > 15 * 60 * 1000L // Stale if older than 15 mins
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .iosGlass(shape = MaterialTheme.shapes.large),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = if (bothClosed) BorderStroke(1.5.dp, Brush.horizontalGradient(
            listOf(
                CoupleFitIdentityTokens.SynchronyGold.copy(alpha = 0.6f),
                Color(0xFFFFA500).copy(alpha = 0.8f),
                CoupleFitIdentityTokens.SynchronyGold.copy(alpha = 0.6f)
            )
        )) else null
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.large)
        ) {
            // Contained subtle aurora mesh strictly within Activity Gauges box boundary (CPX-03)
            AuroraTelemetryMesh(
                modifier = Modifier
                    .matchParentSize()
                    .clip(MaterialTheme.shapes.large),
                userGlowColor = if (bothClosed) CoupleFitIdentityTokens.SynchronyGold.copy(alpha = 0.12f) else MotionGlowCyan.copy(alpha = 0.05f),
                partnerGlowColor = if (bothClosed) Color(0xFFFFA500).copy(alpha = 0.12f) else CoupleFitIdentityTokens.PartnerGlow.copy(alpha = 0.05f)
            )

            // Q11 / ANM-05: Golden Luminescence Bloom when both rings closed
            if (bothClosed) {
                GoldenLuminescenceBloom(
                    modifier = Modifier.matchParentSize(),
                    reduceMotion = reduceMotion
                )
            }

            Column(modifier = Modifier.padding(AppSpacing.md)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ACTIVITY GAUGES",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (bothClosed) CoupleFitIdentityTokens.SynchronyGold else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    // Interactive Telemetry Status Pill (FLOW-03, Two-Material tactile control)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                color = if (isStale) SystemOrange.copy(alpha = 0.18f) else SystemGreen.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .border(
                                width = 0.5.dp,
                                if (isStale) SystemOrange.copy(alpha = 0.25f) else SystemGreen.copy(alpha = 0.25f),
                                RoundedCornerShape(12.dp)
                            )
                            .iosClickable(
                                onClickLabel = "View Telemetry Diagnostics",
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    showDiagnosticDialog = true
                                }
                            )
                    ) {
                        Text(
                            text = if (isStale) "🟡 ${healthData.partnerLastUpdatedLabel} • Cached ℹ️"
                            else "🟢 ${healthData.partnerLastUpdatedLabel} • Live ⚡",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = if (isStale) SystemOrange else SystemGreen
                        )
                    }
                }

                // Q11: Celebratory Banner Payoff when both rings closed
                AnimatedVisibility(
                    visible = bothClosed,
                    enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { -it / 2 }
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = CoupleFitIdentityTokens.SynchronyGold.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, CoupleFitIdentityTokens.SynchronyGold.copy(alpha = 0.45f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = AppSpacing.xs, bottom = AppSpacing.xs)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "💍 Couple Rings Closed Today! ✨",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.4.sp
                                ),
                                color = CoupleFitIdentityTokens.SynchronyGold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(AppSpacing.sm))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // User Column (Dynamic User Name - CPX-06)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (healthData.userName.isNotBlank() && healthData.userName != "You") healthData.userName else "You",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (bothClosed) CoupleFitIdentityTokens.SynchronyGold else MotionCyanStart
                        )
                        Spacer(modifier = Modifier.height(AppSpacing.xs))
                        Box(contentAlignment = Alignment.Center) {
                            MotionArcProgressGauge(
                                progress = healthData.stepsProgress,
                                currentValue = healthData.steps,
                                compact = true,
                                gradientColors = if (bothClosed) listOf(CoupleFitIdentityTokens.SynchronyGold, Color(0xFFFFA500), MotionCyanStart)
                                else listOf(MotionCyanStart, MotionSkyMid, MotionBlueEnd),
                                glowColor = if (bothClosed) CoupleFitIdentityTokens.SynchronyGold else MotionGlowCyan,
                                animTrigger = animTrigger,
                                reduceMotion = reduceMotion
                            )
                            ConfettiParticleExplosion(
                                visible = userClosureCelebrated,
                                modifier = Modifier.size(160.dp),
                                radius = 70.dp,
                                reduceMotion = reduceMotion
                            )
                        }
                        Spacer(modifier = Modifier.height(AppSpacing.xs))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "⏱️ ",
                                style = MaterialTheme.typography.labelSmall
                            )
                            NumberPopInText(
                                text = "${healthData.activeMinutes}m",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                animTrigger = animTrigger,
                                reduceMotion = reduceMotion
                            )
                            Text(
                                text = "  •  🔥 ",
                                style = MaterialTheme.typography.labelSmall
                            )
                            NumberPopInText(
                                text = "${healthData.caloriesBurned} kcal",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                animTrigger = animTrigger,
                                reduceMotion = reduceMotion
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .height(160.dp)
                            .width(1.dp)
                            .background(
                                if (bothClosed) CoupleFitIdentityTokens.SynchronyGold.copy(alpha = 0.5f)
                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                            )
                    )

                    // Partner Column (Dynamic Partner Name - CPX-06, Symmetrical Baseline Alignment - CPX-05)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (healthData.partnerName.isNotBlank()) healthData.partnerName else "Partner",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (bothClosed) Color(0xFFFFA500) else MotionPartnerStart
                        )
                        Spacer(modifier = Modifier.height(AppSpacing.xs))

                        // Direct manipulation double-tap target on partner ring
                        Box(
                            modifier = Modifier
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onDoubleTap = {
                                            val now = System.currentTimeMillis()
                                            if (now > cheerCooldownUntil) {
                                                cheerCooldownUntil = now + 3000L // 3s cooldown limiter
                                                cheerTriggerCount++
                                                showCheerCelebration = true
                                                CoupleFitHaptics.performHeartbeat(context, haptic)
                                                onSendCheer("Cheered your steps! 🔥❤️")
                                            }
                                        }
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            MotionArcProgressGauge(
                                progress = healthData.partnerStepsProgress,
                                currentValue = healthData.partnerSteps,
                                compact = true,
                                gradientColors = if (bothClosed) listOf(Color(0xFFFFA500), CoupleFitIdentityTokens.SynchronyGold, MotionPartnerStart)
                                else listOf(MotionPartnerStart, MotionPartnerMid, MotionPartnerEnd),
                                glowColor = if (bothClosed) Color(0xFFFFA500) else MotionPartnerGlow,
                                animTrigger = animTrigger,
                                reduceMotion = reduceMotion
                            )

                            // High-performance Floating Heart & Flame Cluster (FLOW-02, DSN-02)
                            FloatingHeartCheerCluster(
                                visible = showCheerCelebration,
                                modifier = Modifier.size(140.dp),
                                reduceMotion = reduceMotion,
                                onAnimationEnd = { showCheerCelebration = false }
                            )

                            // 100% Daily Ring Closure Confetti (DSN-02, MIC-02)
                            ConfettiParticleExplosion(
                                visible = partnerClosureCelebrated,
                                modifier = Modifier.size(160.dp),
                                radius = 70.dp,
                                reduceMotion = reduceMotion
                            )
                        }

                        Spacer(modifier = Modifier.height(AppSpacing.xs))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "⏱️ ",
                                style = MaterialTheme.typography.labelSmall
                            )
                            NumberPopInText(
                                text = "${healthData.partnerActiveMinutes}m",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                animTrigger = animTrigger,
                                reduceMotion = reduceMotion
                            )
                            Text(
                                text = "  •  🔥 ",
                                style = MaterialTheme.typography.labelSmall
                            )
                            NumberPopInText(
                                text = "${healthData.partnerCaloriesBurned} kcal",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                animTrigger = animTrigger,
                                reduceMotion = reduceMotion
                            )
                        }
                    }
                }
            }

            // Q11 / ANM-05: Synchronized 56-Particle Dual Ring Cascade Overlay
            CoupleRingClosureHarmonicBurst(
                visible = bothClosureCelebrated,
                modifier = Modifier.matchParentSize(),
                reduceMotion = reduceMotion
            )
        }
    }

    // Telemetry Diagnostic Dialog (FLOW-03)
    if (showDiagnosticDialog) {
        TelemetryDiagnosticDialog(
            partnerName = healthData.partnerName,
            lastUpdatedLabel = healthData.partnerLastUpdatedLabel,
            isStale = isStale,
            onDismiss = { showDiagnosticDialog = false },
            onPingPartner = onPingPartner
        )
    }
}

/**
 * Ambient Golden Luminescence Bloom for Synchronized Ring Closures (Q11 / ANM-05).
 */
@Composable
private fun GoldenLuminescenceBloom(
    modifier: Modifier = Modifier,
    reduceMotion: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "goldenBloom")
    val bloomAlpha by infiniteTransition.animateFloat(
        initialValue = 0.08f,
        targetValue = if (reduceMotion) 0.08f else 0.22f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bloomAlpha"
    )

    Box(
        modifier = modifier
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        CoupleFitIdentityTokens.SynchronyGold.copy(alpha = bloomAlpha),
                        Color(0xFFFFA500).copy(alpha = bloomAlpha * 0.5f),
                        Color.Transparent
                    )
                )
            )
    )
}

/**
 * Synchronized Harmonic 56-Particle Dual Cascade (Q11 / ANM-05).
 * Emits 56 particles harmoniously originating across both user and partner rings.
 */
@Composable
fun CoupleRingClosureHarmonicBurst(
    visible: Boolean,
    modifier: Modifier = Modifier,
    reduceMotion: Boolean = false,
    particleCount: Int = 56,
    onAnimationEnd: () -> Unit = {}
) {
    if (!visible) return

    val progress = remember { Animatable(0f) }
    val density = LocalDensity.current

    val particles = remember {
        val colors = listOf(
            CoupleFitIdentityTokens.SynchronyGold,
            Color(0xFFFFA500),
            CoupleFitIdentityTokens.UserPrimary,
            CoupleFitIdentityTokens.PartnerPrimary,
            SystemGreen,
            Color.White
        )
        val random = Random(777)
        List(particleCount) { index ->
            val isUserSide = index % 2 == 0
            val angle = if (isUserSide) {
                // Bias upwards and left/center
                -Math.PI / 2.0 + (random.nextDouble() - 0.5) * Math.PI * 1.2
            } else {
                // Bias upwards and right/center
                -Math.PI / 2.0 + (random.nextDouble() - 0.5) * Math.PI * 1.2
            }
            val speed = 0.5f + random.nextFloat() * 0.7f
            val color = colors[index % colors.size]
            val size = 5f + random.nextFloat() * 7f
            HarmonicParticle(
                isUserSide = isUserSide,
                angleRad = angle,
                speed = speed,
                color = color,
                size = size,
                isCircle = index % 3 != 0,
                rotationSpeed = (random.nextFloat() - 0.5f) * 400f
            )
        }
    }

    LaunchedEffect(visible, reduceMotion) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = if (reduceMotion) 800 else 1200,
                easing = FastOutSlowInEasing
            )
        )
        onAnimationEnd()
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (reduceMotion) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val currentAlpha = (1f - progress.value).coerceIn(0f, 1f)
                drawCircle(
                    color = CoupleFitIdentityTokens.SynchronyGold.copy(alpha = currentAlpha * 0.4f),
                    radius = size.minDimension * 0.4f * (0.8f + 0.2f * progress.value),
                    center = center,
                    style = Stroke(width = with(density) { 3.dp.toPx() })
                )
            }
        } else {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val currentProgress = progress.value
                val alpha = (1f - currentProgress * currentProgress).coerceIn(0f, 1f)
                val maxDistance = size.width * 0.45f
                val gravityY = with(density) { 30.dp.toPx() } * currentProgress * currentProgress

                val userCenter = Offset(size.width * 0.25f, size.height * 0.5f)
                val partnerCenter = Offset(size.width * 0.75f, size.height * 0.5f)

                particles.forEach { particle ->
                    val origin = if (particle.isUserSide) userCenter else partnerCenter
                    val dist = maxDistance * particle.speed * currentProgress
                    val posX = origin.x + (dist * cos(particle.angleRad)).toFloat()
                    val posY = origin.y + (dist * sin(particle.angleRad)).toFloat() + gravityY

                    val currentRotation = particle.rotationSpeed * currentProgress

                    rotate(degrees = currentRotation, pivot = Offset(posX, posY)) {
                        if (particle.isCircle) {
                            drawCircle(
                                color = particle.color.copy(alpha = alpha),
                                radius = particle.size / 2f,
                                center = Offset(posX, posY)
                            )
                        } else {
                            drawRect(
                                color = particle.color.copy(alpha = alpha),
                                topLeft = Offset(posX - particle.size / 2f, posY - particle.size / 2f),
                                size = Size(particle.size, particle.size * 1.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class HarmonicParticle(
    val isUserSide: Boolean,
    val angleRad: Double,
    val speed: Float,
    val color: Color,
    val size: Float,
    val isCircle: Boolean,
    val rotationSpeed: Float
)
