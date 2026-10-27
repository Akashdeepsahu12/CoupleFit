# Emil Kowalski Design Engineering Craft Analysis: ENG-01 to ENG-04

> **Design Engineering Philosophy Reference**: Emil Kowalski (`emil-design-eng`)  
> **Target Scope**: `ENG-01`, `ENG-02`, `ENG-03`, `ENG-04`, `APL-02`, `ANM-01`, `MIC-01`, `MIC-03`  
> **Status**: Completed & Approved for Implementation  
> **Release Target**: `v2.8.0-design-eng`

---

## 1. Executive Summary & Philosophy

In modern digital products, functionality is a commodity. Craft, feel, and sensory resonance are the true differentiators. When interaction physics feel calibrated and unseen details compound, users perceive software as responsive, living, and trustworthy.

This audit applies Emil Kowalski's core principles:
1. **Taste is trained, not innate**: Reverse engineer physical motion, measure timing, and calibrate springs rather than relying on default curves.
2. **Unseen details compound**: Microscopic improvements in spring settling, track luminescence, and haptic rhythm combine into an experience people love without knowing why.
3. **Asymmetric timing**: Fast and crisp where the user commands the system (pointer-down), cushioned and smooth where the system settles (release).
4. **Physical continuity**: Elements never restart from zero; motion respects momentum and interrupts gracefully.

---

## 2. Mandatory Emil Kowalski Before / After Craft Benchmarks

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

## 3. Deep-Dive Design Specifications

### 3.1 `ENG-01` & `APL-02`: Asymmetric Press Physics (`BounceClick.kt`)
- **Pointer-Down Response (`isPressed == true`)**:
  - `targetScale = pressedScale` (default `0.975f`)
  - `targetAlpha = pressedAlpha` (default `0.88f`)
  - `animationSpec = spring(dampingRatio = 1.0f, stiffness = Spring.StiffnessHigh)` (1000f)
  - Result: Instant, zero-lag tactile depression matching the physical finger strike.
- **Pointer-Up / Release Response (`isPressed == false`)**:
  - `targetScale = 1.0f`
  - `targetAlpha = 1.0f`
  - `animationSpec = spring(dampingRatio = 0.82f, stiffness = 750f)`
  - Result: Crisp mechanical return with zero rubbery overshoot.

### 3.2 `ENG-02` & `ANM-01`: Fluid Ring Physics & Interruptibility (`ActivityRing.kt`)
- **No Zero Snap**: The `animatedProgress.snapTo(0f)` call is permanently removed.
- **Spring Retargeting**: When `progress` updates (whether via Health Connect background sync, user telemetry mock, or pull-to-refresh), `animatedProgress.animateTo(progress, spring(dampingRatio = 0.78f, stiffness = 120f))` smoothly guides the arc from its existing angular coordinate.
- **Motion Reduction**: Under `reduceMotion == true`, instantly snaps to target `animatedProgress.snapTo(progress)` to safeguard vestibular comfort.

### 3.3 `ENG-03` & `MIC-01`: Heartbeat Sensory Haptics (`CoupleFitHaptics.kt`)
- **Waveform Rhythm**:
  - Phase 1: 12ms systolic pulse at amplitude ~102/255 (40%).
  - Interval: 80ms quiet diastolic pause.
  - Phase 2: 18ms ventricular contraction pulse at amplitude ~178/255 (70%).
- **Fallback Hierarchy**:
  1. API 26+ amplitude waveform: `VibrationEffect.createWaveform(timings, amplitudes, -1)`
  2. Legacy timing waveform: `VibrationEffect.createWaveform(longArrayOf(0, 12, 80, 18), -1)`
  3. Context-free Compose fallback: `LocalHapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)`

### 3.4 `ENG-04`: Dynamic Optical Depth & Luminescent Glow (`ActivityRing.kt`)
- **Track Glow**:
  - Background track stroke uses `trackColor ?: gradientEnd.copy(alpha = 0.12f)` instead of dead dark gray.
  - An underlying diffused halo stroke (width $+ 4\text{dp}$, `alpha = 0.05f`) provides specular luminance on pure OLED black.
- **Cap Occlusion Shadow**:
  - When the progress arc is active ($> 0.05f$), a soft tinted drop shadow is rendered under the leading rounded cap at $(\cos \theta, \sin \theta)$ offset, giving tactile separation between concentric dials.

---

## 4. Verification & Testing Strategy
1. **Local Test Suites**:
   - `testDebugUnitTest` must pass with 100% success.
   - `compileDebugAndroidTestKotlin` must compile without errors.
   - Author instrumented tests verifying `CoupleFitHaptics` and `iosClickable` parameterization.
2. **ARTEMIS Live Verification (`emulator-5554`)**:
   - Live hierarchy inspection of dashboard rings and touch targets.
   - Live screenshot capture verifying typography, luminescent dial glow, and visual calibration.
