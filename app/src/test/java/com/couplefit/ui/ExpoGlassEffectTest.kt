package com.couplefit.ui

import com.couplefit.ui.glass.GLASS_EDGE_COLOR
import com.couplefit.ui.glass.GLASS_EDGE_WIDTH
import com.couplefit.ui.glass.GlassColorScheme
import com.couplefit.ui.glass.GlassEffectStyle
import com.couplefit.ui.glass.GlassEffectStyleConfig
import com.couplefit.ui.glass.isGlassEffectAPIAvailable
import com.couplefit.ui.glass.isGlassSupported
import com.couplefit.ui.glass.isLiquidGlassAvailable
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit test suite verifying Expo Glass Effect component architecture and API parity
 * (Phase 48: GLS-09 / EXPO-03).
 */
class ExpoGlassEffectTest {

    @Test
    fun glassEffectStyles_containExpectedExpoVariants() {
        val styles = GlassEffectStyle.values()
        assertEquals(3, styles.size)
        assertNotNull(GlassEffectStyle.valueOf("Regular"))
        assertNotNull(GlassEffectStyle.valueOf("Clear"))
        assertNotNull(GlassEffectStyle.valueOf("None"))
    }

    @Test
    fun glassColorSchemes_containExpectedExpoVariants() {
        val schemes = GlassColorScheme.values()
        assertEquals(3, schemes.size)
        assertNotNull(GlassColorScheme.valueOf("Auto"))
        assertNotNull(GlassColorScheme.valueOf("Light"))
        assertNotNull(GlassColorScheme.valueOf("Dark"))
    }

    @Test
    fun glassEffectStyleConfig_hasSensibleDefaultsAndCustomValues() {
        val defaultConfig = GlassEffectStyleConfig()
        assertEquals(GlassEffectStyle.Regular, defaultConfig.style)
        org.junit.Assert.assertFalse(defaultConfig.animate)
        assertEquals(0.3f, defaultConfig.animationDuration, 0.001f)
        assertEquals(300, defaultConfig.durationMillis)

        val customConfig = GlassEffectStyleConfig(
            style = GlassEffectStyle.Clear,
            animate = true,
            animationDuration = 0.28f
        )
        assertEquals(GlassEffectStyle.Clear, customConfig.style)
        assertTrue(customConfig.animate)
        assertEquals(0.28f, customConfig.animationDuration, 0.001f)
        assertEquals(280, customConfig.durationMillis)
    }

    @Test
    fun runtimeHelpers_matchUnderlyingPlatformSupport() {
        val supported = isGlassSupported()
        assertEquals(supported, isGlassEffectAPIAvailable())
        assertEquals(isGlassEffectAPIAvailable(), isLiquidGlassAvailable())
    }

    @Test
    fun physicalConstants_areWithinSafeOpticalRanges() {
        assertTrue(GLASS_EDGE_WIDTH.value in 0.25f..1.0f)
        assertTrue(GLASS_EDGE_COLOR.alpha in 0.15f..0.35f)
    }
}
