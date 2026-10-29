# Phase 30 Plan: Color-Blind Ergonomics & Multi-Modal Metric Glyphs (CLR-03)

> **Phase**: 30  
> **Defect Key**: `CLR-03` (Cataloged in [`Documentation/Fixinglist.md`](file:///D:/Couple_fit/CoupleFit/Documentation/Fixinglist.md) and [`Documentation/triage.md`](file:///D:/Couple_fit/CoupleFit/Documentation/triage.md))  
> **WCAG Requirement**: WCAG 2.2 Success Criterion 1.4.1 (Use of Color - Level A) & 1.4.11 (Non-text Contrast - Level AA)  
> **Status**: In Progress  

---

## 1. Problem Statement & Root Cause Analysis

### Problem Statement
In previous iterations of the CoupleFit dashboard, activity metrics, telemetry chips, and competition standings relied heavily or solely on hue differentiation:
- Red steps (`#FF2D55` / `StepsAccent`) and Green exercise (`#32D74B` / `ExerciseAccent`) are perceived with identical or indistinguishable chromaticity by ~8% of male individuals experiencing Deuteranopia (green-weak/blind) or Protanopia (red-weak/blind).
- Competition text strings ("You're leading...", "Partner is leading...") differentiated winning from trailing solely via `SystemGreen` vs `SystemRed`.
- Dual activity gauge subtitles displayed raw text `"0m • 374 kcal"` without visual iconography to distinguish duration from caloric burn.
- Live Heart Rate cards displayed "Peak" and "Min" chips with color accents without directional glyph cues.
- The partner offline card displayed raw steps alongside pills without iconography.

### Root Cause
Components lacked multi-modal identification (pairing color with shape/glyph/icon). Under color-blind simulation filters, users cannot disambiguate metrics or standings without secondary shape cues.

---

## 2. Deliverables & Acceptance Criteria

1. **Multi-Modal Metric Glyphs in `MetricPill`**:
   - `MetricPill` in [`DashboardScreen.kt`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/ui/dashboard/DashboardScreen.kt) accepts an optional `glyph: String? = null`.
   - When provided, the glyph is rendered alongside the value in a horizontal alignment with proper spacing and text style.
   - `PartnerOfflineCard` passes:
     - Steps: `"👟"` (Footsteps)
     - Active Minutes: `"⏱️"` (Stopwatch)
     - Caloric Burn: `"🔥"` (Flame)
     - Average Heart Rate: `"❤️"` (Heart)

2. **Dual Activity Gauge Subtitle Disambiguation**:
   - In `SideBySideActivityCard`, change:
     - User subtitle: `"⏱️ ${healthData.activeMinutes}m  •  🔥 ${healthData.caloriesBurned} kcal"`
     - Partner subtitle: `"⏱️ ${healthData.partnerActiveMinutes}m  •  🔥 ${healthData.partnerCaloriesBurned} kcal"`

3. **Multi-Modal Competition Standings**:
   - In `DashboardScreen.kt` ("TODAY'S COMPETITION"):
     - User Leading: `"🏆 You're leading by ${stepDiff} steps!"`
     - Partner Leading: `"⚡ ${healthData.partnerName} is leading by ${-stepDiff} steps!"`
     - Tied: `"🤝 Tied at ${healthData.steps} steps! Keep moving!"`
   - In `ChallengeComponents.kt` (`DuelCard`):
     - User Leading: `"🏆 You're leading by $diff $unitLabel! 🔥"`
     - Partner Leading: `"⚡ $partnerName is leading by ${-diff} $unitLabel! Keep pushing! 💪"`
     - Tied: `"🤝 Tied neck-and-neck at ${challenge.myProgress} $unitLabel!"`

4. **Directional Peak / Min Telemetry Cues**:
   - In Live Heart Rate card:
     - Peak: `"▲ $peakVal Peak"` (Upward arrow)
     - Min: `"▼ $minVal Min"` (Downward arrow)

5. **Anatomical Glyph in Blood Oxygen Card**:
   - In `DashboardScreen.kt` partner SpO2 card:
     - Subtitle label: `"🫁 O2"`

6. **Adaptive Deep Dive Telemetry Multi-Modal Chips**:
   - In [`DashboardAdaptiveScreen.kt`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/ui/dashboard/DashboardAdaptiveScreen.kt):
     - `TelemetryMetric` enum enhanced with `val glyph: String`:
       - `STEPS`: `"👟"`
       - `HEART_RATE`: `"❤️"`
       - `ACTIVE_MINUTES`: `"⏱️"`
       - `CALORIES`: `"🔥"`
     - `FilterChip` displays `leadingIcon = { Text(metric.glyph, fontSize = 12.sp) }`.
     - Bilateral Stat Cards prepend `metric.glyph` to titles (`"Your ${selectedMetric.label}"`, `"${healthData.partnerName}'s ${selectedMetric.label}"`).

---

## 3. Test & Verification Bar

1. **Local Unit Tests**:
   - `./gradlew testDebugUnitTest` must pass with 100% success (32 tasks, 0 failures).
2. **Android Test Compilation**:
   - `./gradlew compileDebugAndroidTestKotlin` must compile with 0 errors.
3. **ARTEMIS Live Device Capture**:
   - Verify on `emulator-5554` (`Pixel_10_Pro`, API 36) with screenshot capture showing the rendered glyphs in the live UI hierarchy.
