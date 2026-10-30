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

fun isGlassSupported(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

private const val VIBRANCY = 1f
private const val BLUR_RADIUS_DP = 28f
private const val LENS_HEIGHT = 0.5f
private const val LENS_AMOUNT = 0.5f
private const val LENS_MAX_DP = 48f
private const val GLASS_RESOLUTION_SCALE = 1f

val GLASS_EDGE_WIDTH = 0.5.dp
val GLASS_EDGE_COLOR = Color.White.copy(alpha = 0.22f)

@Composable
fun glassContentColor(): Color =
    if (MaterialTheme.colorScheme.surface.luminance() > 0.5f) Color.Black else Color.White

@Composable
fun glassBarTint(): Color =
    if (MaterialTheme.colorScheme.surface.luminance() > 0.5f) {
        Color.White.copy(alpha = 0.35f)
    } else {
        Color(0xFF0E131E).copy(alpha = 0.58f)
    }

/**
 * Exact BitChord Liquid Glass modifier (from liquideffect.md).
 * Applies:
 * 1. 0.33x Hardware Downscaled Backdrop Layer sampling
 * 2. 1.5x Color Vibrancy Saturation boost
 * 3. Real-time RenderEffect Gaussian Blur
 * 4. Physical 3D Lens Refraction with 7-Band Chromatic Dispersion (Android 13+)
 * 5. Tinted Surface Sheen & 0.5dp Specular Rim Border
 */
@Composable
fun Modifier.liquidGlass(
    shape: CornerBasedShape = RoundedCornerShape(percent = 50),
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
    val blurPx = with(density) { BLUR_RADIUS_DP.dp.toPx() } * GLASS_RESOLUTION_SCALE
    val lensHeightPx = with(density) { (LENS_HEIGHT * LENS_MAX_DP).dp.toPx() } * GLASS_RESOLUTION_SCALE
    val lensAmountPx = with(density) { (LENS_AMOUNT * LENS_MAX_DP).dp.toPx() } * GLASS_RESOLUTION_SCALE

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
                        refractionHeight = lensHeightPx,
                        refractionAmount = lensAmountPx,
                        depthEffect = true,
                        chromaticAberration = true,
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
