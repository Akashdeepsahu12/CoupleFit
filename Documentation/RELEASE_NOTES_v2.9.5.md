# CoupleFit Release Notes — v2.9.5: Liqui Design Liquid Glass Heart Rate Redesign & Antislop-UI Polish 💧💓

**Release Version**: `v2.9.5-liqui-design-heart-rate`  
**Git Branch**: `dev_akash`  
**Date**: October 28, 2026  
**Reference Specification**: [leefanv/liqui-design](https://github.com/leefanv/liqui-design)  
**Defects Resolved**: `LQ-01`, `LQ-02`, `AS-01`  
**Design Standard**: `antislop:antislop-ui` & Liqui Design Material Architecture

---

## Highlights

1. **Liqui Design 4-Layer Optical Model (`LQ-01`)**:
   - Ported official optical tokens (`LiquiTokens.kt`) from `leefanv/liqui-design`.
   - Applied `Modifier.liquiGlass()` featuring hardware Gaussian blur, 2-stop optical tint wash (`0x9E1E202E` to `0x66121420`), directional specular rim light falloff (`0x80FFFFFF` to `0x1EFFFFFF`), and 22dp squircle superellipse curvature.
2. **Two-Material Tactile Controls (`LQ-02`)**:
   - Upgraded "Sync Health Connect" into a solid, tactile action button in `VitalsAccent`, separating optical glass viewing surfaces from value-bearing controls.
3. **Antislop-UI Hierarchy Polish (`AS-01`)**:
   - Eliminated generic AI `VITALS PULSE` eyebrow badge duplication; elevated `Heart Rate` to direct title with vector heart glyph.
   - Replaced endless pulsing studio recording dot with a calm, tactile `Health Connect` / `Live Telemetry` state pill.

---

## Verification Summary
- `.\gradlew.bat testDebugUnitTest`: 100% pass rate (32 actionable tasks).
- `.\gradlew.bat compileDebugAndroidTestKotlin`: 100% pass rate (0 errors).
- Live Android Emulator (`emulator-5554`): Verified with ARTEMIS (`liqui_design_heart_rate_verified.jpg`).
