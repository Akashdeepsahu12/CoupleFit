package com.couplefit.ui.glass

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val LocalLiquidGlassEnabled = staticCompositionLocalOf { true }
val LocalAppBackdrop = staticCompositionLocalOf<Backdrop?> { null }
val LocalRefractionBackdrop: ProvidableCompositionLocal<Backdrop?> get() = LocalAppBackdrop

fun isGlassSupported(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

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
