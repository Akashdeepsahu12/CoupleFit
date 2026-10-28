# CoupleFit Release Notes: v2.9.9 — Ambient Video Background 🌌🌿

> **Release Tag**: `v2.9.9-ambient-video-background`  
> **Milestone**: Phase 24 — Atmospheric Ambient Motion Video Background Integration  
> **Target Device**: `emulator-5554` (Pixel 10 Pro, Android 17 API 36)  
> **Governing Specifications**: [`Documentation/PLAN_PHASE_24_AMBIENT_VIDEO_BACKGROUND.md`](PLAN_PHASE_24_AMBIENT_VIDEO_BACKGROUND.md) | [`Documentation/Fixinglist.md`](Fixinglist.md) (`BGV-01`)  
> **Date**: October 2026  

---

## 1. Executive Summary

CoupleFit v2.9.9 introduces an atmospheric, hardware-accelerated ambient liquid motion video background (`green.mp4`), transforming the app canvas from static darkness into an ethereal, living aurora that diffuses subtly through translucent frosted glass cards (`Modifier.iosGlass()`, `Modifier.glassDock()`) and the floating navigation dock.

The system is engineered for maximum performance, battery conservation, and accessibility:
- **Zero Aspect Distortion**: Implements dynamic 2D center-crop scaling matrix with center pivot `(width / 2, height / 2)` across arbitrary screen resolutions.
- **Compose Layering Harmony**: Utilizes native `TextureView` embedded via `AndroidView` to participate directly in the Compose graphics layer hierarchy, preventing window-level hole punching associated with legacy `SurfaceView`.
- **Battery & CPU Conservation**: Automatically pauses playback on `Lifecycle.Event.ON_PAUSE` / `ON_STOP` and resumes on `ON_RESUME`, completely eliminating background CPU and GPU draw.
- **Accessibility Motion Guard**: Bypasses or freezes video playback when `reduceMotion` is active.

---

## 2. Key Deliverables & Architectural Enhancements

### 2.1 Video Asset Packaging (`R.raw.green`)
- Packaged the optimized 6.00s H.264 video (`green.mp4`, 270x480 @ 30fps, 4490kbps silent loop) into `app/src/main/res/raw/green.mp4`.
- Extracted preview artifact [`green_preview.jpg`](green_preview.jpg) confirming emerald-teal atmospheric liquid aurora aesthetics.

### 2.2 `AmbientVideoBackground.kt` Component
- Built reusable Compose container wrapping Android `TextureView` and silent looping `MediaPlayer`.
- Implemented `adjustAspectRatio(textureView, videoWidth, videoHeight, viewWidth, viewHeight)` calculating scale factor `max(viewWidth / videoWidth, viewHeight / videoHeight)` applied via `android.graphics.Matrix`.
- Bound to `LocalLifecycleOwner.current.lifecycle` with `LifecycleEventObserver`.

### 2.3 Navigation Shell Integration (`CoupleFitNavigationSuite.kt`)
- Embedded `AmbientVideoBackground` at the root `Box` of the navigation suite behind both compact `Scaffold` and expanded `Row` layouts.
- Wired `reduceMotion` parameter through from `MainActivity.kt`'s user accessibility preferences.

---

## 3. Verification & Quality Assurance

### 3.1 Automated Tests
- `.\gradlew.bat testDebugUnitTest`: **BUILD SUCCESSFUL** (100% green bar, 41 actionable tasks).
- `.\gradlew.bat compileDebugAndroidTestKotlin`: **BUILD SUCCESSFUL** (0 errors).

### 3.2 Live Device ARTEMIS Verification (`emulator-5554`)
- **Inspection View**: Captured screenshot and verified UI hierarchy with Google ARTEMIS.
- **Visual Proof**: Verified `ambient_video_background_verified.jpg`:
  - Emerald liquid aurora video background continuously loops behind resting glass cards.
  - Specular bevels and translucent slate cards refract the ambient glow cleanly.
  - Zero letterboxing or stretching on 1280x2856 display.
  - All text, metrics, and navigation dock items maintain full contrast and touch target integrity.

---

## 4. Git Metadata & Tag
- **Commit Message**: `feat(ui): apply ambient green aurora video background with lifecycle awareness (BGV-01)`
- **Release Tag**: `v2.9.9-ambient-video-background`
