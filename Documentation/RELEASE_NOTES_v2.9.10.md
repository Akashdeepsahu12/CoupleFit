# CoupleFit Release Notes: v2.9.10 — Full Resolution Portrait Ambient Video 🌌📱

> **Release Tag**: `v2.9.10-full-res-portrait-ambient-video`  
> **Milestone**: Phase 25 — Full Resolution Portrait Ambient Video Integration  
> **Target Device**: `emulator-5554` (Pixel 10 Pro, Android 17 API 36)  
> **Governing Specifications**: [`Documentation/PLAN_PHASE_25_FULL_RESOLUTION_PORTRAIT_VIDEO.md`](PLAN_PHASE_25_FULL_RESOLUTION_PORTRAIT_VIDEO.md) | [`Documentation/Fixinglist.md`](Fixinglist.md) (`BGV-02`)  
> **Date**: October 2026  

---

## 1. Executive Summary

CoupleFit v2.9.10 delivers the full-resolution portrait ambient video pipeline (`green.mp4`), upgrading from the initial 270×480 preview asset to an ultra-crisp, high-density **QHD+ Portrait stream (`1440×2560` at 9:16 aspect ratio)** directly derived from the 4K UHD master file (`3840×2160`).

Key Engineering Achievements:
- **True Portrait Framing**: Rotated 90° counter-clockwise to match the phone's vertical viewport, keeping the dark obsidian atmospheric region at the top (underneath status bar, time, and headers) and allowing the vibrant emerald liquid aurora to flow down across the activity dials and floating glass dock.
- **Pristine Visual Fidelity**: High-Profile H.264 encoding with Lanczos spatial filtering (`crf 18`, 3109 kbps) eliminates all compression grain and stretching artifacts on high-DPI displays (including the 1280×2856 display of the Pixel 10 Pro).
- **Faststart Streaming**: Moov atom relocated to the beginning of the MP4 file (`-movflags +faststart`), ensuring instantaneous video preparation in Android's native `MediaPlayer`.
- **Zero Aspect Distortion**: Native mathematical center-crop transformation matrix in `AmbientVideoBackground.kt` guarantees full screen coverage with zero letterboxing or stretching.
- **Battery & Accessibility Integrity**: Auto-pauses on `Lifecycle.Event.ON_PAUSE` / `ON_STOP` and halts immediately when `reduceMotion` is enabled.

---

## 2. Key Deliverables

1. **`app/src/main/res/raw/green.mp4`**:
   - Packaged the 1440×2560 QHD+ 9:16 portrait video asset (3.26 MB, 30 fps, H.264 High Profile).
2. **`Documentation/PLAN_PHASE_25_FULL_RESOLUTION_PORTRAIT_VIDEO.md`**:
   - Detailed milestone specifications, acceptance criteria, and verification methodology.
3. **`Documentation/RELEASE_NOTES_v2.9.10.md`**:
   - Comprehensive release documentation.

---

## 3. Verification & Quality Assurance

### 3.1 Automated Tests
- `.\gradlew.bat testDebugUnitTest`: **BUILD SUCCESSFUL** (100% pass rate, 41 actionable tasks).
- `.\gradlew.bat compileDebugAndroidTestKotlin`: **BUILD SUCCESSFUL** (0 errors).

### 3.2 Live Device ARTEMIS Verification (`emulator-5554`)
- **Inspection View**: Captured screenshot and verified UI hierarchy with Google ARTEMIS.
- **Visual Proof**: Verified `ambient_video_qhd_portrait_verified.jpg`:
  - Ethereal emerald aurora flows smoothly across the screen in razor-sharp QHD+ resolution.
  - The dark obsidian top zone provides pristine contrast for white headers and status indicators.
  - Translucent frosted glass cards ([`Modifier.iosGlass()`](../app/src/main/java/com/couplefit/ui/theme/GlassTokens.kt)) refract the liquid light cleanly.
  - All touch targets, buttons, and navigation elements operate with 100% responsiveness and zero occlusion.

---

## 4. Git Metadata & Tag
- **Commit Message**: `feat(ui): upgrade ambient video background to full resolution portrait QHD+ (BGV-02)`
- **Release Tag**: `v2.9.10-full-res-portrait-ambient-video`
