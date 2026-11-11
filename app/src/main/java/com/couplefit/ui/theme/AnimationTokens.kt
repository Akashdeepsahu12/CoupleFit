package com.couplefit.ui.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring

/**
 * Standardized Spring Physics Tokens for CoupleFit.
 * Establishes a cohesive motion vocabulary across the application (AUD-P1-10, MOT-01).
 */
object AnimationTokens {
    /**
     * Smooth liquid physical momentum (e.g. ActivityRing, gauges, telemetry curves).
     * Damping: 0.85f (critically damped settle, zero jarring bounce).
     * Stiffness: 160f (fluid Apple Fitness-grade sweep response ≈ 0.4s).
     */
    val SpringPhysical: SpringSpec<Float> = spring(
        dampingRatio = 0.85f,
        stiffness = 160f
    )

    /**
     * Responsive tactile UI feedback (e.g. heartbeat pulses, nudge taps, card presses).
     * Damping: 0.68f (subtle playful spring rebound).
     * Stiffness: Spring.StiffnessMedium (400f).
     */
    val SpringResponsive: SpringSpec<Float> = spring(
        dampingRatio = 0.68f,
        stiffness = Spring.StiffnessMedium
    )

    /**
     * High-speed kinetic navigation and dock morphing (e.g. active tab capsule, drawer).
     * Damping: Spring.DampingRatioMediumBouncy (0.7f).
     * Stiffness: 420f.
     */
    val SpringNavigation: SpringSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = 420f
    )
}
