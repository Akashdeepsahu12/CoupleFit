package com.couplefit.ui.glass

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val LocalLiquidGlassEnabled = staticCompositionLocalOf { true }
val LocalAppBackdrop = staticCompositionLocalOf<Backdrop?> { null }
val LocalRefractionBackdrop: ProvidableCompositionLocal<Backdrop?> get() = LocalAppBackdrop

val LocalGlassContainerActive = compositionLocalOf { false }
val LocalGlassContainerSpacing = compositionLocalOf { 0.dp }

fun isGlassSupported(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

/**
 * Glass effect appearance styles matching https://docs.expo.dev/versions/latest/sdk/glass-effect/
 */
enum class GlassEffectStyle {
    /**
     * Standard iOS frosted liquid glass with balanced diffusion, optical refraction,
     * specular rim highlight, and rich background translucency.
     */
    Regular,

    /**
     * High-clarity crystalline liquid glass with ultra-subtle tint wash, lower blur diffusion,
     * and heightened refraction, letting the background shine through with crystalline optics.
     */
    Clear,

    /**
     * Passthrough / clean translucent surface without heavy AGSL chromatic refraction.
     */
    None
}

/**
 * Glass color scheme override matching expo-glass-effect's colorScheme prop.
 */
enum class GlassColorScheme {
    Auto,
    Light,
    Dark
}

/**
 * Animation and style configuration for [GlassView], matching expo-glass-effect's
 * object configuration for glassEffectStyle:
 * `{ style: "clear" | "regular" | "none", animate: boolean, animationDuration: number }`
 */
data class GlassEffectStyleConfig(
    val style: GlassEffectStyle = GlassEffectStyle.Regular,
    val animate: Boolean = true,
    val animationDurationMs: Int = 300
)

/**
 * Runtime check helper matching expo-glass-effect's isGlassEffectAPIAvailable()
 */
fun isGlassEffectAPIAvailable(): Boolean = isGlassSupported()

/**
 * Runtime check helper matching expo-glass-effect's isLiquidGlassAvailable()
 */
fun isLiquidGlassAvailable(): Boolean = isGlassEffectAPIAvailable()

// Calibrated physical constants matching refraction.md (BitChord Physical Refraction Engine)
private const val VIBRANCY = 1f
private const val DEFAULT_BLUR_RADIUS_DP = 10f
private const val DEFAULT_REFRACTION_HEIGHT_DP = 24f
private const val DEFAULT_REFRACTION_AMOUNT_DP = 26f
private const val GLASS_RESOLUTION_SCALE = 1f

val GLASS_EDGE_WIDTH = 0.5.dp
val GLASS_EDGE_COLOR = Color.White.copy(alpha = 0.22f)

@Composable
fun glassContentColor(): Color =
    if (MaterialTheme.colorScheme.surface.luminance() > 0.5f) Color.Black else Color.White

@Composable
fun glassBarTint(): Color =
    if (MaterialTheme.colorScheme.surface.luminance() > 0.5f) {
        Color.White.copy(alpha = 0.12f)
    } else {
        Color(0xFF0E131E).copy(alpha = 0.45f)
    }

/**
 * Physical 3D Lens Refraction modifier (from refraction.md):
 * Applies:
 * 1. 1:1 Hardware Backdrop Layer sampling
 * 2. Real-time RenderEffect Gaussian Blur (10dp for sharp spectral contrast)
 * 3. Physical AGSL 3D Lens Refraction with 7-Wavelength Chromatic Dispersion (Android 13+)
 * 4. Inward convex normal curvature displacement
 * 5. Translucent surface tint & 0.5dp specular rim border
 */
@Composable
fun Modifier.refractiveLens(
    shape: CornerBasedShape = RoundedCornerShape(percent = 50),
    blurRadius: Dp = DEFAULT_BLUR_RADIUS_DP.dp,
    refractionHeight: Dp = DEFAULT_REFRACTION_HEIGHT_DP.dp,
    refractionAmount: Dp = DEFAULT_REFRACTION_AMOUNT_DP.dp,
    chromaticAberration: Boolean = true,
    depthEffect: Boolean = true,
    surfaceTint: Color? = null,
    borderWidth: Dp = GLASS_EDGE_WIDTH,
    borderColor: Color = GLASS_EDGE_COLOR
): Modifier {
    val backdrop = LocalAppBackdrop.current
    val isEnabled = LocalLiquidGlassEnabled.current

    if (!isGlassSupported() || !isEnabled || backdrop == null) {
        return this
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.92f), shape)
            .border(borderWidth, borderColor, shape)
    }

    val density = LocalDensity.current
    val blurPx = with(density) { blurRadius.toPx() } * GLASS_RESOLUTION_SCALE
    val refHeightPx = with(density) { refractionHeight.toPx() } * GLASS_RESOLUTION_SCALE
    val refAmountPx = with(density) { refractionAmount.toPx() } * GLASS_RESOLUTION_SCALE

    val surfaceTintColor = surfaceTint ?: glassBarTint()

    return clip(shape)
        .drawBackdrop(
            backdrop = backdrop,
            shape = { shape },
            effects = {
                colorControls(saturation = 1f + 0.5f * VIBRANCY)
                blur(blurPx)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    lens(
                        refractionHeight = refHeightPx,
                        refractionAmount = refAmountPx,
                        depthEffect = depthEffect,
                        chromaticAberration = chromaticAberration,
                    )
                }
            },
            onDrawSurface = {
                drawRect(color = surfaceTintColor, size = size)
            },
            backdropScale = GLASS_RESOLUTION_SCALE,
        )
        .border(borderWidth, borderColor, shape)
}

