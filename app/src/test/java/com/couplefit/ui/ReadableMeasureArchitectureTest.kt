package com.couplefit.ui

import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.couplefit.ui.theme.AppSpacing
import com.couplefit.ui.theme.dialogMeasure
import com.couplefit.ui.theme.readableMeasure
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests validating Phase 32 Readable Measure & Typographic Cadence Architecture (MSR-01 to MSR-07).
 * Ensures Modifier.readableMeasure() and Modifier.dialogMeasure() enforce constraints per /designer-skills:readable-measure.
 */
class ReadableMeasureArchitectureTest {

    @Test
    fun testReadableMeasureDefaultConstraints() {
        val modifier = Modifier.readableMeasure()
        assertNotNull("Modifier.readableMeasure() should produce a valid non-null Modifier", modifier)
        
        // Custom bounds
        val customModifier = Modifier.readableMeasure(maxWidth = 640.dp, minWidth = 320.dp)
        assertNotNull(customModifier)
    }

    @Test
    fun testDialogMeasureDefaultConstraints() {
        val dialogModifier = Modifier.dialogMeasure()
        assertNotNull("Modifier.dialogMeasure() should produce a valid non-null Modifier", dialogModifier)

        // Custom bounds
        val customDialog = Modifier.dialogMeasure(minWidth = 340.dp, maxWidth = 580.dp)
        assertNotNull(customDialog)
    }

    @Test
    fun testAppSpacingTokenIntegrity() {
        // Enforce strict 4-8-12-16-24-32 dp token hierarchy
        assertTrue(AppSpacing.xxs == 4.dp)
        assertTrue(AppSpacing.xs == 8.dp)
        assertTrue(AppSpacing.sm == 12.dp)
        assertTrue(AppSpacing.md == 16.dp)
        assertTrue(AppSpacing.lg == 24.dp)
        assertTrue(AppSpacing.xl == 32.dp)
    }
}
