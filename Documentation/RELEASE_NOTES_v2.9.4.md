# CoupleFit Release Notes — v2.9.4: Live Heart Rate Layout Architecture Polish & Ambient Waveform 💓✨

**Release Version**: `v2.9.4-heart-rate-layout-polish`  
**Git Branch**: `dev_akash`  
**Date**: October 28, 2026  
**Defects Resolved**: `VHR-05`, `DSN-04`, `ENG-06`  
**Design Standard**: `ui-ux-pro-max` & Emil Kowalski Design Engineering

---

## Highlights

1. **Unwrapped LIVE Badge Integrity (`VHR-05`)**:
   - Eliminated vertical text wrapping where `"LIVE"` was squished into a 17px wide strip.
   - Enforced `noWrap = true`, `maxLines = 1`, and flexible header weighting.
2. **Unified Hero Metric Header (`DSN-04`)**:
   - Eliminated duplicate stacked headers between the card and `InteractiveChart`.
   - Unified BPM readout into an authoritative `displaySmall` hero number (`72` or `--` BPM) aligned by baseline.
   - Removed harsh `HorizontalDivider`, restoring liquid frosted glass continuity.
3. **Living Ambient ECG Waveform (`ENG-06`)**:
   - Replaced empty gray boxes with a living, animated cardiogram rhythm canvas and an interactive "Sync Now ↻" trigger.

---

## Verification Summary
- `.\gradlew.bat testDebugUnitTest`: 100% pass rate (32 actionable tasks).
- `.\gradlew.bat compileDebugAndroidTestKotlin`: 100% pass rate (0 errors).
- Live Android Emulator (`emulator-5554`): Verified with ARTEMIS (`live_heart_rate_layout_polished_verified.jpg`).
