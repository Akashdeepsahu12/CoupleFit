package com.couplefit.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.couplefit.data.local.MetricSharingPreferences
import com.couplefit.data.model.UserSession
import com.couplefit.ui.auth.AuthUiState
import com.couplefit.ui.settings.*
import com.couplefit.ui.theme.CoupleFitTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Instrumented UI tests for IA-03, IA-04, and IA-10:
 * - 4-Hub segmented navigation bar renders all pills and switches active hub.
 * - Hub 1: Partnership & Account details, linked state, unlinking and signout actions.
 * - Hub 2: Health Sync & Sharing, sync frequency, granular metric sharing switches.
 * - Hub 3: App Experience, dark mode, reduce motion, notifications.
 * - Hub 4: Privacy & Safety, biometric lock, privacy transparency navigation, data actions.
 */
class SettingsHubComposeUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun settingsHubBar_renders_all_four_pills_and_handles_selection() {
        var selectedHub by mutableStateOf(SettingsHub.PARTNERSHIP)

        composeTestRule.setContent {
            CoupleFitTheme {
                SettingsHubBar(
                    activeHub = selectedHub,
                    onHubSelected = { selectedHub = it }
                )
            }
        }

        // Verify all 4 tabs exist
        composeTestRule.onNodeWithText("Partnership").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sync & Sharing").assertIsDisplayed()
        composeTestRule.onNodeWithText("Experience").assertIsDisplayed()
        composeTestRule.onNodeWithText("Privacy & Safety").assertIsDisplayed()

        // Switch to Sync & Sharing
        composeTestRule.onNodeWithText("Sync & Sharing").performClick()
        assertEquals(SettingsHub.HEALTH_SYNC, selectedHub)

        // Switch to Experience
        composeTestRule.onNodeWithText("Experience").performClick()
        assertEquals(SettingsHub.EXPERIENCE, selectedHub)

        // Switch to Privacy & Safety
        composeTestRule.onNodeWithText("Privacy & Safety").performClick()
        assertEquals(SettingsHub.PRIVACY, selectedHub)

