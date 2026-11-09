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
- **Status**: 🟡 In Progress
- **Goal**: Cache `RuntimeShader` compilation outside draw loops, eliminate `Path` allocations in `InteractiveChart`, remove 10-second polling recompositions in `DashboardScreen`, release `ExoPlayer` on dispose in `AmbientVideoBackground`, and cancel stale jobs in `ChallengeViewModel`.
