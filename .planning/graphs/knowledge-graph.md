# CoupleFit Local Knowledge Graph 🕸️

> Generated via `/gsd-graphify` lifecycle mandate.

```mermaid
graph TD
    classDef arch fill:#1E293B,stroke:#38BDF8,stroke-width:2px,color:#fff;
    classDef ui fill:#0F172A,stroke:#10B981,stroke-width:2px,color:#fff;
    classDef data fill:#1E1E38,stroke:#F59E0B,stroke-width:2px,color:#fff;
    classDef doc fill:#2D1537,stroke:#EC4899,stroke-width:2px,color:#fff;

    subgraph Documentation["Documentation & Research Layer"]
        IA["INFORMATION_ARCHITECTURE.md"]:::doc
        CS["card-sort-analysis.md"]:::doc
        FL["Fixinglist.md"]:::doc
        TRI["triage.md"]:::doc
        P5P["PLAN_PHASE_05_IA_SETTINGS.md"]:::doc
        P6P["PLAN_PHASE_06_NAVIGATION_POLISH.md"]:::doc
        RN23["RELEASE_NOTES_v2.3.0.md"]:::doc
        RN24["RELEASE_NOTES_v2.4.0.md"]:::doc
        RN25["RELEASE_NOTES_v2.5.0.md"]:::doc
        RN26["RELEASE_NOTES_v2.6.0.md"]:::doc
        RN27["RELEASE_NOTES_v2.7.0.md"]:::doc
        RN28["RELEASE_NOTES_v2.8.0.md"]:::doc
        RN29["RELEASE_NOTES_v2.9.0.md"]:::doc
        RN291["RELEASE_NOTES_v2.9.1.md"]:::doc
        RN292["RELEASE_NOTES_v2.9.2.md"]:::doc
        RN293["RELEASE_NOTES_v2.9.3.md"]:::doc
        P16P["PLAN_PHASE_16_GLASSMORPHISM_REDESIGN_AND_LAYOUT_POLISH.md"]:::doc
        P17P["PLAN_PHASE_17_FRONTEND_TASTE_AND_HUMAN_PRESENCE.md"]:::doc
        P18P["PLAN_PHASE_18_LIVE_HEART_RATE_DASHBOARD_SURFACE.md"]:::doc
        P14P["PLAN_PHASE_14_DESIGN_ENGINEERING_ENG01_TO_ENG04.md"]:::doc
        P15P["PLAN_PHASE_15_GLASSMORPHISM_GLS01_TO_GLS06.md"]:::doc
        ENGD["emil_design_eng_eng01_to_eng04_analysis.md"]:::doc
        GLSD["design_system_glassmorphism_gls01_gls02_analysis.md"]:::doc
        P9P["PLAN_PHASE_09_FRONTEND_DESIGN_DSN01_TO_DSN03.md"]:::doc
        FDD["frontend_design_dsn01_dsn02_dsn03_analysis.md"]:::doc
        P8P["PLAN_PHASE_08_WIREFRAME_WF01_TO_WF07.md"]:::doc
        P7P["PLAN_PHASE_07_USER_FLOWS_FLOW01_02_03.md"]:::doc
        UFD["user_flow_diagram_flow01_flow02_flow03_analysis.md"]:::doc
        UFR["user_flow_innovations_resolution.md"]:::doc
        WFS["wireframe_spec_wf01_wf02_wf03_analysis.md"]:::doc
        NAVAUD["navigation_patterns_appwide_audit.md"]:::doc
        AG["AGENTS.md"]:::doc
    end

    subgraph NavigationLayer["Navigation & Routing Layer"]
        CFD["CoupleFitDestinations.kt<br/>(Today, Challenges, Trends, Hub)"]:::arch
        CFNS["CoupleFitNavigationSuite.kt<br/>(Dual Vectors, Bold Weight, Haptics)"]:::arch
        FND["FloatingGlassNavBar<br/>(16dp Elevation, 32dp Pill, 28dp Blur)"]:::arch
        MA["MainActivity.kt<br/>(Scaffold Host, BackHandler Re-Anchoring)"]:::arch
        MANI["AndroidManifest.xml<br/>(enableOnBackInvokedCallback)"]:::arch
    end

    subgraph UILayer["UI Screens & Composables"]
        DAS["DashboardAdaptiveScreen.kt<br/>(Adaptive Multi-Pane & Telemetry)"]:::ui
        DS["DashboardScreen.kt<br/>(Operational Epicenter)"]:::ui
        MDP["MedicationDuePill.kt<br/>(Contextual Due Pill)"]:::ui
        QNR["QuickNudgeRow.kt<br/>(Thumb-Zone Partner Cheers)"]:::ui
        ACPC["ActiveChallengePreviewCard.kt<br/>(Modular Challenge Preview)"]:::ui
        CSCR["ChallengesScreen.kt<br/>(Duels & Trophies)"]:::ui
        HTS["HealthTrendsScreen.kt<br/>(Historical Trends & Meds)"]:::ui
        CHS["CoupleHubScreen.kt<br/>(Partner Profile & Privacy)"]:::ui
        SET["SettingsScreen.kt<br/>(4-Hub Settings Architecture)"]:::ui
        HUB1["PartnershipHubContent<br/>(Profile, Partner, Unlink Modal)"]:::ui
        HUB2["HealthSyncHubContent<br/>(Health Connect & Metric Sharing)"]:::ui
        HUB3["ExperienceHubContent<br/>(Dark Mode, Motion, Units)"]:::ui
        HUB4["PrivacyHubContent<br/>(Biometrics, Transparency, Export)"]:::ui
        SBAC["SideBySideActivityCard<br/>(Double-Tap Ring Cheer, Status Pill)"]:::ui
        BCK["BounceClick.kt<br/>(Asymmetric Spring 1000f/750f, Scale 0.975x)"]:::ui
        AR["ActivityRing.kt<br/>(Fluid Spring 120f, Luminescent Track Glow)"]:::ui
        CFH["CoupleFitHaptics.kt<br/>(Heartbeat Tick 12ms/80ms/18ms)"]:::arch
        GLST["GlassTokens.kt<br/>(Primitive -> Semantic -> Component)"]:::arch
        MODG["Modifier.iosGlass()<br/>(20dp Blur, 8% White Tint, Specular Rim)"]:::ui
        ATM["AuroraTelemetryMesh<br/>(Dual Radial Cyan/Coral Mesh GLS-03)"]:::ui
        ARBP["0-Step Breathing Ring Pulse<br/>(VIS-05 / TST-03)"]:::ui
        TDD["TelemetryDiagnosticDialog<br/>(Room SSOT Cache, Ping Partner)"]:::ui
    end

    subgraph WidgetLayer["Widget & Quick Action Layer"]
        CFAW["CoupleFitAppWidget.kt<br/>(Bilateral Mini-Gauges, Poke Action)"]:::arch
        PAC["PokeActionCallback.kt<br/>(Zero-Cold-Start Nudge Invocation)"]:::arch
        WEP["WidgetEntryPoint.kt<br/>(PartnerRepository DI)"]:::arch
    end

    subgraph DataLayer["Data & Repositories"]
        HD["HealthDao.kt / HealthDatabase.kt"]:::data
        HE["HealthEntities.kt (CoupleChallengeEntity)"]:::data
        CVM["ChallengeViewModel.kt"]:::data
        AVM["AuthViewModel.kt"]:::data
        HCVM["HealthConnectViewModel.kt<br/>(Metric Sharing Flow)"]:::data
        DSSTORE["CoupleFitPreferencesDataStore.kt"]:::data
    end

    subgraph TestLayer["Instrumented Verification Layer"]
        SHCUT["SettingsHubComposeUiTest.kt<br/>(IA-10: 5 Hub Tests)"]:::arch
        CSCCUT["CardSortConvergenceComposeUiTest.kt"]:::arch
        NCUT["NavigationComposeUiTest.kt"]:::arch
    end

    %% Relationships
    CS -->|Validates 4-Tab Mental Model| CFD
    IA -->|Defines 4-Hub Architecture| SET
    FL -->|Catalogs Defect IA-03, IA-04, IA-10| SET
    
    CFD -->|Enums & Routes| CFNS
    CFNS -->|Hosts Tabs| MA
    MA -->|Tab 1: Today| DAS
    MA -->|Tab 2: Challenges| CSCR
    MA -->|Tab 3: Trends| HTS
    MA -->|Tab 4: Couple Hub| CHS
    MA -->|Settings Route| SET

    DAS --> DS
    DS --> MDP
    DS --> QNR
    DS --> ACPC
    ACPC -->|CTA: View Duels| CSCR
    CSCR --> CVM
    CSCR --> HE
    BCK -->|Calibrated Touch Press| DS
    CFH -->|Heartbeat Nudge Haptic| QNR
    CFH -->|Milestone Cheer Haptic| SBAC
    AR -->|Luminescent Track & Fluid Spring| DS
    GLST -->|Tokens| MODG
    MODG -->|Translucency & Specular Rim| DS
    MODG -->|Translucency & Specular Rim| HTS
    FND -->|Floating Dock Shell| CFNS
    CHS -->|Privacy Shortcut [🔒]| SET

    SET --> HUB1
    SET --> HUB2
    SET --> HUB3
    SET --> HUB4
    HUB1 --> AVM
    HUB2 --> HCVM
    HCVM --> DSSTORE
    SHCUT -->|Verifies IA-03 & IA-10| SET
    
    WFS -->|Specifies Fold & Triad| DS
    WFS -->|Specifies Triad & Perspectives| HTS
    WFS -->|Specifies Thumb Bottom Sheet| CSCR
    P8P -->|Specifies Fold & Triad Plan| WFS
    RN26 -->|Documents Release v2.6.0| P8P
    P9P -->|Specifies Identity & Celebration| FDD
    RN27 -->|Documents Release v2.7.0| P9P
    FDD -->|Bilateral Progress| HTS
    FDD -->|Confetti & Rim Lighting| DS
    P16P -->|Dual-Layer Frosted Glass & Layout Resilience| DS
    P16P -->|96dp Scroll Clearance & Specular Rims| HTS
    RN291 -->|Documents Release v2.9.1| P16P
    P17P -->|Emancipated Glyphs & Warm Presence| DS
    P17P -->|Contextual Frosted Quick Nudges| QNR
    RN292 -->|Documents Release v2.9.2| P17P
    P18P -->|Permanently Unfolded Live Heart Rate Waveform| DS
    RN293 -->|Documents Release v2.9.3| P18P
```

