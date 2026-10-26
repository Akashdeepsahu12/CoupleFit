# CoupleFit v2.3.0 Release Notes 🚀

**Release Date:** October 2026  
**Build Status:** 🟢 Green (Unit Tests 100% Pass, AndroidTest Compile 0 Errors)  
**Milestone:** Phase 5 — Information Architecture & Settings 4-Hub Convergence (`IA-03`, `IA-04`, `IA-10`)

---

## 🌟 Overview & Highlights

CoupleFit v2.3.0 accomplishes a major architectural milestone by deconstructing the legacy 9-accordion settings monolith into a streamlined, high-cohesion **4-Hub Settings Architecture**. Backed by `CoupleFitPreferencesDataStore` and reactive StateFlows in `HealthConnectViewModel`, all user preferences (granular health metrics, sync frequencies, biometric locks, appearance) are now permanently persisted and verified with automated Compose UI test suites.

---

## 🧩 Key Deliverables & Enhancements

### 1. 4-Hub Settings Architecture (`IA-03`)
Replaced the 1,007-line accordion monolith with 4 modular composable hubs and an accessible footer:
* **Hub 1: Partnership & Account (`PartnershipHubContent`)**:
  - Displays authenticated user profile (`UserSession`) with display name and email.
  - Live partner status badge showing connected state (`[Jordan (Connected)]`).
  - Strict 2-step verification unlinking dialog (`CSA-07`) requiring typing `"UNLINK"`.
  - Firebase Auth sign-out flow with data preservation notice.
* **Hub 2: Health Sync & Sharing (`HealthSyncHubContent`)**:
  - Health Connect permissions manager card.
  - Samsung Health Bridge toggle with dedicated guidance dialog.
  - Background sync cadence selector (`Every 15m`, `Every 30m`, `Hourly`, `Manual Only`).
  - Granular metric sharing switches (`shareSteps`, `shareWorkouts`, `shareCalories`, `shareHr`, `shareSpo2`) backed directly by DataStore.
* **Hub 3: App Experience (`ExperienceHubContent`)**:
  - Dark Theme toggle supporting circular reveal animation.
  - System-level Reduce Motion accessibility switch.
  - Measurement unit selector (Metric vs Imperial).
  - Multi-channel notification controls (`Critical Alerts`, `Partner Nudges & Cheers`, `Daily Summary`, `Quiet Hours`).
* **Hub 4: Privacy & Safety (`PrivacyHubContent`)**:
  - Biometric App Lock toggle for fingerprint/face protection.
  - In-place navigation entry point to `PrivacyTransparencyScreen`.
  - JSON telemetry export trigger.
  - Permanent account & cloud telemetry deletion modal.
* **Section 5: About & Support Footer (`AboutAndSupportFooter`)**:
  - Semantic app version badge (`v2.3.0`).
  - Web link to official Privacy Policy.
  - Open-source third-party software licenses dialog.

### 2. Documentation Governance Alignment (`IA-04`)
* Formally aligned the repository's unified flat `Documentation/` directory with the 4-folder Diataxis taxonomy in `Documentation/INDEX.md`, `Documentation/phases.md`, and `Documentation/triage.md`.
* Indexed all architectural specs, defect logs, and operational manuals into categorized navigation hierarchies.

### 3. Automated UI Test Suite (`IA-10`)
* Authored [`SettingsHubComposeUiTest.kt`](file:///D:/Couple_fit/CoupleFit/app/src/androidTest/java/com/couplefit/ui/SettingsHubComposeUiTest.kt) containing 5 comprehensive tests:
  1. `settingsHubBar_renders_all_four_pills_and_handles_selection()`: Verifies tab chips render and switch between all 4 hubs.
  2. `partnershipHub_renders_user_profile_and_triggers_unlink_modal()`: Validates user profile, partner connection, and unlinking dialog triggers.
  3. `healthSyncHub_renders_sharing_preferences_and_toggles()`: Asserts sharing preferences and sync cadence chips.
  4. `experienceHub_renders_appearance_and_notifications()`: Verifies dark theme and notification settings.
  5. `privacyHub_renders_lock_transparency_and_data_controls()`: Tests biometric locks, export, and transparency navigation.

---

## 🧪 Verification Matrix

| Verification Tier | Command | Result |
| :--- | :--- | :--- |
| **Unit Test Suite** | `.\gradlew.bat testDebugUnitTest` | 🟢 **100% Pass** (32 actionable tasks, 0 failures) |
| **Kotlin Compilation** | `.\gradlew.bat compileDebugKotlin` | 🟢 **BUILD SUCCESSFUL** (0 errors) |
| **AndroidTest Compilation** | `.\gradlew.bat compileDebugAndroidTestKotlin` | 🟢 **BUILD SUCCESSFUL** (0 errors) |
| **Assembly Build** | `.\gradlew.bat assembleDebug` | 🟢 **BUILD SUCCESSFUL** |
