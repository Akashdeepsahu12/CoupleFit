# CoupleFit — Phase 74: Self-Healing Health Connect Pipeline, Hardware Step Fallback & 1-Tap Diagnostics (`SYNC-03`, `HC-02`, `DIAG-01`)

> **Target File Location**: `Documentation/PHASE74_SYNC_DIAGNOSTICS_PLAN.md`  
> **Active Working Branch**: `updatev1`  
> **Related ADRs**: ADR-001 (Offline-First Room SSOT), ADR-002 (Zero-Write Health Connect Privacy Contract), ADR-003 (Monotonic Sequential Write Lane), ADR-005 (Multi-Room Auto-Convergence)  
> **Defect & Feature IDs**: `HC-02` (Hardware Step Sensor Fallback), `SYNC-03` (Auto-Foreground Sync on Resume), `DIAG-01` (1-Tap Sync Diagnostic Banner), `DIAG-02` (Bilateral Sync Status Telemetry)

---

## 1. User-Facing Diagnostic Guide: The 5 Reasons Partner Data Shows 0 Steps

### 1.1 The #1 Reason: No Fitness App is Writing Data into Health Connect ("The Empty Hub Problem")
Health Connect does not track steps or heart rate by itself—it is a central database and bridge. It relies on a fitness tracking app (like Samsung Health, Google Fit, or Fitbit) to write sensor data into it.
* **The Cause**: If your partner installed CoupleFit and granted permissions to Health Connect, but their tracking app (e.g., Samsung Health or Google Fit) has not been configured to sync into Health Connect, Health Connect remains empty ($0$ steps).
* **The Manual Fix**:
  * **If using Samsung Health**: Open **Samsung Health** -> tap **3 dots (top right)** -> **Settings** -> **Health Connect** -> Turn on **"Allow all"** (or enable **Steps**).
  * **If using Google Fit / Fitbit**: Open **Google Fit / Fitbit** -> **Settings** -> **Health Connect** -> Ensure permissions to **Write steps and heart rate** are enabled.
  * **Open that tracking app once**: Fitness apps batch their writes. Opening the tracking app forces it to push the latest step count into Health Connect.

### 1.2 CoupleFit Has Not Been Granted Read Permissions in Health Connect
* **The Cause**: In CoupleFit, if your partner dismissed the permission prompt or denied read access, the app receives a `SecurityException` or returns $0$ records. In `app/src/main/java/com/couplefit/ui/healthconnect/HealthConnectViewModel.kt` (lines 350–361), the UI enters `HealthConnectUiState.PermissionRevoked`.
* **The Manual Fix**:
  1. Have your partner open **CoupleFit** and go to **Settings -> Sync & Sharing** tab.
  2. Under **Google Health Connect**, check if the badge says **Action Required** instead of **Connected**.
  3. Tap **"Manage Health Permissions (10 Metrics)"** and select **"Allow all"**.
  4. Alternatively, open phone **Settings -> Apps -> Health Connect -> App permissions -> CoupleFit -> Allow all**.

### 1.3 Health Connect is Not Installed or Needs an Update (Android 9–13 vs. Android 14+)
* **On Android 14+**: Health Connect is built directly into Android system settings (**Settings -> Security & privacy -> Health Connect**).
* **On Android 9 through 13**: Health Connect is a standalone app from Google. If your partner is on Android 13 or below and has not downloaded or updated Health Connect from the Play Store, the SDK status returns `SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED` or `SDK_UNAVAILABLE`.
* **The Manual Fix**:
  * Search for **"Health Connect"** on the Google Play Store (`https://play.google.com/store/apps/details?id=com.google.android.apps.healthdata`) and tap **Install** or **Update**.

