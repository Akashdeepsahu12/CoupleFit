# Phase 53 Milestone Plan: Performance & GPU Leak Elimination

**Target Milestone**: v2.9.39  
**Status**: IN EXECUTION  
**Associated Defect IDs**: `AGSL-01`, `PERF-02`, `UI-01`, `EXO-01`, `COR-01`  

---

## 1. Problem Statement & Findings
The EGM Goldfish review identified 5 critical performance hazards affecting UI framerate, GPU memory, battery consumption, and coroutine lifecycle:
1. **`AGSL-01`**: `BackdropEngine.kt` instantiates `RuntimeShader(shaderString)` directly inside the draw-phase `lens()` modifier method on every single frame, causing repeated Skia shader compilation, GC allocation churn, and frame-rate degradation on Android 13+.
2. **`PERF-02`**: `InteractiveChart.kt` instantiates up to 6 `Path` objects, 2 `PathMeasure` objects, and multiple `Brush.verticalGradient` instances inside the `Canvas` draw pass every time `currentDrawProgress` interpolates across animation ticks.
3. **`UI-01`**: `DashboardScreen.kt` runs an unthrottled `while (true)` coroutine ticking `syncTrigger++` every 10 seconds, forcing redundant dashboard recompositions even when reactive Room and Firestore Flows are already observing and driving state changes.
4. **`EXO-01`**: `AmbientVideoBackground.kt` fails to release `ExoPlayer` when the composable leaves composition (only releasing on `onSurfaceTextureDestroyed`), and fails to dynamically switch the media item if `videoResId` changes while active.
5. **`COR-01`**: `ChallengeViewModel.kt` `observeChallenges()` launches un-tracked coroutines without canceling previous listeners, leading to stacked concurrent Firestore flows when couple IDs update.

---

## 2. Explicit Acceptance Criteria
- [ ] **AC-53.1 (`AGSL-01`)**: `BackdropEngine.kt` uses a singleton/cached `RuntimeShader` instance for both refraction and dispersion shaders on Android 13+ (`@RequiresApi(TIRAMISU)`), setting uniforms on the cached shader rather than recompiling.
- [ ] **AC-53.2 (`PERF-02`)**: `InteractiveChart.kt` hoists `Brush` allocations and reuses `Path` instances with `.rewind()` / `.reset()`, eliminating object allocation inside the frame render loop.
- [ ] **AC-53.3 (`UI-01`)**: The 10-second polling `syncTrigger++` loop in `DashboardScreen.kt` is removed; reactive `Flow` observation drives all state updates.
- [ ] **AC-53.4 (`EXO-01`)**: `AmbientVideoBackground.kt` releases `ExoPlayer` inside `DisposableEffect.onDispose` and dynamically swaps `setMediaItem()` via `LaunchedEffect(videoResId, exoPlayerRef)`.
- [ ] **AC-53.5 (`COR-01`)**: `ChallengeViewModel.kt` tracks `observeJob: Job?` and cancels any existing listener before launching a new challenge observation.
- [ ] **AC-53.6**: Unit tests pass 100% green (`.\gradlew testDebugUnitTest`); AndroidTest Kotlin compiles with zero errors; verified on live emulator (`emulator-5554`).

---

## 3. Implementation Tasks & Dependencies
1. **Task 1 (`BackdropEngine.kt`)**: Add `ShaderCache` singleton with lazy `RuntimeShader` instantiation. Update `lens()` to use cached shaders.
2. **Task 2 (`InteractiveChart.kt`)**: Re-use `Path` instances (`rewind()`) and hoist gradient brushes.
3. **Task 3 (`DashboardScreen.kt`)**: Remove the 10-second polling `LaunchedEffect`.
4. **Task 4 (`AmbientVideoBackground.kt`)**: Add player release in `DisposableEffect.onDispose` and media item dynamic update in `LaunchedEffect(videoResId, exoPlayerRef)`.
5. **Task 5 (`ChallengeViewModel.kt`)**: Add `private var observeJob: Job? = null` and cancel prior job in `observeChallenges()`.
6. **Task 6 (Verification & Report)**: Run automated tests, install to emulator, capture screen state, and write `Documentation/phase53_verification_report.md`.
