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

## Phase 5: Production Hardening & Remote Synchronization
- **Status**: ⚪ Pending Next Milestone
- **Goal**: Full offline-first sync reconciliation, battery-efficient BLE background telemetry polling, and End-to-End device session validation.