### 1.4 Telemetry Visibility / Sharing Toggled OFF in CoupleFit Settings
* **The Cause**: Even if your partner's phone fetches their own data locally, if individual metric sharing toggles are disabled in `CoupleFitPreferencesDataStore.kt`, CoupleFit blocks uploading those metrics to Cloud Firestore. You will see $0$ or `"Awaiting sync"` on your phone.
* **The Manual Fix**:
  * Have your partner go to **CoupleFit -> Settings -> Sync & Sharing -> Telemetry Visibility & Sharing** and verify that **Share Steps**, **Share Workouts**, and **Share Calories** are toggled **ON**.

### 1.5 Battery Optimization Putting CoupleFit to Sleep
* **The Cause**: Aggressive battery savers (especially on Samsung One UI, Xiaomi, or OnePlus) restrict background sync workers.
* **The Manual Fix**:
  * In **CoupleFit -> Settings -> Sync & Sharing**, tap **"Background Running & Sync -> Request"** (or phone **Settings -> Apps -> CoupleFit -> Battery -> Unrestricted**).
  * Perform a manual pull-to-refresh on the **Today** dashboard to trigger an immediate foreground sync.

### 1.6 Quick 2-Minute Diagnostic Checklist for Your Partner
1. Open **Samsung Health (or Google Fit)** -> **Settings** -> **Health Connect** -> Turn **ON** write permissions for steps.
2. Open **CoupleFit** -> **Settings** -> **Sync & Sharing** -> Confirm **Google Health Connect** says **Connected**.
3. On CoupleFit's **Today** screen, swipe down to **pull-to-refresh**.

---

## 2. Architectural Root Cause Analysis (Why Manual Setup Fails & Code-Level Remediation)

Relying on users to manually configure five different OS and third-party app menus creates high onboarding drop-off. Phase 74 eliminates this friction programmatically in code while preserving all architectural invariants:

1. **Root Cause 1 (The Empty Hub Problem)**:
   * **Diagnosis**: Health Connect is an inert storage bridge until Samsung Health or Google Fit writes to it. Even when linked, feeder apps batch their writes irregularly.
   * **Engineering Fix (`HC-02`)**: Introduce `HardwareStepSensorManager.kt` using Android's native `Sensor.TYPE_STEP_COUNTER` (`android.permission.ACTIVITY_RECOGNITION`). Whenever Health Connect returns $0$ steps, CoupleFit resolves effective steps via $\max(\text{healthConnectSteps}, \text{hardwareDailySteps})$ and writes the result directly into the local Room SSOT (`HealthDao`) on `Dispatchers.IO.limitedParallelism(1)`. In strict compliance with **ADR-002**, CoupleFit never writes hardware sensor steps back into Health Connect.
2. **Root Cause 2 & 3 (Buried Permission & SDK Update Recovery)**:
   * **Diagnosis**: When `HealthConnectUiState.PermissionRevoked` or `SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED` occurs, the recovery button is hidden inside **Settings -> Sync & Sharing**.
   * **Engineering Fix (`DIAG-01`)**: Surface a Liquid Glass `SyncDiagnosticBanner.kt` directly at the top of `DashboardScreen.kt` with 1-tap intent launchers for Health Connect permissions, Google Play Store updates, Samsung Health / Google Fit deep-linking, and Battery Optimization exemption.
3. **Root Cause 4 (Silent Sharing Suppression & Blind Partner UI)**:
   * **Diagnosis**: If a partner has telemetry toggles off or a broken Health Connect setup, the receiving device only sees $0$ steps with no context.
   * **Engineering Fix (`DIAG-02`)**: Enforce default `true` values for `shareSteps`, `shareWorkouts`, and `shareCalories` in `CoupleFitPreferencesDataStore.kt`, and broadcast a lightweight `syncDiagnostics` status map (`OK`, `EMPTY_HUB_SENSOR_FALLBACK`, `PERMISSION_REVOKED`, `SHARING_PAUSED`) to Firestore (`users/{uid}` and `couples/{coupleId}`).