## Entity Cross-Reference Table

| Symbol / File | Layer | Primary Responsibility | Upstream Dependencies | Downstream Consumers |
| :--- | :--- | :--- | :--- | :--- |
| `wireframe_spec_wf01_wf02_wf03_analysis.md` | Spec / UX | Master wireframe spec, 3-tier fold, universal triad, modal bottom sheets | `/designer-skills:wireframe-spec` | `DashboardScreen.kt`, `ChallengesScreen.kt`, `HealthTrendsScreen.kt` |
| `PLAN_PHASE_08_WIREFRAME_WF01_TO_WF07.md` | Plan / Roadmap | Milestone plan for wireframe ergonomics, fold integrity, triad, sheets, auth resilience | `/gsd-plan-phase` | `DashboardScreen.kt`, `ChallengesScreen.kt`, `HealthTrendsScreen.kt` |
| `RELEASE_NOTES_v2.6.0.md` | Release / Ops | Version 2.6.0 release notes: Fold integrity, triad standardization, M3 bottom sheet | Release Deployment | Production Stakeholders |
| `PLAN_PHASE_09_FRONTEND_DESIGN_DSN01_TO_DSN03.md` | Plan / Roadmap | Milestone plan for frontend design, dual identity tokens, Canvas confetti, tabular numerals | `/gsd-plan-phase` | `DashboardScreen.kt`, `HealthTrendsScreen.kt`, `ConfettiCelebration.kt` |
| `RELEASE_NOTES_v2.7.0.md` | Release / Ops | Version 2.7.0 release notes: Electric Cyan & Vibrant Coral identity tokens, 60fps Canvas confetti, directional rim lighting | Release Deployment | Production Stakeholders |
| `frontend_design_dsn01_dsn02_dsn03_analysis.md` | Spec / UX | Visual identity spec, dual spectrums, 60fps Canvas confetti, Direction C brand definition | `/designer-skills:frontend-design` | `DashboardScreen.kt`, `HealthTrendsScreen.kt`, `Color.kt` |
| `CoupleFitDestination` | Navigation | Type-safe `@Serializable` routes for 4 validated tabs + Settings | `card-sort-analysis.md` | `TopLevelDestination`, `MainActivity.kt` |
| `TopLevelDestination` | Navigation | Metadata enum (routes, icons, labels, a11y descriptions) | `CoupleFitDestinations.kt` | `CoupleFitNavigationSuite.kt`, `MainActivity.kt` |
| `CoupleFitNavigationSuite` | Navigation | Adaptive M3 `NavigationBar` (<600dp) & `NavigationRail` (≥600dp) | `WindowSizeClass`, `TopLevelDestination` | `MainActivity.kt`, `NavigationComposeUiTest.kt` |
| `MainActivity` | App Entry | Activity lifecycle, theme reveal, auth router, adaptive shell host | `CoupleFitNavigationSuite`, `AuthViewModel` | Android OS |
| `ChallengesScreen` | UI (Tab 2) | Head-to-head duels, step races, milestone/trophy vault, sub-tabs | `ChallengeViewModel`, `SideBySideChallengeCard` | `MainActivity.kt` |
| `HealthTrendsScreen` | UI (Tab 3) | Multi-horizon telemetry, connected M3 timeframe & perspective rows | `TelemetryPerspective`, Material 3 | `MainActivity.kt`, `NavigationComposeUiTest.kt` |
| `TelemetryPerspective` | Navigation / Domain | Segmented perspective enum (`COMBINED`, `YOU`, `PARTNER`) | `HealthTrendsScreen.kt` | `HealthTrendsScreen.kt`, `NavigationComposeUiTest.kt` |
| `CoupleHubScreen` | UI (Tab 4) | Relationship anchor, streaks, anniversaries, privacy shortcut | `AppSpacing`, `SettingsScreen` | `MainActivity.kt` |
| `ChallengeSubTab` | Navigation | Segmented sub-navigation enum (`ACTIVE_DUELS`, `TROPHIES`) | `ChallengesScreen.kt` | `ChallengesScreen.kt`, `NavigationComposeUiTest.kt` |
| `CoupleFitAppWidget` | Widget | Jetpack Glance widget with bilateral mini-gauges & inline poke action | `GlanceModifier`, `PokeActionCallback` | Android Home Screen |
| `PokeActionCallback` | Widget / Action | Zero-cold-start background nudge action callback | `PartnerRepository`, `WidgetEntryPoint` | `CoupleFitAppWidget` |
| `SideBySideActivityCard` | UI (Dashboard) | Dual activity motion gauges, double-tap cheer gesture, stale telemetry pill | `MotionArcProgressGauge`, `HealthDashboardUiState` | `DashboardScreen.kt`, `NavigationComposeUiTest.kt` |
| `TelemetryDiagnosticDialog` | UI (Dialog) | Transparent partner sync health, Room SSOT cache info, high-priority ping action | `AlertDialog`, `HealthConnectViewModel` | `SideBySideActivityCard`, `DashboardScreen.kt` |
| `NavigationComposeUiTest` | Test | Instrumented UI tests for multi-modal tabs, sub-headers, sub-tabs, perspective | AndroidX Compose Test Rule | CI / Local Verification |
| `PLAN_PHASE_16_GLASSMORPHISM_REDESIGN_AND_LAYOUT_POLISH.md` | Plan / Roadmap | Milestone plan for dual-layer liquid glass, 28% aurora backlighting, Day-1 decoupling, and dock contrast | `/gsd-plan-phase` | `DashboardScreen.kt`, `GlassTokens.kt`, `HealthTrendsScreen.kt` |
| `RELEASE_NOTES_v2.9.1.md` | Release / Ops | Version 2.9.1 release notes: Dual-layer specular frosted glass, decoupled Day-1 layout, and unselected dock contrast | Release Deployment | Production Stakeholders |
| `PLAN_PHASE_17_FRONTEND_TASTE_AND_HUMAN_PRESENCE.md` | Plan / Roadmap | Milestone plan for emancipated floating glyphs, relational presence, contextual quick-nudges, and tug-of-war balance bar | `/gsd-plan-phase` | `DashboardScreen.kt`, `QuickNudgeRow.kt` |
| `RELEASE_NOTES_v2.9.2.md` | Release / Ops | Version 2.9.2 release notes: Frontend design taste, emancipated glyphs, human presence copy, and dynamic balance bar | Release Deployment | Production Stakeholders |
| `PLAN_PHASE_18_LIVE_HEART_RATE_DASHBOARD_SURFACE.md` | Plan / Roadmap | Milestone plan for permanently unfolded live heart rate telemetry and waveform on dashboard | `/gsd-plan-phase` | `DashboardScreen.kt`, `InteractiveChart.kt` |
| `RELEASE_NOTES_v2.9.3.md` | Release / Ops | Version 2.9.3 release notes: Promoted live heart rate telemetry dashboard, permanent waveform, and Emil Kowalski craft | Release Deployment | Production Stakeholders |

