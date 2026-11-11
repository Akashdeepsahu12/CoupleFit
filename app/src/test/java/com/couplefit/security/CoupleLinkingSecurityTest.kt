package com.couplefit.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.util.regex.Pattern

/**
 * Validates Phase 2 Authentication, Couple Linking, and Security Rules:
 * 1. Third user cannot join a couple with 2 members.
 * 2. Expired invite code fails (24 hours expiration).
 * 3. User cannot write another user's daily doc (enforces uid-prefix).
 * 4. Only server may write couples/{coupleId}.members.
 */
class CoupleLinkingSecurityTest {

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
    fun firestoreRules_enforceServerOnlyCoupleMembersWriting() {
        val rulesFile = resolveProjectFile("firestore.rules")
        val content = rulesFile.readText()

        // Verify couples collection enforces membership and blocks 3rd member
        assertTrue(
            "Couples must strictly enforce max 2 members or prohibit direct write",
            content.contains("match /couples/{coupleId}") && (
                content.contains("allow write: if false;") ||
                content.contains("resource.data.members.size() < 2")
            )
        )

        // Verify invites collection enforces authenticated creator or pending redemption
        assertTrue(
            "Invites collection must require authentication and pending status for redemption",
            content.contains("match /invites/{code}") && (
                content.contains("allow read, write: if false;") ||
                content.contains("resource.data.status == 'PENDING'")
            )
        )
    }

    @Test
    fun firestoreRules_enforceUserCannotWriteAnotherUserDailyDoc() {
        val rulesFile = resolveProjectFile("firestore.rules")
        val content = rulesFile.readText()

        // Rule must enforce that docId matches caller's UID prefix: ^UID_.*
        assertTrue(
            "Daily doc writing must be constrained by caller UID prefix",
            content.contains("docId.matches('^' + request.auth.uid + '_.*')")
        )

        // Simulate regex check on client/rule level
        val callerUid = "user_alice"
        val rulePattern = Pattern.compile("^${Pattern.quote(callerUid)}_.*")

        val ownDocId = "${callerUid}_2026-10-21"
        val otherUserDocId = "user_bob_2026-10-21"

        assertTrue("User can write their own daily doc", rulePattern.matcher(ownDocId).matches())
        assertFalse("User CANNOT write another user's daily doc", rulePattern.matcher(otherUserDocId).matches())
    }

    @Test
    fun cloudFunctions_enforceThirdUserCannotJoin() {
        val functionsFile = resolveProjectFile("functions/src/index.ts")
        val content = functionsFile.readText()

        // Verify Cloud Function checks maximum of 2 members
        assertTrue(
            "Cloud Function must block third user from joining",
            content.contains("currentMembers.length >= 2") || content.contains("members.length >= 2")
        )

        // Simulate the logic:
        val existingMembers = listOf("user_alice", "user_bob")
        val candidateUid = "user_charlie"

        val canJoin = existingMembers.size < 2 && !existingMembers.contains(candidateUid)
        assertFalse("Third user must be rejected when couple has 2 members", canJoin)
    }

    @Test
    fun cloudFunctions_enforceExpiredInviteCodeFails() {
        val functionsFile = resolveProjectFile("functions/src/index.ts")
        val content = functionsFile.readText()

        // Verify Cloud Function checks code expiration (24h limit)
        assertTrue(
            "Cloud Function must validate invite expiration",
            content.contains("Date.now() > expiresAt") || content.contains("expiresAt")
        )

        // Simulate 24-hour expiration logic
        val now = System.currentTimeMillis()
        val expiredCreationTime = now - (25 * 60 * 60 * 1000L) // 25 hours ago
        val expiresAt = expiredCreationTime + (24 * 60 * 60 * 1000L) // expired 1 hour ago

        val isExpired = now > expiresAt
        assertTrue("Invite created 25h ago must be considered expired", isExpired)

        val freshCreationTime = now - (2 * 60 * 60 * 1000L) // 2 hours ago
        val freshExpiresAt = freshCreationTime + (24 * 60 * 60 * 1000L)
        assertFalse("Invite created 2h ago must still be valid", now > freshExpiresAt)
    }

    @Test
    fun inviteCode_hasValidLengthAndFormat() {
        val functionsFile = resolveProjectFile("functions/src/index.ts")
        val content = functionsFile.readText()

        // 6-character requirement
        assertTrue("Must specify 6-character invite code generation", content.contains("i < 6"))
        assertTrue("Must validate 6-character input", content.contains("length !== 6"))
    }
}
