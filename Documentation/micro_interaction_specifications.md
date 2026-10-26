# CoupleFit — Micro-Interaction Engineering & Interaction Specifications ⏱️✨

> **Command Trigger**: `/designer-skills:micro-interaction-spec`  
> **Associated Defect Logs in Fixinglist.md**: `MIC-01`, `MIC-02`, `MIC-03`, `MIC-04`


> **Micro-Interaction Architecture & Physics Spec**  
> **Target Framework**: Jetpack Compose Animation (`Animatable`, `spring`, `graphicsLayer`) & Android Haptics  
> **Audited Standards**: Dan Saffer Micro-Interaction Framework (Trigger $\to$ Rules $\to$ Feedback $\to$ Loops/Modes), Emil Kowalski Polish Rules, WCAG 2.2 Level AA  
> **Companion Documents**: [`Fixinglist.md`](file:///D:/Couple_fit/CoupleFit/Documentation/Fixinglist.md), [`gesture_patterns_specification.md`](file:///D:/Couple_fit/CoupleFit/Documentation/gesture_patterns_specification.md), [`feedback_patterns_architecture.md`](file:///D:/Couple_fit/CoupleFit/Documentation/feedback_patterns_architecture.md)

---

## 1. Executive Summary & Micro-Interaction Philosophy

Micro-interactions are the functional moments that elevate software from a cold database interface into an intimate, living experience. In **CoupleFit**, every micro-interaction is designed around four core tenets:
1. **Mechanical Restraint**: Spring physics replace arbitrary duration timers. Nothing bounces or wobbles unnecessarily; movements feel weighted and calibrated.
2. **Interpersonal Synchrony**: Actions mirror physical gestures between partners (heartbeats, taps, synchronized milestones).
3. **Respect for Focus**: Total animation durations stay between **120ms and 450ms** so user flow is never blocked.
4. **Accessible Grace**: Every interaction respects `reduceMotion` by gracefully substituting instant alpha fades or static states, and provides TalkBack state announcements.

---

## 2. Master Micro-Interaction Catalog

Below are detailed, production-ready specifications for the **6 primary micro-interactions** in CoupleFit:

```
┌────────────────────────────────────────────────────────────────────────┐
│                      CoupleFit Micro-Interactions                      │
├──────────────────────────┬─────────────────────────────────────────────┤
│ 1. Mechanical Button Tap │ Spring compression + M3 bounded ripple      │
│ 2. Dual-Pulse Heartbeat  │ Interpersonal poke + floating heart burst   │
│ 3. Activity Ring Closure │ 100% goal celebration + confetti sweep      │
│ 4. Magnetic Chart Scrub  │ Snap-to-hour node + tooltip elevation       │
│ 5. Streak Flame Ignition │ Consecutive day milestone path-fill         │
│ 6. Habit Completion Morph│ Checkmark vector morph + rim shine          │
└──────────────────────────┴─────────────────────────────────────────────┘
```

---

### Spec 1: Mechanical Button Tap (`MIC-SPEC-01`)

*Purpose: Provide tactile certainty and weight to all interactive CTA buttons across the app.*

| Dimension | Specification |
| :--- | :--- |
| **1. Trigger** | Pointer down on any primary or secondary button (`onTouchDown`). |
| **2. Rules** | - Down event: Immediately deflect button scale from `1.0f` to `0.975f` with alpha drop to `0.88f`.<br>- Move event: If touch travels $>30\text{dp}$ away from bounds, abort to cancellation.<br>- Up event: Release spring back to `1.0f` and fire click handler. |
| **3. Feedback** | - **Visual**: Smooth spring-damped compression.<br>- **Haptic**: Mechanical click on touch down (`EFFECT_CLICK`).<br>- **Indication**: Material 3 bounded ripple radiating from touch origin. |
| **4. Physics & Timing** | `spring(dampingRatio = 0.82f, stiffness = 750f)` — Duration: ~160ms. |
| **5. Loops / Modes** | Single execution. Rapid tapping queues retargeting from current position without popping. |
| **6. Accessibility** | `reduceMotion`: Skip spring compression; use 120ms opacity fade (`1.0f` $\leftrightarrow$ `0.7f`). |

```kotlin
@Composable
fun Modifier.mechanicalButtonPress(
    onClick: () -> Unit,
    reduceMotion: Boolean = false
): Modifier {
    val scale = remember { Animatable(1f) }
    val haptic = LocalHapticFeedback.current

    return this
        .graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
        }
        .pointerInput(reduceMotion) {
            detectTapGestures(
                onPress = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    if (!reduceMotion) {
                        scale.animateTo(0.975f, spring(dampingRatio = 0.82f, stiffness = 750f))
                    }
                    tryAwaitRelease()
                    if (!reduceMotion) {
                        scale.animateTo(1.0f, spring(dampingRatio = 0.82f, stiffness = 750f))
                    }
                    onClick()
                }
            )
        }
}
```

---

### Spec 2: Interpersonal Dual-Pulse Heartbeat Nudge (`MIC-SPEC-02`)

*Purpose: Send an affectionate, non-verbal nudge or cheer to partner with real-time feedback.*

| Dimension | Specification |
| :--- | :--- |
| **1. Trigger** | Tap on `[👋 Poke]` / `[🔥 Cheer]` or double-tap partner's outer ring. |
| **2. Rules** | - Instantly spawn 6-particle floating heart cluster floating upwards ($40\text{dp}$).<br>- Dispatch local Room mutation optimistically.<br>- Background worker pushes Firestore document to trigger partner's FCM. |
| **3. Feedback** | - **Visual**: Floating hearts scale from `0.4f` $\to$ `1.0f` while drifting upward with `0.8f` fade-out.<br>- **Haptic**: Custom Dual-Pulse Heartbeat: `15ms @ 40%` $\to$ `75ms rest` $\to$ `22ms @ 85%`.<br>- **Audio**: Optional soft double acoustic tap (`pop_soft.ogg`). |
| **4. Physics & Timing** | Total duration: 500ms. Float path uses `FastOutSlowInEasing`. |
| **5. Loops / Modes** | Throttled to max 1 nudge per 3 seconds to prevent device vibration motor flooding. |
| **6. Accessibility** | TalkBack announcement: *"Cheer sent to [Partner Name]"*. |

---

### Spec 3: Activity Ring Closure Celebration (`MIC-SPEC-03`)

*Purpose: Celebrate closing 100% of the daily step/calorie goal together.*

| Dimension | Specification |
| :--- | :--- |
| **1. Trigger** | Step or active calorie count reaches $\ge 100\%$ of target goal. |
| **2. Rules** | - Arc progress reaches $1.0$.<br>- Specular light beam sweeps along the ring perimeter ($360^\circ$).<br>- 24 Canvas confetti particles explode radially outward from ring cap ($30\text{dp}–70\text{dp}$). |
| **3. Feedback** | - **Visual**: Specular rim sweep + dual-color confetti (User Cyan `#00E5FF` + Partner Coral `#FF4568`).<br>- **Haptic**: Triple ascending crescendo fanfare: `20ms @ 35%` $\to$ `25ms @ 65%` $\to$ `45ms @ 100%`. |
| **4. Physics & Timing** | - Shimmer sweep: 400ms.<br>- Confetti gravity decay: 650ms. |
| **5. Loops / Modes** | Fires once per day per metric ring upon crossing 100% threshold. |
| **6. Accessibility** | `reduceMotion`: Confetti particles suppressed; replaced by subtle gold border pulse. |

---

### Spec 4: Magnetic Chart Scrubber Snapping (`MIC-SPEC-04`)

*Purpose: Ensure precise, effortless inspection of hourly telemetry points on historical curves.*

| Dimension | Specification |
| :--- | :--- |
| **1. Trigger** | Horizontal drag across plot area in `InteractiveChart.kt` (after exceeding 12px touch slop). |
| **2. Rules** | - Calculate nearest hourly telemetry node.<br>- If pointer is within 14dp of a node, magnetically snap vertical indicator line and tooltip pill to node.<br>- Calculate difference between user steps and partner steps at that exact hour. |
| **3. Feedback** | - **Visual**: Vertical hairline indicator shifts to node; floating glass pill floats 16dp above curve.<br>- **Haptic**: Crisp magnetic tick (`EFFECT_TICK`) as cursor crosses node boundary. |
| **4. Physics & Timing** | Instantaneous tracking with velocity damping; tooltip dismisses with 120ms fade on finger lift. |
| **5. Loops / Modes** | Continuous tracking during drag event. |
| **6. Accessibility** | Provide hardware volume button or accessibility stepper to step through hours one-by-one. |

---

### Spec 5: Streak Continuity Rail Flame Ignition (`MIC-SPEC-05`)

*Purpose: Motivate unbroken bilateral exercise habits.*

| Dimension | Specification |
| :--- | :--- |
| **1. Trigger** | Both partners complete their daily rings on consecutive days. |
| **2. Rules** | - Background track of `StreakContinuityRail` fills with electric coral/gold gradient.<br>- Day node circle illuminates from `#14161C` to vibrant Coral `#FF4568`.<br>- Flame vector icon scales up with 1.25x overshoot bounce. |
| **3. Feedback** | - **Visual**: Gradient track fill + flame bounce.<br>- **Haptic**: Success click (`EFFECT_HEAVY_CLICK`). |
| **4. Physics & Timing** | `spring(dampingRatio = 0.65f, stiffness = 500f)` — Total duration: 380ms. |
| **5. Loops / Modes** | Increments streak counter badge (`4 Days 🔥`). |
| **6. Accessibility** | Announces: *"Streak day [N] completed with partner"*. |

---

### Spec 6: Interactive Task & Medication Morph (`MIC-SPEC-06`)

*Purpose: Acknowledge medication or workout logging with satisfying closure.*

| Dimension | Specification |
| :--- | :--- |
| **1. Trigger** | Tap on `[💊 08:00 AM Vitamin D Due - Tap to Log [✓]]` chip on Today screen. |
| **2. Rules** | - Pill border shifts from Coral `#FF4568` to Muted Slate `#32D74B`.<br>- Empty circle vector smoothly morphs into a checkmark via path interpolation.<br>- Pill collapses from 48dp height to 0dp over 250ms, gently lifting remaining cards. |
| **3. Feedback** | - **Visual**: Smooth vector checkmark draw + card collapse.<br>- **Haptic**: Light confirmation tap (`EFFECT_CLICK`). |
| **4. Physics & Timing** | Path morph: 200ms `FastOutSlowInEasing`. Height collapse: 250ms. |
| **5. Loops / Modes** | Reversible via 4-second floating undo snackbar. |
| **6. Accessibility** | Immediate screen reader announcement: *"Vitamin D logged successfully"*. |

---

## 3. Implementation Checklist & Verification

- [ ] **Spring Parameters Normalized**: Replace loose springs (`damping 0.6f`) with crisp mechanical springs (`damping 0.82f`, `stiffness 750f`).
- [ ] **Haptics Integration**: Wire `CoupleFitHaptics` directly to touch event handlers in `BounceClick.kt` and `NudgeButton.kt`.
- [ ] **Accessibility Verification**: Verify all 6 micro-interactions respect `LocalConfiguration.current` and system `reduceMotion` preferences.
- [ ] **Zero Jank Gate**: Profile animations with Android Studio Macrobenchmark; ensure zero frame drops (>60fps / 120fps on high-refresh displays).
