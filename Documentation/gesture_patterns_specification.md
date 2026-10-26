# CoupleFit — Gesture Patterns UX & Interaction Specification 👆✨

> **Command Trigger**: `/designer-skills:gesture-patterns`  
> **Associated Defect Logs in Fixinglist.md**: `GST-01`, `GST-02`, `GST-03`


> **Mobile & Large-Screen Touch Interaction Design Guide**  
> **Target Framework**: Jetpack Compose Gesture APIs (`pointerInput`, `detectTapGestures`, `detectDragGestures`, `detectHorizontalDragGestures`)  
> **Audited Standards**: Material Design 3 Gestures, iOS Human Interface Guidelines, WCAG 2.2 Success Criterion 2.5.1 (Pointer Gestures) & 2.5.2 (Pointer Cancellation)  
> **Companion Documents**: [`Fixinglist.md`](file:///D:/Couple_fit/CoupleFit/Documentation/Fixinglist.md), [`feedback_patterns_architecture.md`](file:///D:/Couple_fit/CoupleFit/Documentation/feedback_patterns_architecture.md)

---

## 1. Executive Summary & Gesture Audit Score (6.4 / 10)

Gesture-based interaction is the foundation of high-craft mobile interfaces. In a shared health app, gestures can create playful, direct emotional connections between partners.

### 1.1 Existing Gesture Strengths
- **Continuous Chart Scrubbing**: [`InteractiveChart.kt`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/ui/components/InteractiveChart.kt#L377-L415) features continuous drag scrub with real-time haptic tick feedback (`TextHandleMove`).
- **Pull-To-Refresh**: [`DashboardScreen.kt`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/ui/dashboard/DashboardScreen.kt#L180-L215) provides an intuitive downward pull to trigger sync.
- **Lenis Smooth Scroll Physics**: Custom overscroll damping on the main dashboard column.

### 1.2 Critical Flaws & Deficiencies
1. **Vertical/Horizontal Scroll Lock Collision in Charts**: In [`InteractiveChart.kt`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/ui/components/InteractiveChart.kt#L406), `change.consume()` unconditionally consumes pointer events during chart scrubbing inside a vertically scrolling `lenisScroll()` container. This causes jitter, trapped gestures, and erratic scrolling when dragging diagonally.
2. **Missing Slop / Threshold Detection**: The scrubber activates immediately upon pointer down without testing whether the user intended to scroll vertically down the dashboard or scrub horizontally across the graph.
3. **Zero Direct Manipulation on Hero Activity Rings**: The primary visual figure of the app (the concentric Activity Rings) is purely static and non-interactive. Tapping, double-tapping, or rotating does nothing.
4. **WCAG 2.5.1 Compliance Gap**: No non-gesture alternatives exist for scrubber tooltips or horizontal carousels.

---

## 2. Critical Changes Needed (Remediation Plan)

### Critical Fix 1: Chart Scrubber Touch Slop & Direction Lock (`GST-01`)
- **Problem**: When users scroll down the dashboard and touch near a chart, the chart intercepts the touch immediately without waiting for horizontal movement threshold, stalling dashboard scrolling.
- **Remediation**: Use `detectHorizontalDragGestures` with an initial horizontal drag threshold (touch slop > 12px) before consuming events, allowing vertical scroll events to pass through seamlessly to the parent container.

```kotlin
// Optimized Horizontal Chart Scrubber with Directional Disambiguation
Modifier.pointerInput(data) {
    detectHorizontalDragGestures(
        onDragStart = { offset ->
            updateScrubbedIndex(offset.x)
        },
        onDragEnd = {
            scrubbedIndex = null
        },
        onDragCancel = {
            scrubbedIndex = null
        },
        onHorizontalDrag = { change, _ ->
            change.consume() // Only consumes AFTER horizontal slop is exceeded
            updateScrubbedIndex(change.position.x)
        }
    )
}
```

---

### Critical Fix 2: Pointer Cancellation Zone & Release Safety (`GST-02`)
- **Problem**: In destructive or critical modals (e.g. Unlinking Partner or Starting Challenge), lifting a finger outside the button boundary does not cancel cleanly.
- **Remediation**: In accordance with **WCAG 2.5.2 (Pointer Cancellation)**, all button actions must execute on `Up` (touch release), and sliding more than 30dp outside the target button boundary must automatically abort the activation without firing.

---

## 3. High-Value Gesture Innovations for CoupleFit 🚀

To elevate the app from functional utility to an intimate, beloved companion, we specify **5 high-delight gestures**:

```
 1. Double-Tap Partner's Ring             2. Pull-Down Over-Scroll Kiss
   ┌───────────────────────┐                ┌───────────────────────┐
   │     (( ❤️ Burst ))    │                │  Pull down past 120dp │
   │   ( [Partner Ring] )  │                │  (Hearts rain down)   │
   │  Instant Cheer & Buzz │                │  "Synced with love"   │
   └───────────────────────┘                └───────────────────────┘
 3. Long-Press Metric Peel                4. Swipe-to-Poke on Standings
   ┌───────────────────────┐                ┌───────────────────────┐
   │  Hold 400ms on Steps  │                │  Swipe partner row    │
   │  Expands Hourly Beats │                │  reveals [👋 Poke]    │
   └───────────────────────┘                └───────────────────────┘
 5. Pinch-to-Span Chart Zoom
   ┌───────────────────────┐
   │  Pinch 2 fingers      │
   │  Day ↔ Week ↔ Month   │
   └───────────────────────┘
```

---

### 3.1 Gesture 1: "Double-Tap Partner's Ring to Cheer" (High Delight)
- **Mental Model**: Like Instagram/TikTok double-tap to like, but with physical haptic intimacy.
- **Interaction Spec**:
  - **Trigger**: Double-tap directly within the outer/partner ring radius on `ActivityRing.kt`.
  - **Visual Feedback**: Radial 12-particle heart confetti burst radiating outward from the touch point.
  - **Sensory Feedback**: Dual-pulse heartbeat haptic (`15ms @ 40%` $\to$ `75ms` $\to$ `22ms @ 85%`).
  - **Network Action**: Sends an instant lightweight `POKE` nudge via Firebase in the background.

```kotlin
@Composable
fun PartnerRingInteractiveOverlay(
    onDoubleTapCheer: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { offset ->
                        onDoubleTapCheer()
                    }
                )
            }
    )
}
```

---

### 3.2 Gesture 2: "Pull-Down Over-Scroll Intimacy Reveal"
- **Mental Model**: Extending standard pull-to-refresh beyond utilitarian sync.
- **Interaction Spec**:
  - Pulling down past standard refresh threshold (80dp) to 120dp reveals a warm bilateral status card:
    - *"Akash & Partner: 14,280 steps together today • 4-day streak 🔥"*
  - Releasing past 120dp triggers an animated celebratory particle burst and refreshes Health Connect data.

---

### 3.3 Gesture 3: "Swipe-to-Nudge on Standings Leaderboard"
- **Mental Model**: iOS Mail / Android Messages swipe action.
- **Interaction Spec**:
  - On the **Duels & Standings** card, swiping the partner's row to the left by 64dp reveals quick action pills:
    - `[👋 Poke]` `[🔥 Cheer]` `[💬 Ping]`
  - Swiping fully past 140dp triggers an immediate quick cheer with a haptic click.

---

### 3.4 Gesture 4: "Long-Press Metric Peel" (Progressive Disclosure)
- **Mental Model**: Peek and pop for daily telemetry.
- **Interaction Spec**:
  - Long-pressing (400ms) any metric card (Steps, Active Calories, Water) smoothly expands a floating translucent overlay showing hourly distribution and partner comparison without navigating away from the dashboard.
  - Lifting the finger smoothly collapses the card back into place.

---

### 3.5 Gesture 5: "Pinch-to-Span Chart Resolution"
- **Mental Model**: Pinch to zoom time scales.
- **Interaction Spec**:
  - On `InteractiveChart.kt`, two-finger pinch-in switches time resolution from **Day** $\to$ **Week** $\to$ **Month**.
  - Pinch-out zooms back into granular hourly segments.
  - Eliminates the need to tap small segmented pills at the top of the screen.

---

## 4. Accessibility & Non-Gesture Alternatives (WCAG 2.5.1)

In compliance with **WCAG 2.5.1 Pointer Gestures**, every gesture interaction must have a visible, single-tap alternative:
1. **Double-Tap Ring Cheer**: Maintained alongside the visible `[👋 Poke]` button below the ring.
2. **Chart Pinch Zoom**: Maintained alongside standard `[D] [W] [M] [Y]` segmented filter chips.
3. **Swipe-to-Nudge**: Standings card displays a persistent 3-dot overflow menu icon with all nudge actions available in a standard popup.
4. **Scrubber Drag**: Accessible via left/right stepper buttons for TalkBack screen reader navigation.

---

## 5. Verification & Testing Checklist

- [ ] **Touch Slop Disambiguation**: Verify vertical dashboard scrolling does not get stuck when initiating scrolls on top of `InteractiveChart.kt`.
- [ ] **Pointer Cancellation**: Drag touch 40dp away from buttons in dialogs before releasing; verify action does not fire.
- [ ] **Double-Tap Ring**: Test double-tap latency threshold (300ms window); ensure single tap does not misfire.
- [ ] **TalkBack Non-Gesture Alternatives**: Verify TalkBack accessibility focus moves cleanly through all data points without requiring continuous drag.
