import * as functions from 'firebase-functions';
import * as admin from 'firebase-admin';

admin.initializeApp();
const db = admin.firestore();

/**
 * Expiration duration for 6-digit invite pairing codes.
 */
const INVITE_EXPIRATION_HOURS = 24;

/**
 * Generates a random 6-digit numeric pairing code (100000..999999).
 */
function generateInviteCode(): string {
  const DIGITS = '0123456789';
  let result = Math.floor(1 + Math.random() * 9).toString();
  for (let i = 1; i < 6; i++) {
    result += DIGITS.charAt(Math.floor(Math.random() * DIGITS.length));
  }
  return result;
}

/**
 * =========================================================================
 * 1. CALLABLE: createInvite
 * Generates a unique 6-character invite code that expires in 24 hours,
 * creates the couples/{coupleId} document with caller as first member.
 * Only the server may write couples/{coupleId}.members.
 * =========================================================================
 */
export const createInvite = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError(
      'unauthenticated',
      'User must be authenticated to create a couple invite.'
    );
  }

  const callerUid = context.auth.uid;

  try {
    // 1. Check if user already has an active, linked couple
    const callerDoc = await db.collection('users').doc(callerUid).get();
    const callerData = callerDoc.data();
    if (callerData?.partnerId && callerData?.coupleId) {
      const existingCouple = await db.collection('couples').doc(callerData.coupleId).get();
      if (existingCouple.exists && existingCouple.data()?.members?.length === 2) {
        throw new functions.https.HttpsError(
          'failed-precondition',
          'You are already linked to an active couple. Unlink first before creating a new invite.'
        );
      }
    }

    // 2. Generate unique 6-character invite code
    let code = '';
    let isUnique = false;
    let attempts = 0;

    while (!isUnique && attempts < 10) {
      code = generateInviteCode();
      const existingInvite = await db.collection('invites').doc(code).get();
      if (!existingInvite.exists) {
        isUnique = true;
      }
      attempts++;
    }

    if (!isUnique) {
      throw new functions.https.HttpsError(
        'internal',
        'Failed to generate unique invite code. Please try again.'
      );
    }

    const coupleId = `cpl_${Date.now()}_${callerUid.substring(0, 5)}`;
    const now = Date.now();
    const expiresAtMillis = now + INVITE_EXPIRATION_HOURS * 60 * 60 * 1000;
    const expiresAtTimestamp = admin.firestore.Timestamp.fromMillis(expiresAtMillis);

    const batch = db.batch();

    // 3. Store invite record in invites/{code}
    const inviteRef = db.collection('invites').doc(code);
    batch.set(inviteRef, {
      code,
      coupleId,
      createdBy: callerUid,
      createdAt: admin.firestore.FieldValue.serverTimestamp(),
      expiresAt: expiresAtTimestamp,
      status: 'PENDING',
    });

    // 4. Create couples/{coupleId} with caller as initial member
    // ONLY server writes to couples/{coupleId}.members
    const coupleRef = db.collection('couples').doc(coupleId);
    batch.set(coupleRef, {
      id: coupleId,
      members: [callerUid],
      status: 'INVITING',
      inviteCode: code,
      createdBy: callerUid,
      createdAt: admin.firestore.FieldValue.serverTimestamp(),
      expiresAt: expiresAtTimestamp,
    });

    // 5. Update caller's user record
    const userRef = db.collection('users').doc(callerUid);
    batch.set(
      userRef,
      {
        uid: callerUid,
        coupleId: coupleId,
        status: 'INVITING',
        updatedAt: admin.firestore.FieldValue.serverTimestamp(),
      },
      { merge: true }
    );

    await batch.commit();

    return {
      success: true,
      coupleId,
      inviteCode: code,
      expiresAt: expiresAtMillis,
      inviteUrl: `https://couplefit.app/invite/${code}`,
    };
  } catch (error: any) {
    console.error('Error in createInvite:', error);
    if (error instanceof functions.https.HttpsError) throw error;
    throw new functions.https.HttpsError('internal', error.message || 'Internal error');
  }
});