        // Switch back to Partnership
        composeTestRule.onNodeWithText("Partnership").performClick()
        assertEquals(SettingsHub.PARTNERSHIP, selectedHub)
    }

    @Test
    fun partnershipHub_renders_user_profile_and_triggers_unlink_modal() {
        var unlinkClicked = false
        var signOutClicked = false

        val testUiState = AuthUiState(
            userSession = UserSession(
                uid = "test_user_1",
                displayName = "Alex Morgan",
                email = "alex@couplefit.app",
                coupleId = "couple_101",
                partnerId = "partner_202"
            )
        )

        composeTestRule.setContent {
            CoupleFitTheme {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    PartnershipHubContent(
                        authUiState = testUiState,
                        partnerName = "Taylor Jordan",
                        isPartnerLinked = true,
                        onUnlinkClicked = { unlinkClicked = true },
                        onSignOutClicked = { signOutClicked = true }
                    )
                }
            }
        }

        // Verify user profile info
        composeTestRule.onNodeWithText("Alex Morgan").assertIsDisplayed()
        composeTestRule.onNodeWithText("alex@couplefit.app").assertIsDisplayed()

        // Verify partner connection status
        composeTestRule.onNodeWithText("Taylor Jordan (Connected)").assertIsDisplayed()

        // Verify unlinking trigger
        composeTestRule.onNodeWithText("Unlink Partner").performClick()
        assertTrue(unlinkClicked)

        // Verify sign out trigger
        composeTestRule.onNodeWithText("Sign Out").performClick()
        assertTrue(signOutClicked)
    }

    @Test
    fun healthSyncHub_renders_sharing_preferences_and_toggles() {
        var toggledStepsValue: Boolean? = null
        var toggledWorkoutsValue: Boolean? = null
        var syncFrequencyValue: String? = null

        val testSharingPrefs = MetricSharingPreferences(
            shareSteps = true,
            shareWorkouts = false,
            shareCalories = true,
            shareHr = true,
            shareSpo2 = false
        )

        composeTestRule.setContent {
            CoupleFitTheme {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    HealthSyncHubContent(
                        hasPermissions = true,
                        onManagePermissions = {},
                        isSamsungConnected = false,
                        onToggleSamsung = {},
                        syncFrequency = "Hourly",
                        onSyncFrequencyChange = { syncFrequencyValue = it },
                        sharingPrefs = testSharingPrefs,
                        onToggleSteps = { toggledStepsValue = it },
                        onToggleWorkouts = { toggledWorkoutsValue = it },
                        onToggleCalories = {},
                        onToggleHr = {},
                        onToggleSpo2 = {}
                    )
                }
            }
        }

        // Verify section titles
        composeTestRule.onNodeWithText("HEALTH CONNECT").assertIsDisplayed()
        composeTestRule.onNodeWithText("Connected & Active").assertIsDisplayed()
        composeTestRule.onNodeWithText("BACKGROUND SYNC CADENCE").assertIsDisplayed()
        composeTestRule.onNodeWithText("GRANULAR PARTNER SHARING").assertIsDisplayed()

        // Verify sharing metric items
        composeTestRule.onNodeWithText("Share Daily Steps").assertIsDisplayed()
        composeTestRule.onNodeWithText("Share Workout Sessions").assertIsDisplayed()
        composeTestRule.onNodeWithText("Share Active Calories").assertIsDisplayed()
        composeTestRule.onNodeWithText("Share Resting Heart Rate").assertIsDisplayed()
        composeTestRule.onNodeWithText("Share Blood Oxygen (SpO2)").assertIsDisplayed()

        // Change sync frequency
        composeTestRule.onNodeWithText("Every 15m").performClick()
        assertEquals("Every 15m", syncFrequencyValue)
    }

    @Test
    fun experienceHub_renders_appearance_and_notifications() {
        var toggledDarkTheme: Boolean? = null
        var toggledMotion: Boolean? = null

        composeTestRule.setContent {
            CoupleFitTheme {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    ExperienceHubContent(
                        isDarkTheme = true,
                        onToggleDarkTheme = { toggledDarkTheme = it },
                        onToggleDarkThemeWithOrigin = null,
                        reduceMotion = false,
                        onToggleReduceMotion = { toggledMotion = it },
                        notifyCritical = true,
                        onToggleNotifyCritical = {},
                        notifyNudges = true,
                        onToggleNotifyNudges = {},
                        notifySummary = true,
                        onToggleNotifySummary = {},
                        quietHoursEnabled = false,
                        onToggleQuietHours = {},
                        useMetric = true,
                        onToggleUseMetric = {}
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("APPEARANCE").assertIsDisplayed()
        composeTestRule.onNodeWithText("Dark Theme").assertIsDisplayed()
        composeTestRule.onNodeWithText("Reduce Motion").assertIsDisplayed()
        composeTestRule.onNodeWithText("UNITS OF MEASURE").assertIsDisplayed()
        composeTestRule.onNodeWithText("NOTIFICATIONS").assertIsDisplayed()
    }

    @Test
    fun privacyHub_renders_lock_transparency_and_data_controls() {
        var openedTransparency = false
        var deleteAccountClicked = false

        composeTestRule.setContent {
            CoupleFitTheme {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    PrivacyHubContent(
                        biometricLock = false,
                        onToggleBiometricLock = {},
                        onOpenTransparency = { openedTransparency = true },
                        onExportData = {},
                        onDeleteAccountClicked = { deleteAccountClicked = true }
                    )
                }
            }
        }

        // Verify privacy items
        composeTestRule.onNodeWithText("ACCESS PROTECTION").assertIsDisplayed()
        composeTestRule.onNodeWithText("Biometric App Lock").assertIsDisplayed()
        composeTestRule.onNodeWithText("AUDIT & TRANSPARENCY").assertIsDisplayed()
        composeTestRule.onNodeWithText("Privacy Transparency Center").assertIsDisplayed()
        composeTestRule.onNodeWithText("DATA MANAGEMENT").assertIsDisplayed()
        composeTestRule.onNodeWithText("Export Health Telemetry (JSON)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Delete Account & Data").assertIsDisplayed()

        // Click Transparency Center
        composeTestRule.onNodeWithText("Privacy Transparency Center").performClick()
        assertTrue(openedTransparency)

        // Click Delete Account
        composeTestRule.onNodeWithText("Delete Account & Data").performClick()
        assertTrue(deleteAccountClicked)
    }
}
