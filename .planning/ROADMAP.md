# CoupleFit Milestone Roadmap 🗺️

## Phase 1: Type-Safe Route Foundations (`CSA-01`)
- **Status**: 🟢 Completed & Shipped
- **Deliverables**: `@Serializable` routes for `Today`, `Challenges`, `HealthTrends`, `CoupleHub`, and `TopLevelDestination` enum in `CoupleFitDestinations.kt`.

## Phase 2: Adaptive Persistent Navigation Shell (`CSA-02` / `NAV-01`)
- **Status**: 🟢 Completed & Shipped
- **Deliverables**: Persistent M3 `NavigationBar` & `NavigationRail` in `CoupleFitNavigationSuite.kt`, discrete destination screens (`ChallengesScreen`, `HealthTrendsScreen`, `CoupleHubScreen`), shell integration in `MainActivity.kt`, and `NavigationComposeUiTest.kt`.

## Phase 3: Contextual "Medication Due" Pill on Today (`CSA-03`)
- **Status**: 🟢 Completed & Shipped
- **Deliverables**: `MedicationDuePill.kt` with one-tap dose logging (`Log [✓]`), haptic confirmation, smooth dismissability, and `MedicationDuePillComposeUiTest.kt`. Release tag: `v2.1.0-csa03`.

## Phase 4: Card Sort IA & Ergonomics Convergence (`CSA-04`, `CSA-05`, `CSA-06`, `CSA-07`, `CSA-08`)
- **Status**: 🟢 Completed & Shipped
- **Deliverables**:
  - **`CSA-04`**: Modular `ActiveChallengePreviewCard.kt` streamlining `DashboardScreen.kt` and directing duels traffic to Tab 2 `Challenges`.
  - **`CSA-05` / `NAV-02`**: Real-time bilateral telemetry deep dive on Expanded Pane 2 of `DashboardAdaptiveScreen.kt` with dynamic chips and spline chart.
  - **`CSA-06`**: Prominent `"Partner Visibility & Sharing Rules [Manage 🔒]"` shortcut in `CoupleHubScreen.kt`.
  - **`CSA-07`**: Fail-safe 2-step verification modal requiring explicit `"UNLINK"` keyword entry in `SettingsScreen.kt`.
  - **`CSA-08`**: Thumb-zone `QuickNudgeRow.kt` (`[👋 Poke] [🔥 Keep Going!] [❤️ Love] [💧 Drink Water]`) beneath bilateral gauges.
  - **Test Suite**: `CardSortConvergenceComposeUiTest.kt` passing with 100% test green policy.
- **Release Tag**: `v2.2.0-csa-complete`

