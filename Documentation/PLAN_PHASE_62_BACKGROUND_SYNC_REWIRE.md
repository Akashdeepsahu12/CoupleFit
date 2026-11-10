# Phase 62: Background Synchronization Pipeline Rewire & Recent Partner Telemetry Fallback (`SYNC-02`, `WRK-02`, `PTR-03`) 💎⚙️

> **Governing Standards**: [`AGENTS.md`](file:///D:/Couple_fit/CoupleFit/AGENTS.md)  
> **Master Defect Tracker**: [`Documentation/Fixinglist.md`](file:///D:/Couple_fit/CoupleFit/Documentation/Fixinglist.md)  
> **Target Release**: `v2.9.48-background-sync-rewire`

---

## 1. Problem Statement & Root Cause Analysis

The user reported:
> *"Rewire background"*

Following the deep-dive diagnostic findings in `SYNC-02`, three critical architectural gaps exist in the background data pipeline:

1. **Dead & Divergent `SyncWorker.kt` Architecture (`WRK-02`)**:
   - `SyncWorker.kt` currently delegates to `healthRepository.syncWithPartner()`, which uses hardcoded dummy strings (`userId = "current_user"`, `partnerId = "partner_user"`) and attempts to write to `users/{userId}/sharedView` via legacy `FirebaseSyncManager`.
   - Firestore security rules strictly require `couples/{coupleId}/daily/{uid_date}`, meaning `SyncWorker` operations are unauthorized and fail.
   - `SyncWorker` never pulls incremental step updates from Health Connect via the Changes API, never uploads to the verified `couples/{coupleId}/daily/{myUid_date}` path, and never pulls the partner's latest remote aggregate.

2. **Unscheduled Background Work in Application Entry Point (`WRK-03`)**:
   - Neither `SyncWorker` nor `HealthConnectSyncWorker` was enqueued as a recurring periodic task in `CoupleFitApplication.onCreate()`.
   - Consequently, background synchronization never executed autonomously while the app was closed or device was idle.

3. **Timezone & Midnight Boundary Blank Gauge Gap (`PTR-03`)**:
   - `HealthConnectViewModel` and `HealthDao` only observe the exact current local date string (`LocalDate.now()`).
   - When the user's phone crosses midnight (e.g. 12:00 AM IST) or when the partner is located in a later time zone (where it is still the previous calendar day), no document exists for today's date yet.
   - The UI immediately dropped partner steps to 0 with `"Awaiting sync"` rather than gracefully displaying the partner's most recent verified activity with contextual recency metadata (e.g., `"Yesterday • 8,420 steps"`).

---

## 2. Milestone Deliverables

### A. Background `SyncWorker` Modernization (`WRK-02`)
- In [`SyncWorker.kt`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/service/SyncWorker.kt):
  - Inject `HealthConnectManager`, `CoupleFitPreferencesDataStore`, `HealthDao`, `SyncRepository`, `PartnerRepository`, and `FirebaseAuthManager`.
  - In `doWork()`:
    1. **Health Connect Pull**: Call `healthConnectManager.syncViaChangesApi(dataStore, healthDao)` to ensure Room SSOT has the latest local sensor steps.
    2. **Session Verification**: Retrieve current user session from `authManager.getCurrentSession()`.
    3. **Remote Upload**: If `session.isLinked && session.coupleId != "couple_1"`, call `syncRepository.uploadMyDailyAggregate(coupleId, myUid, dateStr, force = true)`.
    4. **Remote Partner Ingestion**: Call `partnerRepository.syncPartnerDailyFromRemote(coupleId, partnerId, dateStr)`. If today's document is null, automatically check yesterday's date (`minusDays(1)`).
    5. **Glance Widget Refresh**: Call `CoupleFitAppWidget().updateAll(applicationContext)`.

### B. Recurring Periodic WorkManager Scheduling (`WRK-03`)
- In [`CoupleFitApplication.kt`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/CoupleFitApplication.kt):
  - Configure a 15-minute `PeriodicWorkRequest` for `SyncWorker`:
    ```kotlin
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()
    val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
        .setConstraints(constraints)
        .build()
    WorkManager.getInstance(this).enqueueUniquePeriodicWork(
        "CoupleFitSyncWorker",
        ExistingPeriodicWorkPolicy.KEEP,
        syncRequest
    )
    ```

### C. `HealthRepositoryImpl.kt` Alignment (`SYNC-03`)
- In [`HealthRepositoryImpl.kt`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/data/repository/HealthRepositoryImpl.kt):
  - Inject `SyncRepository`, `PartnerRepository`, and `CoupleFitPreferencesDataStore`.
  - Update `syncWithPartner()` to use dynamic authenticated credentials and delegate to `syncRepository.uploadMyDailyAggregate` and `partnerRepository.syncPartnerDailyFromRemote`.

### D. Recent Telemetry Fallback Engine (`PTR-03`)
- In [`HealthDao.kt`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/data/local/HealthDao.kt):
  - Add `getLatestPartnerDaily(partnerId: String): PartnerDailyEntity?` and `getLatestPartnerDailyFlow(partnerId: String): Flow<PartnerDailyEntity?>` ordering by `dateStr DESC, lastUpdated DESC LIMIT 1`.
- In [`PartnerRepository.kt`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/domain/repository/PartnerRepository.kt) & [`PartnerRepositoryImpl.kt`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/data/repository/PartnerRepositoryImpl.kt):
  - Expose `getLatestPartnerDailyFlow(partnerId: String)`.
  - In `syncPartnerDailyFromRemote(coupleId, partnerUid, dateStr)`: If primary date has no document, attempt fetching `yesterdayStr` to maintain timezone synchrony.
- In [`HealthConnectViewModel.kt`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/ui/healthconnect/HealthConnectViewModel.kt):
  - When `partnerDaily` for today has 0 steps or is null, fall back to the latest recorded partner telemetry from `getLatestPartnerDaily`, displaying the steps and formatting the badge (e.g. `"Yesterday • 8,420 steps"` or relative timestamp).

---

## 3. Test & Verification Plan

1. **Deterministic Unit Test Suite**:
   - Author `BackgroundSyncWorkerTest.kt` verifying `SyncWorker` runs Health Connect pull, session retrieval, remote upload, and partner pull.
   - Run `./gradlew testDebugUnitTest` ensuring 100% test pass rate.
   - Run `./gradlew compileDebugAndroidTestKotlin` ensuring 0 compilation errors.

2. **Live Device Verification via ARTEMIS (`emulator-5554`)**:
   - Inspect WorkManager job registration using `adb shell dumpsys jobscheduler`.
   - Verify `SyncWorker` manual execution using `adb shell cmd jobscheduler run -f com.couplefit <job_id>` or WorkManager trigger.
   - Inspect live device UI hierarchy and screenshot verifying that partner telemetry remains visible and does not drop to empty 0 steps across date boundaries.
