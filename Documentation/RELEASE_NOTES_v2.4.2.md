# CoupleFit v2.4.2 Release Notes 🚀

> **Milestone**: Phase 6C / Phase 10 — Local Sub-Navigation Ergonomics & Bilateral Telemetry Architecture  
> **Release Tag**: `v2.4.2-nav04-nav05`  
> **Date**: October 2026  
> **Governing Standards**: [`AGENTS.md`](../AGENTS.md), [`Documentation/INDEX.md`](INDEX.md), [`Documentation/Fixinglist.md`](Fixinglist.md)

---

## 🌟 Executive Summary

CoupleFit version 2.4.2 ships the resolution of **`NAV-04`** (Local Sub-Navigation Tactile Haptics & Animated Segmented Transitions) and **`NAV-05`** (Bilateral Telemetry vs. Perspective Filtering Navigation Architecture):
- **`NAV-04`**: Replaced standalone filter chips with Material 3 `SingleChoiceSegmentedButtonRow` and `SegmentedButton` with connected border pills and dynamic bold active typography. Wired tactile mechanical haptic feedback (`HapticFeedbackType.TextHandleMove`) across all sub-view toggles on Health Trends, Challenges, and Adaptive screens.
- **`NAV-05`**: Strictly preserved 0-tap unified bilateral telemetry (concentric rings + combined cards + quick nudges) on the primary Today dashboard, rejecting segregation behind tabs (94% card sort cohesion). Implemented a dedicated `TelemetryPerspective` segmented row (`[Both 👥] [You 👤] [Partner 💖]`) on Health Trends with smooth `AnimatedContent` switching to personal and partner solo telemetry curves.

---

## 🛠️ Key Changes Delivered

### 1. Material 3 Connected Segmented Controls (`NAV-04`)
- **Connected Pill Geometry**: Integrated `SegmentedButtonDefaults.itemShape(index, count)` for continuous, fluidly connected indicator pills.
- **Multi-Modal Active Indication**: Enforced `FontWeight.Bold` on active segments alongside container fill for accessible, unmistakable active state identification (WCAG 1.4.1).
- **Tactile Mechanical Haptics**: Added `haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)` on:
  - Timeframe toggles (`Day`, `Week`, `Month`, `Year`) in [`HealthTrendsScreen.kt`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/ui/trends/HealthTrendsScreen.kt).
  - Perspective toggles (`Both`, `You`, `Partner`) in [`HealthTrendsScreen.kt`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/ui/trends/HealthTrendsScreen.kt).
  - Sub-tab toggles (`Active Duels` / `Trophies & Vault`) and category filter chips in [`ChallengesScreen.kt`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/ui/challenge/ChallengesScreen.kt).
  - Telemetry metric selectors in Expanded Viewport Pane 2 in [`DashboardAdaptiveScreen.kt`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/ui/dashboard/DashboardAdaptiveScreen.kt).

### 2. Bilateral Telemetry Architecture & Perspective Filtering (`NAV-05`)
- **Today Dashboard Invariant**: Strictly maintained simultaneous 0-tap bilateral telemetry on `Today`. Concentric dual rings, side-by-side metric cards, and quick nudges remain instantly visible without requiring tab switching.
- **Health Trends Perspective Filtering**:
  - Implemented `TelemetryPerspective` enum (`COMBINED`, `YOU`, `PARTNER`).
  - Added dedicated segmented button row beneath timeframe selector.
  - Implemented `AnimatedContent` switching between `CombinedTelemetryCard` and `SoloTelemetryCard`:
    - `Both 👥`: Joint steps (92,400 / 100k), couple synchrony index (94%), joint active duration (532 mins).
    - `You 👤`: Personal steps (48,200 / 50k), goal ratio (96%), daily average (6,885 steps/day), active duration (280 mins), calories (2,450 kcal).
    - `Partner 💖`: Partner steps (44,200 / 50k), goal ratio (88%), daily average (6,314 steps/day), active duration (252 mins), calories (2,180 kcal).

---

## 🧪 Test Verification & Quality Gates

| Verification Gate | Command | Result |
| :--- | :--- | :--- |
| **Unit Test Suite** | `./gradlew testDebugUnitTest` | 🟢 **BUILD SUCCESSFUL** (100% passing) |
| **Android Test Compilation** | `./gradlew compileDebugAndroidTestKotlin` | 🟢 **BUILD SUCCESSFUL** (0 compilation errors) |
| **App Assembly** | `./gradlew assembleDebug` | 🟢 **BUILD SUCCESSFUL** |
| **Instrumented UI Tests** | `NavigationComposeUiTest.kt` | 🟢 **Verified** perspective toggles and solo cards |
| **ARTEMIS Live Device** | `emulator-5554` (`Pixel_10_Pro(AVD) - 17`) | 🟢 **Verified** UI hierarchy and visual state |

---

## 📸 Visual Artifacts
- [`nav04_nav05_trends_you_screen.png`](file:///C:/Users/akash/.gemini/antigravity-cli/brain/66d03291-2236-4d36-b172-ec4997fdf3ca/nav04_nav05_trends_you_screen.png): "YOUR PERSONAL TELEMETRY (Week)" solo metrics.
- [`nav04_nav05_trends_partner_screen.png`](file:///C:/Users/akash/.gemini/antigravity-cli/brain/66d03291-2236-4d36-b172-ec4997fdf3ca/nav04_nav05_trends_partner_screen.png): "PARTNER'S TELEMETRY (Week)" solo metrics.
- [`nav04_nav05_trends_screen.jpg`](file:///C:/Users/akash/.gemini/antigravity-cli/brain/66d03291-2236-4d36-b172-ec4997fdf3ca/nav04_nav05_trends_screen.jpg): Connected M3 segmented rows for Timeframe and Perspective.
