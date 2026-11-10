package com.couplefit.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.couplefit.ui.theme.*
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/**
 * Deterministic Test Suite verifying WCAG 2.2 Level AA / AAA text contrast ratios
 * for all semantic tokens in [Color.kt] across dark and light palettes (Phase 60: TXT-01, TXT-02).
 */
class TextColorContrastTest {

    private fun linearizeComponent(c: Float): Double {
        return if (c <= 0.04045f) {
            c / 12.92
        } else {
            ((c + 0.055) / 1.055).pow(2.4)
        }
    }

    private fun calculateRelativeLuminance(color: Color): Double {
        val r = linearizeComponent(color.red)
        val g = linearizeComponent(color.green)
        val b = linearizeComponent(color.blue)
        return 0.2126 * r + 0.7152 * g + 0.0722 * b
    }

    /**
     * Blends a foreground color with alpha onto an opaque background color.
     */
    private fun compositeOver(fg: Color, bg: Color): Color {
        val alpha = fg.alpha
        val r = fg.red * alpha + bg.red * (1f - alpha)
        val g = fg.green * alpha + bg.green * (1f - alpha)
        val b = fg.blue * alpha + bg.blue * (1f - alpha)
        return Color(r, g, b, 1f)
    }

    private fun calculateContrastRatio(fg: Color, bg: Color): Double {
        val effectiveFg = if (fg.alpha < 1f) compositeOver(fg, bg) else fg
        val l1 = calculateRelativeLuminance(effectiveFg)
        val l2 = calculateRelativeLuminance(bg)
        val lighter = max(l1, l2)
        val darker = min(l1, l2)
        return (lighter + 0.05) / (darker + 0.05)
    }

    @Test
    fun `dark mode primary text exceeds 12 to 1 contrast on dark surface`() {
        val contrast = calculateContrastRatio(TextPrimaryDark, SurfaceDark)
        assertTrue("Dark primary text contrast was $contrast, expected >= 12.0", contrast >= 12.0)
    }

    @Test
    fun `dark mode secondary text exceeds 7 to 1 contrast on dark surface`() {
        val contrast = calculateContrastRatio(TextSecondaryDark, SurfaceDark)
        assertTrue("Dark secondary text contrast was $contrast, expected >= 7.0 (WCAG AAA)", contrast >= 7.0)
    }

    @Test
    fun `dark mode tertiary text exceeds 4_5 to 1 contrast on dark surface`() {
        val contrast = calculateContrastRatio(TextTertiaryDark, SurfaceDark)
        assertTrue("Dark tertiary text contrast was $contrast, expected >= 4.5 (WCAG AA)", contrast >= 4.5)
    }

    @Test
    fun `light mode primary text exceeds 12 to 1 contrast on light surface`() {
        val contrast = calculateContrastRatio(TextPrimaryLight, SurfaceLight)
        assertTrue("Light primary text contrast was $contrast, expected >= 12.0", contrast >= 12.0)
    }

    @Test
    fun `light mode secondary text exceeds 7 to 1 contrast on light surface`() {
        val contrast = calculateContrastRatio(TextSecondaryLight, SurfaceLight)
        assertTrue("Light secondary text contrast was $contrast, expected >= 7.0 (WCAG AAA)", contrast >= 7.0)
    }

    @Test
    fun `light mode tertiary text exceeds 4_5 to 1 contrast on light surface`() {
        val contrast = calculateContrastRatio(TextTertiaryLight, SurfaceLight)
        assertTrue("Light tertiary text contrast was $contrast, expected >= 4.5 (WCAG AA)", contrast >= 4.5)
    }

    @Test
    fun `user identity tokens satisfy WCAG contrast on accents`() {
        val userContrast = calculateContrastRatio(CoupleFitIdentityTokens.OnUserAccent, CoupleFitIdentityTokens.UserPrimary)
        assertTrue("User identity text contrast was $userContrast, expected >= 7.0 (WCAG AAA)", userContrast >= 7.0)

        val partnerContrast = calculateContrastRatio(CoupleFitIdentityTokens.OnPartnerAccent, CoupleFitIdentityTokens.PartnerPrimary)
        assertTrue("Partner identity text contrast was $partnerContrast, expected >= 3.0 (WCAG AA Large/Bold)", partnerContrast >= 3.0)
    }
}
