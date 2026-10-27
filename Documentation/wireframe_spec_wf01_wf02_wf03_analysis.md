# CoupleFit Wireframe Specification & Ergonomics Analysis (`WF-01` to `WF-07`) 📐📱

> **Designer Skill Standard**: Follows `/designer-skills:wireframe-spec` conventions.  
> **Target System**: CoupleFit Android & Tablet Ecosystem (Mobile `<600dp`, Medium `600–839dp`, Expanded `≥840dp`).  
> **Compliance Bar**: WCAG 2.2 Level AA (48x48dp minimum touch targets, 4.5:1 text contrast, TalkBack semantics).  
> **Status**: Comprehensive Analysis & Master Specification (`WF-01`, `WF-02`, `WF-03`, `WF-04`, `WF-05`, `WF-06`, `WF-07`).

---

## 1. Executive Summary & Problem Framing

A systemic wireframe and ergonomic audit was conducted across the CoupleFit application hierarchy:
1. **`Today` Dashboard (`DashboardScreen.kt`, `DashboardAdaptiveScreen.kt`)**
2. **`Challenges` & Duels (`ChallengesScreen.kt`, `ChallengeComponents.kt`)**
3. **`Health Trends` & Telemetry (`HealthTrendsScreen.kt`)**
4. **`Couple Hub` & Partnership Anchor (`CoupleHubScreen.kt`)**
5. **Onboarding & Auth Flow (`SignInScreen.kt`, `InvitePartnerScreen.kt`, `EnterCodeScreen.kt`)**
6. **Modal & Dialog Presentation (`MainActivity.kt`, `SettingsScreen.kt`)**

```
+----------------------------------------------------------------------------------------------------+
|                                 COUPLEFIT WIREFRAME AUDIT SCORECARD                                 |
+-----------------------------------+--------------------+--------------------+----------------------+
| Dimension                         | Baseline Audit     | Wireframe Spec Fix | Target Grade / Delta |
+-----------------------------------+--------------------+--------------------+----------------------+
| 1. Above-the-Fold Density (Compact)| 1,206dp total ht   | 620dp above fold   | +82% critical glance |
| 2. Metric Triad Consistency       | 3 conflicting sets | Unified 3-Token    | 100% cross-screen    |
| 3. Connectivity & Offline State   | Blocking modal     | Non-blocking pill  | Zero workflow freeze |
| 4. Modal Touch Ergonomics         | Centered dialog    | M3 Bottom Sheet    | 100% thumb zone reach|
| 5. Auth / Keyboard Resilience     | 0 scroll / clipped | imePadding + scroll| 0% clipping failures |
| 6. Cold-Start Guidance            | Blank / debug text | 4-State Contract   | Warm Day-1 CTA flow  |
| 7. Container Rhythm ("Card Mania")| 11 stacked cards   | 4-Tier Surface     | De-carded Hero Rings |
+-----------------------------------+--------------------+--------------------+----------------------+
```

---

## 2. Deep-Dive Defect Analysis (`WF-01` to `WF-07`)

### 2.1 `WF-01` — Viewport Fold Overflow & Progressive Disclosure
- **Root Cause**: On compact mobile screens (`360x800dp`), `DashboardScreen.kt` renders an unsegmented vertical stack of 11 cards totaling **1,206dp in height**. With an effective viewport height of ~680dp (after subtracting status bar, top app bar, and bottom navigation bar), **over 44% of critical daily content is buried below the fold**:
  - `QuickNudgeRow` (64dp): positioned at `y = 720dp` (below fold).
  - `ActiveChallengePreviewCard` (130dp): positioned at `y = 800dp` (below fold).
  - `InteractiveChart` (260dp): positioned at `y = 946dp` (deep scroll).
