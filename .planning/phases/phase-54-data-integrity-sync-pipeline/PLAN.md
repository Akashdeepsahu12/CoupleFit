# Phase 54 Milestone Plan: Data Integrity, Room SSOT & Background Sync Pipeline

**Target Milestone**: v2.9.40  
**Status**: IN EXECUTION  
**Associated Defect IDs**: `DAT-01`, `DAT-04`, `DAT-02`, `DAT-05`, `WRK-01`, `HC-02`, `PERF-01`  

---

## 1. Problem Statement & Findings
The EGM Goldfish audit flagged critical data persistence, synchrony, and background pipeline gaps:
1. **`DAT-01`**: `HealthDao.kt`'s query `SELECT * FROM partnerDaily WHERE dateStr = :dateStr LIMIT 1` lacks deterministic ordering, arbitrarily returning stale or wrong partner records if multiple partner records exist.
2. **`DAT-04`**: In `HealthConnectViewModel.kt`, exceptions during sync are silently swallowed into `HealthConnectUiState.Empty` and `isSyncing = false` without exposing a sync error to the UI, misleading users into believing sync succeeded.
3. **`DAT-02`**: Firestore timestamp parsing assumes numeric epoch values (`(data["lastUpdated"] as? Number)?.toLong()`), failing when Firestore returns `com.google.firebase.Timestamp`.
4. **`DAT-05` / `PERF-01`**: Unsynchronized concurrent Room writes on Firestore snapshot delivery allow out-of-order writes and stale overwrites.
5. **`WRK-01`**: `HealthConnectSyncWorker` is implemented but never enqueued via WorkManager, leaving background synchronization completely dormant.
6. **`HC-02`**: In `HealthConnectManager.kt`, `dataStore.saveChangesToken(...)` is called prior to committing data into Room; a downstream failure permanently loses Health Connect events.

---

## 2. Explicit Acceptance Criteria
- [ ] **AC-54.1 (`DAT-01`)**: `HealthDao.kt` orders date-only partner daily queries by `ORDER BY lastUpdated DESC LIMIT 1`.
- [ ] **AC-54.2 (`DAT-04`)**: `HealthDashboardUiState` and `HealthConnectUiState` expose `syncErrorMessage: String?` and `Error(message)`, surfaced upon network or sync failures.
- [ ] **AC-54.3 (`DAT-02`)**: `PartnerRepositoryImpl.kt` parses `lastUpdated` robustly across `Number`, `Timestamp`, and `String` representations.
- [ ] **AC-54.4 (`DAT-05`, `PERF-01`)**: `PartnerRepositoryImpl.kt` sequences database writes on a dedicated `limitedParallelism(1)` dispatcher with monotonic timestamp verification (`lastUpdated >= existing.lastUpdated`).
- [ ] **AC-54.5 (`WRK-01`)**: `CoupleFitApplication.kt` enqueues unique 15-minute periodic background sync via `WorkManager.enqueueUniquePeriodicWork`.
- [ ] **AC-54.6 (`HC-02`)**: `HealthConnectManager.kt` defers saving `pendingToken` until AFTER Room insertions succeed.
- [ ] **AC-54.7**: All local unit tests pass 100% green (`.\gradlew testDebugUnitTest`); AndroidTest Kotlin compiles with 0 errors; verified on live emulator (`emulator-5554`).

---

## 3. Implementation Tasks & Dependencies
1. **Task 1 (`HealthDao.kt`)**: Add `ORDER BY lastUpdated DESC` on `LIMIT 1` partner queries (`DAT-01`).
2. **Task 2 (`HealthConnectViewModel.kt`)**: Surface `syncErrorMessage` on error rather than silently failing (`DAT-04`).
3. **Task 3 (`PartnerRepositoryImpl.kt`)**: Implement polymorphic timestamp parsing (`DAT-02`) and monotonic sequential DB write fence (`DAT-05`, `PERF-01`).
4. **Task 4 (`CoupleFitApplication.kt`)**: Schedule `HealthConnectSyncWorker` via WorkManager (`WRK-01`).
5. **Task 5 (`HealthConnectManager.kt`)**: Commit Changes API token transactionally only after Room insert (`HC-02`).
6. **Task 6 (Verification & Report)**: Test suite, live emulator verification, and `Documentation/phase54_verification_report.md`.
