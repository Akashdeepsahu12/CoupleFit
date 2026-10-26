# CoupleFit v2.4.0 Release Notes 🚀

> **Milestone**: Phase 6 — Navigation Architecture Polish & Platform Hardening  
> **Release Tag**: `v2.4.0-nav-polish`  
> **Date**: October 2026  
> **Governing Standards**: [`AGENTS.md`](../AGENTS.md), [`Documentation/INDEX.md`](INDEX.md), [`Documentation/Fixinglist.md`](Fixinglist.md)

---

## 🌟 Executive Summary

CoupleFit version 2.4.0 completes the comprehensive navigation architecture polish and platform hardening sprint, resolving key usability, accessibility, and platform integration items identified in our navigation systems audits:
- **`NAV-03`**: Multi-modal active tab indication combining dual filled/outlined vector icon switching, dynamic bold typography on active labels, and subtle tactile haptics (`HapticFeedbackType.TextHandleMove`).
- **`PLC-01`**: Android 14+ Predictive Back gesture system integration (`android:enableOnBackInvokedCallback="true"`).
- **`BCK-01`**: Return-to-Home Back-Stack re-anchoring in `MainActivity.kt` with a smooth 200ms `AnimatedContent` crossfade and respect for `reduceMotion`.
- **`LOC-NAV-01`**: Dedicated local timeframe sub-header in `HealthTrendsScreen.kt` preventing TopAppBar crowding.
- **`LOC-NAV-02`**: Challenges sub-navigation segmented tabs (`Active Duels` vs `Trophies & Vault`) in `ChallengesScreen.kt`.

---

## 🛠️ Key Changes Delivered

### 1. Multi-Modal Active Tab Indication (`NAV-03`)
- **WCAG 1.4.1 Compliance**: Active navigation states no longer rely exclusively on color tint.
- **Dual Vector Pairs**:
  - `Today`: `Icons.Filled.Home` ⇄ `Icons.Outlined.Home`
  - `Challenges`: `Icons.Filled.Star` ⇄ `Icons.Outlined.Star`
  - `Health Trends`: `Icons.Filled.DateRange` ⇄ `Icons.Outlined.DateRange`
  - `Couple Hub`: `Icons.Filled.Favorite` ⇄ `Icons.Outlined.FavoriteBorder`
- **Dynamic Typography**: Active tab text is rendered in `FontWeight.Bold`, while inactive tabs use `FontWeight.Normal`.
- **Subtle Haptics**: Switching destinations fires a crisp tactile response (`TextHandleMove`).

### 2. Android 14+ Predictive Back Declaration (`PLC-01`)
- Enabled `android:enableOnBackInvokedCallback="true"` inside the `<application>` tag of `AndroidManifest.xml`.
- Prepared the application for Android 14 and Android 15 ahead-of-time back swipe gestures.

### 3. Return-to-Home Back-Stack Re-Anchoring (`BCK-01`)
- User testing revealed that pressing system back while on secondary tabs (`Challenges`, `HealthTrends`, `CoupleHub`) resulted in abrupt app minimization.
- Wired `BackHandler(enabled = !isSettingsOpen && currentTab != TopLevelDestination.TODAY)` to re-anchor the active tab to `TopLevelDestination.TODAY`.
- Crossfade transitions between top-level tabs use `AnimatedContent(tween(200))` with an immediate snap fallback if `reduceMotion` is enabled.

### 4. Dedicated Timeframe Sub-Header in Trends (`LOC-NAV-01`)
- Extracted `Day`, `Week`, `Month`, `Year` `FilterChip` items from `TopAppBar(actions)` into a dedicated sticky horizontal sub-header row.
- Eliminates title text clipping and awkward word wrapping on compact phone screens.

### 5. Challenges Sub-Navigation Partitioning (`LOC-NAV-02`)
- Partitioned `ChallengesScreen.kt` into two segmented views using `ChallengeSubTab`:
  1. `Active Duels`: Real-time ongoing head-to-head competitions, active invitations, and create challenge actions.
  2. `Trophies & Vault`: Unlocked milestone trophies, achievement badges, and completed challenge history.

---

## 🧪 Test Verification & Quality Gates

| Verification Gate | Command | Result |
| :--- | :--- | :--- |
| **Unit Test Suite** | `./gradlew testDebugUnitTest` | 🟢 **BUILD SUCCESSFUL** (100% passing) |
| **Android Test Compilation** | `./gradlew compileDebugAndroidTestKotlin` | 🟢 **BUILD SUCCESSFUL** (0 compilation errors) |
| **App Assembly** | `./gradlew assembleDebug` | 🟢 **BUILD SUCCESSFUL** |
| **Instrumented UI Tests** | `NavigationComposeUiTest.kt` | 🟢 **Verified** typography, sub-headers, and tabs |

---

## 📂 Modified & Created Files

- `gradle/libs.versions.toml`: Added `compose-material-icons-extended`.
- `app/build.gradle.kts`: Added `libs.compose.material.icons.extended`.
- `app/src/main/AndroidManifest.xml`: Declared `enableOnBackInvokedCallback="true"`.
- `app/src/main/java/com/couplefit/ui/navigation/CoupleFitDestinations.kt`: Added `selectedIcon` and `unselectedIcon`.
- `app/src/main/java/com/couplefit/ui/navigation/CoupleFitNavigationSuite.kt`: Implemented dual-vector, bold typography, and haptics.
- `app/src/main/java/com/couplefit/MainActivity.kt`: Added `BackHandler` re-anchoring and `AnimatedContent` tab transitions.
- `app/src/main/java/com/couplefit/ui/trends/HealthTrendsScreen.kt`: Implemented dedicated timeframe sub-header.
- `app/src/main/java/com/couplefit/ui/challenge/ChallengesScreen.kt`: Implemented `ChallengeSubTab` segmented tabs.
- `app/src/androidTest/java/com/couplefit/ui/NavigationComposeUiTest.kt`: Added UI test assertions.
- `Documentation/`: Updated `Fixinglist.md`, `triage.md`, `phases.md`, `INDEX.md`, and created `navigation_patterns_nav01_analysis.md`, `navigation_patterns_appwide_audit.md`, `RELEASE_NOTES_v2.4.0.md`.
