package com.couplefit.ui.components

import android.os.Build
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * One digit column. Renders a vertical strip of 0..9 repeated (spins + 1)
 * times, clips to a single cell, fades the top/bottom edges, and applies a
 * vertical motion blur during the spin (API 31+).
 */
@Composable
internal fun ReelDigit(
    targetDigit: Int,
    config: SpinConfig,
    textStyle: TextStyle,
    columnIndex: Int,
    animateOnFirstComposition: Boolean,
    reducedMotion: Boolean,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val cellPx = with(density) { config.cell.toPx() }
    val blurPx = with(density) { config.spinBlur.toPx() }

    // translationY in px; the resting position for `targetDigit` at spin start.
    val translation = remember { Animatable(-targetDigit * cellPx) }
    val blurY = remember { Animatable(0f) }

    // ---- re-spin trigger ----
    var prevDigit by remember { mutableStateOf(targetDigit) }
    var spinKey by remember { mutableStateOf(0) }
    LaunchedEffect(targetDigit) {
        if (prevDigit != targetDigit) {
            spinKey++
            prevDigit = targetDigit
        }
    }

    // ---- animation ----
    var hasPlayedOnce by remember { mutableStateOf(false) }
    LaunchedEffect(spinKey) {
        val targetCells = (config.spins * 10 + targetDigit).toFloat()
        val targetY = -targetCells * cellPx

        val shouldAnimate = (animateOnFirstComposition || hasPlayedOnce) && !reducedMotion
        if (!shouldAnimate) {
            translation.snapTo(targetY)
            blurY.snapTo(0f)
            hasPlayedOnce = true
            return@LaunchedEffect
        }

        // Normalize to current digit so we never visibly jump backwards.
        val currentCell = -translation.value / cellPx
        val currentDigit = ((currentCell.toInt() % 10) + 10) % 10
        translation.snapTo(-currentDigit * cellPx)

        delay(columnIndex * config.staggerMs.toLong())

        coroutineScope {
            launch {
                translation.animateTo(
                    targetValue = targetY,
                    animationSpec = tween(config.durationMs, easing = config.easing),
                )
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                launch {
                    blurY.snapTo(blurPx)
                    blurY.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(config.durationMs, easing = config.easing),
                    )
                }
            }
        }
        hasPlayedOnce = true
    }

    val tabular = textStyle.copy(fontFeatureSettings = "tnum")
    val edgeFade = Brush.verticalGradient(
        0f to Color.Transparent,
        0.22f to Color.Black,
        0.78f to Color.Black,
        1f to Color.Transparent,
    )

    val digitWidth = config.cellWidth ?: config.cell

    Box(
        modifier = modifier
            .height(config.cell)
            .width(digitWidth)
            .graphicsLayer {
                compositingStrategy = CompositingStrategy.Offscreen
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && blurY.value > 0.01f) {
                    renderEffect = android.graphics.RenderEffect
                        .createBlurEffect(0f, blurY.value, android.graphics.Shader.TileMode.DECAL)
                        .asComposeRenderEffect()
                } else {
                    renderEffect = null
                }
            }
            .clipToBounds()
            .drawWithContent {
                drawContent()
                // Soft fade at top/bottom — Compose equivalent of mask-image.
                drawRect(brush = edgeFade, blendMode = BlendMode.DstIn)
            },
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(modifier = Modifier.graphicsLayer { translationY = translation.value }) {
            repeat((config.spins + 1) * 10 + 1) { i ->
                Box(
                    modifier = Modifier
                        .height(config.cell)
                        .width(digitWidth),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = (i % 10).toString(), style = tabular)
                }
            }
        }
    }
}
