# Phase 52: Security, Rules Hardening, Zero-Write Privacy & Production Configuration 🛡️📋

> **Governing Specifications**: [`Documentation/EGM_FULL_APP_REVIEW.md`](file:///D:/Couple_fit/CoupleFit/Documentation/EGM_FULL_APP_REVIEW.md)  
> **Master Defect Log**: [`Documentation/Fixinglist.md`](file:///D:/Couple_fit/CoupleFit/Documentation/Fixinglist.md)  
> **Target Milestone**: v2.9.38  
> **Associated Defect Identifiers**: `SEC-01`, `SEC-02`, `SEC-04`, `HC-01`, `CFG-01`, `CFG-02`, `R8-01`  

---

## 1. Executive Summary & Goals

Phase 52 addresses the immediate critical security contradictions, zero-write privacy violations, and production configuration defects uncovered by the EGM Full-Stack Audit:
1. **Firestore Security Rules Contradictions (`SEC-01`)**: `firestore.rules` currently specifies `match /challenges/{challengeId} { allow write: if false; }`, which blocks 100% of client challenge creations with `PERMISSION_DENIED`. Update rules to validate and allow challenge creation and progress updates by couple members.
2. **User Data Access Lockdown (`SEC-02`)**: Restrict `/users/{uid}` read permissions from global authenticated access to strictly the document owner and their verified partner. Enforce telemetry value bounds on daily aggregate records (`steps < 250000`).
3. **Zero-Trust Read-Only Health Connect Contract (`HC-01`)**: Eliminate `writeSampleSteps` from `HealthConnectManager.kt` and its invocation in `HealthConnectViewModel.kt`. Health Connect integration must strictly never mutate or insert system health records.
4. **Glance Home Screen Widget Dynamic Session Credentials (`CFG-02`)**: Eliminate hardcoded `"couple_1"`, `"user_1"`, and `"partner_1"` from `PokeActionCallback.kt`. Retrieve active session identifiers from session cache.
5. **GDPR/CCPA Data Deletion Completeness (`SEC-04`)**: Update `AuthViewModel.deleteUserData()` to wipe the local Room database (`healthDatabase.clearAllTables()`) and clear DataStore preferences before signing out.
6. **Clean Release Build & Manifest Declarations (`CFG-01`, `R8-01`)**:
   - Clean `app/build.gradle.kts` release signing block.
   - Clean `AndroidManifest.xml` by removing deprecated Samsung survey and battery exemption tags.
   - Harden `proguard-rules.pro` by removing MockK references and adding keep rules for Glance `ActionCallback` and serialization models.

---

## 2. Explicit Acceptance Criteria & Test Bars

- [ ] **Criterion 1 (Challenge Security Rules)**: `firestore.rules` permits couple members to create and update challenges in their couple document while rejecting non-members or forged creator UIDs.
- [ ] **Criterion 2 (User Privacy Lockdown)**: `firestore.rules` blocks unlinked third parties from reading arbitrary `/users/{uid}` profiles.
- [ ] **Criterion 3 (Zero-Write Health Connect Contract)**: `writeSampleSteps` is completely deleted from `HealthConnectManager.kt` and removed from ViewModel mock logic. All Health Connect client interactions remain strictly read-only.
- [ ] **Criterion 4 (Dynamic Widget Poke)**: `PokeActionCallback.kt` dynamically reads `cached_couple_id`, `cached_partner_id`, and `myUid`.
- [ ] **Criterion 5 (Local Database Wipe on Delete)**: `AuthViewModel.deleteUserData()` executes `healthDatabase.clearAllTables()`.
- [ ] **Criterion 6 (Production Build & ProGuard)**: MockK is removed from release ProGuard rules, Glance `ActionCallback` is preserved against R8 stripping, and dead Samsung permissions are removed from `AndroidManifest.xml`.
- [ ] **Criterion 7 (Test Verification)**: `.\gradlew.bat testDebugUnitTest` and `compileDebugAndroidTestKotlin` pass 100% green.

---

## 3. Implementation Plan & Task Breakdown

### Task 1: Update Firestore Security Rules (`firestore.rules`)
- Update `match /users/{uid}` to allow read only if `isOwner(uid)` or linked partner.
- Update `match /challenges/{challengeId}` to allow create if `isCoupleMember(coupleId)` and `request.resource.data.createdBy == request.auth.uid`. Allow update for progress and completion.
- Add step range validation to `daily/{docId}` (`steps >= 0 && steps < 250000`).

### Task 2: Enforce Zero-Write Privacy in Health Connect
- Remove `writeSampleSteps` from [`HealthConnectManager.kt`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/data/healthconnect/HealthConnectManager.kt).
- Remove `healthConnectManager.writeSampleSteps(count)` from [`HealthConnectViewModel.kt`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/ui/healthconnect/HealthConnectViewModel.kt).

### Task 3: Dynamic Session Credentials in Glance Widget
- In [`WidgetEntryPoint.kt`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/widget/WidgetEntryPoint.kt), expose `fun authManager(): FirebaseAuthManager`.
- In [`PokeActionCallback.kt`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/widget/PokeActionCallback.kt), retrieve active `coupleId`, `partnerId`, `myUid`, and `displayName` from session cache rather than hardcoded `"couple_1"`.

### Task 4: Complete Data Wipe on Account Deletion
- In [`AuthViewModel.kt`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/ui/auth/AuthViewModel.kt), inject `HealthDatabase` and call `healthDatabase.clearAllTables()` upon `deleteUserData()` success. Also clear partner daily records on `unlinkPartner()`.

### Task 5: Manifest, ProGuard & Build Configuration Cleanup
- In [`AndroidManifest.xml`](file:///D:/Couple_fit/CoupleFit/app/src/main/AndroidManifest.xml), remove dead Samsung survey permission, battery exemption permission, and Samsung package visibility tags.
- In [`app/proguard-rules.pro`](file:///D:/Couple_fit/CoupleFit/app/proguard-rules.pro), remove MockK rule; add keep rules for Glance `ActionCallback` and `@Serializable` classes.
- In [`app/build.gradle.kts`](file:///D:/Couple_fit/CoupleFit/app/build.gradle.kts), clean release signing block.

---

## 4. Verification & Validation Steps

1. Run `./gradlew testDebugUnitTest` to ensure all existing tests pass.
2. Run `./gradlew compileDebugAndroidTestKotlin` to verify compilation.
3. Install debug build on `emulator-5554` and verify dashboard and widget interaction.
4. Update `Documentation/Fixinglist.md`, `triage.md`, `phases.md`, and `INDEX.md`.
