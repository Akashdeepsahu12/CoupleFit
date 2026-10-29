# Milestone Plan: Phase 32 — Readable Measure & Typographic Cadence Architecture (`MSR-01` to `MSR-07`) 📐🔤

> **Tracking Reference**: [`Documentation/Fixinglist.md#section-14`](file:///D:/Couple_fit/CoupleFit/Documentation/Fixinglist.md)
> **Active Design Skill**: [`/designer-skills:readable-measure`](file:///C:/Users/akash/.gemini/config/plugins/designer-skills/skills/readable-measure/SKILL.md)
> **Branch**: `dev_akash`
> **Target Release Tag**: `v2.9.17-readable-measure-msr01`

---

## 1. Executive Summary & Ergonomic Mission

In accordance with `/designer-skills:readable-measure`, optimal saccadic eye movement and text comprehension require **45 to 75 characters per line (CPL)** (target ideal: **~65 CPL**). 
Prior to this phase:
- On tablet / foldable expanded viewports (>=600dp up to 1280dp), full-width screens (`PrivacyTransparencyScreen`, `SettingsScreen`, `LinkedConfirmationScreen`) let body text sprawl to **112–160 CPL**, causing ocular fatigue and return-sweep tracking dropouts (`MSR-01`, `MSR-03`).
- On small modals and foldables in flex mode (width ~300dp), text descriptions were compressed into **22–34 CPL**, causing jagged, fragmented line wraps (`MSR-02`, `MSR-04`).
- In `DashboardAdaptiveScreen.kt`, splitting into a 40/60 master-detail layout at 600dp choked Master Pane 1 into ~260dp, destroying dashboard card readability (`MSR-05`).
- Open source license notices and dashboard card subtitles lacked container constraints, sprawling across wide screens (`MSR-06`, `MSR-07`).

Phase 32 introduces the foundational Compose architectural primitives `Modifier.readableMeasure()` and `Modifier.dialogMeasure()`, and applies them across all affected components to strictly constrain character count to the 45–75 CPL comfort zone.

---

## 2. Explicit Acceptance Criteria & Test Bars

| Target Defect | Component | Criteria / Metric | Test Bar |
| :--- | :--- | :--- | :--- |
| **`MSR-01`** | `PrivacyTransparencyScreen.kt`, `SettingsScreen.kt` | Tablet body text measure constrained to max 620dp (~65 CPL). Content horizontally centered. | Visual inspection on tablet viewport; line characters <= 75 CPL. |
| **`MSR-02`** | `ChallengeComponents.kt` (`StartChallengeDialog`) | Modal content width constrained to `dialogMeasure(min = 320.dp, max = 500.dp)`. | Dialog measure between 45 and 65 CPL on all screen densities. |
| **`MSR-03`** | `LinkedConfirmationScreen.kt` | Root layout bounded with `readableMeasure(maxWidth = 480.dp)`. Text wraps at 45–60 CPL. | No sprawling single-line confirmation on tablet or foldable. |
| **`MSR-04`** | `DashboardScreen.kt` (`TelemetryDiagnosticDialog`) | Applied `Modifier.dialogMeasure()`. Minimum width 320dp even on narrow foldables. | Description maintains >= 40 CPL without awkward word breaking. |
| **`MSR-05`** | `DashboardAdaptiveScreen.kt` | Two-pane master-detail reserved strictly for `WindowWidthSizeClass.Expanded` (>=840dp). `Medium` (600–839dp) displays single column centered layout. | Pane 1 width >= 400dp on all viewports, eliminating choked 22–30 CPL state. |
| **`MSR-06`** | `SettingsScreen.kt` (`showLicensesDialog`) | License acknowledgment dialog bounded by `Modifier.dialogMeasure(min = 340.dp, max = 580.dp)`. | Legal text lines stay within 55–75 CPL on tablets. |
| **`MSR-07`** | `DashboardScreen.kt` (Cards) | Dashboard cards wrap subtitles within responsive boundaries. | Subtitle text does not sprawl past 75 CPL. |

---

## 3. Architectural Design

### 3.1 `Modifier.readableMeasure()` & `Modifier.dialogMeasure()` Primitive
Defined in [`app/src/main/java/com/couplefit/ui/theme/Spacing.kt`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/ui/theme/Spacing.kt):

```kotlin
package com.couplefit.ui.theme

import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Constrains text reading containers to the optimal typographic measure of 45–75 CPL
 * (~600dp–640dp) on tablets, foldables, and desktop/DeX displays.
 */
fun Modifier.readableMeasure(
    maxWidth: Dp = 620.dp,
    minWidth: Dp = Dp.Unspecified
): Modifier = this.widthIn(min = minWidth, max = maxWidth)

/**
 * Standardizes dialog and modal sheet measures, enforcing a minimum width of 320dp
 * to prevent choked measure (<35 CPL) and a maximum of 500dp to avoid sprawl.
 */
fun Modifier.dialogMeasure(
    minWidth: Dp = 320.dp,
    maxWidth: Dp = 500.dp
): Modifier = this.widthIn(min = minWidth, max = maxWidth)
```

---

## 4. Task Dependencies & Execution Wave

```
Wave 1: Core Theme Primitives
├── Task 1: Add readableMeasure and dialogMeasure to Spacing.kt
└── Task 2: Unit test verification of Spacing modifier extensions

Wave 2: Screen & Dialog Layout Refactors
├── Task 3: PrivacyTransparencyScreen.kt & SettingsScreen.kt (MSR-01)
├── Task 4: ChallengeComponents.kt & StartChallengeDialog (MSR-02)
├── Task 5: LinkedConfirmationScreen.kt (MSR-03)
├── Task 6: TelemetryDiagnosticDialog & License Dialog (MSR-04, MSR-06)
├── Task 7: DashboardAdaptiveScreen.kt breakpoint refinement (MSR-05)
└── Task 8: DashboardScreen.kt Card Subtitles (MSR-07)

Wave 3: Deterministic Verification & ARTEMIS Captures
├── Task 9: Gradle testDebugUnitTest (32 tests green)
├── Task 10: Gradle compileDebugAndroidTestKotlin (0 errors)
├── Task 11: ARTEMIS visual capture on emulator-5554
└── Task 12: Documentation sync & git release tagging
```

---

## 5. Risk Assessment & Mitigation
- **Regression Risk**: Existing unit tests or UI tests expecting full-width bounds.
  - *Mitigation*: `widthIn()` specifies max-width constraints only, which naturally expands on compact phones (360–412dp) with zero layout shifts or regressions.
- **Breakpoint Risk**: Medium viewports (600–839dp) transitioning from two-pane to single-column.
  - *Mitigation*: Single-column on Medium viewports aligns with official Android Material 3 Adaptive Scaffold guidelines (`ListDetailPaneScaffold`).
