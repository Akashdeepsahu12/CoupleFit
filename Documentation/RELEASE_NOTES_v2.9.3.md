# CoupleFit v2.9.3 Release Notes — Promoted Live Heart Rate Telemetry Dashboard & Unfolded Waveform 💓📈

> **Milestone Version**: `v2.9.3`  
> **Release Tag**: `v2.9.3-live-heart-rate`  
> **Release Date**: October 2026  
> **Governing Standards**: `/emil-design-eng`, `/taste-skill:design-taste-frontend`, WCAG 2.2 Level AA  
> **Associated Defect Logs**: `VHR-04`, `TST-09`, `ENG-05`

---

## 1. Executive Summary

CoupleFit **v2.9.3** promotes the live cardiovascular vitals monitor to a permanent, prominent first-class feature on the primary dashboard, directly resolving user feedback requesting that live heart rate not be hidden inside an expandable accordion dropdown:
1. **Unfolded Telemetry Surface (`VHR-04`)**: Completely removed `isVitalsChartExpanded` and the `"Expand ▼ / Collapse ▲"` toggle. The cardiovascular waveform is rendered immediately and permanently on the primary dashboard.
2. **Pulsing Heart Glyph & Live Telemetry Badge (`ENG-05`)**: Added a 24dp `AnimatedHeartIcon` pulsing softly in `VitalsAccent` with live BPM hero readout, alongside a glowing `"LIVE"` pill badge.
3. **Liquid Glass Surface Integration (`GlassTokens.kt`)**: The live waveform container is rendered on `.iosGlass(MaterialTheme.shapes.large)` with 3-stop specular top glints.
4. **Stable Layout Dynamics**: Eliminates 220px vertical layout shifts caused by dynamic expanding/collapsing.

---

## 2. Emil Kowalski Design Engineering Review

| Before | After | Why |
| --- | --- | --- |
| `var isVitalsChartExpanded by remember { mutableStateOf(false) }` (Waveform hidden by default) | Promoted permanently visible `InteractiveChart` on the dashboard | Health telemetry is primary situational context; hiding core vitals behind a click forces unnecessary friction. |
| `clickable { isVitalsChartExpanded = !isVitalsChartExpanded }` + `Text("Expand ▼ / Collapse ▲")` | Clean static card header with pulsing `AnimatedHeartIcon` and glowing `LIVE` badge | Users shouldn't have to fiddle with disclosures to inspect their heart rate; good defaults mean immediate clarity out of the box. |
| Accordion content toggle causing sudden 220px layout height jump (`animateContentSize`) | Stable 200dp waveform container with continuous real-time rendering | Height morphing on vital operational data causes content jank and disrupts reading flow. |
| Generic "📈" emoji header icon | `AnimatedHeartIcon` with live pulsing animation and `reduceMotion` protection | Domain-specific icons create emotional resonance and physical tactile life on OLED black. |

---

## 3. Verified Artifacts & Live Testing

| Artifact File | Description & Verification Proof |
| :--- | :--- |
| `live_heart_rate_unfolded_verified.jpg` | Verified permanently unfolded Live Heart Rate card, glowing LIVE badge, pulsing heart icon, and immediate visibility on primary dashboard. |

---

## 4. Test Suite & Verification Results

```bash
# Automated Unit Test Suite (100% Success)
.\gradlew.bat testDebugUnitTest
# Result: BUILD SUCCESSFUL in 34s (32 actionable tasks, 0 failures)

# Android Instrumentation Compilation Suite
.\gradlew.bat compileDebugAndroidTestKotlin
# Result: BUILD SUCCESSFUL in 5s (0 errors)

# Live Device Installation & Verification
.\gradlew.bat installDebug
# Result: BUILD SUCCESSFUL (installed on emulator-5554)
```

---

## 5. Master Defect & Requirement Traceability

| ID | Category | Status | Verification Evidence |
| :--- | :--- | :--- | :--- |
| `VHR-04` | Visual Hierarchy | **Resolved** | Permanent unfolded vitals pulse card in `DashboardScreen.kt` |
| `TST-09` | Design Taste | **Resolved** | Elimination of accordion dropdown disclosure gimmick |
| `ENG-05` | Design Engineering | **Resolved** | Pulsing `AnimatedHeartIcon` and glowing `LIVE` telemetry pill |
