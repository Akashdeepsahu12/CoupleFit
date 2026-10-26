# CoupleFit — Data Visualization QA, Chart Audit & Design Recommendations 📊📈

> **Command Trigger**: `/designer-skills:data-visualization`  
> **Associated Defect Logs in Fixinglist.md**: `DTV-01`, `DTV-02`, `DTV-03`


> **Telemetry Visualization & Quantitative Display Audit**  
> **Audited Component**: [`InteractiveChart.kt`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/ui/components/InteractiveChart.kt) (771 lines)  
> **Audited Standards**: Edward Tufte Data-Ink Ratio, Cleveland & McGill Graphical Perception Hierarchy, WCAG 2.2 Colorblind Legibility (Deuteranopia/Protanopia)  
> **Companion Documents**: [`Fixinglist.md`](file:///D:/Couple_fit/CoupleFit/Documentation/Fixinglist.md), [`motion_system_architecture.md`](file:///D:/Couple_fit/CoupleFit/Documentation/motion_system_architecture.md)

---

## 1. Executive Summary & Chart QA Score (7.4 / 10)

[`InteractiveChart.kt`](file:///D:/Couple_fit/CoupleFit/app/src/main/java/com/couplefit/ui/components/InteractiveChart.kt) is the central quantitative telemetry engine of CoupleFit. It visualizes continuous biometric streams (steps, heart rate, active calories, SpO2) across multiple time horizons (`[D] [W] [M] [Y]`).

### 1.1 The Scorecard
| Visualization Dimension | Score | Evaluation & Findings |
| :--- | :---: | :--- |
| **1. Graphical Perception & Fit** | `8.5 / 10` | Cubic Bézier smooth spline curves convey biological continuity exceptionally well; single-point fallback baseline is elegant. |
| **2. Data-Ink Ratio (Tufte)** | `7.8 / 10` | 3 subtle dashed gridlines; eliminates heavy grid clutter; area fill gradient provides depth without occluding coordinates. |
| **3. Multi-Series Comparative Encoding** | `5.2 / 10` | **Major Flaw**: The chart currently displays only a **single user's stream**. In a couple accountability app, partner telemetry is invisible on the main curve. |
| **4. Interactivity & Direct Inspection**| `8.0 / 10` | Continuous scrubbing with haptic ticks (`TextHandleMove`) and live floating tooltip pill; however, lacks magnetic snapping to hourly nodes. |
| **5. Colorblind Safety & Contrast** | `6.8 / 10` | High contrast on OLED canvas, but tooltip text (`11sp`) merges with background curves without an elevation drop shadow. |
| **6. Touch Ergonomics & Axis Legibility**| `7.9 / 10` | Clean right-aligned Y-axis labels and bottom X-axis time points; 10dp breathing room prevents stroke clipping. |
| **Overall Chart Rating** | **`7.4 / 10`** | **Visually striking single-series curve, but fundamentally lacking bilateral partner comparison.** |

---

## 2. Key Chart Strengths in Codebase

1. **Graceful Single-Point Handling**: Lines 467–512 elegantly avoid rendering a broken flat line when only 1 telemetry reading exists; instead, it renders a subtle reference baseline with a pulsing center coordinate beacon.
2. **Apple-Style Progressive Reveal**: Smooth path-measurement reveal animation (`revealProgress` using `PathMeasure.getSegment`) draws the curve from left to right on initial load.
3. **Statistical Takeaway Integration**: Lines 105–112 calculate statistical minimums, maximums, and averages, driving the dynamic counter header.

---

## 3. Critical Chart Defects & Deficiencies Identified

### Defect 1: Bilateral Telemetry Blindspot (`DTV-01`)
- **Problem**: In an app named **CoupleFit**, `InteractiveChart.kt` accepts a single `List<ChartPoint>` (`val data`). Users cannot visually compare their curve against their partner's curve on the same coordinate axis.
- **Cognitive Impact**: Destroys shared synchrony and friendly competition; forces users to mentally remember numbers rather than visually perceiving the gap between lines.
- **Remediation**: Upgrade chart to a **Bilateral Multi-Series Waveform**:
  - **User Curve**: Solid vibrant Cyan line (`#00E5FF`) with 2.5dp stroke.
  - **Partner Curve**: Dotted or semi-translucent Coral line (`#FF4568`) with 2.0dp stroke.
  - **Gap Fill (Delta)**: Area between the two lines subtly illuminated in the leading partner's color.

---

### Defect 2: Tooltip Silhouetting & Text Legibility Collision (`DTV-02`)
- **Problem**: When scrubbing across peaks, the floating callout tooltip pill slides directly across high-contrast Bézier curves and dashed gridlines without elevation shadow or specular rim isolation.
- **Visual Impact**: Micro 11sp typography (`labelSmall`) inside the tooltip pill visually merges with underlying strokes, breaking character legibility.
- **Remediation**: Add `Modifier.shadow(elevation = 6.dp)` and a 1px top specular rim highlight to the floating tooltip container.

---

### Defect 3: Lack of Magnetic Node Snapping (`DTV-03`)
- **Problem**: Scrubbing calculates a continuous floating-point horizontal offset. Because telemetry points occur at discrete intervals (e.g. 1:00 PM, 2:00 PM), landing on exact hour marks requires fine-motor dexterity.
- **User Impact**: Difficult to scrub while walking or running on a treadmill.
- **Remediation**: Implement a **14dp magnetic capture radius**: when the scrubber gets within 14dp of an hourly telemetry node, it magnetically snaps into place with a sharp `EFFECT_TICK` haptic bump.

---

### Defect 4: Absence of Non-Gesture Stepper Controls (WCAG 2.5.1) (`DTV-04`)
- **Problem**: Reading historical points is accessible *only* via continuous touch drag. Low-dexterity users or users relying on TalkBack cannot scrub to inspect hourly breakdowns.
- **Remediation**: Provide discreet previous/next chevron steppers or expose TalkBack actions (`AccessibilityAction`) to step through data nodes sequentially.

---

## 4. Modern Data Visualization Techniques Recommended for CoupleFit 🚀

```
 1. Bilateral Dual-Stream Overlay         2. Leading Partner Delta Fill
   ┌───────────────────────┐                ┌───────────────────────┐
   │ ── User (Cyan #00E5FF)│                │ Area between curves   │
   │ ┄┄ Partner (Coral)    │                │ shows who is ahead    │
   │ Side-by-side synchrony│                │ at each hour of day   │
   └───────────────────────┘                └───────────────────────┘
 3. Magnetic Hour Snapping                4. Contextual Goal Horizon Line
   ┌───────────────────────┐                ┌───────────────────────┐
   │ Cursor snaps to node  │                │ ──── 10,000 Step Goal │
   │ 14dp capture radius   │                │ Dynamic golden line   │
   │ [12:00 PM • 6,420]    │                │ shows progress to goal│
   └───────────────────────┘                └───────────────────────┘
```

---

### 4.1 Technique 1: Bilateral Dual-Series Waveform
Render both partners on a unified coordinate system:

```kotlin
@Composable
fun BilateralInteractiveChart(
    userData: List<ChartPoint>,
    partnerData: List<ChartPoint>,
    goalValue: Float? = 10000f,
    modifier: Modifier = Modifier
) {
    // Canvas plots both paths:
    // 1. User Path: Solid Cyan (#00E5FF)
    // 2. Partner Path: Dashed or translucent Coral (#FF4568)
    // 3. Goal Line: Subtle gold horizontal rule (#FFD60A)
}
```

---

### 4.2 Technique 2: "Ahead / Behind" Delta Zone Shading
Fill the vertical space between the User and Partner lines:
- When **User is ahead**, the area between lines glows with a faint 8% Cyan tint.
- When **Partner is ahead**, the area between lines glows with a faint 8% Coral tint.
- Instantly communicates daily lead changes throughout the day at a single glance.

---

### 4.3 Technique 3: Goal Horizon Benchmark Line
- Draw an understated horizontal rule at the target step goal (e.g. 10,000 steps).
- When a partner's curve crosses above the goal line, the curve stroke subtly blooms with an ambient glow, visually reinforcing achievement.

---

## 5. Verification & Testing Checklist

- [ ] **Bilateral Rendering**: Test chart with both user and partner data streams active; verify zero label collision.
- [ ] **Tooltip Elevation**: Confirm tooltip pill text is 100% legible against high-density curve peaks.
- [ ] **Magnetic Snap**: Scrub across 24 hourly data points; confirm sharp `EFFECT_TICK` haptic on node capture.
- [ ] **Single-Point Test**: Provide a list with only 1 point; verify baseline and center beacon render cleanly without NaN/crash.
- [ ] **Accessibility / TalkBack**: Enable TalkBack and swipe; verify individual hourly values are read sequentially.
