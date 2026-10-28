# CoupleFit Release Notes: v2.9.12 — Dynamic Ambient Video Background Shuffle 🌌🔀

> **Release Tag**: `v2.9.12-ambient-video-shuffle`  
> **Milestone**: Phase 27 — Dynamic Ambient Video Background Shuffle & Extensible Registry  
> **Target Device**: `emulator-5554` (Pixel 10 Pro, Android 17 API 36)  
> **Governing Specifications**: [`Documentation/PLAN_PHASE_27_AMBIENT_VIDEO_SHUFFLE.md`](PLAN_PHASE_27_AMBIENT_VIDEO_SHUFFLE.md) | [`Documentation/Fixinglist.md`](Fixinglist.md) (`BGV-04`)  
> **Date**: October 2026  

---

## 1. Executive Summary

CoupleFit v2.9.12 delivers the **Dynamic Ambient Video Background Shuffle** system (`BGV-04`), adding a new high-fidelity sapphire liquid background ([`blue.mp4`](../app/src/main/res/raw/blue.mp4)) and an extensible, decoupled registry architecture ([`AmbientVideoRegistry.kt`](../app/src/main/java/com/couplefit/ui/components/AmbientVideoRegistry.kt)) that shuffles backgrounds dynamically across app launches while allowing future background additions with a single line of code.

Key Engineering Achievements:
- **`blue.mp4` Portrait Transcoding Pipeline**: Source 1920×1080 (16:9, 54 fps, 93.7 MB) video rotated 90° clockwise (`transpose=1`) and scaled via Lanczos spatial filtering (`scale=1080:2400:flags=lanczos`) into a native 9:20 portrait stream (`R.raw.blue`, 8.15 MB, 91% size reduction). Clockwise rotation places dark obsidian/navy regions at the top for header contrast and luminous sapphire waves across the bottom.
- **Extensible Ambient Video Registry (`AmbientVideoRegistry.kt`)**: Decoupled catalog defining metadata (`id`, `resId`, `displayName`, `description`, `dominantAccent`) and an active shuffle pool (`activeShufflePool = listOf(GREEN1, BLUE)`). Future backgrounds can be added simply by registering them in the pool.
- **Non-Repeating Shuffle on Cold Start**: On app creation in [`MainActivity.kt`](../app/src/main/java/com/couplefit/MainActivity.kt), reads `last_ambient_bg_id` and selects randomly from the pool excluding the last ID, guaranteeing non-repeating alternation (`blue` -> `green1` -> `blue`) across sessions.
- **Mid-Session Stability**: Background choice is bound to session state with `rememberSaveable { ... }`, preventing jarring video reloads or switches during tab navigation.
- **Hardware TextureView & Glass Optics**: Translucent frosted glass cards ([`Modifier.iosGlass()`](../app/src/main/java/com/couplefit/ui/theme/GlassTokens.kt)) and floating navigation dock diffuse sapphire blue and emerald green liquid light seamlessly with zero window punch-through.

---

## 2. Key Deliverables

1. **`app/src/main/res/raw/blue.mp4`**:
   - Transcoded 1080×2400 30fps faststart sapphire video asset (8.15 MB, H.264 High Profile).
2. **`app/src/main/java/com/couplefit/ui/components/AmbientVideoRegistry.kt`**:
   - Extensible registry, asset data contracts, catalog, active pool, and non-repeating shuffle selection algorithm.
3. **`app/src/main/java/com/couplefit/ui/navigation/CoupleFitNavigationSuite.kt`**:
   - Parameterized `@RawRes ambientVideoResId` with registry default fallback.
4. **`app/src/main/java/com/couplefit/MainActivity.kt`**:
   - SharedPreferences-backed non-repeating shuffle state with `rememberSaveable` session stability.
5. **`app/src/test/java/com/couplefit/ui/AmbientVideoRegistryTest.kt`**:
   - Complete unit test suite verifying registry integrity, ID lookups, non-repeating shuffle logic, and edge case fallbacks.
6. **`Documentation/PLAN_PHASE_27_AMBIENT_VIDEO_SHUFFLE.md`**:
   - Detailed milestone specifications, acceptance criteria, and verification methodology.
7. **`Documentation/RELEASE_NOTES_v2.9.12.md`**:
   - Comprehensive release documentation.

---

## 3. Verification & Quality Assurance

### 3.1 Automated Tests
- `.\gradlew.bat testDebugUnitTest`: **BUILD SUCCESSFUL** (100% pass rate, 41 actionable tasks).
- `.\gradlew.bat compileDebugAndroidTestKotlin`: **BUILD SUCCESSFUL** (0 errors).
- `AmbientVideoRegistryTest`: 5 unit tests verifying initial pool, case-insensitive ID lookups, non-repeating shuffle, and pool fallbacks.

### 3.2 Live Device ARTEMIS Verification (`emulator-5554`)
- **Inspection View**: Captured screenshot and verified UI hierarchy with Google ARTEMIS.
- **Visual Proof**: Verified `ambient_video_blue_verified.jpg`:
  - Deep sapphire blue liquid background renders smoothly beneath frosted glass cards and floating dock.
  - White text in header zone maintains 100% legibility over dark obsidian/navy top region.
  - Multi-session restarts verified via ADB SharedPreferences:
    - Session 1: `last_ambient_bg_id = blue`
    - Session 2: `last_ambient_bg_id = green1`
    - Session 3: `last_ambient_bg_id = blue`

---

## 4. Git Metadata & Tag
- **Commit Message**: `feat(ui): dynamic ambient video background shuffle with extensible registry (BGV-04)`
- **Release Tag**: `v2.9.12-ambient-video-shuffle`
