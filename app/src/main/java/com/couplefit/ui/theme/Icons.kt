package com.couplefit.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Custom code-drawn vector icons for CoupleFit to avoid using any external SVGs.
 */
object CoupleFitIcons {

    val Heart = ImageVector.Builder(
        name = "Heart",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1.0f,
            stroke = null,
            strokeAlpha = 1.0f,
            strokeLineWidth = 1.0f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Miter,
            strokeLineMiter = 1.0f,
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(12f, 21.35f)
            lineTo(10.55f, 20.03f)
            curveTo(5.4f, 15.36f, 2f, 12.28f, 2f, 8.5f)
            curveTo(2f, 5.42f, 4.42f, 3f, 7.5f, 3f)
            curveTo(9.24f, 3f, 10.91f, 3.81f, 12f, 5.09f)
            curveTo(13.09f, 3.81f, 14.76f, 3f, 16.5f, 3f)
            curveTo(19.58f, 3f, 22f, 5.42f, 22f, 8.5f)
            curveTo(22f, 12.28f, 18.6f, 15.36f, 13.46f, 20.04f)
            lineTo(12f, 21.35f)
            close()
        }
    }.build()

    val Footsteps = ImageVector.Builder(
        name = "Footsteps",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = SolidColor(Color.Black),
            pathFillType = PathFillType.NonZero
        ) {
            // Simplified custom footprint path to represent steps
            moveTo(6.5f, 11.5f)
            curveTo(8.0f, 11.5f, 9.0f, 10.0f, 9.0f, 8.5f)
            curveTo(9.0f, 6.0f, 7.0f, 4.0f, 5.0f, 4.0f)
            curveTo(3.0f, 4.0f, 2.0f, 6.0f, 2.0f, 8.0f)
            curveTo(2.0f, 10.0f, 4.5f, 11.5f, 6.5f, 11.5f)
            close()
            
            moveTo(17.5f, 20.0f)
            curveTo(19.0f, 20.0f, 20.0f, 18.5f, 20.0f, 17.0f)
            curveTo(20.0f, 14.5f, 18.0f, 12.5f, 16.0f, 12.5f)
            curveTo(14.0f, 12.5f, 13.0f, 14.5f, 13.0f, 16.5f)
            curveTo(13.0f, 18.5f, 15.5f, 20.0f, 17.5f, 20.0f)
            close()
        }
    }.build()
}

