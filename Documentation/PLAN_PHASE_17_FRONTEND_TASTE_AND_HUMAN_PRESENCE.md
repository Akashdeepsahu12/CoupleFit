# Phase 17 Milestone Plan — Frontend Design Taste, Emancipated Floating Glyphs & Warm Human Presence 🎨✨

> **Milestone Tag**: `v2.9.2-frontend-taste`  
> **Governing Standards**: `/taste-skill:design-taste-frontend`, `/emilkowalski-skills:emil-design-eng`, `/antislop`, WCAG 2.2 Level AA  
> **Associated Defect & Enhancement Logs**: `TST-04`, `TST-05`, `TST-06`, `TST-07`, `TST-08`

---

## 1. Executive Summary & Intent

CoupleFit is fundamentally about **two people sharing their health journey together**, not a clinical dashboard or generic database viewer. This milestone eliminates pervasive AI-template design habits ("AI slop") and replaces them with high-craft, human, and taste-driven decisions:
1. **Emancipated Floating Utility Glyphs (`TST-04`)**: Discard generic opaque gray circular icon saucers (`Box(size = 36.dp, clip = CircleShape, background = surfaceVariant)`). Utility icons float cleanly with crisp contrast and accessible 44x44dp touch targets.
2. **Warm Human Partner Presence (`TST-05`)**: De-robotize system strings (`"Synced at 10:45 AM"`, `"Awaiting partner sync • Offline ready"`) into living relational presence (*"You & Alex are in sync • Updated 10:45 AM"*, *"Waiting for Alex to open CoupleFit • Showing last active data"*).
3. **Contextual Frosted Quick-Nudges (`TST-06`)**: Elevate static gray emoji buttons into dynamic, context-aware liquid glass pills with rich couple prompts (*"🏃 Catching up!"*, *"⚡ You got this!"*, *"👋 Quick Poke"*, *"❤️ Thinking of you"*).
4. **Dashboard Layout Mid-Page Spacer Clean-up (`TST-07`)**: Eliminate the accidental 96dp mid-page gaps in the "My Stats" and "Partner's Stats" tabs, preserving natural vertical rhythm and restricting scroll clearance solely to the bottom of the screen container.
5. **Playful Rivalry Balance Bar (`TST-08`)**: Upgrade the static step competition text into an engaging, visual balance indicator that dynamically visualizes the step tug-of-war between partners.

---

## 2. Explicit Acceptance Criteria & Test Bars

### Criteria 1: Floating Utility Icons (`TST-04`)
- [ ] Header Settings icon in `DashboardScreen.kt` has no opaque circular gray background.
- [ ] Minimum touch target of 44x44dp preserved for WCAG 2.2 AA accessibility.
- [ ] Icon tint retains high contrast against the OLED dark backdrop.

### Criteria 2: Warm Partner Presence Copy (`TST-05`)
- [ ] Sync status row in `DashboardScreen.kt` renders relational, human presence text.
- [ ] Partner tab subtitle and card copy reflect warm waiting states rather than IT server language.

### Criteria 3: Contextual Frosted Glass Quick-Nudges (`TST-06`)
- [ ] Quick-nudge pills in `QuickNudgeRow.kt` use `Modifier.iosGlass()` with specular rims instead of muddy gray `surfaceVariant`.
- [ ] Nudge options adapt dynamically based on whether the partner is leading, trailing, or tied.

### Criteria 4: Mid-Page Spacing Integrity (`TST-07`)
- [ ] Accidental 96dp spacer removed before `MY VITALS` in Tab 1 ("My Stats").
- [ ] Accidental 96dp spacer removed before `PARTNER'S VITALS` in Tab 2 ("Partner's Stats").
- [ ] Bottom container spacer (96dp) at line 1272 retained for floating dock clearance.

### Criteria 5: Playful Step Competition Balance Bar (`TST-08`)
- [ ] "Today's Competition" card displays an animated dual-color split progress bar showing the step proportion between user and partner.

### Criteria 6: Verification & Test Bar
- [ ] `./gradlew.bat testDebugUnitTest` passes 100% (32/32 tasks).
- [ ] `./gradlew.bat compileDebugAndroidTestKotlin` compiles with 0 errors.
- [ ] Live Android execution verified on `emulator-5554` via ARTEMIS with screenshot proof.

---

## 3. Task Breakdown & Dependency Order

1. **Task 1: Spacing & Clean Header Glyphs (`DashboardScreen.kt`)**:
   - Emancipate Settings `IconButton` from circular gray saucer.
   - Remove misplaced 96dp spacers at line 875 and 1127.
   - Humanize header sync copy and partner status subtitles.
2. **Task 2: Playful Rivalry Balance Bar (`DashboardScreen.kt`)**:
   - Add dual-color step proportion gauge in the "Today's Competition" card.
3. **Task 3: Dynamic Contextual Quick Nudges (`QuickNudgeRow.kt`)**:
   - Implement `Modifier.iosGlass(RoundedCornerShape(20.dp))` on nudge pills.
   - Add dynamic step-relative nudge suggestions (`mySteps`, `partnerSteps`).
4. **Task 4: Automated Test Suite & Live ARTEMIS Verification**:
   - Add UI assertion in `NavigationComposeUiTest.kt`.
   - Run Gradle test suite and capture live device screenshot proof on `emulator-5554`.
5. **Task 5: Documentation Synchronization & Git Shipment**:
   - Proactively update `Fixinglist.md`, `triage.md`, `phases.md`, and `INDEX.md`.
   - Commit with conventional commit message and tag release `v2.9.2-frontend-taste`.