### Phase 19: Live Heart Rate Layout Architecture Polish & Ambient Waveform
- **Node**: `PLAN_PHASE_19_LIVE_HEART_RATE_LAYOUT_POLISH.md`
  - Defines `VHR-05`, `DSN-04`, `ENG-06`.
  - Links: `DashboardScreen.kt`, `InteractiveChart.kt`, `LiveHeartRateEmptyWaveform.kt`, `NavigationComposeUiTest.kt`.
- **Node**: `LiveHeartRateEmptyWaveform.kt`
  - Implements ambient ECG cardiogram rhythm canvas (`ENG-06`).

### Phase 20: Liqui Design Liquid Glass Heart Rate Redesign & Antislop-UI Polish
- **Node**: `PLAN_PHASE_20_LIQUI_DESIGN_HEART_RATE.md`
  - Defines `LQ-01`, `LQ-02`, `AS-01`.
  - Links: `LiquiGlassTokens.kt`, `DashboardScreen.kt`, `LiveHeartRateEmptyWaveform.kt`, `NavigationComposeUiTest.kt`.
- **Node**: `LiquiGlassTokens.kt`
  - Ports official `leefanv/liqui-design` 4-layer optical model, tint wash, and directional specular rim (`LQ-01`).


### Phase 21: Razor-Sharp Optical Liquid Glass Refactor
- **Node**: `PLAN_PHASE_21_CRISP_OPTICAL_GLASS.md`
  - Defines `BLR-01` (container RenderEffect blur elimination).
  - Links: `LiquiGlassTokens.kt`, `DashboardScreen.kt`, `LiveHeartRateEmptyWaveform.kt`.
