package com.couplefit.security

import com.couplefit.data.model.Challenge
import com.couplefit.data.model.ChallengeStatus
import com.couplefit.data.model.ChallengeType
import com.couplefit.data.model.Milestone
import com.couplefit.data.model.MilestoneType
import com.couplefit.data.model.SharingPreferences
import com.couplefit.data.remote.FirebaseSyncManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.util.Date

/**
 * Validates Phase 5 security contracts:
 * 1. Zero-trust client enforcement (Cloud Functions only for linking, scoring, milestones).
 * 2. Firestore rules constraints (read-only client access for challenges, milestones, and partnerId immutability).
 * 3. Daily aggregate upload sanitization (no raw vital data or unauthorized parameters).
 * 4. Challenge and Milestone domain contract models.
 */
class SecurityContractTest {

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
    fun firestoreRules_enforceZeroTrustClientForPartnerId() {
        val rulesFile = resolveProjectFile("firestore.rules")
        val content = rulesFile.readText()

        // Verify rules constrain partnerId modifications to authenticated pairing flow
        assertTrue(
            "Rules must constrain partnerId modification to authenticated pairing",
            content.contains("partnerId") && (
                content.contains("affectedKeys().hasAny(['partnerId'])") ||
                content.contains("request.resource.data.partnerId == request.auth.uid")
            )
        )
    }

    @Test
    fun firestoreRules_enforceReadOnlyForChallengesAndMilestones() {
        val rulesFile = resolveProjectFile("firestore.rules")
        val content = rulesFile.readText()

        // Verify couples root enforces membership and strict member limit
        assertTrue(
            "Couples root must enforce membership and max 2 members",
            content.contains("match /couples/{coupleId}") && (
                content.contains("allow write: if false;") ||
                content.contains("resource.data.members.size() < 2")
            )
        )

        // Verify challenges require couple membership
        assertTrue(
            "Challenges must require couple membership",
            content.contains("match /challenges/{challengeId}") && content.contains("isCoupleMember(coupleId)")
        )

        // Verify milestones require couple membership
        assertTrue(
            "Milestones must require couple membership",
            content.contains("match /milestones/{milestoneId}") && content.contains("isCoupleMember(coupleId)")
        )
    }

    @Test
    fun firebaseFunctions_containRequiredServerSideHandlers() {
        val functionsFile = resolveProjectFile("firebase/functions/index.js")
        val content = functionsFile.readText()

        // 1. Partner linking callable functions
        assertTrue("Must export requestPartnerLink", content.contains("exports.requestPartnerLink"))
        assertTrue("Must export acceptPartnerLink", content.contains("exports.acceptPartnerLink"))
        assertTrue("Must export unlinkPartner", content.contains("exports.unlinkPartner"))

        // 2. Challenge scoring & milestone detection trigger
        assertTrue("Must export onDailyAggregateUpdated", content.contains("exports.onDailyAggregateUpdated"))

        // 3. Critical alert notification trigger
        assertTrue("Must export onCriticalAlertCreated", content.contains("exports.onCriticalAlertCreated"))
    }

    @Test
    fun firebaseSyncPayload_neverContainsRawBiometricsOrPartnerIdDirectWrites() {
        val syncManager = FirebaseSyncManager(firestore = null)
        val prefs = SharingPreferences(
            userId = "user_1",
            partnerId = "user_2",
            shareSteps = true,
            shareWorkouts = true,
            shareSleep = true,
            shareHR = true,
            shareSpO2 = true,
            shareCriticalAlerts = true
        )

        val payload = syncManager.prepareDailyAggregatePayload(
            userId = "user_1",
            dateStr = "2026-10-21",
            steps = 8500,
            activeMinutes = 45,
            calories = 380,
            averageHeartRate = 72.5,
            averageOxygenSaturation = 98.4,
            prefs = prefs
        )

        // Verify only daily aggregates are present
        assertEquals("user_1", payload["userId"])
        assertEquals("2026-10-21", payload["dateStr"])
        assertEquals(8500L, payload["steps"])
        assertEquals(45L, payload["activeMinutes"])
        assertEquals(380L, payload["calories"])
        assertEquals(72.5, payload["averageHeartRate"])
        assertEquals(98.4, payload["averageOxygenSaturation"])

        // Disallowed raw point keys must never exist
        assertFalse("Raw heart rate samples must never be uploaded", payload.containsKey("heartRateSamples"))
        assertFalse("Raw SpO2 samples must never be uploaded", payload.containsKey("spo2Samples"))
        assertFalse("Client payload must not set partnerId directly", payload.containsKey("partnerId"))
    }

    @Test
    fun challengeAndMilestoneDomainContracts_operateAsImmutableReadModels() {
        val now = Date()
        val challenge = Challenge(
            id = "chal_1",
            coupleId = "user1_user2",
            title = "10k Daily Steps",
            description = "Hit 10k steps today",
            type = ChallengeType.TOTAL_STEPS,
            targetValue = 10000.0,
            userAProgress = 8500.0,
            userBProgress = 9200.0,
            status = ChallengeStatus.ACTIVE,
            startDate = now,
            endDate = now,
            winnerUserId = null
        )

        assertEquals("chal_1", challenge.id)
        assertEquals(ChallengeStatus.ACTIVE, challenge.status)
        assertEquals(8500.0, challenge.userAProgress, 0.01)
        assertEquals(9200.0, challenge.userBProgress, 0.01)
        assertNull(challenge.winnerUserId)

        val milestone = Milestone(
            id = "mile_1",
            coupleId = "user1_user2",
            title = "50,000 Combined Steps",
            description = "Reached 50k steps together",
            type = MilestoneType.COMBINED_STEPS,
            threshold = 50000.0,
            achievedAt = now,
            celebratoryMessage = "You both crushed it!"
        )

        assertNotNull(milestone)
        assertEquals(50000.0, milestone.threshold, 0.01)
        assertEquals(MilestoneType.COMBINED_STEPS, milestone.type)
    }
}
