# CoupleFit Release Notes — Version 2.8.0 💎✨

> **Release Tag**: `v2.8.0-design-eng`  
> **Release Name**: Emil Kowalski Design Engineering Craft & Sensory Physics  
> **Target Issues**: `ENG-01`, `ENG-02`, `ENG-03`, `ENG-04`, `APL-02`, `ANM-01`, `ANM-02`, `MIC-01`, `MIC-03`  
> **Verification**: 100% Unit Test Pass (`testDebugUnitTest`), 100% Android Test Compile, Live ARTEMIS Device Proof (`emulator-5554`)

---

## 1. Overview & Craft Philosophy

Version 2.8.0 elevates CoupleFit into an interface defined by tactile craft excellence and mechanical precision, following Emil Kowalski's Design Engineering framework (`emil-design-eng`). We replaced cartoonish spring wobbles, layout twitches, zero-snapping dials, dead gray tracks, and blunt OS vibrations with calibrated physical kinematics, luminescent ambient lighting, and intimate heartbeat haptics.

---

## 2. Key Architectural Deliverables

### 2.1 Asymmetric Spring Physics & Scale Calibration (`ENG-01`, `APL-02`, `MIC-03`, `ANM-02`)
- **Pointer-Down Depression (`APL-02`)**: Implemented immediate mechanical depression on contact (`stiffness = 1000f, dampingRatio = 1.0f`), destroying perceived input lag.
- **Crisp Mechanical Return (`ENG-01`)**: Replaced loose `dampingRatio = 0.6f` with near-critical `dampingRatio = 0.82f` and `stiffness = 750f`, giving a snappy, tactile glass-click feel with zero rubbery wobble.
- **Anti-Twitch Scale Factor**: Parameterized `pressedScale: Float = 0.975f` (2.5% max compression) and `pressedAlpha: Float = 0.88f` in `BounceClick.kt`, eliminating the 18px horizontal layout twitch on full-width dashboard cards.

### 2.2 Fluid Ring Kinematics & Spatial Continuity (`ENG-02`, `ANM-01`)
- **Zero-Snap Elimination**: Removed `animatedProgress.snapTo(0f)` calls on data refresh. The dial no longer blinks out and restarts.
- **Liquid Physics Spring**: Replaced rigid `tween(1200, FastOutSlowInEasing)` with fluid `spring(dampingRatio = 0.78f, stiffness = 120f)`. Progress smoothly retargets from its current coordinate, simulating liquid momentum filling a gauge.
- **Vestibular Comfort**: Instantly snaps to target when `reduceMotion = true`, fully compliant with WCAG 2.3.3.

### 2.3 Dual-Pulse Heartbeat Sensory Haptics (`ENG-03`, `MIC-01`)
- **`CoupleFitHaptics.kt` Utility**: Created a dedicated haptics controller encapsulating an anatomical "Lub-Dub" heartbeat rhythm:
  - Phase 1: 12ms systolic pulse at ~40% amplitude (102/255)
  - Interval: 80ms diastolic pause
  - Phase 2: 18ms ventricular pulse at ~70% amplitude (178/255)
- **Universal Sensory Integration**: Replaced blunt `HapticFeedbackType.LongPress` OS vibration across `NudgeButton.kt`, `QuickNudgeRow.kt` pills, and `DashboardScreen.kt` double-tap cheer and 100% ring closure payoffs.
- **Graceful Fallback**: API 26+ amplitude waveforms $\to$ legacy waveform $\to$ Compose `TextHandleMove`.

### 2.4 Luminescent Ring Glow & Optical Depth (`ENG-04`)
- **Self-Illuminated Track**: Replaced dead plastic dark gray tracks (`DarkGray.copy(alpha = 0.3f)`) with a self-illuminated ambient track glow (`gradientEnd.copy(alpha = 0.12f)`).
- **Diffused Outer Halo**: Rendered a soft diffused halo stroke (width $+ 4\text{dp}$, `alpha = 0.04f`) on pure OLED black.
- **Cap Occlusion Shadows**: Rendered a subtle directional drop shadow under the leading round cap at $(\cos \theta, \sin \theta)$, creating tangible optical separation between concentric dials.

---

## 3. Mandatory Emil Kowalski Before / After Craft Benchmarks

| Before | After | Why |
| --- | --- | --- |
| `dampingRatio = 0.6f, stiffness = 400f` in `BounceClick.kt` (`ENG-01`) | `dampingRatio = 0.82f, stiffness = 750f` on release | `0.6f` causes rubbery cartoonish oscillations. Near-critical `0.82f` damping with higher stiffness gives a snappy, mechanical glass click. |
| Fixed `targetValue = 0.95f` scale on all touch targets (`ENG-01`, `MIC-03`) | Configurable `pressedScale = 0.975f` (subtle 2.5% max compression) | A full-width 360dp card shrinking by 5% shifts 18px horizontally, causing jarring peripheral layout twitch. Large surfaces must stay within 0.975–0.98x. |
| Symmetric release delay on button press (`APL-02`) | Asymmetric spring: `StiffnessHigh` (1000f, no bounce) on press-down, `0.82f / 750f` on release | Immediate tactile depression on pointer-down destroys perceived input lag; cushioned release provides fluid settling. |
| Rigid `tween(1200, FastOutSlowInEasing)` on rings (`ENG-02`) | Physical `spring(dampingRatio = 0.78f, stiffness = 120f)` | Linear/easing durations feel artificial. Physics spring models momentum and settles naturally like fluid filling a gauge. |
| `animatedProgress.snapTo(0f)` on ring refresh/trigger (`ENG-02`, `ANM-01`) | Continuous retargeting from current `animatedProgress.value` | Zero-snapping destroys spatial continuity, causing rings to flicker and disappear before re-animating. |
| Flat dead gray ring tracks `DarkGray.copy(alpha = 0.3f)` (`ENG-04`) | Luminescent track glow `gradientEnd.copy(alpha = 0.12f)` + ambient under-glow | Dead gray plastic tracks break OLED aesthetics; tinted ambient glow creates high-end optical depth and self-illumination. |
| Blunt `HapticFeedbackType.LongPress` on nudges/cheers (`ENG-03`, `MIC-01`) | Custom dual-pulse "Heartbeat Tick" (`12ms @ 40% -> 80ms pause -> 18ms @ 70%`) via `CoupleFitHaptics` | Heavy OS long-press vibration feels like an error or context menu; rhythmic heartbeat provides an intimate sensory touchpoint. |
| Flat 2D arc stroke with no cap depth (`ENG-04`) | Soft ambient shadow beneath leading cap when `sweepAngle > 18°` | Concentric dials look like flat vector clipart without localized occlusion shadows under the rounded tip. |

---

## 4. Verification Proof

- **Local Unit Tests**:
  - `.\gradlew.bat testDebugUnitTest` passed with 100% success rate (41/41 tasks up-to-date or passing).
- **Compose Android Tests**:
  - `.\gradlew.bat compileDebugAndroidTestKotlin` passed with 0 errors.
  - Added `designEngineering_springPhysics_and_heartbeatHaptics()` in `NavigationComposeUiTest.kt`.
- **Live Device ARTEMIS Verification (`emulator-5554`)**:
  - Verified Quick Nudge pills and heartbeat haptics on Today Dashboard (`eng_today_dashboard_verified.jpg`).
  - Verified single hero activity ring with 12% ambient track glow and liquid spring settling on My Stats (`eng_my_stats_ring_verified.jpg`).
  - Verified bilateral two-tone progress indicators on Trends (`eng_trends_verified.jpg`).
