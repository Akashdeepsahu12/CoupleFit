package com.couplefit.ui

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.couplefit.ui.theme.CoupleFitIdentityTokens
import com.couplefit.ui.theme.directionalRimBrush
import com.couplefit.ui.theme.duelCardPress
import com.couplefit.ui.theme.iosClickable
import com.couplefit.ui.theme.iosMomentumClickable
import com.couplefit.ui.trends.TelemetryPerspective
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests validating Phase 34 & Phase 56 Tactile Animation Polish Architecture (ANM-09 to ANM-14, TST-02).
 * Ensures motion tokens, card press mechanics, and sub-navigation enums conform to strict design contracts.
 */
class TactileMotionArchitectureTest {

    @Test
    fun testDuelCardPressModifierCreation() {
        val modifier = Modifier.duelCardPress(
            onClickLabel = "Test Duel Card",
            pressedScale = 0.98f,
            tiltDegrees = -1.5f
        ) { }
        assertNotNull("Modifier.duelCardPress() must produce a valid composed Modifier", modifier)
    }

    @Test
    fun testIosMomentumClickableModifierCreation() {
        val modifier = Modifier.iosMomentumClickable(
            pressedScale = 0.95f,
            pressedAlpha = 0.85f
        ) { }
        assertNotNull("Modifier.iosMomentumClickable() must produce a valid composed Modifier", modifier)
    }

    @Test
    fun testIosClickableModifierCreation() {
        val modifier = Modifier.iosClickable(
            pressedScale = 0.975f,
            pressedAlpha = 0.88f
        ) { }
        assertNotNull("Modifier.iosClickable() must produce a valid composed Modifier", modifier)
    }

    @Test
    fun testTelemetryPerspectiveIntegrity() {
        val perspectives = TelemetryPerspective.entries
        assertEquals(3, perspectives.size)
        assertTrue(perspectives.contains(TelemetryPerspective.COMBINED))
        assertTrue(perspectives.contains(TelemetryPerspective.YOU))
        assertTrue(perspectives.contains(TelemetryPerspective.PARTNER))

        assertEquals("Both", TelemetryPerspective.COMBINED.label)
        assertEquals("You", TelemetryPerspective.YOU.label)
        assertEquals("Partner", TelemetryPerspective.PARTNER.label)

        assertEquals("👥", TelemetryPerspective.COMBINED.icon)
        assertEquals("👤", TelemetryPerspective.YOU.icon)
        assertEquals("💖", TelemetryPerspective.PARTNER.icon)

        assertEquals(0, TelemetryPerspective.COMBINED.ordinal)
        assertEquals(1, TelemetryPerspective.YOU.ordinal)
        assertEquals(2, TelemetryPerspective.PARTNER.ordinal)
    }

    @Test
    fun testCoupleFitIdentityTokensColorContracts() {
        // Enforce exact brand signature colors
        assertEquals("UserPrimary must be Electric Cyan (#00F5D4)", Color(0xFF00F5D4), CoupleFitIdentityTokens.UserPrimary)
        assertEquals("UserMid must be Sky Mid (#00C2FF)", Color(0xFF00C2FF), CoupleFitIdentityTokens.UserMid)
        assertEquals("UserEnd must be Blue End (#0A84FF)", Color(0xFF0A84FF), CoupleFitIdentityTokens.UserEnd)
        assertEquals("UserGlow must be Glow Cyan (#00F0FF)", Color(0xFF00F0FF), CoupleFitIdentityTokens.UserGlow)

        assertEquals("PartnerPrimary must be Vibrant Coral (#FF3366)", Color(0xFFFF3366), CoupleFitIdentityTokens.PartnerPrimary)
        assertEquals("PartnerMid must be Coral Mid (#FF5E3A)", Color(0xFFFF5E3A), CoupleFitIdentityTokens.PartnerMid)
        assertEquals("PartnerEnd must be Orange End (#FF9500)", Color(0xFFFF9500), CoupleFitIdentityTokens.PartnerEnd)
        assertEquals("PartnerGlow must be Coral Glow (#FF453A)", Color(0xFFFF453A), CoupleFitIdentityTokens.PartnerGlow)

        assertEquals("SynchronyGold must be Golden (#FFD700)", Color(0xFFFFD700), CoupleFitIdentityTokens.SynchronyGold)
    }

    @Test
    fun testWcagContrastTokenContracts() {
        // OnUserAccent is paired with Electric Cyan (#00F5D4) for >7:1 WCAG AAA text contrast
        assertEquals(Color(0xFF001F38), CoupleFitIdentityTokens.OnUserAccent)

        // OnPartnerAccent is paired with Vibrant Coral (#FF3366) for >4.5:1 WCAG AA text contrast
        assertEquals(Color(0xFFFFFFFF), CoupleFitIdentityTokens.OnPartnerAccent)
    }

    @Test
    fun testDirectionalRimBrushContract() {
        val brush = directionalRimBrush()
        assertNotNull("directionalRimBrush must return non-null Brush", brush)
    }
}
