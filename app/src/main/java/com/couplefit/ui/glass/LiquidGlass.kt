package com.couplefit.ui.glass

import android.os.Build
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
 * Runtime check helper matching expo-glass-effect's isGlassEffectAPIAvailable()
 */
fun isGlassEffectAPIAvailable(): Boolean = isGlassSupported()

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
 * 1. [GlassEffectStyle] variants: Regular (frosted liquid), Clear (crystalline), or None.
 * 2. [isInteractive] touch state with spring physics compression and specular rim boost.
 * 3. [tintColor] and [colorScheme] overrides.
 * 4. Fallback rendering on unsupported hardware or disabled state.
 */
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
): Modifier {
    val backdrop = LocalAppBackdrop.current
    val isEnabled = LocalLiquidGlassEnabled.current
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

    // Kinematic parameters calibrated based on glassEffectStyle
    val blurRadius = when (glassEffectStyle) {
        GlassEffectStyle.Regular -> 14.dp
        GlassEffectStyle.Clear -> 6.dp
        GlassEffectStyle.None -> 0.dp
    }
    val refractionHeight = when (glassEffectStyle) {
        GlassEffectStyle.Regular -> 22.dp
        GlassEffectStyle.Clear -> 26.dp
        GlassEffectStyle.None -> 0.dp
    }
    val refractionAmount = when (glassEffectStyle) {
        GlassEffectStyle.Regular -> 22.dp
        GlassEffectStyle.Clear -> 28.dp
        GlassEffectStyle.None -> 0.dp
    }

    // Tint overlay calculation
    val surfaceTintColor = tintColor ?: if (isDark) {
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
            blurRadius = blurRadius,
            refractionHeight = refractionHeight,
            refractionAmount = refractionAmount,
            chromaticAberration = true,
            depthEffect = true,
            surfaceTint = surfaceTintColor,
            borderWidth = borderWidth,
            borderColor = effectiveBorderColor
        )
}