4. **Root Cause 5 (Background Worker Throttling & Manual Pull-to-Refresh Dependency)**:
   * **Diagnosis**: Phase 66 throttled periodic `WorkManager` sync to $6$ hours (`PWR-01`), meaning if a partner opens the app without swiping down to pull-to-refresh, their latest steps aren't pushed immediately.
   * **Engineering Fix (`SYNC-03`)**: Attach a lifecycle `ON_RESUME` observer on `DashboardScreen.kt` with a $60\text{-second}$ cooldown that automatically pulls local steps (Health Connect + Hardware Sensor fallback) and triggers an immediate foreground Firestore push.

---

## 3. Phase 74 Step-by-Step Implementation Plan (`updatev1`)

| Slice | Target Files | Architectural Action | Verification Criteria |
| :--- | :--- | :--- | :--- |
| **Slice 1: Hardware Step Sensor Fallback (`HC-02`)** | `data/sensor/HardwareStepSensorManager.kt`, `AndroidManifest.xml`, `HealthConnectViewModel.kt` | Declare `android.permission.ACTIVITY_RECOGNITION`. Read `Sensor.TYPE_STEP_COUNTER` delta per calendar day when Health Connect returns $0$ steps. Persist to Room SSOT on the sequential write lane. | Partner device records real steps immediately even if Samsung Health or Google Fit is unlinked. |
| **Slice 2: Auto-Foreground Sync on Resume (`SYNC-03`)** | `DashboardScreen.kt`, `HealthConnectViewModel.kt`, `SyncRepositoryImpl.kt` | Add `LifecycleResumeEffect` on `DashboardScreen` calling `viewModel.onDashboardResumed()` with a $60\,000\text{ ms}$ debounce window. | Opening CoupleFit automatically syncs local steps to Firestore without requiring a manual swipe-down. |
| **Slice 3: 1-Tap OS & Feeder Intent Helper (`DIAG-01`)** | `util/HealthSyncIntentHelper.kt`, `ui/dashboard/components/SyncDiagnosticBanner.kt` | Build deep-link intents for Health Connect Settings, Play Store update, Samsung Health (`com.sec.android.app.shealth`), Google Fit (`com.google.android.apps.fitness`), and Battery Unrestricted dialog. | Tapping the banner resolves permission, battery, or feeder-app setup in 1 tap from the Today tab. |
| **Slice 4: Default-ON Telemetry & Diagnostic Broadcast (`DIAG-02`)** | `CoupleFitPreferencesDataStore.kt`, `PartnerRepositoryImpl.kt` | Default `shareSteps`, `shareCalories`, and `shareWorkouts` to `true`. Mirror `syncDiagStatus` to `users/{uid}` and `couples/{coupleId}`. | Partner card displays exact diagnostic badge if partner's Health Connect is misconfigured. |
| **Slice 5: Automated Unit Test Suite** | `app/src/test/java/com/couplefit/ui/Phase74SyncDiagnosticsTest.kt` | Add 8 unit tests covering $\max(\text{hcSteps}, \text{hwSteps})$ arbitration, reboot baseline reset, default-ON sharing flags, diagnostic state priority, and $60\text{ s}$ resume throttle. | $148/148$ JVM Unit Tests Green, `assembleDebug` clean. |

---

## 4. Kotlin & Jetpack Compose Implementation Blueprints

### 4.1 Native Hardware Step Counter Fallback (`HardwareStepSensorManager.kt`)
Reads the phone's built-in pedometer (`Sensor.TYPE_STEP_COUNTER`) whenever Health Connect returns $0$ steps, maintaining a daily midnight baseline in `SharedPreferences` and handling device reboots gracefully:

