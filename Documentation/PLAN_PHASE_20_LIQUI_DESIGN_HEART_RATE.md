# Phase 20: Liqui Design Liquid Glass Heart Rate Redesign & Antislop-UI Polish

**Status**: ACTIVE  
**Goal**: Re-engineer the dashboard Live Heart Rate card using the official optical physics and two-material philosophy of [leefanv/liqui-design](https://github.com/leefanv/liqui-design) paired with the strict design hygiene of `antislop:antislop-ui`.

---

## 1. Problem Statement & Design Diagnosis

### A. Antislop-UI Violations in Current Card
1. **Eyebrow Badge Above Headline**: The card currently renders `VITALS PULSE` above `Live Heart Rate`. This is generic AI-slop eyebrow duplication. The real title (`Heart Rate`) should speak directly without filler category labels.
2. **Decorative Status Dot & Looping Pulses**: A glowing, endless pulsing dot on a loop next to `LIVE` mimics a recording studio light rather than a calm, authoritative biometric readout.
3. **Generic Blur vs. True Liquid Glass**: Previous glass was a standard flat Compose blur with a single-tone semi-transparent background. It lacked the physical optical refraction, dual-phase specular rim, and squircle lens depth that makes liquid glass feel like physical polished silica.

### B. Liqui Design Optical Specifications (Ported from `leefanv/liqui-design`)
1. **4-Layer Optical Stack**:
   - **Layer 1: Base Optical Blur (`backdrop-filter`)**: Hardware `RenderEffect` Gaussian blur (20dp) with tile clamp on Android 12+.
   - **Layer 2: Multi-Stop Tint Wash (`--lq-tint` to `--lq-tint-deep`)**: Dark-mode optical gradient (`0x9E1E202E` -> `0x66121420`) ensuring high-contrast text legibility.
   - **Layer 3: Refractive Specular Rim (`--lq-rim-hi` to `--lq-rim-lo`)**: 1.25dp optical bevel border with directional light falloff (`0x80FFFFFF` top edge, `0x1EFFFFFF` lower edge).
   - **Layer 4: Continuous Squircle Superellipse**: Continuous corner curvature (22dp radius) preventing corner distortion.
2. **Two-Material Philosophy**:
   - **Glass**: Dedicated to surfaces you look *through* (the card container, lens, waveform stage).
   - **Solid Fill**: Dedicated to controls that carry *action/value* (the primary "Sync Now" button, active metric chips).

---

## 2. Planned Implementation Steps

1. **Liqui Design Modifier / Composable (`LiquiGlassTokens.kt` / `LiquiGlass.kt`)**:
   - Create `Modifier.liquiGlass()` implementing the 4-layer optical model, directional specular bevel, and multi-stop tint wash.
2. **Antislop Layout Architecture (`DashboardScreen.kt`)**:
   - Strip `VITALS PULSE` eyebrow duplication.
   - Clean, honest header: `Heart Rate` with calm vector heart glyph and crisp telemetry tag.
   - Hero metric display: Big bold BPM (`72` or `--`) in `displaySmall`, unit suffix `BPM`, resting/sync status, and peak/min markers.
3. **Two-Material Waveform & Action Stage**:
   - When data is present: 130dp spline curve (`InteractiveChart(showHeader = false)`).
   - When awaiting data: Clean ambient ECG baseline with a solid, tactile **Sync Now** action button adhering to the two-material rule.
4. **Deterministic Test Bar**:
   - `.\gradlew.bat testDebugUnitTest` (100% pass rate).
   - `.\gradlew.bat compileDebugAndroidTestKotlin` (100% pass rate).
   - Live Android verification via ARTEMIS (`hierarchy` and `screenshot`).
5. **Shipment & Documentation**:
   - Update `Fixinglist.md`, `triage.md`, `phases.md`, `INDEX.md`, `knowledge-graph.md`, and author `RELEASE_NOTES_v2.9.5.md`.
   - Git tag `v2.9.5-liqui-design-heart-rate`.
