# CoupleFit 🏃‍♂️❤️🏃‍♀️

**CoupleFit** is a modern, privacy-first, offline-first Android application designed for couples to track fitness, close activity rings, conquer challenges, and celebrate health milestones together in real time.

Built with **Jetpack Compose**, **Health Connect**, **Room (SSOT)**, **Jetpack Glance Widgets**, and **Firebase** (Auth, Firestore, Cloud Functions, FCM, Crashlytics, Analytics, Performance Monitoring, Remote Config).

---

## 📱 Features

- **Dual Activity Rings (Apple Fitness / iOS 17 Style)**: Side-by-side rings for Steps, Active Minutes, and Calories Burned for both you and your partner.
- **Biometric Telemetry**: Real-time sync of Heart Rate (Bézier curves) and Oxygen Saturation ($SpO_2$) alongside daily aggregate vitals.
- **Seamless 6-Digit Pairing & Deep Links**:
  - Generate a secure 6-digit invite code (`100000..999999`) or share verified Android App Links (`https://couplefit.app/invite/{code}` & `couplefit://invite`).
  - One-tap clipboard pasting, automatic code redemption, and instant demo mode for testing.
- **Zero-Trust Cloud Architecture**: Server-authoritative Cloud Functions enforce couple membership (strictly max 2 members), invite code expiration (24h), rate limits, and milestone scoring.
- **Couple Challenges**:
  - Compete in Steps Races or Active Minute Streaks.
  - Server-side hourly cron evaluation automatically computes scores and declares winners.
- **Real-Time Nudges & Milestone Celebrations**:
  - High-priority FCM push notifications when your partner cheers you on (rate-limited to 1 per 5 mins).
  - Milestone triggers celebrate 10k steps, closed rings, and streaks once per day with zero duplicates.
- **Jetpack Glance Home-Screen Widget**:
  - Live home-screen widget displaying dual activity rings.
  - **Zero-Network Contract**: Reads strictly from the local Room database; updated automatically by background sync workers.
- **Comprehensive Privacy Controls**:
  - Per-metric sharing toggles (Steps, Workouts, Calories, Heart Rate, SpO2).
  - Full GDPR/CCPA data deletion (`deleteUserData`) and unlinking flows.
  - Local database secured by Android hardware-backed File-Based Encryption (FBE).

---

## 🏗️ Architecture & Data Flow

CoupleFit adheres strictly to **Modern Android Architecture (Clean Architecture + MVVM + UDF)** with an **Offline-First Single Source of Truth (SSOT)**:

```
+-----------------------------------------------------------------------------------+
|                                  CoupleFit Client                                 |
|                                                                                   |
|  +--------------------+        +---------------------+        +----------------+  |
|  |   Health Connect   |  --->  |    Room Database    |  <---  | Jetpack Glance |  |
|  |   (Changes API)    |        |   (Local SSOT DB)   |        |  (Home Widget) |  |
|  +--------------------+        +----------+----------+        +----------------+  |
|                                           |                                       |
|                                           v                                       |
|                              +------------------------+                           |
|                              |      Sync Worker       |                           |
|                              | (Foreground / 15-Min)  |                           |
|                              +------------+-----------+                           |
+-------------------------------------------|---------------------------------------+
                                            | HTTPS / TLS 1.3
                                            v
+-----------------------------------------------------------------------------------+
|                             Firebase Backend Services                             |
|                                                                                   |
|  +---------------------+        +--------------------+        +----------------+  |
|  | Cloud Firestore     |  --->  | Cloud Functions    |  --->  | Firebase Cloud |  |
|  | Security Rules      |        | Server Authority   |        | Messaging (FCM)|  |
|  | (Owner-only writes) |        | (Scoring & Invites)|        | (Push Nudges)  |  |
|  +---------------------+        +--------------------+        +----------------+  |
+-----------------------------------------------------------------------------------+
```

1. **Hardware Ingestion**: `HealthConnectManager` uses the Health Connect Changes API to incrementally ingest steps, active minutes, calories, heart rate, and $SpO_2$. Wear OS and phone step counts are deduplicated hardware-side.
2. **Local Persistence (SSOT)**: Ingested metrics are committed into the Room database (`HealthDatabase`). The UI and Glance Widget observe Room `Flow` queries and never block on network requests.
3. **Background Synchronization**: `HealthConnectSyncWorker` and `SyncWorker` upload user aggregates to Firestore at most once every 15 minutes (or on app foreground), and update the Glance widget.
4. **Cloud Security Rules**: Enforce that users can only write to their own daily aggregate documents (`couples/{coupleId}/daily/{myUid_date}`). Client apps cannot write directly to couple membership arrays or invite states.

---

## 🛠️ Tech Stack & Dependencies

