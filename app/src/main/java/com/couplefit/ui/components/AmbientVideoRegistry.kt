package com.couplefit.ui.components

import androidx.annotation.RawRes
import androidx.compose.ui.graphics.Color
import com.couplefit.R

/**
 * Metadata descriptor for an ambient video background asset (BGV-04).
 *
 * @property id Unique stable identifier (e.g., "green1", "blue")
 * @property resId Android raw resource pointer (`R.raw.*`)
 * @property displayName Human-readable label (e.g., "Emerald Aurora")
 * @property description Descriptive subtitle detailing visual atmosphere
 * @property dominantAccent Signature color accent for subtle badge/glow harmony
 */
data class AmbientVideoAsset(
    val id: String,
    @RawRes val resId: Int,
    val displayName: String,
    val description: String,
    val dominantAccent: Color
)

/**
 * Extensible Registry and Shuffle Manager for Ambient Video Backgrounds.
 *
 * Provides a decoupled catalog of available background streams, an active shuffle pool,
 * and deterministic non-repeating shuffle selection across app launches.
 */
object AmbientVideoRegistry {

    val GREEN1 = AmbientVideoAsset(
        id = "green1",
        resId = R.raw.green1,
        displayName = "Emerald Aurora",
        description = "Native 9:20 portrait liquid emerald gradient",
        dominantAccent = Color(0xFF00F5D4)
    )

    val BLUE = AmbientVideoAsset(
        id = "blue",
        resId = R.raw.blue,
        displayName = "Sapphire Flow",
        description = "Deep oceanic sapphire ribbons with luminous cyan highlights",
        dominantAccent = Color(0xFF00B0FF)
    )

    val GREEN_CLASSIC = AmbientVideoAsset(
        id = "green",
        resId = R.raw.green,
        displayName = "Classic Aurora",
        description = "Original QHD+ ambient fluid stream",
        dominantAccent = Color(0xFF00E676)
    )

    /**
     * All registered ambient video assets in the app ecosystem.
     */
    val allAssets: List<AmbientVideoAsset> = listOf(
        GREEN1,
        BLUE,
        GREEN_CLASSIC
    )

    /**
     * The active shuffle pool used on app launch.
     * Currently configured with green1 and blue per user request ("till then use this two").
     * When new backgrounds are added, appending them here automatically includes them in shuffle!
     */
    val activeShufflePool: List<AmbientVideoAsset> = listOf(
        GREEN1,
        BLUE
    )

    /**
     * Fallback default asset when no selection is available.
     */
    val defaultAsset: AmbientVideoAsset get() = GREEN1

    /**
     * Looks up an asset by its unique string [id], case-insensitively.
     * Falls back to [defaultAsset] if the ID is unknown or null.
     */
    fun getById(id: String?): AmbientVideoAsset {
        if (id.isNullOrBlank()) return defaultAsset
        return allAssets.find { it.id.equals(id, ignoreCase = true) } ?: defaultAsset
    }

    /**
     * Selects a random background from [pool], excluding [lastSelectedId] if [pool] has > 1 item.
     * Guarantees non-repeating alternation on consecutive launches while preserving complete randomness.
     */
    fun selectShuffledBackground(
        lastSelectedId: String? = null,
        pool: List<AmbientVideoAsset> = activeShufflePool,
        randomSeed: kotlin.random.Random = kotlin.random.Random.Default
    ): AmbientVideoAsset {
        if (pool.isEmpty()) return defaultAsset
        if (pool.size == 1) return pool.first()

        val candidates = pool.filterNot { it.id.equals(lastSelectedId, ignoreCase = true) }
        val selectionPool = if (candidates.isNotEmpty()) candidates else pool
        return selectionPool.random(randomSeed)
    }
}