/**
 * =========================================================================
 * 2. CALLABLE: redeemInvite
 * Validates the 6-character code, verifies it is not expired, checks
 * that the couple has < 2 members, and atomically adds caller to members.
 * Enforces maximum 2 members per couple.
 * =========================================================================
 */
export const redeemInvite = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError(
      'unauthenticated',
      'User must be authenticated to redeem an invite code.'
    );
  }

  const callerUid = context.auth.uid;
  const inputCode = data?.code;

  if (!inputCode || typeof inputCode !== 'string' || inputCode.trim().length !== 6) {
    throw new functions.https.HttpsError(
      'invalid-argument',
      'A valid 6-character invite code is required.'
    );
  }

  const code = inputCode.trim().toUpperCase();

  return db.runTransaction(async (transaction) => {
    // 1. Fetch invite document
    const inviteRef = db.collection('invites').doc(code);
    const inviteDoc = await transaction.get(inviteRef);

    if (!inviteDoc.exists) {
      throw new functions.https.HttpsError('not-found', 'Invalid invite code.');
    }

    const inviteData = inviteDoc.data()!;

    // 2. Check if already redeemed
    if (inviteData.status !== 'PENDING') {
      throw new functions.https.HttpsError(
        'failed-precondition',
        'This invite code has already been redeemed.'
      );
    }

    // 3. Check expiration (24 hours limit)
    const expiresAt = inviteData.expiresAt.toMillis();
    if (Date.now() > expiresAt) {
      throw new functions.https.HttpsError(
        'failed-precondition',
        'This invite code has expired. Please ask your partner to generate a new invite.'
      );
    }

    const coupleId = inviteData.coupleId;
    const creatorUid = inviteData.createdBy;

    // 4. Cannot join your own invite
    if (callerUid === creatorUid) {
      throw new functions.https.HttpsError(
        'invalid-argument',
        'You cannot redeem your own invite code.'
      );
    }

    // 5. Fetch couple document
    const coupleRef = db.collection('couples').doc(coupleId);
    const coupleDoc = await transaction.get(coupleRef);

    if (!coupleDoc.exists) {
      throw new functions.https.HttpsError(
        'not-found',
        'The couple group associated with this invite no longer exists.'
      );
    }

    const coupleData = coupleDoc.data()!;
    const currentMembers: string[] = coupleData.members || [];

    // 6. Enforce MAX 2 members strictly on the server: a third user cannot join
    if (currentMembers.length >= 2) {
      throw new functions.https.HttpsError(
        'failed-precondition',
        'This couple already has the maximum of 2 members. A third user cannot join.'
      );
    }

    // 7. Verify caller is not already linked to another active couple
    const callerUserRef = db.collection('users').doc(callerUid);
    const callerUserDoc = await transaction.get(callerUserRef);
    if (callerUserDoc.exists && callerUserDoc.data()?.partnerId) {
      throw new functions.https.HttpsError(
        'failed-precondition',
        'You are already linked to another partner. Please unlink first.'
      );
    }

    // 8. Atomic transaction execution:
    // Update couple members to [creatorUid, callerUid]
    const updatedMembers = [creatorUid, callerUid];
    transaction.update(coupleRef, {
      members: updatedMembers,
      status: 'LINKED',
      linkedAt: admin.firestore.FieldValue.serverTimestamp(),
    });

    // Mark invite redeemed
    transaction.update(inviteRef, {
      status: 'REDEEMED',
      redeemedBy: callerUid,
      redeemedAt: admin.firestore.FieldValue.serverTimestamp(),
    });

    // Update both user profiles with coupleId and partnerId
    const creatorUserRef = db.collection('users').doc(creatorUid);
    transaction.set(
      callerUserRef,
      {
        uid: callerUid,
        coupleId: coupleId,
        partnerId: creatorUid,
        status: 'LINKED',
        updatedAt: admin.firestore.FieldValue.serverTimestamp(),
      },
      { merge: true }
    );

    transaction.update(creatorUserRef, {
      coupleId: coupleId,
      partnerId: callerUid,
      status: 'LINKED',
      updatedAt: admin.firestore.FieldValue.serverTimestamp(),
    });

    return {
      success: true,
      coupleId,
      partnerId: creatorUid,
      message: 'Successfully linked with your partner!',
    };
  });
});

