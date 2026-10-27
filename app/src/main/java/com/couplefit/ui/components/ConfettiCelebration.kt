package com.couplefit.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.couplefit.ui.theme.CoupleFitIdentityTokens
import com.couplefit.ui.theme.SystemGreen
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class Particle(
    val angleRad: Double,
    val speed: Float,
    val color: Color,
    val size: Float,
    val isCircle: Boolean,
    val rotationSpeed: Float
)

/**
 * High-performance 60fps Canvas Confetti Explosion (DSN-02, MIC-02).
 * Triggers radial celebratory burst upon 100% daily ring closure.
 * Gracefully degrades to a golden specular ring pulse when reduceMotion is enabled.
 */
@Composable
fun ConfettiParticleExplosion(
    visible: Boolean,
    modifier: Modifier = Modifier,
    particleCount: Int = 28,
    radius: Dp = 80.dp,
    reduceMotion: Boolean = false,
    onAnimationEnd: () -> Unit = {}
) {
    if (!visible) return

    val progress = remember { Animatable(0f) }
    val density = LocalDensity.current

    // Deterministic particles per explosion instance
    val particles = remember {
        val colors = listOf(
            CoupleFitIdentityTokens.UserPrimary,
            CoupleFitIdentityTokens.PartnerPrimary,
            CoupleFitIdentityTokens.SynchronyGold,
            SystemGreen,
            Color.White
        )
        val random = Random(42)
        List(particleCount) { index ->
            val angle = (2.0 * Math.PI / particleCount) * index + (random.nextDouble() - 0.5) * 0.4
            val speed = 0.6f + random.nextFloat() * 0.6f
            val color = colors[index % colors.size]
            val size = 6f + random.nextFloat() * 6f
            Particle(
                angleRad = angle,
                speed = speed,
                color = color,
                size = size,
                isCircle = index % 2 == 0,
                rotationSpeed = (random.nextFloat() - 0.5f) * 360f
            )
        }
    }

    LaunchedEffect(visible, reduceMotion) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = if (reduceMotion) 600 else 800,
                easing = if (reduceMotion) LinearEasing else FastOutSlowInEasing
            )
        )
        onAnimationEnd()
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (reduceMotion) {
            // Accessible alternative: graceful golden pulse without moving particles
            Canvas(modifier = Modifier.fillMaxSize()) {
                val currentAlpha = (1f - progress.value).coerceIn(0f, 1f)
                val pulseRadius = (size.minDimension / 2f) * (0.85f + 0.25f * progress.value)
                drawCircle(
                    color = CoupleFitIdentityTokens.SynchronyGold.copy(alpha = currentAlpha * 0.8f),
                    radius = pulseRadius,
                    center = center,
                    style = Stroke(width = with(density) { 3.dp.toPx() })
                )
            }
        } else {
            // Full 60fps radial particle physics
            Canvas(modifier = Modifier.fillMaxSize()) {
                val maxRadiusPx = with(density) { radius.toPx() }
                val currentProgress = progress.value
                val alpha = (1f - currentProgress * currentProgress).coerceIn(0f, 1f)
                val gravityY = with(density) { 35.dp.toPx() } * currentProgress * currentProgress

                particles.forEach { particle ->
                    val distance = maxRadiusPx * particle.speed * currentProgress
                    val posX = center.x + (distance * cos(particle.angleRad)).toFloat()
                    val posY = center.y + (distance * sin(particle.angleRad)).toFloat() + gravityY

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
                                size = Size(particle.size, particle.size * 1.6f)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Floating Heart & Flame Cluster for interpersonal double-tap partner cheers (FLOW-02, DSN-02).
 */
@Composable
fun FloatingHeartCheerCluster(
    visible: Boolean,
    modifier: Modifier = Modifier,
    reduceMotion: Boolean = false,
    onAnimationEnd: () -> Unit = {}
) {
    if (!visible) return

    val progress = remember { Animatable(0f) }

    LaunchedEffect(visible) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1100, easing = FastOutSlowInEasing)
        )
        onAnimationEnd()
    }

    val cheerIcons = listOf("❤️", "💖", "🔥", "✨", "💕")
    val offsets = listOf(
        Pair(-24.dp, -10.dp),
        Pair(18.dp, -18.dp),
        Pair(0.dp, -30.dp),
        Pair(-14.dp, -48.dp),
        Pair(20.dp, -42.dp)
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        val currentProgress = progress.value
        val alpha = (1f - currentProgress).coerceIn(0f, 1f)

        cheerIcons.forEachIndexed { idx, icon ->
            val baseOffset = offsets[idx % offsets.size]
            val upwardTravel = if (reduceMotion) 0.dp else (baseOffset.second - (currentProgress * 40).dp)
            val horizontalDrift = if (reduceMotion) 0.dp else (baseOffset.first * (1f + currentProgress * 0.3f))

            Text(
                text = icon,
                fontSize = (18 + (idx % 3) * 4).sp,
                modifier = Modifier
                    .offset(x = horizontalDrift, y = upwardTravel)
                    .alpha(alpha)
            )
        }
    }
}
