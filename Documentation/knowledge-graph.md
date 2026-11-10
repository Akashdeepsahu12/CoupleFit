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
        P39P["PLAN_PHASE_39_LIQUID_GLASS_DOCK_OPTICAL_DECOUPLING.md"]:::doc
        VR39["phase39_verification_report.md"]:::doc
        P41P["PLAN_PHASE_41_BITCHORD_LIQUID_GLASS.md"]:::doc
        VR41["phase41_verification_report.md"]:::doc
        P40P["PLAN_PHASE_40_KARPATHY_NAVIGATION_CLEANUP.md"]:::doc
        VR40["phase40_verification_report.md"]:::doc
        P42P["PLAN_PHASE_42_REFRACTION_LENS_ENGINE.md"]:::doc
        VR42["phase42_verification_report.md"]:::doc
        P43P["PLAN_PHASE_43_NUMBER_POP_IN_ANIMATION.md"]:::doc
        VR43["phase43_verification_report.md"]:::doc
        P44P["PLAN_PHASE_44_EXPO_GLASS_EFFECT_BOTTOM_BAR.md"]:::doc
        VR44["phase44_verification_report.md"]:::doc
        P45P["PLAN_PHASE_45_EXPO_MESH_GRADIENT_BACKGROUND.md"]:::doc
        VR45["phase45_verification_report.md"]:::doc
    end

    subgraph NavigationLayer["Navigation & Routing Layer"]
        CFD["CoupleFitDestinations.kt<br/>(Today, Challenges, Trends, Hub)"]:::arch
        CFNS["CoupleFitNavigationSuite.kt<br/>(3-Layer Decoupled Dock, Haptics)"]:::arch
        LGE["LiquidGlassEngine.kt<br/>(AGSL Refraction & Dispersion)"]:::arch
        BDE["BackdropEngine.kt<br/>(Kyant0 Backdrop Recorder, 7-Band Dispersion)"]:::arch
        LQG["LiquidGlass.kt<br/>(Modifier.liquidGlass, 28dp Blur, Specular Rim)"]:::arch
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
    BDE -->|Backdrop Layer Recording & AGSL Shader| LQG
    LQG -->|Liquid Glass Styling| CFNS
    P41P -->|Milestone Plan| BDE
    VR41 -->|Verification Report| CFNS
    P43P["PLAN_PHASE_43_NUMBER_POP_IN_ANIMATION.md"]:::doc
    VR43["phase43_verification_report.md"]:::doc
    P43P -->|Specifies Number Pop-In| NPI[NumberPopIn.kt]
    NPI -->|Kinematic Spring Numbers| MAG[MotionArcGauge.kt]
    MAG -->|Animated Gauges & Pill Chips| DS
    VR43 -->|Verifies Pop-In Triggers & Physics| DS
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

### Global Ambient Video Architecture (BGV-06)
- **Domain**: Visual Motion & Ambient Atmosphere (`/designer-skills:brand`)
- **Nodes**:
  - `MainActivity.kt` -> Hosts root `AmbientVideoBackground` with ExoPlayer hardware loop
  - `CoupleFitNavigationSuite.kt` -> Delegates background rendering to root
  - Transparent Cladding -> `ChallengesScreen.kt`, `HealthTrendsScreen.kt`, `CoupleHubScreen.kt`, `SettingsScreen.kt`, Auth screens
  - Status: Shipped in Phase 33 (`v2.9.18-global-ambient-video-bgv06`)

### Phase 34: Tactile Animation Polish & Kinetic Micro-Interactions Architecture (ANM-09 to ANM-14) 🟢 Resolved
- **Domain**: Animation, Sensory Haptics & Micro-Interactions (`/designer-skills:interfaces-that-feel`, `/emil-design-eng`, `/motion`)
- **Milestone Tag**: `v2.9.19-tactile-motion-anm09-14`
- **Nodes & Edges**:
  - `ANM-09`: `CoupleFitNavigationSuite.kt` -> Implements floating dock active capsule pill indicator with bouncy spring dynamics (`Spring.DampingRatioMediumBouncy`, stiffness = `420f`) and directional elastic stretch (`scaleX = 1f + 0.16f * delta`).
  - `ANM-10`: `BounceClick.kt`, `ChallengesScreen.kt`, `ChallengeComponents.kt` -> Implements `Modifier.duelCardPress()` providing instant `0.98x` scale compression, `-1.5°` 3D perspective tilt, and subtle mechanical tick haptic on press.
  - `ANM-11`: `HealthTrendsScreen.kt` -> Implements `FluidSegmentedSwitcher` with sliding spring capsule indicator (`dampingRatio = 0.75f, stiffness = 400f`) across `Day`/`Week`/`Month`/`Year` and `Both`/`You`/`Partner` sub-navigation.
  - `ANM-12`: `DashboardScreen.kt` -> Implements `OrbitalSyncPulse` with rotating dual-particle ring (Cyan user, Coral partner) and radiating radar waves during active sync, settling into breathing green synchrony dot.
  - `ANM-13`: `MedicationDuePill.kt` -> Implements `AnimatedContent` scale punch transition, `CoupleFitHaptics.performHeartbeat` tactile confirmation, and vertical spring ejection auto-dismiss.
  - `ANM-14`: `CoupleHubScreen.kt` -> Implements interactive partner avatar touch trigger with anatomical dual-pulse heartbeat haptics (`Lub-Dub`), heart scale pump, and expanding radiant luminous aura waves.
  - `TEST-34`: `TactileMotionArchitectureTest.kt` -> Automated unit test suite verifying modifier creation, token integrity, and perspective enums (33/33 unit tests green).
