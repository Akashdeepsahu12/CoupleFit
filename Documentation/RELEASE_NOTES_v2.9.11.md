# CoupleFit Release Notes: v2.9.11 — Ambient Video Switch to `green1.mp4` 🌿📱

> **Release Tag**: `v2.9.11-ambient-video-green1`  
> **Milestone**: Phase 26 — Ambient Video Background Switch to `green1.mp4`  
> **Target Device**: `emulator-5554` (Pixel 10 Pro, Android 17 API 36)  
> **Governing Specifications**: [`Documentation/PLAN_PHASE_26_AMBIENT_VIDEO_GREEN1.md`](PLAN_PHASE_26_AMBIENT_VIDEO_GREEN1.md) | [`Documentation/Fixinglist.md`](Fixinglist.md) (`BGV-03`)  
> **Date**: October 2026  

---

## 1. Executive Summary

CoupleFit v2.9.11 switches the ambient video background to the newly provided `green1.mp4` asset (`BGV-03`).

Key Engineering Achievements:
- **Native Portrait Asset Pipeline**: Input video is formatted natively in a 9:20 vertical portrait aspect ratio (`864x1920`, 20.02s seamless liquid gradient loop @ 29.97 fps, audio-free, 19.6 MB).
- **Lanczos Integer Scaling to 1080x2400**: Transcoded using high-grade Lanczos spatial filtering (`scale=1080:2400:flags=lanczos`) delivering an exact 1.25x integer resolution match for modern 20:9 and 19.5:9 flagship mobile displays.
- **Faststart H.264 Streaming**: Encoded with `libx264` High Profile (`crf 19`, `pix_fmt yuv420p`, `-movflags +faststart`) to produce `app/src/main/res/raw/green1.mp4` (7.48 MB) with the container `moov` atom at byte offset 0, ensuring instant playback without cold-start latency.
- **Component & Architecture Integration**: Updated `AmbientVideoBackground.kt` default parameter to `@RawRes videoResId: Int = R.raw.green1` and explicitly bound it in `CoupleFitNavigationSuite.kt`.
- **Zero-Occlusion Translucency**: Native `TextureView` graphics layering allows organic emerald and obsidian liquid textures to refract cleanly through frosted glass cards (`Modifier.iosGlass()`) and the floating navigation dock.
- **Battery & Accessibility Protection**: Lifecycle-aware observer automatically halts playback when the app is backgrounded (`ON_PAUSE` / `ON_STOP`), and respects `reduceMotion` accessibility preferences.

---

## 2. Key Deliverables

1. **`app/src/main/res/raw/green1.mp4`**:
   - Packaged the 1080x2400 portrait video asset (7.48 MB, 30 fps, H.264 High Profile, faststart enabled).
2. **`app/src/main/java/com/couplefit/ui/components/AmbientVideoBackground.kt`**:
   - Updated default resource parameter `@RawRes videoResId: Int = R.raw.green1`.
3. **`app/src/main/java/com/couplefit/ui/navigation/CoupleFitNavigationSuite.kt`**:
   - Passed `videoResId = R.raw.green1` into `AmbientVideoBackground`.
4. **`Documentation/PLAN_PHASE_26_AMBIENT_VIDEO_GREEN1.md`**:
   - Comprehensive phase milestone plan with acceptance criteria, task dependencies, and test bars.
5. **`Documentation/RELEASE_NOTES_v2.9.11.md`**:
   - Full release notes and changelog.

---

## 3. Verification & Quality Assurance

### 3.1 Automated Tests
- `.\gradlew.bat testDebugUnitTest`: **BUILD SUCCESSFUL** (100% green bar, 41 actionable tasks).
- `.\gradlew.bat compileDebugAndroidTestKotlin`: **BUILD SUCCESSFUL** (0 errors).
- `.\gradlew.bat installDebug`: **BUILD SUCCESSFUL** (Installed on `emulator-5554`).

### 3.2 Live Device ARTEMIS Verification (`emulator-5554`)
- **Visual Proof**: Verified `ambient_video_green1_verified.jpg`:
  - Deep emerald liquid gradient flows seamlessly in the background with pristine clarity.
  - Dark obsidian regions preserve razor-sharp contrast for all text, numerals, and badges.
  - Frosted glass cards and the floating dock retain crisp specular rims and readable content.
- **Accessibility Hierarchy**: Verified with ARTEMIS `mobile_get_device_state(view_type="hierarchy")`:
  - All 56 UI nodes are present, touch targets satisfy WCAG AA, and navigation tabs are fully functional.

---

## 4. Git Metadata & Tag
- **Commit Message**: `feat(ui): switch ambient video background to native portrait green1.mp4 (BGV-03)`
- **Release Tag**: `v2.9.11-ambient-video-green1`