```kotlin
package com.couplefit.data.sensor

import android.content.Context
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.time.LocalDate
import kotlin.coroutines.resume

class HardwareStepSensorManager(context: Context) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private val prefs: SharedPreferences =
        context.getSharedPreferences("couplefit_hw_step_baseline", Context.MODE_PRIVATE)

    val isHardwareSensorAvailable: Boolean
        get() = stepSensor != null

    /**
     * Reads cumulative boot steps from Sensor.TYPE_STEP_COUNTER and computes today's delta.
     * Used as a zero-friction fallback when Health Connect returns 0 steps ("Empty Hub").
     * Strictly read-only: never writes back to Health Connect (ADR-002).
     */
    suspend fun readTodayHardwareSteps(): Int {
        val sensor = stepSensor ?: return 0
        val totalBootSteps = withTimeoutOrNull(1500L) {
            suspendCancellableCoroutine<Int?> { continuation ->
                val listener = object : SensorEventListener {
                    override fun onSensorChanged(event: SensorEvent?) {
                        val value = event?.values?.firstOrNull()?.toInt()
                        sensorManager.unregisterListener(this)
                        if (continuation.isActive) continuation.resume(value)
                    }

                    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
                }
                val registered = sensorManager.registerListener(
                    listener,
                    sensor,
                    SensorManager.SENSOR_DELAY_UI
                )
                if (!registered && continuation.isActive) {
                    continuation.resume(null)
                }
                continuation.invokeOnCancellation {
                    sensorManager.unregisterListener(listener)
                }
            }
        } ?: return 0

        return computeDailyDeltaAndUpdateBaseline(
            todayKey = LocalDate.now().toString(),
            totalBootSteps = totalBootSteps,
            prefs = prefs
        )
    }

    companion object {
        internal fun computeDailyDeltaAndUpdateBaseline(
            todayKey: String,
            totalBootSteps: Int,
            prefs: SharedPreferences
        ): Int {
            val savedDate = prefs.getString("baseline_date", null)
            val savedBaseline = prefs.getInt("baseline_steps", -1)

            // Reset baseline on new calendar day or if device rebooted (totalBootSteps < savedBaseline)
            if (savedDate != todayKey || savedBaseline < 0 || totalBootSteps < savedBaseline) {
                prefs.edit()
                    .putString("baseline_date", todayKey)
                    .putInt("baseline_steps", totalBootSteps)
                    .apply()
                return 0
            }

            return (totalBootSteps - savedBaseline).coerceAtLeast(0)
        }
    }
}
```

### 4.2 1-Tap OS, Play Store & Feeder App Intent Helper (`HealthSyncIntentHelper.kt`)
Provides 1-tap resolution actions for all 5 diagnostic blockers:

```kotlin
package com.couplefit.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.health.connect.client.HealthConnectClient

object HealthSyncIntentHelper {

    const val HEALTH_CONNECT_PACKAGE = "com.google.android.apps.healthdata"
    const val SAMSUNG_HEALTH_PACKAGE = "com.sec.android.app.shealth"
    const val GOOGLE_FIT_PACKAGE = "com.google.android.apps.fitness"

    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return pm.isIgnoringBatteryOptimizations(context.packageName)
    }

    @SuppressLint("BatteryLife")
    fun requestIgnoreBatteryOptimizations(context: Context) {
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:${context.packageName}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(intent) }
    }

    fun openHealthConnectSettingsOrStore(context: Context) {
        val status = HealthConnectClient.getSdkStatus(context, HEALTH_CONNECT_PACKAGE)
        if (status == HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED ||
            status == HealthConnectClient.SDK_UNAVAILABLE
        ) {
            val playIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("market://details?id=$HEALTH_CONNECT_PACKAGE")
            ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
            runCatching { context.startActivity(playIntent) }.onFailure {
                context.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=$HEALTH_CONNECT_PACKAGE")
                    ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                )
            }
            return
        }

        val settingsAction = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            "android.health.connect.action.MANAGE_HEALTH_PERMISSIONS"
        } else {
            "androidx.health.ACTION_HEALTH_CONNECT_SETTINGS"
        }
        runCatching {
            context.startActivity(Intent(settingsAction).apply {
                putExtra(Intent.EXTRA_PACKAGE_NAME, context.packageName)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        }
    }

    fun detectInstalledFeederAppName(context: Context): String? {
        val pm = context.packageManager
        return when {
            runCatching { pm.getPackageInfo(SAMSUNG_HEALTH_PACKAGE, 0) }.isSuccess -> "Samsung Health"
            runCatching { pm.getPackageInfo(GOOGLE_FIT_PACKAGE, 0) }.isSuccess -> "Google Fit"
            else -> null
        }
    }

    fun openPrimaryFeederApp(context: Context): Boolean {
        val pm = context.packageManager
        val targetPkg = listOf(SAMSUNG_HEALTH_PACKAGE, GOOGLE_FIT_PACKAGE).firstOrNull { pkg ->
            runCatching { pm.getPackageInfo(pkg, 0) }.isSuccess
        } ?: return false

        val launchIntent = pm.getLaunchIntentForPackage(targetPkg)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        } ?: return false

        context.startActivity(launchIntent)
        return true
    }
}
```

