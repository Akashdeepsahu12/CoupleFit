# Phase 58 Milestone Plan: Adaptive Responsive Typography System (`TYP-06` / `ADP-01`) 📐🔤

> **Master Defect / Enhancement ID**: `TYP-06` (Adaptive Responsive Typography Scale)  
> **Target Files**:
> - [`app/src/main/java/com/couplefit/ui/theme/Type.kt`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/ui/theme/Type.kt)
> - [`app/src/main/java/com/couplefit/ui/theme/Theme.kt`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/ui/theme/Theme.kt)
> - [`app/src/test/java/com/couplefit/ui/AdaptiveTypographyTest.kt`](file:///D:/Couple_fit/CoupleFit/app/src/test/java/com/couplefit/ui/AdaptiveTypographyTest.kt)  
> **Status**: Ready for Execution

---

## 1. Problem Statement & Root Cause

The user reported:  
> *"the typo are not adaptive can you fix it/"*

Currently in CoupleFit:
1. **Static Typography Definition**:
   [`app/src/main/java/com/couplefit/ui/theme/Type.kt`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/ui/theme/Type.kt) defines a single static `val Typography = Typography(...)` object. Regardless of device form factor (320dp narrow phone, 412dp standard phone, 600dp foldable, or 840dp+ tablet), all font sizes and line heights are fixed.
2. **Hero Metric Disconnection**:
   [`HeroMetricDisplay`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/ui/theme/Type.kt#L24) (`52.sp`, ExtraBold) is a static `TextStyle` that is neither provided through `CompositionLocalProvider` nor adapted to screen width class, causing potential clipping on narrow viewports (<360dp) and appearing modest on wide tablets.
3. **No Window Width Class Scaling**:
   [`app/src/main/java/com/couplefit/ui/theme/Theme.kt`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/ui/theme/Theme.kt) passes the static `Typography` into `MaterialTheme` without detecting viewport width or window size class.

---

## 2. Architectural Design & Acceptance Criteria

### 2.1 Viewport Width Rungs & Scale Factors
| Window Size Class | Screen Width (`dp`) | Scale Factor | `HeroMetricDisplay` | `displayLarge` | `titleLarge` | `bodyMedium` | `labelSmall` |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Compact Narrow** | `< 360.dp` | **`0.90f`** | `46.sp` | `34.sp` | `17.sp` | `13.5.sp` | `10.sp` |
| **Compact Standard** | `360.dp .. 599.dp` | **`1.00f`** | `52.sp` | `38.sp` | `19.sp` | `15.sp` | `11.sp` |
| **Medium (Foldable/Portrait)** | `600.dp .. 839.dp` | **`1.10f`** | `57.sp` | `42.sp` | `21.sp` | `16.5.sp` | `12.sp` |
| **Expanded (Tablet/Landscape)** | `≥ 840.dp` | **`1.20f`** | `62.sp` | `46.sp` | `23.sp` | `18.sp` | `13.sp` |

### 2.2 Core Deliverables
1. **`getAdaptiveTypography(screenWidthDp: Int)` & `@Composable rememberAdaptiveTypography()`**:
   - Mathematical proportional scaling preserving mathematical ratio, font weights, tabular numeral feature settings (`fontFeatureSettings = "tnum"`), and outdoor tracking (`letterSpacing = 0.8.sp` on `labelSmall`).
   - Scales font sizes and line heights proportionally so line spacing never collides or clips.
2. **`getAdaptiveHeroMetric(screenWidthDp: Int)` & `LocalHeroMetricDisplay`**:
   - `LocalHeroMetricDisplay` `CompositionLocal` exposed for custom fitness hero readouts.
   - `MaterialTheme.heroMetricDisplay` extension getter for clean caller ergonomics.
3. **`CoupleFitTheme` Integration**:
   - [`Theme.kt`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/ui/theme/Theme.kt) observes `LocalConfiguration.current.screenWidthDp` and dynamically instantiates adaptive typography and hero metrics inside `CompositionLocalProvider`.
4. **Automated Unit Tests**:
   - [`AdaptiveTypographyTest.kt`](file:///D:/Couple_fit/CoupleFit/app/src/test/java/com/couplefit/ui/AdaptiveTypographyTest.kt): Verifies monotonic scaling across all 4 screen size rungs, line height proportionality, and font feature settings preservation.

---

## 3. Test & Verification Bars
- `.\gradlew.bat testDebugUnitTest`: 100% green.
- `.\gradlew.bat compileDebugAndroidTestKotlin`: 100% clean compilation.
- Live device verification on `emulator-5554` via ARTEMIS.