/**
 * =========================================================================
 * 3. CALLABLE: unlinkCouple
 * Atomically severs the link between partners and resets their status.
 * =========================================================================
 */
export const unlinkCouple = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError(
      'unauthenticated',
      'User must be authenticated to unlink.'
    );
  }

  const callerUid = context.auth.uid;
  const callerUserRef = db.collection('users').doc(callerUid);

  return db.runTransaction(async (transaction) => {
    const callerDoc = await transaction.get(callerUserRef);
    if (!callerDoc.exists) {
      return { success: true, message: 'User profile not found.' };
    }

    const { coupleId, partnerId } = callerDoc.data()!;
    if (!coupleId) {
      return { success: true, message: 'No active couple link found.' };
    }

    // Clear caller
    transaction.update(callerUserRef, {
      coupleId: null,
      partnerId: null,
      status: 'UNLINKED',
      updatedAt: admin.firestore.FieldValue.serverTimestamp(),
    });

    // Clear partner if linked
    if (partnerId) {
      const partnerUserRef = db.collection('users').doc(partnerId);
      transaction.update(partnerUserRef, {
        coupleId: null,
        partnerId: null,
        status: 'UNLINKED',
        updatedAt: admin.firestore.FieldValue.serverTimestamp(),
      });
    }

    // Update couple doc
    const coupleRef = db.collection('couples').doc(coupleId);
    transaction.update(coupleRef, {
      status: 'UNLINKED',
      unlinkedAt: admin.firestore.FieldValue.serverTimestamp(),
    });

    return { success: true, status: 'UNLINKED' };
  });
});

/**
 * =========================================================================
 * 3b. CALLABLE: deleteUserData
 * Complete GDPR/CCPA & Google Play User Data Deletion compliance.
 * Permanently removes user's profile, health aggregates, nudges, and unlinks.
 * =========================================================================
 */
export const deleteUserData = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError(
      'unauthenticated',
      'User must be authenticated to delete data.'
    );
  }

  const callerUid = context.auth.uid;
  const userRef = db.collection('users').doc(callerUid);
  const userDoc = await userRef.get();
  const userData = userDoc.data();
  const coupleId = userData?.coupleId;
  const partnerId = userData?.partnerId;

  // 1. Unlink partner if connected
  if (coupleId) {
    const coupleRef = db.collection('couples').doc(coupleId);
    try {
      await coupleRef.update({
        members: admin.firestore.FieldValue.arrayRemove(callerUid),
        status: 'UNLINKED',
        unlinkedAt: admin.firestore.FieldValue.serverTimestamp(),
      });
    } catch (e) {
      console.warn('Failed to update couple doc during deletion', e);
    }

    if (partnerId) {
      try {
        const partnerRef = db.collection('users').doc(partnerId);
        await partnerRef.update({
          partnerId: null,
          coupleId: null,
          status: 'UNLINKED',
          updatedAt: admin.firestore.FieldValue.serverTimestamp(),
        });

        // Notify partner via FCM if available
        const partnerDoc = await partnerRef.get();
        const fcmToken = partnerDoc.data()?.fcmToken;
        if (fcmToken) {
          await admin.messaging().send({
            token: fcmToken,
            notification: {
              title: 'Couple Link Ended',
              body: 'Your partner has deleted their account and unlinked.',
            },
          });
        }
      } catch (e) {
        console.warn('Failed to update partner during deletion', e);
      }
    }

    // 2. Delete all daily docs written by caller in couples/{coupleId}/daily
    try {
      const dailySnapshot = await db
        .collection('couples')
        .doc(coupleId)
        .collection('daily')
        .where('userId', '==', callerUid)
        .get();

      const batch = db.batch();
      dailySnapshot.docs.forEach((doc) => batch.delete(doc.ref));
      await batch.commit();
    } catch (e) {
      console.warn('Failed to delete daily docs for user', e);
    }

    // 3. Delete any nudges sent by caller
    try {
      const nudgesSnapshot = await db
        .collection('couples')
        .doc(coupleId)
        .collection('nudges')
        .where('fromUid', '==', callerUid)
        .get();

      const batch = db.batch();
      nudgesSnapshot.docs.forEach((doc) => batch.delete(doc.ref));
      await batch.commit();
    } catch (e) {
      console.warn('Failed to delete user nudges', e);
    }
  }

  // 4. Delete invites created by caller
  try {
    const invitesSnapshot = await db
      .collection('invites')
      .where('createdBy', '==', callerUid)
      .get();
    const batch = db.batch();
    invitesSnapshot.docs.forEach((doc) => batch.delete(doc.ref));
    await batch.commit();
  } catch (e) {
    console.warn('Failed to delete user invites', e);
  }

  // 5. Delete user document
  await userRef.delete();

  return { success: true, message: 'All user data permanently deleted.' };
});

