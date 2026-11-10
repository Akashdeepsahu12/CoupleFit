package com.couplefit.ui.ui

import androidx.compose.ui.text.font.FontWeight
import com.couplefit.ui.theme.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AdaptiveTypographyTest {

    @Test
    fun testAdaptiveScaleRungs() {
        // Compact Narrow (<360dp)
        assertEquals(0.90f, getAdaptiveTypographyScale(320), 0.001f)
        assertEquals(0.90f, getAdaptiveTypographyScale(359), 0.001f)

        // Compact Standard (360dp..599dp)
        assertEquals(1.00f, getAdaptiveTypographyScale(360), 0.001f)
        assertEquals(1.00f, getAdaptiveTypographyScale(412), 0.001f)
        assertEquals(1.00f, getAdaptiveTypographyScale(599), 0.001f)

        // Medium / Foldable / Tablet Portrait (600dp..839dp)
        assertEquals(1.10f, getAdaptiveTypographyScale(600), 0.001f)
        assertEquals(1.10f, getAdaptiveTypographyScale(720), 0.001f)
        assertEquals(1.10f, getAdaptiveTypographyScale(839), 0.001f)

        // Expanded / Large Tablet / Landscape (>=840dp)
        assertEquals(1.20f, getAdaptiveTypographyScale(840), 0.001f)
        assertEquals(1.20f, getAdaptiveTypographyScale(1024), 0.001f)
    }

    @Test
    fun testAdaptiveTypographyMonotonicScaling() {
        val narrow = getAdaptiveTypography(320)
        val standard = getAdaptiveTypography(412)
        val medium = getAdaptiveTypography(720)
        val expanded = getAdaptiveTypography(1024)

        // Check DisplayLarge scales monotonically
        assertTrue(narrow.displayLarge.fontSize.value < standard.displayLarge.fontSize.value)
        assertTrue(standard.displayLarge.fontSize.value < medium.displayLarge.fontSize.value)
        assertTrue(medium.displayLarge.fontSize.value < expanded.displayLarge.fontSize.value)

        // Check TitleLarge scales monotonically
        assertTrue(narrow.titleLarge.fontSize.value < standard.titleLarge.fontSize.value)
        assertTrue(standard.titleLarge.fontSize.value < medium.titleLarge.fontSize.value)
        assertTrue(medium.titleLarge.fontSize.value < expanded.titleLarge.fontSize.value)

        // Check BodyMedium scales monotonically
        assertTrue(narrow.bodyMedium.fontSize.value < standard.bodyMedium.fontSize.value)
        assertTrue(standard.bodyMedium.fontSize.value < medium.bodyMedium.fontSize.value)
        assertTrue(medium.bodyMedium.fontSize.value < expanded.bodyMedium.fontSize.value)
    }

    @Test
    fun testTabularNumeralsPreservedAcrossScales() {
        val narrow = getAdaptiveTypography(320)
        val expanded = getAdaptiveTypography(1024)

        assertEquals("tnum", narrow.displayLarge.fontFeatureSettings)
        assertEquals("tnum", narrow.headlineLarge.fontFeatureSettings)
        assertEquals("tnum", expanded.displayLarge.fontFeatureSettings)
        assertEquals("tnum", expanded.headlineLarge.fontFeatureSettings)
    }

    @Test
    fun testAdaptiveHeroMetricScaling() {
        val narrowHero = getAdaptiveHeroMetric(320)
        val standardHero = getAdaptiveHeroMetric(412)
        val mediumHero = getAdaptiveHeroMetric(720)
        val expandedHero = getAdaptiveHeroMetric(1024)

        assertEquals(FontWeight.ExtraBold, standardHero.fontWeight)
        assertEquals("tnum", standardHero.fontFeatureSettings)

        // Standard is 52.sp
        assertEquals(52.0f, standardHero.fontSize.value, 0.001f)

        // Narrow is 52 * 0.9 = 46.8f
        assertEquals(46.8f, narrowHero.fontSize.value, 0.01f)

        // Medium is 52 * 1.1 = 57.2f
        assertEquals(57.2f, mediumHero.fontSize.value, 0.01f)

        // Expanded is 52 * 1.2 = 62.4f
        assertEquals(62.4f, expandedHero.fontSize.value, 0.01f)
    }
}
