package com.couplefit.ui.components

import android.provider.Settings
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight

/**
 * Animated numeric counter that displays [value] with smooth digit slide transitions.
 * Separators (, . - +) remain static while digits transition crisply.
 *
 * Eliminates fragile offscreen alpha masking to ensure 100% reliable rendering
 * across all Android versions, GPUs, and emulators.
 */
@Composable
fun SpinningCounter(
    value: Long,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.displayMedium.copy(
        fontWeight = FontWeight.SemiBold,
    ),
    config: SpinConfig = SpinConfig(),
    animateOnFirstComposition: Boolean = false,
) {
    val reducedMotion = rememberReducedMotionEnabled()
    val formatted = remember(value) { formatWithSeparators(value) }
    val tabularStyle = style.copy(fontFeatureSettings = "tnum")

    Row(
        modifier = modifier
            .semantics { contentDescription = value.toString() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        formatted.forEachIndexed { index, ch ->
            if (ch.isDigit()) {
                val digit = ch - '0'
                if (reducedMotion) {
                    Text(
                        text = ch.toString(),
                        style = tabularStyle,
                        maxLines = 1
                    )
                } else {
                    val animDuration = config.durationMs.coerceIn(250, 750)
                    val staggerDelay = ((formatted.length - 1 - index) * config.staggerMs.coerceIn(20, 60))
                        .coerceAtMost(300)

                    AnimatedContent(
                        targetState = digit,
                        transitionSpec = {
                            if (targetState >= initialState) {
                                (slideInVertically(
                                    animationSpec = tween(
                                        durationMillis = animDuration,
                                        delayMillis = staggerDelay,
                                        easing = FastOutSlowInEasing
                                    )
                                ) { height -> height } + fadeIn(
                                    animationSpec = tween(animDuration, delayMillis = staggerDelay)
                                )).togetherWith(
                                    slideOutVertically(
                                        animationSpec = tween(
                                            durationMillis = animDuration,
                                            delayMillis = staggerDelay,
                                            easing = FastOutSlowInEasing
                                        )
                                    ) { height -> -height } + fadeOut(
                                        animationSpec = tween(animDuration, delayMillis = staggerDelay)
                                    )
                                )
                            } else {
                                (slideInVertically(
                                    animationSpec = tween(
                                        durationMillis = animDuration,
                                        delayMillis = staggerDelay,
                                        easing = FastOutSlowInEasing
                                    )
                                ) { height -> -height } + fadeIn(
                                    animationSpec = tween(animDuration, delayMillis = staggerDelay)
                                )).togetherWith(
                                    slideOutVertically(
                                        animationSpec = tween(
                                            durationMillis = animDuration,
                                            delayMillis = staggerDelay,
                                            easing = FastOutSlowInEasing
                                        )
                                    ) { height -> height } + fadeOut(
                                        animationSpec = tween(animDuration, delayMillis = staggerDelay)
                                    )
                                )
                            }
                        },
                        label = "digit_${formatted.length - index}"
                    ) { d ->
                        Text(
                            text = d.toString(),
                            style = tabularStyle,
                            maxLines = 1
                        )
                    }
                }
            } else {
                Text(
                    text = ch.toString(),
                    style = tabularStyle,
                    maxLines = 1
                )
            }
        }
    }
}

/** 1234567 -> "1,234,567"; -1234 -> "-1,234". */
private fun formatWithSeparators(v: Long): String {
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

/**
 * Reads the system animator scale. Returns true when the user has requested
 * reduced motion (Developer options / Accessibility -> "Remove animations",
 * or animator duration scale == 0).
 */
@Composable
private fun rememberReducedMotionEnabled(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        try {
            Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f,
            ) == 0f
        } catch (_: Throwable) {
            false
        }
    }
}
