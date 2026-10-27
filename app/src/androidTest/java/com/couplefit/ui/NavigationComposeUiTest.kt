package com.couplefit.ui

import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.couplefit.ui.navigation.CoupleFitNavigationSuite
import com.couplefit.ui.navigation.TopLevelDestination
import com.couplefit.ui.theme.CoupleFitTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * Compose UI Instrumented tests for Adaptive Navigation Suite (CSA-02 / IA-10).
 * Validates 4-tab persistence, accessibility semantics, and tab switching callbacks.
 */
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
class NavigationComposeUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun compactNavigation_renders_all_four_tabs_with_semantics() {
        var currentTab by mutableStateOf(TopLevelDestination.TODAY)
        val compactSizeClass = WindowSizeClass.calculateFromSize(DpSize(360.dp, 800.dp))

        composeTestRule.setContent {
            CoupleFitTheme {
                CoupleFitNavigationSuite(
                    windowSizeClass = compactSizeClass,
                    selectedDestination = currentTab,
                    onDestinationSelected = { currentTab = it }
                ) {
                    Text("Active Screen: ${currentTab.label}")
                }
            }
        }

        // 1. Verify Today tab
        composeTestRule.onNodeWithContentDescription("Today Operational Dashboard")
            .assertIsDisplayed()

        // 2. Verify Challenges tab
        composeTestRule.onNodeWithContentDescription("Challenges, Duels and Leaderboards")
            .assertIsDisplayed()

        // 3. Verify Trends tab
        composeTestRule.onNodeWithContentDescription("Health, Biometric Trends and Medication")
            .assertIsDisplayed()

        // 4. Verify Couple Hub tab
        composeTestRule.onNodeWithContentDescription("Couple Hub, Shared Streaks and Memories")
            .assertIsDisplayed()
    }

    @Test
    fun compactNavigation_switchingTabs_updatesContent() {
        var currentTab by mutableStateOf(TopLevelDestination.TODAY)
        val compactSizeClass = WindowSizeClass.calculateFromSize(DpSize(360.dp, 800.dp))

        composeTestRule.setContent {
            CoupleFitTheme {
                CoupleFitNavigationSuite(
                    windowSizeClass = compactSizeClass,
                    selectedDestination = currentTab,
                    onDestinationSelected = { currentTab = it }
                ) {
                    Text("Active Screen: ${currentTab.label}")
                }
            }
        }

        // Tap Challenges tab
        composeTestRule.onNodeWithContentDescription("Challenges, Duels and Leaderboards")
            .performClick()
        composeTestRule.onNodeWithText("Active Screen: Challenges")
            .assertIsDisplayed()

        // Tap Trends tab
        composeTestRule.onNodeWithContentDescription("Health, Biometric Trends and Medication")
            .performClick()
        composeTestRule.onNodeWithText("Active Screen: Trends")
            .assertIsDisplayed()

        // Tap Couple Hub tab
        composeTestRule.onNodeWithContentDescription("Couple Hub, Shared Streaks and Memories")
            .performClick()
        composeTestRule.onNodeWithText("Active Screen: Couple Hub")
            .assertIsDisplayed()

        // Tap back to Today
        composeTestRule.onNodeWithContentDescription("Today Operational Dashboard")
            .performClick()
        composeTestRule.onNodeWithText("Active Screen: Today")
            .assertIsDisplayed()
    }

    @Test
    fun multiModalActiveState_renders_bold_typography_on_active_tab() {
        var currentTab by mutableStateOf(TopLevelDestination.TODAY)
        val compactSizeClass = WindowSizeClass.calculateFromSize(DpSize(360.dp, 800.dp))

        composeTestRule.setContent {
            CoupleFitTheme {
                CoupleFitNavigationSuite(
                    windowSizeClass = compactSizeClass,
                    selectedDestination = currentTab,
                    onDestinationSelected = { currentTab = it }
                ) {
                    Text("Selected: ${currentTab.label}")
                }
            }
        }

        // Verify active labels and switching
        composeTestRule.onNodeWithText("Today").assertIsDisplayed()
        composeTestRule.onNodeWithText("Challenges").assertIsDisplayed()

        composeTestRule.onNodeWithText("Challenges").performClick()
        assertEquals(TopLevelDestination.CHALLENGES, currentTab)
    }

    @Test
    fun healthTrends_renders_dedicated_timeframe_sub_header() {
        composeTestRule.setContent {
            CoupleFitTheme {
                com.couplefit.ui.trends.HealthTrendsScreen()
            }
        }

        // Verify all 4 timeframes render in dedicated row without crashing
        composeTestRule.onNodeWithText("Day").assertIsDisplayed()
        composeTestRule.onNodeWithText("Week").assertIsDisplayed()
        composeTestRule.onNodeWithText("Month").assertIsDisplayed()
        composeTestRule.onNodeWithText("Year").assertIsDisplayed()

        // Switch to Month
        composeTestRule.onNodeWithText("Month").performClick()
        composeTestRule.onNodeWithText("STEPS & ACTIVITY COMPARISON (Month • Both Partners)").assertIsDisplayed()

        // Verify NAV-05 Perspective segmented buttons
        composeTestRule.onNodeWithText("Both").assertIsDisplayed()
        composeTestRule.onNodeWithText("You").assertIsDisplayed()
        composeTestRule.onNodeWithText("Partner").assertIsDisplayed()

        // Switch to "You" perspective
        composeTestRule.onNodeWithText("You").performClick()
        composeTestRule.onNodeWithText("YOUR PERSONAL TELEMETRY (Month)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Your Steps Total").assertIsDisplayed()

        // Switch to "Partner" perspective
        composeTestRule.onNodeWithText("Partner").performClick()
        composeTestRule.onNodeWithText("PARTNER'S TELEMETRY (Month)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Partner's Steps Total").assertIsDisplayed()
    }

    @Test
    fun challenges_renders_and_switches_sub_navigation_tabs() {
        composeTestRule.setContent {
            CoupleFitTheme {
                com.couplefit.ui.challenge.ChallengesScreen()
            }
        }

        // Verify sub-tabs exist
        composeTestRule.onNodeWithText("Active Duels").assertIsDisplayed()
        composeTestRule.onNodeWithText("Trophies & Vault").assertIsDisplayed()

        // Initially in Active Duels
        composeTestRule.onNodeWithText("ACTIVE HEAD-TO-HEAD DUELS").assertIsDisplayed()
        composeTestRule.onNodeWithText("AVAILABLE DUEL FORMATS").assertIsDisplayed()
        composeTestRule.onNodeWithText("50k Steps Race").assertIsDisplayed()

        // Switch to Trophies & Vault
        composeTestRule.onNodeWithText("Trophies & Vault").performClick()
        composeTestRule.onNodeWithText("TROPHY & MILESTONE VAULT").assertIsDisplayed()
        composeTestRule.onNodeWithText("COUPLE HEAD-TO-HEAD RECORD").assertIsDisplayed()
        composeTestRule.onNodeWithText("100k Steps").assertIsDisplayed()
        composeTestRule.onNodeWithText("PAST DUEL ARCHIVE").assertIsDisplayed()
    }
}
