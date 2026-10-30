# Milestone Plan: Phase 43 — Number Pop-In Motion Physics & Dashboard Tracker Choreography (`POP-01`) 🔢✨

> **Phase**: Phase 43  
> **Defect / Feature Code**: `POP-01`  
> **Source Specification**: [transitions.dev: Number Pop-In](https://transitions.dev/detail.html?t=number-pop-in) by Jakub Antalik  
> **Target Files**:
> - [`app/src/main/java/com/couplefit/ui/components/NumberPopIn.kt`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/ui/components/NumberPopIn.kt) *(New Component)*
> - [`app/src/main/java/com/couplefit/ui/components/MotionArcGauge.kt`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/ui/components/MotionArcGauge.kt)
> - [`app/src/main/java/com/couplefit/ui/dashboard/DashboardScreen.kt`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/ui/dashboard/DashboardScreen.kt)

---

## 1. Executive Summary & Problem Formulation

The user requested integrating the **Number Pop-In** transition from [transitions.dev/detail.html?t=number-pop-in](https://transitions.dev/detail.html?t=number-pop-in) across the step counter and all dashboard telemetry trackers, ensuring the animation replays every time the app opens.

### Physical Kinematics of transitions.dev `number-pop-in`:
```css
:root {
  --digit-dur: 500ms;
  --digit-distance: 8px;
  --digit-stagger: 70ms;
  --digit-blur: 2px;
  --digit-ease: cubic-bezier(0.34, 1.45, 0.64, 1);
  --digit-dir-x: 0;
  --digit-dir-y: 1;
}

@keyframes t-digit-pop-in {
  0% {
    transform: translate(0, 8px);
    opacity: 0;
    filter: blur(2px);
  }
  100% {
    transform: translate(0, 0);
    opacity: 1;
    filter: blur(0);
  }
}
```

### Architectural Requirements:
1. **Per-Digit Independent Stagger**: Each character and digit animates independently with a `70ms` staggered offset (`delayMillis = index * 70ms`).
2. **Overshoot Easing Curve**: Exact cubic-bezier curve `CubicBezierEasing(0.34f, 1.45f, 0.64f, 1.0f)` providing an organic spring pop-in with a 45% overshoot peak settling smoothly to 1.0.
3. **Motion Blur & Translation**: Digit starts offset downwards by `8.dp` with a subtle `2.dp` Gaussian blur (evaluated via `RenderEffect.createBlurEffect` on Android 12+) that clears to 0 as the digit pops into place.
4. **App-Open & Resume Triggering**: A unified `popInTrigger` bound to both initial composition and `Lifecycle.Event.ON_RESUME` guarantees the animation plays every time the user opens or returns to the app.
5. **Universal Dashboard Tracker Coverage**:
   - Primary bilateral activity gauges (User & Partner step counters).
   - Under-gauge telemetry readouts (Active Minutes & Caloric Burn).
   - Hero telemetry on My Stats & Partner's Stats tabs (`StatCard`).
   - Heart Rate & ECG hero BPM readout (`70 BPM`).
   - Today's Competition standings lead counter.
6. **Accessibility & Graceful Degradation**:
   - When system `reduceMotion` is enabled, snaps immediately without translation, delay, or blur (WCAG 2.3.3 compliant).

---

## 2. Explicit Acceptance Criteria

- [ ] **AC-1 (NumberPopIn Engine)**: Create [`NumberPopIn.kt`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/ui/components/NumberPopIn.kt) with `NumberPopInText` and `NumberPopInCounter` implementing transitions.dev physical parameters (`500ms` duration, `8.dp` Y-offset, `70ms` per-character stagger, `2.dp` blur, and `CubicBezierEasing(0.34f, 1.45f, 0.64f, 1.0f)`).
- [ ] **AC-2 (Step Counters Integration)**: In [`MotionArcGauge.kt`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/ui/components/MotionArcGauge.kt), use `NumberPopInCounter` for both compact bilateral gauges and hero single-user gauges.
- [ ] **AC-3 (Metric Chips & Vitals Integration)**: Metric pill chips (`km`, `kcal`, `min`), under-gauge readouts, and Heart Rate BPM display use `NumberPopInText`.
- [ ] **AC-4 (App-Open Trigger)**: `DashboardScreen.kt` increments a `popInTrigger` on `Lifecycle.Event.ON_RESUME` so every app launch/resumption executes the pop-in choreography.
- [ ] **AC-5 (Accessibility Guardrail)**: Gated on `reduceMotion`; skips animation when system animation scale is 0.
- [ ] **AC-6 (Test Suite Green)**: `.\gradlew.bat testDebugUnitTest` and `compileDebugAndroidTestKotlin` pass 100%.
- [ ] **AC-7 (Live Device Verification)**: Verified on Samsung Galaxy S24 (`SM-S921B`) and Google Pixel 10 Pro (`emulator-5554`) via Google ARTEMIS.

---

## 3. Task Breakdown & Execution Waves

- **Wave 1**: Build [`app/src/main/java/com/couplefit/ui/components/NumberPopIn.kt`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/ui/components/NumberPopIn.kt).
- **Wave 2**: Wire `NumberPopIn` into [`MotionArcGauge.kt`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/ui/components/MotionArcGauge.kt) and [`DashboardScreen.kt`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/ui/dashboard/DashboardScreen.kt).
- **Wave 3**: Audit code via **Karpathy Guidelines**.
- **Wave 4**: Run automated unit tests and compile Android tests.
- **Wave 5**: Install on devices, capture live verification screenshots via Google ARTEMIS, author verification report, update documentation, and commit with release tag.
