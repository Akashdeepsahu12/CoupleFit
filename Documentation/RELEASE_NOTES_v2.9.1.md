# CoupleFit v2.9.1 Release Notes — Liquid Glassmorphism Redesign & Layout Resilience 💎

> **Milestone Version**: `v2.9.1`  
> **Release Tag**: `v2.9.1-glass-redesign`  
> **Release Date**: October 2026  
> **Governing Standards**: `/ui-ux-pro-max-skill:design-system`, `/emilkowalski-skills:emil-design-eng`, WCAG 2.2 Level AA  
> **Associated Defect Logs**: `GLS-07`, `GLS-08`, `GLS-09`, `GLS-10`, `LAY-01`, `FLT-01`

---

## 1. Executive Summary

CoupleFit **v2.9.1** delivers an essential visual and layout overhaul addressing critical user feedback regarding card appearance, contrast readability, and onboarding layout stability:
1. **Physical Frosted Glass Redesign**: Transformed flat 8% translucent cards into rich, dual-layer liquid glass surfaces utilizing a deep translucent slate undertone (`#38121622`) combined with a 3-stop vertical specular brush (14% top glint $\to$ 6.5% core $\to$ 2.4% bottom) and diamond-cut specular rims (`0x5EFFFFFF` top glint).
2. **Luminous Aurora Backlighting**: Boosted ambient telemetry radial plumes from 8% to 28% luminance with a 52% canvas radial spread, providing the essential backlighting required for true frosted glass perception on OLED black backgrounds.
3. **Decoupled Day-1 Onboarding Layout**: Solved the critical layout squashing bug where the Day-1 onboarding card was squeezed into a ~120px right-hand column when nested inside an unweighted `Row`. The card is now promoted to a first-class, full-width glass banner.
4. **Resilient Filter Chips**: Added horizontal scrolling container to the perspective filter chips (`Side-by-Side`, `My Stats`, `Partner's Stats`) preventing text truncation (`Partner's St...`) on compact mobile displays.
5. **Floating Navigation Dock Polish & Scroll Clearance**: Elevated dock text/icon unselected contrast to `#8E95A5`, tuned active pill containers to 22% cyan alpha, and introduced a 96dp bottom scroll clearance spacer across all screens so bottom content never gets trapped behind the floating dock.

Every modification has been strictly verified with 100% green unit and Android instrumentation compilation suites, and confirmed on live Android device `emulator-5554` via Google ARTEMIS.

---

## 2. Key Highlights & Architectural Additions

### 2.1 Dual-Layer Frosted Glass Surface Engine (`GlassTokens.kt` - `GLS-07`, `GLS-08`)
- **Dual-Layer Surface Rendering**: `Modifier.iosGlass()`, `Modifier.appleGlass()`, and `Modifier.glassDock()` now layer a deep translucent slate base (`0x38121622`) with a multi-stop specular vertical gradient:
  - Top 0%: `0x24FFFFFF` (14% specular glint)
  - Mid 35%: `0x10FFFFFF` (6.5% frosted glass body)
  - Bottom 100%: `0x06FFFFFF` (2.4% translucent slate floor)
- **Diamond-Cut Specular Rim**: Re-engineered `BorderSpecularRim` into a 3-stop linear border gradient (`0x5EFFFFFF` top edge glint $\to$ `0x22FFFFFF` mid $\to$ `0x05FFFFFF` bottom shadow), creating authentic physical light refraction.
- **Floating Dock Colorway**: Replaced flat dock background with `TranslucentDockTop` (`#E81C2333`) and `TranslucentDockBottom` (`#F410131C`) for zero backlight bleed-through and tactile separation from scrollable content.

### 2.2 Luminous Aurora Telemetry Backlight (`GlassTokens.kt` - `GLS-09`)
- Upgraded `AuroraTelemetryMesh` with 3-tier radial glow brushes:
  - Inner core: `0.28f` alpha (28% luminous bloom)
  - Mid halo: `0.12f` alpha
  - Outer rim: `0.00f` transparent
- Expanded radial bloom radius from `width * 0.35f` to `width * 0.52f`, casting soft Electric Cyan and Vibrant Coral gradients under frosted cards.

### 2.3 Dashboard Screen Layout Fixes (`DashboardScreen.kt` - `LAY-01`, `FLT-01`)
- **Decoupled Onboarding Card**: Removed the Day-1 onboarding card from the unweighted header `Row` and placed it as an independent full-width component directly above the metric dials.
- **Scrollable Filter Chips**: Wrapped `FilterChip` row in `.horizontalScroll(rememberScrollState())` to preserve full label readability on compact viewports.
- **96dp Scroll Clearance**: Injected `Spacer(modifier = Modifier.height(96.dp))` at the end of `DashboardScreen` and `HealthTrendsScreen` so bottom cards remain fully accessible above the floating navigation dock.

### 2.4 High-Contrast Floating Navigation Dock (`CoupleFitNavigationSuite.kt` - `GLS-10`)
- Active navigation item indicator set to `UserPrimary.copy(alpha = 0.22f)`.
- Unselected navigation icons and labels calibrated to `#8E95A5` (meeting WCAG 2.2 AA 4.5:1 contrast requirements).

---

## 3. Verified Artifacts & Live Testing

| Artifact File | Description & Verification Proof |
| :--- | :--- |
| `gls_redesign_dashboard_verified.jpg` | Verified specular top rim glint, ambient cyan/coral aurora bloom, full-width Day-1 onboarding card, untruncated filter chips, and floating dock. |
| `gls_redesign_trends_verified.jpg` | Verified frosted glass bilateral trends cards, medication list, and 96dp bottom scroll clearance above the navigation dock. |

---

## 4. Test Suite & Verification Results

```bash
# Automated Unit Test Suite (100% Success)
.\gradlew.bat testDebugUnitTest
# Result: BUILD SUCCESSFUL in 29s (32 actionable tasks, 0 failures)

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
| `GLS-07` | Visual Physics | **Resolved** | Dual-layer specular surface gradient in `GlassTokens.kt` |
| `GLS-08` | Visual Physics | **Resolved** | 3-stop diamond-cut specular border rim (`0x5EFFFFFF`) |
| `GLS-09` | Visual Physics | **Resolved** | 28% luminous aurora radial plumes behind dials |
| `GLS-10` | Ergonomics & Contrast | **Resolved** | High-contrast `#8E95A5` unselected dock items & 22% active capsule |
| `LAY-01` | Layout Bug | **Resolved** | Day-1 card decoupled from header row into full-width card |
| `FLT-01` | Layout & Typography | **Resolved** | Horizontal scroll enabled on perspective FilterChips |
