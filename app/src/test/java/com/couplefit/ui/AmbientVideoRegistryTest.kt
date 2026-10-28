package com.couplefit.ui

import com.couplefit.R
import com.couplefit.ui.components.AmbientVideoAsset
import com.couplefit.ui.components.AmbientVideoRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * Unit tests for [AmbientVideoRegistry] and dynamic background shuffle logic (BGV-04).
 */
class AmbientVideoRegistryTest {

    @Test
    fun registryContainsRequiredInitialPool() {
        val pool = AmbientVideoRegistry.activeShufflePool
        assertEquals(2, pool.size)

        val ids = pool.map { it.id }
        assertTrue("Pool should contain green1", ids.contains("green1"))
        assertTrue("Pool should contain blue", ids.contains("blue"))

        assertEquals(R.raw.green1, AmbientVideoRegistry.GREEN1.resId)
        assertEquals(R.raw.blue, AmbientVideoRegistry.BLUE.resId)
    }

    @Test
    fun getById_returnsCorrectAsset_andFallsBackGracefully() {
        assertEquals("green1", AmbientVideoRegistry.getById("green1").id)
        assertEquals("blue", AmbientVideoRegistry.getById("blue").id)
        assertEquals("green", AmbientVideoRegistry.getById("green").id)

        // Case-insensitivity
        assertEquals("blue", AmbientVideoRegistry.getById("BLUE").id)
        assertEquals("green1", AmbientVideoRegistry.getById("GrEeN1").id)

        // Unknown / null fallbacks to defaultAsset
        assertEquals(AmbientVideoRegistry.defaultAsset, AmbientVideoRegistry.getById("non_existent_id"))
        assertEquals(AmbientVideoRegistry.defaultAsset, AmbientVideoRegistry.getById(null))
        assertEquals(AmbientVideoRegistry.defaultAsset, AmbientVideoRegistry.getById(""))
    }

    @Test
    fun selectShuffledBackground_alternatesNonRepeating_whenTwoAssetsInPool() {
        // When last asset was green1, shuffle MUST pick blue
        val nextAfterGreen = AmbientVideoRegistry.selectShuffledBackground(
            lastSelectedId = "green1",
            pool = AmbientVideoRegistry.activeShufflePool
        )
        assertEquals("blue", nextAfterGreen.id)

        // When last asset was blue, shuffle MUST pick green1
        val nextAfterBlue = AmbientVideoRegistry.selectShuffledBackground(
            lastSelectedId = "blue",
            pool = AmbientVideoRegistry.activeShufflePool
        )
        assertEquals("green1", nextAfterBlue.id)
    }

    @Test
    fun selectShuffledBackground_handlesNullOrEmptyPoolSafely() {
        // Null lastSelectedId picks an item from active pool
        val selected = AmbientVideoRegistry.selectShuffledBackground(lastSelectedId = null)
        assertTrue(
            AmbientVideoRegistry.activeShufflePool.map { it.id }.contains(selected.id)
        )

        // Empty pool fallback
        val fromEmpty = AmbientVideoRegistry.selectShuffledBackground(pool = emptyList())
        assertEquals(AmbientVideoRegistry.defaultAsset, fromEmpty)

        // 1-item pool returns that single item even if it matches lastSelectedId
        val singleItem = AmbientVideoRegistry.GREEN1
        val fromSingle = AmbientVideoRegistry.selectShuffledBackground(
            lastSelectedId = "green1",
            pool = listOf(singleItem)
        )
        assertEquals(singleItem, fromSingle)
    }

    @Test
    fun selectShuffledBackground_respectsRandomSeed() {
        val customPool = listOf(
            AmbientVideoAsset("a", 1, "A", "", androidx.compose.ui.graphics.Color.Black),
            AmbientVideoAsset("b", 2, "B", "", androidx.compose.ui.graphics.Color.Black),
            AmbientVideoAsset("c", 3, "C", "", androidx.compose.ui.graphics.Color.Black)
        )

        // With seed 42, deterministic pseudo-random choice
        val choice1 = AmbientVideoRegistry.selectShuffledBackground(
            lastSelectedId = "a",
            pool = customPool,
            randomSeed = Random(42)
        )
        assertNotEquals("a", choice1.id)
    }
}