- **Node**: `LiquiGlassTokens.kt`
  - Eliminates card-wide `graphicsLayer { renderEffect = ... }` container blur.
  - Retains 4-layer optical model (directional shadow, squircle clip, deep tint base + gradient wash, directional specular rim).
  - Preserves 100% native raster clarity and typography sharpness.


### Phase 22: Dashboard UI Design System Harmonization & Heart Rate Convergence
- **Node**: `PLAN_PHASE_22_UI_HARMONIZATION.md`
  - Defines `DSN-05` (glass material, 16dp geometry, typographic hierarchy harmonization) and `LAY-02` (width margin alignment).
  - Links: `DashboardScreen.kt`, `LiveHeartRateEmptyWaveform.kt`, `ActiveChallengePreviewCard.kt`, `GlassTokens.kt`.
- **Node**: `DashboardScreen.kt`
  - Applies `Modifier.iosGlass(shape = MaterialTheme.shapes.large)` to Heart Rate card.
  - Standardizes category header to `LIVE HEART RATE` with `AnimatedHeartIcon` and 12dp status pill.
- **Node**: `LiveHeartRateEmptyWaveform.kt`
  - Draws ECG cardiogram rhythm and traveling pulse bead directly on frosted slate glass.
  - Replaces solid button with frosted glass pill button matching `QuickNudgeRow`.
