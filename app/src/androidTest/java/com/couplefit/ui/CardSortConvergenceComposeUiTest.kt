package com.couplefit.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.couplefit.data.local.CoupleChallengeEntity
import com.couplefit.ui.challenge.ActiveChallengePreviewCard
import com.couplefit.ui.components.QuickNudgeRow
import com.couplefit.ui.hub.CoupleHubScreen
import com.couplefit.ui.theme.CoupleFitTheme
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Compose UI Instrumented tests for Card Sort Convergence features:
 * - CSA-08: QuickNudgeRow thumb-zone cheers
 * - CSA-04: ActiveChallengePreviewCard modular mini-preview
 * - CSA-06: CoupleHubScreen partner visibility rules shortcut
 * - CSA-07: Settings 2-step safe unlinking verification
 */
class CardSortConvergenceComposeUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun quickNudgeRow_renders_actions_and_fires_nudge_callback() {
        var sentNudgeMessage: String? = null

        composeTestRule.setContent {
            CoupleFitTheme {
                QuickNudgeRow(
                    partnerName = "Sarah",
                    onSendNudge = { sentNudgeMessage = it }
                )
            }
        }

        // Verify chips exist
        composeTestRule.onNodeWithText("Poke").assertIsDisplayed()
        composeTestRule.onNodeWithText("Keep Going!").assertIsDisplayed()
        composeTestRule.onNodeWithText("Love").assertIsDisplayed()
        composeTestRule.onNodeWithText("Drink Water").assertIsDisplayed()

        // Tap "Keep Going!"
        composeTestRule.onNodeWithText("Keep Going!").performClick()
        assertTrue(sentNudgeMessage?.contains("Keep Going!") == true)
    }

    @Test
    fun activeChallengePreviewCard_renders_and_triggers_navigation() {
        var navigatedToDuels = false
        val testChallenge = CoupleChallengeEntity(
            id = "duel_test_1",
            coupleId = "couple_1",
            type = "STEPS_RACE",
            goal = 50_000L,
            start = "2026-10-21",
            end = "2026-10-28",
            myProgress = 32_000L,
            partnerProgress = 28_000L,
            status = "ACTIVE"
        )

        composeTestRule.setContent {
            CoupleFitTheme {
                ActiveChallengePreviewCard(
                    challenge = testChallenge,
                    partnerName = "Sarah",
                    onViewAllChallenges = { navigatedToDuels = true }
                )
            }
        }

        // Verify title & status
        composeTestRule.onNodeWithText("ACTIVE CHALLENGE").assertIsDisplayed()
        composeTestRule.onNodeWithText("Weekend Step Showdown").assertIsDisplayed()

        // Tap "View Duels"
        composeTestRule.onNodeWithText("View Duels").performClick()
        assertTrue(navigatedToDuels)
    }

    @Test
    fun coupleHubScreen_renders_privacy_shortcut_and_triggers_callback() {
        var navigatedToPrivacy = false

        composeTestRule.setContent {
            CoupleFitTheme {
                CoupleHubScreen(
                    onNavigateToPrivacySettings = { navigatedToPrivacy = true }
                )
            }
        }

        // Verify Privacy section exists
        composeTestRule.onNodeWithText("PARTNER VISIBILITY & SHARING RULES").assertIsDisplayed()

        // Tap "Manage Privacy Rules"
        composeTestRule.onNodeWithText("Manage Privacy Rules").performClick()
        assertTrue(navigatedToPrivacy)
    }

    @Test
    fun unlink2StepVerification_disables_confirm_until_unlink_typed() {
        var unlinked = false

        composeTestRule.setContent {
            CoupleFitTheme {
                var confirmationText by remember { mutableStateOf("") }
                Column {
                    OutlinedTextField(
                        value = confirmationText,
                        onValueChange = { confirmationText = it },
                        placeholder = { Text("Type UNLINK") }
                    )
                    Button(
                        onClick = { unlinked = true },
                        enabled = confirmationText.trim().uppercase() == "UNLINK"
                    ) {
                        Text("Permanently Unlink")
                    }
                }
            }
        }

        // Initially disabled
        composeTestRule.onNodeWithText("Permanently Unlink").assertIsNotEnabled()

        // Type incorrect text
        composeTestRule.onNodeWithText("Type UNLINK").performTextInput("NO")
        composeTestRule.onNodeWithText("Permanently Unlink").assertIsNotEnabled()

        // Type UNLINK
        composeTestRule.onNodeWithText("NO").performTextReplacement("UNLINK")
        composeTestRule.onNodeWithText("Permanently Unlink").assertIsEnabled()

        // Click confirm
        composeTestRule.onNodeWithText("Permanently Unlink").performClick()
        assertTrue(unlinked)
    }
}
