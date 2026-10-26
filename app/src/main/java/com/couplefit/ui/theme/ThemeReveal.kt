package com.couplefit.ui.theme

import android.graphics.Bitmap
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlin.math.hypot
import kotlin.math.max

/**
 * State and controller for Telegram-style Circular Reveal Theme Transition.
 */
class ThemeRevealState {
    val isRevealingState: MutableState<Boolean> = mutableStateOf(false)
    val revealOriginState: MutableState<Offset> = mutableStateOf(Offset.Zero)
    val previousBitmapState: MutableState<Bitmap?> = mutableStateOf(null)

    var isRevealing: Boolean
        get() = isRevealingState.value
        set(value) { isRevealingState.value = value }

    var revealOrigin: Offset
        get() = revealOriginState.value
        set(value) { revealOriginState.value = value }

    var previousBitmap: Bitmap?
        get() = previousBitmapState.value
        set(value) { previousBitmapState.value = value }
}

@Composable
fun rememberThemeRevealState(): ThemeRevealState {
    return remember { ThemeRevealState() }
}

/**
 * Container that performs the Telegram-style expanding circular reveal when the theme changes.
 *
 * How it works:
 * 1. An old-theme bitmap snapshot is captured right before switching themes.
 * 2. The underlying tree immediately displays the new theme.
 * 3. The old bitmap is rendered on top inside an inverted circular clip (ClipOp.Difference)
 *    OR the circle expands from `origin`, cutting away the old screenshot to uncover the new theme.
 * 4. Animates outward to the farthest screen corner in ~500ms using FastOutSlowInEasing.
 */
@Composable
fun ThemeCircularRevealContainer(
    revealState: ThemeRevealState,
    reduceMotion: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(revealState.isRevealing) {
        if (revealState.isRevealing) {
            if (reduceMotion) {
                // Respect reduced motion: instantly complete without transition
                progress.snapTo(1f)
            } else {
                // Telegram-style 500ms FastOutSlowIn circular reveal
                progress.snapTo(0f)
                progress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
                )
            }
            // Transition completed: clean up snapshot overlay
            revealState.isRevealing = false
            revealState.previousBitmap = null
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Active Content rendered with current (new) theme
        content()

        // Circular Reveal Overlay with the old screenshot
        val oldBitmap = revealState.previousBitmap
        if (revealState.isRevealing && oldBitmap != null && !oldBitmap.isRecycled) {
            val composeBitmap = remember(oldBitmap) { oldBitmap.asImageBitmap() }
            val currentProgress = progress.value

            Canvas(modifier = Modifier.fillMaxSize()) {
                val screenW = size.width
                val screenH = size.height

                if (reduceMotion) {
                    // Simple opacity crossfade
                    val alpha = (1f - currentProgress).coerceIn(0f, 1f)
                    drawImage(
                        image = composeBitmap,
                        dstOffset = IntOffset.Zero,
                        dstSize = IntSize(screenW.toInt(), screenH.toInt()),
                        alpha = alpha
                    )
                } else {
                    // Calculate maximum distance to the farthest screen corner
                    val origin = if (revealState.revealOrigin == Offset.Zero) {
                        Offset(screenW / 2f, screenH / 2f)
                    } else {
                        revealState.revealOrigin
                    }

                    val maxRadius = max(
                        hypot(origin.x.toDouble(), origin.y.toDouble()),
                        max(
                            hypot((screenW - origin.x).toDouble(), origin.y.toDouble()),
                            max(
                                hypot(origin.x.toDouble(), (screenH - origin.y).toDouble()),
                                hypot((screenW - origin.x).toDouble(), (screenH - origin.y).toDouble())
                            )
                        )
                    ).toFloat()

                    val currentRadius = maxRadius * currentProgress

                    // Clip out the expanding circle from the old snapshot:
                    // As the circle grows, the new theme is exposed beneath it.
                    val circlePath = Path().apply {
                        addOval(
                            androidx.compose.ui.geometry.Rect(
                                center = origin,
                                radius = currentRadius
                            )
                        )
                    }

                    clipPath(path = circlePath, clipOp = ClipOp.Difference) {
                        drawImage(
                            image = composeBitmap,
                            dstOffset = IntOffset.Zero,
                            dstSize = IntSize(screenW.toInt(), screenH.toInt())
                        )
                    }
                }
            }
        }
    }
}

