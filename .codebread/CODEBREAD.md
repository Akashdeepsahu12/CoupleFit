# CoupleFit System Architecture & Tracing Brief

## Overview
**CoupleFit** is a privacy-first fitness companion Android application designed for couples. It combines local biometric tracking (Google Health Connect & Samsung Health SDKs) with offline-first Room persistence and real-time Cloud Firestore synchronization.

### Key Metrics
- **Files**: 101
- **Functions & Methods**: 366
- **Database Tables & External Boundaries**: 9
- **System Connections**: 1249

---

## 10-Stage API-to-Database Tracing Pipeline
```
1. Sensor Ingestion (Hardware PPG, Accelerometer, SpO2)
   ↓
2. Android Provider SDK (Health Connect / Samsung Health)
   ↓
3. Biometrics Aggregator (HealthConnectManager / SensitiveHealthProvider)
   ↓
4. Room SSOT SQLite Cache (HealthDao -> health_daily_aggregates)
   ↓
5. Background Sync Orchestration (WorkManager SyncWorker)
   ↓
6. Privacy & Metric Sanitization (FirebaseSyncManager -> Strips raw points)
   ↓
7. Cloud API Sync (Cloud Firestore users/{userId}/sharedView/{dateStr})
   ↓
8. Real-time Partner Ingestion (Firestore snapshot listener)
   ↓
9. Partner Room Cache (HealthDao -> partner_daily_aggregates)
   ↓
10. Reactive UI & Glance Widget (Compose Dashboard, Hub, Glance Home Widget)
```

---

## Architecture Layers & Packages
- **Frontend (`ui/`)**: Jetpack Compose glassmorphic UI, `DashboardScreen`, `CoupleHubScreen`, `ChallengesScreen`, `ActivityRing`, `MotionArcGauge`, and ViewModels.
- **Backend (`data/healthconnect/`, `data/samsunghealth/`, `data/remote/`, `service/`, `widget/`)**:
  - `HealthConnectManager`: Local biometrics aggregation and permission controller.
  - `FirebaseSyncManager`: Strict privacy-respecting sync manager.
  - `CoupleLinkManager`: Cryptographic 6-digit invite code pairing.
  - `SyncWorker`: Periodic WorkManager background flush.
  - `CoupleFitAppWidget`: Glance widget for Android home screen.
- **Database (`data/local/`)**:
  - `HealthDatabase` (Room v4): Local offline single-source-of-truth.
  - `HealthDao`: Reactive Flow queries and transaction-safe sync updates.
  - Tables: `health_metrics`, `health_daily_aggregates`, `partner_daily_aggregates`, `partnerdaily`, `couplechallenges`.
- **Config & DI (`di/`)**:
  - Hilt Singleton modules: `DatabaseModule`, `FirebaseModule`, `RepositoryModule`, `NetworkModule`.
