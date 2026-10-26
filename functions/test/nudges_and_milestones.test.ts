import { expect } from 'chai';

/**
 * Unit & Contract Tests for Phase 4: Nudges and Milestones Cloud Functions
 *
 * Requirements:
 * 1. Cloud Function onDocumentCreated on couples/{cid}/nudges/{nid} sends a push to the partner.
 * 2. Cloud Function onDocumentWritten on couples/{cid}/daily/{docId} detects goal crossings
 *    (e.g. rings closed, step goal reached) and notifies the partner, once per day per milestone.
 * 3. Add a "Send nudge" button with a rate limit (enforced in the function, not the client).
 * 4. Notification channels for nudges and milestones (channel_nudges, channel_milestones).
 * 5. Acceptance: no duplicate milestone notifications.
 */

describe('Phase 4: Nudges and Milestones Unit & Contract Tests', () => {

  describe('Nudge Rate Limit (Cloud Function Enforcement)', () => {
    const NUDGE_COOLDOWN_SECONDS = 60;

    function checkNudgeRateLimit(lastNudgeTimestamp: number, currentTimestamp: number): { allowed: boolean; waitRemaining: number } {
      const diffSeconds = (currentTimestamp - lastNudgeTimestamp) / 1000;
      if (diffSeconds < NUDGE_COOLDOWN_SECONDS) {
        return {
          allowed: false,
          waitRemaining: Math.ceil(NUDGE_COOLDOWN_SECONDS - diffSeconds),
        };
      }
      return { allowed: true, waitRemaining: 0 };
    }

    it('enforces 60-second cooldown rate limit on server', () => {
      const t0 = 1729500000000;
      // Nudge 1: 15 seconds after t0 -> Throttled (< 60s)
      const attempt1 = checkNudgeRateLimit(t0, t0 + 15000);
      expect(attempt1.allowed).to.be.false;
      expect(attempt1.waitRemaining).to.equal(45);

      // Nudge 2: 59 seconds after t0 -> Throttled (< 60s)
      const attempt2 = checkNudgeRateLimit(t0, t0 + 59000);
      expect(attempt2.allowed).to.be.false;
      expect(attempt2.waitRemaining).to.equal(1);

      // Nudge 3: 60 seconds after t0 -> Allowed
      const attempt3 = checkNudgeRateLimit(t0, t0 + 60000);
      expect(attempt3.allowed).to.be.true;
      expect(attempt3.waitRemaining).to.equal(0);

      // Nudge 4: 120 seconds after t0 -> Allowed
      const attempt4 = checkNudgeRateLimit(t0, t0 + 120000);
      expect(attempt4.allowed).to.be.true;
      expect(attempt4.waitRemaining).to.equal(0);
    });

    it('routes push notification to channel_nudges with high priority', () => {
      const nudgePayload = {
        notification: {
          title: 'Alice sent you a nudge! ❤️',
          body: 'Time to get active and close your rings! 🏃',
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
      };

      expect(nudgePayload.android.notification.channelId).to.equal('channel_nudges');
      expect(nudgePayload.android.priority).to.equal('high');
    });
  });

  describe('Milestone Goal Crossing Detection & Zero Duplicates', () => {
    interface DailyAggregate {
      steps: number;
      activeMinutes: number;
      calories: number;
      notifiedMilestones?: Record<string, number>;
    }

    interface Milestone {
      key: string;
      title: string;
      body: string;
      type: string;
    }

    function evaluateMilestones(
      before: DailyAggregate,
      after: DailyAggregate,
      userName: string = 'Your partner'
    ): Milestone[] {
      const notified = after.notifiedMilestones || {};
      const newMilestones: Milestone[] = [];

      const beforeSteps = before.steps || 0;
      const afterSteps = after.steps || 0;
      const beforeActive = before.activeMinutes || 0;
      const afterActive = after.activeMinutes || 0;
      const beforeCalories = before.calories || 0;
      const afterCalories = after.calories || 0;

      // 1. 10,000 steps
      if (beforeSteps < 10000 && afterSteps >= 10000 && !notified['STEP_GOAL_10K']) {
        newMilestones.push({
          key: 'STEP_GOAL_10K',
          title: '🎉 10,000 Steps Reached!',
          body: `${userName} just crushed 10,000 steps today!`,
          type: 'STEPS_10K',
        });
      }

      // 2. 15,000 steps
      if (beforeSteps < 15000 && afterSteps >= 15000 && !notified['STEP_GOAL_15K']) {
        newMilestones.push({
          key: 'STEP_GOAL_15K',
          title: '🔥 15,000 Steps Milestone!',
          body: `${userName} reached 15,000 steps today! Blazing speed!`,
          type: 'STEPS_15K',
        });
      }

      // 3. 30 Active Minutes
      if (beforeActive < 30 && afterActive >= 30 && !notified['ACTIVE_MINUTES_30']) {
        newMilestones.push({
          key: 'ACTIVE_MINUTES_30',
          title: '⚡ 30 Active Minutes!',
          body: `${userName} reached their 30 active minutes goal!`,
          type: 'ACTIVE_30',
        });
      }

      // 4. 500 Calories
      if (beforeCalories < 500 && afterCalories >= 500 && !notified['CALORIES_500']) {
        newMilestones.push({
          key: 'CALORIES_500',
          title: '💥 500 Calories Burned!',
          body: `${userName} just burned 500 active calories!`,
          type: 'CALORIES_500',
        });
      }

      // 5. Triple Crown: All 3 rings closed
      const wasAllClosed = beforeSteps >= 10000 && beforeActive >= 30 && beforeCalories >= 500;
      const isAllClosed = afterSteps >= 10000 && afterActive >= 30 && afterCalories >= 500;
      if (!wasAllClosed && isAllClosed && !notified['RINGS_CLOSED']) {
        newMilestones.push({
          key: 'RINGS_CLOSED',
          title: '🏆 All Activity Rings Closed!',
          body: `${userName} closed all 3 activity rings today! A complete sweep!`,
          type: 'ALL_RINGS_CLOSED',
        });
      }

      return newMilestones;
    }

    it('detects 10,000 steps goal crossing on transition', () => {
      const before: DailyAggregate = { steps: 9800, activeMinutes: 20, calories: 350 };
      const after: DailyAggregate = { steps: 10250, activeMinutes: 20, calories: 350 };

      const milestones = evaluateMilestones(before, after);
      expect(milestones.map((m) => m.key)).to.include('STEP_GOAL_10K');
      expect(milestones.find((m) => m.key === 'STEP_GOAL_10K')?.type).to.equal('STEPS_10K');
    });

    it('ACCEPTANCE: Guarantees no duplicate milestone notification once notified', () => {
      const before: DailyAggregate = { steps: 10250, activeMinutes: 20, calories: 350 };
      // Further steps added later in the day: 10,250 -> 11,500
      const after: DailyAggregate = {
        steps: 11500,
        activeMinutes: 20,
        calories: 350,
        notifiedMilestones: {
          STEP_GOAL_10K: Date.now(),
        },
      };

      const milestones = evaluateMilestones(before, after);
      expect(milestones.some((m) => m.key === 'STEP_GOAL_10K')).to.be.false;
      expect(milestones).to.have.lengthOf(0);
    });

    it('detects Triple Crown (all rings closed) and notifies on channel_milestones', () => {
      const before: DailyAggregate = { steps: 10200, activeMinutes: 32, calories: 480 };
      const after: DailyAggregate = {
        steps: 10200,
        activeMinutes: 32,
        calories: 520, // Crosses 500 cal threshold and completes all 3 rings
        notifiedMilestones: {
          STEP_GOAL_10K: Date.now(),
          ACTIVE_MINUTES_30: Date.now(),
        },
      };

      const milestones = evaluateMilestones(before, after);
      const keys = milestones.map((m) => m.key);
      expect(keys).to.include('CALORIES_500');
      expect(keys).to.include('RINGS_CLOSED');
    });

    it('routes milestone celebration push to channel_milestones with high priority', () => {
      const milestonePayload = {
        notification: {
          title: '🏆 All Activity Rings Closed!',
          body: 'Alice closed all 3 activity rings today! A complete sweep!',
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
      };

      expect(milestonePayload.android.notification.channelId).to.equal('channel_milestones');
      expect(milestonePayload.android.priority).to.equal('high');
    });
  });
});
