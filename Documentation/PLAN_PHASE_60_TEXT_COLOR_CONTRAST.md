# Phase 60: Text Color & Contrast Hardening (`TXT-01`, `TXT-02`, `TXT-03`)

## 1. Executive Summary & Root Cause Analysis
During Phase 59 verification, users reported: *"fix all the text color"*.
Upon deep inspection of device state and telemetry screenshots, a critical theming and contrast defect was discovered:
1. **Light / Dark Mode Canvas Disconnect**:
   - `MainActivity.kt` initialized `isDarkTheme` via `prefs.getBoolean("is_dark_theme", isSystemInDarkTheme())`.
   - On Android devices/emulators set to system light mode (`Night mode: no`), `isDarkTheme` evaluated to `false`.
   - In `Theme.kt`, `LightColorScheme` correctly assigned dark text tokens (`TextPrimaryLight = Color(0xFF000000)`, `TextSecondaryLight = Color(0x993C3C43)`).
   - However, `AmbientVideoBackground.kt` was hardcoded to an obsidian black scrim (`Color(0xFF07080A)` with 0.45f - 0.80f opacity) over dark ambient video, and cards were transparent glass (`containerColor = Color.Transparent`).
   - Consequently, when in light mode, pitch-black and dark grey text was rendered directly on top of a pitch-black background, making the entire interface illegible.
2. **Sub-Optimal Dark Mode Alpha Tokens (`Color.kt`)**:
   - `TextSecondaryDark = Color(0x99EBEBF5)` (60% alpha). Over frosted glass or ambient video luminance, contrast dropped to ~4.8:1, failing high-readability standards.
   - `TextTertiaryDark = Color(0x4DEBEBF5)` (30% alpha). Severely degraded contrast (~2.2:1), creating muddy, washed-out timestamps, overlines, and metric units.
3. **Compounded Opacity Degradation in UI Components**:
   - Throughout `DashboardScreen.kt`, `SideBySideActivityCard.kt`, `MotionArcGauge.kt`, and `InteractiveChart.kt`, elements were styled with `color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)` or `copy(alpha = 0.7f)`.
   - Since `onSurfaceVariant` was already 60% alpha, multiplying by 0.6f resulted in an effective opacity of 36%, rendering text nearly invisible.
   - Chart gridline Y-axis labels in `InteractiveChart.kt` used `argb(160, ...)`, making scales faint.
   - Unselected navigation bar items in `CoupleFitNavigationSuite.kt` used low-contrast `Color(0xFF8E95A5)`.

---

## 2. Milestone Objectives & Work Breakdown Structure (WBS)

### Wave 1: Global Theming & Canvas Contrast Foundation (`TXT-01`)
- **Task 1.1**: Default `is_dark_theme` to `true` in `MainActivity.kt` (`prefs.getBoolean("is_dark_theme", true)`), ensuring the flagship Obsidian Apple Fitness aesthetic is always preserved by default.
- **Task 1.2**: Update `AmbientVideoBackground.kt` to dynamically adjust `scrimBrush` based on theme:
  - Dark Theme: Obsidian scrim (`Color(0xFF07080A)` 0.50f - 0.85f).
  - Light Theme: Luminous frosted pearl scrim (`Color(0xFFF8FAFC)` 0.85f - 0.96f), ensuring WCAG 2.2 AAA contrast (> 10:1) for dark typography on light mode.

### Wave 2: Semantic Color Calibration & Contrast Enhancement (`TXT-02`)
- **Task 2.1**: Update `Color.kt` text tokens:
  - `TextPrimaryDark = Color(0xFFFFFFFF)` (100% pure white)
  - `TextSecondaryDark = Color(0xD9EBF2FA)` (85% alpha high-contrast secondary text)
  - `TextTertiaryDark = Color(0xA6CBD5E1)` (65% alpha tertiary text)
  - `TextPrimaryLight = Color(0xFF0F172A)` (Slate-900 high readability)
  - `TextSecondaryLight = Color(0xFF334155)` (Slate-700 secondary text)
  - `TextTertiaryLight = Color(0xFF64748B)` (Slate-500 tertiary text)
- **Task 2.2**: Update `Theme.kt` `DarkColorScheme` and `LightColorScheme` to map semantic text tokens cleanly to `onSurface`, `onSurfaceVariant`, `onBackground`, and `outlineVariant`.

### Wave 3: UI Screen Contrast Hardening & Compound Opacity Pruning (`TXT-03`)
- **Task 3.1**: Prune compound `.copy(alpha = ...)` on text colors in:
  - `DashboardScreen.kt` (banner subtitles, sync status, standing cards)
  - `SideBySideActivityCard.kt` (metric chips, activity labels)
  - `MotionArcGauge.kt` (goal labels, unit captions, weekly streak days)
  - `InteractiveChart.kt` (chart axis values, scrub captions, delta labels; boost Y-axis paint alpha from 160 to 220)
  - `CoupleFitNavigationSuite.kt` (unselected tab icon and text color boosted to high-contrast slate)

---

## 3. Explicit Acceptance Criteria
- [ ] 1. Dark Mode text contrast on all primary surfaces exceeds 7:1 for body and 4.5:1 for tertiary captions.
- [ ] 2. Light Mode text contrast on all primary surfaces exceeds 10:1 against frosted canvas.
- [ ] 3. No text element in the dashboard or primary screens drops below 60% effective alpha.
- [ ] 4. All automated unit tests (`./gradlew testDebugUnitTest`) pass 100% green.
- [ ] 5. Android Test Kotlin compilation (`./gradlew compileDebugAndroidTestKotlin`) succeeds with 0 errors.
- [ ] 6. Live emulator verification via ARTEMIS confirms vibrant, crisp typography across all screens without clipping or dimness.