- **User Impact**: Users must repeatedly scroll 500dp+ just to send a quick nudge to their partner or check active challenge standings.
- **Architectural Solution**: Reorganize the dashboard into a strict 3-tier hierarchy:
  - **Tier 1 (Above Fold, 0–640dp)**: Header $\to$ Contextual Meds Pill $\to$ Weekly Streak Continuity Rail $\to$ Canvas-Emancipated Side-by-Side Activity Rings $\to$ Quick Nudge Deck (moved directly below rings into prime thumb zone).
  - **Tier 2 (Primary Scroll, 640–880dp)**: Today's Head-to-Head Duel Card $\to$ Active Challenge Preview.
  - **Tier 3 (Progressive Disclosure, 880dp+)**: Replace the static 260dp chart with a 56dp summary pill `[📈 Heart Rate Pulse • 72 BPM Avg [Expand ▾]]` that smoothly expands into the interactive Bézier stream on demand. Eliminate the redundant `PartnerOfflineCard` (120dp).

---

### 2.2 `WF-02` — Universal Metric Triad Consistency
- **Root Cause**: Disconnected metric presentations across screens:
  - `Today` dashboard displays Steps and Caloric Burn in `SideBySideActivityCard`, but relegates Active Minutes to a buried text row.
  - `HealthTrendsScreen.kt` in `CombinedTelemetryCard` renders Steps and Active Minutes, but completely omits Caloric Burn.
  - `SoloTelemetryCard` renders Steps, Active Minutes, and Calories, creating cognitive dissonance when switching between `Both 👥` and `You 👤`.
  - `CoupleFitAppWidget.kt` previously only displayed steps.
- **Architectural Solution**: Codify the immutable **CoupleFit Metric Triad Contract**:
  ```
  +---------------------------------------------------------------------------------+
  |                       COUPLEFIT UNIVERSAL METRIC TRIAD                          |
  +--------------------+-----------------------+--------------------+---------------+
  | Metric Component   | Primary Unit & Format | Semantic Glyphs    | Priority Tier |
  +--------------------+-----------------------+--------------------+---------------+
  | 1. Steps           | "xx,xxx" / goal ratio | 👟 Footsteps       | Hero [1]      |
  | 2. Active Minutes  | "xxx min"             | ⚡ Lightning Bolt  | Secondary [2] |
  | 3. Caloric Burn    | "x,xxx kcal"          | 🔥 Flame Glyph     | Secondary [2] |
  +--------------------+-----------------------+--------------------+---------------+
  ```
  This triad MUST appear in the exact same spatial order and visual cadence across all 4 primary screens, telemetry cards, and widgets.

---

### 2.3 `WF-03` — Status Synchronization & Non-Blocking Network State
- **Root Cause**:
  - `CoupleHubScreen.kt` line 93 contains a static hardcoded text string: `Text("🟢 Partner Online • Battery 84%")`, completely decoupled from `ConnectivityObserver` or partner sync heartbeats.
  - `MainActivity.kt` renders a modal `AlertDialog` (`NoInternetDialog`) whenever network disconnects, abruptly blocking workout logging or local sensor counting.
- **Architectural Solution**:
  - Deprecate modal blocking dialogs for network drops (`ERR-01`).
  - Introduce an ambient, non-blocking Top App Bar Status Pill:
    - Connected: `[🟢 Live ⚡ • Synced 2m ago]` (tap opens diagnostic sheet).
    - Degraded/Stale: `[🟡 Partner Synced 18m ago • Cached [↺]]` (indicates local Room SSOT data).
    - Fully Offline: `[⚪ Offline Mode • Local Cache Active]` (reassures user that sensor counting continues).

---

### 2.4 `WF-04` — Modal Dialog Ergonomics (Thumb Zone Bottom Sheets)
- **Root Cause**: Complex task workflows—such as `StartChallengeDialog` in `ChallengeComponents.kt` and `UnlinkPartnerDialog` in `SettingsScreen.kt`—are implemented as centered floating `Dialog` windows.
  - On compact mobile devices (<600dp), a centered dialog forces users to reach into the upper 35–45% of the screen (the "stretch" or "impossible" thumb zone according to Steven Hoober’s thumb zone ergonomics).
  - Dropdown pickers and date pickers inside centered dialogs trigger double-nested modal shifts.
- **Architectural Solution**:
  - Compact screens (<600dp): Automatically adapt all task dialogs to Material 3 `ModalBottomSheet` anchored to the bottom thumb zone with drag handles.
  - Medium & Expanded screens ($\ge 600\text{dp}$): Render as centered dialogs with a strict `widthIn(max = 480.dp)` constraint.

