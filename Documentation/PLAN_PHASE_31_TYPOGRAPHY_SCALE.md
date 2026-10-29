# Phase 31 Plan: Typography Scale Hierarchy & Harmonious Contrast (TYP-02)

> **Phase**: 31  
> **Defect Key**: `TYP-02` (Cataloged in [`Documentation/Fixinglist.md`](file:///D:/Couple_fit/CoupleFit/Documentation/Fixinglist.md) and [`Documentation/triage.md`](file:///D:/Couple_fit/CoupleFit/Documentation/triage.md))  
> **Associated Skill**: `/designer-skills:typography-scale`  
> **Design Target**: Eliminate size duplication between `headlineSmall` and `titleLarge` to establish distinct optical hierarchy.  
> **Status**: In Progress  

---

## 1. Problem Statement & Root Cause Analysis

### Problem Statement
In `app/src/main/java/com/couplefit/ui/theme/Type.kt`, both `headlineSmall` and `titleLarge` share identical typographic metrics:
```kotlin
headlineSmall = TextStyle(
    fontFamily = NeueHelveticaFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 20.sp,
    lineHeight = 26.sp,
    letterSpacing = 0.sp,
    fontFeatureSettings = "tnum"
),
titleLarge = TextStyle(
    fontFamily = NeueHelveticaFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 20.sp,
    lineHeight = 26.sp,
    letterSpacing = 0.15.sp
)
```
Because both styles evaluate to `20.sp` font size and `26.sp` line height with `FontWeight.SemiBold`, section headers (which use `headlineSmall` or `headlineMedium`) visually blend with card titles (which use `titleLarge`), causing **optical flattening**. Users scanning the screen fail to perceive clear boundaries between overarching dashboard sections and discrete card containers.

### Typographic Principles (`/designer-skills:typography-scale`)
A robust digital typography scale requires distinct, non-overlapping rungs on the size ladder:
1. **Mathematical Ratio Step**: The scale follows a harmonious progressive ratio (~1.12 to 1.25) ensuring that every typographic role communicates its structural seniority.
2. **Size Distinctness**:
   - `headlineSmall` represents section/module titles (`21.sp`, Bold, `27.sp` line height, `(-0.1).sp` letter spacing, tabular numbers).
   - `titleLarge` represents card containers and modal headers (`19.sp`, SemiBold, `25.sp` line height, `0.15.sp` letter spacing).
3. **Weight Pairing**: Headlines command higher optical density (`FontWeight.Bold`), while titles provide structural framing without overpowering the data inside (`FontWeight.SemiBold`).

---

## 2. Deliverables & Acceptance Criteria

1. **`app/src/main/java/com/couplefit/ui/theme/Type.kt` Update**:
   - Calibrate `headlineSmall`:
     - `fontSize = 21.sp`
     - `lineHeight = 27.sp`
     - `fontWeight = FontWeight.Bold`
     - `letterSpacing = (-0.1).sp`
     - `fontFeatureSettings = "tnum"`
   - Calibrate `titleLarge`:
     - `fontSize = 19.sp`
     - `lineHeight = 25.sp`
     - `fontWeight = FontWeight.SemiBold`
     - `letterSpacing = 0.15.sp`
2. **Audit Composable Usages**:
   - Verify that all cards and headers across `DashboardScreen.kt`, `DashboardAdaptiveScreen.kt`, `ChallengesScreen.kt`, and `HealthTrendsScreen.kt` render with clean proportional separation.
3. **Exploration & Cataloging of Further Gaps**:
   - Log new animation gaps (`ANM-05` Dual Ring Closure Celebration Cascade, `ANM-06` Interactive Chart scrub popover fluid spring physics) and micro-typography opportunities (`TYP-05`) into `Fixinglist.md` and `triage.md`.

---

## 3. Test & Verification Bar

1. **Local Unit Tests**:
   - `./gradlew testDebugUnitTest` must pass with 100% success (32 tasks, 0 failures).
2. **Android Test Compilation**:
   - `./gradlew compileDebugAndroidTestKotlin` must compile with 0 errors.
3. **ARTEMIS Live Device Capture**:
   - Verify on `emulator-5554` (`Pixel_10_Pro`, API 36) with screenshot capture showing the refreshed typographic hierarchy in the live UI.
