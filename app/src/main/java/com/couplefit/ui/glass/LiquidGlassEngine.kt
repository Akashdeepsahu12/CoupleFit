package com.couplefit.ui.glass

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.intellij.lang.annotations.Language

/**
 * Pure Frosted Glass Refraction Lens Engine
 * Implements AGSL Runtime Shader Lens Refraction (Android 13+ / API 33+)
 * with subtle physical chromatic dispersion (RGB split 1.015 / 1.000 / 0.985).
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

fun isShaderRefractionSupported(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

/**
 * AGSL Refraction lens modifier applicable over Haze or drawing surfaces (Android 13+).
 * Bends and subtly refracts background content along the outer chamfer edge.
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
            // True corner radius derived from shape height for 50% pill curvature
            val r = size.height * 0.5f
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
