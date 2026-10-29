package com.couplefit.ui.theme

import android.os.Build
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Three-Layer Design System Token Architecture for Liquid Glass Surfaces (GLS-01, GLS-06, GLS-08).
 * Follows `/ui-ux-pro-max-skill:design-system` and `/emilkowalski-skills:emil-design-eng` guidelines.
 */

// =========================================================================
// 1. Primitive Tokens (Raw Values)
// =========================================================================
object GlassPrimitives {
    // Opacity / Tint Primitives (Preserved for backward compatibility)
    val TintWhite04 = Color(0x0AFFFFFF)      // 4% white (specular shadow decay)
    val TintWhite08 = Color(0x14FFFFFF)      // 8% white (resting card surface)
    val TintWhite10 = Color(0x1AFFFFFF)      // 10% white (floating nav dock)
    val TintWhite12 = Color(0x1FFFFFFF)      // 12% white (hover/elevated surface)
    val TintWhite16 = Color(0x29FFFFFF)      // 16% white (modal surface)
    val TintWhite22 = Color(0x38FFFFFF)      // 22% white (specular highlight)
    val TintDarkScrim75 = Color(0xBF07080A)  // 75% dark scrim for modal backdrops

    // Luminous Specular Stop Primitives (GLS-08)
    val TintWhiteSpecularTop = Color(0x5EFFFFFF)  // 37% white specular light catch
    val TintWhiteSpecularMid = Color(0x22FFFFFF)  // 13% white body sheen
    val TintWhiteSurfaceGlint = Color(0x24FFFFFF) // 14% white top surface glint
    val TintWhiteSurfaceBody = Color(0x10FFFFFF)  // 6.5% white body
    val TintWhiteSurfaceBase = Color(0x06FFFFFF)  // 2.4% white shadow base

    // Atmospheric Frosted Bases (GLS-08, GLS-10)
    val TranslucentSlateBase = Color(0x38121622)   // Deep translucent midnight slate base
    val TranslucentDockTop = Color(0xE81C2333)     // 91% opacity frosted dark navy slate
    val TranslucentDockBottom = Color(0xF410131C)  // 96% opacity frosted dark slate

    // Fallback Solid Colors for Android < 12 (API 29-30)
    val SolidSlateCard = Color(0xFF141720)
    val SolidSlateDock = Color(0xFF181B26)
    val SolidSlateModal = Color(0xFF202433)

    // Blur Radii Primitives
    val BlurCard: Dp = 20.dp
    val BlurDock: Dp = 28.dp
    val BlurModal: Dp = 32.dp

    // Elevation & Geometry Primitives
    val ElevationDock: Dp = 16.dp
    val RadiusCard: Dp = 22.dp
    val RadiusPill: Dp = 32.dp
    val RadiusModal: Dp = 28.dp
}

// =========================================================================
// 2. Semantic Tokens (Purpose & Role Aliases)
// =========================================================================
object GlassSemantics {
    // Background Tints (Backward-compatible)
    val SurfaceCardBg = GlassPrimitives.TintWhite08
    val SurfaceDockBg = GlassPrimitives.TintWhite10
    val SurfaceModalBg = GlassPrimitives.TintWhite16

    // Diamond-Cut Top Specular Rim Gradient Token (GLS-08)
    val BorderSpecularRim: Brush = Brush.verticalGradient(
        0.0f to GlassPrimitives.TintWhiteSpecularTop,
        0.20f to GlassPrimitives.TintWhiteSpecularMid,
        1.0f to GlassPrimitives.TintWhite04
    )

    // Backward-compatible accessor
    fun borderSpecularRim(): Brush = BorderSpecularRim

    // Luminous Frosted Glass Surface Gradients (GLS-08, GLS-10)
    val CardSurfaceBrush: Brush = Brush.verticalGradient(
        listOf(
            GlassPrimitives.TintWhiteSurfaceGlint,
            GlassPrimitives.TintWhiteSurfaceBody,
            GlassPrimitives.TintWhiteSurfaceBase
        )
    )

