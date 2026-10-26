package com.couplefit.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable

/**
 * Type-Safe Route Destinations using Navigation Compose 2.8+ and Kotlinx Serialization.
 *
 * Designed and validated against the 4 core pillars established in `card-sort-analysis.md`
 * and `INFORMATION_ARCHITECTURE.md` (CSA-01).
 */
sealed interface CoupleFitDestination {

    /**
     * Tab 1: Operational epicenter featuring dual activity rings, partner status,
     * quick nudges, live vitals, and contextual pills.
     */
    @Serializable
    data object Today : CoupleFitDestination

    /**
     * Tab 2: Gamification pillar featuring active/past duels, step races,
     * couple leaderboards, and the trophy/milestone vault.
     */
    @Serializable
    data object Challenges : CoupleFitDestination

    /**
     * Tab 3: Deep analytics telemetry featuring multi-horizon historical charts,
     * medication tracker, and sensor diagnostics.
     */
    @Serializable
    data object HealthTrends : CoupleFitDestination

    /**
     * Tab 4: Emotional relationship anchor featuring shared streaks,
     * anniversary countdown, partner profile, and quick privacy link.
     */
    @Serializable
    data object CoupleHub : CoupleFitDestination

    /**
     * Administrative & Configuration modal sheet/screen.
     */
    @Serializable
    data object Settings : CoupleFitDestination

    /**
     * Health Connect permission and sync diagnostic destination.
     */
    @Serializable
    data object HealthConnect : CoupleFitDestination

    /**
     * Detailed view for an individual head-to-head duel or step race.
     */
    @Serializable
    data class ChallengeDetail(val challengeId: String) : CoupleFitDestination

    /**
     * Legacy destination alias for [Today].
     * Retained to preserve backward compatibility with existing tests and call sites.
     */
    @Deprecated("Use Today instead", ReplaceWith("Today"))
    @Serializable
    data object Dashboard : CoupleFitDestination
}

/**
 * UI Metadata specification for the 4 core persistent top-level navigation tabs
 * validated by Card Sort Analysis and Information Architecture (CSA-01 & CSA-02).
 */
enum class TopLevelDestination(
    val route: CoupleFitDestination,
    val icon: ImageVector,
    val label: String,
    val contentDescription: String
) {
    TODAY(
        route = CoupleFitDestination.Today,
        icon = Icons.Default.Home,
        label = "Today",
        contentDescription = "Today Operational Dashboard"
    ),
    CHALLENGES(
        route = CoupleFitDestination.Challenges,
        icon = Icons.Default.Star,
        label = "Challenges",
        contentDescription = "Challenges, Duels and Leaderboards"
    ),
    HEALTH_TRENDS(
        route = CoupleFitDestination.HealthTrends,
        icon = Icons.Default.DateRange,
        label = "Trends",
        contentDescription = "Health, Biometric Trends and Medication"
    ),
    COUPLE_HUB(
        route = CoupleFitDestination.CoupleHub,
        icon = Icons.Default.Favorite,
        label = "Couple Hub",
        contentDescription = "Couple Hub, Shared Streaks and Memories"
    );

    companion object {
        val allDestinations = entries
    }
}
