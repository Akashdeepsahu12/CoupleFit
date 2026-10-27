# User Flow Diagrams & Architectural Analysis: FLOW-01, FLOW-02, FLOW-03 🗺️

> **Skill Invocation**: [`/designer-skills:user-flow-diagram`](file:///C:/Users/akash/.gemini/config/plugins/designer-skills/skills/user-flow-diagram/SKILL.md)  
> **Target Subsystems**: 
> - **`FLOW-01`**: Zero-Tap Home Screen Glance Widget & Quick Poke Flow  
> - **`FLOW-02`**: Direct Manipulation Double-Tap Partner Ring Cheer Flow  
> - **`FLOW-03`**: Offline / Stale Telemetry Transparency & Diagnostics Flow  
> **Governance & Specs**: [`Documentation/card-sort-analysis.md`](file:///D:/Couple_fit/CoupleFit/Documentation/card-sort-analysis.md), [`Documentation/Fixinglist.md`](file:///D:/Couple_fit/CoupleFit/Documentation/Fixinglist.md) (§6), and [`AGENTS.md`](file:///D:/Couple_fit/CoupleFit/AGENTS.md)  
> **Date**: October 2026

---

## 1. Executive Summary

This document presents the complete interaction architecture, state transition trees, and end-to-end user flow diagrams for three foundational engagement innovations in **CoupleFit**:

1. **`FLOW-01` (Zero-Tap Home Screen Widget & Quick Poke)**: Bypasses the application cold-start barrier by bringing bilateral activity telemetry and zero-tap partner nudging directly to the Android Home Screen via Jetpack Glance.
2. **`FLOW-02` (Double-Tap Ring to Cheer Micro-Interaction)**: Implements direct-manipulation gestures on the `Today` screen dual activity gauges, allowing instant partner encouragement with heart particle bursts and physical haptics without scrolling to reaction rows.
3. **`FLOW-03` (Offline / Stale Telemetry Transparency)**: Solves the critical ambiguity between a partner being physically sedentary versus their wearable being disconnected, utilizing an adaptive status pill and one-tap background ping protocol.

---

## 2. Standard Flow Diagram Notation

All diagrams follow the formal user flow notation specified by `/designer-skills:user-flow-diagram`:

| Symbol / Shape | Meaning | Purpose in CoupleFit |
| :--- | :--- | :--- |
| `(( Entry / Exit ))` | Entry or Exit Point | Android OS Launcher, Notification Tap, or User Dismissal |
| `[ Rectangle ]` | View / Screen | Jetpack Glance Widget, Today Screen, Bottom Sheet Modal |
| `{ Diamond }` | Decision Branch | Auth check, Pairing check, Online status, Time delta |
| `([ Rounded Rect ])` | User Action | Tap widget, Double-tap ring, Click Poke button |
| `[[ Subroutine / Sys ]]` | System / Background Process | Room SSOT query, FCM push, Health Connect sync |

---

## 3. FLOW-01: Zero-Tap Home Screen Glance Widget & Quick Poke

### 3.1 Flow Overview & User Goal
- **User Goal**: View partner synchrony at a glance and send quick accountability nudges directly from the Android Home Screen without unlocking and launching the full app.
- **Velocity Impact**: Increases daily partner touchpoints from 2×/day (morning/evening app opens) to 10–12×/day (ambient glances during phone unlocks).

### 3.2 End-to-End User Flow Diagram

```mermaid
flowchart TD
    HOME(("Android Home Screen Launcher")) --> WIDGET["CoupleFit 4x2 Glance Widget"]
    
    WIDGET --> READ_ROOM[["System: Room SSOT HealthDao Query<br/>(Today's User & Partner Aggregates)"]]
    READ_ROOM --> CHECK_STATE{"Pairing & Auth Valid?"}
    
    CHECK_STATE -- No / Unlinked --> W_UNLINKED["Widget State: 'Tap to Link Partner 💖'"]
    W_UNLINKED -->|Tap Anywhere| LAUNCH_PAIR["Launch App -> Onboarding/Pairing Gate"]
    
    CHECK_STATE -- Yes --> W_ACTIVE["Widget Active State:<br/>• Bilateral Mini-Rings (You vs Partner)<br/>• Step Counts & Active Mins<br/>• Inline 'Poke 👋' Button"]
    
    W_ACTIVE --> USER_CHOICE{"User Interaction"}
    
    USER_CHOICE -- Tap Body Area --> LAUNCH_APP["System: actionStartActivity(MainActivity)<br/>Flags: CLEAR_TOP | NEW_TASK"]
    LAUNCH_APP --> APP_TODAY["Navigate to Tab 1: Today Dashboard"]
    
    USER_CHOICE -- Tap 'Poke 👋' Button --> POKE_ACTION(["User Action: Tap Inline 'Poke' Button"])
    POKE_ACTION --> HAPTIC_W[["System: Vibrator Pulse (Light Tick)"]]
    HAPTIC_W --> OPTIMISTIC["Widget UI: Optimistic Feedback<br/>'Poked! 💖' (Button Disabled for 3s)"]
    
    OPTIMISTIC --> NET_CHECK{"Device Online?"}
    
    NET_CHECK -- Offline --> ENQUEUE_WORK[["System: WorkManager Enqueues Nudge<br/>Constraints: NetworkType.CONNECTED"]]
    ENQUEUE_WORK --> WIDGET_OFFLINE["Widget: 'Queued (Offline) ⏱️'"]
    
    NET_CHECK -- Online --> SEND_FCM[["System: Send Nudge to Firebase Function<br/>Payload: {fromUid, type: 'POKE', time: now}"]]
    SEND_FCM --> PARTNER_DEVICE[["Partner Device Receives FCM High-Priority Push"]]
    PARTNER_DEVICE --> NOTIF["Partner Lock Screen Notification:<br/>'💖 Partner poked you! Time to move!'"]
    
    OPTIMISTIC --> RESET_TIMER[["Timer: 3 seconds delay"]]
    RESET_TIMER --> W_ACTIVE

    style HOME fill:#4A148C,color:#fff
    style W_ACTIVE fill:#1565C0,color:#fff
    style OPTIMISTIC fill:#2E7D32,color:#fff
    style CHECK_STATE fill:#E65100,color:#fff
    style NET_CHECK fill:#E65100,color:#fff
```

### 3.3 Error Paths & Edge Cases
1. **Room Database Empty (Fresh Boot)**: Displays neutral skeleton placeholder with cached fallback values until first `GlanceAppWidget.updateAll()` triggers.
2. **Rapid Multi-Tap (Poke Spamming)**: Button disables immediately on first tap for 3,000ms with debouncing to prevent flooding FCM downstream pipelines.
3. **Widget Sizing Adaptability**: Supports 3x2, 4x2, and 4x3 layouts; collapses step breakdown labels on smaller grid sizes while preserving ring geometry.

---

## 4. FLOW-02: Direct Manipulation "Double-Tap Partner's Ring to Cheer"

### 4.1 Flow Overview & User Goal
- **User Goal**: Send an instant cheer or celebration to the partner through direct touch manipulation on their activity ring, avoiding vertical scrolling or hunting for action buttons.
- **Ergonomic Context**: On compact phone screens (<600dp), the bottom reaction bar is often below the fold; double-tapping the hero ring provides immediate spatial affordance.

### 4.2 End-to-End User Flow Diagram

```mermaid
flowchart TD
    APP_TODAY(("User on Today Dashboard")) --> VIEW_GAUGES["SideBySideActivityCard (Activity Gauges)"]
    
    VIEW_GAUGES --> TOUCH_EVENT(["User Touch on Partner's Motion Gauge Canvas"])
    
    TOUCH_EVENT --> TAP_DETECT{"PointerInput: detectTapGestures()"}
    
    TAP_DETECT -- Single Tap --> TOOLTIP["Display Partner Telemetry Quick Tooltip<br/>(e.g., 'Goal: 10,000 steps • 72% completed')"]
    TOOLTIP --> DISMISS_TOOLTIP[["Auto-dismiss after 2.5s or on outside tap"]]
    
    TAP_DETECT -- Double Tap (Within 300ms) --> THROTTLE_CHECK{"Cheer Sent in Last 3s?"}
    
    THROTTLE_CHECK -- Yes (Spam Prevented) --> HAPTIC_REJECT[["System: HapticFeedbackType.Reject / Double Buzz"]]
    HAPTIC_REJECT --> TOAST_COOLDOWN["Transient Toast: 'Partner cheered! Give them a breather ⏱️'"]
    
    THROTTLE_CHECK -- No (Eligible) --> TRIGGER_CHEER["System: Fire Cheer Event"]
    
    TRIGGER_CHEER --> PARALLEL_1["1. Local Visual Feedback:<br/>Heart & Confetti Particle Overlay Emitted<br/>(Spring Animation: Scale 1.0 -> 1.4 -> 0.0)"]
    TRIGGER_CHEER --> PARALLEL_2["2. Local Tactile Feedback:<br/>LocalHapticFeedback.performHapticFeedback(<br/>HapticFeedbackType.LongPress / Heartbeat)"]
    TRIGGER_CHEER --> PARALLEL_3["3. Network Action:<br/>HealthConnectViewModel.sendNudgeToPartner(<br/>'Cheered your activity! 🔥❤️')"]
    
    PARALLEL_3 --> SEND_API[["Cloud Function / FCM Dispatch"]]
    SEND_API --> PARTNER_PHONE[["Partner Device Receives Cheer"]]
    
    PARTNER_PHONE --> PARTNER_EFFECTS["Partner Experience:<br/>• Haptic Heartbeat Vibration<br/>• Floating Heart Toast Notification<br/>• Partner's Activity Gauge pulses green"]

    style APP_TODAY fill:#4A148C,color:#fff
    style VIEW_GAUGES fill:#1565C0,color:#fff
    style TRIGGER_CHEER fill:#2E7D32,color:#fff
    style TAP_DETECT fill:#E65100,color:#fff
    style THROTTLE_CHECK fill:#E65100,color:#fff
```

### 4.3 Micro-Interaction Specifications
- **Double-Tap Window**: 300ms max between pointer down events; touch slop radius ≤ 18dp.
- **Particle Dynamics**: 8–12 semi-transparent floating hearts and sparks radiating outward from the gauge center with gravity damping and 450ms lifetime.
- **Accessibility Alternative (WCAG 2.5.1)**: Double-tap is an enhanced shortcut; the standard accessible single-tap `QuickNudgeRow` buttons remain fully available below for assistive tech and TalkBack.

---

## 5. FLOW-03: Offline / Stale Telemetry Transparency & Recovery

### 5.1 Flow Overview & User Goal
- **User Goal**: Immediately distinguish between a partner who is resting vs. a partner whose device is disconnected/dead, with zero guesswork and an active path to sync.
- **The Core Problem**: In wearable IoT pairing, static values cause relationship anxiety ("Is my partner okay? Why haven't they walked?").

### 5.2 End-to-End User Flow Diagram

```mermaid
flowchart TD
    OPEN_APP(("User Opens Today Screen")) --> LOAD_DATA[["HealthConnectViewModel loads UI State<br/>(myDailyAggregate + partnerDailyRecord)"]]
    
    LOAD_DATA --> EVAL_DELTA{"Calculate Delta:<br/>System.currentTimeMillis() - partnerLastSyncTime"}
    
    EVAL_DELTA -- Delta ≤ 15 Minutes --> FRESH_STATE["Fresh Telemetry State:<br/>• Standard Status Pill: '🟢 Synced Xm ago'<br/>• Vitals displayed with full opacity<br/>• Normal live synchrony ring"]
    
    EVAL_DELTA -- Delta > 15 Minutes --> STALE_STATE["Stale Telemetry State:<br/>• Amber Status Pill above gauges:<br/>'🟡 Partner synced 42m ago • Cached'"]
    
    STALE_STATE --> USER_TAP_PILL(["User Taps Amber Status Pill"])
    
    USER_TAP_PILL --> OPEN_SHEET["Open Telemetry Diagnostic Bottom Sheet"]
    
    OPEN_SHEET --> SHEET_CONTENT["Diagnostic Sheet Displays:<br/>1. Last known sync: Time & Battery Level<br/>2. Disconnection probable cause<br/>3. Action: 'Ping Partner Device 📡'<br/>4. Action: 'View Offline Details'"]
    
    SHEET_CONTENT --> USER_PING(["User Taps 'Ping Partner Device 📡'"])
    
    USER_PING --> SEND_PING[["System: Send High-Priority Silent FCM Push<br/>Topic: sync_request / targetUid"]]
    
    SEND_PING --> PING_RESULT{"Ping Delivery Status"}
    
    PING_RESULT -- Partner Phone Off / No Network --> PING_TIMEOUT["Sheet: 'Partner unreachable.<br/>Device may be offline or in battery saver.'"]
    PING_TIMEOUT --> RETRY_OPT["Offer SMS / WhatsApp Nudge Link"]
    
    PING_RESULT -- Partner Phone Receives Ping --> BACKGROUND_SYNC[["Partner OS wakes HealthSyncWorker<br/>(Queries Watch / Health Connect SDK)"]]
    BACKGROUND_SYNC --> UPLOAD_FIRESTORE[["Partner Worker uploads fresh snapshot to Cloud"]]
    UPLOAD_FIRESTORE --> LISTENER_TRIGGER[["User App Firestore SnapshotListener fires"]]
    LISTENER_TRIGGER --> UPDATE_ROOM[["Room HealthDao updates partnerDailyRecord"]]
    UPDATE_ROOM --> FRESH_ANIM["UI Transition:<br/>Status Pill morphs to '🟢 Just synced'<br/>Activity gauges animate to updated values"]
    FRESH_ANIM --> CLOSE_SHEET[["Auto-dismiss Diagnostic Sheet after 1.5s"]]

    style OPEN_APP fill:#4A148C,color:#fff
    style FRESH_STATE fill:#2E7D32,color:#fff
    style STALE_STATE fill:#F57C00,color:#fff
    style OPEN_SHEET fill:#1565C0,color:#fff
    style EVAL_DELTA fill:#E65100,color:#fff
    style PING_RESULT fill:#E65100,color:#fff
```

### 5.3 Diagnostic Status Taxonomy

| Delta Horizon | Visual Token | Status Copy | Interaction Affordance |
| :--- | :--- | :--- | :--- |
| **0 – 15 mins** | `SystemGreen` (🟢) | `Synced Xm ago` | Passive confirmation; no action required. |
| **16 – 60 mins** | `SystemOrange` (🟡) | `Partner synced Xm ago • Showing cached vitals` | Tappable pill; opens Diagnostic Sheet. |
| **> 60 mins** | `SystemRed` (🔴) | `Partner inactive since HH:MM • Sync stalled` | Emphasized banner; highlights "Ping Partner" CTA. |
| **Local User Offline**| `SystemGray` (⚪) | `You are offline • Showing local device cache` | Prompts local Wi-Fi / Cellular check. |

---

## 6. Implementation Blueprint & File Touchpoints

| Flow ID | Feature Component | Primary Files Touched | Architectural Layer |
| :--- | :--- | :--- | :--- |
| **`FLOW-01`** | Interactive Glance Widget | [`CoupleFitAppWidget.kt`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/widget/CoupleFitAppWidget.kt), `PokeActionCallback.kt` | UI / Glance AppWidget & WorkManager |
| **`FLOW-02`** | Double-Tap Ring Cheer | [`DashboardScreen.kt`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/ui/dashboard/DashboardScreen.kt) (`SideBySideActivityCard`), `ConfettiParticleOverlay.kt` | Compose UI / PointerInput & Motion |
| **`FLOW-03`** | Stale Telemetry Status Pill & Ping | [`DashboardScreen.kt`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/ui/dashboard/DashboardScreen.kt), `HealthConnectViewModel.kt`, `SyncNotificationService.kt` | UI / ViewModel & Cloud Messaging |

---

## 7. Quality & Verification Gates

1. **Automated Unit Tests**:
   - `testDebugUnitTest`: Verify delta time calculation logic, status string formatting, and FCM payload serialization.
2. **Instrumented Compose Tests**:
   - Verify double-tap detection on `SideBySideActivityCard` without interfering with scroll.
   - Verify Glance widget provides content without crashing when Room is empty.
3. **Live ARTEMIS Device Verification**:
   - Verify visual rendering of widget, double-tap particle burst, and amber status pill on `emulator-5554`.