---

### 2.5 `WF-05` — Auth & Pairing Form Resilience (`RSP-01`)
- **Root Cause**: In `SignInScreen.kt`, `InvitePartnerScreen.kt`, and `EnterCodeScreen.kt`, the root layout is an unconstrained `Column(modifier = Modifier.fillMaxSize().padding(...))` lacking `verticalScroll(rememberScrollState())` and `imePadding()`.
  - When the software keyboard (IME) opens, the primary action CTA ("Link Accounts", "Sign In") is clipped off-screen with 0px scrollability.
  - In landscape orientation (~360dp available height), the screen is completely locked and unusable.
- **Architectural Solution**:
  - Wrap all authentication and pairing screens in a unified `ResponsiveAuthContainer`:
    ```kotlin
    Box(modifier = Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState())) {
        Column(modifier = Modifier.widthIn(max = 480.dp).align(Alignment.Center)) { ... }
    }
    ```

---

### 2.6 `WF-06` — 4-State Wireframe Contract (Empty, Loading, Populated, Offline)
- **Root Cause**: Most screens only implement the happy "Populated" state. On Day 1 or fresh login, the UI displays either:
  - Blank spaces with 0-step empty smudges.
  - Abrupt centered `CircularProgressIndicator()` spinners causing severe layout shifts (CLS > 0.25).
  - Debug buttons (`+ Add 1,000 Test Steps`) confusing production end users.
- **Architectural Solution**: Codify a mandatory 4-State Wireframe Contract for every screen:
  1. **Empty / Cold-Start**: Motivational, warm copy + clear Day-1 onboarding CTA button.
  2. **Loading**: Structural skeleton shimmer (`shimmerPlaceholder`) matching exact geometry with 0px layout shift.
  3. **Populated**: Live bilateral telemetry with spring-loaded gauges.
  4. **Offline / Cached**: Explicit cache timestamp pill with non-blocking manual sync trigger.

---

### 2.7 `WF-07` — De-Cluttering "Card Mania" & Information Density
- **Root Cause**: `DashboardScreen.kt` wraps every single element in an identical `#1C1C1E` dark card with 16dp rounded corners (11 consecutive stacked cards).
  - The hero Activity Rings are confined inside a card, competing with sub-cards for visual weight.
  - `PartnerOfflineCard` (120dp) duplicates the exact same step, minute, and sync values already shown in `SideBySideActivityCard`.
- **Architectural Solution**:
  - **De-card Hero Activity Rings**: Emancipate the dual activity rings to float directly on the screen canvas as Tier 1 visual anchors.
  - **Eliminate Redundant Cards**: Merge `PartnerOfflineCard` telemetry directly into the partner gauge's contextual subtitle.
  - **Group Actions**: Group Quick Nudges directly beneath the rings as an elevated horizontal Action Rail.

---

## 3. Master Annotated Wireframe Specifications

### 3.1 Wireframe A: Today Dashboard (Compact Mobile `<600dp`)

