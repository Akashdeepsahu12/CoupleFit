package com.couplefit.ui.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role

/**
 * Replaces the default Android Ripple effect with an iOS-style spring scale and dim micro-animation.
 * When the user presses the component, it scales down slightly and drops opacity.
 *
 * Emil Kowalski Craft Specifications (ENG-01, APL-02, MIC-03):
 * - Pointer-down: Instant mechanical depression with zero lag (stiffness = 1000f, damping = 1.0f).
 * - Release: Snappy mechanical glass-click return without rubbery cartoonish oscillation (stiffness = 750f, damping = 0.82f).
 * - Scale: Subtle 2.5% compression (pressedScale = 0.975f) preventing 18px horizontal layout twitch on full-width cards.
 */
fun Modifier.iosClickable(
    interactionSource: MutableInteractionSource? = null,
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    pressedScale: Float = 0.975f,
    pressedAlpha: Float = 0.88f,
    onClick: () -> Unit
) = composed {
    val actualInteractionSource = interactionSource ?: remember { MutableInteractionSource() }
    val isPressed by actualInteractionSource.collectIsPressedAsState()

    // Asymmetric iOS spring physics: instant mechanical depression on press-down,
    // crisp calibrated settling on release (ENG-01, APL-02, MIC-03)
    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1f,
        animationSpec = if (isPressed) {
            spring(
                dampingRatio = Spring.DampingRatioNoBouncy, // 1.0f: zero lag or wobble on contact
                stiffness = Spring.StiffnessHigh // 1000f: immediate tactile feedback
            )
        } else {
            spring(
                dampingRatio = Spring.DampingRatioNoBouncy, // 1.0f: Apple WWDC critically damped settle (no overshoot)
                stiffness = 800f // Snappy mechanical glass-click return (response ≈ 0.35s)
            )
        },
        label = "iosScale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isPressed) pressedAlpha else 1f,
        animationSpec = spring(
            dampingRatio = 1f, // No bounce for opacity
            stiffness = 600f
        ),
        label = "iosAlpha"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
            this.alpha = alpha
        }
        .clickable(
            interactionSource = actualInteractionSource,
            indication = null, // Disables the default Android Ripple!
            enabled = enabled,
            onClickLabel = onClickLabel,
            role = role,
            onClick = onClick
        )
}

/**
 * Apple Design Momentum Clickable modifier (WWDC Designing Fluid Interfaces).
 * Calibrated with under-damped spring physics (dampingRatio = 0.80f, stiffness = 700f)
 * for interactive actions that carry playful momentum (cheers, nudges, duels).
 */
fun Modifier.iosMomentumClickable(
    interactionSource: MutableInteractionSource? = null,
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    pressedScale: Float = 0.95f,
    pressedAlpha: Float = 0.85f,
    onClick: () -> Unit
) = composed {
    val actualInteractionSource = interactionSource ?: remember { MutableInteractionSource() }
    val isPressed by actualInteractionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1f,
        animationSpec = if (isPressed) {
            spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessHigh
            )
        } else {
            spring(
                dampingRatio = 0.80f, // Momentum bounce on release
                stiffness = 700f
            )
        },
        label = "iosMomentumScale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isPressed) pressedAlpha else 1f,
        animationSpec = spring(
            dampingRatio = 1f,
            stiffness = 600f
        ),
        label = "iosMomentumAlpha"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
            this.alpha = alpha
        }
        .clickable(
            interactionSource = actualInteractionSource,
            indication = null,
            enabled = enabled,
            onClickLabel = onClickLabel,
            role = role,
            onClick = onClick
        )
}

/**
 * ANM-10: Dynamic Duel Card Tap Press Physics & 3D Perspective Tilt.
 * Emil Kowalski tactile card interaction specifications:
 * - Pointer-down: Subtle 2% compression (pressedScale = 0.98f) and slight -1.5° perspective tilt.
 * - Release: Snappy momentum spring release (stiffness = 700f, dampingRatio = 0.78f).
 * - Feedback: Performs subtle mechanical tick haptic on press.
 */
fun Modifier.duelCardPress(
    interactionSource: MutableInteractionSource? = null,
    enabled: Boolean = true,
    onClickLabel: String? = null,
    pressedScale: Float = 0.98f,
    tiltDegrees: Float = -1.5f,
    onClick: () -> Unit = {}
) = composed {
    val actualInteractionSource = interactionSource ?: remember { MutableInteractionSource() }
    val isPressed by actualInteractionSource.collectIsPressedAsState()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(isPressed) {
        if (isPressed) {
            CoupleFitHaptics.performSubtleTick(context, haptic)
        }
    }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1f,
        animationSpec = if (isPressed) {
            spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessHigh)
        } else {
            spring(dampingRatio = 0.78f, stiffness = 700f)
        },
        label = "duelCardScale"
    )

    val tilt by animateFloatAsState(
        targetValue = if (isPressed) tiltDegrees else 0f,
        animationSpec = if (isPressed) {
            spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessHigh)
        } else {
            spring(dampingRatio = 0.78f, stiffness = 700f)
        },
        label = "duelCardTilt"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
            rotationX = tilt
            cameraDistance = 16f * density
        }
        .clickable(
            interactionSource = actualInteractionSource,
            indication = null,
            enabled = enabled,
            onClickLabel = onClickLabel,
            onClick = onClick
        )
}
