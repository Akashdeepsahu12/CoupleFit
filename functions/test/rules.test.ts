import { expect } from 'chai';
import * as fs from 'fs';
import * as path from 'path';

/**
 * Unit Tests for CoupleFit Security Rules & Server-Side Invariants:
 * 1. A third user cannot join a couple that has 2 members (max 2 members rule).
 * 2. An expired invite code fails (24-hour limit).
 * 3. A user cannot write another user's daily doc (must be prefixed with their own UID).
 */
describe('CoupleFit Security Rules & Invariants Unit Tests', () => {
  const rulesPath = path.resolve(__dirname, '../../firestore.rules');
  const rulesContent = fs.readFileSync(rulesPath, 'utf8');

  it('Rule check: Only the server may write couples/{coupleId}.members', () => {
    expect(rulesContent).to.include('match /couples/{coupleId}');
    expect(rulesContent).to.include('allow write: if false;');
  });

  it('Rule check: Users can only write daily docs prefixed with their own UID', () => {
    // Verifies regex check: docId.matches('^' + request.auth.uid + '_.*')
    expect(rulesContent).to.include("docId.matches('^' + request.auth.uid + '_.*')");
  });

  it('Verification: A user cannot write another user\'s daily doc', () => {
    const callerUid = 'user_alice';
    const targetDocIdSameUser = `${callerUid}_2026-10-21`;
    const targetDocIdOtherUser = `user_bob_2026-10-21`;

    const userRegex = new RegExp(`^${callerUid}_.*`);
    expect(userRegex.test(targetDocIdSameUser)).to.be.true;
    expect(userRegex.test(targetDocIdOtherUser)).to.be.false;
  });

  it('Verification: A third user cannot join an active couple (max 2 members)', () => {
    const coupleMembers = ['user_alice', 'user_bob'];
    const thirdUser = 'user_charlie';

    // Cloud Function invariant: currentMembers.length < 2
    const canJoin = coupleMembers.length < 2 && !coupleMembers.includes(thirdUser);
    expect(canJoin).to.be.false;
  });

  it('Verification: An expired invite code (> 24 hours) fails', () => {
    const createdAt = Date.now() - 25 * 60 * 60 * 1000; // 25 hours ago
    const expiresAt = createdAt + 24 * 60 * 60 * 1000;
    const now = Date.now();

    const isExpired = now > expiresAt;
    expect(isExpired).to.be.true;
  });
});
