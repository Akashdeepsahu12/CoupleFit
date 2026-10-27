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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Three-Layer Design System Token Architecture for Liquid Glass Surfaces (GLS-01, GLS-06).
 * Follows `/ui-ux-pro-max-skill:design-system` guidelines.
 */

// =========================================================================
// 1. Primitive Tokens (Raw Values)
// =========================================================================
object GlassPrimitives {
    // Opacity / Tint Primitives
    val TintWhite04 = Color(0x0AFFFFFF)      // 4% white (specular shadow decay)
    val TintWhite08 = Color(0x14FFFFFF)      // 8% white (resting card surface)
    val TintWhite10 = Color(0x1AFFFFFF)      // 10% white (floating nav dock)
    val TintWhite12 = Color(0x1FFFFFFF)      // 12% white (hover/elevated surface)
    val TintWhite16 = Color(0x29FFFFFF)      // 16% white (modal surface)
    val TintWhite22 = Color(0x38FFFFFF)      // 22% white (top specular glare)
    val TintDarkScrim75 = Color(0xBF07080A)  // 75% dark scrim for modal backdrops

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
    val RadiusCard: Dp = 20.dp
    val RadiusPill: Dp = 32.dp
    val RadiusModal: Dp = 28.dp
}

// =========================================================================
// 2. Semantic Tokens (Purpose & Role Aliases)
// =========================================================================
object GlassSemantics {
    // Background Tints
    val SurfaceCardBg = GlassPrimitives.TintWhite08
    val SurfaceDockBg = GlassPrimitives.TintWhite10
    val SurfaceModalBg = GlassPrimitives.TintWhite16

    // Specular Rim Gradient Token (Cached immutable instance)
    val BorderSpecularRim: Brush = Brush.verticalGradient(
        listOf(
            GlassPrimitives.TintWhite22,
            GlassPrimitives.TintWhite04
        )
    )

    // Backward-compatible accessor
    fun borderSpecularRim(): Brush = BorderSpecularRim

    // Ambient Lighting Meshes (Aurora Telemetry Glow GLS-03)
    val AmbientGlowUserCyan = MotionCyanStart.copy(alpha = 0.08f)
    val AmbientGlowPartnerCoral = MotionPartnerStart.copy(alpha = 0.08f)
    val AmbientGlowSynchronyGold = CoupleFitIdentityTokens.SynchronyGold.copy(alpha = 0.12f)
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
// 4. Reusable Glass Modifiers (GLS-01, APL-01)
// =========================================================================

/**
 * Applies Apple-grade Liquid Glassmorphic styling to any Compose surface:
 * - 8% translucent white tint over pitch OLED black on Android 12+ (API 31+)
 * - 1px vertical specular gradient border (`BorderSpecularRim`)
 * - Graceful solid tinted slate fallback for Android 10/11
 * - Preserves foreground typography and icon crispness by avoiding child RenderEffect blurring
 */
fun Modifier.iosGlass(
    shape: Shape = GlassComponentTokens.CardCornerShape,
    tintColor: Color = GlassSemantics.SurfaceCardBg,
    fallbackColor: Color = GlassPrimitives.SolidSlateCard,
    blurRadius: Dp = GlassPrimitives.BlurCard,
    borderBrush: Brush = GlassSemantics.BorderSpecularRim,
    borderWidth: Dp = 1.dp
): Modifier = this
    .clip(shape)
    .background(
        color = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            tintColor
        } else {
            fallbackColor
        },
        shape = shape
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
    tintColor: Color = GlassSemantics.SurfaceCardBg,
    fallbackColor: Color = GlassPrimitives.SolidSlateCard,
    blurRadius: Dp = GlassPrimitives.BlurCard,
    borderBrush: Brush = GlassSemantics.BorderSpecularRim,
    borderWidth: Dp = 1.dp
): Modifier = iosGlass(
    shape = shape,
    tintColor = tintColor,
    fallbackColor = fallbackColor,
    blurRadius = blurRadius,
    borderBrush = borderBrush,
    borderWidth = borderWidth
)

/**
 * Glass dock modifier specifically calibrated for navigation bars (GLS-02).
 */
fun Modifier.glassDock(
    shape: Shape = GlassComponentTokens.DockCornerShape,
    tintColor: Color = GlassSemantics.SurfaceDockBg,
    fallbackColor: Color = GlassPrimitives.SolidSlateDock,
    blurRadius: Dp = GlassPrimitives.BlurDock,
    borderBrush: Brush = GlassSemantics.BorderSpecularRim,
    borderWidth: Dp = 1.dp
): Modifier = iosGlass(
    shape = shape,
    tintColor = tintColor,
    fallbackColor = fallbackColor,
    blurRadius = blurRadius,
    borderBrush = borderBrush,
    borderWidth = borderWidth
)

/**
 * Glass modal modifier specifically calibrated for sheets and dialogs (GLS-04).
 */
fun Modifier.glassModal(
    shape: Shape = GlassComponentTokens.ModalCornerShape,
    tintColor: Color = GlassSemantics.SurfaceModalBg,
    fallbackColor: Color = GlassPrimitives.SolidSlateModal,
    blurRadius: Dp = GlassPrimitives.BlurModal,
    borderBrush: Brush = GlassSemantics.BorderSpecularRim,
    borderWidth: Dp = 1.dp
): Modifier = iosGlass(
    shape = shape,
    tintColor = tintColor,
    fallbackColor = fallbackColor,
    blurRadius = blurRadius,
    borderBrush = borderBrush,
    borderWidth = borderWidth
)

// =========================================================================
// 5. Ambient Aurora Telemetry Mesh Composable (GLS-03)
// =========================================================================

/**
 * Ambient dual-radial aurora mesh lighting simulating dynamic back-illumination
 * behind glass cards (Electric Cyan for user, Vibrant Coral for partner).
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
            // User Cyan Ambient Aura (Left)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(userGlowColor, Color.Transparent),
                    center = Offset(width * 0.25f, height * 0.45f),
                    radius = width * 0.45f
                ),
                center = Offset(width * 0.25f, height * 0.45f),
                radius = width * 0.45f
            )
            // Partner Coral Ambient Aura (Right)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(partnerGlowColor, Color.Transparent),
                    center = Offset(width * 0.75f, height * 0.45f),
                    radius = width * 0.45f
                ),
                center = Offset(width * 0.75f, height * 0.45f),
                radius = width * 0.45f
            )
        }
        content()
    }
}
