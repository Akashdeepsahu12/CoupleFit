# Milestone Plan: Phase 35 — True Floating Hover Dock Architecture & Black Strip Elimination (`DCK-01`) 🛸✨

> **Tracking Reference**: [`Documentation/Fixinglist.md`](file:///D:/Couple_fit/CoupleFit/Documentation/Fixinglist.md) §58
> **Active Design Skills**: [`/taste-skill:design-taste-frontend`](file:///C:/Users/akash/.gemini/config/plugins/taste-skill/skills/taste/SKILL.md), [`/antislop-ui`](file:///C:/Users/akash/.gemini/config/plugins/antislop/skills/antislop-ui/SKILL.md), [`/motion`](file:///C:/Users/akash/.gemini/config/plugins/designer-skills/skills/motion/SKILL.md)
> **Branch**: `dev_akash`
> **Target Release Tag**: `v2.9.20-floating-hover-dock-dck01`

---

## 1. Executive Summary & Problem Diagnosis

### 1.1 The User Problem
The user reported:
> *"i want the navigation menu in the bottom to hover, i can see it is on a black strip."*

### 1.2 Root Cause Analysis
1. **Scaffold Layout Partitioning**: In `CoupleFitNavigationSuite.kt`, the floating dock was wrapped inside `Scaffold(bottomBar = { ... })`. Material 3's `Scaffold` enforces strict layout partitioning: it measures the `bottomBar` and places the content above it, preventing screen content from rendering behind or underneath the bottom dock.
2. **Double Bottom Padding in `MainActivity.kt`**: `MainActivity.kt` received `innerPadding` from `CoupleFitNavigationSuite` and wrapped all screen content in `Box(modifier = Modifier.padding(innerPadding))`. This applied an extra ~132dp bottom padding, truncating all dashboard and tab content far above the bottom of the screen and leaving an empty dead void.
3. **Dock Surface Opacity**: In `GlassTokens.kt`, `TranslucentDockTop` was set to `0xE81C2333` (91% opacity) and `TranslucentDockBottom` was set to `0xF410131C` (96% opacity). This rendered the dock as a near-solid black bar rather than a translucent hovering glass capsule.
4. **System Navigation Bar Scrim**: In `MainActivity.kt`, `enableEdgeToEdge()` was invoked without explicit transparent system navigation bar styles, leaving default system scrim behind the gesture bar.

---

## 2. Target Architecture Delivered

1. **Overlay Floating Dock Architecture**:
   - In `CoupleFitNavigationSuite.kt`, replace `Scaffold(bottomBar = ...)` with a root `Box(modifier.fillMaxSize())`.
   - Screen content renders edge-to-edge across the entire screen (`fillMaxSize()`).
   - The floating dock is placed as an overlay anchored at `Alignment.BottomCenter` with `padding(horizontal = 20.dp, vertical = 12.dp).navigationBarsPadding()`.
2. **Edge-to-Edge Screen Flow**:
   - In `MainActivity.kt`, remove the restrictive `innerPadding` bottom constraint from the main screen container. Content (cards, dials, charts, video) flows continuously underneath the dock.
   - All screens already include bottom clearance spacers (`Spacer(modifier = Modifier.height(96.dp))` to `108.dp`), ensuring bottom cards scroll completely above the dock when scrolled to the end.
3. **Translucent Optical Liquid Glass Dock**:
   - In `GlassTokens.kt`, recalibrate `TranslucentDockTop` to `Color(0x66182030)` (40% translucent frosted slate) and `TranslucentDockBottom` to `Color(0x880E131E)` (53% translucent deep midnight glass).
   - Specular top rim border and ambient drop shadow provide tactile elevation and physical separation from scrolling content.
4. **Transparent System Navigation Bar**:
   - In `MainActivity.kt`, configure `enableEdgeToEdge()` with explicit transparent `SystemBarStyle` for both status bar and navigation bar.

---

## 3. Explicit Acceptance Criteria & Test Bars

| Target Component | Acceptance Criteria | Test Bar |
| :--- | :--- | :--- |
| **`CoupleFitNavigationSuite.kt`** | Root layout is `Box(Modifier.fillMaxSize())` with dock positioned as overlay at `Alignment.BottomCenter`. | Content renders edge-to-edge behind the floating dock. |
| **`MainActivity.kt`** | Remove `Modifier.padding(innerPadding)` from screen content `Box`. Configure transparent `SystemBarStyle`. | No bottom partition or black strip at the bottom of the window. |
| **`GlassTokens.kt`** | Recalibrate dock surface to 40%-53% optical glass translucency. | Living ambient video and content are visibly refracted under the dock. |
| **Test Suite** | `./gradlew testDebugUnitTest` and `./gradlew compileDebugAndroidTestKotlin` pass 100% green. | 0 test failures, 0 compile errors. |
| **Live Device Verification** | Capture ARTEMIS screenshots on `emulator-5554` verifying scrolling content flows under dock and zero black strip. | Live screenshot and pixel analysis confirms true hover. |

---

## 4. Execution Waves

- **Wave 1**: Recalibrate dock tokens in `GlassTokens.kt`.
- **Wave 2**: Re-architect `CoupleFitNavigationSuite.kt` to overlay floating dock.
- **Wave 3**: Update `MainActivity.kt` with edge-to-edge transparency and full-bleed screen container.
- **Wave 4**: Deterministic verification (`testDebugUnitTest`, `compileDebugAndroidTestKotlin`, ARTEMIS captures, documentation synchronization, commit and tag).
