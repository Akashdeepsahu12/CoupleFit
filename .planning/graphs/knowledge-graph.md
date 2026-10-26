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
        AG["AGENTS.md"]:::doc
    end

    subgraph NavigationLayer["Navigation & Routing Layer"]
        CFD["CoupleFitDestinations.kt<br/>(Today, Challenges, Trends, Hub)"]:::arch
        CFNS["CoupleFitNavigationSuite.kt<br/>(M3 NavigationBar & Rail)"]:::arch
        MA["MainActivity.kt<br/>(Adaptive Scaffold Host)"]:::arch
    end

    subgraph UILayer["UI Screens & Composables"]
        DAS["DashboardAdaptiveScreen.kt<br/>(Pane 1 & Pane 2)"]:::ui
        DS["DashboardScreen.kt<br/>(Operational Epicenter)"]:::ui
        MDP["MedicationDuePill.kt<br/>(Contextual Due Pill)"]:::ui
        CSCR["ChallengesScreen.kt<br/>(Duels & Trophies)"]:::ui
        HTS["HealthTrendsScreen.kt<br/>(Historical Trends & Meds)"]:::ui
        CHS["CoupleHubScreen.kt<br/>(Partner Profile & Privacy)"]:::ui
        SET["SettingsScreen.kt<br/>(Account & Danger Zone)"]:::ui
    end

    subgraph DataLayer["Data & Repositories"]
        HD["HealthDao.kt / HealthDatabase.kt"]:::data
        HE["HealthEntities.kt (CoupleChallengeEntity)"]:::data
        CVM["ChallengeViewModel.kt"]:::data
        AVM["AuthViewModel.kt"]:::data
    end

    %% Relationships
    CS -->|Validates 4-Tab Mental Model| CFD
    IA -->|Defines Ergonomic Layout| CFNS
    FL -->|Catalogs Defect CSA-02 / NAV-01| MA
    
    CFD -->|Enums & Routes| CFNS
    CFNS -->|Hosts Tabs| MA
    MA -->|Tab 1: Today| DAS
    MA -->|Tab 2: Challenges| CSCR
    MA -->|Tab 3: Trends| HTS
    MA -->|Tab 4: Couple Hub| CHS
    MA -->|Utility / Danger Zone| SET

    DAS --> DS
    DS --> MDP
    CSCR --> CVM
    CSCR --> HE
    CHS -->|Privacy Shortcut [🔒]| SET
```

## Entity Cross-Reference Table

| Symbol / File | Layer | Primary Responsibility | Upstream Dependencies | Downstream Consumers |
| :--- | :--- | :--- | :--- | :--- |
| `CoupleFitDestination` | Navigation | Type-safe `@Serializable` routes for 4 validated tabs + Settings | `card-sort-analysis.md` | `TopLevelDestination`, `MainActivity.kt` |
| `TopLevelDestination` | Navigation | Metadata enum (routes, icons, labels, a11y descriptions) | `CoupleFitDestinations.kt` | `CoupleFitNavigationSuite.kt`, `MainActivity.kt` |
| `CoupleFitNavigationSuite` | Navigation | Adaptive M3 `NavigationBar` (<600dp) & `NavigationRail` (≥600dp) | `WindowSizeClass`, `TopLevelDestination` | `MainActivity.kt`, `NavigationComposeUiTest.kt` |
| `MainActivity` | App Entry | Activity lifecycle, theme reveal, auth router, adaptive shell host | `CoupleFitNavigationSuite`, `AuthViewModel` | Android OS |
| `ChallengesScreen` | UI (Tab 2) | Head-to-head duels, step races, milestone/trophy vault | `ChallengeViewModel`, `SideBySideChallengeCard` | `MainActivity.kt` |
| `HealthTrendsScreen` | UI (Tab 3) | Multi-horizon telemetry, medication schedule, sensor diagnostics | `AppSpacing`, Material 3 | `MainActivity.kt` |
| `CoupleHubScreen` | UI (Tab 4) | Relationship anchor, streaks, anniversaries, privacy shortcut | `AppSpacing`, `SettingsScreen` | `MainActivity.kt` |