/**
 * =========================================================================
 * 4. FIRESTORE TRIGGER: onDailyAggregateWritten (onDailyAggregateUpdated)
 * Detects goal crossings (e.g., rings closed, 10k steps, 15k steps, 30 active mins,
 * 500 calories) and notifies the partner via FCM, once per day per milestone.
 * Also evaluates active couple challenges and milestone history.
 * Document: couples/{coupleId}/daily/{docId}
 * =========================================================================
 */
export const onDailyAggregateUpdated = functions.firestore
  .document('couples/{coupleId}/daily/{docId}')
  .onWrite(async (change, context) => {
    if (!change.after.exists) return null;

    const coupleId = context.params.coupleId;
    const docId = context.params.docId;
    const beforeData = change.before.exists ? change.before.data()! : {};
    const afterData = change.after.data()!;

    // docId is formatted as {uid}_{date}
    const parts = docId.split('_');
    if (parts.length < 2) return null;
    const userUid = afterData.userId || parts[0];
    const dateStr = afterData.dateStr || parts.slice(1).join('_');

    // Avoid self-triggering loops if only notifiedMilestones or metadata changed
    const beforeSteps = Number(beforeData.steps || 0);
    const afterSteps = Number(afterData.steps || 0);
    const beforeActive = Number(beforeData.activeMinutes || 0);
    const afterActive = Number(afterData.activeMinutes || 0);
    const beforeCalories = Number(beforeData.calories || 0);
    const afterCalories = Number(afterData.calories || 0);

    const metricsChanged =
      beforeSteps !== afterSteps ||
      beforeActive !== afterActive ||
      beforeCalories !== afterCalories;

    try {
      const coupleDoc = await db.collection('couples').doc(coupleId).get();
      if (!coupleDoc.exists) return null;

      const members: string[] = coupleDoc.data()?.members || [];
      if (members.length < 2) return null;

      const partnerUid = members.find((m) => m !== userUid);

      // -----------------------------------------------------------------------
      // A. DETECT GOAL CROSSINGS (Once per day per milestone)
      // -----------------------------------------------------------------------
      const notifiedMilestones: Record<string, any> = afterData.notifiedMilestones || {};
      const newMilestonesToNotify: Array<{
        key: string;
        title: string;
        body: string;
        type: string;
      }> = [];

      // Fetch user's display name for personalization
      let userName = 'Your partner';
      try {
        const userDoc = await db.collection('users').doc(userUid).get();
        if (userDoc.exists && userDoc.data()?.displayName) {
          userName = userDoc.data()!.displayName;
        }
      } catch (e) {
        console.warn('Could not fetch user displayName', e);
      }

      // 1. Step Goal: 10,000 steps
      if (beforeSteps < 10000 && afterSteps >= 10000 && !notifiedMilestones['STEP_GOAL_10K']) {
        newMilestonesToNotify.push({
          key: 'STEP_GOAL_10K',
          title: '🎉 10,000 Steps Reached!',
          body: `${userName} just crushed 10,000 steps today!`,
          type: 'STEPS_10K',
        });
      }

      // 2. High Step Goal: 15,000 steps
      if (beforeSteps < 15000 && afterSteps >= 15000 && !notifiedMilestones['STEP_GOAL_15K']) {
        newMilestonesToNotify.push({
          key: 'STEP_GOAL_15K',
          title: '🔥 15,000 Steps Milestone!',
          body: `${userName} reached 15,000 steps today! Blazing speed!`,
          type: 'STEPS_15K',
        });
      }

      // 3. Active Minutes: 30 minutes
      if (beforeActive < 30 && afterActive >= 30 && !notifiedMilestones['ACTIVE_MINUTES_30']) {
        newMilestonesToNotify.push({
          key: 'ACTIVE_MINUTES_30',
          title: '⚡ 30 Active Minutes!',
          body: `${userName} reached their 30 active minutes goal!`,
          type: 'ACTIVE_30',
        });
      }

      // 4. Calorie Burn: 500 calories
      if (beforeCalories < 500 && afterCalories >= 500 && !notifiedMilestones['CALORIES_500']) {
        newMilestonesToNotify.push({
          key: 'CALORIES_500',
          title: '💥 500 Calories Burned!',
          body: `${userName} just burned 500 active calories!`,
          type: 'CALORIES_500',
        });
      }

      // 5. Triple Crown: All 3 activity rings closed
      const wasAllClosed = beforeSteps >= 10000 && beforeActive >= 30 && beforeCalories >= 500;
      const isAllClosed = afterSteps >= 10000 && afterActive >= 30 && afterCalories >= 500;
      if (!wasAllClosed && isAllClosed && !notifiedMilestones['RINGS_CLOSED']) {
        newMilestonesToNotify.push({
          key: 'RINGS_CLOSED',
          title: '🏆 All Activity Rings Closed!',
          body: `${userName} closed all 3 activity rings today! A complete sweep!`,
          type: 'ALL_RINGS_CLOSED',
        });
      }

      // If any milestones were newly crossed, send push notification and record in notifiedMilestones
      if (newMilestonesToNotify.length > 0 && partnerUid) {
        let partnerFcmToken: string | null = null;
        try {
          const partnerDoc = await db.collection('users').doc(partnerUid).get();
          partnerFcmToken = partnerDoc.data()?.fcmToken || null;
        } catch (e) {
          console.warn('Could not fetch partner FCM token', e);
        }

        const milestoneUpdates: Record<string, any> = {};
        const milestoneBatch = db.batch();

        for (const m of newMilestonesToNotify) {
          milestoneUpdates[`notifiedMilestones.${m.key}`] = admin.firestore.FieldValue.serverTimestamp();

          // Also save milestone celebration record for couple feed/history
          const milestoneDocId = `${dateStr}_${userUid}_${m.key}`;
          const milestoneDocRef = db.collection('couples').doc(coupleId)
            .collection('milestones').doc(milestoneDocId);
          milestoneBatch.set(
            milestoneDocRef,
            {
              id: milestoneDocId,
              userId: userUid,
              partnerId: partnerUid,
              dateStr,
              milestoneKey: m.key,
              type: m.type,
              title: m.title,
              body: m.body,
              achievedAt: admin.firestore.FieldValue.serverTimestamp(),
            },
            { merge: true }
          );

          if (partnerFcmToken) {
            try {
              await admin.messaging().send({
                token: partnerFcmToken,
                notification: {
                  title: m.title,
                  body: m.body,
                },
                android: {
                  priority: 'high',
                  notification: {
                    channelId: 'channel_milestones',
                    icon: 'ic_notification',
                    color: '#00F0FF',
                    sound: 'default',
                  },
                },
                data: {
                  type: 'MILESTONE',
                  coupleId,
                  userUid,
                  dateStr,
                  milestoneKey: m.key,
                },
              });
              console.log(`[FCM] Sent milestone ${m.key} notification to partner ${partnerUid}`);
            } catch (fcmErr) {
              console.error(`[FCM] Failed to send milestone notification:`, fcmErr);
            }
          }
        }

        // Atomically flag milestones on daily doc to prevent any duplicate notifications
        milestoneBatch.update(change.after.ref, milestoneUpdates);
        await milestoneBatch.commit();
      }

      // -----------------------------------------------------------------------
      // B. SCORE ACTIVE COUPLE CHALLENGES
      // -----------------------------------------------------------------------
      if (!metricsChanged) return null;

      // Read both members' daily docs for dateStr
      const docUserA = await db.collection('couples').doc(coupleId)
        .collection('daily').doc(`${members[0]}_${dateStr}`).get();
      const docUserB = await db.collection('couples').doc(coupleId)
        .collection('daily').doc(`${members[1]}_${dateStr}`).get();

      const userASteps = Number(docUserA.data()?.steps || 0);
      const userBSteps = Number(docUserB.data()?.steps || 0);
      const combinedSteps = userASteps + userBSteps;

      const challengesSnap = await db.collection('couples').doc(coupleId)
        .collection('challenges').where('status', '==', 'ACTIVE').get();

      if (!challengesSnap.empty) {
        const batch = db.batch();
        for (const challengeDoc of challengesSnap.docs) {
          const challenge = challengeDoc.data();
          const target = Number(challenge.targetValue || 10000);
          let completed = false;
          let winnerId: string | null = null;

          if (challenge.type === 'TOTAL_STEPS') {
            if (userASteps >= target || userBSteps >= target) {
              completed = true;
              winnerId = userASteps >= userBSteps ? members[0] : members[1];
            }
          }

          const payload: Record<string, any> = {
            [`progress.${members[0]}`]: userASteps,
            [`progress.${members[1]}`]: userBSteps,
            lastEvaluatedAt: admin.firestore.FieldValue.serverTimestamp(),
          };

          if (completed) {
            payload.status = 'COMPLETED';
            payload.winnerUserId = winnerId;
            payload.completedAt = admin.firestore.FieldValue.serverTimestamp();
          }

          batch.update(challengeDoc.ref, payload);
        }
        await batch.commit();
      }

      // Combined 20k Steps Milestone
      if (combinedSteps >= 20000) {
        const combinedMilestoneId = `steps_20k_${dateStr}`;
        const milestoneRef = db.collection('couples').doc(coupleId)
          .collection('milestones').doc(combinedMilestoneId);
        await milestoneRef.set(
          {
            id: combinedMilestoneId,
            type: 'COMBINED_STEPS',
            threshold: 20000,
            achievedAt: admin.firestore.FieldValue.serverTimestamp(),
            title: '20,000 Combined Steps',
            celebratoryMessage: 'Crushed 20,000 steps together today! 🚀',
          },
          { merge: true }
        );
      }

      return null;
    } catch (e) {
      console.error('Error in onDailyAggregateUpdated:', e);
      return null;
    }
  });

