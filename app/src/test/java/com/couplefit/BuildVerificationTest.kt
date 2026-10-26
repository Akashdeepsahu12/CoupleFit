package com.couplefit

import com.couplefit.ui.navigation.CoupleFitDestination
import com.couplefit.ui.navigation.TopLevelDestination
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class BuildVerificationTest {

    private fun resolveProjectFile(path: String): File {
        val candidates = listOf(
            File(path),
            File("..", path),
            File("../..", path)
        )
        return candidates.firstOrNull { it.exists() }
            ?: throw AssertionError("Could not find file '$path'. Checked paths: ${candidates.map { it.absolutePath }}")
    }

    @Test
    fun testVersionCatalogExistsAndContainsModernDependencies() {
        val catalogFile = resolveProjectFile("gradle/libs.versions.toml")
        val content = catalogFile.readText()

        // Core modern versions
        assertTrue("libs.versions.toml must declare AGP 8.7.2", content.contains("agp = \"8.7.2\""))
        assertTrue("libs.versions.toml must declare Kotlin 2.0.21", content.contains("kotlin = \"2.0.21\""))
        assertTrue("libs.versions.toml must declare KSP 2.0.21", content.contains("ksp = \"2.0.21-1.0.28\""))
        assertTrue("libs.versions.toml must declare compose-compiler plugin", content.contains("org.jetbrains.kotlin.plugin.compose"))

        // Stable Health Connect
        assertTrue("libs.versions.toml must use stable Health Connect 1.1.0", content.contains("health-connect = \"1.1.0\""))
        assertFalse("libs.versions.toml must not use alpha for Health Connect", content.contains("health-connect = \"1.1.0-alpha"))

        // Required new modern libraries
        assertTrue("libs.versions.toml must declare Timber", content.contains("com.jakewharton.timber"))
        assertTrue("libs.versions.toml must declare Navigation Compose", content.contains("androidx.navigation"))
        assertTrue("libs.versions.toml must declare Kotlinx Serialization", content.contains("kotlinx-serialization"))
        assertTrue("libs.versions.toml must declare DataStore Preferences", content.contains("androidx.datastore"))
        assertTrue("libs.versions.toml must declare Coil", content.contains("io.coil-kt"))

        // KSP for Room and Hilt
        assertTrue("libs.versions.toml must declare Hilt", content.contains("com.google.dagger"))
        assertTrue("libs.versions.toml must declare Room", content.contains("androidx.room"))
    }

    @Test
    fun testAppBuildScriptModernization() {
        val appBuildFile = resolveProjectFile("app/build.gradle.kts")
        val content = appBuildFile.readText()

        // Target and Compile SDK
        assertTrue("app must set compileSdk to 35", content.contains("compileSdk = 35"))
        assertTrue("app must set targetSdk to 35", content.contains("targetSdk = 35"))
        assertTrue("app must keep minSdk at 29", content.contains("minSdk = 29"))

        // Compose Compiler Gradle Plugin
        assertTrue("app must apply compose.compiler plugin", content.contains("alias(libs.plugins.compose.compiler)"))
        assertFalse("app must NOT use legacy composeOptions block", content.contains("composeOptions {"))
        assertFalse("app must NOT use kotlinCompilerExtensionVersion", content.contains("kotlinCompilerExtensionVersion"))

        // KSP instead of kapt
        assertFalse("app build.gradle.kts must not invoke kapt", content.contains("kapt("))
        assertFalse("app build.gradle.kts must not apply kapt plugin", content.contains("kapt"))
        assertTrue("app build.gradle.kts must invoke ksp for Hilt", content.contains("ksp(libs.hilt.compiler)"))
        assertTrue("app build.gradle.kts must invoke ksp for Room", content.contains("ksp(libs.room.compiler)"))
    }

    @Test
    fun testTypeSafeNavigationDestinations() {
        // Core 4 Card Sort Validated Pillars (CSA-01)
        val today = CoupleFitDestination.Today
        val challenges = CoupleFitDestination.Challenges
        val healthTrends = CoupleFitDestination.HealthTrends
        val coupleHub = CoupleFitDestination.CoupleHub

        assertNotNull(today)
        assertNotNull(challenges)
        assertNotNull(healthTrends)
        assertNotNull(coupleHub)

        // Legacy and Utility Destinations
        val dashboard = CoupleFitDestination.Dashboard
        val settings = CoupleFitDestination.Settings
        val healthConnect = CoupleFitDestination.HealthConnect
        val challengeDetail = CoupleFitDestination.ChallengeDetail(challengeId = "chal_99")

        assertNotNull(dashboard)
        assertNotNull(settings)
        assertNotNull(healthConnect)
        assertEquals("chal_99", challengeDetail.challengeId)

        // Top Level Destinations Metadata (CSA-01 & CSA-02)
        assertEquals(4, TopLevelDestination.entries.size)
        assertEquals(CoupleFitDestination.Today, TopLevelDestination.TODAY.route)
        assertEquals(CoupleFitDestination.Challenges, TopLevelDestination.CHALLENGES.route)
        assertEquals(CoupleFitDestination.HealthTrends, TopLevelDestination.HEALTH_TRENDS.route)
        assertEquals(CoupleFitDestination.CoupleHub, TopLevelDestination.COUPLE_HUB.route)
    }
}