- **Node**: `ActiveChallengePreviewCard.kt`
  - Eliminates duplicate horizontal padding, unifying card margins across the dashboard.


### Phase 23: Interactive Heart Rate Graph Restoration
- **Node**: `PLAN_PHASE_23_INTERACTIVE_GRAPH_RESTORED.md`
  - Defines `GRP-01` (deterministic diurnal cardiovascular telemetry curve fallback).
  - Links: `DashboardScreen.kt`, `InteractiveChart.kt`, `RELEASE_NOTES_v2.9.8.md`.
- **Node**: `DashboardScreen.kt`
  - Restores `InteractiveChart` on Tab 0 (Today / Dashboard) and Tab 1 (My Stats).
  - Employs deterministic diurnal curve across 06:00, 09:00, 12:00, 15:00, 18:00, 21:00 when wearable data has fewer than 2 points.
  - Expands stage height to 145dp, preserving reference gridlines, time axis, luminous gradient fill, and touch scrubbing.
- **Node**: `RELEASE_NOTES_v2.9.8.md`
  - Documents release v2.9.8, verified on live Android emulator (`emulator-5554`) via Google ARTEMIS.


### Phase 24: Ambient Liquid Video Background Integration
- **Node**: `PLAN_PHASE_24_AMBIENT_VIDEO_BACKGROUND.md`
  - Defines `BGV-01` (atmospheric motion video background behind Jetpack Compose hierarchy).
  - Links: `AmbientVideoBackground.kt`, `CoupleFitNavigationSuite.kt`, `MainActivity.kt`, `R.raw.green`, `RELEASE_NOTES_v2.9.9.md`.