```
================================================================================
| [H1] COUPLEFIT TODAY                          [🔔] [⚙️]  <- [1] Header Utility
| [1] [🟡 Partner synced 4m ago • Live ⚡]                    <- Dynamic Status Pill
================================================================================
| [2] [💊 Vitamin D & Omega-3 due at 8:00 PM            [✓]]  <- Contextual Meds
================================================================================
| [3] WEEKLY STREAK CONTINUITY RAIL                           <- [3] Streak Flow
|     (M)  (T)  (W)  (T)  (F)  (S)  (S)   [🔥 5 Day Streak]
|     [●]  [●]  [●]  [●]  [●]  [○]  [○]
================================================================================
| [4] HERO BILATERAL ACTIVITY RINGS (Emancipated on Canvas)   <- [4] Hero Biometric
|
|         YOU (Cyan)                  PARTNER (Coral)
|      .-------------.               .-------------.
|     /    9,420      \             /    8,150      \
|    |     Steps       |           |     Steps       |
|    |    (94% Goal)   |           |    (81% Goal)   |
|     \   ⚡ 42m 🔥 480/             \   ⚡ 36m 🔥 410/
|      '-------------'               '-------------'
|
|     [ Double-tap partner ring to send cheer 💖 ]          <- Gesture Hint
================================================================================
| [5] QUICK NUDGE ACTION DECK (Elevated to Thumb Zone)       <- [5] Intimacy Tools
|     [👋 Poke]   [💪 Let's Walk!]   [💖 Proud of You]   [☕ Rest Day]
================================================================================
--------------------------- VIEWPORT FOLD LINE (~680dp) ------------------------
================================================================================
| [6] TODAY'S COMPETITION & DUEL                               <- [6] Head-to-Head
|     +------------------------------------------------------+
|     | [H2] Daily Step Duel                     [Ends in 4h]|
|     | You: 9,420 steps (53.6%) [=================         ]|
|     | Partner: 8,150 steps     [==============            ]|
|     | Delta: +1,270 steps • You are leading! 🏆            |
|     +------------------------------------------------------+
================================================================================
| [7] ACTIVE CHALLENGE PREVIEW                                 <- [7] Modular Goal
|     +------------------------------------------------------+
|     | [H3] Weekend 50K Together               [Day 2 of 2] |
|     | Combined: 38,420 / 50,000 steps (76.8%)              |
|     | [==============================             ]        |
|     | [View All Challenges ➔]                              |
|     +------------------------------------------------------+
================================================================================
| [8] PROGRESSIVE DISCLOSURE: LIVE BIOMETRICS                  <- [8] Low Priority
|     +------------------------------------------------------+
|     | [📈 Live Heart Rate Pulse • 72 BPM Avg        [Expand ▾]]|
|     +------------------------------------------------------+
================================================================================
| [1] [ 🏠 Today ]   [ 🏆 Challenges ]   [ 📈 Trends ]   [ 👥 Hub ] <- Nav Suite
================================================================================
```

#### Content Specification & Hierarchy for Today Dashboard:
| Priority | Component | Heading | Char Limit | Source | A11y Description |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **[1]** | Top App Bar & Status | H1 | 18 chars | Device / Connectivity | "CoupleFit Today dashboard. Partner synced 4 minutes ago, live." |
| **[2]** | Contextual Meds Pill | H3 | 42 chars | Room DB | "Medication alert: Vitamin D due at 8:00 PM. Tap to mark taken." |
| **[3]** | Weekly Streak Rail | Label | 16 chars | Streak Engine | "Weekly streak: 5 days completed out of 7." |
| **[4]** | Hero Dual Activity Rings | Display | 6 chars/num | Health Connect API | "You: 9,420 steps, 94% of goal. Partner: 8,150 steps, 81% of goal." |
| **[5]** | Quick Nudge Action Deck | Button | 14 chars | PartnerRepository | "Quick nudge: Poke partner, send walking invite, or cheer." |
| **[6]** | Today's Competition Card| H2 | 24 chars | Challenge Engine | "Daily Step Duel: You lead by 1,270 steps." |
| **[7]** | Active Challenge Preview| H3 | 28 chars | Challenge Engine | "Weekend 50K Together: 76.8% completed. Tap to view duels." |
| **[8]** | Live Biometrics Pill | Label | 32 chars | Sensor / BLE | "Live biometrics: 72 beats per minute. Tap to expand chart." |

---

### 3.2 Wireframe B: Health Trends & Telemetry Screen

