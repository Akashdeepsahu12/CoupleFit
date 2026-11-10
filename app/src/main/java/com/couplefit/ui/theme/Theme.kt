package com.couplefit.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = SystemBlueDark,
    onPrimary = TextPrimaryDark,
    secondary = StepsAccent,
    onSecondary = TextPrimaryDark,
    tertiary = MotionPartnerStart, // Vibrant Coral for Partner (DSN-01)
    background = BackgroundDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceElevatedDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = BorderSubtleDark,
    error = SystemRedDark,
    onError = TextPrimaryDark
)

private val LightColorScheme = lightColorScheme(
    primary = SystemBlueLight,
    onPrimary = SurfaceLight,
    secondary = StepsAccent,
    onSecondary = SurfaceLight,
    tertiary = MotionPartnerStart, // Vibrant Coral for Partner (DSN-01)
    background = BackgroundLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceElevatedLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = BorderSubtleLight,
    error = SystemRedLight,
    onError = SurfaceLight
)

@Composable
fun CoupleFitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            if (Build.VERSION.SDK_INT < 35) {
                @Suppress("DEPRECATION")
                window.statusBarColor = androidx.compose.ui.graphics.Color.Transparent.toArgb()
                @Suppress("DEPRECATION")
                window.navigationBarColor = androidx.compose.ui.graphics.Color.Transparent.toArgb()
            }
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    val adaptiveTypography = rememberAdaptiveTypography()
    val adaptiveHeroMetric = rememberAdaptiveHeroMetric()

    CompositionLocalProvider(
        LocalHeroMetricDisplay provides adaptiveHeroMetric
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = adaptiveTypography,
            shapes = Shapes,
            content = content
        )
    }
}
