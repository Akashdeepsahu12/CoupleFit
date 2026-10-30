package com.couplefit.ui.components

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Exact physical cubic-bezier easing curve from transitions.dev:
 * cubic-bezier(0.34, 1.45, 0.64, 1)
 *
 * Implemented with a robust monotonic solver that prevents floating-point no-solution exceptions
 * near t=1.0 while faithfully preserving the 45% overshoot peak and smooth settle.
 */
class SafeCubicBezierEasing(
    private val x1: Float,
    private val y1: Float,
    private val x2: Float,
    private val y2: Float
) : Easing {
    override fun transform(fraction: Float): Float {
        if (fraction <= 0f) return 0f
        if (fraction >= 1f) return 1f

        var low = 0f
        var high = 1f
        var t = fraction

        for (i in 0 until 18) {
            val oneMinusT = 1f - t
            val currentX = 3f * oneMinusT * oneMinusT * t * x1 +
                    3f * oneMinusT * t * t * x2 +
                    t * t * t
            if (currentX < fraction) {
                low = t
            } else {
                high = t
            }
            t = (low + high) * 0.5f
        }

        val oneMinusT = 1f - t
        return 3f * oneMinusT * oneMinusT * t * y1 +
                3f * oneMinusT * t * t * y2 +
                t * t * t
    }
}

val NumberPopInEasing: Easing = SafeCubicBezierEasing(0.34f, 1.45f, 0.64f, 1.0f)

private const val DEFAULT_DURATION_MS = 500
private const val DEFAULT_STAGGER_MS = 70
private val DEFAULT_DISTANCE_DP = 8.dp
private val DEFAULT_BLUR_DP = 2.dp

/**
 * Number Pop-In animated counter component inspired by transitions.dev/detail.html?t=number-pop-in.
 * Formats [value] with thousands commas and pops each character in with independent stagger,
 * overshoot translation, subtle Gaussian motion blur, and opacity fade.
 */
@Composable
fun NumberPopInCounter(
    value: Long,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    prefix: String = "",
    suffix: String = "",
    animTrigger: Any? = null,
    staggerMs: Int = DEFAULT_STAGGER_MS,
    durationMs: Int = DEFAULT_DURATION_MS,
    distanceDp: Dp = DEFAULT_DISTANCE_DP,
    blurDp: Dp = DEFAULT_BLUR_DP,
    reduceMotion: Boolean = false,
) {
    val formatted = remember(value, prefix, suffix) { "$prefix${formatWithCommas(value)}$suffix" }
    NumberPopInText(
        text = formatted,
        modifier = modifier.semantics { contentDescription = "$prefix$value$suffix" },
        style = style,
        color = color,
        animTrigger = animTrigger,
        staggerMs = staggerMs,
        durationMs = durationMs,
        distanceDp = distanceDp,
        blurDp = blurDp,
        reduceMotion = reduceMotion
    )
}

/**
 * Number Pop-In animated text component inspired by transitions.dev/detail.html?t=number-pop-in.
 * Staggers each character's arrival with directional translation, subtle blur, and overshoot easing.
 */
@Composable
fun NumberPopInText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    animTrigger: Any? = null,
    staggerMs: Int = DEFAULT_STAGGER_MS,
    durationMs: Int = DEFAULT_DURATION_MS,
    distanceDp: Dp = DEFAULT_DISTANCE_DP,
    blurDp: Dp = DEFAULT_BLUR_DP,
    reduceMotion: Boolean = false,
) {
    val density = LocalDensity.current
    val distancePx = with(density) { distanceDp.toPx() }
    val blurPx = with(density) { blurDp.toPx() }
    val tabularStyle = remember(style) { style.copy(fontFeatureSettings = "tnum") }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        text.forEachIndexed { index, char ->
            SinglePopInChar(
                char = char,
                index = index,
                style = tabularStyle,
                color = color,
                animTrigger = animTrigger,
                staggerMs = staggerMs,
                durationMs = durationMs,
                distancePx = distancePx,
                blurPx = blurPx,
                reduceMotion = reduceMotion
            )
        }
    }
}

@Composable
private fun SinglePopInChar(
    char: Char,
    index: Int,
    style: TextStyle,
    color: Color,
    animTrigger: Any?,
    staggerMs: Int,
    durationMs: Int,
    distancePx: Float,
    blurPx: Float,
    reduceMotion: Boolean
) {
    if (char == ' ') {
        Text(text = " ", style = style)
        return
    }

    val progress = remember { Animatable(if (reduceMotion) 1f else 0f) }

    LaunchedEffect(animTrigger, char) {
        if (reduceMotion) {
            progress.snapTo(1f)
            return@LaunchedEffect
        }
        progress.snapTo(0f)
        val delayTime = (index * staggerMs.toLong()).coerceAtMost(600L)
        delay(delayTime)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = durationMs,
                easing = NumberPopInEasing
            )
        )
    }

    val t = progress.value
    val offsetY = (1f - t) * distancePx
    val alphaVal = t.coerceIn(0f, 1f)
    val currentBlur = ((1f - t) * blurPx).coerceAtLeast(0f)

    Box(
        modifier = Modifier.graphicsLayer {
            this.translationY = offsetY
            this.alpha = alphaVal
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && currentBlur > 0.3f) {
                renderEffect = RenderEffect.createBlurEffect(
                    currentBlur,
                    currentBlur,
                    Shader.TileMode.CLAMP
                ).asComposeRenderEffect()
            }
        }
    ) {
        Text(
            text = char.toString(),
            style = style,
            color = color,
            maxLines = 1
        )
    }
}

private fun formatWithCommas(v: Long): String {
    val s = v.toString()
    val negative = s.startsWith("-")
    val digits = if (negative) s.substring(1) else s
    val out = StringBuilder()
    digits.reversed().forEachIndexed { i, c ->
        if (i > 0 && i % 3 == 0) out.append(',')
        out.append(c)
    }
    return (if (negative) "-" else "") + out.reverse()
}