/**
 * Exact BitChord Liquid Glass modifier (from liquideffect.md & refraction.md).
 * Configured with calibrated 10dp blur, 24dp bevel thickness, and 26dp optical lens power.
 */
@Composable
fun Modifier.liquidGlass(
    shape: CornerBasedShape = RoundedCornerShape(percent = 50),
    surfaceTint: Color? = null,
    borderWidth: Dp = GLASS_EDGE_WIDTH,
    borderColor: Color = GLASS_EDGE_COLOR
): Modifier = refractiveLens(
    shape = shape,
    blurRadius = DEFAULT_BLUR_RADIUS_DP.dp,
    refractionHeight = DEFAULT_REFRACTION_HEIGHT_DP.dp,
    refractionAmount = DEFAULT_REFRACTION_AMOUNT_DP.dp,
    chromaticAberration = true,
    depthEffect = true,
    surfaceTint = surfaceTint,
    borderWidth = borderWidth,
    borderColor = borderColor
)

/**
 * Expo Glass Effect native liquid glass modifier matching https://docs.expo.dev/versions/latest/sdk/glass-effect/
 *
 * Implements:
 * 1. [GlassEffectStyleConfig] with dynamic property animation across blur, refraction, and tint.
 * 2. [LocalGlassContainerActive] and [LocalGlassContainerSpacing] proximity blending coordination.
 * 3. [isInteractive] touch state with spring physics compression and specular rim boost.
 * 4. [tintColor] and [colorScheme] overrides.
 * 5. Fallback rendering on unsupported hardware or disabled state.
 */
