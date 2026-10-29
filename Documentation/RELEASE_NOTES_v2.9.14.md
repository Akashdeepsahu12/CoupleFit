# CoupleFit Release Notes — v2.9.14: Apple Design & Liqui Design Comprehensive UI & Animation Redesign (`DSN-06`, `ANM-04`) 🍏💧

> **Release Version**: `v2.9.14`  
> **Release Tag**: `v2.9.14-apple-liqui-redesign`  
> **Git Checkpoint / Base Commit**: `0c5c9342a01e98c937fab97fe39e30097478e053` (`v2.9.13-ambient-video-fade-in`)  
> **Governing Standards**: Apple Human Interface Guidelines (WWDC *Designing Fluid Interfaces*, *Principles of Great Design*), Liqui Design System (`https://github.com/leefanv/liqui-design`), and Emil Kowalski Design Engineering  
> **Target Devices**: Google Pixel 10 Pro / Flagship Android Devices (Android 17 API 36, `emulator-5554`)

---

## 1. Executive Summary

CoupleFit v2.9.14 introduces a comprehensive visual and motion overhaul combining **Apple Design** WWDC fluid interface principles with the **Liqui Design** 4-layer optical liquid glass architecture. The interface transforms static and translucent elements into responsive physical surfaces that respond immediately on touch, settle with critically damped kinematics, and maintain optical clarity over living video backgrounds.

---

## 2. Key Features & Enhancements

### 1. Liqui Design 4-Layer Optical Architecture (`DSN-06`)
- **Layer 1 (Ambient Elevation Shadow)**: Soft drop shadow (`8dp` on cards, `16dp` on floating navigation dock) creating spatial separation above the living video background.
- **Layer 2 (Continuous Squircle Curvature)**: Squircles calculated with continuous curvature (`22dp` card radius, `20dp` pill radius, `32dp` dock radius), eliminating visual corner pinching.
- **Layer 3 (Translucent Optical Substrate & Tint Wash)**: Deep translucent obsidian foundation (`#2610131E`) paired with a 2-stop optical tint wash (`--lq-tint` to `--lq-tint-deep`) without child `RenderEffect` blurring, preserving 100% razor-sharp typography and graphs.
- **Layer 4 (Directional Specular Bevel Rim)**: Diamond-cut `1.25dp` vertical gradient border (50% white highlight at top descending to 12% shadow at bottom), emulating light catching liquid glass.

### 2. Two-Material Control Philosophy (`DSN-06`)
- **Tactile Semi-Solid Controls**: In accordance with Liqui's Two-Material philosophy, interactive controls (Quick Nudge pills, diagnostic chips, telemetry status pills) use distinct tactile semi-solid materials (`liquiTactilePill`) rather than stacking translucent glass on glass.
- **High-Contrast Typography**: Restores text contrast to >7:1 against translucent surfaces (`LiquiTokens.DarkText`), ensuring effortless outdoor and dark-room legibility.

### 3. Apple WWDC Fluid Interface Spring Dynamics (`ANM-04`)
- **Zero-Latency Tactile Depression**: Immediate pointer-down response (`scale = 0.975f`, `alpha = 0.88f`, `dampingRatio = 1.0f`, `stiffness = 1000f`) with zero input lockouts.
- **Critically Damped Settling**: Touch release settled with Apple critically damped physics (`dampingRatio = 1.0f`, `stiffness = 800f`, `response ≈ 0.35s`), eliminating unnatural rubbery oscillations.
- **Momentum Physics**: Created `iosMomentumClickable` (`dampingRatio = 0.80f`, `stiffness = 700f`, `scale = 0.95f`, `alpha = 0.85f`) for actions carrying playful momentum (cheers, nudges, duels).
- **Activity Ring Kinematics**: Tuned `ActivityRing.kt` spring sweep retargeting to `dampingRatio = 0.85f`, `stiffness = 160f` for fluid, unhurried Apple Fitness momentum settling.

---

## 3. Verification & Build Quality

- **Unit Test Suite**: `./gradlew testDebugUnitTest` passed 100% (32 actionable tasks, 0 failures).
- **Android Test Compilation**: `./gradlew compileDebugAndroidTestKotlin` passed with 0 errors.
- **Live Device ARTEMIS Verification (`emulator-5554`)**:
  - `apple_liqui_redesign_verified.jpg`: Verified dashboard top view with 4-layer Liqui optical glass cards, tactile status pill, and razor-sharp typography over living video background.
  - `apple_liqui_redesign_nudges_verified.jpg`: Verified Quick Nudge row with high-contrast tactile pills and sapphire blue video shuffle.
  - `apple_liqui_redesign_heart_rate_verified.jpg`: Verified Live Heart Rate Card with smooth cubic spline, peak indicator ring (90 BPM), baseline grid, and floating navigation dock.

---

## 4. Reversibility & Rollback

This entire release is isolated and completely reversible:
- **Keep**: Retain `v2.9.14-apple-liqui-redesign`.
- **Revert**: Instantly restore previous state via `git reset --hard v2.9.13-ambient-video-fade-in`.
