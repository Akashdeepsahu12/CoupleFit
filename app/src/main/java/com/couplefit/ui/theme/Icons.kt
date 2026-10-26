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
 * Modern stroke-based icons based on Lucide & Heroicons design systems:
 * 24dp viewport, 2dp stroke width, rounded caps and joins.
 * For interactive spring animations, use the matching Animated*Icon composables in com.couplefit.ui.components.
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
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2.0f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(19f, 14f)
            curveTo(20.49f, 12.54f, 22f, 10.79f, 22f, 8.5f)
            curveTo(22f, 5.46f, 19.54f, 3f, 16.5f, 3f)
            curveTo(14.74f, 3f, 13.5f, 3.8f, 12f, 5f)
            curveTo(10.5f, 3.8f, 9.26f, 3f, 7.5f, 3f)
            curveTo(4.46f, 3f, 2f, 5.46f, 2f, 8.5f)
            curveTo(2f, 10.8f, 3.51f, 12.55f, 5f, 14f)
            lineTo(12f, 21f)
            close()
        }
    }.build()

    val Settings = ImageVector.Builder(
        name = "Settings",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        // Outer gear rim with 8 teeth
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2.0f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(12.22f, 2f)
            lineTo(11.78f, 2f)
            curveTo(10.74f, 2f, 9.87f, 2.79f, 9.78f, 3.82f)
            curveTo(9.66f, 5.16f, 8.24f, 6.01f, 7f, 5.42f)
            curveTo(6.05f, 4.96f, 4.91f, 5.37f, 4.45f, 6.32f)
            lineTo(4.23f, 6.7f)
            curveTo(3.77f, 7.65f, 4.04f, 8.81f, 4.88f, 9.44f)
            curveTo(5.97f, 10.26f, 5.97f, 11.89f, 4.88f, 12.71f)
            curveTo(4.04f, 13.34f, 3.77f, 14.5f, 4.23f, 15.45f)
            lineTo(4.45f, 15.83f)
            curveTo(4.91f, 16.78f, 6.05f, 17.19f, 7f, 16.73f)
            curveTo(8.24f, 16.14f, 9.66f, 16.99f, 9.78f, 18.33f)
            curveTo(9.87f, 19.36f, 10.74f, 20.15f, 11.78f, 20.15f)
            lineTo(12.22f, 20.15f)
            curveTo(13.26f, 20.15f, 14.13f, 19.36f, 14.22f, 18.33f)
            curveTo(14.34f, 16.99f, 15.76f, 16.14f, 17f, 16.73f)
            curveTo(17.95f, 17.19f, 19.09f, 16.78f, 19.55f, 15.83f)
            lineTo(19.77f, 15.45f)
            curveTo(20.23f, 14.5f, 19.96f, 13.34f, 19.12f, 12.71f)
            curveTo(18.03f, 11.89f, 18.03f, 10.26f, 19.12f, 9.44f)
            curveTo(19.96f, 8.81f, 20.23f, 7.65f, 19.77f, 6.7f)
            lineTo(19.55f, 6.32f)
            curveTo(19.09f, 5.37f, 17.95f, 4.96f, 17f, 5.42f)
            curveTo(15.76f, 6.01f, 14.34f, 5.16f, 14.22f, 3.82f)
            curveTo(14.13f, 2.79f, 13.26f, 2f, 12.22f, 2f)
            close()
        }
        // Inner circle
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2.0f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(15f, 12f)
            curveTo(15f, 13.66f, 13.66f, 15f, 12f, 15f)
            curveTo(10.34f, 15f, 9f, 13.66f, 9f, 12f)
            curveTo(9f, 10.34f, 10.34f, 9f, 12f, 9f)
            curveTo(13.66f, 9f, 15f, 10.34f, 15f, 12f)
            close()
        }
    }.build()

    val ArrowBack = ImageVector.Builder(
        name = "ArrowBack",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2.0f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            // Shaft
            moveTo(19f, 12f)
            lineTo(5f, 12f)
            // Head
            moveTo(12f, 19f)
            lineTo(5f, 12f)
            lineTo(12f, 5f)
        }
    }.build()

    val WifiOff = ImageVector.Builder(
        name = "WifiOff",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        // Top right wave arc
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2.0f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(10.71f, 5.05f)
            curveTo(14.5f, 5.5f, 19.5f, 6.8f, 22.58f, 9f)
        }
        // Top left wave arc
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2.0f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(1.42f, 9f)
            curveTo(2.8f, 7.8f, 4.4f, 6.8f, 6.12f, 6.12f)
        }
        // Mid wave arc
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2.0f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(8.53f, 16.11f)
            curveTo(9.5f, 15.2f, 10.7f, 14.8f, 12f, 14.8f)
            curveTo(13.3f, 14.8f, 14.5f, 15.2f, 15.48f, 16.11f)
        }
        // Center Dot
        path(
            fill = SolidColor(Color.Black),
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 1.0f
        ) {
            moveTo(13f, 20f)
            curveTo(13f, 20.55f, 12.55f, 21f, 12f, 21f)
            curveTo(11.45f, 21f, 11f, 20.55f, 11f, 20f)
            curveTo(11f, 19.45f, 11.45f, 19f, 12f, 19f)
            curveTo(12.55f, 19f, 13f, 19.45f, 13f, 20f)
            close()
        }
        // Strike Slash
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2.0f,
            strokeLineCap = StrokeCap.Round
        ) {
            moveTo(2f, 2f)
            lineTo(22f, 22f)
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
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2.0f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            // Left Foot
            moveTo(5.5f, 13f)
            curveTo(4f, 13f, 3f, 11f, 3f, 8.5f)
            curveTo(3f, 5.5f, 5f, 3.5f, 7.5f, 3.5f)
            curveTo(8.5f, 3.5f, 9f, 5f, 9f, 7.5f)
            curveTo(9f, 10f, 7f, 13f, 5.5f, 13f)
            close()

            // Right Foot
            moveTo(18.5f, 20.5f)
            curveTo(17f, 20.5f, 15f, 18.5f, 15f, 16f)
            curveTo(15f, 13f, 17f, 11f, 19.5f, 11f)
            curveTo(20.5f, 11f, 21f, 12.5f, 21f, 15f)
            curveTo(21f, 17.5f, 20f, 20.5f, 18.5f, 20.5f)
            close()
        }
    }.build()

    val Activity = ImageVector.Builder(
        name = "Activity",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2.0f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(22f, 12f)
            lineTo(18f, 12f)
            lineTo(15f, 21f)
            lineTo(9f, 3f)
            lineTo(6f, 12f)
            lineTo(2f, 12f)
        }
    }.build()

    val ChevronDown = ImageVector.Builder(
        name = "ChevronDown",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2.0f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(6f, 9f)
            lineTo(12f, 15f)
            lineTo(18f, 9f)
        }
    }.build()

    val ChevronRight = ImageVector.Builder(
        name = "ChevronRight",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2.0f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(9f, 18f)
            lineTo(15f, 12f)
            lineTo(9f, 6f)
        }
    }.build()

    val Check = ImageVector.Builder(
        name = "Check",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2.4f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(4f, 12f)
            lineTo(9f, 17f)
            lineTo(20f, 6f)
        }
    }.build()
}