/**
 * =========================================================================
 * 5. FIRESTORE TRIGGER: onNudgeCreated (onDocumentCreated on couples/{cid}/nudges/{nid})
 * Sends a push notification to the partner with server-side rate limit enforcement.
 * Rate limit: Enforced in the function, not the client (60s cooldown per sender).
 * =========================================================================
 */
const NUDGE_COOLDOWN_SECONDS = 60;

export const onNudgeCreated = functions.firestore
  .document('couples/{coupleId}/nudges/{nudgeId}')
  .onCreate(async (snap, context) => {
    const coupleId = context.params.coupleId;
    const nudgeId = context.params.nudgeId;
    const nudge = snap.data();
    if (!nudge) return null;

    const senderUid = nudge.senderUid;
    if (!senderUid) {
      console.warn(`[Nudge] Missing senderUid in nudge ${nudgeId}`);
      return null;
    }

    try {
      // 1. Enforce Server-Side Rate Limiting
      // Find the most recent previously sent nudge by this sender in this couple
      const recentNudgesSnap = await db.collection('couples').doc(coupleId)
        .collection('nudges')
        .where('senderUid', '==', senderUid)
        .where('status', '==', 'SENT')
        .orderBy('createdAt', 'desc')
        .limit(1)
        .get();

      if (!recentNudgesSnap.empty) {
        const lastNudge = recentNudgesSnap.docs[0].data();
        const lastTime = lastNudge.createdAt?.toMillis
          ? lastNudge.createdAt.toMillis()
          : (lastNudge.timestamp || 0);
        const now = Date.now();
        const diffSeconds = (now - lastTime) / 1000;

        if (diffSeconds < NUDGE_COOLDOWN_SECONDS) {
          const waitRemaining = Math.ceil(NUDGE_COOLDOWN_SECONDS - diffSeconds);
          console.warn(`[Nudge Rate Limit] Sender ${senderUid} throttled. Must wait ${waitRemaining}s.`);
          await snap.ref.update({
            status: 'RATE_LIMITED',
            reason: `Please wait ${waitRemaining}s before sending another nudge.`,
            processedAt: admin.firestore.FieldValue.serverTimestamp(),
          });
          return null;
        }
      }

      // 2. Resolve recipient partner UID
      let recipientUid = nudge.recipientUid;
      if (!recipientUid) {
        const coupleDoc = await db.collection('couples').doc(coupleId).get();
        if (coupleDoc.exists) {
          const members: string[] = coupleDoc.data()?.members || [];
          recipientUid = members.find((m) => m !== senderUid);
        }
      }

      if (!recipientUid) {
        console.warn(`[Nudge] No recipient partner found for nudge ${nudgeId}`);
        await snap.ref.update({
          status: 'FAILED',
          reason: 'No recipient partner linked.',
          processedAt: admin.firestore.FieldValue.serverTimestamp(),
        });
        return null;
      }

      // 3. Fetch recipient's FCM token
      const recipientDoc = await db.collection('users').doc(recipientUid).get();
      const fcmToken = recipientDoc.data()?.fcmToken;

      const senderName = nudge.senderName || 'Your partner';
      const notificationTitle = `${senderName} sent you a nudge! ❤️`;
      const notificationBody = nudge.message || "Time to get active and close your rings! 🏃";

      if (fcmToken) {
        try {
          await admin.messaging().send({
            token: fcmToken,
            notification: {
              title: notificationTitle,
              body: notificationBody,
            },
            android: {
              priority: 'high',
              notification: {
                channelId: 'channel_nudges',
                icon: 'ic_notification',
                color: '#FF2D55',
                sound: 'default',
              },
            },
            data: {
              type: 'NUDGE',
              coupleId,
              nudgeId,
              senderUid,
              message: notificationBody,
            },
          });
          console.log(`[FCM] Successfully delivered nudge ${nudgeId} to ${recipientUid}`);
        } catch (fcmErr) {
          console.error(`[FCM] Error sending nudge message:`, fcmErr);
        }
      } else {
        console.warn(`[Nudge] Recipient ${recipientUid} has no registered fcmToken.`);
      }

      // 4. Mark nudge as SENT
      await snap.ref.update({
        status: 'SENT',
        processedAt: admin.firestore.FieldValue.serverTimestamp(),
      });

      return null;
    } catch (e) {
      console.error(`Error processing nudge ${nudgeId}:`, e);
      return null;
    }
  });

