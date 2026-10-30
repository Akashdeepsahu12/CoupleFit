package com.couplefit.ui.glass

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.graphics.Shader
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.intellij.lang.annotations.Language

/**
 * Pure Frosted Glass, Refraction Lens, and Mesh Dispersion Engine
 * Extracted directly from BitChord & Android AGSL Runtime Shaders (liquideffect.md).
 *
 * Implements:
 * 1. AGSL Runtime Shader Lens Refraction (Android 13+ / API 33+)
 * 2. Chromatic Aberration / Dispersion (RGB wavelength splitting 1.08 / 1.00 / 0.92)
 * 3. Hardware-Accelerated 32dp Deep Frosted Gaussian Blur (RenderEffect)
 * 4. Specular Highlight Boundary Rim
 */
@Language("AGSL")
const val REFRACTION_WITH_DISPERSION_AGSL = """
uniform shader content;
uniform float2 size;
uniform float4 radii; // top-left, top-right, bottom-right, bottom-left
uniform float refractionHeight;
uniform float refractionAmount;

float radiusAt(float2 coord, float4 r) {
    if (coord.x >= 0.0) {
        return (coord.y <= 0.0) ? r.y : r.z;
    } else {
        return (coord.y <= 0.0) ? r.x : r.w;
    }
}

float sdRoundedRect(float2 coord, float2 halfSize, float radius) {
    float2 cornerCoord = abs(coord) - (halfSize - float2(radius));
    float outside = length(max(cornerCoord, 0.0)) - radius;
    float inside = min(max(cornerCoord.x, cornerCoord.y), 0.0);
    return outside + inside;
}

float2 gradSdRoundedRect(float2 coord, float2 halfSize, float radius) {
    float2 cornerCoord = abs(coord) - (halfSize - float2(radius));
    float outside = length(max(cornerCoord, 0.0)) - radius;
    float inside = min(max(cornerCoord.x, cornerCoord.y), 0.0);
    float2 signCoord = sign(coord);
    if (outside > 0.0) {
        return signCoord * normalize(max(cornerCoord, 0.0));
    } else {
        return (cornerCoord.x > cornerCoord.y) ? float2(signCoord.x, 0.0) : float2(0.0, signCoord.y);
    }
}

half4 main(float2 coord) {
    float2 halfSize = size * 0.5;
    float2 centeredCoord = coord - halfSize;
    float radius = radiusAt(centeredCoord, radii);
    float dist = sdRoundedRect(centeredCoord, halfSize, radius);

    if (dist <= 0.0 && dist >= -refractionHeight) {
        float progress = 1.0 + (dist / refractionHeight); // 0 (edge) to 1 (inner)
        float factor = sin(progress * 1.5707963) * refractionAmount;
        float2 normal = gradSdRoundedRect(centeredCoord, halfSize, radius);

        // Chromatic Aberration / Dispersion offset (subtle realistic RGB split)
        float2 rOffset = -normal * factor * 1.015;
        float2 gOffset = -normal * factor * 1.000;
        float2 bOffset = -normal * factor * 0.985;

        half4 rCol = content.eval(coord + rOffset);
        half4 gCol = content.eval(coord + gOffset);
        half4 bCol = content.eval(coord + bOffset);

        return half4(rCol.r, gCol.g, bCol.b, (rCol.a + gCol.a + bCol.a) / 3.0);
    }
    return content.eval(coord);
}
"""

fun isGlassSupported(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
fun isShaderRefractionSupported(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

private val GLASS_EDGE_WIDTH = 0.5.dp
private val GLASS_EDGE_COLOR = Color.White.copy(alpha = 0.10f)

/**
 * High-performance standalone Liquid Glass modifier:
 * - Real-time RenderEffect Blur with enhanced 32dp blur radius (Android 12+)
 * - AGSL Lens Refraction & Chromatic Aberration (Android 13+)
 * - Specular Light Edge Rim
 * - Vibrancy Surface Tint
 */
@Composable
fun Modifier.standaloneLiquidGlass(
    shape: CornerBasedShape = RoundedCornerShape(percent = 50),
    blurRadius: Dp = 24.dp,
    refractionHeight: Dp = 6.dp,
    refractionAmount: Dp = 2.5.dp,
    tintColor: Color? = null,
): Modifier {
    if (!isGlassSupported()) {
        return this
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.92f), shape)
            .border(GLASS_EDGE_WIDTH, GLASS_EDGE_COLOR, shape)
    }

    val density = LocalDensity.current
    val blurPx = with(density) { blurRadius.toPx() }
    val refHeightPx = with(density) { refractionHeight.toPx() }
    val refAmountPx = with(density) { refractionAmount.toPx() }

    val defaultTint = if (MaterialTheme.colorScheme.surface.luminance() > 0.5f) {
        Color.White.copy(alpha = 0.25f)
    } else {
        Color(0xFF0E131E).copy(alpha = 0.35f)
    }
    val surfaceTint = tintColor ?: defaultTint

    val runtimeShader = remember {
        if (isShaderRefractionSupported()) {
            RuntimeShader(REFRACTION_WITH_DISPERSION_AGSL)
        } else null
    }

    return this
        .graphicsLayer {
            clip = true
            this.shape = shape

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val blurEffect = RenderEffect.createBlurEffect(
                    blurPx,
                    blurPx,
                    Shader.TileMode.CLAMP
                )

                val finalEffect = if (isShaderRefractionSupported() && runtimeShader != null) {
                    runtimeShader.setFloatUniform("size", size.width, size.height)
                    val r = with(density) { 32.dp.toPx() }
                    runtimeShader.setFloatUniform("radii", r, r, r, r)
                    runtimeShader.setFloatUniform("refractionHeight", refHeightPx)
                    runtimeShader.setFloatUniform("refractionAmount", refAmountPx)

                    val shaderEffect = RenderEffect.createRuntimeShaderEffect(
                        runtimeShader,
                        "content"
                    )
                    RenderEffect.createChainEffect(shaderEffect, blurEffect)
                } else {
                    blurEffect
                }

                renderEffect = finalEffect.asComposeRenderEffect()
            }
        }
        .drawWithContent {
            drawContent()
            drawRect(color = surfaceTint)
        }
        .border(GLASS_EDGE_WIDTH, GLASS_EDGE_COLOR, shape)
}

/**
 * AGSL Refraction lens modifier applicable over Haze or drawing surfaces.
 */
@Composable
fun Modifier.liquidRefractionLens(
    shape: CornerBasedShape = RoundedCornerShape(percent = 50),
    refractionHeight: Dp = 6.dp,
    refractionAmount: Dp = 2.5.dp
): Modifier {
    if (!isShaderRefractionSupported()) return this

    val density = LocalDensity.current
    val refHeightPx = with(density) { refractionHeight.toPx() }
    val refAmountPx = with(density) { refractionAmount.toPx() }

    val runtimeShader = remember { RuntimeShader(REFRACTION_WITH_DISPERSION_AGSL) }

    return this.graphicsLayer {
        clip = true
        this.shape = shape

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            runtimeShader.setFloatUniform("size", size.width, size.height)
            val r = with(density) { 32.dp.toPx() }
            runtimeShader.setFloatUniform("radii", r, r, r, r)
            runtimeShader.setFloatUniform("refractionHeight", refHeightPx)
            runtimeShader.setFloatUniform("refractionAmount", refAmountPx)

            renderEffect = RenderEffect.createRuntimeShaderEffect(
                runtimeShader,
                "content"
            ).asComposeRenderEffect()
        }
    }
}