### 4.3 Liquid Glass 1-Tap Diagnostic Banner (`SyncDiagnosticBanner.kt`)
Renders a tactile, squircle-curved diagnostic card on `DashboardScreen.kt` whenever a local or partner sync blocker is detected:

```kotlin
package com.couplefit.ui.dashboard.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

enum class SyncBlockerType {
    NONE,
    SDK_UPDATE_REQUIRED,
    PERMISSION_REVOKED,
    SHARING_DISABLED,
    EMPTY_HUB_FEEDER_UNLINKED,
    BATTERY_OPTIMIZED
}

data class SyncDiagnosticUiModel(
    val blockerType: SyncBlockerType = SyncBlockerType.NONE,
    val title: String = "",
    val description: String = "",
    val actionLabel: String = "",
    val usingHardwareSensorFallback: Boolean = false,
    val partnerDiagnosticNote: String? = null
)

@Composable
fun SyncDiagnosticBanner(
    diagnostic: SyncDiagnosticUiModel,
    onPrimaryActionClick: (SyncBlockerType) -> Unit,
    modifier: Modifier = Modifier
) {
    val isVisible = diagnostic.blockerType != SyncBlockerType.NONE ||
        diagnostic.partnerDiagnosticNote != null

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier
    ) {
        val cardShape = RoundedCornerShape(22.dp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .clip(cardShape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x331E293B),
                            Color(0x550F172A)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0x6638BDF8),
                            Color(0x22FFFFFF)
                        )
                    ),
                    shape = cardShape
                )
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (diagnostic.blockerType != SyncBlockerType.NONE) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = diagnostic.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = diagnostic.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                        if (diagnostic.actionLabel.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .padding(start = 12.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFF00E5FF).copy(alpha = 0.22f))
                                    .border(
                                        width = 1.dp,
                                        color = Color(0xFF00E5FF).copy(alpha = 0.65f),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .clickable { onPrimaryActionClick(diagnostic.blockerType) }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = diagnostic.actionLabel,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                diagnostic.partnerDiagnosticNote?.let { note ->
                    Text(
                        text = note,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFFFB74D)
                    )
                }
            }
        }
    }
}
```

### 4.4 Step Arbitration & Auto-Resume Foreground Sync (`HealthConnectViewModel.kt`)
Integrates the hardware step fallback and $60\text{-second}$ `ON_RESUME` auto-sync cooldown:

```kotlin
// Inside HealthConnectViewModel.kt
private var lastForegroundAutoSyncMs: Long = 0L

fun onDashboardResumed() {
    val now = System.currentTimeMillis()
    if (now - lastForegroundAutoSyncMs > 60_000L) {
        lastForegroundAutoSyncMs = now
        refreshAndSync(forceRemotePush = true)
    }
}

internal suspend fun resolveEffectiveSteps(
    healthConnectSteps: Int,
    readHardwareSteps: suspend () -> Int
): Pair<Int, String> {
    if (healthConnectSteps > 0) {
        return healthConnectSteps to "HEALTH_CONNECT"
    }
    val hwSteps = readHardwareSteps()
    return if (hwSteps > 0) {
        hwSteps to "HARDWARE_SENSOR_FALLBACK"
    } else {
        0 to "EMPTY_HUB"
    }
}
```

