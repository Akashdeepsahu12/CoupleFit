# CoupleFit — Release v2.0.0

**Release Tag:** `v2.0.0`  
**Version Code:** `2`  
**Version Name:** `2.0.0`  
**Release Date:** October 22, 2026  
**Target Platform:** Android (Native)  
**Target SDK:** 35 (Android 15)  
**Min SDK:** 29 (Android 10)  
**Build Toolchain:** Java 17 / Kotlin 2.1.0 / AGP 8.7.3 / KSP  

---

## 📦 Release Artifacts

- **Release APK:** `CoupleFit-v2.0.0-release.apk` (15.9 MB, R8 minified & resource-shrunk)
- **Release Directory:** `D:\CoupleFit Releases\CoupleFit_v2.0`
- **Release Binaries:** `D:\CoupleFit Releases\CoupleFit_v2.0\release_apk\`

---

## 🚀 Key Highlights & Architectural Changes

### 1. Dual Health Ingestion Engines
- **Health Connect API (Android 14+ / Jetpack):** Differential step counts, workouts, active calories, continuous heart rate, and SpO2 via the `Changes API`.
- **Samsung Health Data API:** Direct fallback pipeline for Samsung Galaxy Watch / Galaxy devices.

### 2. Real-Time Cloud Telemetry & Device Pairing
- **Production Zero-Trust Security:** Zero client tampering with `partnerId` or challenge scores.
- **Bi-Directional Real-Time Linking:** Real-time multi-source listener observing `couples/{coupleId}` and `invites/{code}` with 3.5s heartbeat fallback ensuring the inviter device immediately auto-transitions to the Dashboard when the partner redeems the 6-digit code.
- **Dynamic Session Resolution:** `UserSession.isLinked` dynamically derived so session copies immediately reflect linking status across both devices.
- **Bi-directional Health Sync:** Daily summaries (`couples/{coupleId}/daily/{uid_date}`) with strict UID prefix constraints.

### 3. Rich Interactive UI & Glance Widget
- **Dynamic Activity Rings:** Native Compose Canvas activity rings with micro-animations.
- **Interactive Dual-User Charts:** Bezier-curved activity comparisons between partners.
- **Partner Nudges:** Real-time motivational alerts with system vibration and push notification delivery.
- **Home Screen Widget:** Jetpack Glance app widget (`CoupleFitAppWidget`) auto-updating on health sync.

### 4. Enterprise-Grade Stability & Verification
- **Automated Tests:** 65/65 unit and security contract tests passing cleanly.
- **ProGuard / R8 Optimization:** Strict consumer rules preserving reflection and serialization contracts.
