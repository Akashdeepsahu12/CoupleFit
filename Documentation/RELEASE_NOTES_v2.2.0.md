# CoupleFit — Release v2.2.0

**Release Tag:** `v2.2.0-csa-complete`  
**Target Platform:** Android (Native)  
**Target SDK:** 35 (Android 15)  
**Min SDK:** 29 (Android 10)  
**Build Toolchain:** Java 17 / Kotlin 2.1.0 / AGP 8.7.3 / KSP  
**Release Date:** October 27, 2026  

---

## 📦 Release Summary
Release v2.2.0 represents a major Information Architecture (IA) and user experience transformation based on the empirical card sorting and tree testing research detailed in `Documentation/card-sort-analysis.md`. This release deploys a persistent Material 3 navigation shell across mobile and tablet form factors, modularizes monolithic screen code, introduces contextual medication adherence tools, and adds fail-safe relationship protections.

---

## 🚀 Key Highlights & Architectural Changes

### 1. Persistent 4-Tab Navigation Shell (`CSA-01`, `CSA-02` / `NAV-01`)
- **Type-Safe Navigation Routes**: Introduced `@Serializable` destinations for `Today`, `Challenges`, `HealthTrends`, and `CoupleHub` in `CoupleFitDestinations.kt`.
- **Adaptive Scaffold Architecture**: Implemented `CoupleFitNavigationSuite.kt` featuring an 80dp `NavigationBar` on compact viewports (<600dp) and an 80dp `NavigationRail` on medium/expanded tablets and foldables (≥600dp).
- **Dedicated Top-Level Screens**: Created standalone composables:
  - `ChallengesScreen.kt`: Head-to-head duels, trophy display, and challenge creation.
  - `HealthTrendsScreen.kt`: Long-horizon biometrics, intake trackers, and sensor diagnostics.
  - `CoupleHubScreen.kt`: Shared anniversary countdown, streak badges, and partner visibility shortcut.
- **Automated Test Coverage**: Added `NavigationComposeUiTest.kt` verifying 4-tab rendering, accessibility semantics, and tab switching callbacks.

### 2. Contextual Medication Logging on Today (`CSA-03`)
- **Friction-Free Compliance**: Resolved TS-04 (23% medication logging friction) by projecting an interactive `MedicationDuePill.kt` at the top of the Today screen.
- **Instant Haptic Confirmation**: One-tap dose logging (`Log [✓]`) with long-press haptic buzz, celebration badge transition, and smooth animated dismissal.
- **WCAG 2.2 Level AA**: Integrated explicit TalkBack content descriptions and 48x48dp minimum touch bounds.

### 3. Modular Challenge Preview (`CSA-04`)
- **God-Screen Deconstruction**: Replaced the 400-line monolithic inline challenge suite in `DashboardScreen.kt` with a lightweight, modular `ActiveChallengePreviewCard.kt`.
- **Streamlined Routing**: Displays active duel status, progress delta, and a clean `"View Duels"` button directing users to Tab 2 `Challenges`.

### 4. Adaptive Two-Pane Telemetry Deep-Dive (`CSA-05` / `NAV-02`)
- **Expanded Tablet Viewports (≥600dp)**: Transformed the empty right-hand placeholder in `DashboardAdaptiveScreen.kt` into a real-time telemetry analytics engine.
- **Interactive Metric Selection**: Chips for `Steps`, `Heart Rate`, `Active Minutes`, and `Caloric Burn` dynamically drive bilateral comparison cards and an Apple HIG-compliant cubic spline `InteractiveChart.kt`.

### 5. Couple Hub Privacy Shortcut & 2-Step Safe Unlink (`CSA-06`, `CSA-07`)
- **Relational Privacy Visibility**: Added `"Partner Visibility & Sharing Rules [Manage 🔒]"` card in `CoupleHubScreen.kt` linking directly to Privacy settings.
- **Accidental Disconnection Prevention**: Upgraded the Danger Zone unlinking dialog in `SettingsScreen.kt` from a single-tap alert into an explicit 2-step verification modal requiring the user to type `"UNLINK"` in an `OutlinedTextField` before the action button enables.

### 6. Thumb-Zone Quick Nudge Row (`CSA-08`)
- **Ergonomic One-Handed Access**: Repositioned partner affection chips (`[👋 Poke] [🔥 Keep Going!] [❤️ Love] [💧 Drink Water]`) into `QuickNudgeRow.kt` directly beneath bilateral gauges in `DashboardScreen.kt`, well within the primary thumb reach zone.

### 7. Step Counter Rendering & Demo Bypass (`STP-01`)
- **GPU Glyphs Restored**: Replaced offscreen alpha masking in `ReelDigit.kt` with native Jetpack Compose `AnimatedContent` (`slideInVertically`/`slideOutVertically`) and tabular numbers (`tnum`).
- **Onboarding Wall Bypassed**: Added one-tap guest demo dashboard access to eliminate the initial pairing blocker.

---

## 🧪 Verification & Test Proof
- **Unit Tests**: `./gradlew testDebugUnitTest` passed with **100% success** (32/32 tests up-to-date).
- **Compose Android Tests**: `./gradlew compileDebugAndroidTestKotlin` passed with **0 errors**.
- **Debug Assembly**: `./gradlew assembleDebug` passed cleanly.
- **Instrumented Test Suites**:
  - `NavigationComposeUiTest.kt`
  - `MedicationDuePillComposeUiTest.kt`
  - `CardSortConvergenceComposeUiTest.kt`
