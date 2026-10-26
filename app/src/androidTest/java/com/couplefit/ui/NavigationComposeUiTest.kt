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
}