- **Verification Evidence**: [`Documentation/phase34_verification_report.md`](file:///D:/Couple_fit/CoupleFit/Documentation/phase34_verification_report.md)

### Phase 35: Floating Hover Dock Architecture & Black Strip Elimination (DCK-01) 🟢 Resolved
- **Domain**: Frontend Design Taste & Optical Translucency (`/taste-skill:design-taste-frontend`, `/ui-ux-pro-max-skill:design-system`)
- **Milestone Tag**: `v2.9.20-floating-hover-dock-dck01`
- **Nodes & Edges**:
  - `DCK-01`: Floating Hover Dock & Black Strip Elimination
  - `CoupleFitNavigationSuite.kt` -> Replaced `Scaffold(bottomBar = ...)` with root `Box(Modifier.fillMaxSize())`, rendering screen content full bleed (`PaddingValues(bottom = 96.dp)`) with floating glass dock overlay anchored at `Alignment.BottomCenter` with `navigationBarsPadding()`.
  - `GlassTokens.kt` -> Recalibrated optical translucency: `TranslucentDockTop` from 91% to 40% (`0x66182030`), `TranslucentDockBottom` from 96% to 53% (`0x880E131E`), preserving top specular rim and 8dp elevation shadow.
  - `MainActivity.kt` -> Enabled edge-to-edge transparent system bars for both status and navigation bars (`Color.TRANSPARENT`), removed inner padding constraint for true edge-to-edge rendering.
  - `Fixinglist.md` -> Section 58 & Master Defect Table entry `DCK-01`
  - `Documentation/phase35_verification_report.md` -> Full live device screenshot evidence and test logs

### Karpathy Codebase Review & De-Slop Architecture (KRP-01 to KRP-06) 📋 Cataloged
- **Domain**: Simplicity First, Code Hygiene & Anti-Speculative Engineering (`/andrej-karpathy-skills:karpathy-guidelines`)
- **Nodes & Edges**:
  - `KRP-01`: Speculative Dead Samsung Health Subsystem (`com.couplefit.data.samsunghealth.*`, 5 files) -> Unused Dagger bindings in `RepositoryModule.kt`; 100% superseded by Health Connect. Target: Prune in Wave 36.
  - `KRP-02`: God-Composable Bloat (`DashboardScreen.kt`, 2,202 lines) -> Extract sub-components (`SideBySideActivityCard`, `WeeklyStreakRow`, `StandingsCard`, dialogs) under `com.couplefit.ui.dashboard.components.*`.
  - `KRP-03`: Unconditional Gesture Interception (`InteractiveChart.kt`, L384-421) -> `change.consume()` unconditionally consumes vertical drags; target: horizontal touch-slop gate.
  - `KRP-04`: Bilateral Telemetry Deficit (`InteractiveChart.kt`) -> Add optional `partnerData: List<ChartDataPoint>?` with dashed Coral curve and translucent delta shading.
  - `KRP-05`: Orphaned & Conflicting Motion Helpers (`FloatingPathsBackground.kt`, `SmoothScrollPhysics.kt`) -> 264 lines of unused canvas animations and conflicting nested scroll physics.
  - `KRP-06`: Stepper Animation Inconsistency (`SpinningCounter.kt`, `ReelDigit.kt`, `SpinConfig.kt`) -> 260 lines used in only 1 secondary card; unify with standard `AnimatedContent`.
  - `Fixinglist.md` -> Section 59 & Master Defect Table entries `KRP-01` to `KRP-06`


### Phase 36: Real-Time Liquid Glass Bottom Navigation Bar & Settings Toggle (LIQ-01) 🟢 Resolved
- **Domain**: Real-Time Shader Glassmorphism & User Preference Architecture (`/taste-skill:design-taste-frontend`, `/ui-ux-pro-max-skill:design-system`)
- **Milestone Tag**: `v2.9.21-liquid-glass-navbar-liq01`
- **Nodes & Edges**:
  - `LIQ-01`: Real-Time Liquid Glass Bottom Navigation Bar & Experience Hub Toggle
  - `dev.chrisbanes.haze:haze:1.3.1` & `haze-materials:1.3.1` -> Added to `app/build.gradle.kts` for AGSL / RenderEffect hardware-accelerated frosted glass blur.
  - `CoupleFitNavigationSuite.kt` -> Instantiates `val hazeState = remember { HazeState() }`, attaches `.hazeSource(state = hazeState)` to the scrollable screen content host, and applies `.hazeEffect(state = hazeState, style = HazeMaterials.ultraThin())` + `RoundedCornerShape(percent = 50)` + `0.5.dp` translucent border (`Color.White.copy(alpha = 0.18f)`) to the floating navigation dock. Falls back to solid dark surface (`surface.copy(alpha = 0.95f)`) when disabled.
  - `CoupleFitPreferencesDataStore.kt` -> Implements `KEY_LIQUID_GLASS`, `isLiquidGlassEnabled: Flow<Boolean>`, and `setLiquidGlassEnabled(enabled: Boolean)` for persistent state.
  - `MainActivity.kt` -> Observes `liquidGlassEnabled`, wires bidirectional state to `SettingsScreen` and passes `liquidGlassEnabled` into `CoupleFitNavigationSuite`.
  - `SettingsScreen.kt` -> Introduces `"Liquid Glass Navigation Bar"` switch row under `APPEARANCE & MOTION` in `ExperienceHubContent`, located directly below `"Reduce Motion"`.
  - `Fixinglist.md` -> Section 60 & Master Defect Table entry `LIQ-01`
  - `Documentation/phase36_verification_report.md` -> Full live device screenshot evidence and test logs on Samsung Galaxy S24 (`SM-S921B`).

### Phase 37: UI Cleanliness & De-Boxing Refactor (BOX-01) 🟢 Resolved
- **Domain**: Frontend Design Taste, Aesthetic Purity & De-Slop (`/taste-skill:design-taste-frontend`, `/antislop-ui`, `/ui-ux-pro-max-skill:design-system`)
- **Milestone Tag**: `v2.9.22-debox-clean-ui-box01`
- **Nodes & Edges**:
  - `BOX-01`: UI Cleanliness & De-Boxing Refactor
  - `CoupleFitNavigationSuite.kt` -> Removed hard `BorderStroke(1.dp, ...)` from the active tab indicator; switched to luminous radial/vertical gradient glow pill with `RoundedCornerShape(percent = 50)`. Softened dock border stroke (`Color.White.copy(alpha = 0.10f)`) and dock drop shadow (`elevation = 4.dp`, `spotColor = Color.Transparent`, `ambientColor = Color.Black.copy(alpha = 0.15f)`).
  - `GlassTokens.kt` & `LiquiGlassTokens.kt` -> Softened `borderWidth` from `1.dp` to `0.5.dp`; recalibrated specular stops from 37% to 16% (`0x28FFFFFF`) and 14% to 6% (`0x10FFFFFF`); reduced shadow elevation from `8.dp` to `4.dp` with transparent spot color; calibrated `BezelWidth` and `borderWidth` to `0.5.dp`.
  - `DashboardScreen.kt` -> Removed hard box borders (`border = null`) on all 3 `FilterChip` items ("Side-by-Side", "My Stats", "Partner's Stats"); softened telemetry status badge border from `1.dp` (`0.4f` alpha) to `0.5.dp` (`0.25f` alpha); replaced `.systemBarsPadding()` with `.statusBarsPadding()` to eliminate redundant bottom padding gaps.
  - `MotionArcGauge.kt` -> Softened today streak indicator border on "Fri" from `1.5.dp` (`0.8f` alpha) to `0.5.dp` (`0.35f` alpha).
  - `Fixinglist.md` -> Section 61 & Master Defect Table entry `BOX-01`
  - `Documentation/phase37_verification_report.md` -> Full live device screenshot evidence and test logs on Samsung Galaxy S24 (`SM-S921B`).


### Phase 38: Couple Intimacy Profile, AGSL Refraction Liquid Glass Dock & Expanded Telemetry (CPX-01) 🟢 Resolved
- **Domain**: Couple Intimacy Telemetry, AGSL Shaders & Permission Hardening (`/taste-skill:design-taste-frontend`, `/android-clean-architecture`, `/security-and-hardening`)
- **Milestone Tag**: `v2.9.23-couple-intimacy-refraction-cpx01`
- **Nodes & Edges**:
  - `CPX-01`: Couple Intimacy Profile, AGSL Refraction Liquid Glass Dock & Expanded Telemetry
  - `FirebaseAuthManager.kt` & `AuthViewModel.kt` -> Implements SharedPreferences session caching (`couplefit_session_cache`) for `cached_couple_id`, `cached_partner_id`, and display names; `AuthViewModel` initializes directly to `AuthFlowStep.Dashboard` if linked, eliminating cold-start invite partner flash.
  - `MedicationDuePill.kt` & `DashboardScreen.kt` -> Implements `isWithinDoseWindow(scheduledTime, 60)` with circular time math to contextually display `MedicationDuePill` only within $[-60m, +60m]$ of scheduled time.
  - `MotionArcGauge.kt` & `DashboardScreen.kt` -> Relocated `AuroraTelemetryMesh` inside the Card container bounded by `Box.clipToBounds()` and dropped alpha to 0.05f; softened radial glow brush opacity to `0.12f` and glow stroke to `3.dp`.
  - `ActiveChallengePreviewCard.kt` -> Wrapped card in `.duelCardPress` spring physics with interactive "⚡ Challenge Partner to a 24h step race!" CTA navigating to Challenges.
  - `DashboardScreen.kt` -> Removed `"Double-tap ring to cheer 💖"` text, restoring identical column heights and 100% baseline symmetry between User and Partner gauges.
  - `CoupleFitPreferencesDataStore.kt` & `HealthConnectViewModel.kt` -> Bound dynamic `userName` and `partnerName` into `HealthDashboardUiState` for live presence and cheering chips.
  - `CoupleHubScreen.kt` & `CoupleHubViewModel.kt` -> Architected full Couple Dossier: Days Paired counter ("💍 Paired for 540 Days"), Birthdays, Relationship description, partner avatar heartbeat ripple, and interactive editing bottom sheet dialog.
  - `ChallengesScreen.kt` & `SettingsScreen.kt` -> Replaced all lingering 1dp card borders with `iosGlass` (0.5dp specular highlights and transparent containers).
  - `LiquidGlassEngine.kt` & `CoupleFitNavigationSuite.kt` -> Implemented `REFRACTION_WITH_DISPERSION_AGSL` runtime shader on Android 13+ with chromatic RGB split factors (`1.08 / 1.00 / 0.92`), 32dp frosted blur (`HazeMaterials.regular()`), and specular highlights.
  - `LocalHapticFeedback` -> Applied tactile haptics across navigation dock pills, settings switches, hub tabs, duel format cards, and filter chips.
  - `AndroidManifest.xml` & `HealthConnectManager.kt` -> Declared `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` and 10 Health Connect read permissions (`StepsRecord`, `HeartRateRecord`, `TotalCaloriesBurnedRecord`, `ActiveCaloriesBurnedRecord`, `DistanceRecord`, `ExerciseSessionRecord`, `PowerRecord`, `SpeedRecord`, `SleepSessionRecord`, `OxygenSaturationRecord`) with strictly zero write permissions; added native battery optimization exemption request card in `SettingsScreen.kt`.
  - `Fixinglist.md` -> Section 62 & Master Defect Table entry `CPX-01`
  - `Documentation/phase38_verification_report.md` -> Full live device screenshot evidence and test logs on Pixel 10 Pro (`emulator-5554`) & Galaxy S24 (`SM-S921B`).

### Phase 39: Bottom Navigation Liquid Glass Optical Decoupling & Razor-Sharp Typography (LIQ-02) 🟢 Resolved
- **Domain**: Real-Time Shader Glassmorphism, Optical Composability & Material 3 Typography (`/taste-skill:design-taste-frontend`, `/ui-ux-pro-max-skill:design-system`, `/antislop-ui`)
- **Milestone Tag**: `v2.9.24-liquid-glass-optical-decoupling-liq02`
- **Nodes & Edges**:
  - `LIQ-02`: Bottom Navigation Liquid Glass Optical Decoupling & Razor-Sharp Typography
  - `CoupleFitNavigationSuite.kt` -> Deconstructed floating navigation dock into 3-Layer Decoupled Glass Architecture:
    - Layer 1 (Glass Backdrop Plate): Independent background Composable with Haze blur (`HazeMaterials.ultraThin()`), AGSL chamfer rim refraction (`refractionHeight = 6.dp`, `refractionAmount = 2.5.dp`), subtle vertical gradient tint, and 0.5dp specular border brush.
    - Layer 2 (Sliding Active Capsule Pill): Smooth spring-animated indicator capsule inside `BoxWithConstraints` isolated from runtime shader effects.
    - Layer 3 (Foreground Navigation Tabs): Direct `Row` of tabs with `Icon` and `Text` running without any shader `renderEffect`, restoring 100% razor-sharp, crisp typography and zero chromatic melting/ghosting.
  - `LiquidGlassEngine.kt` -> Calibrated physical optical constants:
    - Reduced chromatic dispersion RGB multipliers to `1.015 / 1.000 / 0.985` (down from `1.08 / 1.00 / 0.92`), eliminating 7px color separation and producing a subtle, realistic 0.5px prismatic rim refraction.
    - Reduced default `refractionHeight` to `6.dp` and `refractionAmount` to `2.5.dp` to prevent horizontal smear of background elements gliding underneath the dock.
  - `Fixinglist.md` -> Section 63 & Master Defect Table entry `LIQ-02`
  - `Documentation/phase39_verification_report.md` -> Full live device screenshot evidence and test logs on Google Pixel 10 Pro (`emulator-5554`).

### Phase 40: Karpathy Navigation & Liquid Glass Engine De-Slop & Performance Hardening (KRP-07) 🟢 Resolved
- **Domain**: Simplicity First, Code Hygiene, Accessibility Gating & GPU Draw Phase Optimization (`/andrej-karpathy-skills:karpathy-guidelines`)
- **Milestone Tag**: `v2.9.25-karpathy-navigation-cleanup-krp07`
- **Nodes & Edges**:
  - `KRP-07`: Karpathy Navigation & Liquid Glass Engine De-Slop & Performance Hardening
  - `LiquidGlassEngine.kt` -> Pruned 90 lines of dead speculative code (`Modifier.standaloneLiquidGlass`, `isGlassSupported`, `GLASS_EDGE_WIDTH`, `GLASS_EDGE_COLOR`, and unused imports). Calculated exact pill corner radius dynamically as `val r = size.height * 0.5f` in `liquidRefractionLens`.
  - `CoupleFitNavigationSuite.kt` ->
    - Pruned orphaned `import androidx.compose.foundation.BorderStroke`.
    - Wrapped background and border gradient `Brush` objects in `remember { ... }`, eliminating memory allocation churn on recomposition.
    - Gated active indicator animations, stretch factor, and icon scaling with `reduceMotion`: snaps instantly with zero oscillations and zero elastic stretching when enabled.
    - Migrated sliding indicator capsule positioning from `Modifier.offset(x = ...)` to `Modifier.graphicsLayer { translationX = ...; scaleX = ... }`, completely eliminating Compose measure/layout passes during spring motion and ensuring 100% GPU draw-phase execution.
  - `Fixinglist.md` -> Section 64 & Master Defect Table entry `KRP-07`
  - `Documentation/phase40_verification_report.md` -> Full live device screenshot evidence and test logs on Samsung Galaxy S24 (`SM-S921B`).

### Phase 41: BitChord Liquid Glass Bottom Navigation Bar & Physical 3D Refraction Engine (LIQ-04) 🟢 Resolved
- **Domain**: Kyant0-Inspired Backdrop Engine, 7-Band Chromatic Dispersion & Kinetic Stretch-Squash Dock (`liquideffect.md`)
- **Milestone Tag**: `v2.9.26-bitchord-liquid-glass-refraction-liq04`
- **Nodes & Edges**:
  - `LIQ-04`: BitChord Liquid Glass Bottom Navigation Bar & Physical 3D Refraction Engine
  - `BackdropEngine.kt` -> Built `Backdrop` interface, `LayerBackdrop` with `GraphicsLayer.record`, `rememberLayerBackdrop`, and `Modifier.drawBackdrop` with real-time 1:1 hardware buffer sampling, saturation vibrancy boost, and AGSL 7-band chromatic dispersion shader.
  - `LiquidGlass.kt` -> Exposed `LocalAppBackdrop` and `LocalLiquidGlassEnabled`, calibrated physical constants.
  - `CoupleFitNavigationSuite.kt` -> Attached `liquidGlass` dock with decoupled foreground rendering and kinetic active indicator stretch & squash.

### Phase 41.1: Ambient Video Background Occlusion Fix (BGV-07) 🟢 Resolved
- **Domain**: Ambient Motion & Canvas Transparency (`BGV-07`)
- **Milestone Tag**: `v2.9.27-ambient-background-occlusion-fix-bgv07`
- **Nodes & Edges**:
  - `BGV-07`: Ambient Video Background Occlusion Fix
  - `CoupleFitNavigationSuite.kt` -> Removed opaque solid `#07080A` `drawRect(windowBg)` canvas fill from `rememberLayerBackdrop()`. Restored living emerald/sapphire video background blooming cleanly behind all frosted glass cards and liquid dock.

### Phase 42: BitChord Physical 3D Lens Refraction & 7-Wavelength Chromatic Dispersion Engine (REF-01) 🟢 Resolved
- **Domain**: Physical Optics, AGSL Convex Lens Refraction & Spectral Dispersion (`refraction.md` & `/karpathy-guidelines`)
- **Milestone Tag**: `v2.9.28-physical-refraction-lens-ref01`
- **Nodes & Edges**:
  - `REF-01`: Physical 3D Lens Refraction & 7-Wavelength Chromatic Dispersion Engine
  - `BackdropEngine.kt` ->
    - Added `RoundedRectRefractionShaderString` (monochromatic fallback) and `RoundedRectRefractionWithDispersionShaderString` (7-band chromatic dispersion: Red, Orange, Yellow, Green, Cyan, Blue, Purple).
    - Inverted `RenderEffect` chain ordering: `createChainEffect(lensEffect, blurEffect)`. Background is blurred first into a frosted baseline (inner), and the AGSL lens shader evaluates second (outer), ensuring rainbow chromatic dispersion bands remain razor-sharp along the pill bevel without being diffused away.
    - Applied optical inversion: passed `-kotlin.math.abs(refractionAmount)` to uniform, inverting outward SDF gradient into inward physical convex lens magnification.
  - `LiquidGlass.kt` -> Calibrated physical defaults (`blurRadius = 10.dp`, `refractionHeight = 24.dp`, `refractionAmount = 26.dp`, `borderColor = Color.White.copy(alpha = 0.22f)`), and exposed `Modifier.refractiveLens(...)`.
  - `CoupleFitNavigationSuite.kt` -> Bound dock border to `GLASS_EDGE_COLOR` (0.5dp, 22% white) and preserved living ambient video background.
  - `Documentation/phase42_verification_report.md` -> Live hardware screenshot verification on Google Pixel 10 Pro (`emulator-5554`) and Samsung Galaxy S24 (`SM-S921B`).


### Phase 43: Number Pop-In Motion Physics & Dashboard Tracker Choreography (POP-01) 🟢 Resolved
- **Domain**: Kinematic Motion Physics & Tabular Numeral Choreography (transitions.dev)
- **Milestone Tag**: `v2.9.29-number-pop-in-trackers-pop01`
- **Nodes & Edges**:
  - `POP-01`: Number Pop-In Motion Physics & Dashboard Tracker Choreography
  - `NumberPopIn.kt` -> Monotonic cubic solver (`SafeCubicBezierEasing`) evaluating `cubic-bezier(0.34, 1.45, 0.64, 1.0)` with 45% overshoot, `NumberPopInText` and `NumberPopInCounter` with 70ms character stagger, 8dp upward translation, 2dp blur, and tabular numbers (`tnum`).
  - `MotionArcGauge.kt` -> Integrated into `MotionArcProgressGauge` and `MotionPillChip`.
  - `DashboardScreen.kt` -> Bound to `Lifecycle.Event.ON_RESUME` to re-trigger pop-in choreography every time the app opens or resumes.
  - `Documentation/phase43_verification_report.md` -> Live hardware screenshot verification on Google Pixel 10 Pro (`emulator-5554`) and Samsung Galaxy S24.

### Phase 44: Expo Glass Effect Native Liquid Glass Bottom Navigation Bar (EXPO-01) 🟢 Resolved
- **Domain**: Native Glass Optics, Dynamic Touch Compression & Decoupled Rendering (Expo Glass Effect SDK)
- **Milestone Tag**: `v2.9.30-expo-glass-effect-bottom-bar-expo01`
- **Nodes & Edges**:
  - `EXPO-01`: Expo Glass Effect Native Liquid Glass Bottom Navigation Bar
  - `LiquidGlass.kt` -> Declared `GlassEffectStyle` (`Regular`, `Clear`, `None`), `GlassColorScheme` (`Auto`, `Light`, `Dark`), `isGlassEffectAPIAvailable()`, and `Modifier.expoGlassEffect(...)` with style calibration, dynamic spring compression (`scale = 0.985f`), specular rim boost (`alpha = 0.42f`), and `reduceMotion` accessibility gating.
  - `CoupleFitNavigationSuite.kt` -> Bound tab `MutableInteractionSource` states to dock press detection, applied `expoGlassEffect`, and preserved 100% razor-sharp decoupled foreground typography and living video background.
  - `Documentation/phase44_verification_report.md` -> Live hardware screenshot verification on Google Pixel 10 Pro (`emulator-5554`).


### Phase 45: Expo Mesh Gradient Background SDK Implementation (MSH-01) 🟢 Resolved
- **Domain**: Native Mesh Gradient Canvas & Harmonic Orbital Drift (Expo Mesh Gradient SDK)
- **Milestone Tag**: `v2.9.31-expo-mesh-gradient-background-msh01`
- **Nodes & Edges**:
  - `MSH-01`: Expo Mesh Gradient Background SDK Implementation
  - `MeshGradient.kt` -> Declared `MeshGradient` & `MeshGradientView` with direct Expo SDK parameter parity (`columns`, `rows`, `colors`, `points`, `smoothsColors`, `mask`), AGSL `RuntimeShader` on Android 13+ with Hermite spatial weighting ($C^\infty$ continuous, zero banding), multi-radial Canvas fallback on Android < 13, and `AmbientMeshGradientBackground` with 18s Lissajous orbital vertex drift and `reduceMotion` gating.
  - `MainActivity.kt` -> Wired `AmbientMeshGradientBackground` as the primary background in `ThemeCircularRevealContainer`.
  - `SettingsScreen.kt` -> Added `Expo Mesh Gradient Background` switch row in Experience Hub.
  - `MeshGradientTest.kt` -> Comprehensive unit test suite (grid dimensions, signature palettes, normalized bounds).
  - `Documentation/phase45_verification_report.md` -> Live hardware screenshot verification on Google Pixel 10 Pro (`emulator-5554`).


### Phase 46: Expo Glass Effect Component Architecture (`GlassContainer` & `GlassView`) for Bottom Navigation Bar (GLS-08 / EXPO-02) 🟢 Resolved
- **Domain**: Native Glass Component Architecture, Nested Optical Refraction & Tactile Haptics (Expo Glass Effect SDK)
- **Milestone Tag**: `v2.9.32-expo-glass-effect-components-expo02`
- **Nodes & Edges**:
  - `EXPO-02`: Expo Glass Effect Component Architecture (`GlassContainer` & `GlassView`)
  - `LiquidGlass.kt` -> Declared `GlassContainer` (parent coordinating container with `LocalGlassContainerActive`, `LocalGlassContainerSpacing`, and shared spacing), `GlassView` (discrete configurable glass surface composable), and `isLiquidGlassAvailable()` runtime helper.
  - `CoupleFitNavigationSuite.kt` -> Wrapped bottom navigation dock in `GlassContainer(spacing = 8.dp, shape = pillShape)`. Rendered dock plate with `GlassView(Regular)` and sliding active indicator capsule as child `GlassView(Clear, tintColor = UserPrimary.copy(alpha = 0.22f), isInteractive = true)` with crystalline refraction (6dp blur, 26dp refraction, cyan tint wash, specular rim) and interactive spring touch compression.
  - `ExpoGlassEffectTest.kt` -> Unit test suite verifying `GlassEffectStyle` and `GlassColorScheme` variants, runtime helper parity, and calibrated physical constants (100% green bar).
  - `Documentation/phase46_verification_report.md` -> Live hardware screenshot verification across "Today", "Challenges", "Trends", and "Couple Hub" tabs on Google Pixel 10 Pro (`emulator-5554`).


### Phase 47: Restore Ambient Video Aurora Gradient Background (BGV-08) 🟢 Resolved
- **Domain**: Living Canvas Restoration, Ambient Video Motion & Optical Harmony
- **Milestone Tag**: `v2.9.33-restore-ambient-video-bg-bgv08`
- **Nodes & Edges**:
  - `BGV-08`: Restore Ambient Video Aurora Gradient Background
  - `MainActivity.kt` -> Reverted default root canvas from experimental AGSL mesh gradient back to `AmbientVideoBackground(videoResId = currentAmbientBgAsset.resId)`. Migrated SharedPreferences key to `expo_mesh_gradient_enabled` defaulting to `false`, guaranteeing immediate restoration on all devices.
  - `SettingsScreen.kt` -> Updated `meshGradientEnabled` to default `false` in Experience Hub while preserving optional toggle for laboratory experimentation.
  - `CoupleFitNavigationSuite.kt` & `LiquidGlass.kt` -> Confirmed full optical harmony between restored living video background and Phase 46 Expo Glass Effect bottom dock (`GlassContainer` & `GlassView(Clear)`).
  - `Documentation/phase47_verification_report.md` -> Live hardware screenshot verification on Google Pixel 10 Pro (`emulator-5554`) confirming restored deep sapphire/midnight blue aurora video background.


### Phase 48: Expo Glass Effect Advanced Dynamic Proximity & Liquid Blending Engine (EXPO-03 / GLS-09) 🟢 Resolved
- **Domain**: Dynamic Style Animation, Container Proximity Blending & Tactile Haptics (Expo Glass Effect SDK)
- **Milestone Tag**: `v2.9.34-expo-glass-effect-proximity-blending-expo03`
- **Nodes & Edges**:
  - `EXPO-03`: Expo Glass Effect Advanced Dynamic Proximity & Liquid Blending Engine
  - `LiquidGlass.kt` -> Declared `GlassEffectStyleConfig(style, animate, animationDurationMs)`, continuous property animation via `animateFloatAsState` (blur radius, refraction depth, refraction amount) and `animateColorAsState` (tint color), and container proximity blending bonus based on `containerSpacing`. Overloaded `GlassView` and `Modifier.expoGlassEffect`.
  - `CoupleFitNavigationSuite.kt` -> Wrapped bottom navigation dock in `GlassContainer(spacing = 8.dp)`. Applied animated `styleConfig = GlassEffectStyleConfig(Regular, animate = true, 300ms)` to dock plate and `styleConfig = GlassEffectStyleConfig(Clear, animate = true, 280ms)` to sliding active indicator capsule. Decoupled foreground rendering guarantees 100% crisp typography and icons.
  - `ExpoGlassEffectTest.kt` -> Unit test suite verifying `GlassEffectStyleConfig` default and custom values, style enum variants, and runtime helpers (100% green bar).
  - `Documentation/phase48_verification_report.md` -> Live hardware screenshot verification across Today, Challenges, and Couple Hub on Google Pixel 10 Pro (`emulator-5554`).


### Phase 49: Expo MCP Official Glass Effect Alignment (EXPO-04 / GLS-10) 🟢 Resolved
- **Domain**: Expo MCP SDK Ingestion, Exact Schema Alignment & Live Verification
- **Milestone Tag**: `v2.9.35-expo-mcp-glass-effect-alignment-expo04`
- **Nodes & Edges**:
  - `EXPO-04`: Expo MCP Official Glass Effect Alignment
  - `Expo MCP Server` -> Retrieved official documentation via `search_documentation` and `read_documentation` tools for `expo-glass-effect` (~58.0.5).
  - `LiquidGlass.kt` -> Aligned `GlassEffectStyleConfig` with Expo's exact schema: `style: GlassEffectStyle`, `animate: Boolean = false`, and `animationDuration: Float = 0.3f` (in seconds). Bound `durationMillis` to Compose animation specs.
  - `CoupleFitNavigationSuite.kt` -> Configured outer dock plate and sliding active indicator capsule with aligned `GlassEffectStyleConfig` instances (`0.3f` and `0.28f` seconds).
  - `ExpoGlassEffectTest.kt` -> Unit test suite verifying schema defaults (`animate = false`, `animationDuration = 0.3f`) and custom overrides (100% green bar).
  - `Documentation/phase49_verification_report.md` -> Live hardware screenshot verification across Today, Challenges, and Couple Hub on Google Pixel 10 Pro (`emulator-5554`).


### Phase 50: Media3 Video Engine, Bilateral Telemetry, Dashboard Modularization & Relational Kinetics (KRP-01-KRP-04, VID-01, ANM-05-ANM-07) 🟢 Resolved
- **Domain**: AndroidX Media3 ExoPlayer Engine, Directional Touch-Slop Arbitration, Monolith Decomposition & Harmonic Celebrations
- **Milestone Tag**: v2.9.36-media3-bilateral-telemetry-relational-kinetics
- **Nodes & Edges**:
  - KRP-03: Directional touch-slop arbitration in InteractiveChart.kt (abs(totalDx) > touchSlop && abs(totalDx) > abs(totalDy)).
  - KRP-02 & Q7: Monolithic DashboardScreen.kt decomposed into com.couplefit.ui.dashboard.components.* + DashboardUiState.kt.
  - KRP-01: Pruned dead legacy Samsung Health subsystem, duplicate mp4 binaries, and FloatingPathsBackground.kt.
  - KRP-04 / DTV-01: Bilateral dual splines (cyan user, dashed coral partner) with delta fill and synchronized dual-reading tooltip.
  - VID-01: AndroidX Media3 ExoPlayer 1.4.1 upgrade on TextureView with zero-gap loop playback.
  - ANM-05: CoupleRingClosureHarmonicBurst with 56-particle dual Canvas cascade and fanfare haptics.
  - ANM-07: Parabolic flying nudge particles in QuickNudgeRow.kt.
  - Documentation/phase50_verification_report.md -> Live hardware screenshot verification across Today, My Stats, and Partner's Stats on Google Pixel 10 Pro (emulator-5554).

### Phase 51: Real-Time Partner Telemetry Synchronization & Demo Seeding Engine (PTR-01) 🟢 Resolved
- **Domain**: Partner Telemetry Synchronization, Room SSOT Seeding & Session Binding
- **Milestone Tag**: v2.9.37-partner-telemetry-sync-ptr01
- **Nodes & Edges**:
  - PTR-01: Remediation for missing partner telemetry in UI.
  - PartnerRepository.kt & PartnerRepositoryImpl.kt -> Added seedDemoPartnerDaily(partnerId, dateStr, defaultSteps) and addPartnerSteps(partnerId, dateStr, count) for local Room SSOT mutation.
  - HealthConnectViewModel.kt -> Injected FirebaseAuthManager, added observeUserSessionAndPartnerSync() to dynamically observe authenticated credentials and re-bind Firestore snapshot listeners via startListeningToPartner(), and exposed addSamplePartnerSteps().
  - AuthViewModel.kt -> Auto-seeded realistic partner daily telemetry during guest sign-in (signInAsGuest) and demo entry (enterDemoDashboard).
  - DashboardScreen.kt -> Added +1k Partner button in the morning welcome card alongside +1k Steps, wired partner stat card add sample steps trigger, and updated nudge refresh callback.
  - PartnerRepositoryTest.kt & PartnerTelemetrySyncTest.kt -> Automated unit test suite verifying Room seeding, step increments, UI mapping, and dual ring closure (32/32 tests 100% green).
  - Documentation/phase51_verification_report.md -> Live hardware screenshot verification on Google Pixel 10 Pro (emulator-5554) confirming Partner: 7,420 steps, 49m, 311 kcal, and active sync indicator.

### Phase 57: Rollback of EGM Automated UI Disruptions & Restoration of Phase 51 Interface (EGM-REV-01) 🟢 Resolved
- **Domain**: System Recovery, Interface Restoration & AGSL Shader Pipeline
- **Milestone Tag**: `v2.9.43-rollback-egm-ui-restored`
- **Nodes & Edges**:
  - `EGM-REV-01`: Reverted automated UI disruptions from Phases 52-56 back to Phase 51 baseline.
  - `LiquidGlassEngine.kt`: Restored full AGSL runtime shader and chromatic dispersion optics.
  - `DashboardScreen.kt` & `HealthTrendsScreen.kt`: Restored clean typography, pull-refresh dynamics, and partner telemetry engine.
  - `Documentation/phase57_verification_report.md` -> Live hardware screenshot verification on Google Pixel 10 Pro (`emulator-5554`).

### Phase 58: Adaptive Responsive Typography System (TYP-06 / ADP-01) 🟢 Resolved
- **Domain**: Responsive Typography, Window Size Class Breakpoints & CompositionLocal Telemetry Tokens
- **Milestone Tag**: `v2.9.44-adaptive-typography-typ06`
- **Nodes & Edges**:
  - `TYP-06`: Dynamic Material 3 typography scaling across Window Size Classes (<360dp: 0.90x, 360-599dp: 1.00x, 600-839dp: 1.10x, ≥840dp: 1.20x).
  - `ADP-01`: Unified responsive hero telemetry display token (`LocalHeroMetricDisplay` at 52sp baseline).
  - `Type.kt` -> Declared `getAdaptiveTypographyScale()`, `getAdaptiveTypography()`, `getAdaptiveHeroMetric()`, `rememberAdaptiveTypography()`, and `rememberAdaptiveHeroMetric()`.
  - `Theme.kt` -> Bound dynamic adaptive typography and hero metrics, wrapped `MaterialTheme` inside `CompositionLocalProvider(LocalHeroMetricDisplay provides adaptiveHeroMetric)`.
  - `AdaptiveTypographyTest.kt` -> 5-test unit suite verifying scale rungs, proportional sizing, monotonic growth, and tabular numerals preservation (100% green bar).
  - `Documentation/phase58_verification_report.md` -> Live hardware screenshot verification across Today dashboard on Google Pixel 10 Pro (`emulator-5554`).
