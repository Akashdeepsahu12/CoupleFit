# Phase 21: Razor-Sharp Optical Liquid Glass Refactor (`PLAN_PHASE_21_CRISP_OPTICAL_GLASS.md`)

## 1. Problem Diagnosis & Root Cause Analysis
During live device visual audit on `emulator-5554` via Google ARTEMIS, the user reported that the newly redesigned Heart Rate card appeared completely blurry (`BLR-01`).

### The Root Cause
In Jetpack Compose, developers coming from web development often confuse CSS `backdrop-filter: blur(20px)` with Compose's `Modifier.graphicsLayer { renderEffect = RenderEffect.createBlurEffect(20f, 20f, CLAMP) }`.
- **Web CSS `backdrop-filter: blur(20px)`**: Only applies an image filter to the pixels directly beneath the element, leaving the element's DOM children (text, SVGs, buttons) unaffected and completely sharp.
- **Android Compose `Modifier.graphicsLayer { renderEffect = ... }`**: Creates an offscreen layer containing the Composable background **and all of its child rendering operations**. The entire rendered layer is then processed with the Gaussian blur before being drawn to the window buffer.

Because `.liquiGlass()` was applied to the outer `Card` container in `DashboardScreen.kt`, every single word and glyph inside the card (`"Heart Rate"`, `"--"`, `"BPM"`, `"Sync Health Connect"`, icons) was subjected to a 20f Gaussian blur, making the entire card appear out-of-focus and illegible.

---

## 2. Architectural Solution
Following the precedent established in `GlassTokens.kt` (`iosGlass`):
> *"Preserves foreground typography and icon crispness by avoiding child RenderEffect blurring"*

We refactored `Modifier.liquiGlass()` in `LiquiGlassTokens.kt`:
1. **Removed Layer-Wide Blur**: Eliminated `Modifier.graphicsLayer { renderEffect = RenderEffect.createBlurEffect(...) }` from the card container modifier chain.
2. **Multi-Layered Optical Depth**: Retained the high-fidelity optical depth of the Liquid Design paradigm:
   - Directional elevation shadow (`ambientColor` + `spotColor`)
   - Anti-aliased squircle clip (`LiquiTokens.SquircleCornerShape`, 22dp radius)
   - Translucent optical base substrate (`DarkTintDeep` / `LightTintDeep`)
   - 2-stop directional optical gradient tint wash (`DarkTint` to `DarkTintDeep`)
   - Directional specular rim light bezel border (`DarkRimHi` to `DarkRimLo` 1.25dp)
3. **Razor-Sharp Typography**: Child Composables are now rendered directly to the target canvas with 100% native raster and vector clarity, restoring maximum contrast and WCAG 2.2 Level AA compliance.

---

## 3. Acceptance Criteria & Test Bars
- [x] **AC-1**: Remove `graphicsLayer` blur from `Modifier.liquiGlass()` in `LiquiGlassTokens.kt`.
- [x] **AC-2**: Full unit test suite (`.\gradlew.bat testDebugUnitTest`) passes with 100% green bar.
- [x] **AC-3**: Android test sources compile cleanly (`.\gradlew.bat compileDebugAndroidTestKotlin`).
- [x] **AC-4**: App installs and runs on `emulator-5554` without crashing or visual regressions.
- [x] **AC-5**: ARTEMIS screenshot verification confirms crisp, razor-sharp typography and preserved liquid glass specular bezel.
- [x] **AC-6**: Documentation synchronized across `Fixinglist.md`, `triage.md`, `phases.md`, `INDEX.md`, and `RELEASE_NOTES_v2.9.6.md`.
