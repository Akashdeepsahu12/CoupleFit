# Milestone Plan: Phase 34 — Tactile Animation Polish & Kinetic Micro-Interactions (`ANM-09`..`ANM-14`) 🎬✨

> **Tracking Reference**: [`Documentation/Fixinglist.md`](file:///D:/Couple_fit/CoupleFit/Documentation/Fixinglist.md) §56, §57
> **Active Design & Motion Skills**: [`/designer-skills:interfaces-that-feel`](file:///C:/Users/akash/.gemini/config/plugins/designer-skills/skills/interfaces-that-feel/SKILL.md), [`/emil-design-eng`](file:///C:/Users/akash/.gemini/antigravity-cli/skills/emil-design-eng/SKILL.md), [`/motion`](file:///C:/Users/akash/.gemini/config/plugins/designer-skills/skills/motion/SKILL.md)
> **Branch**: `dev_akash`
> **Target Release Tag**: `v2.9.19-tactile-motion-anm09-14`

---

## 1. Executive Summary & Design Vision

Phase 34 implements tactile micro-interaction polish across all four primary app surfaces and key interaction widgets, addressing audit findings `ANM-09` through `ANM-14`. By applying Emil Kowalski's four-stage motion evaluation gate (*Frequency, Purpose, Speed, Function*), this phase elevates CoupleFit from a static UI into a physically grounded, delightful, and responsive relationship experience.

---

## 2. Problem Diagnosis & Targeted Solutions

| Defect ID | Target File | Current State (Gap) | Target Motion Architecture |
| :--- | :--- | :--- | :--- |
| **`ANM-09`** | [`CoupleFitNavigationSuite.kt`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/ui/navigation/CoupleFitNavigationSuite.kt) | M3 bottom navigation dock crossfades indicators without spatial continuity or spring-driven physical elongation. | Implement a shared spring-morphing capsule pill indicator with bouncy physics (`dampingRatio = 0.80f, stiffness = 400f`) that elastically stretches in the travel direction before snapping into place. |
| **`ANM-10`** | [`ChallengesScreen.kt`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/ui/challenge/ChallengesScreen.kt), [`ChallengeComponents.kt`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/ui/challenge/ChallengeComponents.kt) | Duel format and challenge cards lack dynamic touch press physics or perspective tilt on press. | Implement `Modifier.duelCardPress()` with asymmetric iOS physics: instantaneous `0.98x` compression and subtle `-1.5°` 3D perspective tilt on touch-down, followed by an under-damped spring bounce on release. |
| **`ANM-11`** | [`HealthTrendsScreen.kt`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/ui/trends/HealthTrendsScreen.kt) | `Day / Week / Month / Year` timeframe selector uses disjoint `SingleChoiceSegmentedButtonRow` with jarring, abrupt state switches. | Implement `FluidSegmentedSwitcher`: a frosted glass capsule with a continuous spring-gliding pill indicator (`dampingRatio = 0.75f, stiffness = 400f`) that slides seamlessly beneath active time horizons. |
| **`ANM-12`** | [`DashboardScreen.kt`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/ui/dashboard/DashboardScreen.kt) | Telemetry sync status displays static text and generic progress spinner during pull-to-refresh and background refresh. | Implement `OrbitalSyncPulse`: a living dual-particle cyan/amber orbital ring with expanding concentric radar ripples that activate during sync and settle into a calming green breath when in sync. |
| **`ANM-13`** | [`MedicationDuePill.kt`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/ui/components/MedicationDuePill.kt) | Medication logging snaps button instantly into "Done ✓" badge, and icon emojis swap abruptly without animation. | Implement `AnimatedContent` transitions with scale punch (`initialScale = 0.88f`), dual-pulse heartbeat haptic confirmation (`CoupleFitHaptics.performHeartbeat`), and smooth spring ejection on dismiss. |
| **`ANM-14`** | [`CoupleHubScreen.kt`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/ui/hub/CoupleHubScreen.kt) | Partner profile avatar is static and non-interactive, missing an opportunity for intimate relational feedback. | Implement interactive heartbeat touch trigger: dual-pulse haptic rhythm (`Lub-Dub` 12ms/80ms/18ms), animated dual-scale heart pump, and expanding luminous radial ripple aura. |

---

## 3. Explicit Acceptance Criteria & Test Bars

1. **`ANM-09` Navigation Pill Glide**:
   - Switching between any of the 4 tabs animates the active pill indicator smoothly across the dock without snapping.
   - Touch targets remain at least 48dp with TalkBack content descriptions preserved.
2. **`ANM-10` Card Press Mechanics**:
   - Pressing on challenge and duel cards compresses smoothly to `0.98x` with `-1.5°` tilt.
   - Lifting releases with snappy spring recovery (no cartoonish overshoot).
3. **`ANM-11` Fluid Timeframe Switcher**:
   - Tapping between Day, Week, Month, Year causes the pill indicator to glide smoothly along the X-axis to the selected segment.
   - Text color and weight transition fluidly.
4. **`ANM-12` Orbital Sync Pulse**:
   - Active telemetry refresh renders the rotating cyan/amber orbital particle ring and radar wave.
   - Idle state displays the calm breathing sync dot.
5. **`ANM-13` Medication Dose Celebration**:
   - Tapping "Log [✓]" transitions button into "Done ✓" badge via `AnimatedContent`.
   - Fires `performHeartbeat()` haptic feedback.
   - Card auto-dismisses smoothly after 2.5 seconds with spring shrink.
6. **`ANM-14` Partner Avatar Heartbeat Ripple**:
   - Tapping partner avatar triggers dual-pulse heartbeat haptic.
   - Displays expanding radiant glow aura and heart scale bounce.
7. **Compilation & Test Suite**:
   - `./gradlew testDebugUnitTest` must pass with 100% success (0 failures).
   - `./gradlew compileDebugAndroidTestKotlin` must compile with 0 errors.
   - Live visual confirmation on `emulator-5554` with ARTEMIS screenshots.

---

## 4. Execution Waves

- **Wave 1 (`ANM-13` & `ANM-14`)**: Tactile touch & haptic micro-interactions in `MedicationDuePill.kt` and `CoupleHubScreen.kt`.
- **Wave 2 (`ANM-11` & `ANM-10`)**: Fluid timeframe switcher in `HealthTrendsScreen.kt` and duel card press physics in `BounceClick.kt` / `ChallengesScreen.kt` / `ChallengeComponents.kt`.
- **Wave 3 (`ANM-12` & `ANM-09`)**: Orbital sync ripple in `DashboardScreen.kt` and dock indicator spring morphing in `CoupleFitNavigationSuite.kt`.
- **Wave 4 (Verification & Polish)**: Run unit tests, compile Android test APK, deploy to `emulator-5554`, capture ARTEMIS screenshots, update documentation, ship and tag release.