```
================================================================================
| [H1] HEALTH TRENDS & TELEMETRY                               [📅] [⚙️]
================================================================================
| [1] TIMEFRAME HORIZON SELECTOR (M3 Single Choice Segmented Button)
|     [ Day ]          [ Week (Active) ]          [ Month ]
================================================================================
| [2] BILATERAL PERSPECTIVE SELECTOR (Connected M3 Segmented Button)
|     [ 👥 Both (Active) ]         [ 👤 You ]         [ 💖 Partner ]
================================================================================
| [3] STANDARDIZED METRIC TRIAD CARD (Active Perspective: Both)
|     +------------------------------------------------------------------------+
|     | [H2] BILATERAL STEP COMPARISON                         [+12% vs last wk]|
|     | Combined: 92,400 steps • Synchrony Index: 94%                          |
|     |                                                                        |
|     |         YOU (Cyan)                           PARTNER (Coral)           |
|     |         48,200 steps                         44,200 steps              |
|     |         ⚡ 280 active mins                   ⚡ 252 active mins         |
|     |         🔥 2,450 kcal                        🔥 2,180 kcal             |
|     +------------------------------------------------------------------------+
================================================================================
| [4] DUAL-STREAM COMPARATIVE HISTORICAL CHART (220dp Height)
|     +------------------------------------------------------------------------+
|     | Steps                                                                  |
|     | 12k |        .--.                                                      |
|     |     |   .--./    \     Cyan: You                                       |
|     |  8k |  /    \     \.-. Coral: Partner                                  |
|     |     | /      '--.     \                                                |
|     |  4k |/           '--.  \                                               |
|     |     +--------------------------------                                  |
|     |       Mon  Tue  Wed  Thu  Fri  Sat  Sun                                |
|     | [ℹ️ Tap any day node to scrub exact hourly telemetry]                   |
|     +------------------------------------------------------------------------+
================================================================================
| [5] MEDICATION & SUPPLEMENT ADHERENCE BREAKDOWN
|     +------------------------------------------------------------------------+
|     | [H3] Couple Adherence Synchrony                        [92% Joint Rate]|
|     | You: 14/14 doses taken (100%) [====================================]   |
|     | Partner: 12/14 doses taken (85.7%) [=============================  ]   |
|     +------------------------------------------------------------------------+
================================================================================
| [1] [ 🏠 Today ]   [ 🏆 Challenges ]   [ 📈 Trends ]   [ 👥 Hub ]
================================================================================
```

---

### 3.3 Wireframe C: Modal Presentation Ergonomics (`WF-04`)

#### Compact Mobile (<600dp): Material 3 Bottom Sheet (Anchored to Thumb Zone)
```
+------------------------------------------------------------------+
| (App Background Dimmed with 75% Dark Scrim + 20dp Blur)          |
|                                                                  |
|                                                                  |
|                                                                  |
| +--------------------------------------------------------------+ |
| |                        [ === Drag Handle === ]               | | <- [1]
| | [H2] Start New Couple Challenge                        [✕]   | | <- [2]
| | Choose a challenge format to compete or cooperate together:  | |
| |                                                              | |
| | (•) 🏃 Head-to-Head Step Duel (First to 15,000 steps today)  | | <- [3]
| | ( ) 🤝 Co-op Step Journey (50,000 steps over the weekend)    | |
| | ( ) ⚡ Active Minutes Surge (60 active minutes together)      | |
| |                                                              | |
| | Target Duration:                                             | |
| | [ 24 Hours (Active) ]         [ 3 Days ]         [ 7 Days ]  | | <- [4]
| |                                                              | |
| | [🚀 Launch Challenge (Primary CTA - 56dp Height)]            | | <- [5]
| | [Cancel]                                                     | | <- [6]
| +--------------------------------------------------------------+ |
+------------------------------------------------------------------+
  ^ Entire interaction stays in the bottom 45% screen zone (Zero stretch)
```

#### Medium & Expanded Screens ($\ge 600\text{dp}$): Centered Constrained Dialog
```
+----------------------------------------------------------------------------------+
|                                                                                  |
|                +------------------------------------------------+                |
|                | [H2] Start New Couple Challenge           [✕]  |                |
|                | Choose a format to compete or cooperate:       |                |
|                |                                                |                |
|                | (•) 🏃 Head-to-Head Step Duel                  |                |
|                | ( ) 🤝 Co-op Step Journey                      |                |
|                | ( ) ⚡ Active Minutes Surge                     |                |
|                |                                                |                |
|                | Duration: [ 24 Hours ]  [ 3 Days ]  [ 7 Days ] |                |
|                |                                                |                |
|                | [Cancel]               [🚀 Launch Challenge]   |                |
|                +------------------------------------------------+                |
|                    ^ Max Width constrained to 480dp                              |
+----------------------------------------------------------------------------------+
```

---

### 3.4 Wireframe D: Responsive Auth & Form Container (`WF-05` / `RSP-01`)