- **Node**: `AmbientVideoBackground.kt`
  - Encapsulates native Android `TextureView` via `AndroidView` for Compose layer blending under `Modifier.iosGlass()` without hole punching.
  - Implements dynamic 2D center-crop matrix `(viewWidth/2, viewHeight/2)` for distortion-free aspect filling.
  - Lifecycle-aware auto-pause/resume (`ON_PAUSE` / `ON_RESUME`) for zero battery drain.
  - Respects user accessibility `reduceMotion` preferences.
- **Node**: `CoupleFitNavigationSuite.kt`
  - Integrates `AmbientVideoBackground` as the foundational background layer beneath navigation scaffold and all screens.
- **Node**: `RELEASE_NOTES_v2.9.9.md`
  - Documents release v2.9.9, verified on live Android emulator (`emulator-5554`) via Google ARTEMIS.


### Phase 25: Full Resolution Portrait Ambient Video Integration
- **Node**: `PLAN_PHASE_25_FULL_RESOLUTION_PORTRAIT_VIDEO.md`
  - Defines `BGV-02` (upgrading ambient video pipeline from 270x480 preview to 1440x2560 QHD+ 9:16 portrait).
  - Links: `AmbientVideoBackground.kt`, `CoupleFitNavigationSuite.kt`, `R.raw.green`, `RELEASE_NOTES_v2.9.10.md`.
- **Node**: `R.raw.green`
  - Upgraded video asset to 1440×2560 QHD+ Portrait (3.26MB, H.264 High Profile, Lanczos interpolation, faststart moov atom).
- **Node**: `RELEASE_NOTES_v2.9.10.md`
  - Documents release v2.9.10, verified on live Android emulator (`emulator-5554`) via Google ARTEMIS.


### Phase 26: Ambient Video Background Switch to `green1.mp4`
- **Node**: `PLAN_PHASE_26_AMBIENT_VIDEO_GREEN1.md`
  - Defines `BGV-03` (switching ambient background video to native 9:20 portrait `green1.mp4`).
  - Links: `AmbientVideoBackground.kt`, `CoupleFitNavigationSuite.kt`, `R.raw.green1`, `RELEASE_NOTES_v2.9.11.md`.
- **Node**: `R.raw.green1`
  - Native 9:20 portrait video transcoded to 1080x2400 via Lanczos interpolation (7.48MB, 30fps, H.264 High Profile, faststart moov atom).
- **Node**: `AmbientVideoBackground.kt`
  - Updated default video resource parameter `@RawRes videoResId: Int = R.raw.green1`.
  - Preserves TextureView blending, dynamic center-crop scaling matrix, lifecycle pause/resume observer, and `reduceMotion` accessibility toggle.
- **Node**: `RELEASE_NOTES_v2.9.11.md`
  - Documents release v2.9.11, verified on live Android emulator (`emulator-5554`) via Google ARTEMIS.



