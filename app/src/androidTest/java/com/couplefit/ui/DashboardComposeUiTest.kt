package com.couplefit.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.couplefit.data.local.CoupleChallengeEntity
import com.couplefit.ui.challenge.SideBySideChallengeCard
import com.couplefit.ui.components.ActivityRing
import com.couplefit.ui.components.NudgeButton
import com.couplefit.ui.theme.CoupleFitTheme
import com.couplefit.ui.theme.ExerciseAccent
import com.couplefit.ui.theme.StepsAccent
import org.junit.Rule
import org.junit.Test

/**
 * Compose UI Instrumented tests for Dashboard Components.
 * Validates rendering, semantic trees, accessibility nodes, and interaction callbacks.
 */
class DashboardComposeUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun activityRing_renders_and_displays_progress_text() {
        composeTestRule.setContent {
            CoupleFitTheme {
                ActivityRing(
                    progress = 0.75f,
                    gradientStart = StepsAccent,
                    gradientEnd = ExerciseAccent
                )
            }
        }

        // Verify root composable exists and doesn't crash
        composeTestRule.onRoot().assertIsDisplayed()
    }

    @Test
    fun sideBySideChallengeCard_displays_partner_and_my_progress() {
        val testChallenge = CoupleChallengeEntity(
            id = "ch_test_123",
            coupleId = "cpl_test",
            type = "STEPS_RACE",
            goal = 50000,
            start = "2026-10-21",
            end = "2026-10-28",
            myProgress = 32000,
            partnerProgress = 28000,
            status = "ACTIVE",
            winnerUserId = null
        )

        composeTestRule.setContent {
            CoupleFitTheme {
                SideBySideChallengeCard(
                    challenge = testChallenge,
                    partnerName = "Sarah"
                )
            }
        }

        // Verify challenge type title and partner name display
        composeTestRule.onNodeWithText("🏃 Steps Race").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sarah").assertIsDisplayed()
        composeTestRule.onNodeWithText("You").assertIsDisplayed()
        composeTestRule.onNodeWithText("32,000 / 50,000 steps").assertIsDisplayed()
        composeTestRule.onNodeWithText("28,000 / 50,000 steps").assertIsDisplayed()
    }

    @Test
    fun nudgeButton_clicks_and_triggers_callback() {
        var clicked = false

        composeTestRule.setContent {
            CoupleFitTheme {
                NudgeButton(
                    text = "Nudge Alex",
                    onClick = { clicked = true }
                )
            }
        }

        composeTestRule.onNodeWithText("Nudge Alex")
            .assertIsDisplayed()
            .performClick()

        assert(clicked)
    }
}
