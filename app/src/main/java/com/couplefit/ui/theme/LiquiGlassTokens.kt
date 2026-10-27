package com.couplefit.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Liqui Design Optical Tokens & Material System.
 * Ported from official repository: https://github.com/leefanv/liqui-design
 *
 * Implements the 4-layer optical model:
 * 1. Base Hardware Translucent Optical Substrate (Preserves foreground typography & icon crispness by avoiding child RenderEffect blurring)
 * 2. Multi-stop Tint Legibility Wash (--lq-tint to --lq-tint-deep)
 * 3. Directional Specular Bevel Rim (--lq-rim-hi to --lq-rim-lo)
 * 4. Superellipse Continuous Squircle Geometry (22dp radius)
 */
object LiquiTokens {
    // Dark Theme Optical Palette (leefanv/liqui-design/packages/glass/src/tokens.ts)
    val DarkText = Color(0xF5, 0xF6, 0xFF, (255 * 0.96f).toInt())
    val DarkTextDim = Color(0xF5, 0xF6, 0xFF, (255 * 0.55f).toInt())
    val DarkTint = Color(0x30, 0x32, 0x46, (255 * 0.62f).toInt())         // --lq-tint
    val DarkTintDeep = Color(0x12, 0x14, 0x20, (255 * 0.40f).toInt())     // --lq-tint-deep
    val DarkRimHi = Color(0xFF, 0xFF, 0xFF, (255 * 0.50f).toInt())        // --lq-rim-hi
    val DarkRimLo = Color(0xFF, 0xFF, 0xFF, (255 * 0.12f).toInt())        // --lq-rim-lo
    val DarkHighlight = Color(0xFF, 0xFF, 0xFF, (255 * 0.18f).toInt())    // --lq-highlight
    val DarkControl = Color(0x7E, 0x80, 0x92, (255 * 0.30f).toInt())      // --lq-control
    val DarkControlRim = Color(0xFF, 0xFF, 0xFF, (255 * 0.22f).toInt())   // --lq-control-rim
    val DarkAccent = Color(0xFF6F9DFF)                                    // --lq-accent
    val DarkDanger = Color(0xFFFF7076)                                    // --lq-danger

    // Light Theme Optical Palette
    val LightText = Color(0x1C, 0x1C, 0x28, (255 * 0.95f).toInt())
    val LightTextDim = Color(0x1C, 0x1C, 0x28, (255 * 0.55f).toInt())
    val LightTint = Color(0xFF, 0xFF, 0xFF, (255 * 0.48f).toInt())        // --lq-tint
    val LightTintDeep = Color(0xFF, 0xFF, 0xFF, (255 * 0.14f).toInt())    // --lq-tint-deep
    val LightRimHi = Color(0xFF, 0xFF, 0xFF, (255 * 0.85f).toInt())       // --lq-rim-hi
    val LightRimLo = Color(0xFF, 0xFF, 0xFF, (255 * 0.28f).toInt())       // --lq-rim-lo
    val LightHighlight = Color(0xFF, 0xFF, 0xFF, (255 * 0.50f).toInt())   // --lq-highlight
    val LightControl = Color(0xFF, 0xFF, 0xFF, (255 * 0.72f).toInt())     // --lq-control
    val LightControlRim = Color(0x1C, 0x1C, 0x28, (255 * 0.16f).toInt())  // --lq-control-rim
    val LightAccent = Color(0xFF2F6BFF)
    val LightDanger = Color(0xFFE5484D)

    // Squircle Geometry & Bevel Tokens
    val CardRadius: Dp = 22.dp
    val BezelWidth: Dp = 1.25.dp
}

/**
 * Liqui Design Modifier implementing physical optical liquid glass.
 *
 * @param shape Continuous squircle shape boundary
 * @param frost Optical material density scaling (0.0 to 1.0)
 * @param elevation Depth shadow elevation
 */
fun Modifier.liquiGlass(
    shape: Shape = RoundedCornerShape(LiquiTokens.CardRadius),
    frost: Float = 0.35f,
    elevation: Dp = 8.dp
): Modifier = composed {
    val isDark = isSystemInDarkTheme()

    // 2-Stop Optical Tint Legibility Gradient Wash
    val tintBrush = Brush.verticalGradient(
        colors = if (isDark) {
            listOf(
                LiquiTokens.DarkTint.copy(alpha = (0.62f * (1f + frost * 0.15f)).coerceAtMost(0.85f)),
                LiquiTokens.DarkTintDeep.copy(alpha = (0.40f * (1f + frost * 0.15f)).coerceAtMost(0.65f))
            )
        } else {
            listOf(
                LiquiTokens.LightTint.copy(alpha = (0.48f * (1f + frost * 0.15f)).coerceAtMost(0.75f)),
                LiquiTokens.LightTintDeep.copy(alpha = (0.14f * (1f + frost * 0.15f)).coerceAtMost(0.40f))
            )
        }
    )

    // Directional Optical Specular Rim Light Bezel
    val specularRimBrush = Brush.verticalGradient(
        colors = if (isDark) {
            listOf(
                LiquiTokens.DarkRimHi,
                LiquiTokens.DarkHighlight,
                LiquiTokens.DarkRimLo
            )
        } else {
            listOf(
                LiquiTokens.LightRimHi,
                LiquiTokens.LightHighlight,
                LiquiTokens.LightRimLo
            )
        }
    )

    this
        .shadow(
            elevation = elevation,
            shape = shape,
            ambientColor = if (isDark) Color(0x66000005) else Color(0x240A0F28),
            spotColor = if (isDark) Color(0x88000005) else Color(0x380A0F28)
        )
        .clip(shape)
        .background(
            color = if (isDark) LiquiTokens.DarkTintDeep else LiquiTokens.LightTintDeep,
            shape = shape
        )
        .background(brush = tintBrush, shape = shape)
        .border(width = LiquiTokens.BezelWidth, brush = specularRimBrush, shape = shape)
}
