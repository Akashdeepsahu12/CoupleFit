# Release Notes v2.9.15: Color-Blind Ergonomics & Multi-Modal Metric Glyphs (CLR-03)

> **Release Version**: `v2.9.15`  
> **Tag**: `v2.9.15-colorblind-ergonomics-clr03`  
> **Associated Defect Key**: `CLR-03` (Cataloged in [`Documentation/Fixinglist.md`](file:///D:/Couple_fit/CoupleFit/Documentation/Fixinglist.md) and [`Documentation/triage.md`](file:///D:/Couple_fit/CoupleFit/Documentation/triage.md))  
> **WCAG Compliance**: WCAG 2.2 Success Criterion 1.4.1 (Use of Color - Level A) & 1.4.11 (Non-text Contrast - Level AA)  

---

## 1. Overview & Purpose
In previous versions of CoupleFit, activity telemetry, vital status chips, and head-to-head competition standings relied solely on hue differentiation (`#FF2D55` Red steps vs `#32D74B` Green workouts, and `SystemGreen` vs `SystemRed` winner/loser text). For approximately 8% of male users experiencing Deuteranopia or Protanopia, red and green hues appear chromatically identical, obscuring whether they are leading or trailing and which metric belongs to which category.

With **v2.9.15**, CoupleFit enforces universal **Multi-Modal Identification**: every color is paired with an unmistakable icon glyph and directional shape cue, ensuring 100% accessible comprehension for all users regardless of color perception.

---

## 2. Key Enhancements & Architectural Changes

1. **Multi-Modal `MetricPill` Glyphs**:
   - `MetricPill(label, value, color, glyph = null)` in [`DashboardScreen.kt`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/ui/dashboard/DashboardScreen.kt) renders high-clarity icon glyphs beside numerical readouts:
     - Steps: `"👟"` (Footsteps)
     - Active Minutes: `"⏱️"` (Stopwatch)
     - Caloric Burn: `"🔥"` (Flame)
     - Average Heart Rate: `"❤️"` (Heart)
   - `PartnerOfflineCard` displays `"👟"` alongside partner's spinning step counter.

2. **Dual Activity Gauge Subtitle Disambiguation**:
   - Subtitles under user and partner activity gauges updated from plain text to multi-modal indicators:
     - User: `"⏱️ ${activeMinutes}m  •  🔥 ${caloriesBurned} kcal"`
     - Partner: `"⏱️ ${partnerActiveMinutes}m  •  🔥 ${partnerCaloriesBurned} kcal"`

3. **Multi-Modal Competition Standings**:
   - Prepend distinct shape and trophy glyphs to eliminate color-only standing indicators:
     - User Leading: `"🏆 You're leading by ${stepDiff} steps!"`
     - Partner Leading: `"⚡ ${healthData.partnerName} is leading by ${-stepDiff} steps!"`
     - Tied: `"🤝 Tied at ${healthData.steps} steps! Keep moving!"`
   - In [`ChallengeComponents.kt`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/ui/challenge/ChallengeComponents.kt), head-to-head duel cards pair leading states with `🏆`, trailing states with `⚡`, and tied states with `🤝`.

4. **Directional Telemetry Indicators**:
   - In the Live Heart Rate Card, Peak and Min chips feature directional triangles:
     - Peak: `"▲ $peakVal Peak"`
     - Min: `"▼ $minVal Min"`

5. **Anatomical Glyphs**:
   - Blood Oxygen (SpO2) card now pairs with `"🫁 O2"`.

6. **Adaptive Deep Dive Telemetry**:
   - In [`DashboardAdaptiveScreen.kt`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/ui/dashboard/DashboardAdaptiveScreen.kt), `TelemetryMetric` enum contains `val glyph: String`, and `FilterChip` renders `leadingIcon = { Text(metric.glyph) }`.

---

## 3. Verification & Live Device Proof
- `.\gradlew.bat testDebugUnitTest`: **BUILD SUCCESSFUL** (100% green bar, 32 tasks).
- `.\gradlew.bat compileDebugAndroidTestKotlin`: **BUILD SUCCESSFUL** (0 errors, 31 tasks).
- `.\gradlew.bat installDebug`: **BUILD SUCCESSFUL** (Installed on `emulator-5554`, Pixel 10 Pro API 36).
- **Verified ARTEMIS Evidence**:
  - `clr03_verified_dashboard.jpg`
  - `clr03_verified_scrolled.jpg`
  - `clr03_verified_vitals.jpg`
  - `clr03_verified_challenges_tab.jpg`
