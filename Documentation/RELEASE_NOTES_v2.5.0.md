# CoupleFit v2.5.0 Release Notes 🚀

> **Milestone**: Phase 7 / Phase 11 — User Flow Innovations & Telemetry Resilience  
> **Release Tag**: `v2.5.0-flow01-02-03`  
> **Date**: October 2026  
> **Governing Standards**: [`AGENTS.md`](../AGENTS.md), [`Documentation/INDEX.md`](INDEX.md), [`Documentation/Fixinglist.md`](Fixinglist.md)

---

## 🌟 Executive Summary

CoupleFit version 2.5.0 delivers the complete resolution of three critical user flow innovations:
- **`FLOW-01` (Zero-Tap Home Screen Glance Widget & Quick Poke)**: Built an interactive 4x2 Jetpack Glance Home Screen Widget with bilateral mini-gauges (`LinearProgressIndicator` for user and partner) and an inline interactive "Poke 👋" pill button powered by `PokeActionCallback`—firing nudges without cold-starting `MainActivity`.
- **`FLOW-02` (Double-Tap Ring to Cheer Micro-Interaction)**: Implemented direct manipulation on the partner's activity ring in `SideBySideActivityCard`. Double-tapping the partner gauge triggers tactile haptic feedback (`LongPress`), displays an animated floating heart particle burst ("❤️🔥"), and dispatches a celebratory cheer nudge with a 3-second debounce cooldown. A dedicated accessible click hint is also provided.
- **`FLOW-03` (Offline / Stale Telemetry Transparency & Diagnostics)**: Added partner telemetry staleness detection (>15 minutes delta or awaiting sync), rendering an interactive status pill (`🟡 Awaiting sync • Cached ℹ️` vs `🟢 Live ⚡`). Tapping the pill opens `TelemetryDiagnosticDialog` explaining offline Room SSOT caching and providing an immediate "Ping Partner Device 📡" action button with visual confirmation.

---

## 🛠️ Key Changes Delivered

### 1. Zero-Tap Glance Home Screen Widget (`FLOW-01`)
- **File**: [`CoupleFitAppWidget.kt`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/widget/CoupleFitAppWidget.kt)
- **Zero-Cold-Start Action Callback**: Created [`PokeActionCallback.kt`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/widget/PokeActionCallback.kt) executing via `partnerRepository` without launching `MainActivity`.
- **Bilateral Mini-Gauges**: Added horizontal progress bars (`LinearProgressIndicator`) in cyan for user and pink for partner.
- **Interactive Inline Poke**: Renders "Poke 👋" / "Poked! 💖" with a 4-second confirmation state.
- **Dependency Injection**: Augmented [`WidgetEntryPoint.kt`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/widget/WidgetEntryPoint.kt) with `partnerRepository(): PartnerRepository`.

### 2. Direct Manipulation Ring Cheer (`FLOW-02`)
- **File**: [`DashboardScreen.kt`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/ui/dashboard/DashboardScreen.kt)
- **Double-Tap Detection**: Added `pointerInput(Unit) { detectTapGestures(onDoubleTap = ...) }` to the partner's motion gauge container in `SideBySideActivityCard`.
- **Accessible Hint Affordance**: Added `.clickable` to "Double-tap ring to cheer 💖" hint text for TalkBack and single-tap accessibility modalities.
- **Celebratory Particles**: Animated floating text overlay (`AnimatedVisibility` with `scaleIn` + `fadeIn`) displaying `"❤️🔥"` over the gauge for 2.2 seconds.
- **Throttling & Cooldown**: Protected by a 3-second debounce cooldown (`cheerCooldownUntil`) preventing spamming.

### 3. Stale Telemetry Transparency & Recovery (`FLOW-03`)
- **File**: [`DashboardScreen.kt`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/ui/dashboard/DashboardScreen.kt)
- **Status Pill Indicator**: Rendered M3 `Surface(onClick = ...)` in `SideBySideActivityCard`:
  - `🟡 [LastUpdatedLabel] • Cached ℹ️` when delta > 15m or awaiting sync.
  - `🟢 [LastUpdatedLabel] • Live ⚡` when real-time telemetry is fresh.
- **Telemetry Diagnostic Dialog**: [`TelemetryDiagnosticDialog`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/ui/dashboard/DashboardScreen.kt) detailing partner sync status, explaining Room database offline caching, and providing an immediate "Ping Partner Device 📡" button.
- **Visual Feedback**: Displays a high-contrast confirmation banner (`✓ High-priority sync ping sent to Partner!`) upon pinging.

---

## 🧪 Test Verification & Quality Gates

| Verification Gate | Command | Result |
| :--- | :--- | :--- |
| **Unit Test Suite** | `./gradlew testDebugUnitTest` | 🟢 **BUILD SUCCESSFUL** (100% passing) |
| **Android Test Compilation** | `./gradlew compileDebugAndroidTestKotlin` | 🟢 **BUILD SUCCESSFUL** (0 errors) |
| **App Assembly** | `./gradlew assembleDebug` | 🟢 **BUILD SUCCESSFUL** |
| **Live Device Installation** | `adb install -r app-debug.apk` | 🟢 **Success** (`Pixel_10_Pro(AVD) - 17`) |
| **Instrumented Compose UI Tests** | `NavigationComposeUiTest.kt` | 🟢 **Passed** status pill, dialog, ping, cheer, and widget poke |
| **ARTEMIS Live Device** | `mobile_get_device_state` | 🟢 **Verified** hierarchy, dialogs, and cheer hints |

---

## 📸 Visual Artifacts
- [`flow01_02_03_today_screen.jpg`](file:///C:/Users/akash/.gemini/antigravity-cli/brain/66d03291-2236-4d36-b172-ec4997fdf3ca/flow01_02_03_today_screen.jpg): Today dashboard with `🟡 Awaiting sync • Cached ℹ️` status pill and "Double-tap ring to cheer 💖" hint.
- [`flow03_telemetry_diagnostic_dialog.jpg`](file:///C:/Users/akash/.gemini/antigravity-cli/brain/66d03291-2236-4d36-b172-ec4997fdf3ca/flow03_telemetry_diagnostic_dialog.jpg): Partner Sync Diagnostics dialog with Room SSOT explanation and "Ping Partner Device 📡" action.
- [`flow03_ping_partner_confirmed.jpg`](file:///C:/Users/akash/.gemini/antigravity-cli/brain/66d03291-2236-4d36-b172-ec4997fdf3ca/flow03_ping_partner_confirmed.jpg): High-priority sync ping confirmation banner inside diagnostic dialog.
