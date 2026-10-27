# Phase 22: Dashboard UI Design System Harmonization (`PLAN_PHASE_22_UI_HARMONIZATION.md`)

## 1. Problem Statement & Visual Discrepancy Audit
Following the Phase 20/21 iterations on the Live Heart Rate card, user inspection on live hardware via Google ARTEMIS indicated:
> *"it doesn't match with the ui"*

### Detailed Visual Discrepancy Audit (`DSN-05`, `LAY-02`)
1. **Glass Material & Color Inconsistency (`DSN-05`)**:
   - *Dashboard Standard*: All cards (`SideBySideActivityCard`, `Standings Card`, `ActiveChallengePreviewCard`, `StreakTracker`) use `Modifier.iosGlass()` with the app-wide translucent slate base (`GlassPrimitives.TranslucentSlateBase` = `0xCC141720`), subtle vertical gradient specular sheen (`GlassSemantics.CardSurfaceBrush`), and 1.dp diamond-cut specular rim border (`BorderSpecularRim`).
   - *Heart Rate Card Flaw*: Was using `.liquiGlass()` with custom dark indigo/navy blue tint tokens (`LiquiTokens.DarkTint` `#303246` and `DarkTintDeep` `#121420`), causing the card to appear as a jarring, discordant purple-blue block against the obsidian slate dashboard.
2. **Corner Radius Geometry Mismatch**:
   - *Dashboard Standard*: All dashboard cards strictly adhere to `MaterialTheme.shapes.large` (16dp rounded corners).
   - *Heart Rate Card Flaw*: Was using `LiquiTokens.CardRadius` (22dp squircle), making it visibly rounder and bulbous compared to the adjacent cards.
3. **Typographic Hierarchy & Eyebrow Mismatch**:
   - *Dashboard Standard*: Every section uses the standardized design system eyebrow:
     `style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 0.5.sp` (e.g., `TODAY'S COMPETITION`, `ACTIVITY GAUGES`, `ACTIVE CHALLENGE`).
   - *Heart Rate Card Flaw*: Was using mixed-case `Text("Heart Rate", style = MaterialTheme.typography.titleMedium)` without tracking, breaking the visual scanning cadence of the dashboard.
4. **Nested Cutout Box in Empty State**:
   - *Dashboard Standard*: Content breathes directly on the card's native frosted glass surface.
   - *Heart Rate Card Flaw*: `LiveHeartRateEmptyWaveform` had an inner `Box` with `background(LiquiTokens.DarkTintDeep.copy(alpha = 0.45f))` and a heavy solid button, creating an ugly "nested widget" appearance.
5. **Horizontal Margins & Width Alignment (`LAY-02`)**:
   - `ActiveChallengePreviewCard` had duplicate horizontal padding (`.padding(horizontal = AppSpacing.md)` inside a `HorizontalPager` that already had `contentPadding = PaddingValues(horizontal = AppSpacing.md)`), making it narrower than `Standings Card` and `Heart Rate Card`.

---

## 2. Architectural Solution
1. **Harmonize Heart Rate Card with `iosGlass`**:
   - Replaced `.liquiGlass()` with `.iosGlass(shape = MaterialTheme.shapes.large)`.
   - Card shape set to `MaterialTheme.shapes.large` (16dp).
   - Card background and borders now seamlessly match the slate glassmorphic design system of all other cards.
2. **Align Typographic Hierarchy**:
   - Standardized header to:
     - Left: `AnimatedHeartIcon(16.dp, tint = VitalsAccent)` + `Text("LIVE HEART RATE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 0.5.sp)`.
     - Right: Telemetry status pill (`🟢 Live Telemetry` / `⚪ Health Connect`) matching `SideBySideActivityCard`.
3. **Refine Ambient ECG Waveform Stage (`LiveHeartRateEmptyWaveform.kt`)**:
   - Removed the nested dark box; ECG line and status draw directly on the card's native frosted glass surface.
   - Rhythmic ECG cardiogram baseline path (`───/\_/\_/\───`) in `VitalsAccent` with gentle breathing alpha (0.35f to 0.75f) and traveling glowing pulse bead.
   - Action button styled as a frosted glass pill: `Surface(shape = RoundedCornerShape(12.dp), color = VitalsAccent.copy(alpha = 0.12f), border = BorderStroke(1.dp, VitalsAccent.copy(alpha = 0.4f)))` with `"Sync Health Connect ↻"`.
4. **Fix Width Alignment in `ActiveChallengePreviewCard.kt` (`LAY-02`)**:
   - Replaced duplicate `.padding(horizontal = AppSpacing.md)` with `.padding(vertical = AppSpacing.xs)` so all cards have 100% unified width.

---

## 3. Explicit Acceptance Criteria & Test Bars
- [ ] **AC-1**: Heart Rate card uses `Modifier.iosGlass(shape = MaterialTheme.shapes.large)` and matches all other cards in material, color, and 16dp corner radius.
- [ ] **AC-2**: Card header matches dashboard design system standard (`LIVE HEART RATE` in `labelSmall`, bold, tracked, with `AnimatedHeartIcon` and status pill).
- [ ] **AC-3**: Empty state draws ambient ECG waveform and traveling pulse bead directly on card glass without nested dark boxes.
- [ ] **AC-4**: All cards on Tab 0 have identical unified width and margins.
- [ ] **AC-5**: `.\gradlew.bat testDebugUnitTest` passes 100% green.
- [ ] **AC-6**: `.\gradlew.bat compileDebugAndroidTestKotlin` compiles cleanly.
- [ ] **AC-7**: Live device screenshot verification via ARTEMIS proves complete visual harmony.
- [ ] **AC-8**: Documentation updated in `Fixinglist.md`, `triage.md`, `phases.md`, `INDEX.md`, `RELEASE_NOTES_v2.9.7.md`, and `knowledge-graph.md`.