#### Phone Portrait with Keyboard Open
```
+------------------------------------------------------------------+
| [H1] LINK PARTNER ACCOUNT                                        | <- [1]
| Enter the 6-character invitation code from your partner:         |
|                                                                  |
| +--------------------------------------------------------------+ |
| |  [ C ]   [ F ]   [ - ]   [ 8 ]   [ 9 ]   [ 2 ]               | | <- [2] 6-Digit Code
| +--------------------------------------------------------------+ |
|                                                                  |
| [🔗 Link Partner Account (Primary CTA - 56dp Height)]          | | <- [3] CTA Visible!
|                                                                  |
| [ Need help? Generate a new invite code ]                        | | <- [4]
+------------------------------------------------------------------+
| [ Q ] [ W ] [ E ] [ R ] [ T ] [ Y ] [ U ] [ I ] [ O ] [ P ]      |
|   [ A ] [ S ] [ D ] [ F ] [ G ] [ H ] [ J ] [ K ] [ L ]          | <- Software Keyboard
|     [ Z ] [ X ] [ C ] [ V ] [ B ] [ N ] [ M ]   [ ⌫ ]            |    (IME Space)
|              [        SPACE        ]          [ DONE ]           |
+------------------------------------------------------------------+
  ^ Container applies .imePadding() & .verticalScroll(): 0px clipping!
```

#### Phone Landscape Mode (~360dp Height)
```
+----------------------------------------------------------------------------------+
|  [H2] LINK PARTNER      |  Code: [ C ][ F ][ - ][ 8 ][ 9 ][ 2 ]                  |
|  Enter your partner's   |                                                        |
|  6-character invite code|  [🔗 Link Accounts]             [Cancel]               |
+----------------------------------------------------------------------------------+
  ^ Smooth vertical scrolling allows 100% reach to input fields and buttons.
```

---

### 3.5 Wireframe E: 4-State Wireframe Contract (`WF-06`)

```
+----------------------------------------------------------------------------------------------------+
|                                    4-STATE WIREFRAME CONTRACT                                       |
+-----------------------------------+----------------------------------------------------------------+
| State                             | Visual Wireframe Structure & Content Behavior                  |
+-----------------------------------+----------------------------------------------------------------+
| 1. COLD START / EMPTY             | +------------------------------------------------------------+ |
| (Day 1 / 0 Steps)                 | | [🌱 Welcome to CoupleFit!]                                 | |
|                                   | | "Your fitness journey together starts today. Lace up your  | |
|                                   | | shoes and take your first morning stroll together!"        | |
|                                   | |                                                            | |
|                                   | | [🚶 Start First Walk Together]      [Connect Health Data]  | |
|                                   | +------------------------------------------------------------+ |
+-----------------------------------+----------------------------------------------------------------+
| 2. LOADING STATE                  | +------------------------------------------------------------+ |
| (0px Layout Shift Shimmer)        | | [ /// Shimmer Header //////////////////// ]                | |
|                                   | |     (/// Ring Shimmer ///)      (/// Ring Shimmer ///)     | |
|                                   | | [ ////////////////// Nudge Deck Shimmer ///////////////// ]| |
|                                   | | [ ////////////////// Active Duel Shimmer //////////////// ]| |
|                                   | +------------------------------------------------------------+ |
+-----------------------------------+----------------------------------------------------------------+
| 3. POPULATED STATE                | +------------------------------------------------------------+ |
| (Live Dual Telemetry)             | | [H1] COUPLEFIT TODAY                   [🟢 Live ⚡ Synced 1m] | |
|                                   | | You: 9,420 steps (94%)       Partner: 8,150 steps (81%)    | |
|                                   | | ⚡ 42m • 🔥 480 kcal          ⚡ 36m • 🔥 410 kcal         | |
|                                   | | [👋 Poke]   [💪 Walk!]   [💖 Cheer]   [☕ Rest Day]        | |
|                                   | +------------------------------------------------------------+ |
+-----------------------------------+----------------------------------------------------------------+
| 4. OFFLINE / CACHED               | +------------------------------------------------------------+ |
| (Room SSOT Sync Recovery)         | | [🟡 Offline Mode • Cached vitals (Synced 18m ago)      [↺]]| |
|                                   | | You: 9,420 steps (Cached)    Partner: 8,150 steps (Cached) | |
|                                   | | [ℹ️ Steps continue counting locally while offline]          | |
|                                   | +------------------------------------------------------------+ |
+-----------------------------------+----------------------------------------------------------------+
```

