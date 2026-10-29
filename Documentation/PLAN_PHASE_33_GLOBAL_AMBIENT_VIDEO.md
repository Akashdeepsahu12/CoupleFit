# Milestone Plan: Phase 33 — Global Ambient Video Background Architecture (`BGV-06`) 🌌📱✨

> **Tracking Reference**: [`Documentation/Fixinglist.md`](file:///D:/Couple_fit/CoupleFit/Documentation/Fixinglist.md)
> **Active Design & Video Skill**: [`/designer-skills:brand`](file:///C:/Users/akash/.gemini/config/plugins/ui-ux-pro-max-skill/skills/brand/SKILL.md), [`/antislop-ui`](file:///C:/Users/akash/.gemini/config/plugins/antislop/skills/antislop-ui/SKILL.md)
> **Branch**: `dev_akash`
> **Target Release Tag**: `v2.9.18-global-ambient-video-bgv06`

---

## 1. Executive Summary & Problem Diagnosis

### 1.1 The User Problem
The user reported:
> *"can you apply the background on all the screens? i am only able to see on the mail dashboard."*

### 1.2 Root Cause Analysis
1. **Container Scoping**: In `MainActivity.kt`, `AmbientVideoBackground` was instantiated only inside `CoupleFitNavigationSuite`. When navigating to `SettingsScreen` or Auth flows (`SignInScreen`, `InvitePartnerScreen`, `EnterCodeScreen`, `LinkedConfirmationScreen`), the screen was rendered outside `CoupleFitNavigationSuite`, where only a solid `MaterialTheme.colorScheme.background` surface existed.
2. **Opaque Scaffold Cladding**:
   - `ChallengesScreen.kt`: Line 143 declares `Scaffold(containerColor = MaterialTheme.colorScheme.background)` and line 130 sets `TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)`. In dark theme, this paints a 100% solid black shield over the video.
   - `HealthTrendsScreen.kt`: Line 48 uses default `Scaffold` containerColor (`MaterialTheme.colorScheme.background`), blocking the video completely.
   - `CoupleHubScreen.kt`: Line 32 uses default `Scaffold` containerColor (`MaterialTheme.colorScheme.background`), blocking the video completely.
   - `SettingsScreen.kt`: Line 177 uses `containerColor = MaterialTheme.colorScheme.background` and lines 173 sets `TopAppBar` to background color.
   - `PrivacyTransparencyScreen.kt`: Line 65 sets `TopAppBar` to background color.
   - Auth screens (`SignInScreen`, `InvitePartnerScreen`, etc.): Decorated with solid black background surfaces.

### 1.3 Target Architecture
1. **Root-Level Video Hoisting**: Hoist `AmbientVideoBackground` to the root layout in `MainActivity.kt` directly inside `ThemeCircularRevealContainer`. A single persistent ExoPlayer instance plays seamlessly across all screens, tab switches, and sub-screen transitions without hitching or re-buffering.
2. **Transparent Cladding & Optical Glass**:
   - Change `Scaffold` and `TopAppBar` across `ChallengesScreen`, `HealthTrendsScreen`, `CoupleHubScreen`, `SettingsScreen`, `PrivacyTransparencyScreen`, and Auth screens to `containerColor = Color.Transparent`.
   - Cards and surfaces retain their frosted glass tokens (`iosGlass`, `liquiGlass`, or `MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)`), allowing the living ambient video (e.g. `green1.mp4`, `blue.mp4`) to shine through with full portrait resolution and depth.

---

## 2. Explicit Acceptance Criteria & Test Bars

| Target Component | Criteria / Metric | Test Bar |
| :--- | :--- | :--- |
| **`MainActivity.kt`** | Hoist `AmbientVideoBackground` to root `Box(Modifier.fillMaxSize())`. Ensure it spans under all navigation steps. | Video continues playing continuously during tab transitions. |
| **`ChallengesScreen.kt`** | `Scaffold` containerColor = `Color.Transparent`, `TopAppBar` containerColor = `Color.Transparent`. | Ambient video clearly visible behind duel cards on emulator/device. |
| **`HealthTrendsScreen.kt`** | `Scaffold` containerColor = `Color.Transparent`, `TopAppBar` containerColor = `Color.Transparent`. | Ambient video clearly visible behind trend charts and weekly rings. |
| **`CoupleHubScreen.kt`** | `Scaffold` containerColor = `Color.Transparent`, `TopAppBar` containerColor = `Color.Transparent`. | Ambient video clearly visible behind couple presence aura and milestones. |
| **`SettingsScreen.kt`** | `Scaffold` containerColor = `Color.Transparent`, `TopAppBar` containerColor = `Color.Transparent`. | Ambient video clearly visible behind settings hub segments. |
| **Auth Screens** | Remove opaque background fills on `SignInScreen`, `InvitePartnerScreen`, `EnterCodeScreen`, `LinkedConfirmationScreen`. | Ambient video visible on initial onboarding and sign-in. |

---

## 3. Implementation Wave

- **Wave 1**: Root Hoisting in `MainActivity.kt` & `CoupleFitNavigationSuite.kt` refactor.
- **Wave 2**: Screen Scaffold transparency in `ChallengesScreen.kt`, `HealthTrendsScreen.kt`, `CoupleHubScreen.kt`, `SettingsScreen.kt`, and `PrivacyTransparencyScreen.kt`.
- **Wave 3**: Auth Screen background transparency adjustments.
- **Wave 4**: Deterministic Verification (`testDebugUnitTest`, `compileDebugAndroidTestKotlin`, ARTEMIS captures on `emulator-5554` for all 4 primary tabs + Settings).