@Composable
fun Modifier.expoGlassEffect(
    shape: CornerBasedShape = RoundedCornerShape(percent = 50),
    styleConfig: GlassEffectStyleConfig,
    isInteractive: Boolean = true,
    tintColor: Color? = null,
    colorScheme: GlassColorScheme = GlassColorScheme.Auto,
    borderWidth: Dp = GLASS_EDGE_WIDTH,
    borderColor: Color? = null,
    isPressed: Boolean = false,
    reduceMotion: Boolean = false
): Modifier {
    val glassEffectStyle = styleConfig.style
    val backdrop = LocalAppBackdrop.current
    val isEnabled = LocalLiquidGlassEnabled.current
    val inContainer = LocalGlassContainerActive.current
    val containerSpacing = LocalGlassContainerSpacing.current

    val isDark = when (colorScheme) {
        GlassColorScheme.Auto -> MaterialTheme.colorScheme.surface.luminance() <= 0.5f
        GlassColorScheme.Dark -> true
        GlassColorScheme.Light -> false
    }

    if (!isGlassSupported() || !isEnabled || backdrop == null || glassEffectStyle == GlassEffectStyle.None) {
        val fallbackBg = tintColor ?: if (isDark) {
            Color(0xFF0E131E).copy(alpha = 0.90f)
        } else {
            MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
        }
        val fallbackBorder = borderColor ?: GLASS_EDGE_COLOR
        return this
            .clip(shape)
            .background(fallbackBg, shape)
            .border(borderWidth, fallbackBorder, shape)
    }

    // Kinematic parameters calibrated based on glassEffectStyle with container proximity blending
    val targetBlurRadiusDp = when (glassEffectStyle) {
        GlassEffectStyle.Regular -> 14f
        GlassEffectStyle.Clear -> 6f
        GlassEffectStyle.None -> 0f
    }
    val proximityRefractionBonus = if (inContainer) (containerSpacing.value * 0.25f).coerceIn(0f, 6f) else 0f

    val targetRefractionHeightDp = when (glassEffectStyle) {
        GlassEffectStyle.Regular -> 22f + proximityRefractionBonus
        GlassEffectStyle.Clear -> 26f + proximityRefractionBonus
        GlassEffectStyle.None -> 0f
    }
    val targetRefractionAmountDp = when (glassEffectStyle) {
        GlassEffectStyle.Regular -> 22f + proximityRefractionBonus
        GlassEffectStyle.Clear -> 28f + proximityRefractionBonus
        GlassEffectStyle.None -> 0f
    }

    val effectiveBlurRadiusDp by animateFloatAsState(
        targetValue = targetBlurRadiusDp,
        animationSpec = if (reduceMotion || !styleConfig.animate) snap() else tween(durationMillis = styleConfig.animationDurationMs),
        label = "glassBlurRadius"
    )
    val effectiveRefractionHeightDp by animateFloatAsState(
        targetValue = targetRefractionHeightDp,
        animationSpec = if (reduceMotion || !styleConfig.animate) snap() else tween(durationMillis = styleConfig.animationDurationMs),
        label = "glassRefractionHeight"
    )
    val effectiveRefractionAmountDp by animateFloatAsState(
        targetValue = targetRefractionAmountDp,
        animationSpec = if (reduceMotion || !styleConfig.animate) snap() else tween(durationMillis = styleConfig.animationDurationMs),
        label = "glassRefractionAmount"
    )

    // Tint overlay calculation
    val baseTintColor = tintColor ?: if (isDark) {
        when (glassEffectStyle) {
            GlassEffectStyle.Regular -> Color(0xFF0E131E).copy(alpha = 0.45f)
            GlassEffectStyle.Clear -> Color(0xFF0E131E).copy(alpha = 0.20f)
            GlassEffectStyle.None -> Color(0xFF0E131E).copy(alpha = 0.85f)
        }
    } else {
        when (glassEffectStyle) {
            GlassEffectStyle.Regular -> Color.White.copy(alpha = 0.22f)
            GlassEffectStyle.Clear -> Color.White.copy(alpha = 0.12f)
            GlassEffectStyle.None -> Color.White.copy(alpha = 0.90f)
        }
    }

    val effectiveTintColor by animateColorAsState(
        targetValue = baseTintColor,
        animationSpec = if (reduceMotion || !styleConfig.animate) snap() else tween(durationMillis = styleConfig.animationDurationMs),
        label = "glassTintColor"
    )

    // Interactive specular boost
    val interactiveBorderAlpha = if (isInteractive && isPressed && !reduceMotion) 0.42f else 0.22f
    val effectiveBorderColor = borderColor ?: Color.White.copy(alpha = interactiveBorderAlpha)

    // Interactive scale compression
    val interactiveScale by animateFloatAsState(
        targetValue = if (isInteractive && isPressed && !reduceMotion) 0.985f else 1.0f,
        animationSpec = if (reduceMotion) snap() else spring(dampingRatio = 0.82f, stiffness = 600f),
        label = "interactiveGlassScale"
    )

    return this
        .graphicsLayer {
            scaleX = interactiveScale
            scaleY = interactiveScale
        }
        .refractiveLens(
            shape = shape,
            blurRadius = effectiveBlurRadiusDp.dp,
            refractionHeight = effectiveRefractionHeightDp.dp,
            refractionAmount = effectiveRefractionAmountDp.dp,
            chromaticAberration = true,
            depthEffect = true,
            surfaceTint = effectiveTintColor,
            borderWidth = borderWidth,
            borderColor = effectiveBorderColor
        )
}

