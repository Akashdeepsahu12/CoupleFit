package com.couplefit.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.couplefit.ui.components.MeshGradientPreset
import com.couplefit.ui.components.MeshPoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit test suite for Expo Mesh Gradient specifications and presets (MSH-01).
 */
class MeshGradientTest {

    @Test
    fun testPresetDimensionsMatchGridArea() {
        for (preset in MeshGradientPreset.entries) {
            val expectedSize = preset.columns * preset.rows
            assertEquals(
                "Preset ${preset.name} must have colors.size == columns * rows",
                expectedSize,
                preset.colors.size
            )
            assertTrue("Columns must be >= 2", preset.columns >= 2)
            assertTrue("Rows must be >= 2", preset.rows >= 2)
        }
    }

    @Test
    fun testCoupleFitMidnightSignaturePalette() {
        val midnight = MeshGradientPreset.CoupleFitMidnight
        assertEquals(3, midnight.columns)
        assertEquals(3, midnight.rows)
        assertEquals(9, midnight.colors.size)

        // Verify top-right is Electric Cyan and bottom-left is Vibrant Coral
        val cyanColor = midnight.colors[2]
        val coralColor = midnight.colors[6]

        assertTrue("Cyan green channel must be high", cyanColor.green > 0.8f)
        assertTrue("Cyan blue channel must be high", cyanColor.blue > 0.7f)
        assertTrue("Coral red channel must be high", coralColor.red > 0.8f)
    }

    @Test
    fun testMeshPointToOffsetConversion() {
        val point = MeshPoint(0.5f, 0.75f)
        val offset = point.toOffset()
        assertEquals(0.5f, offset.x, 0.001f)
        assertEquals(0.75f, offset.y, 0.001f)
    }

    @Test
    fun testAllPresetColorsHaveFullAlpha() {
        for (preset in MeshGradientPreset.entries) {
            for ((index, color) in preset.colors.withIndex()) {
                assertEquals(
                    "Color at index $index in preset ${preset.name} should have alpha == 1.0f",
                    1.0f,
                    color.alpha,
                    0.001f
                )
            }
        }
    }
}