### Phase 27: Dynamic Ambient Video Background Shuffle & Extensible Registry
- **Node**: `PLAN_PHASE_27_AMBIENT_VIDEO_SHUFFLE.md`
  - Defines `BGV-04` (dynamic ambient video background shuffle across app opens and extensible registry architecture).
  - Links: `AmbientVideoRegistry.kt`, `AmbientVideoBackground.kt`, `CoupleFitNavigationSuite.kt`, `MainActivity.kt`, `R.raw.blue`, `R.raw.green1`, `AmbientVideoRegistryTest.kt`, `RELEASE_NOTES_v2.9.12.md`.
- **Node**: `AmbientVideoRegistry.kt`
  - Extensible registry decoupling video assets from UI composables.
  - Active shuffle pool `[green1, blue]` with one-line extensibility for future assets.
  - Non-repeating random selection algorithm ensuring variety on consecutive launches.
- **Node**: `R.raw.blue`
  - Transcoded 1080x2400 portrait stream with 90° clockwise rotation and Lanczos interpolation (8.15MB, 30fps, faststart moov atom).
- **Node**: `MainActivity.kt`
  - SharedPreferences-backed cold-start shuffle with session-stable `rememberSaveable`.
- **Node**: `RELEASE_NOTES_v2.9.12.md`
  - Documents release v2.9.12, verified on live Android emulator (`emulator-5554`) via Google ARTEMIS.


### Phase 28: Natural Ambient Video Background Fade-In Transition
- **Node**: `PLAN_PHASE_28_AMBIENT_VIDEO_FADE_IN.md`
  - Defines `BGV-05` (natural ambient video background fade-in transition eliminating cold-start visual snap).
  - Links: `AmbientVideoBackground.kt`, `RELEASE_NOTES_v2.9.13.md`.
- **Node**: `AmbientVideoBackground.kt`
  - Decoder first-frame readiness detection via `MEDIA_INFO_VIDEO_RENDERING_START`.
  - 1200ms `FastOutSlowInEasing` alpha fade-in bloom over `#07080A` obsidian canvas.
  - Hardware-accelerated `Modifier.graphicsLayer { alpha = videoAlpha }`.
  - Respects `reduceMotion` accessibility toggle.
- **Node**: `RELEASE_NOTES_v2.9.13.md`
  - Documents release v2.9.13, verified on live Android emulator (`emulator-5554`) via Google ARTEMIS.


### Phase 29: Apple Design & Liqui Design Comprehensive UI & Animation Redesign
- **Node**: `PLAN_PHASE_29_APPLE_LIQUI_REDESIGN.md`
  - Defines `DSN-06` (Liqui Design 4-layer optical liquid glass, squircle geometry, and Two-Material tactile control architecture) and `ANM-04` (Apple WWDC fluid interface spring dynamics and tactile latency elimination).
  - Links: `LiquiGlassTokens.kt`, `GlassTokens.kt`, `BounceClick.kt`, `QuickNudgeRow.kt`, `ActivityRing.kt`, `DashboardScreen.kt`, `CoupleFitNavigationSuite.kt`, `RELEASE_NOTES_v2.9.14.md`.
- **Node**: `LiquiGlassTokens.kt`
  - Ported 4-layer optical model from `https://github.com/leefanv/liqui-design`.
  - Ambient soft drop shadow elevation (`8dp` to `16dp`), superellipse continuous squircle (`22dp`), 2-stop tint legibility wash, directional diamond-cut specular bevel rim (`1.25dp`).
  - Two-Material interactive tactile control modifier `Modifier.liquiTactilePill()` with high contrast (>7:1 ratio).
- **Node**: `GlassTokens.kt`
  - Enhanced `Modifier.iosGlass()` with `8dp` ambient elevation shadow, continuous squircle shape, and diamond-cut specular rim.
  - Enhanced `Modifier.glassDock()` with `16dp` ambient elevation shadow and directional specular rim.
  - Preserves zero child container blur for 100% razor-sharp typography.