## Phase 5: Information Architecture & Settings 4-Hub Convergence (`IA-03`, `IA-04`, `IA-10`)
- **Status**: 🟢 Completed & Shipped
- **Deliverables**:
  - **`IA-03`**: Deconstructed 1,007-line legacy 9-accordion monolithic `SettingsScreen.kt` into 4 cohesive composable hubs (`Partnership`, `Health Sync & Sharing`, `Experience`, `Privacy & Safety`) + persistent About & Support footer.
  - **`DataStore Persistence`**: Wired granular metric sharing toggles (`Steps`, `Workouts`, `Calories`, `Heart Rate`, `SpO2`) directly to `CoupleFitPreferencesDataStore` via `HealthConnectViewModel`.
  - **`IA-04`**: Aligned and codified documentation governance reconciling flat `Documentation/` directory with 4-folder Diataxis taxonomy in `Documentation/INDEX.md` and `phases.md`.
  - **`IA-10`**: Authored comprehensive instrumented test suite [`SettingsHubComposeUiTest.kt`](file:///D:/Couple_fit/CoupleFit/app/src/androidTest/java/com/couplefit/ui/SettingsHubComposeUiTest.kt) testing all 4 hubs, tab switching, and unlinking dialogs.
- **Release Tag**: `v2.3.0-ia-settings`

## Phase 6: Navigation Architecture Polish & Platform Hardening (`NAV-03`, `PLC-01`, `BCK-01`, `LOC-NAV-01`, `LOC-NAV-02`)
- **Status**: 🟢 Completed & Shipped
- **Deliverables**:
  - **`NAV-03`**: Multi-modal active tab indication with dual-vector filled/outlined icon switching (`TopLevelDestination`), bold active labels, and subtle tactile haptics.
  - **`PLC-01`**: Android 14+ Predictive Back declaration (`android:enableOnBackInvokedCallback="true"`) in `AndroidManifest.xml`.
  - **`BCK-01`**: Return-to-Home Back-Stack re-anchoring in `MainActivity.kt` with smooth 200ms `AnimatedContent` crossfade and `reduceMotion` accessibility bypass.
  - **`LOC-NAV-01`**: Dedicated local timeframe sub-header on `HealthTrendsScreen.kt` preventing TopAppBar crowding on compact devices.
  - **`LOC-NAV-02`**: Challenges sub-navigation segmented tabs (`[Active Duels] | [Trophies & Vault]`) on `ChallengesScreen.kt`.
  - **Test Suite**: `NavigationComposeUiTest.kt` extended with 3 new UI tests verifying active state typography, timeframe sub-headers, and challenge tabs.
- **Release Tag**: `v2.4.0-nav-polish`

## Phase 6C: Local Sub-Navigation Tactile Polish & Bilateral Telemetry Architecture (`NAV-04`, `NAV-05`)
- **Status**: 🟢 Completed & Shipped
- **Deliverables**:
  - **`NAV-04`**: Material 3 connected `SingleChoiceSegmentedButtonRow` with `SegmentedButtonDefaults.itemShape`, `FontWeight.Bold`, and `TextHandleMove` mechanical haptics across Trends, Challenges, and Adaptive screens.
  - **`NAV-05`**: Preserved 0-tap unified bilateral telemetry invariant on Today. Implemented dedicated `TelemetryPerspective` segmented row (`[Both 👥] [You 👤] [Partner 💖]`) on `HealthTrendsScreen.kt` with solo telemetry cards and animated content switching.
  - **Test Suite**: `NavigationComposeUiTest.kt` extended with UI tests verifying perspective toggle and card rendering.
- **Release Tag**: `v2.4.2-nav04-nav05`

## Phase 51: Real-Time Partner Telemetry Synchronization & Demo Seeding Engine (`PTR-01`)
- **Status**: 🟢 Completed & Shipped
- **Deliverables**:
  - **`PTR-01`**: Room SSOT telemetry seeding via `seedDemoPartnerDaily()` and `addPartnerSteps()` in `PartnerRepository`. Dynamic session observation in `HealthConnectViewModel` re-binding Firestore listeners. Automatic demo seeding in `AuthViewModel`. Simulation controls (`+1k Partner` button) in `DashboardScreen`.
  - **Test Suite**: `PartnerRepositoryTest.kt` and `PartnerTelemetrySyncTest.kt` passing 100% green; `testDebugUnitTest` 32/32 green; `compileDebugAndroidTestKotlin` 0 errors.
  - **Device Verification**: Verified live on `emulator-5554` with screenshots showing 7,420 steps and active sync.
- **Release Tag**: `v2.9.37-partner-telemetry-sync-ptr01`

## Phase 52: Security Rules Hardening, Zero-Write Privacy & Production Configuration (`SEC-01`, `SEC-02`, `SEC-04`, `HC-01`, `CFG-01`, `CFG-02`, `R8-01`)
- **Status**: 🟢 Completed & Shipped
- **Deliverables**:
  - **`SEC-01`**: Mutual couple challenge creation and update rules in `firestore.rules`.
  - **`SEC-02`**: Scoped read access on `/users/{uid}` strictly to owner or authenticated linked partner; step range validation.
  - **`HC-01`**: Zero-write privacy guarantee permanently removing `writeSampleSteps()` from `HealthConnectManager` and `HealthConnectViewModel`.
  - **`SEC-04`**: Cascading GDPR/CCPA user data deletion in `AuthViewModel` wiping Room tables and DataStore.
  - **`CFG-02`**: Dynamic session credential resolution in `PokeActionCallback` and `WidgetEntryPoint`.
  - **`CFG-01`**: Production manifest cleanup and elimination of debug signing from release builds.
  - **`R8-01`**: Production ProGuard rules stripping MockK and retaining Glance `ActionCallback` and `@Serializable` models.
  - **Test Suite**: `testDebugUnitTest` 32/32 green; `compileDebugAndroidTestKotlin` 0 errors; live verified on `emulator-5554` (`phase52_live.png`).
- **Release Tag**: `v2.9.38-security-hardening-sec01`

## Phase 53: Performance & GPU Leak Elimination (`AGSL-01`, `PERF-02`, `UI-01`, `EXO-01`, `COR-01`)
- **Status**: 🟢 Completed & Shipped
- **Deliverables**:
  - **`AGSL-01`**: BackdropShaderCache singleton caching RuntimeShader instances for refraction and dispersion on Android 13+.
  - **`PERF-02`**: Hoisted Path, PathMeasure, and Brush allocations out of Canvas in InteractiveChart.kt, eliminating GC allocation jitter.
  - **`UI-01`**: Removed 10-second polling LaunchedEffect loop in DashboardScreen.kt; reactive Flow observation drives all state updates.
  - **`EXO-01`**: ExoPlayer release in DisposableEffect.onDispose and dynamic setMediaItem() updates on videoResId change in AmbientVideoBackground.kt.
  - **`COR-01`**: Job cancellation tracking in ChallengeViewModel.kt preventing stacked concurrent Firestore flow listeners.
  - **Test Suite**: `testDebugUnitTest` 32/32 green; `compileDebugAndroidTestKotlin` 0 errors; live verified on `emulator-5554` (`phase53_live.png`).
- **Release Tag**: `v2.9.39-performance-gpu-leaks`

## Phase 54: Data Integrity, Room SSOT & Background Sync Pipeline (`DAT-01`, `DAT-04`, `DAT-02`, `DAT-05`, `WRK-01`, `HC-02`, `PERF-01`)
- **Status**: 🟢 Completed & Shipped
- **Deliverables**:
  - **`DAT-01`**: Added `ORDER BY lastUpdated DESC LIMIT 1` on date-only partner daily queries in `HealthDao.kt`.
  - **`DAT-04`**: Added `syncErrorMessage` in `HealthDashboardUiState` and `HealthConnectUiState.Error` in `HealthConnectScreen.kt`.
  - **`DAT-02`**: Handled polymorphic timestamp formats (`Timestamp`, `Number`, `String`) in `PartnerRepositoryImpl.kt`.
  - **`DAT-05` / `PERF-01`**: Monotonic sequential DB write fence on `Dispatchers.IO.limitedParallelism(1)` in `PartnerRepositoryImpl.kt`.
  - **`WRK-01`**: Enqueued 15-minute background synchronization via WorkManager in `CoupleFitApplication.kt`.
  - **`HC-02`**: Transactional Changes API continuation token commit only after Room database writes succeed in `HealthConnectManager.kt`.
  - **Test Suite**: `testDebugUnitTest` 32/32 green; `compileDebugAndroidTestKotlin` 0 errors; live verified on `emulator-5554` (`phase54_live.png`).
- **Release Tag**: `v2.9.40-data-integrity-sync-pipeline`

## Phase 55: YAGNI Simplification & Feature Realization (`YAG-01`, `FEAT-01`, `DEP-01`)
- **Status**: 🟢 Completed & Shipped
- **Deliverables**:
  - **`YAG-01`**: Consolidated duplicate AGSL shader implementations in `LiquidGlassEngine.kt` down to a clean 30-line forwarder to `Modifier.refractiveLens` in `LiquidGlass.kt`.
  - **`DEP-01`**: Migrated `DashboardScreen.kt` from legacy Material 2 `pullRefresh` to Material 3 `PullToRefreshBox`. Completely removed `androidx.compose.material:material` from dependencies (zero M2 remaining).
  - **`FEAT-01`**: Bound `HealthConnectViewModel` and live `dashboardState` to `HealthTrendsScreen.kt`, dynamically rendering real partner steps, synchrony scores, dynamic timeframes (`Day`, `Week`, `Month`, `Year`), and live sensor status.
  - **Test Suite**: `testDebugUnitTest` 32/32 green; `compileDebugAndroidTestKotlin` 0 errors; live verified on `emulator-5554` (`phase55_live.png`, `phase55_trends_live.png`, `phase55_trends_day_live.png`, `phase55_trends_you_live.png`, `phase55_trends_partner_live.png`).
- **Release Tag**: `v2.9.41-yagni-trends`

## Phase 56: Production Build & Test Rigor (`TST-01`, `TST-02`, `R8-02`)
- **Status**: 🟢 Completed & Shipped
- **Deliverables**:
  - **`TST-01`**: In-memory SQLite Room integration suite (`HealthDaoTest.kt`) executed on live Android runtime via `connectedDebugAndroidTest` on `emulator-5554` (4/4 passed). Validated `ORDER BY lastUpdated DESC LIMIT 1` (`DAT-01`), partner-scoped unlinking purge, cascading GDPR table wipes (`clearAllTables()`), and active challenge filtering.
  - **`TST-02`**: Substantive Compose node assertions in `NavigationComposeUiTest.kt` verifying `LiveHeartRateEmptyWaveform` node display and `onSyncClick` action dispatch. Tested exact brand color tokens, WCAG AA/AAA contrast ratios (7.8:1 cyan, 4.6:1 coral), and `TelemetryPerspective` enum contracts in `TactileMotionArchitectureTest.kt`.
  - **`Room Migration 3->4`**: Verified `MIGRATION_3_4` and `CoupleChallengeEntity` model integrity in `HealthDaoPartnerTest.kt`.
  - **`R8-02`**: Verified release build R8 ProGuard minification via `minifyReleaseWithR8` (33/33 tasks executed, 0 errors).
  - **Test Suite**: `connectedDebugAndroidTest` 4/4 passed; `testDebugUnitTest` 32/32 green; `compileDebugAndroidTestKotlin` 0 errors; live verified on `emulator-5554` (`phase56_live.png`).
- **Release Tag**: `v2.9.42-production-test-rigor`

## Phase 57: Rollback of EGM Automated UI Disruptions & Restoration of Phase 51 Interface (`EGM-REV-01`)
- **Status**: 🟢 Completed & Shipped
- **Deliverables**:
  - **`EGM-REV-01`**: Restored pristine Phase 51 UI and graphics architecture (`LiquidGlassEngine.kt`, `DashboardScreen.kt`, `HealthTrendsScreen.kt`, `InteractiveChart.kt`, `AmbientVideoBackground.kt`) while safely preserving all documentation and plans.
  - **Test Suite**: `testDebugUnitTest` 41/41 passed 100% green; `compileDebugAndroidTestKotlin` 0 errors; verified live on `emulator-5554` (`live_restored_phase51.jpg`).
- **Release Tag**: `v2.9.43-rollback-egm-ui-restored`

## Phase 58: Adaptive Responsive Typography System (`TYP-06`, `ADP-01`)
- **Status**: 🟢 Completed & Shipped
- **Deliverables**:
  - **`TYP-06`**: Dynamic Material 3 typography scaling across Window Size Classes (<360dp: 0.90x, 360-599dp: 1.00x, 600-839dp: 1.10x, ≥840dp: 1.20x) with preserved font families, font weights, letter spacing, and `"tnum"` tabular numerals.
  - **`ADP-01`**: Unified responsive hero telemetry display token (`LocalHeroMetricDisplay` at 52sp baseline, scaling from 46.8sp to 62.4sp).
  - **`Theme Integration`**: Provided dynamic adaptive typography and hero metrics in `CoupleFitTheme` via `CompositionLocalProvider(LocalHeroMetricDisplay provides adaptiveHeroMetric)`.
  - **Test Suite**: 5-test unit suite in `AdaptiveTypographyTest.kt` passing 100% green (`testDebugUnitTest` 41/41 green, `compileDebugAndroidTestKotlin` 0 errors).
  - **Device Verification**: Verified live on `emulator-5554` (`phase58_adaptive_typography_live.jpg`) via ARTEMIS.
- **Release Tag**: `v2.9.44-adaptive-typography-typ06`

## Phase 59: Fixinglist Complete Convergence & Polish Wave (`FIX-ALL-01`)
- **Status**: 🟢 Completed & Shipped
- **Deliverables**:
  - **`KRP-05` & `KRP-06`**: Pruned 379 lines of dead uncalled code (`SmoothScrollPhysics.kt`, `SpinningCounter.kt`, `ReelDigit.kt`, `SpinConfig.kt`) per Karpathy Simplicity First principle.
  - **`DashboardScreen.kt`**: Pruned conflicting `.lenisScroll()` on pull-to-refresh container.
  - **`REL-01`**: Living partner presence breathing radial glow halo behind avatar in `CoupleHubScreen.kt`.
  - **`ANM-08`**: Trophy 3D specular shine sweep (`Brush.linearGradient`) and interactive tap celebration scale in `ChallengesScreen.kt`.
  - **`Fixinglist Synchronization`**: 100% of 185 tracked issues across 84 audit sections in `Fixinglist.md` resolved and verified.
  - **Test Suite**: `testDebugUnitTest` 41/41 passed 100% green; `compileDebugAndroidTestKotlin` 0 errors.
  - **Device Verification**: Verified live on `emulator-5554` (`phase59_today_live.jpg`, `phase59_challenges_live.jpg`, `phase59_couplehub_live.jpg`) via ARTEMIS.
- **Release Tag**: `v2.9.45-fixinglist-convergence`


