# CoupleFit — Feedback Patterns Architecture & System Specification ⚡🔔

> **Command Trigger**: `/designer-skills:feedback-patterns`  
> **Associated Defect Logs in Fixinglist.md**: `FDB-01`, `FDB-02`, `FDB-03`


> **Multisensory Feedback Design Guide**  
> **Target Framework**: Jetpack Compose / Material 3 / Android Haptics API (`Vibrator` & `VibrationEffect`)  
> **Audited Standards**: Nielsen Norman Group Usability Heuristics, WCAG 2.2 Accessibility, Emil Kowalski Sensory Feedback Principles  
> **Companion Documents**: [`Fixinglist.md`](file:///D:/Couple_fit/CoupleFit/Documentation/Fixinglist.md), [`error_handling_and_loading_states.md`](file:///D:/Couple_fit/CoupleFit/Documentation/error_handling_and_loading_states.md)

---

## 1. Executive Summary & Design Vision

In **CoupleFit**, feedback is the sensory glue of the shared bilateral experience. Fitness is inherently physical, and an accountability app for couples must mirror that physicality. A tap is not just a digital event; it is an interpersonal touchpoint:
- A cheer sent to a partner must feel distinct from a system warning.
- Closing a daily activity ring must feel celebratory and momentous.
- Toggling a privacy setting must confirm immediate state certainty with an effortless safety net (undo).

This specification establishes a **multimodal feedback architecture** across 4 sensory layers:
1. **Immediate Feedback** (0–50ms): Micro-animations, mechanical button deflections, and haptic clicks.
2. **Confirmation Feedback** (50–300ms): In-context state mutations, checkmark morphs, and undoable floating snackbars.
3. **Status Feedback** (Continuous / Ambient): Activity indicators, real-time sync pulses, and partner presence badges.
4. **Notification Feedback** (Push / In-App Heads-Up): Gentle incoming nudges and shared milestone celebrations.

---

## 2. The 4-Level Feedback Hierarchy Matrix

Feedback delivery follows the **Proximity Principle**: acknowledge actions as close to the physical point of touch as possible before escalating up the screen.

```
┌────────────────────────────────────────────────────────┐
│             Level 4: System / Notification             │  FCM push, glance widget, ambient status bar
├────────────────────────────────────────────────────────┤
│             Level 3: Page / Global Dock                │  Floating undo snackbar, top offline banner
├────────────────────────────────────────────────────────┤
│             Level 2: Component / Card                  │  Card border pulse, animated checkmark, badge
├────────────────────────────────────────────────────────┤
│             Level 1: Inline / Direct Touch             │  Spring compression, haptic tick, particle burst
└────────────────────────────────────────────────────────┘
```

| Feedback Level | Trigger Action | Sensory Channel | Component Used | Duration / Dismissal |
| :--- | :--- | :--- | :--- | :--- |
| **Level 1: Inline** | Button tap, toggle switch, slider drag. | Visual (0.975x scale) + Tactile (`EFFECT_CLICK`). | Interactive element itself. | Immediate (0–120ms release). |
| **Level 1: Scrubber** | Chart scrubbing across hourly telemetry points. | Visual (magnetic node) + Tactile (`EFFECT_TICK`). | Scrubber cursor & tooltip. | Real-time during drag. |
| **Level 2: Component** | Daily goal reached, medication logged. | Visual (Vector path morph) + Haptic (Ascending chime). | Inline card header / checkmark. | 600ms animated transition. |
| **Level 3: Page** | Privacy toggle, preference change, unlinking. | Floating M3 Glass Snackbar with `[Undo]` action. | Screen bottom dock (above nav). | Auto-dismisses in 4.0s. |
| **Level 4: System** | Partner sent a cheer or closed their ring. | Heads-up banner + Dual-pulse heartbeat haptic. | Notification banner / Ring burst. | 5.0s or tap to dismiss. |

---

## 3. Feedback Modalities & Sensory Design Specs

### 3.1 Visual Feedback Systems
- **Color & Vector Redundancy**: Feedback **never relies on color alone**. Every success or error pairs color with an explicit vector icon (e.g., `#32D74B` + `CheckCircle`, `#FF4568` + `AlertCircle`).
- **Path-Morph Checkmark**: When completing tasks (e.g., logging a workout or medication), the icon morphs from a circle to a checkmark using `AnimatedVectorDrawable` or Compose Canvas sweep.
- **Directional Specular Pulse**: Submitting actions highlights the card's top rim with a brief traveling specular light beam (250ms).

---

### 3.2 Haptic Feedback Orchestration (`CoupleFitHaptics`)

Standard Android `HapticFeedbackType.LongPress` is blunt and feels like an error. CoupleFit implements a dedicated haptic engine using `VibrationEffect.createWaveform`:

```kotlin
package com.couplefit.ui.theme

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Custom sensory feedback engine for CoupleFit.
 * Encodes Emil Kowalski's micro-haptics and emotional feedback curves.
 */
class CoupleFitHaptics(context: Context) {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        manager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    /**
     * Crisp mechanical click for button presses and tab switches.
     */
    fun tap() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
        }
    }

    /**
     * Subtle magnetic tick for scrubbing charts and scrolling wheels.
     */
    fun tick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
        }
    }

    /**
     * Dual-pulse "Heartbeat" for partner nudges, cheers, and pokes.
     * Pattern: 15ms tap @ 40% -> 75ms rest -> 22ms tap @ 85% amplitude.
     */
    fun partnerHeartbeat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && vibrator?.hasAmplitudeControl() == true) {
            val timings = longArrayOf(0, 15, 75, 22)
            val amplitudes = intArrayOf(0, 100, 0, 220)
            vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
        } else {
            tap()
        }
    }

    /**
     * Ascending triple-crescendo fanfare when closing a daily activity ring.
     */
    fun ringClosedCelebration() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && vibrator?.hasAmplitudeControl() == true) {
            val timings = longArrayOf(0, 20, 60, 25, 60, 45)
            val amplitudes = intArrayOf(0, 90, 0, 160, 0, 255)
            vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
        } else {
            tap()
        }
    }
}
```

---

## 4. Confirmation & Undo Architecture (Reversible Actions)

### 4.1 "Undo" Over "Are You Sure?" Dialogs
Interruptive modal dialogs ("Are you sure you want to turn off live sync?") cause decision fatigue. For reversible settings:
1. Apply change **immediately** (Optimistic UI).
2. Show a floating **Snackbar with an Undo Action** anchored right above the navigation dock:
   ```
   ┌────────────────────────────────────────────────────────┐
   │ ℹ️ Heart Rate Sharing turned OFF          [UNDO]  (3s) │
   └────────────────────────────────────────────────────────┘
   ```
3. If user taps `[UNDO]`, revert state instantly with a gentle tactile click.
4. If timer expires, commit change permanently to DataStore.

---

### 4.2 Floating Glass Feedback Host (`CoupleFitFeedbackHost`)

```kotlin
package com.couplefit.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.couplefit.ui.theme.AppSpacing

data class FeedbackMessage(
    val id: Long = System.currentTimeMillis(),
    val text: String,
    val icon: @Composable (() -> Unit)? = null,
    val actionLabel: String? = null,
    val onAction: (() -> Unit)? = null,
    val durationMillis: Long = 3500L
)

@Composable
fun CoupleFitFeedbackBar(
    message: FeedbackMessage?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = message != null,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier
    ) {
        if (message != null) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF1E2129).copy(alpha = 0.94f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm)
                    .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp)),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = AppSpacing.md, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    message.icon?.invoke()
                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFE2E5EB)
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    if (message.actionLabel != null && message.onAction != null) {
                        TextButton(
                            onClick = {
                                message.onAction.invoke()
                                onDismiss()
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Text(
                                text = message.actionLabel,
                                color = Color(0xFF00E5FF),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
```

---

## 5. Partner Presence & Ambient Status Feedback

To foster continuous intimacy and synchrony, feedback extends beyond user taps into **ambient relationship telemetry**:

| Partner State | Visual Indicator | Micro-Motion | Meaning |
| :--- | :--- | :--- | :--- |
| **Partner Online & Active** | Solid Green dot (`#32D74B`) beside avatar | Static | Telemetry streaming in real-time. |
| **Partner Exercising Now** | Pulsing Cyan Ring (`#00E5FF`) | Gentle 2s breathing pulse | Active workout in progress (running/cycling). |
| **Partner Inactive / Stale** | Subtle Slate dot (`#6C7280`) | Static | Last synced >15m ago (battery saver/sleep). |
| **Nudge Acknowledged** | Tiny heart icon badge on partner ring | 300ms pop & scale-in | Partner saw or tapped your incoming cheer. |

---

## 6. Implementation & Verification Roadmap

- [ ] **Custom Haptic Engine**: Implement `CoupleFitHaptics.kt` supporting mechanical clicks, chart scrubber ticks, and partner heartbeat waveforms.
- [ ] **Feedback Bar Host**: Integrate `CoupleFitFeedbackBar` into the root app scaffold for floating undo confirmations.
- [ ] **Undo Architecture**: Wire DataStore privacy toggles to optimistic local state with a 4-second undo snackbar.
- [ ] **Celebration Payoff**: Wire ring closure event to trigger particle confetti + `ringClosedCelebration()` haptic pattern.
- [ ] **Accessibility Audit**: Verify all feedback states emit clear `contentDescription` announcements for TalkBack screen readers.