    val DockSurfaceBrush: Brush = Brush.verticalGradient(
        listOf(
            GlassPrimitives.TranslucentDockTop,
            GlassPrimitives.TranslucentDockBottom
        )
    )

    val ModalSurfaceBrush: Brush = Brush.verticalGradient(
        listOf(
            Color(0x35FFFFFF),
            GlassPrimitives.TintWhite16,
            Color(0x0CFFFFFF)
        )
    )

    // Ambient Lighting Meshes (Vibrant Aurora Telemetry Glow GLS-03, GLS-09)
    val AmbientGlowUserCyan = MotionCyanStart.copy(alpha = 0.28f)
    val AmbientGlowPartnerCoral = MotionPartnerStart.copy(alpha = 0.28f)
    val AmbientGlowSynchronyGold = CoupleFitIdentityTokens.SynchronyGold.copy(alpha = 0.32f)
}

// =========================================================================
// 3. Component Tokens (UI Bindings)
// =========================================================================
object GlassComponentTokens {
    val CardCornerShape = RoundedCornerShape(GlassPrimitives.RadiusCard)
    val DockCornerShape = RoundedCornerShape(GlassPrimitives.RadiusPill)
    val ModalCornerShape = RoundedCornerShape(topStart = GlassPrimitives.RadiusModal, topEnd = GlassPrimitives.RadiusModal)
}

// =========================================================================
// 4. Reusable Glass Modifiers (GLS-01, GLS-08, APL-01)
// =========================================================================

/**
 * Applies Apple VisionOS-grade Liquid Glassmorphic styling to any Compose surface:
 * - Dual-layer surface: deep atmospheric slate base + specular vertical gradient sheen
 * - Diamond-cut top specular gradient rim (`BorderSpecularRim`)
 * - Graceful solid tinted slate fallback for Android 10/11
 * - Preserves foreground typography and icon crispness by avoiding child RenderEffect blurring
 */
fun Modifier.iosGlass(
    shape: Shape = GlassComponentTokens.CardCornerShape,
    surfaceBrush: Brush = GlassSemantics.CardSurfaceBrush,
    tintColor: Color = GlassSemantics.SurfaceCardBg,
    fallbackColor: Color = GlassPrimitives.SolidSlateCard,
    blurRadius: Dp = GlassPrimitives.BlurCard,
    borderBrush: Brush = GlassSemantics.BorderSpecularRim,
    borderWidth: Dp = 1.dp
): Modifier = this
    .shadow(
        elevation = 8.dp,
        shape = shape,
        ambientColor = Color(0x66000005),
        spotColor = Color(0x88000005)
    )
    .clip(shape)
    .then(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Modifier
                .background(color = GlassPrimitives.TranslucentSlateBase, shape = shape)
                .background(brush = surfaceBrush, shape = shape)
        } else {
            Modifier.background(color = fallbackColor, shape = shape)
        }
    )
    .border(
        width = borderWidth,
        brush = borderBrush,
        shape = shape
    )

/**
 * Semantic alias for [iosGlass] adhering to Apple Human Interface Guidelines naming.
 */
fun Modifier.appleGlass(
    shape: Shape = GlassComponentTokens.CardCornerShape,
    surfaceBrush: Brush = GlassSemantics.CardSurfaceBrush,
    tintColor: Color = GlassSemantics.SurfaceCardBg,
    fallbackColor: Color = GlassPrimitives.SolidSlateCard,
    blurRadius: Dp = GlassPrimitives.BlurCard,
    borderBrush: Brush = GlassSemantics.BorderSpecularRim,
    borderWidth: Dp = 1.dp
): Modifier = iosGlass(
    shape = shape,
    surfaceBrush = surfaceBrush,
    tintColor = tintColor,
    fallbackColor = fallbackColor,
    blurRadius = blurRadius,
    borderBrush = borderBrush,
    borderWidth = borderWidth
)

