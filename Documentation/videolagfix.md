# Phase 39 Milestone Plan: Ambient Video Lag Elimination & High-Performance Playback Architecture (`VID-01`) 🚀🎥

> **Associated Defect / Issue ID**: `VID-01` (`VID-01` to `VID-04` in [`Fixinglist.md`](file:///d:/Couple_fit/CoupleFit/Documentation/Fixinglist.md))  
> **Target Subsystems**: [`AmbientVideoBackground.kt`](file:///d:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/ui/components/AmbientVideoBackground.kt), [`AmbientVideoRegistry.kt`](file:///d:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/ui/components/AmbientVideoRegistry.kt), [`res/raw/`](file:///d:/Couple_fit/CoupleFit/app/src/main/res/raw), [`app/build.gradle.kts`](file:///d:/Couple_fit/CoupleFit/app/build.gradle.kts), [`gradle/libs.versions.toml`](file:///d:/Couple_fit/CoupleFit/gradle/libs.versions.toml)  
> **Status**: 📋 **Planned & Ready for Execution**

---

## 1. Executive Summary & Root Cause Analysis

In **CoupleFit**, the ambient video background provides a living, organic backdrop across the entire application interface ([`MainActivity.kt`](file:///d:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/MainActivity.kt#L162)). Users have observed intermittent frame drops, jitter, and perceived lag during background playback and concurrent UI scrolling/navigation.

### Root Causes Identified:
1. **Raw Video Stream Weight & Profiling (`VID-01`)**:
   - The raw MP4 video assets in `app/src/main/res/raw/` (`blue.mp4` ~8.55 MB, `green.mp4` ~3.34 MB, `green1.mp4` ~7.66 MB) exceed ideal mobile background telemetry budgets.
   - High resolutions (1080p/4K), variable frame rates (60fps), high keyframe intervals (GOP), and redundant embedded audio channels force mobile hardware decoders to demux and decode heavy packet buffers unnecessarily.
2. **Legacy `MediaPlayer` Decoder Stutter on Loop (`VID-02`)**:
   - [`AmbientVideoBackground.kt`](file:///d:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/ui/components/AmbientVideoBackground.kt#L167) relies on Android's legacy `android.media.MediaPlayer` with `isLooping = true`.
   - Android's native `MediaPlayer` tears down and re-buffers the decoder pipeline on reaching EOF, resulting in a visible 100ms–250ms freeze/hiccup on every loop cycle.
3. **`TextureView` Window Composition Overhead (`VID-03`)**:
   - `TextureView` decodes frames into the application's OpenGLES/Vulkan graphics pipeline, producing an extra memory copy per frame.
   - When combined with real-time blur/glass effects (e.g. Chris Banes Haze sampling on [`CoupleFitNavigationSuite.kt`](file:///d:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/ui/navigation/CoupleFitNavigationSuite.kt)), frame times can exceed the 16.6ms (60Hz) or 8.3ms (120Hz) frame budgets.
4. **Lifecycle & Playback State Desynchronization (`VID-04`)**:
   - `SurfaceTexture` lifecycle recreations and continuous playback during background navigation/dialog popups lead to unnecessary GPU contention and thread starvation.

---

## 2. Milestone Deliverables & Technical Architecture

```
+-----------------------------------------------------------------------------------------+
|                                    CoupleFit UI Window                                  |
|                                                                                         |
|   +---------------------------------------------------------------------------------+   |
|   |                      Foreground Compose UI & Haze Frosted Glass                 |   |
|   |         (Floating Glass Dock, Side-by-Side Activity Cards, Telemetry HUD)        |   |
|   +----------------------------------------+----------------------------------------+   |
|                                            | Overlays with semi-translucent scrim       |
|                                            v                                            |
|   +---------------------------------------------------------------------------------+   |
|   |                           AmbientVideoBackground (v2.0)                         |   |
|   |                                                                                 |   |
|   |   +--------------------------+                     +------------------------+   |   |
|   |   |  AndroidX Media3 ExoPlayer |  --------------->  |  TextureView / Surface |   |   |
|   |   |  (Pre-buffered Zero-Gap) |                     |  (Aspect-Fill Center)  |   |   |
|   |   +--------------------------+                     +------------------------+   |   |
|   |                ^                                                                |   |
|   +----------------|----------------------------------------------------------------+   |
|                    | Streams lightweight 720p 24fps audio-free stream                   |
|   +----------------+----------------------------------------------------------------+   |
|   |                  Optimized Assets: blue.mp4, green.mp4, green1.mp4              |   |
|   |             (H.264 Baseline, Level 3.1, 1.2 Mbps, GOP=24, No Audio)             |   |
|   +---------------------------------------------------------------------------------+   |
+-----------------------------------------------------------------------------------------+
```

### Deliverable 1: Media3 ExoPlayer Migration with Seamless Buffer Looping (`VID-02`)
- Migrate `AmbientVideoBackground.kt` from `android.media.MediaPlayer` to `androidx.media3:media3-exoplayer`.
- Configure `ExoPlayer.Builder` with:
  - `Player.REPEAT_MODE_ALL` for seamless, hardware-accelerated zero-gap loop wrapping.
  - Video-only track selection (disabling audio renderers completely to save CPU cycles).
  - Power-efficient buffer allocations (`DefaultLoadControl` tuned for local raw assets).

### Deliverable 2: Asset Transcoding & Re-Encoding Pipeline (`VID-01`)
- Standardize all MP4 assets in `app/src/main/res/raw/` to mobile background video specifications:
  - **Resolution**: Exactly `720x1280` portrait (or `720x1560` matching native 9:19.5/9:20 aspect ratio).
  - **Framerate**: Constant `24 fps` or `30 fps` (strictly avoiding 60 fps for ambient backgrounds).
  - **Bitrate**: Target average bitrate between `1000 kbps` and `1500 kbps` (maxrate 1800 kbps).
  - **Audio Channel**: Completely stripped (`-an`) so no audio decoders or synchronization clocks are spawned.
  - **GOP (Group of Pictures)**: Fixed keyframe interval every 24 frames (`-g 24 -keyint_min 24 -sc_threshold 0`) to guarantee instant loop resets.
  - **Profile**: H.264 Baseline / Main profile at Level 3.1 for universal hardware decoder acceleration.

### Deliverable 3: TextureView & Compose Render Layer Optimization (`VID-03`)
- Optimize `SurfaceTextureListener` in `AmbientVideoBackground.kt`:
  - Retain `TextureView` for seamless composition under Compose glass layers and dialogs, but disable alpha blending where opaque.
  - Add frame pacing throttles and ensure `adjustAspectRatio` matrix calculation executes only upon dimension changes.
  - Support automatic playback throttle or pause during heavy dialog presentations or power saver mode.

### Deliverable 4: Strict Energy & Reduced Motion Guardrails (`VID-04`)
- Synchronize with `LocalLifecycleOwner` to immediately release or pause decoder resources on `ON_PAUSE` / `ON_STOP`.
- Honor `reduceMotion` user setting and Android OS animator duration scale: when active, instantly settle video alpha to 0f / static obsidian base and pause playback.

---

## 3. Step-by-Step Implementation Plan

### Step 1: Add Media3 ExoPlayer Dependencies
Update `gradle/libs.versions.toml` and `app/build.gradle.kts`:
- Add `androidx-media3-exoplayer = { group = "androidx.media3", name = "media3-exoplayer", version.ref = "media3" }`
- Add `androidx-media3-ui = { group = "androidx.media3", name = "media3-ui", version.ref = "media3" }`

### Step 2: Transcode Ambient Video Assets
Optimize `blue.mp4`, `green.mp4`, and `green1.mp4` using optimized FFmpeg configurations:
```bash
ffmpeg -i app/src/main/res/raw/green1.mp4 -vf "scale=720:1560:force_original_aspect_ratio=decrease,pad=720:1560:(ow-iw)/2:(oh-ih)/2" -c:v libx264 -profile:v baseline -level 3.1 -preset veryslow -b:v 1400k -maxrate 1800k -bufsize 2800k -g 24 -keyint_min 24 -sc_threshold 0 -an -movflags +faststart app/src/main/res/raw/green1_opt.mp4
```

### Step 3: Refactor `AmbientVideoBackground.kt`
- Replace `MediaPlayer` initialization with `ExoPlayer.Builder(context).build()`.
- Set `repeatMode = Player.REPEAT_MODE_ALL`.
- Attach `ExoPlayer.setVideoTextureView(textureView)`.
- Implement listener for `onRenderedFirstFrame` to drive the smooth 1200ms bloom fade-in.
- Ensure proper teardown with `player.release()` in `DisposableEffect`.

### Step 4: Verification & Quality Assurance
- Run `./gradlew.bat testDebugUnitTest` to ensure all existing test suites remain 100% green.
- Verify memory consumption and CPU usage on live Android emulator / device.
- Audit frame rendering consistency across loop cycles (0 dropped frames at loop point).

---

## 4. Acceptance Criteria & Definition of Done

1. **Zero-Gap Looping**: Video loops continuously without any pause, black flash, or micro-stutter at the loop boundary.
2. **Payload Footprint Reduction**: Total raw video asset size in `res/raw` reduced by $\ge 50\%$.
3. **Frame Rate Stability**: Video plays smoothly at a steady 24/30 fps with $<1\%$ CPU decoder load.
4. **Seamless Frosted Glass Rendering**: Liquid Glass dock and cards over the video retain full translucent depth and refraction without jank during scrolling.
5. **Regression-Free Build**: Clean build with zero compilation errors, zero lint warnings, and 100% passing tests.