| Category | Technology | Version | Purpose |
| :--- | :--- | :--- | :--- |
| **Language** | Kotlin | `2.1.0` | Primary language across client code and domain logic |
| **Build Tooling** | AGP & Gradle | `8.7.3` / `8.11.1` | Android Gradle Plugin with Version Catalog (`libs.versions.toml`) |
| **Compiler** | KSP & Compose Compiler | `2.1.0-1.0.29` | Kotlin Symbol Processing replacing kapt |
| **SDK Levels** | Android SDK | `compileSdk 35` / `targetSdk 35` / `minSdk 29` | Android 15 ready, universal File-Based Encryption baseline |
| **UI Toolkit** | Jetpack Compose | BOM `2024.12.01` | Declarative UI, Material 3, micro-animations, adaptive layouts |
| **Home Widget** | Jetpack Glance | `1.1.1` | Native home-screen widget powered by Glance Material 3 |
| **Dependency Injection**| Dagger Hilt | `2.53.1` | Compile-time dependency injection across ViewModels & Workers |
| **Local Database** | Room Database | `2.6.1` (`room-ktx`) | Offline-first SQLite persistence with reactive Kotlin Flow |
| **Health APIs** | Health Connect SDK | `1.1.0-alpha10` | Standardized health data access with incremental Changes API |
| **Cloud Services** | Firebase BOM | `33.7.0` | Auth, Firestore, Cloud Functions, FCM, Crashlytics, Analytics, Perf |
| **Preferences** | DataStore Preferences | `1.1.1` | Coroutine-based asynchronous key-value persistence |
| **Background Tasks** | WorkManager | `2.10.0` | Periodic 15-minute telemetry sync and widget refresh |

---

## 🚀 Getting Started & Setup

### Prerequisites
- **JDK 17** (e.g. OpenJDK 17 or Temurin 17)
- **Android Studio** Ladybug (2024.2.1+) or newer
- Android Device or Emulator running **Android 10+ (API 29+)** with Google Play Services and Health Connect installed

### 1. Clone & Open
```bash
git clone https://github.com/your-org/couplefit.git
cd couplefit/CoupleFit
```

### 2. Firebase Configuration
1. Place your `google-services.json` inside the `app/` directory:
   ```
   CoupleFit/app/google-services.json
   ```
2. Enable the required Firebase services in [Firebase Console](https://console.firebase.google.com):
   - **Authentication**: Enable Google Sign-In and Anonymous Authentication. Add your Web Client ID to `strings.xml` or `FirebaseAuthManager.DEFAULT_WEB_CLIENT_ID`.
   - **Cloud Firestore**:
     - Navigate to **Build > Firestore Database > Create database**.
     - Choose your database location and select **Start in test mode** (or apply the project's [firestore.rules](firestore.rules)).
   - **Cloud Messaging (FCM)**: Enable FCM for partner nudges.
   - **Cloud Functions**: Deploy the backend functions:
     ```bash
     cd functions
     npm install
     npm run deploy
     ```

### 3. Build & Run
```bash
# Run unit tests (all 63 automated tests)
./gradlew testDebugUnitTest

# Build debug APK
./gradlew assembleDebug

# Build release APK (R8 minified with Baseline Profiles)
./gradlew assembleRelease
```

---

## 👥 Partner Pairing Guide

1. **Device A (Inviter)**:
   - Signs in and navigates to **Invite Your Partner**.
   - An easy-to-read **6-digit pairing code** (e.g., `709967`) is automatically generated.
   - Tap **Copy Code** or **Share Link** (`https://couplefit.app/invite/709967`).
2. **Device B (Partner)**:
   - Taps the shared link or navigates to **Enter Invite Code**.
   - Enters the 6 digits (or uses the **⚡ Use [Code]** quick-fill button).
   - Taps **Connect with Partner** — both accounts are linked instantly!
3. **Demo / Offline Mode**:
   - If Cloud Firestore is not yet provisioned, you can tap **Test Demo Link** on either device to preview the paired celebration, dual dashboard rings, Glance widgets, and nudges immediately.

---

## 🧪 Testing & Verification

CoupleFit includes an automated test suite verifying security contracts, link managers, Room migrations, and Health Connect workflows:

```bash
# Execute all unit tests
./gradlew testDebugUnitTest

# Lint audit
./gradlew lintDebug
```

Key test suites:
- `CoupleLinkingSecurityTest.kt`: Enforces zero-trust rules, 2-member couple caps, and invite expiration.
- `SecurityContractTest.kt`: Validates Firestore ownership checks and docId prefix requirements.
- `HealthDaoPartnerTest.kt`: Verifies Room caching of partner metrics and migrations.
- `Phase6PrivacyAndReadinessTest.kt`: Confirms permission minimization (read-only steps, no `WRITE_STEPS`).
- `DashboardComposeUiTest.kt`: UI test verifying dual-ring rendering and metric card states.

---

## 📚 Project Documentation

All comprehensive architectural and operational documentation has been organized into the [`Documentation/`](Documentation/) directory:

- [**Documentation Hub & Index**](Documentation/INDEX.md)
- [**Antigravity Skills Directory**](Documentation/skillscommand.md)
- [**Implementation Phases & Milestones**](Documentation/phases.md)
- [**Tech Stack & Architecture Review**](Documentation/techstackreview.md)
- [**Release Notes v2.0.1**](Documentation/RELEASE_NOTES_v2.0.1.md)
- [**Release Notes v2.0.0**](Documentation/RELEASE_NOTES_v2.0.0.md)
- [**Transformation Changelog**](Documentation/CHANGELOG_TRANSFORMATION.md)

---

## 📄 License & Privacy

- **Privacy Policy**: CoupleFit requests only the health permissions strictly required for the dashboard. Intraday raw sensor waveforms, GPS routes, and toggled-off metrics never leave your device.
- Full technical documentation is available in [Documentation/phases.md](Documentation/phases.md) and [Documentation/techstackreview.md](Documentation/techstackreview.md).