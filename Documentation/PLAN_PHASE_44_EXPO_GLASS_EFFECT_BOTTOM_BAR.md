# Milestone Plan: Phase 44 — Expo Glass Effect Native Liquid Glass Bottom Navigation Bar (`EXPO-01`)

> **Milestone**: Phase 44 (`EXPO-01`)  
> **Topic**: Expo Glass Effect Native Liquid Glass Component Architecture Applied to Bottom Navigation Bar  
> **Reference**: [docs.expo.dev/versions/latest/sdk/glass-effect/](https://docs.expo.dev/versions/latest/sdk/glass-effect/) (`expo-glass-effect` / Apple Liquid Glass Architecture)  
> **Lifecycle Gate**: `/gsd-plan-phase` & `/impeccable`  
> **Status**: DRAFT / IN PROGRESS

---

## 1. Context & Motivation

The user requested applying the official Expo Glass Effect from [https://docs.expo.dev/versions/latest/sdk/glass-effect/](https://docs.expo.dev/versions/latest/sdk/glass-effect/) to the bottom bar of the app.

`expo-glass-effect` provides the Apple iOS native Liquid Glass (`UIVisualEffectView`) component architecture for modern navigation bars and UI elements:
1. **Configurable Glass Styles (`glassEffectStyle`)**:
   - `'regular'`: Native frosted liquid glass with balanced diffusion, optical refraction, specular rim highlight, and rich translucency (`tintColor`).
   - `'clear'`: High-clarity crystalline liquid glass with ultra-subtle tint wash, lower blur diffusion, and heightened refraction / edge highlights, letting the vibrant background shine through with crystalline optics.
   - `'none'`: Passthrough / transparent.
2. **Interactive Tactile Feedback (`isInteractive = true`)**:
   - In `expo-glass-effect`, interactive glass responds dynamically to user touch gestures with physical depression, specular highlight intensification, and fluid spring animations.
3. **Platform & Theme Adaptation (`colorScheme: 'auto' | 'light' | 'dark'`, `tintColor`)**:
   - Automatic dynamic tinting based on theme or explicit tint color, supporting user customization.
   - Graceful degradation: On devices or configurations where hardware AGSL / RenderEffect is unavailable, it falls back seamlessly to an ultra-refined translucent glass surface without layout shifts or crashes.

---

## 2. Technical Architecture & Component Design

In our native Android Jetpack Compose codebase, we will translate the Expo Glass Effect API into idiomatic Kotlin and Compose primitives:

### 2.1 Domain Enums & Models (`LiquidGlass.kt`)
- `enum class GlassEffectStyle`:
  - `Regular`: Balanced frosted liquid glass (14dp blur, 22dp refraction, 0.45 alpha tint, 0.22 alpha specular rim).
  - `Clear`: High-clarity crystalline liquid glass (6dp blur, 26dp refraction, 0.20 alpha tint, 0.35 alpha specular rim).
  - `None`: Neutral frosted surface without AGSL chromatic distortion.
- `enum class GlassColorScheme`:
  - `Auto`: Follows system / app dark/light theme.
  - `Light`: Forces light-mode frosted glass appearance.
  - `Dark`: Forces dark-mode frosted glass appearance.

### 2.2 Modifier Architecture (`Modifier.expoGlassEffect`)
```kotlin
@Composable
fun Modifier.expoGlassEffect(
    shape: CornerBasedShape = RoundedCornerShape(percent = 50),
    glassEffectStyle: GlassEffectStyle = GlassEffectStyle.Regular,
    isInteractive: Boolean = true,
    tintColor: Color? = null,
    colorScheme: GlassColorScheme = GlassColorScheme.Auto,
    borderWidth: Dp = GLASS_EDGE_WIDTH,
    borderColor: Color? = null,
    reduceMotion: Boolean = false
): Modifier
```

### 2.3 Bottom Navigation Integration (`CoupleFitNavigationSuite.kt`)
- Wire `Modifier.expoGlassEffect` directly into the floating dock container.
- Support `isInteractive = true` with interactive tactile spring depression on the dock container when tabs are pressed.
- Expose `glassEffectStyle` parameter in `CoupleFitNavigationSuite` with default `GlassEffectStyle.Regular`.
- Preserve the living `AmbientVideoBackground` pass-through via `rememberLayerBackdrop()`.
- Preserve decoupled foreground rendering for 100% razor-sharp, zero-distortion icons and typography.

---

## 3. Explicit Acceptance Criteria & Test Bars

1. **`AC-EXPO-01` (Glass Style Variants)**:
   - `GlassEffectStyle.Regular`: Frosted liquid glass with 14dp blur, 22dp refraction, and specular rim.
   - `GlassEffectStyle.Clear`: Ultra-translucent crystalline glass with 6dp blur and heightened refraction.
   - `GlassEffectStyle.None`: Clean fallback without shader arithmetic.
2. **`AC-EXPO-02` (Interactive Feedback)**:
   - When `isInteractive = true`, tab clicks trigger micro-spring depression (`0.985f`) and specular rim illumination boost.
   - Fluid active indicator pill continues to morph and slide with spring physics.
3. **`AC-EXPO-03` (Decoupled Foreground & Living Background)**:
   - Foreground icons and tab labels remain 100% razor-sharp with 0 text melting or chromatic ghosting.
   - Living ambient video background remains visible blooming underneath the dock.
4. **`AC-EXPO-04` (Accessibility)**:
   - `reduceMotion` disables spring scale and compression, snapping transitions instantly.
5. **`AC-EXPO-05` (Automated Tests & Device Verification)**:
   - `./gradlew testDebugUnitTest` passes 100% green.
   - `./gradlew compileDebugAndroidTestKotlin` compiles with 0 errors.
   - Live visual verification on Android device / emulator via ARTEMIS.

---

## 4. Task Dependencies & Execution Wave

- **Wave 1**: Add `GlassEffectStyle`, `GlassColorScheme`, and `Modifier.expoGlassEffect` to [`LiquidGlass.kt`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/ui/glass/LiquidGlass.kt).
- **Wave 2**: Update [`CoupleFitNavigationSuite.kt`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/ui/navigation/CoupleFitNavigationSuite.kt) to use `Modifier.expoGlassEffect` with interactive touch handling and style options.
- **Wave 3**: Verification with `./gradlew testDebugUnitTest` and `./gradlew compileDebugAndroidTestKotlin`.
- **Wave 4**: Live device / emulator deployment and ARTEMIS screenshot / hierarchy capture.
- **Wave 5**: Documentation update (`Fixinglist.md`, `triage.md`, `phases.md`, `INDEX.md`, and report).
- **Wave 6**: Commit, tag release (`v2.9.30-expo-glass-effect-bottom-bar-expo01`), cleanup, and graphify.
