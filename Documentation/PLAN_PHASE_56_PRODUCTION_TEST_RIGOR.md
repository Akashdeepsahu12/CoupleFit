# Milestone Plan: Phase 56 — Production Build & Test Rigor 💎🛡️

## Goal
Elevate test rigor and release readiness across the entire CoupleFit application:
1. Author an in-memory SQLite Room test suite (`HealthDaoTest.kt`) validating `DAT-01` (`ORDER BY lastUpdated DESC LIMIT 1`), active challenge queries, and cascading table wipes (`clearAllTables()`).
2. Replace hollow unit tests in [`NavigationComposeUiTest.kt#L409-L422`](file:///D:/Couple_fit/CoupleFit/app/src/androidTest/java/com/couplefit/ui/NavigationComposeUiTest.kt#L409-L422) with substantive Compose UI node and interaction assertions (`LiveHeartRateEmptyWaveform`, `onSyncClick` verification).
3. Replace trivial null-check tests in [`TactileMotionArchitectureTest.kt`](file:///D:/Couple_fit/CoupleFit/app/src/test/java/com/couplefit/ui/TactileMotionArchitectureTest.kt) with concrete mathematical color tokens, WCAG AA contrast, and motion physics contract verifications.
4. Verify release build compilation and R8 ProGuard configuration (`.\gradlew assembleRelease` / `minifyReleaseWithR8`).

---

## Task Breakdown & Dependencies

| Task ID | Task Description | Target Files | Success Criteria |
| :--- | :--- | :--- | :--- |
| **TASK-56.1** | Replace hollow Compose tests with real node & interaction assertions | `NavigationComposeUiTest.kt` | Tests assert `LiveHeartRateEmptyWaveform` node display and verify `onSyncClick` callback invocation. |
| **TASK-56.2** | Replace trivial null checks with contract assertions | `TactileMotionArchitectureTest.kt` | Tests assert exact `CoupleFitIdentityTokens` values, WCAG contrast ratios, and `TelemetryPerspective` enum contracts. |
| **TASK-56.3** | Author in-memory SQLite Room test suite | `HealthDaoTest.kt` in `androidTest` & `HealthDaoPartnerTest.kt` in `test` | In-memory Room database verifies `getPartnerDailyFlow` sorting, `deletePartnerDailyData`, `clearAllTables`, and challenge filtering. |
| **TASK-56.4** | Release build and R8 verification | `app/build.gradle.kts`, `proguard-rules.pro` | `compileDebugUnitTestKotlin`, `testDebugUnitTest`, and `compileDebugAndroidTestKotlin` pass 100% green. |
| **TASK-56.5** | Documentation & Verification Report | `Documentation/phase56_verification_report.md`, `Fixinglist.md`, `phases.md`, `triage.md`, `INDEX.md`, `ROADMAP.md` | All documentation updated and synchronized. |

---

## Test Verification Bar
- `.\gradlew.bat testDebugUnitTest` must pass with 100% success (0 failures).
- `.\gradlew.bat compileDebugAndroidTestKotlin` must pass with 0 errors.
- Live device/emulator verification on `emulator-5554`.