/**
 * Glass dock modifier specifically calibrated for navigation bars (GLS-02, GLS-10).
 * Implements high-contrast frosted slate gradient base and specular top rim.
 */
fun Modifier.glassDock(
    shape: Shape = GlassComponentTokens.DockCornerShape,
    surfaceBrush: Brush = GlassSemantics.DockSurfaceBrush,
    fallbackColor: Color = GlassPrimitives.SolidSlateDock,
    blurRadius: Dp = GlassPrimitives.BlurDock,
    borderBrush: Brush = GlassSemantics.BorderSpecularRim,
    borderWidth: Dp = 1.dp
): Modifier = this
    .shadow(
        elevation = GlassPrimitives.ElevationDock,
        shape = shape,
        ambientColor = Color(0x77000008),
        spotColor = Color(0x9900000C)
    )
    .clip(shape)
    .then(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Modifier.background(brush = surfaceBrush, shape = shape)
        } else {
            Modifier.background(color = fallbackColor, shape = shape)
        }
    )
    .border(
        width = borderWidth,
        brush = borderBrush,
        shape = shape
    )

/**
 * Glass modal modifier specifically calibrated for sheets and dialogs (GLS-04).
 */
fun Modifier.glassModal(
    shape: Shape = GlassComponentTokens.ModalCornerShape,
    surfaceBrush: Brush = GlassSemantics.ModalSurfaceBrush,
    tintColor: Color = GlassSemantics.SurfaceModalBg,
    fallbackColor: Color = GlassPrimitives.SolidSlateModal,
    blurRadius: Dp = GlassPrimitives.BlurModal,
    borderBrush: Brush = GlassSemantics.BorderSpecularRim,
    borderWidth: Dp = 1.dp
): Modifier = this
    .clip(shape)
    .then(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Modifier
                .background(color = GlassPrimitives.SolidSlateModal.copy(alpha = 0.85f), shape = shape)
                .background(brush = surfaceBrush, shape = shape)
        } else {
            Modifier.background(color = fallbackColor, shape = shape)
        }
    )
    .border(
        width = borderWidth,
        brush = borderBrush,
        shape = shape
    )

// =========================================================================
// 5. Ambient Aurora Telemetry Mesh Composable (GLS-03, GLS-09)
// =========================================================================

/**
 * Ambient dual-radial aurora mesh lighting simulating dynamic back-illumination
 * behind glass cards (Luminous Electric Cyan for user, Radiant Vibrant Coral for partner).
 */
@Composable
fun AuroraTelemetryMesh(
    modifier: Modifier = Modifier,
    userGlowColor: Color = GlassSemantics.AmbientGlowUserCyan,
    partnerGlowColor: Color = GlassSemantics.AmbientGlowPartnerCoral,
    content: @Composable BoxScope.() -> Unit = {}
) {
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val width = size.width
            val height = size.height
            // User Cyan Ambient Aura (Left) - Luminous 3-tier radial glow
            drawCircle(
                brush = Brush.radialGradient(
                    colorStops = arrayOf(
                        0.0f to userGlowColor,
                        0.45f to userGlowColor.copy(alpha = userGlowColor.alpha * 0.45f),
                        1.0f to Color.Transparent
                    ),
                    center = Offset(width * 0.25f, height * 0.48f),
                    radius = width * 0.52f
                ),
                center = Offset(width * 0.25f, height * 0.48f),
                radius = width * 0.52f
            )
            // Partner Coral Ambient Aura (Right) - Luminous 3-tier radial glow
            drawCircle(
                brush = Brush.radialGradient(
                    colorStops = arrayOf(
                        0.0f to partnerGlowColor,
                        0.45f to partnerGlowColor.copy(alpha = partnerGlowColor.alpha * 0.45f),
                        1.0f to Color.Transparent
                    ),
                    center = Offset(width * 0.75f, height * 0.48f),
                    radius = width * 0.52f
                ),
                center = Offset(width * 0.75f, height * 0.48f),
                radius = width * 0.52f
            )
        }
        content()
    }
}
