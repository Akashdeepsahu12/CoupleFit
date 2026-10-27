# Phase 18 Milestone Plan — Promoted Live Heart Rate Telemetry Dashboard & Unfolded Waveform 💓📈

> **Milestone Tag**: `v2.9.3-live-heart-rate`  
> **Governing Standards**: `/emil-design-eng`, `/taste-skill:design-taste-frontend`, WCAG 2.2 Level AA  
> **Associated Defect & Enhancement Logs**: `VHR-04`, `TST-09`, `ENG-05`

---

## 1. Executive Summary & Intent

In previous iterations, the primary vitals waveform on the Side-by-Side dashboard was tucked behind a collapsed disclosure accordion (`var isVitalsChartExpanded by remember { mutableStateOf(false) }`), requiring the user to tap `"Expand ▼"` to view their live heart rate data.

In accordance with **Emil Kowalski's Design Engineering Philosophy** ("Good defaults matter more than options. Ship beautiful out of the box"), real-time cardiovascular telemetry is core operational information in a fitness app, not secondary metadata. This milestone eliminates the collapsed accordion gimmick and promotes the **Live Heart Rate & Vitals Pulse Waveform** to a permanent, prominent, and beautifully rendered first-class citizen directly on the primary dashboard:
1. **Permanent Surface Display**: Remove `isVitalsChartExpanded` and the accordion toggle. The vitals waveform is open and live immediately on screen load.
2. **Dynamic Live BPM Readout**: Display the live/average heart rate in bold tabular typography with an animated pulsing heart glyph (`AnimatedHeartIcon`) that beats with life.
3. **Liquid Glass Aesthetic & Specular Rim**: Encased in `.iosGlass(shape = MaterialTheme.shapes.large)` with 14% top glint and soft ambient shadow.
4. **Stable Non-Jumping Layout**: Eliminates 220px vertical layout shifts caused by dynamic expanding/collapsing.

---

## 2. Explicit Acceptance Criteria & Test Bars

### Criteria 1: No Accordion or Hidden Dropdown
- [ ] Heart rate chart is rendered immediately without requiring user interaction or tapping "Expand ▼".
- [ ] `isVitalsChartExpanded` state and `"Expand ▼ / Collapse ▲"` text controls are completely removed from `DashboardScreen.kt`.

### Criteria 2: Living Heart Rate Header
- [ ] Header features `AnimatedHeartIcon` pulsing softly in `VitalsAccent` (`#00F5D4`).
- [ ] Displays live BPM / average BPM with tabular figures (`tnum`) for rock-solid stability.

### Criteria 3: Permanent Interactive Waveform
- [ ] 200dp interactive spline chart container rendered directly on the glass card.
- [ ] Graceful empty state when no records are synced today.

### Criteria 4: Verification & Test Bar
- [ ] `./gradlew.bat testDebugUnitTest` passes 100% (32/32 tasks).
- [ ] `./gradlew.bat compileDebugAndroidTestKotlin` compiles with 0 errors.
- [ ] Live Android execution verified on `emulator-5554` via ARTEMIS with screenshot proof.

---

## 3. Task Breakdown & Dependency Order

1. **Task 1: Unfold Live Heart Rate Telemetry Card (`DashboardScreen.kt`)**:
   - Replace collapsible `Card` with a permanently open, prominent `Card`.
   - Remove `isVitalsChartExpanded`, `clickable`, and `"Expand ▼"` label.
   - Embed `InteractiveChart` directly below the metric header with subtle divider.
2. **Task 2: Automated Test Suite Update (`NavigationComposeUiTest.kt`)**:
   - Add unit/instrumentation assertion verifying permanent visibility of `"Live Heart Rate"` and `"Heart Rate Pulse (BPM)"`.
3. **Task 3: Deterministic Verification & ARTEMIS Proof**:
   - Run Gradle suites, install updated APK, and capture live device screenshot proof on `emulator-5554`.
4. **Task 4: Documentation Synchronization & Git Shipment**:
   - Update `Fixinglist.md`, `triage.md`, `phases.md`, and `INDEX.md`.
   - Stage, commit with conventional message, and tag `v2.9.3-live-heart-rate`.
