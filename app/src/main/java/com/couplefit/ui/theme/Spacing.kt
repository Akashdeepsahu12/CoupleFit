package com.couplefit.ui.theme

import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Single consistent spacing token scale used app-wide.
 * Strictly 4 / 8 / 12 / 16 / 24 / 32 dp.
 */
object AppSpacing {
    val xxs: Dp = 4.dp
    val xs: Dp = 8.dp
    val sm: Dp = 12.dp
    val md: Dp = 16.dp
    val lg: Dp = 24.dp
    val xl: Dp = 32.dp
}

/**
 * Constrains text reading containers to the optimal typographic measure of 45–75 characters
 * per line (~600dp–640dp) on tablets, foldables, and desktop/DeX displays.
 *
 * Adheres strictly to `/designer-skills:readable-measure`:
 * - Sustained reading & transparency copy: 55–70 CPL (~600dp)
 * - UI cards & settings: 45–65 CPL (~520dp)
 * - Excludes short labels and micro-badges
 */
fun Modifier.readableMeasure(
    maxWidth: Dp = 620.dp,
    minWidth: Dp = Dp.Unspecified
): Modifier = this.widthIn(min = minWidth, max = maxWidth)

/**
 * Standardizes dialog and modal sheet measures, enforcing a minimum width of 320dp
 * to prevent choked measure (<35 CPL) and a maximum of 500dp to avoid sprawl on wide viewports.
 */
fun Modifier.dialogMeasure(
    minWidth: Dp = 320.dp,
    maxWidth: Dp = 500.dp
): Modifier = this.widthIn(min = minWidth, max = maxWidth)