- **Node**: `BounceClick.kt`
  - Calibrated `Modifier.iosClickable()` with instant pointer-down depression (`scale = 0.975f`, `alpha = 0.88f`, `damping 1.0f`, `stiffness 1000f`) and Apple critically damped return (`damping 1.0f`, `stiffness 800f`, response 0.35s).
  - Added `Modifier.iosMomentumClickable()` for momentum-carrying playful actions (`damping 0.80f`, `stiffness 700f`, `scale = 0.95f`, `alpha = 0.85f`).
- **Node**: `QuickNudgeRow.kt`
  - Upgraded nudge pills to Two-Material tactile semi-solid material (`liquiTactilePill`) with `iosMomentumClickable` and dual-pulse heartbeat haptics.
- **Node**: `ActivityRing.kt`
  - Fluid physics spring retargeting (`dampingRatio = 0.85f`, `stiffness = 160f`) for unhurried Apple Fitness-grade momentum settling.
- **Node**: `RELEASE_NOTES_v2.9.14.md`
  - Documents release v2.9.14, verified on live Android emulator (`emulator-5554`) via Google ARTEMIS.

### Phase 30: Color-Blind Ergonomics (CLR-03)
- **Node**: `feat/colorblind-ergonomics-clr03`
- **Edges**:
  - `DashboardScreen.kt` -> `MetricPill` (Glyph-enabled)
  - `DashboardAdaptiveScreen.kt` -> `TelemetryMetric` (Glyph-enabled)
  - `ChallengeComponents.kt` -> `SideBySideChallengeCard` (Standing Glyphs)
  - `Fixinglist.md` -> Section 53
- **WCAG Compliance**: WCAG 2.2 AA (1.4.1 Use of Color & 1.4.11 Non-text Contrast)

### Phase 31: Typography Scale Hierarchy (TYP-02)
- **Node**: `feat/typography-scale-typ02`
- **Edges**:
  - `Type.kt` -> `headlineSmall` (21sp, Bold, tnum)
  - `Type.kt` -> `titleLarge` (19sp, SemiBold)
  - `Fixinglist.md` -> Section 54
  - `Fixinglist.md` -> `ANM-05`, `ANM-06`, `TYP-05`

### Newly Cataloged Sensory & Animation Backlog
- **Nodes**:
  - `ANM-05`: Dual Ring Closure Celebration Cascade (`DashboardScreen.kt`)
  - `ANM-06`: Interactive Chart Scrub Spring Kinematics (`InteractiveChart.kt`)
  - `ANM-07`: Flying Nudge Trajectory & Partner Ring Resonance (`QuickNudgeRow.kt`)
  - `ANM-08`: Trophy 3D Specular Reveal & Badge Flip (`ChallengesScreen.kt`)
  - `TYP-05`: Telemetry Micro-Unit Optical Baseline Alignment (`Theme.kt`)
  - `GLS-11`: Hub & Trends 4-Layer Liqui Glass Harmonization (`CoupleHubScreen.kt`, `HealthTrendsScreen.kt`)
  - `REL-01`: Living Partner Presence Breathing Halo (`CoupleHubScreen.kt`)
- **Target Milestones**: Phase 32+ Animation & Relational Polish Waves

### Readable Measure Architecture (MSR-01 to MSR-07)
- **Domain**: Typographic Cadence & Saccadic Ergonomics (`/designer-skills:readable-measure`)
- **Nodes**:
  - `MSR-01`: Tablet Line Sprawl (130–160 CPL) -> `PrivacyTransparencyScreen.kt`, `SettingsScreen.kt`
  - `MSR-02`: Dialog Choked Measure (28–34 CPL) -> `StartChallengeDialog.kt`
  - `MSR-03`: Confirmation Text Sprawl (112 CPL) -> `LinkedConfirmationScreen.kt`
  - `MSR-04`: Diagnostic Foldable Measure (26–32 CPL) -> `DashboardScreen.kt`
  - `MSR-05`: Master Pane Choked Measure (22–30 CPL) -> `DashboardAdaptiveScreen.kt`
  - `MSR-06`: Legal Notice Sprawl (150–180 CPL) -> `SettingsScreen.kt`
  - `MSR-07`: Card Subtitle Sprawl (90–125 CPL) -> `DashboardScreen.kt`
  - `Modifier.readableMeasure()` -> `Spacing.kt` / `Measure.kt`
