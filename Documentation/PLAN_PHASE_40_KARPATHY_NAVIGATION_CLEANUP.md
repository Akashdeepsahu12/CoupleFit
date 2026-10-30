# Phase 40 Milestone Plan: Karpathy Navigation & Liquid Glass Engine De-Slop & Performance Hardening (`KRP-07`)

> **Governing Principles**: Karpathy Guidelines (Think Before Coding, Simplicity First, Surgical Changes, Goal-Driven Execution)  
> **Master Defect Catalog**: [`Documentation/Fixinglist.md`](file:///D:/Couple_fit/CoupleFit/Documentation/Fixinglist.md) (§64, `KRP-07`)  
> **Triage Catalog**: [`Documentation/triage.md`](file:///D:/Couple_fit/CoupleFit/Documentation/triage.md)  
> **Target Files**:
> - [`app/src/main/java/com/couplefit/ui/glass/LiquidGlassEngine.kt`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/ui/glass/LiquidGlassEngine.kt)
> - [`app/src/main/java/com/couplefit/ui/navigation/CoupleFitNavigationSuite.kt`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/ui/navigation/CoupleFitNavigationSuite.kt)

---

## 1. Executive Summary & Defect Scope

Following the Karpathy Codebase Review & Audit of Phase 39, five concrete issues were uncovered across speculative code, accessibility motion gating, draw-phase optimization, and orphaned dependencies:
1. **Dead Speculative Abstraction**: `standaloneLiquidGlass` (~70 lines of unused code), `isGlassSupported()`, and related constants in `LiquidGlassEngine.kt` were created speculatively and are never invoked.
2. **Hardcoded Geometric Discrepancy**: `liquidRefractionLens` hardcoded `val r = with(density) { 32.dp.toPx() }` inside the draw scope instead of using the true pill radius (`size.height * 0.5f = 34.dp`).
3. **Accessibility Motion Bypass**: The floating navigation dock accepted `reduceMotion: Boolean = false`, but its internal spring animations (`Spring.DampingRatioMediumBouncy`) and elastic stretching (`stretchFactor`) completely ignored it.
4. **Compose Draw Phase Inefficiency**:
   - Background and border gradient `Brush` objects were instantiated on every recomposition instead of being `remember`ed.
   - `Modifier.offset(x = tabWidth * animatedIndex)` caused a full Compose **Layout** pass on every frame of the spring animation rather than staying purely in the **Draw** phase via `graphicsLayer { translationX = ... }`.
5. **Orphaned Import**: `import androidx.compose.foundation.BorderStroke` in `CoupleFitNavigationSuite.kt` left behind from Phase 37.

---

## 2. Explicit Acceptance Criteria & Test Bars

- [ ] **AC 1 (Simplicity / Anti-Slop)**: Remove `Modifier.standaloneLiquidGlass`, `isGlassSupported()`, `GLASS_EDGE_WIDTH`, `GLASS_EDGE_COLOR`, and unused imports from `LiquidGlassEngine.kt`.
- [ ] **AC 2 (True Geometry)**: In `liquidRefractionLens`, calculate the corner radius dynamically as `size.height * 0.5f` so that the AGSL refraction lens perfectly matches the 68dp dock's continuous pill curvature.
- [ ] **AC 3 (Accessibility)**: When `reduceMotion == true`, dock animations must snap instantly (`snap()`), disable elastic elongation (`stretchFactor = 1f`), and disable icon spring scaling.
- [ ] **AC 4 (Performance & Zero Relayout)**:
   - `remember` the backdrop gradient, specular border gradient, and active pill gradient.
   - Migrate indicator pill positioning to `graphicsLayer { translationX = ... }` to eliminate layout re-measuring during spring movement.
- [ ] **AC 5 (Clean Hygiene)**: Remove orphaned `BorderStroke` import from `CoupleFitNavigationSuite.kt`.
- [ ] **Verification Bar**:
   - `.\gradlew.bat testDebugUnitTest` passes 100%.
   - `.\gradlew.bat compileDebugAndroidTestKotlin` passes with 0 errors.
   - Deploy to `emulator-5554` and verify visually with ARTEMIS.

---

## 3. Step-by-Step Execution Plan

### Step 1: Clean and Prune `LiquidGlassEngine.kt`
- Remove lines 110-179 (`standaloneLiquidGlass`).
- Remove `isGlassSupported()`, `GLASS_EDGE_WIDTH`, `GLASS_EDGE_COLOR`.
- Prune unused imports: `MaterialTheme`, `drawWithContent`, `luminance`.
- In `liquidRefractionLens`, compute `val r = size.height * 0.5f` dynamically.

### Step 2: Optimize and Gate `CoupleFitNavigationSuite.kt`
- Remove `import androidx.compose.foundation.BorderStroke`.
- `remember` all 3 gradient brushes (`backdropGradient`, `borderGradient`, `activeIndicatorGradient`).
- Gate `animatedIndex`, `stretchFactor`, `iconScale`, and `animatedColor` on `reduceMotion`.
- Migrate active indicator capsule from `Modifier.offset(x = ...)` to `Modifier.graphicsLayer { translationX = ...; scaleX = ... }`.

### Step 3: Deterministic Verification & ARTEMIS Hardware Inspection
- Run `./gradlew.bat testDebugUnitTest`.
- Run `./gradlew.bat compileDebugAndroidTestKotlin`.
- Deploy to emulator and inspect live navigation with ARTEMIS.

### Step 4: Documentation Synchronization & Git Release
- Update `Documentation/Fixinglist.md` (add Section 64, `KRP-07`).
- Update `Documentation/triage.md` (update resolved count to 92).
- Update `Documentation/phases.md` (add Phase 40).
- Update `Documentation/INDEX.md`.
- Generate `Documentation/phase40_verification_report.md`.
- Update `.planning/graphs/knowledge-graph.md`.
- Commit with conventional messaging and tag release `v2.9.25-karpathy-navigation-cleanup-krp07`.
