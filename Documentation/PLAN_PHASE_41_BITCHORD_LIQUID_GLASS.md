# Phase 41 Milestone Plan: BitChord Liquid Glass Bottom Navigation Bar & Physical 3D Refraction Engine (`LIQ-04`)

> **Governing Blueprint**: [`Documentation/liquideffect.md`](file:///D:/Couple_fit/CoupleFit/Documentation/liquideffect.md) & BitChord Reference (`kushagrasinghx/BitChord`)  
> **Master Defect Catalog**: [`Documentation/Fixinglist.md`](file:///D:/Couple_fit/CoupleFit/Documentation/Fixinglist.md) (§65, `LIQ-04`)  
> **Triage Catalog**: [`Documentation/triage.md`](file:///D:/Couple_fit/CoupleFit/Documentation/triage.md)  
> **Target Files**:
> - [`app/src/main/java/com/couplefit/ui/glass/BackdropEngine.kt`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/ui/glass/BackdropEngine.kt) (New)
> - [`app/src/main/java/com/couplefit/ui/glass/LiquidGlass.kt`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/ui/glass/LiquidGlass.kt) (New)
> - [`app/src/main/java/com/couplefit/ui/navigation/CoupleFitNavigationSuite.kt`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/ui/navigation/CoupleFitNavigationSuite.kt)
> - [`app/src/main/java/com/couplefit/MainActivity.kt`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/MainActivity.kt)

---

## 1. Executive Summary & Defect Scope

The user requested applying the authentic BitChord Liquid Glass bottom bar architecture from [`liquideffect.md`](file:///D:/Couple_fit/CoupleFit/liquideffect.md) and [kushagrasinghx/BitChord](https://github.com/kushagrasinghx/BitChord).

### The Architectural Limitation of Container Blur
As explained in `liquideffect.md`, Compose composables cannot blur or refract content behind them simply by setting a `RenderEffect` on their own container. In previous phases, Haze was used to sample backdrop content, but it lacked BitChord's:
1. **0.33x Hardware Downscaled Backdrop Layer Recorder (`LayerBackdrop`)**: Captures screen content into an isolated GPU `GraphicsLayer` with an opaque window background, eliminating transparent pixel rendering bugs and guaranteeing silky 60/120fps performance.
2. **Color Vibrancy Saturation Boost (`colorControls`)**: Boosts the color saturation of the content passing underneath the glass by 1.5x (`saturation = 1f + 0.5f * VIBRANCY`), making colors pop under frosted diffusion.
3. **Physical 3D Lens Refraction with 7-Band Chromatic Dispersion**:
   - `circleMap(1.0 - -sd / refractionHeight) * refractionAmount`
   - Depth pushing effect (`depthEffect * normalize(centeredCoord)`)
   - 7-sample rainbow chromatic dispersion (Red, Orange, Yellow, Green, Cyan, Blue, Purple) with fractional spectral weights.
4. **Decoupled Foregrounds with Zero Blur**: In `drawBackdrop`, `drawLayer(layer)` draws the refracted background, `onDrawSurface` draws the tinted sheen, and then `drawContent()` renders child icons and labels directly to the canvas without any `RenderEffect`, guaranteeing 100% razor-sharp typography.
5. **BitChord Elastic Kinematics**: Active indicator pill with physical stretch and squash (`scaleX = 1f + lag * STRETCH`, `scaleY = 1f - lag * STRETCH * SQUASH`), haptic tick feedback, and `reduceMotion` accessibility gating.

---

## 2. Explicit Acceptance Criteria & Test Bars

- [ ] **AC 1 (Backdrop Recording Engine)**: Create `BackdropEngine.kt` implementing `Backdrop`, `rememberLayerBackdrop`, `LayerBackdrop`, `Modifier.layerBackdrop`, `BackdropEffectScope` (`blur`, `colorControls`, `lens`), and `Modifier.drawBackdrop` with 0.33f resolution downscaling.
- [ ] **AC 2 (Liquid Glass Modifier & Theme Tokens)**: Create `LiquidGlass.kt` providing `LocalAppBackdrop`, `LocalLiquidGlassEnabled`, `Modifier.liquidGlass`, and light/dark theme tinted surfaces with 0.5dp specular border.
- [ ] **AC 3 (Navigation Suite Integration)**:
  - Wire `rememberLayerBackdrop` and `Modifier.layerBackdrop(appBackdrop)` in `CoupleFitNavigationSuite.kt`.
  - Provide `LocalAppBackdrop` and `LocalLiquidGlassEnabled` via `CompositionLocalProvider`.
  - Apply `Modifier.liquidGlass` to the floating dock container.
  - Implement BitChord dynamic stretch & squash kinematics on the active indicator capsule.
- [ ] **AC 4 (Accessibility & Fallbacks)**:
  - Respect `reduceMotion`: snap instantly without stretch/squash.
  - Graceful fallback on Android < 12 (API 31) or when Liquid Glass is toggled OFF in Settings: render elegant solid dark tinted surface with specular border.
- [ ] **Verification Bar**:
  - `.\gradlew.bat testDebugUnitTest` passes 100%.
  - `.\gradlew.bat compileDebugAndroidTestKotlin` passes with 0 errors.
  - Deployed to Samsung Galaxy S24 (`SM-S921B`) and verified with ARTEMIS screenshots.

---

## 3. Step-by-Step Execution Plan

### Step 1: Author `BackdropEngine.kt`
- Port the exact BitChord Kyant0 backdrop engine from `liquideffect.md` with:
  - `RoundedRectSDF`
  - `RoundedRectRefractionWithDispersionShaderString` (7-band chromatic dispersion)
  - `LayerBackdrop` with `GraphicsLayer.record`
  - `BackdropEffectScope` (`blur`, `colorControls`, `lens`)
  - `Modifier.drawBackdrop` with 0.33f scale.

### Step 2: Author `LiquidGlass.kt`
- Port `LocalAppBackdrop`, `LocalLiquidGlassEnabled`, and `Modifier.liquidGlass(shape, surfaceTint, borderWidth, borderColor)`.

### Step 3: Integrate into `CoupleFitNavigationSuite.kt`
- Record dashboard content with `.layerBackdrop(appBackdrop)`.
- Apply `.liquidGlass(shape = pillShape, surfaceTint = ...)` to the dock backdrop.
- Implement BitChord sliding pill stretch & squash kinematics with `reduceMotion` gating.

### Step 4: Deterministic Verification & Live Device Testing
- Run `./gradlew.bat testDebugUnitTest`.
- Run `./gradlew.bat compileDebugAndroidTestKotlin`.
- Deploy to Samsung Galaxy S24 and capture ARTEMIS screenshots.

### Step 5: Documentation Synchronization & Git Release
- Update `Fixinglist.md`, `triage.md`, `phases.md`, `INDEX.md`, and write `phase41_verification_report.md`.
- Update `knowledge-graph.md`.
- Commit with conventional messaging and tag release `v2.9.26-bitchord-liquid-glass-refraction-liq04`.
