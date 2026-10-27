# Phase 19: Live Heart Rate Layout Architecture Polish & Ambient Waveform (UI-UX Pro Max)

**Status**: ACTIVE  
**Goal**: Resolve layout flaws, header redundancy, squeezed "LIVE" badge wrapping, harsh divider slicing, and cavernous empty states in the dashboard Live Heart Rate card, elevating it to an elite, polished visual standard adhering to `ui-ux-pro-max` and Emil Kowalski design engineering principles.

---

## 1. Problem Diagnosis & Defects Addressed

- **`VHR-05` (Squeezed "LIVE" Badge & Text Wrapping)**:
  - *Symptom*: In the card header row, `Text("Live Heart Rate • ...")` was unweighted, pushing the `LIVE` pill badge against the card boundary and crushing its width to 17px (`[889,468][906,532]`), causing `"L-I-V-E"` to wrap vertically character-by-character.
  - *Root Cause*: Missing `Modifier.weight(1f, fill = false)` on the title column and missing `softWrap = false` / `maxLines = 1` on the badge text.
  - *Fix*: Apply `weight(1f, fill = false)` to title group, enforce `softWrap = false` and `maxLines = 1` on badge pill with dedicated horizontal padding.

- **`DSN-04` (Header Redundancy & Harsh Divider Slicing)**:
  - *Symptom*: When data is loaded, the card displayed a custom header with `Live Heart Rate • 72 BPM Avg`, sliced by a stark `HorizontalDivider`, followed immediately by `InteractiveChart` displaying another duplicate header (`72 BPM`, `HEART RATE PULSE (BPM)`, `Daily Avg ...`).
  - *Root Cause*: Wrapping `InteractiveChart` without controlling its internal header rendering (`showHeader`).
  - *Fix*: Add `showHeader: Boolean = true` to `InteractiveChart.kt` (defaulting to true for backward compatibility). On Tab 0, set `showHeader = false` and unify all metric, peak/min, and status readouts into a single authoritative hero header. Eliminate `HorizontalDivider` to preserve liquid frosted glass continuity.

- **`ENG-06` (Cavernous Empty State ➔ Living Ambient ECG Canvas)**:
  - *Symptom*: When awaiting data or in zero-BPM state, the 200dp container rendered a stark, empty gray rectangle with two plain sentences, feeling disconnected and broken.
  - *Fix*: Create `LiveHeartRateEmptyWaveform` featuring an ambient ECG cardiogram rhythm canvas (`───/\_/\_/\───`) in `VitalsAccent`, an animated traveling scanning bead, reassuring typography, and an interactive "Sync Now ↻" quick-action button.

---

## 2. Acceptance Criteria & Test Bars

1. **Badge Integrity**:
   - The `LIVE` badge on the top right must render horizontally on a single line (`[ ● LIVE ]`) with zero vertical wrapping.
2. **Hero Metric Hierarchy**:
   - Big Number readout (`displaySmall`) for BPM (`72` or `--`), unit suffix `BPM`, and context label (`• Today's Avg` or `• Listening for pulse`), baseline-aligned.
3. **Zero Redundancy**:
   - No duplicate headers or stacked BPM values.
4. **Living Ambient State**:
   - Awaiting state renders `LiveHeartRateEmptyWaveform` with an animated ECG baseline and a working "Sync Now" trigger.
5. **Deterministic Verification**:
   - `.\gradlew.bat testDebugUnitTest` 100% green.
   - `.\gradlew.bat compileDebugAndroidTestKotlin` 100% green.
   - Live Android verification via ARTEMIS on `emulator-5554` (`hierarchy` and `screenshot`).