---

## 5. Ready-to-Run Antigravity Multi-Agent Prompt

Copy and paste the following command directly into your Antigravity console to execute **Phase 74** on branch `updatev1`:

```text
/ecc:orch-add-feature --agents code-architect, kotlin-reviewer, gsd-debugger, gsd-executor --mcp artemis

Execute Phase 74 (SYNC-03, HC-02, DIAG-01, DIAG-02: Self-Healing Health Connect Pipeline, Hardware Step Sensor Fallback & 1-Tap Sync Diagnostics) on branch `updatev1` according to `Documentation/PHASE74_SYNC_DIAGNOSTICS_PLAN.md`:

1. **Hardware Step Counter Fallback (`HC-02` - Empty Hub Fix)**:
   - Create `HardwareStepSensorManager.kt` using `Sensor.TYPE_STEP_COUNTER` (and request `ACTIVITY_RECOGNITION` permission alongside Health Connect permissions).
   - In `HealthConnectViewModel.kt` / `SyncRepositoryImpl.kt`, when Health Connect returns `0` steps for today, fall back to `max(healthConnectSteps, hardwareSensorTodaySteps)` and persist into Room SSOT (`HealthDao`) on the monotonic write lane without writing back to Health Connect (preserving ADR-002).

2. **Automatic Foreground Sync on App Resume (`SYNC-03`)**:
   - Add a lifecycle observer (`ON_RESUME`) on `DashboardScreen.kt` / `HealthConnectViewModel.kt` with a 60-second cooldown so opening CoupleFit automatically pulls local steps and pushes to Cloud Firestore (`couples/{coupleId}` and candidate rooms) without waiting for the 6-hour WorkManager job or requiring a manual pull-to-refresh.

3. **Default-ON Telemetry Sharing & Bilateral Diagnostic Status (`DIAG-01` & `DIAG-02`)**:
   - In `CoupleFitPreferencesDataStore.kt`, ensure `shareSteps`, `shareWorkouts`, and `shareCalories` default to `true` and add an `enableAllTelemetrySharing()` helper.
   - Mirror a lightweight `syncDiagnostics` state (`hcPermissionGranted`, `stepSource`, `sharingEnabled`, `batteryUnrestricted`) to Firestore `users/{uid}` and `couples/{coupleId}` so the partner's Today & Couple Hub screens show an explicit diagnostic reason if partner steps are 0.

4. **1-Tap Sync Health Resolution Banner on Today & Settings**:
   - Create `HealthSyncIntentHelper.kt` and `SyncDiagnosticBanner.kt` in `com.couplefit.ui.dashboard.components`.
   - Surface a liquid-glass 1-tap resolution card on `DashboardScreen.kt` when:
     a) Health Connect is missing/needs update -> 1-tap Play Store (`com.google.android.apps.healthdata`)
     b) Read permissions are revoked -> 1-tap Health Connect Permission launcher
     c) Sharing is toggled off -> 1-tap "Enable Sharing Now"
     d) Battery optimization is restricting background sync -> 1-tap `ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`
     e) Health Connect returns 0 steps while Samsung Health/Google Fit is installed -> 1-tap "Open Samsung Health / Google Fit to Sync" button.

5. **Tests & Documentation**:
   - Create `Phase74SyncDiagnosticsTest.kt` verifying hardware step fallback arbitration, default-ON telemetry flags, diagnostic banner state transitions, and 60s auto-resume sync throttling.
   - Run `./gradlew testDebugUnitTest` and `./gradlew assembleDebug` to confirm 100% green build, and update `Documentation/Fixinglist.md` and `Documentation/allcontext.md` on branch `updatev1`.
```