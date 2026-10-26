package com.couplefit.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * A custom [NestedScrollConnection] that gives every scrollable container in the app
 * iOS/Lenis-style "rubber-band" over-scroll and a spring snap-back.
 *
 * Behaviour:
 *  1. While the user drags past the top/bottom edge, the content stretches with
 *     a logarithmic resistance factor (feels springy, not locked).
 *  2. When the finger lifts, a [spring] animation snaps the content back to its
 *     natural resting position.
 *  3. Fling velocity is preserved and handed back to the child scroll so the
 *     in-bounds momentum is not eaten.
 */
class LenisNestedScrollConnection(
    private val overScrollOffset: Animatable<Float, AnimationVector1D>,
    private val scope: kotlinx.coroutines.CoroutineScope
) : NestedScrollConnection {

    // Resistance factor: lower = more stretch, 0.38 matches iOS feel closely
    private val resistanceFactor = 0.38f

    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        // If there is already over-scroll in the opposite direction, eat some scroll
        // to restore neutral position before passing to child.
        val delta = available.y
        val currentOffset = overScrollOffset.value
        return if ((delta > 0 && currentOffset < 0) || (delta < 0 && currentOffset > 0)) {
            val consumed = if (abs(delta) > abs(currentOffset)) {
                currentOffset
            } else {
                delta
            }
            scope.launch { overScrollOffset.snapTo(currentOffset - consumed) }
            Offset(0f, consumed)
        } else {
            Offset.Zero
        }
    }

    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource
    ): Offset {
        // Only apply rubber-band when dragging (not flinging), and there is leftover scroll
        if (source == NestedScrollSource.Drag && available.y != 0f) {
            val rawDelta = available.y
            // Logarithmic resistance: stretch shrinks as you pull further
            val resistance = resistanceFactor / (1f + abs(overScrollOffset.value) * 0.008f)
            val resistedDelta = rawDelta * resistance
            scope.launch { overScrollOffset.snapTo(overScrollOffset.value + resistedDelta) }
            return available // consume leftover so child doesn't jitter
        }
        return Offset.Zero
    }

    override suspend fun onPreFling(available: Velocity): Velocity {
        // If the content is currently stretched past bounds, snap back immediately on release
        if (overScrollOffset.value != 0f) {
            overScrollOffset.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
            return Velocity.Zero
        }
        return super.onPreFling(available)
    }

    override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
        // Spring snap back to zero after a fling hits the boundary
        if (overScrollOffset.value != 0f) {
            overScrollOffset.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
        return Velocity.Zero
    }
}

/**
 * Applies iOS/Lenis-style smooth scroll physics to any Composable.
 *
 * Usage: `Modifier.lenisScroll()`
 */
fun Modifier.lenisScroll(): Modifier = composed {
    val scope = rememberCoroutineScope()
    val overScrollOffset = remember { Animatable(0f) }
    val connection = remember { LenisNestedScrollConnection(overScrollOffset, scope) }

    this
        .nestedScroll(connection)
        .offset { IntOffset(0, overScrollOffset.value.roundToInt()) }
}
