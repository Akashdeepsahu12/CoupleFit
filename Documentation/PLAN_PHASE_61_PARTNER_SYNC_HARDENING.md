# Phase 61: Real-Time Partner Telemetry Synchronization & Firestore Pipeline Hardening (`PTR-02`, `SYNC-01`, `DAT-06`) 💎📡

> **Governing Standards**: [`AGENTS.md`](file:///D:/Couple_fit/CoupleFit/AGENTS.md)  
> **Master Defect Tracker**: [`Documentation/Fixinglist.md`](file:///D:/Couple_fit/CoupleFit/Documentation/Fixinglist.md)  
> **Target Release**: `v2.9.47-partner-sync-hardening`

---

## 1. Problem Statement & Root Cause Analysis

The user reported:
> *"I am still not able to see my partner's data in my app. it not syncing, can you fix this?"*

A deep inspection of the live Android runtime (`emulator-5554`), SQLite databases (`couplefit_health.db`, `firestore.db`), and source code identified three interlocking root causes:

1. **`HealthDao` Ambiguous `LIMIT 1` Query Hijacked by Dummy Data (`DAT-06`)**:
   - `HealthDao.getPartnerDailyFlow(dateStr)` executed `SELECT * FROM partnerDaily WHERE dateStr = :dateStr LIMIT 1` without filtering by `partnerId` or ordering by priority.
   - At startup, before the authenticated session loaded, a dummy row for `partner_1` with 7,420 steps was inserted into `partnerDaily`.
   - When the real linked partner (`RmkTPEUJlwYVm23evsWOj6cOpi02`) was inserted, SQLite continued returning `partner_1` due to its earlier rowid.
   - Consequently, the UI remained permanently frozen on `partner_1` (displaying "Last updated 13h ago • Cached").

2. **Race Condition & Hardcoded Default Sync Credentials (`SYNC-01`)**:
   - `HealthConnectViewModel.init` invoked `checkStatusAndFetchData()` before `observeUserSessionAndPartnerSync()` received the authenticated `UserSession`.
   - `checkStatusAndFetchData()` read default `coupleId = "couple_1"` and `myUid = "me"`, uploading data to `couples/couple_1/daily/me_2026-11-10`.
   - `SyncRepositoryImpl` recorded `lastSyncTimestamp = now`, causing subsequent sync requests for the real couple (`cpl_...`) to be throttled for 15 minutes.
   - When the real session arrived, no foreground upload was triggered for the actual couple.

3. **Missing One-Shot Remote Partner Sync on Pull-to-Refresh (`PTR-02`)**:
   - `HealthConnectViewModel` had no mechanism to explicitly fetch the latest partner data from Firestore on pull-to-refresh; it relied solely on `addSnapshotListener`.
   - `listenToPartnerDaily` lacked polymorphic parsing for timestamps (`com.google.firebase.Timestamp`, numeric epoch, String) and non-standard number representations.
   - On pull-to-refresh, `uploadMyDailyAggregate` had `force = false`, preventing on-demand forced synchronizations.

---

## 2. Milestone Deliverables

### A. Room Database & DAO Hardening (`DAT-06`)
- In [`HealthDao.kt`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/data/local/HealthDao.kt):
  - Update `getPartnerDailyFlow(dateStr)` and `getPartnerDaily(dateStr)` to prioritize non-dummy partner IDs and order by latest update:
    ```sql
    SELECT * FROM partnerDaily 
    WHERE dateStr = :dateStr 
    ORDER BY CASE WHEN partnerId != 'partner_1' THEN 0 ELSE 1 END, lastUpdated DESC 
    LIMIT 1
    ```
  - Ensure `getPartnerDailyFlow(partnerId, dateStr)` is the primary query used by `HealthConnectViewModel`.

### B. Session Cache Pre-Population & Clean Initialization (`SYNC-01`)
- In [`HealthConnectViewModel.kt`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/ui/healthconnect/HealthConnectViewModel.kt):
  - Pre-populate initial `coupleId`, `partnerId`, `myUid`, and `isPartnerLinked` from `authManager.getCurrentSession()` in the view model constructor or state initializer, eliminating the split-second fallback to `"couple_1"`.
  - When `observeUserSessionAndPartnerSync()` emits a linked partner, automatically purge dummy `"partner_1"` records:
    ```kotlin
    if (session.isLinked && session.partnerId != null && session.partnerId != "partner_1") {
        healthDao.deletePartnerDailyData("partner_1")
    }
    ```
  - Pass the explicit `partnerId` to `startObservingLocalDb(partnerId)` and re-bind when the session changes.
  - Automatically trigger an immediate `uploadMyDailyAggregate(coupleId, myUid, dateStr, force = true)` upon session resolution to ensure the partner immediately sees fresh user steps.

### C. Direct Remote Partner Fetch on Refresh (`PTR-02`)
- In [`PartnerRepository.kt`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/domain/repository/PartnerRepository.kt) & [`PartnerRepositoryImpl.kt`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/data/repository/PartnerRepositoryImpl.kt):
  - Declare and implement `syncPartnerDailyFromRemote(coupleId: String, partnerUid: String, dateStr: String): Result<PartnerDailyEntity?>` using `docRef.get().await()`.
  - Add robust polymorphic parsing for `lastUpdated` (supporting `Timestamp`, `Number`, and `Date`) and numeric fields (`Number` or `String`).
- In [`HealthConnectViewModel.kt`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/ui/healthconnect/HealthConnectViewModel.kt):
  - In `checkStatusAndFetchData(force: Boolean = true)`:
    - Pass `force = true` on user-initiated refresh.
    - Call `partnerRepository.syncPartnerDailyFromRemote(coupleId, partnerId, dateStr)` to actively fetch and update partner telemetry in Room.
    - Set `isSyncing = false` and update `lastSyncedAt` only after both Health Connect and Firestore sync passes complete.

---

## 3. Test & Verification Plan

1. **Unit Test Suite**:
   - Authored in `PartnerTelemetrySyncTest.kt` or new `PartnerRemoteSyncTest.kt`:
     - Test polymorphic timestamp parsing (`Timestamp`, `Number`, `String`).
     - Test `HealthDao` query priority (real partner preferred over dummy `partner_1`).
     - Test one-shot remote fetch writing to Room SSOT.
     - Test immediate session resolution without `"couple_1"` leakage.
   - Run `./gradlew testDebugUnitTest` (target: 100% green).
   - Run `./gradlew compileDebugAndroidTestKotlin` (target: 0 compilation errors).

2. **Live Device Verification via ARTEMIS (`emulator-5554`)**:
   - Install fresh debug APK on `emulator-5554`.
   - Verify Today dashboard displays partner data without "Awaiting sync" or stale 13h timestamps.
   - Test pull-to-refresh: verify sync spinner starts, executes remote sync, and smoothly dismisses.
   - Capture live screenshots verifying Today, Trends, and Couple Hub.

---

## 4. Acceptance Criteria
- [x] Zero leakage of `"couple_1"` or `"partner_1"` when a real user session exists.
- [x] `HealthDao` queries prioritize the real partner's records.
- [x] Pull-to-refresh triggers forced upload of user data and one-shot fetch of partner data from Firestore.
- [x] All unit and Android test suites pass 100% green.
- [x] Live ARTEMIS verification confirms active sync on `emulator-5554`.
