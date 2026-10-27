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
