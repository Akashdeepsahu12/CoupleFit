package com.couplefit.ui.components

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Motion tokens for [SpinningCounter].
 *
 * Names and defaults mirror the CSS custom properties exposed by
 * transitions.dev for the "Spinning counter" recipe:
 *
 *   --reel-dur, --reel-cell, --reel-spin-blur, --reel-stagger, --reel-ease
 */
data class SpinConfig(
    val durationMs: Int = 1400,                 // --reel-dur
    val cell: Dp = 30.dp,                       // --reel-cell
    val cellWidth: Dp? = null,                  // optional custom width per digit cell
    val spinBlur: Dp = 3.dp,                    // --reel-spin-blur
    val staggerMs: Int = 90,                    // --reel-stagger
    val spins: Int = 3,                         // full revolutions before landing
    val easing: Easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f), // --reel-ease
)
