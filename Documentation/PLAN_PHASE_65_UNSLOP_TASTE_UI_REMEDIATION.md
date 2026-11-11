# Phase 65: antislop + taste UI Remediation & Polish Wave (`AUD-P0`, `AUD-P1`, `AUD-P2`, `AUD-P3`)

## 1. Executive Summary & Root Cause Analysis
Following the comprehensive UI audit completed in [`Documentation/AUDIT_UNSLOP_TASTE_UI_REPORT.md`](AUDIT_UNSLOP_TASTE_UI_REPORT.md), this phase systematically resolves all prioritized defect findings across the application without breaking any existing functionality or test contracts:
1. **Motion & Loop Hygiene (`AUD-P0-01`, `AUD-P0-03`, `AUD-P1-04`, `AUD-P1-10`)**:
   - Perpetual arc glow in `MotionArcGauge.kt` and `ActivityRing.kt` strobed continuously with no settling or reduction when motion was disabled.
   - `CoupleHubScreen.kt` partner presence halo ignored `reduceMotion` and pulsed identically regardless of whether the partner was online or offline.
   - Motion spring parameters lacked a unified vocabulary across components.
2. **Copy, Microcopy & Typography Craft (`AUD-P0-02`, `AUD-P1-02`, `AUD-P1-06`, `AUD-P2-03`, `AUD-P3-02`)**:
   - `QuickNudgeRow.kt` used raw emoji in pill faces, reading as generic decorative slop.
   - Section headers used uppercase with wide letter-spacing (`QUICK CHEERS & NUDGES`).
   - Multiple inline `sp` values bypassed adaptive typography scaling.
3. **Color, Contrast & Accessibility Hardening (`AUD-P0-04`, `AUD-P1-07`, `AUD-P1-08`, `AUD-P3-04`)**:
   - `TextSecondaryDark` alpha at 85% risked falling below WCAG AA on light glass states; raised to 92% (`Color(0xEBEBF2FA)`).
   - `MotionPartnerGlow` saturated red-orange (#FF453A) on OLED dark canvas risked halation; desaturated to `Color(0xFFE03D33)`.
   - Custom animated icons in `AnimatedIcons.kt` lacked `contentDescription` semantic parameters for TalkBack.
   - Nudge pills lacked a 44dp minimum touch width.
4. **Data Integrity, State Resilience & Visual Hierarchy (`AUD-P1-01`, `AUD-P1-03`, `AUD-P1-05`, `AUD-P1-09`, `AUD-P2-02`, `AUD-P2-04`)**:
   - `ChallengesScreen.kt` badge counts were hardcoded constants (1, 6) rather than reflecting real data.
   - `SettingsScreen.kt` local UI states used ephemeral `remember` instead of `rememberSaveable`.
   - `CoupleHubScreen.kt` milestone card lacked visual emphasis.
   - `SignInScreen.kt` activity ring showed fake 85% progress on an unauthenticated screen.
   - `QuickNudgeRow.kt` "In Sync!" required exact integer step equality, virtually never triggering.

---

## 2. Milestone Objectives & Work Breakdown Structure (WBS)

### Wave 1: Color, Typography & Canonical Motion Tokens (`CLR-03`, `TYP-07`, `MOT-01`)
- **Task 1.1**: Update `Color.kt` to boost `TextSecondaryDark` to 92% alpha (`Color(0xEBEBF2FA)`) and calibrate `MotionPartnerGlow` to `Color(0xFFE03D33)` (`AUD-P1-07`, `AUD-P1-08`).
- **Task 1.2**: Create `AnimationTokens.kt` defining canonical spring specifications: `SpringPhysical`, `SpringResponsive`, `SpringNavigation` (`AUD-P1-10`).
- **Task 1.3**: Add semantic `ScreenTitle` typography role to `Type.kt` (`AUD-P2-03`).

### Wave 2: Component Motion Hygiene & Accessibility (`MOT-02`, `A11Y-06`, `CPX-09`)
- **Task 2.1**: Update `MotionArcGauge.kt` to respect `reduceMotion` and soften the arc leading tip pulse from perpetual harsh strobe to calm breathing settle (`AUD-P0-01`).
- **Task 2.2**: Update `AnimatedIcons.kt` with `contentDescription: String? = null` parameter across all animated icons, ensuring TalkBack compatibility (`AUD-P0-04`).
- **Task 2.3**: Update `CoupleHubScreen.kt` to respect `reduceMotion` for the partner halo, differentiate online (vibrant 2.2s pulse) vs offline (subtle dim 4.0s breath), and highlight the milestone relationship card with a `SynchronyGold` border accent (`AUD-P0-03`, `AUD-P1-04`, `AUD-P1-05`).

### Wave 3: QuickNudgeRow Craft & Contextual Logic (`TDD-05`, `CPY-02`, `A11Y-07`)
- **Task 3.1**: In `QuickNudgeRow.kt`, remove emoji decoration from pill faces, preserving emoji in the sent payload (`onSendNudge("${action.emoji} ${action.label}")`) (`AUD-P0-02`).
- **Task 3.2**: In `QuickNudgeRow.kt`, change section title from `"QUICK CHEERS & NUDGES"` to `"Quick cheers"` and synchronize `NavigationComposeUiTest.kt` (`AUD-P1-02`).
- **Task 3.3**: Replace glass on micro-control pills with clean tactile pills, add `defaultMinSize(minWidth = 44.dp)`, change "In Sync!" trigger to `Math.abs(mySteps - partnerSteps) <= 100`, and default partner name to `"your partner"` (`AUD-P1-09`, `AUD-P2-02`, `AUD-P3-02`, `AUD-P3-04`).

### Wave 4: Screen Polish, Data Integrity & State Resilience (`DAT-07`, `RES-02`, `AUTH-03`)
- **Task 4.1**: In `ChallengesScreen.kt`, bind badge count dynamically to `uiState.challenges.count { it.status == "ACTIVE" }` and real trophies (`AUD-P1-01`).
- **Task 4.2**: In `SettingsScreen.kt`, convert local toggle states to `rememberSaveable` to ensure resilience across configuration changes and process restoration (`AUD-P1-03`).
- **Task 4.3**: In `SignInScreen.kt`, reset `ActivityRing` progress from fake `0.85f` to `0f` (ready-to-pair state) and use typography roles (`AUD-P2-04`).

---

## 3. Verification & Test Acceptance Gates
- **Gate 1**: `.\gradlew.bat testDebugUnitTest` must pass with 100% success (0 failures).
- **Gate 2**: `.\gradlew.bat compileDebugAndroidTestKotlin` must compile with 0 errors.
- **Gate 3**: Android UI tests (`NavigationComposeUiTest`, `CardSortConvergenceComposeUiTest`) must compile and pass assertions.
- **Gate 4**: Documentation synchronized across `Fixinglist.md`, `INDEX.md`, `phases.md`, `triage.md`, and `knowledge-graph.md`.
