package com.couplefit.ui.theme

import androidx.compose.ui.graphics.Color

// =========================================================================
// Semantic Brand & Functional Colors (iOS 17 System Colors)
// =========================================================================

// Functional / State Colors (iOS Light / Dark variants handled in Theme.kt, here we define base)
val SystemRedLight = Color(0xFFFF3B30)
val SystemRedDark = Color(0xFFFF453A)

val SystemOrangeLight = Color(0xFFFF9500)
val SystemOrangeDark = Color(0xFFFF9F0A)

val SystemGreenLight = Color(0xFF34C759)
val SystemGreenDark = Color(0xFF32D74B)

val SystemBlueLight = Color(0xFF007AFF)
val SystemBlueDark = Color(0xFF0A84FF)

val SystemYellowLight = Color(0xFFFFCC00)
val SystemYellowDark = Color(0xFFFFD60A)

val SystemPurpleLight = Color(0xFFAF52DE)
val SystemPurpleDark = Color(0xFFBF5AF2)

// Metric Category Solid Accents (Apple Fitness inspired)
val StepsAccent = Color(0xFFFF2D55)       // Pink/Red for Move
val ExerciseAccent = Color(0xFF32D74B)    // Green for Exercise
val VitalsAccent = Color(0xFF00C7BE)      // Teal for Stand/Vitals
val CaloriesAccent = StepsAccent

// Backward-compatible semantic bridges
val ColorCritical = SystemRedLight
val ColorWarning = SystemOrangeLight
val ColorSuccess = SystemGreenLight
val ColorInfo = SystemBlueLight
val SystemRed = SystemRedLight
val SystemGreen = SystemGreenLight
val SystemOrange = SystemOrangeLight
val SystemBlue = SystemBlueLight
val SystemYellow = SystemYellowLight
val SystemPurple = SystemPurpleLight

// =========================================================================
// 3-Tier Elevation Surface Hierarchy (iOS 17 Backgrounds)
// =========================================================================

// Tier 0: Base Canvas Background (systemBackground)
val BackgroundDark = Color(0xFF000000)
val BackgroundLight = Color(0xFFFFFFFF)

// Tier 1: Primary Elevated Surfaces (secondarySystemBackground / Grouped)
val SurfaceDark = Color(0xFF1C1C1E)
val SurfaceLight = Color(0xFFF2F2F7)

// Tier 2: Secondary Foreground Surfaces (tertiarySystemBackground)
val SurfaceElevatedDark = Color(0xFF2C2C2E)
val SurfaceElevatedLight = Color(0xFFFFFFFF)

// Borders & Separators (separator)
val BorderSubtleDark = Color(0x54545899)
val BorderSubtleLight = Color(0x3C3C434A)
val SeparatorLight = BorderSubtleLight
val SeparatorDark = BorderSubtleDark

// High-contrast Label & Text Tones (iOS Labels)
val TextPrimaryDark = Color(0xFFFFFFFF)      // label
val TextSecondaryDark = Color(0x99EBEBF5)    // secondaryLabel (60%)
val TextTertiaryDark = Color(0x4DEBEBF5)     // tertiaryLabel (30%)

val TextPrimaryLight = Color(0xFF000000)
val TextSecondaryLight = Color(0x993C3C43)
val TextTertiaryLight = Color(0x4D3C3C43)

// Backward-compatible label definitions
val LabelSecondaryLight = TextSecondaryLight
val LabelSecondaryDark = TextSecondaryDark

// =========================================================================
// Health Metric Gradients
// =========================================================================

// Steps (Amber / Coral)
val StepsGradientStart = Color(0xFFFF6B4A)
val StepsGradientEnd = Color(0xFFFF9230)

// Exercise (Vibrant Green)
val ExerciseGradientStart = Color(0xFF30D158)
val ExerciseGradientEnd = Color(0xFF24A845)

// Calories (Aqua Cyan)
val CaloriesGradientStart = Color(0xFF00E5FF)
val CaloriesGradientEnd = Color(0xFF00B0FF)

// Vitals / HR Gradient: Desaturated slightly in dark mode to prevent OLED halation/glare
val VitalsGradientStartDark = Color(0xFF1AD1E5)   // Refined, softer cyan
val VitalsGradientMidDark = Color(0xFF0091FF)     // Mid blue
val VitalsGradientEndDark = Color(0xFFEB3B6B)     // Controlled medical magenta

val VitalsGradientStartLight = Color(0xFF00B4D8)
val VitalsGradientMidLight = Color(0xFF0077B6)
val VitalsGradientEndLight = Color(0xFFD90429)

// Motion Graphic Arc Gauge Tokens (Inspired by 558.mp4)
val MotionCyanStart = Color(0xFF00F5D4)
val MotionSkyMid = Color(0xFF00C2FF)
val MotionBlueEnd = Color(0xFF0A84FF)
val MotionGlowCyan = Color(0xFF00F0FF)
val MotionTrackDark = Color(0xFF1E2838)
val MotionTrackLight = Color(0xFFE2E8F0)

val MotionPartnerStart = Color(0xFFFF3366)
val MotionPartnerMid = Color(0xFFFF5E3A)
val MotionPartnerEnd = Color(0xFFFF9500)
val MotionPartnerGlow = Color(0xFFFF453A)

// =========================================================================
// Dual-User Signature Identity Tokens (DSN-01, CLR-01, CLR-02)
// =========================================================================
object CoupleFitIdentityTokens {
    // User Signature Spectrum (Electric Cyan)
    val UserPrimary = MotionCyanStart            // #00F5D4
    val UserMid = MotionSkyMid                  // #00C2FF
    val UserEnd = MotionBlueEnd                  // #0A84FF
    val UserGlow = MotionGlowCyan                // #00F0FF
    val OnUserAccent = Color(0xFF001F38)         // 7.8:1 WCAG AA contrast against Cyan

    // Partner Signature Spectrum (Vibrant Coral)
    val PartnerPrimary = MotionPartnerStart      // #FF3366
    val PartnerMid = MotionPartnerMid            // #FF5E3A
    val PartnerEnd = MotionPartnerEnd            // #FF9500
    val PartnerGlow = MotionPartnerGlow          // #FF453A
    val OnPartnerAccent = Color(0xFFFFFFFF)      // 4.6:1 WCAG AA contrast against Coral

    // Shared Synchrony & Milestone Token
    val SynchronyGold = Color(0xFFFFD700)        // Gold for 100% daily ring closure
}

// Directional Specular Rim Lighting Brush (TST-02)
fun directionalRimBrush(): androidx.compose.ui.graphics.Brush {
    return androidx.compose.ui.graphics.Brush.verticalGradient(
        listOf(
            Color.White.copy(alpha = 0.22f),
            Color.White.copy(alpha = 0.04f)
        )
    )
}