@Composable
fun Modifier.expoGlassEffect(
    shape: CornerBasedShape = RoundedCornerShape(percent = 50),
    glassEffectStyle: GlassEffectStyle = GlassEffectStyle.Regular,
    isInteractive: Boolean = true,
    tintColor: Color? = null,
    colorScheme: GlassColorScheme = GlassColorScheme.Auto,
    borderWidth: Dp = GLASS_EDGE_WIDTH,
    borderColor: Color? = null,
    isPressed: Boolean = false,
    reduceMotion: Boolean = false
): Modifier = expoGlassEffect(
    shape = shape,
    styleConfig = GlassEffectStyleConfig(style = glassEffectStyle, animate = false),
    isInteractive = isInteractive,
    tintColor = tintColor,
    colorScheme = colorScheme,
    borderWidth = borderWidth,
    borderColor = borderColor,
    isPressed = isPressed,
    reduceMotion = reduceMotion
)

/**
 * Expo Glass Effect GlassContainer composable matching https://docs.expo.dev/versions/latest/sdk/glass-effect/
 *
 * Parent container that groups and coordinates multiple adjacent or nested [GlassView] components,
 * providing shared spacing thresholds, proximity blending, backdrop sampling, and a unified glass rendering context.
 */
@Composable
fun GlassContainer(
    modifier: Modifier = Modifier,
    spacing: Dp = 8.dp,
    shape: CornerBasedShape = RoundedCornerShape(percent = 50),
    content: @Composable BoxScope.() -> Unit
) {
    CompositionLocalProvider(
        LocalGlassContainerActive provides true,
        LocalGlassContainerSpacing provides spacing
    ) {
        Box(
            modifier = modifier.clip(shape),
            content = content
        )
    }
}

/**
 * Expo Glass Effect GlassView composable matching https://docs.expo.dev/versions/latest/sdk/glass-effect/
 *
 * Renders an individual translucent, light-bending liquid glass surface with optional animated style transitions.
 *
 * @param modifier Layout and positioning modifier
 * @param styleConfig Style & animation configuration ([GlassEffectStyleConfig])
 * @param tintColor Color tint applied over the glass surface
 * @param colorScheme Color scheme override ([GlassColorScheme.Auto], [GlassColorScheme.Light], [GlassColorScheme.Dark])
 * @param isInteractive Enables dynamic interactive touch feedback with spring compression and specular boost
 * @param shape Corner clipping shape (defaults to capsule pill)
 * @param borderWidth Specular edge width
 * @param borderColor Specular edge color
 * @param isPressed External press state override
 * @param reduceMotion Disables spring animations for accessibility
 * @param content Child composable content
 */
@Composable
fun GlassView(
    modifier: Modifier = Modifier,
    styleConfig: GlassEffectStyleConfig,
    tintColor: Color? = null,
    colorScheme: GlassColorScheme = GlassColorScheme.Auto,
    isInteractive: Boolean = false,
    shape: CornerBasedShape = RoundedCornerShape(percent = 50),
    borderWidth: Dp = GLASS_EDGE_WIDTH,
    borderColor: Color? = null,
    isPressed: Boolean = false,
    reduceMotion: Boolean = false,
    content: @Composable (BoxScope.() -> Unit)? = null
) {
    Box(
        modifier = modifier.expoGlassEffect(
            shape = shape,
            styleConfig = styleConfig,
            isInteractive = isInteractive,
            tintColor = tintColor,
            colorScheme = colorScheme,
            borderWidth = borderWidth,
            borderColor = borderColor,
            isPressed = isPressed,
            reduceMotion = reduceMotion
        )
    ) {
        content?.invoke(this)
    }
}

/**
 * Expo Glass Effect GlassView composable matching https://docs.expo.dev/versions/latest/sdk/glass-effect/
 *
 * Direct style overload without animation wrapper.
 */
@Composable
fun GlassView(
    modifier: Modifier = Modifier,
    glassEffectStyle: GlassEffectStyle = GlassEffectStyle.Regular,
    tintColor: Color? = null,
    colorScheme: GlassColorScheme = GlassColorScheme.Auto,
    isInteractive: Boolean = false,
    shape: CornerBasedShape = RoundedCornerShape(percent = 50),
    borderWidth: Dp = GLASS_EDGE_WIDTH,
    borderColor: Color? = null,
    isPressed: Boolean = false,
    reduceMotion: Boolean = false,
    content: @Composable (BoxScope.() -> Unit)? = null
) = GlassView(
    modifier = modifier,
    styleConfig = GlassEffectStyleConfig(style = glassEffectStyle, animate = false),
    tintColor = tintColor,
    colorScheme = colorScheme,
    isInteractive = isInteractive,
    shape = shape,
    borderWidth = borderWidth,
    borderColor = borderColor,
    isPressed = isPressed,
    reduceMotion = reduceMotion,
    content = content
)