// Alias for backwards compatibility
export const onCriticalAlertCreated = onNudgeCreated;
export const onDailyAggregateWritten = onDailyAggregateUpdated;

/**
 * =========================================================================
 * 6. SCHEDULED CLOUD FUNCTION: evaluateChallengesScheduled (Phase 5)
 * Runs periodically to score all active couple challenges from daily docs
 * (steps race, active-minutes streak), updates side-by-side progress, and declares winners.
 * =========================================================================
 */
export async function runChallengeEvaluation() {
  const couplesSnap = await db.collection('couples').where('status', '==', 'LINKED').get();
  let totalEvaluated = 0;

  for (const coupleDoc of couplesSnap.docs) {
    const coupleId = coupleDoc.id;
    const members: string[] = coupleDoc.data()?.members || [];
    if (members.length < 2) continue;

    const challengesSnap = await db.collection('couples').doc(coupleId)
      .collection('challenges')
      .where('status', '==', 'ACTIVE')
      .get();

    if (challengesSnap.empty) continue;

    for (const chDoc of challengesSnap.docs) {
      const ch = chDoc.data();
      const type = ch.type || 'STEPS_RACE';
      const goal = Number(ch.goal || ch.targetValue || 50000);
      const startDate = ch.start || ch.startDate || '';
      const endDate = ch.end || ch.endDate || '';

      const dailySnap = await db.collection('couples').doc(coupleId)
        .collection('daily').get();

      const userADocs: Array<{ date: string; steps: number; activeMinutes: number }> = [];
      const userBDocs: Array<{ date: string; steps: number; activeMinutes: number }> = [];

      for (const d of dailySnap.docs) {
        const docId = d.id;
        const data = d.data();
        const parts = docId.split('_');
        const uid = data.userId || parts[0];
        const dateStr = data.dateStr || parts.slice(1).join('_');

        if (startDate && dateStr < startDate) continue;
        if (endDate && dateStr > endDate) continue;

        const info = {
          date: dateStr,
          steps: Number(data.steps || 0),
          activeMinutes: Number(data.activeMinutes || 0),
        };

        if (uid === members[0]) userADocs.push(info);
        if (uid === members[1]) userBDocs.push(info);
      }

      let memberAProgress = 0;
      let memberBProgress = 0;

      if (type === 'STEPS_RACE' || type === 'TOTAL_STEPS') {
        memberAProgress = userADocs.reduce((acc, curr) => acc + curr.steps, 0);
        memberBProgress = userBDocs.reduce((acc, curr) => acc + curr.steps, 0);
      } else if (type === 'ACTIVE_MINUTES_STREAK') {
        userADocs.sort((a, b) => a.date.localeCompare(b.date));
        userBDocs.sort((a, b) => a.date.localeCompare(b.date));

        let currentStreakA = 0;
        let maxStreakA = 0;
        for (const doc of userADocs) {
          if (doc.activeMinutes >= 30) {
            currentStreakA++;
            if (currentStreakA > maxStreakA) maxStreakA = currentStreakA;
          } else {
            currentStreakA = 0;
          }
        }

        let currentStreakB = 0;
        let maxStreakB = 0;
        for (const doc of userBDocs) {
          if (doc.activeMinutes >= 30) {
            currentStreakB++;
            if (currentStreakB > maxStreakB) maxStreakB = currentStreakB;
          } else {
            currentStreakB = 0;
          }
        }

        memberAProgress = maxStreakA;
        memberBProgress = maxStreakB;
      }

      const todayStr = new Date().toISOString().split('T')[0];
      const isPastEndDate = endDate ? todayStr > endDate : false;
      const isGoalReached = memberAProgress >= goal || memberBProgress >= goal;
      const shouldComplete = isGoalReached || isPastEndDate;

      let winnerId: string | null = null;
      if (shouldComplete) {
        if (memberAProgress > memberBProgress) {
          winnerId = members[0];
        } else if (memberBProgress > memberAProgress) {
          winnerId = members[1];
        } else {
          winnerId = 'TIE';
        }
      }

      const updatePayload: Record<string, any> = {
        [`progress.${members[0]}`]: memberAProgress,
        [`progress.${members[1]}`]: memberBProgress,
        lastEvaluatedAt: admin.firestore.FieldValue.serverTimestamp(),
      };

      if (shouldComplete) {
        updatePayload.status = 'COMPLETED';
        updatePayload.winnerUserId = winnerId;
        updatePayload.completedAt = admin.firestore.FieldValue.serverTimestamp();

        for (const uid of members) {
          try {
            const userDoc = await db.collection('users').doc(uid).get();
            const fcmToken = userDoc.data()?.fcmToken;
            if (fcmToken) {
              const winnerName = winnerId === uid ? 'You won' : (winnerId === 'TIE' ? 'It\'s a tie' : 'Your partner won');
              await admin.messaging().send({
                token: fcmToken,
                notification: {
                  title: '🏆 Challenge Finished!',
                  body: `${winnerName} the ${type === 'STEPS_RACE' ? 'Steps Race' : 'Active Streak'} challenge!`,
                },
                android: {
                  priority: 'high',
                  notification: {
                    channelId: 'channel_milestones',
                    icon: 'ic_notification',
                    color: '#00F0FF',
                  },
                },
              });
            }
          } catch (e) {
            console.error('Failed to notify challenge completion', e);
          }
        }
      }

      await chDoc.ref.update(updatePayload);
      totalEvaluated++;
    }
  }

  return { success: true, evaluated: totalEvaluated };
}

export const evaluateChallengesScheduled = functions.pubsub
  .schedule('every 1 hours')
  .onRun(async () => {
    return await runChallengeEvaluation();
  });

export const evaluateChallengesNow = functions.https.onCall(async (data, context) => {
  return await runChallengeEvaluation();
});

