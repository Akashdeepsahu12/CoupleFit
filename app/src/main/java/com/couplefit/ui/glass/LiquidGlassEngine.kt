package com.couplefit.ui.glass

import android.os.Build
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Android 13+ RuntimeShader support check.
 */
fun isShaderRefractionSupported(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

/**
 * Backward-compatible alias forwarding to [Modifier.refractiveLens] in [LiquidGlass.kt].
 * Consolidates redundant AGSL shaders into BackdropShaderCache (YAG-01).
 *
 * @param shape CornerBasedShape defining boundary curvature.
 * @param refractionHeight Outer rim refraction depth.
 * @param refractionAmount Physical refraction displacement factor.
 */
@Composable
fun Modifier.liquidRefractionLens(
    shape: CornerBasedShape = RoundedCornerShape(percent = 50),
    refractionHeight: Dp = 6.dp,
    refractionAmount: Dp = 2.5.dp
): Modifier = this.refractiveLens(
    shape = shape,
    refractionHeight = refractionHeight,
    refractionAmount = refractionAmount
)

