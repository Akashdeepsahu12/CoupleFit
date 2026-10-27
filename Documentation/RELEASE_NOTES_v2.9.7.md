# CoupleFit Release Notes — Version 2.9.7 🎨✨
**Release Title**: Dashboard UI Design System Harmonization & Heart Rate Convergence  
**Release Tag**: `v2.9.7-dashboard-ui-harmonization`  
**Defects Resolved**: `DSN-05`, `LAY-02` in `Fixinglist.md` and `triage.md`  
**Target Platform**: Android 10+ (API 29–36) | Verified on Pixel 10 Pro (`emulator-5554`)  
**Operating Standard**: Google ARTEMIS Mobile Automation Harness & `AGENTS.md`

---

## 1. Executive Summary & Defect Diagnosis
Following live device observation, the user reported that the Heart Rate card:
> *"doesn't match with the ui"*

A visual audit across the CoupleFit dashboard revealed that the Heart Rate card had deviated from the app's established design system across four critical dimensions:
1. **Material & Color Discordance**: All surrounding cards (`SideBySideActivityCard`, `Standings Card`, `ActiveChallengePreviewCard`, `StreakTracker`) use `Modifier.iosGlass()` with the app's translucent slate-black base (`0xCC141720`), subtle vertical gradient sheen, and 1.dp diamond-cut specular rim border (`BorderSpecularRim`). The Heart Rate card was using custom Liqui Design dark indigo/navy blue tint tokens (`#303246` / `#121420`), causing it to appear as an alien purple block on an obsidian dashboard.
2. **Corner Radius Geometry Mismatch**: All dashboard cards adhere strictly to `MaterialTheme.shapes.large` (16dp rounded corners). The Heart Rate card was using 22dp squircle corners (`LiquiTokens.CardRadius`), resulting in bulbous corners that broke the visual grid.
3. **Typographic Hierarchy Mismatch**: All dashboard cards feature an uppercase, bold, tracked category eyebrow (`style = labelSmall, letterSpacing = 0.5.sp`) with a 16dp icon on the left and a 12dp status pill on the right. The Heart Rate card was using plain mixed-case `Text("Heart Rate", style = titleMedium)`, breaking visual scanning cadence.
4. **Nested Cutout Box**: `LiveHeartRateEmptyWaveform` was nesting an inner dark box with a contrasting solid blue button inside the card, conflicting with the app's seamless, breathing glass surfaces.
5. **Card Width Misalignment**: `ActiveChallengePreviewCard` duplicated horizontal padding, making it narrower than adjacent cards.

---

## 2. Architectural Solution
1. **Unified Glassmorphic Surface**:
   - Replaced `.liquiGlass()` with `Modifier.iosGlass(shape = MaterialTheme.shapes.large)` on the Heart Rate card in `DashboardScreen.kt`.
   - Card background and border now share the exact same slate glassmorphism and specular sheen as the surrounding cards.
2. **Aligned Typography & Category Header**:
   - Upgraded header to:
     - Left: `AnimatedHeartIcon(16.dp, tint = VitalsAccent)` + `Text("LIVE HEART RATE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 0.5.sp)`.
     - Right: Telemetry status pill (`🟢 Live Telemetry` / `⚪ Health Connect`) matching `SideBySideActivityCard`.
3. **Refined Ambient ECG Waveform Stage (`LiveHeartRateEmptyWaveform.kt`)**:
   - Removed nested dark cutout box.
   - Fluid P-Q-R-S-T cardiogram baseline and glowing traveling pulse bead (`phase * w`) draw directly on the card's frosted glass surface.
   - Styled "Sync Health Connect ↻" as a sleek frosted glass pill button matching `QuickNudgeRow`.
4. **Width & Margin Realignment (`ActiveChallengePreviewCard.kt`)**:
   - Standardized `ActiveChallengePreviewCard` to `.padding(vertical = AppSpacing.xs)` and `shape = MaterialTheme.shapes.large`, restoring 100% unified card margins (`[37, ...][962, ...]`).

---

## 3. Verification & Quality Gates
- **Automated Unit Tests**: `./gradlew testDebugUnitTest` passed with 100% green bar (41 actionable tasks).
- **Android Test Compilation**: `./gradlew compileDebugAndroidTestKotlin` compiled with 0 errors.
- **Live Device ARTEMIS Verification (`emulator-5554`)**:
  - Captured `dashboard_ui_harmonized_verified.jpg`: Visually confirmed 100% seamless harmony across `TODAY'S COMPETITION`, `ACTIVE CHALLENGE`, and `LIVE HEART RATE` with identical 16dp corner geometry, unified width alignment, slate frosted glass translucency, and crisp typography.
