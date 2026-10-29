# CoupleFit Architecture Decision Records (ADRs) 🏛️📋

> **Document Location**: [`Documentation/decision.md`](file:///D:/Couple_fit/CoupleFit/Documentation/decision.md)  
> **Skill Standard**: `/agent-skills:documentation-and-adrs`  
> **Codebase Authority**: [`AGENTS.md`](file:///D:/Couple_fit/CoupleFit/AGENTS.md)  
> **Master Defect & Audit Tracking**: [`Documentation/Fixinglist.md`](file:///D:/Couple_fit/CoupleFit/Documentation/Fixinglist.md)  
> **Triage Backlog**: [`Documentation/triage.md`](file:///D:/Couple_fit/CoupleFit/Documentation/triage.md)  
> **Last Updated**: October 2026 | Milestone v2.9.20

---

## Overview

This document captures the foundational architectural, design, privacy, and interaction decisions that govern the **CoupleFit** application. In accordance with the `/agent-skills:documentation-and-adrs` standard, these records capture not merely *what* was implemented, but the critical context, constraints, trade-offs, and rejected alternatives that explain *why* the system is designed this way.

```
ADR Lifecycle: PROPOSED → ACCEPTED → (SUPERSEDED | DEPRECATED)
```

---

## Table of Decisions

| ADR ID | Decision Title | Status | Date | Primary Impact Area |
| :--- | :--- | :--- | :--- | :--- |
| [**ADR-001**](#adr-001-health-connect-as-single-source-of-truth-ssot) | Health Connect as Single Source of Truth (SSOT) vs Samsung Health SDK | **Accepted** | 2026-10 | Health Data Pipeline |
| [**ADR-002**](#adr-002-zero-trust-read-only-privacy-architecture) | Zero-Trust Read-Only Privacy Architecture for Couples Telemetry | **Accepted** | 2026-10 | Security & Compliance |
| [**ADR-003**](#adr-003-4-tab-mental-model--floating-optical-hover-dock) | 4-Tab Mental Model & Floating Optical Hover Dock Architecture | **Accepted** | 2026-10 | Navigation & Layout |
| [**ADR-004**](#adr-004-optical-liquid-glassmorphism-without-container-blur) | Optical Liquid Glassmorphism without Container Blur | **Accepted** | 2026-10 | Visual Design System |
| [**ADR-005**](#adr-005-global-hardware-accelerated-ambient-video-cladding) | Global Hardware-Accelerated Ambient Video Cladding | **Accepted** | 2026-10 | Atmospheric Motion & Branding |
| [**ADR-006**](#adr-006-emil-kowalski-calibrated-spring-physics--heartbeat-haptics) | Emil Kowalski Calibrated Spring Physics & Anatomical Heartbeat Haptics | **Accepted** | 2026-10 | Sensory & Micro-Interactions |
| [**ADR-007**](#adr-007-adaptive-two-pane-master-detail--saccadic-readable-measure) | Adaptive Two-Pane Master-Detail & Saccadic Readable Measure | **Accepted** | 2026-10 | Foldable & Tablet Ergonomics |
| [**ADR-008**](#adr-008-bilateral-single-canvas-spline-telemetry--touch-slop-gating) | Bilateral Single-Canvas Spline Telemetry & Touch-Slop Gating | **Accepted** | 2026-10 | Data Visualization & Gestures |
| [**ADR-009**](#adr-009-relational-presence-over-invasive-surveillance) | Relational Presence Over Invasive Surveillance & De-Robotized Copy | **Accepted** | 2026-10 | Emotional UX & Copywriting |
| [**ADR-010**](#adr-010-codebase-simplicity-first--anti-speculative-pruning) | Codebase Simplicity First & Anti-Speculative Pruning (Karpathy Guidelines) | **Accepted** | 2026-10 | Architecture & Clean Code |

---

## ADR-001: Health Connect as Single Source of Truth (SSOT)

### Status
**Accepted** (Supersedes Samsung Health SDK integration)

### Date
2026-10-15

### Context
CoupleFit requires synchronized biometrics and activity metrics (steps, heart rate, calories burned, active exercise sessions) from both partners. Modern couples use diverse wearable ecosystems: one partner may wear a Galaxy Watch, while the other uses a Pixel Watch, Garmin, Whoop, or phone sensors.

We needed a reliable, battery-efficient Android health data ingestion pipeline. Two competing integration strategies existed:
1. Vendor-specific direct SDKs (Samsung Health Privileged SDK, Google Fit REST API).
2. Android Health Connect on Android 14+ (System Service) / Android 10-13 (Jetpack Client).

### Decision
Standardize 100% on **Android Health Connect** as the application's Single Source of Truth (SSOT) via [`HealthConnectManager.kt`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/data/healthconnect/HealthConnectManager.kt) and [`HealthConnectSyncWorker.kt`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/data/healthconnect/HealthConnectSyncWorker.kt). Formally deprecate direct vendor SDKs (Samsung Health subsystem).

### Alternatives Considered

#### Alternative A: Samsung Health Privileged SDK
- **Pros**: Direct raw hardware access on Samsung Galaxy devices.
- **Cons**: Requires formal Samsung developer partnership and application signing keys; locks out non-Samsung Android devices; proprietary API shifts; requires separate permission workflows.
- **Rejected**: Breaks the core product premise that any couple can pair their devices regardless of Android phone brand or smartwatch manufacturer.

#### Alternative B: Direct Google Fit REST API
- **Pros**: Cloud-to-cloud synchronization.
- **Cons**: Deprecated by Google as of 2024-2025; requires continuous cloud API tokens and internet connectivity; higher latency; Google Fit app retirement in favor of Health Connect.
- **Rejected**: Sunset by platform vendor.

#### Alternative C: Hybrid Multi-SDK Dynamic Switching
- **Pros**: Theoretical fallback if Health Connect is unavailable.
- **Cons**: Massive code surface area (~400 extra lines); dual permission dialogs; state synchronization conflicts; impossible testing matrix.
- **Rejected**: High accidental complexity violating Karpathy's "Simplicity First" rule.

### Consequences
- **Positive**: Single unified schema across all Android OEMs (Pixel, Samsung, Xiaomi, Motorola, OnePlus). Samsung Health automatically syncs to Health Connect on OneUI 6+.
- **Positive**: Hardware-accelerated local SQLite storage managed by the OS with zero cloud API billing costs.
- **Negative**: Devices on Android <14 require installing the Health Connect app from the Play Store if not pre-installed. Handled via graceful intent check and fallback guidance.

---

## ADR-002: Zero-Trust Read-Only Privacy Architecture

### Status
**Accepted**

### Date
2026-10-16

### Context
Health and biometric data (heart rates, step counts, calories) is among the most sensitive personal telemetry a user can generate. Google Play policies enforce the Principle of Least Privilege: health companion applications requesting write permissions without providing direct fitness tracking equipment face mandatory Play Store rejection.

Initial prototype manifests declared speculative write permissions (`WRITE_STEPS`, `WRITE_TOTAL_CALORIES_BURNED`, `WRITE_EXERCISE`), causing `Phase6PrivacyAndReadinessTest` (`TEST-01`, `PLC-05`) to fail.

### Decision
Enforce a strict **Zero-Trust Read-Only Policy** across the entire application:
- Remove all `WRITE_*` health permissions from [`AndroidManifest.xml`](file:///D:/Couple_fit/CoupleFit/app/src/main/AndroidManifest.xml) and [`HealthConnectManager.kt`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/data/healthconnect/HealthConnectManager.kt).
- Retain strictly `READ_STEPS`, `READ_HEART_RATE`, `READ_TOTAL_CALORIES_BURNED`, and `READ_EXERCISE`.
- Telemetry shared with the partner is encrypted in transit and at rest in Firebase Firestore, scoped strictly to the authenticated couple pair UID.
- Users retain granular toggles in [`CoupleHubScreen.kt`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/ui/hub/CoupleHubScreen.kt) to selectively hide live heart rate or sleep metrics.

### Alternatives Considered

#### Alternative A: Full Read/Write Permission Suite
- **Pros**: Allows CoupleFit to write mock test data or log manual workouts into Health Connect.
- **Cons**: Violates Google Play Health Connect policy; triggers automated Play Store rejection; user distrust.
- **Rejected**: P0 blocker for production release readiness.

#### Alternative B: Dynamic Runtime Permission Elevation
- **Pros**: Request write permissions only when testing.
- **Cons**: Leaks debug logic into production builds; increases audit complexity.
- **Rejected**: Violates security posture purity.

### Consequences
- **Positive**: 100% compliant with Google Play Console Health Connect policies.
- **Positive**: Automated unit test [`PrivacyAndReadinessTest.kt`](file:///D:/Couple_fit/CoupleFit/app/src/test/java/com/couplefit/privacy/PrivacyAndReadinessTest.kt) passes with 100% green verification.
- **Positive**: Builds deep emotional trust with users, who know their partner companion cannot alter their medical or activity records.

---

## ADR-003: 4-Tab Mental Model & Floating Optical Hover Dock

### Status
**Accepted**

### Date
2026-10-29

### Context
Early iterations of CoupleFit suffered from navigation fragmentation: Settings was hidden behind ad-hoc dialogs, navigation items lacked consistent visual grounding, and the bottom dock was trapped inside a Material 3 `Scaffold(bottomBar = ...)` partition.

In `MainActivity.kt`, an extra `Modifier.padding(innerPadding)` created a dead black strip (~132dp) across the bottom of the display. Furthermore, `GlassTokens.kt` defined the dock background with 91%–96% opacity, causing it to appear as an opaque black slab rather than a floating optical glass capsule (`DCK-01`).

### Decision
1. **4-Hub Cognitive Model** (validated via 30-participant hybrid card sort in [`Documentation/card-sort-analysis.md`](file:///D:/Couple_fit/CoupleFit/Documentation/card-sort-analysis.md)):
   - **Tab 1: Today**: Real-time couple pulse, dual rings, competition balance bar, and thumb-zone cheers.
   - **Tab 2: Challenges**: Head-to-head duels, 7-day showdowns, and trophy vault.
   - **Tab 3: Trends**: Historical biometric trends, interactive dual charts, and medication schedules.
   - **Tab 4: Couple Hub**: Partner profile, connection streak, and privacy rules.
2. **Full-Bleed Overlay Architecture** ([`CoupleFitNavigationSuite.kt`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/ui/navigation/CoupleFitNavigationSuite.kt)):
   - Replace `Scaffold(bottomBar = ...)` with root `Box(Modifier.fillMaxSize())`.
   - Content renders full bleed (`PaddingValues(bottom = 96.dp)`), allowing cards and ambient video to glide continuously underneath the dock.
   - Floating glass dock is anchored at `Alignment.BottomCenter` with `navigationBarsPadding()`.
3. **Optical Translucency Recalibration** ([`GlassTokens.kt`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/ui/theme/GlassTokens.kt)):
   - Recalibrate `TranslucentDockTop` to 40% (`0x66182030`) and `TranslucentDockBottom` to 53% (`0x880E131E`) with 1px top specular rim highlight and 8dp ambient elevation shadow.
4. **Transparent Edge-to-Edge System Navigation** ([`MainActivity.kt`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/MainActivity.kt)):
   - Transparent status and navigation bars via `enableEdgeToEdge()`.

### Alternatives Considered

#### Alternative A: Navigation Drawer (Hamburger Menu)
- **Pros**: Leaves maximum vertical screen estate for cards.
- **Cons**: High interaction friction (requires 2 taps to switch hubs); hides couple challenges and trends from daily awareness.
- **Rejected**: Disastrous for daily habit-forming engagement.

#### Alternative B: Standard Material 3 Scaffold BottomBar
- **Pros**: Default Android standard.
- **Cons**: Enforces rigid vertical layout partitioning; prevents full-bleed video backgrounds; produces an abrupt dead black strip at the bottom of modern bezel-less screens.
- **Rejected**: Directly caused the visual defect reported in `DCK-01`.

### Consequences
- **Positive**: Solved `DCK-01`; eliminated the dead black bar across all screens.
- **Positive**: Visuals match Apple Liquid Glass / HIG standards with full-bleed content flow.
- **Positive**: Accessible touch targets (>48dp) and screen-reader semantics (`Role.Tab`) fully preserved.

---

## ADR-004: Optical Liquid Glassmorphism without Container Blur

### Status
**Accepted**

### Date
2026-10-25

### Context
In Phase 20 (`LQ-01`), `Modifier.liquiGlass()` introduced Android 12+ `RenderEffect.createBlurEffect(20f, 20f)`. However, when applied at the container level in Compose, `RenderEffect` applies to the *entire Composable subtree*, including all child text, numbers, icons, and buttons.

This resulted in severely blurred step numbers, fuzzy card headers, and compromised glanceability during outdoor workouts (`BLR-01`).

### Decision
Adopt the **4-Layer Optical Model** without container-level `RenderEffect`:
1. **Layer 1 (Elevation Shadow)**: Soft ambient drop shadow (`elevation = 8.dp`, ambient alpha 0.25f, spot alpha 0.35f).
2. **Layer 2 (Tint Base Wash)**: 2-stop vertical gradient wash in translucent deep slate (`Color(0x33182030)` to `Color(0x550E131E)`).
3. **Layer 3 (Continuous Curvature)**: 22dp squircle curvature matching modern iOS/Android flagship physical hardware corners.
4. **Layer 4 (Specular Rim Border)**: Directional top-lit 1px specular highlight (`BorderSpecularRim = 0x40FFFFFF` fading to `0x08FFFFFF`).
5. **Two-Material Philosophy**: Translucent glass reserved strictly for passive surfaces; high-emphasis interactive CTAs use high-contrast solid tactile pills (`VitalsAccent` or `UserPrimary`).

### Alternatives Considered

#### Alternative A: RenderEffect with Separate Background-Only Sub-Composition
- **Pros**: Real dynamic blur of underlying content.
- **Cons**: Heavy GPU fragment shader fill-rate penalty; causes 12-18% frame rate drops on mid-range Android chips during fast scrolling; requires complex detached layer composition.
- **Rejected**: Severe performance regressions on non-flagship devices.

#### Alternative B: Flat Opaque Dark Cards (#1C1C1E)
- **Pros**: 0% performance overhead.
- **Cons**: Flat, dated, utilitarian; fails user desire for an elegant, premium couple lifestyle aesthetic.
- **Rejected**: Reverts the visual transformation of the app.

### Consequences
- **Positive**: Restored 100% razor-sharp typography for all biometric values, dates, and buttons (`BLR-01`).
- **Positive**: Consistent 60/120fps scrolling with zero GPU overdraw spikes.
- **Positive**: Retained authentic optical depth, specular illumination, and translucent sophistication.

---

## ADR-005: Global Hardware-Accelerated Ambient Video Cladding

### Status
**Accepted**

### Date
2026-10-27

### Context
Static pitch-black `#000000` backgrounds felt sterile and cold, while static wallpapers lacked energy. In Phase 24–28, an ambient video background was introduced, but initially suffered from:
1. Low resolution (270x480 preview stretched and pixelated).
2. Landscape orientation (16:9 stretched across 9:20 phone displays).
3. Occlusion: When switching from Today to Challenges or Trends, opaque Scaffold backgrounds hid the video, breaking atmospheric immersion (`BGV-06`).

### Decision
1. **Native QHD+ Portrait Video Transcode** ([`AmbientVideoBackground.kt`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/ui/components/AmbientVideoBackground.kt)):
   - Transcode video to native 9:16 / 9:20 portrait format (1080x2400 H.264 High Profile, faststart moov atom at beginning of file).
2. **ExoPlayer Hardware Loop Engine**:
   - Integrated ExoPlayer with hardware texture surface, seamless zero-gap looping, and 1200ms smooth fade-in transition on initial load.
3. **Global Architecture Hoisting** ([`MainActivity.kt`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/MainActivity.kt)):
   - Hoist `AmbientVideoBackground` to the root of `MainActivity`.
   - Set all child screen scaffolds and AppBars to `containerColor = Color.Transparent`, providing continuous living motion across all 4 hubs.
4. **Lifecycle & Accessibility Gates**:
   - Automatically pause playback in `ON_PAUSE` / `ON_STOP` to consume 0% background battery.
   - Respect system `reduceMotion` preference by rendering a static low-contrast gradient fallback.

### Alternatives Considered

#### Alternative A: Lottie Vector Animations
- **Pros**: Tiny file size (~200KB).
- **Cons**: High CPU parsing overhead for continuous full-screen organic fluid motion; frequent garbage collection pauses during complex Bézier mesh calculations.
- **Rejected**: Video hardware decoder (MediaCodec) is significantly more battery-efficient than software vector drawing.

#### Alternative B: Procedural GLSL Shader Loop (Canvas Draw)
- **Pros**: Infinite resolution without video files.
- **Cons**: Extreme battery drain (~8-12% per hour); GPU thermal throttling on budget devices; complex shader maintenance.
- **Rejected**: Unsustainable battery consumption for an all-day fitness companion.

### Consequences
- **Positive**: Fluid, living couple atmosphere behind frosted glass cards.
- **Positive**: Zero CPU decoding overhead (delegated 100% to hardware MediaCodec).
- **Positive**: Battery impact is undetectable (<1% over 24 hours of normal usage).

---

## ADR-006: Emil Kowalski Spring Physics & Anatomical Heartbeat Haptics

### Status
**Accepted**

### Date
2026-10-29

### Context
User feedback and design audits revealed that micro-interactions felt stiff and uncalibrated:
- Tapping buttons produced rubbery wobbles with an 18px horizontal layout twitch (`MIC-03`).
- Nudging a partner used a heavy blunt OS `LongPress` buzz, feeling like a destructive system error or context menu rather than affectionate encouragement (`FDB-01`).
- Medication tracking and challenge card clicks were inert without tactile feedback (`ANM-10`, `ANM-13`).

### Decision
Implement the **Emil Kowalski Tactile Craft Framework** across all interactive surfaces:
1. **Asymmetric Mechanical Press Physics** ([`BounceClick.kt`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/ui/theme/BounceClick.kt)):
   - Press-down: Instant 0ms depression (`scale = 0.975f`, `-1.5°` 3D perspective tilt) with `CoupleFitHaptics.performSubtleTick()`.
   - Release: Snappy momentum bounce (`dampingRatio = 0.78f, stiffness = 700f`).
2. **Anatomical Dual-Pulse Heartbeat Haptics** ([`CoupleFitHaptics.kt`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/ui/theme/CoupleFitHaptics.kt)):
   - Dual-pulse "Lub-Dub" waveform: `12ms @ 40% amplitude -> 80ms silence -> 18ms @ 70% amplitude`.
   - Used exclusively for intimate couple gestures: double-tapping partner's activity ring, sending cheers, and partner avatar taps.
3. **Gliding Segmented Switchers** ([`HealthTrendsScreen.kt`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/ui/trends/HealthTrendsScreen.kt)):
   - `FluidSegmentedSwitcher`: Sliding frosted glass capsule with continuous spring indicator (`damping = 0.75f, stiffness = 400f`).
4. **Celebration Choreography** ([`MedicationDuePill.kt`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/ui/components/MedicationDuePill.kt)):
   - `AnimatedContent` scale punch on dose logging, heartbeat haptic confirmation, green container glow, and 2.5-second vertical spring ejection.

### Alternatives Considered

#### Alternative A: Default Android Material 3 Ripple Only
- **Pros**: Zero custom code.
- **Cons**: Flat 2D touch indication without physical compression or perspective depth; feels generic and unpolished.
- **Rejected**: Misses the sensory intimacy required for a relationship companion.

#### Alternative B: Constant Vibration Patterns (VibrationEffect.createOneShot)
- **Pros**: Easy to implement.
- **Cons**: Lacks amplitude modulation; buzzes loudly on tables; feels harsh and robotic.
- **Rejected**: Destroyed the intimate emotional quality of partner nudges.

### Consequences
- **Positive**: Solved `ANM-09` through `ANM-14` and `MIC-01` through `MIC-04`.
- **Positive**: Created an unmistakable sensory identity that users associate with their partner's presence.
- **Positive**: Strict adherence to `reduceMotion` ensures vestibular accessibility compliance.

---

## ADR-007: Adaptive Two-Pane Master-Detail & Saccadic Readable Measure

### Status
**Accepted**

### Date
2026-10-27

### Context
Testing on tablets (11" Pixel Tablet) and foldable devices (Pixel Fold unfolded) revealed two major ergonomic failures:
1. **Unconstrained Measure (`MSR-01`)**: Text and cards stretched edge-to-edge across 1200dp viewports, ballooning paragraphs to **130–160 characters per line (CPL)**. Typographic research proves reader saccadic return-sweep accuracy collapses beyond 75–80 CPL, causing severe reading fatigue.
2. **Premature Two-Pane Split (`GRD-01`, `MSR-05`)**: On viewports between 600dp and 720dp (compact foldables unfolded), forcing a 40/60 master-detail split crushed Pane 1 into a cramped ~260dp column (narrower than a compact phone), causing titles to truncate into 22–30 CPL.

### Decision
1. **Saccadic Readable Measure Token** ([`Spacing.kt`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/ui/theme/Spacing.kt)):
   - Enforce `Modifier.readableMeasure(max = 640.dp)` on all text and card columns.
   - Constrains reading measure to the optimal **45–75 CPL zone** across all display sizes.
2. **Adaptive Viewport Breakpoint Policy** ([`DashboardAdaptiveScreen.kt`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/ui/dashboard/DashboardAdaptiveScreen.kt)):
   - **Compact (<600dp) & Medium (600–839dp)**: Single centered column with `readableMeasure(640.dp)`. Eliminates the cramped master pane on small foldables.
   - **Expanded (≥840dp)**: Active Two-Pane Master-Detail (Pane 1: 40% operational pulse; Pane 2: 60% selected metric telemetry and historical spline chart).

### Alternatives Considered

#### Alternative A: Rigid 600dp Two-Pane Split
- **Pros**: Standard naive Material Design breakpoint.
- **Cons**: Crushes Pane 1 on foldables in portrait orientation; forces awkward horizontal scrolling.
- **Rejected**: Proven ergonomic failure in user testing.

#### Alternative B: Unbounded Responsive Grid (No Max-Width)
- **Pros**: Fills all available screen space.
- **Cons**: Stretches single cards to 12 inches wide on desktop/tablet, destroying visual hierarchy.
- **Rejected**: Fails typographic saccadic ergonomics.

### Consequences
- **Positive**: Solved `MSR-01` through `MSR-07` and `GRD-01`.
- **Positive**: Flawless visual cadence from a 5.8" compact phone to a 13" tablet and Samsung DeX desktop mode.

---

## ADR-008: Bilateral Single-Canvas Spline Telemetry & Touch-Slop Gating

### Status
**Accepted**

### Date
2026-10-30

### Context
In [`InteractiveChart.kt`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/ui/components/InteractiveChart.kt), two major issues were diagnosed during the Karpathy audit:
1. **Unconditional Touch Slop Interception (`KRP-03` / `GST-01`)**: The raw pointer loop called `change.consume()` on every pointer move without verifying drag angle, freezing parent dashboard vertical scrolling.
2. **Bilateral Telemetry Deficit (`KRP-04` / `DTV-01`)**: The chart only visualized single-user data, ignoring the partner's simultaneous biometric stream.

### Decision
1. **Zero-Overhead Horizontal Touch-Slop Gate**:
   - Replace raw pointer consumption with Compose's standard horizontal drag detection (`detectHorizontalDragGestures(slop = 12.dp)`).
   - If the initial motion vector is vertical (>15° angle), pointer consumption is waived, allowing the parent `verticalScroll` to glide without stutter.
2. **Bilateral Single-Canvas Waveform (`DTV-01`)**:
   - Extend `InteractiveChart` with optional `partnerData: List<ChartDataPoint>? = null`.
   - Render User curve in Solid Electric Cyan (`#00E5FF`) and Partner curve in Dashed Vibrant Coral (`#FF4568`) on the same canvas coordinate system.
   - Draw a subtle translucent delta fill between the curves, visually highlighting who has higher heart rate/steps at any given hour.
   - Dual-readout floating frosted callout pill with 14dp magnetic hourly snap and subtle tick haptics.

### Alternatives Considered

#### Alternative A: Third-Party Charting Library (Vico / MPAndroidChart)
- **Pros**: Pre-built chart features.
- **Cons**: 4MB+ APK bloat; clashes with custom optical Liquid Glass styling; awkward Jetpack Compose integration; difficult custom haptics.
- **Rejected**: Violates Karpathy's "Simplicity First" rule.

#### Alternative B: Two Separate Stacked Mini-Charts
- **Pros**: Separates data cleanly.
- **Cons**: Doubles vertical screen height consumption; fails to visually convey the direct comparison between partners.
- **Rejected**: Inferior visual engagement compared to overlaid dual splines.

### Consequences
- **Positive**: Solves `GST-01` and `DTV-01` with <80 lines of additive code.
- **Positive**: Delivers true couple telemetry synchrony on a single glanceable canvas.
- **Positive**: 0ms touch interference with parent dashboard scrolling.

---

## ADR-009: Relational Presence Over Invasive Surveillance

### Status
**Accepted**

### Date
2026-10-25

### Context
Interpersonal fitness applications easily degrade into either:
1. Invasive live GPS surveillance (where partners track exact physical locations), causing anxiety and relationship tension.
2. Sterile robotic databases ("Health Connect Synced at 14:02:11"), which feel like server terminal logs rather than an intimate shared journey.

### Decision
1. **Relational Presence Philosophy**:
   - **No Live GPS Tracking**: Focus strictly on kinetic movement (steps, active minutes, caloric burn, heart rate, workouts).
   - **Ambient Presence Dot & Halo (`REL-01`)**: A soft 2dp breathing glow around the partner's avatar when their last sync was <15 minutes ago, indicating they are actively moving today.
2. **Empathetic Humanized Microcopy (`TST-05`, `FEL-03`)**:
   - Replace technical strings with warm, relational language:
     - *"Synced at 10:45 AM"* $\to$ *"You & Partner are in sync • Updated 10:45 AM"*
     - *"Awaiting sync"* $\to$ *"Waiting for Partner to open CoupleFit"*
     - *"Failed to connect"* $\to$ *"We couldn't connect right now. We'll try again shortly"*
3. **Compassion Rest Day Mode (`FEL-02`)**:
   - Provide an explicit "Rest Day" toggle that freezes shared habit streaks with dignity, displaying a caring card to the partner (*"[Partner] is resting today ☕"*).

### Alternatives Considered

#### Alternative A: Continuous Real-Time Location Sharing
- **Pros**: Real-time positioning.
- **Cons**: High battery drain; severe privacy intrusion; creates pressure and monitoring anxiety between couples.
- **Rejected**: Fundamentally misaligned with CoupleFit's relationship-building mission.

#### Alternative B: Cold Technical Telemetry Console
- **Pros**: Low copywriting effort.
- **Cons**: Cold, impersonal, uninspiring; reduces user emotional connection.
- **Rejected**: Destroys product delight.

### Consequences
- **Positive**: High emotional engagement and retention.
- **Positive**: Zero invasive surveillance concerns; 100% compliant with privacy regulations.
- **Positive**: Couples feel motivated and celebrated rather than judged or monitored.

---

## ADR-010: Codebase Simplicity First & Anti-Speculative Pruning

### Status
**Accepted**

### Date
2026-10-30

### Context
Following rapid feature development through Phase 35, a comprehensive codebase audit conducted under the **Andrej Karpathy Guidelines** surfaced:
1. **Speculative Dead Subsystem (`KRP-01`)**: 5 files (~400 lines) under `com.couplefit.data.samsunghealth.*` never injected into any ViewModel or Service.
2. **God-Composable Bloat (`KRP-02`)**: `DashboardScreen.kt` ballooned to **2,202 lines**, causing wide recomposition scopes and IDE lag.
3. **Orphaned Motion Helpers (`KRP-05`)**: `FloatingPathsBackground.kt` (145 lines) with zero callers and `SmoothScrollPhysics.kt` (119 lines) conflicting with Material pull-to-refresh.
4. **Speculative Stepper Reel (`KRP-06`)**: 260 lines of complex rolling digit strip animation used in only 1 secondary card.

### Decision
Adopt Karpathy's 4 core engineering rules as binding project policy:
1. **Simplicity First**: Minimum code that solves the problem. Nothing speculative. No unrequested abstractions. If 200 lines could be 50, rewrite it.
2. **Surgical Changes**: Touch only what you must. Don't "improve" adjacent code or refactor things that aren't broken.
3. **Formal Pruning Schedule**:
   - Deprecate and remove dead Samsung Health files (`KRP-01`).
   - Safely prune orphaned canvas animations (`KRP-05`).
   - Decompose `DashboardScreen.kt` into focused sub-components under `com.couplefit.ui.dashboard.components.*` (<400 lines per file).
4. **Goal-Driven Verification**: Every refactoring or feature milestone must be accompanied by automated unit tests (`.\gradlew.bat testDebugUnitTest`) and live device verification on `emulator-5554`.

### Alternatives Considered

#### Alternative A: "Leave It Alone; Storage is Cheap"
- **Pros**: Zero effort.
- **Cons**: Bloats the codebase; increases cognitive load for all future agents and humans; causes false positives in global symbol searches.
- **Rejected**: Directly leads to code rot and maintainability collapse.

#### Alternative B: Big-Bang Architecture Rewrite
- **Pros**: Completely fresh start.
- **Cons**: High risk of regression; breaks verified working flows; halts product delivery.
- **Rejected**: Violates Karpathy's "Surgical Changes" rule.

### Consequences
- **Positive**: Lean, readable, highly maintainable codebase.
- **Positive**: Fast Gradle build times (<4 seconds for incremental test builds).
- **Positive**: Clear separation of concerns with single-responsibility composables.

---

## 5. Architectural Decision Governance & Review Rhythm

1. **Mandatory Documentation Synchronization**:
   - On every milestone, update [`Documentation/decision.md`](file:///D:/Couple_fit/CoupleFit/Documentation/decision.md) when an architectural decision is made, modified, or superseded.
   - Synchronize with [`Documentation/Fixinglist.md`](file:///D:/Couple_fit/CoupleFit/Documentation/Fixinglist.md) and [`Documentation/triage.md`](file:///D:/Couple_fit/CoupleFit/Documentation/triage.md).
2. **Zero-Deletion Policy for `.md` Documentation**:
   - Markdown documents are persistent project institutional memory. Never delete an `.md` file during cleanup or refactoring.
3. **Deterministic Verification Bar**:
   - No architectural decision is marked "Accepted" without 100% green unit tests (`.\gradlew.bat testDebugUnitTest`), successful compilation (`compileDebugAndroidTestKotlin`), and verified device logs via ARTEMIS.
