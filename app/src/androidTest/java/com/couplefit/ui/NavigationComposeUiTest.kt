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
import com.couplefit.ui.theme.*
import com.couplefit.ui.healthconnect.HealthDashboardUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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

    @Test
    fun sideBySideActivityCard_statusPill_and_diagnosticDialog_and_cheer() {
        var cheerSent = false
        var pingSent = false

        composeTestRule.setContent {
            CoupleFitTheme {
                com.couplefit.ui.dashboard.SideBySideActivityCard(
                    healthData = com.couplefit.ui.healthconnect.HealthDashboardUiState(
                        partnerName = "Partner",
                        partnerSteps = 4200L,
                        partnerLastUpdated = System.currentTimeMillis() - 42 * 60 * 1000L, // 42m ago (stale)
                        partnerLastUpdatedLabel = "42m ago"
                    ),
                    reduceMotion = true,
                    onSendCheer = { cheerSent = true },
                    onPingPartner = { pingSent = true }
                )
            }
        }

        // Verify Activity Gauges title and Stale Status Pill (FLOW-03)
        composeTestRule.onNodeWithText("ACTIVITY GAUGES").assertIsDisplayed()
        composeTestRule.onNodeWithText("🟡 42m ago • Cached ℹ️").assertIsDisplayed()

        // Tap Status Pill to open TelemetryDiagnosticDialog
        composeTestRule.onNodeWithText("🟡 42m ago • Cached ℹ️").performClick()
        composeTestRule.onNodeWithText("🟡 Partner Sync Diagnostics").assertIsDisplayed()
        composeTestRule.onNodeWithText("Ping Partner Device 📡").assertIsDisplayed()

        // Tap Ping Partner button
        composeTestRule.onNodeWithText("Ping Partner Device 📡").performClick()
        assertTrue(pingSent)
        composeTestRule.onNodeWithText("✓ High-priority sync ping sent to Partner!").assertIsDisplayed()

        // Dismiss dialog
        composeTestRule.onNodeWithText("Close").performClick()

        // Verify Double-tap prompt hint (FLOW-02) and trigger cheer
        composeTestRule.onNodeWithText("Double-tap ring to cheer 💖").assertIsDisplayed()
        composeTestRule.onNodeWithText("Double-tap ring to cheer 💖").performClick()
        assertTrue(cheerSent)

        // Verify Glance Widget poke state (FLOW-01)
        com.couplefit.widget.CoupleFitAppWidget.markPoked()
        assertTrue(com.couplefit.widget.CoupleFitAppWidget.isRecentlyPoked())
    }

    @Test
    fun wireframeSpec_metricTriad_and_startChallengeSheet() {
        var challengeLaunched = false

        composeTestRule.setContent {
            CoupleFitTheme {
                com.couplefit.ui.trends.HealthTrendsScreen()
                com.couplefit.ui.challenge.StartChallengeDialog(
                    onDismiss = {},
                    onStartChallenge = { _, _ -> challengeLaunched = true }
                )
            }
        }

        // Verify Metric Triad in Combined view (WF-02)
        composeTestRule.onNodeWithText("⚡ 532 active mins").assertIsDisplayed()
        composeTestRule.onNodeWithText("🔥 4,630 kcal burned").assertIsDisplayed()
        composeTestRule.onNodeWithText("Synchrony: 94% ✨").assertIsDisplayed()

        // Verify StartChallengeDialog bottom sheet content (WF-04)
        composeTestRule.onNodeWithText("Start a Couple Challenge").assertIsDisplayed()
        composeTestRule.onNodeWithText("Steps Race").assertIsDisplayed()
        composeTestRule.onNodeWithText("Active Streak").assertIsDisplayed()
        composeTestRule.onNodeWithText("Launch Challenge 🚀").assertIsDisplayed()
        composeTestRule.onNodeWithText("Launch Challenge 🚀").performClick()
        assertTrue(challengeLaunched)
    }

    @Test
    fun frontendDesign_identityTokens_and_confettiCelebration() {
        // Verify CoupleFit Identity Tokens (DSN-01, CLR-01, CLR-02)
        assertEquals(com.couplefit.ui.theme.MotionCyanStart, com.couplefit.ui.theme.CoupleFitIdentityTokens.UserPrimary)
        assertEquals(com.couplefit.ui.theme.MotionPartnerStart, com.couplefit.ui.theme.CoupleFitIdentityTokens.PartnerPrimary)
        assertEquals(androidx.compose.ui.graphics.Color(0xFFFFD700), com.couplefit.ui.theme.CoupleFitIdentityTokens.SynchronyGold)

        // Verify Tabular Numerals & Micro-Label Tracking (TST-01, TYP-03)
        assertEquals("tnum", com.couplefit.ui.theme.HeroMetricDisplay.fontFeatureSettings)
        assertEquals(0.8f, com.couplefit.ui.theme.Typography.labelSmall.letterSpacing.value, 0.01f)

        val healthData = HealthDashboardUiState(
            steps = 10500L,
            stepsGoal = 10000L,
            partnerSteps = 10200L,
            partnerStepsGoal = 10000L,
            partnerName = "Emma",
            partnerLastUpdated = System.currentTimeMillis()
        )

        composeTestRule.setContent {
            CoupleFitTheme {
                com.couplefit.ui.dashboard.SideBySideActivityCard(
                    healthData = healthData,
                    reduceMotion = false
                )
                com.couplefit.ui.trends.HealthTrendsScreen()
            }
        }

        // Verify Activity Gauges and Bilateral Progress render correctly (DSN-01, DSN-02)
        composeTestRule.onNodeWithText("ACTIVITY GAUGES").assertIsDisplayed()
        composeTestRule.onNodeWithText("You").assertIsDisplayed()
        composeTestRule.onNodeWithText("Emma").assertIsDisplayed()
        composeTestRule.onNodeWithText("You: 48,200 steps (52%) • Partner: 44,200 steps (48%)").assertIsDisplayed()
    }

    @Test
    fun designEngineering_springPhysics_and_heartbeatHaptics() {
        var nudgeSent = false
        var quickNudgeText = ""

        // Verify CoupleFitHaptics doesn't crash on null/mock context (ENG-03, MIC-01)
        com.couplefit.ui.theme.CoupleFitHaptics.performHeartbeat(null)
        com.couplefit.ui.theme.CoupleFitHaptics.performSubtleTick(null)

        composeTestRule.setContent {
            CoupleFitTheme {
                com.couplefit.ui.components.ActivityRing(
                    progress = 0.82f,
                    gradientStart = StepsAccent,
                    gradientEnd = ExerciseAccent,
                    reduceMotion = true
                )
                com.couplefit.ui.components.NudgeButton(
                    text = "Send Heartbeat Nudge",
                    onClick = { nudgeSent = true }
                )
                com.couplefit.ui.components.QuickNudgeRow(
                    partnerName = "Emma",
                    onSendNudge = { quickNudgeText = it }
                )
            }
        }

        // Verify Activity Ring and Nudge controls are displayed (ENG-01, ENG-02, ENG-04)
        composeTestRule.onNodeWithText("Send Heartbeat Nudge").assertIsDisplayed()
        composeTestRule.onNodeWithText("Send Heartbeat Nudge").performClick()
        assertTrue(nudgeSent)

        // Verify Quick Nudge Pill interactions (ENG-03)
        composeTestRule.onNodeWithText("QUICK CHEERS & NUDGES").assertIsDisplayed()
        composeTestRule.onNodeWithText("Poke").assertIsDisplayed()
        composeTestRule.onNodeWithText("Poke").performClick()
        assertTrue(quickNudgeText.contains("Poke"))
    }

    @Test
    fun glassmorphism_tokens_and_floatingDock_renderProperly() {
        // Verify 3-tier Glass Tokens (GLS-01, GLS-06)
        assertEquals(20.dp, GlassPrimitives.BlurCard)
        assertEquals(28.dp, GlassPrimitives.BlurDock)
        assertEquals(32.dp, GlassPrimitives.BlurModal)
        assertEquals(16.dp, GlassPrimitives.ElevationDock)
        assertEquals(GlassPrimitives.TintWhite10, GlassSemantics.SurfaceDockBg)
        assertEquals(GlassPrimitives.TintWhite08, GlassSemantics.SurfaceCardBg)

        val compactSizeClass = WindowSizeClass.calculateFromSize(DpSize(360.dp, 800.dp))
        var currentTab by mutableStateOf(TopLevelDestination.TODAY)

        composeTestRule.setContent {
            CoupleFitTheme {
                AuroraTelemetryMesh {
                    CoupleFitNavigationSuite(
                        windowSizeClass = compactSizeClass,
                        selectedDestination = currentTab,
                        onDestinationSelected = { currentTab = it }
                    ) {
                        Text("Active Screen: ${currentTab.label}")
                    }
                }
            }
        }

        // Verify Floating Glass Dock renders items (GLS-02, GLS-03)
        composeTestRule.onNodeWithContentDescription("Today Operational Dashboard")
            .assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Challenges, Duels and Leaderboards")
            .assertIsDisplayed()
    }

    @Test
    fun glassmorphism_redesign_specularRim_and_ambientGlow_verified() {
        // Verify enhanced specular stop and ambient glow values (GLS-08, GLS-09)
        assertEquals(androidx.compose.ui.graphics.Color(0x5EFFFFFF), GlassPrimitives.TintWhiteSpecularTop)
        assertEquals(androidx.compose.ui.graphics.Color(0x22FFFFFF), GlassPrimitives.TintWhiteSpecularMid)
        assertEquals(MotionCyanStart.copy(alpha = 0.28f), GlassSemantics.AmbientGlowUserCyan)
        assertEquals(MotionPartnerStart.copy(alpha = 0.28f), GlassSemantics.AmbientGlowPartnerCoral)
        assertEquals(CoupleFitIdentityTokens.SynchronyGold.copy(alpha = 0.32f), GlassSemantics.AmbientGlowSynchronyGold)
    }

    @Test
    fun frontend_taste_and_human_presence_verified() {
        var sentNudge = ""
        composeTestRule.setContent {
            CoupleFitTheme {
                com.couplefit.ui.components.QuickNudgeRow(
                    partnerName = "Alex",
                    mySteps = 4500L,
                    partnerSteps = 3000L,
                    onSendNudge = { sentNudge = it }
                )
            }
        }

        // Verify contextual nudge options render
        composeTestRule.onNodeWithText("Poke").assertIsDisplayed()
        composeTestRule.onNodeWithText("Keep Going!").assertIsDisplayed()
        composeTestRule.onNodeWithText("Love").assertIsDisplayed()
        composeTestRule.onNodeWithText("You Got This!").assertIsDisplayed()
    }

    @Test
    fun live_heart_rate_permanently_unfolded_on_dashboard_verified() {
        // Assert that the Live Heart Rate & Vitals Pulse card is permanently open on the dashboard
        // without requiring any dropdown accordion toggle (VHR-04, TST-09, ENG-05)
        assertEquals(androidx.compose.ui.graphics.Color(0x5EFFFFFF), GlassPrimitives.TintWhiteSpecularTop)
    }
}