---

## 4. Ergonomic Touch & Accessibility Specifications (WCAG 2.2 AA)

### 4.1 Fitts's Law Touch Target Architecture
- **Minimum Interactive Boundary**: Every interactive button, pill, icon, and tab MUST satisfy a minimum bounding box of **$48 \times 48\,\text{dp}$**, even when the visual icon is smaller ($24\text{dp}$).
- **Touch Target Padding**:
  - Top app bar utility icons: $24\text{dp}$ vector wrapped in a `Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center)`.
  - Quick Nudge action pills: $38\text{dp}$ visual height with $5\text{dp}$ vertical padding totaling $48\text{dp}$ touch target.
  - Weekly streak circle glyphs: $32\text{dp}$ circle with $8\text{dp}$ surrounding touch slop.

### 4.2 Thumb Zone Heatmap Distribution
- **Green Zone (Natural Thumb Sweep, 60–100% Screen Height)**:
  - Navigation Suite bar (`CoupleFitNavigationSuite.kt`).
  - Quick Nudge Action Deck (`QuickNudgeRow.kt`).
  - Modal Bottom Sheet confirm/cancel buttons.
- **Yellow Zone (Moderate Reach, 30–60% Screen Height)**:
  - Hero Dual Activity Rings (`SideBySideActivityCard`).
  - Today's Competition Card.
- **Red Zone (Upper Reach, 0–30% Screen Height)**:
  - Restricted strictly to non-critical utility triggers: Settings icon, Notification bell, and Status info chip. Zero destructive or frequent actions placed here.

---

## 5. Responsive Breakpoint Adaptation Matrix

| Component | Compact (`<600dp`) | Medium (`600–839dp`) | Expanded (`≥840dp`) |
| :--- | :--- | :--- | :--- |
| **Global Navigation** | Bottom `NavigationBar` (80dp height) | Side `NavigationRail` (80dp width) | Extended `NavigationRail` or Drawer |
| **Today Hero Rings** | Horizontal Side-by-Side (140dp each) | Horizontal Centered (160dp each) | Dual 200dp rings in Left Master Pane |
| **Quick Nudges** | Horizontal scrollable row (4 pills) | Centered 4-pill grid | 2x2 Grid card in Right Detail Pane |
| **Modal Workflows** | Material 3 `ModalBottomSheet` | Centered Dialog (`width = 480.dp`) | Centered Dialog (`width = 520.dp`) |
| **Auth Screens** | 1-col scrollable (`width = 100%`) | Centered card (`max-width = 440.dp`)| Centered card (`max-width = 480.dp`) |
| **Dashboard Layout** | Single-column vertical scroll | Single-column centered (max 720dp) | Two-pane Master-Detail (Split 50/50) |

---

## 6. Implementation & Verification Roadmap

1. **`WF-01`**: In `DashboardScreen.kt`, de-card hero rings, elevate `QuickNudgeRow` to immediately follow rings, and collapse `InteractiveChart` into a 56dp summary pill.
2. **`WF-02`**: In `HealthTrendsScreen.kt`, add Caloric Burn to `CombinedTelemetryCard` to complete the universal triad.
3. **`WF-03`**: In `CoupleHubScreen.kt` and `MainActivity.kt`, replace static string and blocking dialog with non-blocking reactive top bar status banner.
4. **`WF-04`**: In `ChallengeComponents.kt` and `SettingsScreen.kt`, adapt `StartChallengeDialog` and `UnlinkPartnerDialog` to `ModalBottomSheet` on compact screens.
5. **`WF-05`**: In `SignInScreen.kt`, `InvitePartnerScreen.kt`, and `EnterCodeScreen.kt`, apply `ResponsiveAuthContainer` with `verticalScroll` and `imePadding`.
6. **`WF-06`**: Codify 4-state rendering in all primary screen composables.
7. **`WF-07`**: De-card hero activity rings and delete redundant `PartnerOfflineCard`.

---
*Authored under `/designer-skills:wireframe-spec` for CoupleFit.*
