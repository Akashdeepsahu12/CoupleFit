package com.couplefit.ui

import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.couplefit.ui.components.MedicationDuePill
import com.couplefit.ui.theme.CoupleFitTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Compose UI Instrumented tests for Contextual Medication Due Pill (CSA-03).
 * Validates rendering, semantic trees, logging callback, and dismiss callback.
 */
class MedicationDuePillComposeUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun medicationDuePill_renders_with_scheduled_dose_info() {
        composeTestRule.setContent {
            CoupleFitTheme {
                MedicationDuePill(
                    medicationName = "Vitamin D3",
                    scheduledTime = "08:00 AM"
                )
            }
        }

        // Verify scheduled text is displayed
        composeTestRule.onNodeWithText("08:00 AM • Vitamin D3 Due")
            .assertIsDisplayed()

        // Verify Log action button is displayed with correct accessibility description
        composeTestRule.onNodeWithContentDescription("Log Vitamin D3 dose as taken")
            .assertIsDisplayed()

        // Verify dismiss button is displayed
        composeTestRule.onNodeWithContentDescription("Dismiss medication reminder")
            .assertIsDisplayed()
    }

    @Test
    fun medicationDuePill_tapLog_firesCallback_and_updatesUI() {
        var logFired = false

        composeTestRule.setContent {
            CoupleFitTheme {
                MedicationDuePill(
                    medicationName = "Omega-3",
                    scheduledTime = "01:00 PM",
                    onLogDose = { logFired = true }
                )
            }
        }

        // Tap Log button
        composeTestRule.onNodeWithContentDescription("Log Omega-3 dose as taken")
            .performClick()

        // Assert callback fired
        assertTrue("Expected onLogDose callback to be invoked", logFired)

        // Assert UI changes to confirmation state
        composeTestRule.onNodeWithText("Dose Logged!")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Done ✓")
            .assertIsDisplayed()
    }

    @Test
    fun medicationDuePill_tapDismiss_firesCallback() {
        var dismissFired = false

        composeTestRule.setContent {
            CoupleFitTheme {
                MedicationDuePill(
                    medicationName = "Magnesium",
                    scheduledTime = "10:00 PM",
                    onDismiss = { dismissFired = true }
                )
            }
        }

        // Tap dismiss button
        composeTestRule.onNodeWithContentDescription("Dismiss medication reminder")
            .performClick()

        // Assert callback fired
        assertTrue("Expected onDismiss callback to be invoked", dismissFired)
    }
}
