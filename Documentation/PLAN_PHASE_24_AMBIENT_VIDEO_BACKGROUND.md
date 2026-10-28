# Phase 24: Ambient Video Background Integration (`PLAN_PHASE_24_AMBIENT_VIDEO_BACKGROUND.md`) 🎬🌿

## 1. Problem Statement & User Intent
The user requested:
> *"can you apply \"D:\Couple_fit\CoupleFit\green.mp4\" to the app background? you can use mobbin mcp for help"*

### Design & Engineering Analysis (`BGV-01`)
1. **Source Asset Characteristics**:
   - `green.mp4`: Atmospheric, emerald-teal frosted aurora grain motion video (6.00s seamless loop, 30fps H.264, vertical 9:16 mobile aspect ratio, no audio track).
2. **Visual & Architectural Goals**:
   - Bring the app alive with a living, undulating aurora background that refracts through CoupleFit's Liquid Frosted Glass surfaces (`Modifier.iosGlass()`, `Modifier.glassDock()`).
   - Maintain 100% WCAG AA contrast compliance: text, numbers, and activity rings must remain razor-sharp over a cinematic dark obsidian scrim.
3. **Engineering & Battery Constraints**:
   - Zero CPU/battery waste when the app is backgrounded (`ON_PAUSE` / `ON_STOP`).
   - Hardware-accelerated decoding via Android's native `TextureView` + `MediaPlayer` (zero external dependencies, zero APK bloat).
   - Graceful accessibility handling: when `reduceMotion` is toggled ON, freeze or bypass playback.

---

## 2. Architectural Solution
1. **Resource Bundling**:
   - Place `green.mp4` into `app/src/main/res/raw/green.mp4` for standard Android resource resolution via `R.raw.green`.
2. **`AmbientVideoBackground.kt` Implementation**:
   - Dedicated composable in `com.couplefit.ui.components`.
   - Uses `AndroidView` with a `TextureView` for seamless Compose Z-layering and alpha blending.
   - CenterCrop matrix transformation matching viewport dimensions to eliminate letterboxing.
   - Lifecycle observer pausing on `ON_PAUSE` and resuming on `ON_RESUME`.
   - Audio strictly muted (`setVolume(0f, 0f)`), seamless looping (`isLooping = true`).
   - Wrapped with a dual-stage obsidian gradient scrim (`0x4007080A` to `0x7007080A`) for contrast.
3. **Integration Point**:
   - Integrated into `CoupleFitNavigationSuite.kt` behind the 4 validated tabs (`Today`, `Challenges`, `Trends`, `Couple Hub`) and `MainActivity.kt`.
   - Frosted glass cards (`iosGlass`) and the floating navigation dock (`glassDock`) naturally blur and diffuse the moving emerald waves beneath them.

---

## 3. Explicit Acceptance Criteria & Test Bars
- [ ] **AC-1**: `green.mp4` successfully packaged in `app/src/main/res/raw/green.mp4` and builds without resource errors.
- [ ] **AC-2**: `AmbientVideoBackground.kt` renders hardware-accelerated looping video in CenterCrop aspect ratio.
- [ ] **AC-3**: Audio is completely muted and looping is seamless with 0 stutter.
- [ ] **AC-4**: Playback automatically pauses when app goes to background (`ON_PAUSE`), resuming on foreground.
- [ ] **AC-5**: Foreground text, cards, and activity gauges maintain high contrast and 100% legibility over the video.
- [ ] **AC-6**: `.\gradlew.bat testDebugUnitTest` passes 100% green.
- [ ] **AC-7**: `.\gradlew.bat compileDebugAndroidTestKotlin` compiles cleanly with 0 errors.
- [ ] **AC-8**: Verified on live Android emulator `emulator-5554` via Google ARTEMIS with screenshot proof.
- [ ] **AC-9**: Documentation synchronized in `Fixinglist.md`, `triage.md`, `phases.md`, `INDEX.md`, `RELEASE_NOTES_v2.9.9.md`, and `knowledge-graph.md`.
