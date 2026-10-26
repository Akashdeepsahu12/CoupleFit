# CoupleFit Milestone Roadmap 🗺️

## Phase 1: Type-Safe Route Foundations (`CSA-01`)
- **Status**: 🟢 Completed & Shipped
- **Deliverables**: `@Serializable` routes for `Today`, `Challenges`, `HealthTrends`, `CoupleHub`, and `TopLevelDestination` enum in `CoupleFitDestinations.kt`.

## Phase 2: Adaptive Persistent Navigation Shell (`CSA-02` / `NAV-01`)
- **Status**: 🟢 Completed & Shipped
- **Deliverables**: Persistent M3 `NavigationBar` & `NavigationRail` in `CoupleFitNavigationSuite.kt`, discrete destination screens (`ChallengesScreen`, `HealthTrendsScreen`, `CoupleHubScreen`), shell integration in `MainActivity.kt`, and `NavigationComposeUiTest.kt`.

## Phase 3: Contextual "Medication Due" Pill on Today (`CSA-03`)
- **Status**: 🟡 In Progress (Current Phase)
- **Goal**: Add an interactive dismissable/clickable contextual reminder pill `[💊 08:00 AM Vitamin D3 Due - Tap to Log [✓]]` at the top of `Today` (`DashboardScreen.kt`) based on tree test TS-04 findings.
- **Requirements**:
  - Contextual reminder pill with high visual prominence and minimum 48dp touch target.
  - Interactive "Tap to Log [✓]" action with instant haptic and animated confirmation.
  - Dismissable with smooth collapse animation.
  - Zero-defect test bar: 100% green unit tests, compile Android tests, and ARTEMIS verification.

## Phase 4: Modular Screen Extraction (`CSA-04`)
- **Status**: ⚪ Pending
- **Goal**: Deconstruct 1,641-line `DashboardScreen.kt` monolith into dedicated composables.

## Phase 5: Adaptive Multi-Pane Telemetry (`CSA-05` / `NAV-02`)
- **Status**: ⚪ Pending
- **Goal**: Bind right-hand pane in `DashboardAdaptiveScreen.kt` to live telemetry metrics.
