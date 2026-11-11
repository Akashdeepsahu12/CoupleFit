package com.couplefit.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.couplefit.R

// Neue Helvetica / Helvetica Neue Typography Definition
val NeueHelveticaFontFamily = FontFamily(
    Font(resId = R.font.helvetica_neue, weight = FontWeight.Normal),
    Font(resId = R.font.helvetica_neue, weight = FontWeight.Medium),
    Font(resId = R.font.helvetica_neue_bold, weight = FontWeight.SemiBold),
    Font(resId = R.font.helvetica_neue_bold, weight = FontWeight.Bold),
    Font(resId = R.font.helvetica_neue_bold, weight = FontWeight.ExtraBold)
)

// Backward-compatible alias for any references
val InterFontFamily = NeueHelveticaFontFamily

// Hero Metric Display typography role (TYP-01)
val HeroMetricDisplay = TextStyle(
    fontFamily = NeueHelveticaFontFamily,
    fontWeight = FontWeight.ExtraBold,
    fontSize = 52.sp,
    lineHeight = 56.sp,
    letterSpacing = (-1.0).sp,
    fontFeatureSettings = "tnum"
)

// Global typography scale using Neue Helvetica across all Material 3 roles
val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = NeueHelveticaFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 38.sp,
        lineHeight = 44.sp,
        letterSpacing = (-0.5).sp,
        fontFeatureSettings = "tnum" // Tabular numbers prevent digit wobble (TST-01)
    ),
    displayMedium = TextStyle(
        fontFamily = NeueHelveticaFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 38.sp,
        letterSpacing = (-0.4).sp,
        fontFeatureSettings = "tnum"
    ),
    displaySmall = TextStyle(
        fontFamily = NeueHelveticaFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.2).sp,
        fontFeatureSettings = "tnum"
    ),
    headlineLarge = TextStyle(
        fontFamily = NeueHelveticaFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.2).sp,
        fontFeatureSettings = "tnum"
    ),
    headlineMedium = TextStyle(
        fontFamily = NeueHelveticaFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp,
        fontFeatureSettings = "tnum"
    ),
    headlineSmall = TextStyle(
        fontFamily = NeueHelveticaFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 21.sp,
        lineHeight = 27.sp,
        letterSpacing = (-0.1).sp,
        fontFeatureSettings = "tnum"
    ),
    titleLarge = TextStyle(
        fontFamily = NeueHelveticaFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 19.sp,
        lineHeight = 25.sp,
        letterSpacing = 0.15.sp
    ),
    titleMedium = TextStyle(
        fontFamily = NeueHelveticaFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.1.sp
    ),
    titleSmall = TextStyle(
        fontFamily = NeueHelveticaFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = NeueHelveticaFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        lineHeight = 23.sp,
        letterSpacing = 0.25.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = NeueHelveticaFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = NeueHelveticaFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.4.sp
    ),
    labelLarge = TextStyle(
        fontFamily = NeueHelveticaFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = NeueHelveticaFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.3.sp
    ),
    labelSmall = TextStyle(
        fontFamily = NeueHelveticaFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.8.sp // Outdoor sunlight tracking (TYP-03)
    )
)

/**
 * CompositionLocal providing active adaptive HeroMetricDisplay.
 */
val LocalHeroMetricDisplay = compositionLocalOf { HeroMetricDisplay }

/**
 * Standard semantic ScreenTitle role (AUD-P2-03).
 */
val MaterialTheme.screenTitle: TextStyle
    @Composable
    @ReadOnlyComposable
    get() = typography.headlineSmall

/**
 * Convenience accessor for adaptive HeroMetricDisplay from MaterialTheme.
 */
val MaterialTheme.heroMetricDisplay: TextStyle
    @Composable
    @ReadOnlyComposable
    get() = LocalHeroMetricDisplay.current

/**
 * Calculates adaptive typography scale factor based on screen width dp:
 * - < 360dp: 0.90f (Compact narrow, prevents clipping/overflow)
 * - 360dp..599dp: 1.00f (Compact standard mobile)
 * - 600dp..839dp: 1.10f (Medium, foldables & portrait tablets)
 * - >= 840dp: 1.20f (Expanded, landscape tablets & large viewports)
 */
fun getAdaptiveTypographyScale(screenWidthDp: Int): Float = when {
    screenWidthDp < 360 -> 0.90f
    screenWidthDp < 600 -> 1.00f
    screenWidthDp < 840 -> 1.10f
    else -> 1.20f
}

/**
 * Returns an adaptive Typography instance scaled proportionally for the given screen width.
 */
fun getAdaptiveTypography(screenWidthDp: Int): Typography {
    val scale = getAdaptiveTypographyScale(screenWidthDp)
    if (scale == 1.0f) return Typography

    fun TextStyle.scale(): TextStyle = this.copy(
        fontSize = (this.fontSize.value * scale).sp,
        lineHeight = (this.lineHeight.value * scale).sp
    )

    return Typography(
        displayLarge = Typography.displayLarge.scale(),
        displayMedium = Typography.displayMedium.scale(),
        displaySmall = Typography.displaySmall.scale(),
        headlineLarge = Typography.headlineLarge.scale(),
        headlineMedium = Typography.headlineMedium.scale(),
        headlineSmall = Typography.headlineSmall.scale(),
        titleLarge = Typography.titleLarge.scale(),
        titleMedium = Typography.titleMedium.scale(),
        titleSmall = Typography.titleSmall.scale(),
        bodyLarge = Typography.bodyLarge.scale(),
        bodyMedium = Typography.bodyMedium.scale(),
        bodySmall = Typography.bodySmall.scale(),
        labelLarge = Typography.labelLarge.scale(),
        labelMedium = Typography.labelMedium.scale(),
        labelSmall = Typography.labelSmall.scale()
    )
}

/**
 * Returns an adaptive HeroMetricDisplay scaled proportionally for the given screen width.
 */
fun getAdaptiveHeroMetric(screenWidthDp: Int): TextStyle {
    val scale = getAdaptiveTypographyScale(screenWidthDp)
    if (scale == 1.0f) return HeroMetricDisplay
    return HeroMetricDisplay.copy(
        fontSize = (HeroMetricDisplay.fontSize.value * scale).sp,
        lineHeight = (HeroMetricDisplay.lineHeight.value * scale).sp
    )
}

/**
 * Composable remember helper that recalculates Typography whenever configuration screen width changes.
 */
@Composable
fun rememberAdaptiveTypography(): Typography {
    val configuration = LocalConfiguration.current
    return remember(configuration.screenWidthDp) {
        getAdaptiveTypography(configuration.screenWidthDp)
    }
}

/**
 * Composable remember helper that recalculates HeroMetricDisplay whenever configuration screen width changes.
 */
@Composable
fun rememberAdaptiveHeroMetric(): TextStyle {
    val configuration = LocalConfiguration.current
    return remember(configuration.screenWidthDp) {
        getAdaptiveHeroMetric(configuration.screenWidthDp)
    }
}

