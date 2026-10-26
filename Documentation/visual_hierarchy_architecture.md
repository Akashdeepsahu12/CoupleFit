# CoupleFit — Visual Hierarchy & Scan Architecture Specification 👁️📐

> **Command Trigger**: `/designer-skills:visual-hierarchy`  
> **Associated Defect Logs in Fixinglist.md**: `VHR-01`, `VHR-02`, `VHR-03`


> **Visual Hierarchy, Eye Flow & Information Architecture Audit**  
> **Target Framework**: Jetpack Compose / Material 3  
> **Audited Standards**: F-Pattern / Z-Pattern Reading Scanning, The Squint Test, Edward Tufte Data Hierarchy, 1.5x Visual Ratio Rule  
> **Companion Documents**: [`Fixinglist.md`](file:///D:/Couple_fit/CoupleFit/Documentation/Fixinglist.md), [`interfaces_that_feel_emotional_design.md`](file:///D:/Couple_fit/CoupleFit/Documentation/interfaces_that_feel_emotional_design.md)

---

## 1. Executive Summary & Hierarchy Score (6.8 / 10)

Visual hierarchy determines the path a user's eye takes across the screen. In a fitness companion app, users glance at their device while moving, running, or waking up. **Glanceability is survival.**

### 1.1 The Scorecard
| Hierarchy Dimension | Score | Evaluation & Key Observation |
| :--- | :---: | :--- |
| **1. Primary Focus (Entry Point)** | `7.0 / 10` | The bilateral Activity Rings are visually striking, but their surrounding card containment flattens their dominance against secondary cards. |
| **2. Typographic Scale Ratios** | `6.2 / 10` | Headline and title scales blend together (`20sp` vs `20sp`); step counter lacks a true `52sp` hero metric display token. |
| **3. Eye Flow & Scanning Patterns** | `6.5 / 10` | 9 stacked full-width cards create an undifferentiated vertical stream without clear rhythmic section breaks (Squint Test fails). |
| **4. Color Contrast Hierarchy** | `7.2 / 10` | Primary Electric Cyan and Vibrant Coral pop well, but dark cards (`#14161C`) blend into the background canvas (`#000000`). |
| **5. Action Grouping & Proximity** | `6.8 / 10` | Nudge actions are located 250dp below the rings they control; form submit buttons are orphaned outside cards. |
| **Overall Visual Hierarchy Rating** | **`6.8 / 10`** | **Good visual foundation, but flattened by card-mania and weak typographic scale ratios.** |

---

## 2. The 4-Tier Visual Hierarchy Stack for CoupleFit

To eliminate scanning fatigue, every element on the screen must fit into a strict **4-Tier Visual Pyramid**:

```
 ┌────────────────────────────────────────────────────────┐
 │           Tier 1: Primary Figure (The Hero)            │  Dual Rings + Hero Metric Counter (52sp)
 ├────────────────────────────────────────────────────────┤
 │           Tier 2: Primary Actions & Key Vitals         │  Nudge Deck [👋][🔥][❤️], Standings Rank
 ├────────────────────────────────────────────────────────┤
 │           Tier 3: Secondary Content & Breakdown        │  Telemetry Chart, Challenges, Medication Due
 ├────────────────────────────────────────────────────────┤
 │           Tier 4: Tertiary Metadata & Timestamps       │  "Synced 4m ago", battery, copyright, labels
 └────────────────────────────────────────────────────────┘
```

| Tier Level | Component | Visual Attributes | Target Eye Focus Time |
| :--- | :--- | :--- | :--- |
| **Tier 1 (Hero)** | Dual Concentric Activity Rings | $220\text{dp}$ Canvas + `52.sp` ExtraBold Counter (`tnum`). | `0 – 500ms` (Instant Glance) |
| **Tier 2 (Actions)**| Quick Nudge Deck & Current Rank | Pill shape ($44\text{dp}$), high contrast, high elevation ($4\text{dp}$). | `500ms – 1,500ms` |
| **Tier 3 (Context)**| Multi-Series Telemetry & Duels | $16\text{dp}$ card container, $16\text{sp}$ body text, dashed lines. | `1.5s – 5.0s` (Intentional Read) |
| **Tier 4 (Metadata)**| Sync status, battery, X/Y axes | $11\text{sp}$ uppercase, $0.8\text{sp}$ letter spacing, muted slate `#6C7280`.| On-demand reference |

---

## 3. The "Squint Test" Analysis & Visual Fixes

When you blur your eyes (or view the screen in direct sunlight with sunglasses):
- **Current Defect**: The user sees **9 identical dark rectangles** stacked one after another. No single card screams "I am the hero."
- **The Solution (De-Carding the Hero)**:
  1. **Emancipate the Hero Rings**: Remove the card background, borders, and shadows from the top Activity Ring container. Let the dual rings breathe directly on the dark canvas as a pure, floating celestial dial.
  2. **Consolidate Secondary Vitals**: Group secondary metrics (Active Time, Caloric Burn, SpO2) into a unified 2-column grid rather than full-width individual cards.
  3. **Logarithmic Vertical Spacing ($3:1$ Ratio)**:
     - Space between sibling cards: `12dp – 16dp`.
     - Space between major thematic sections: `36dp – 48dp` (`AppSpacing.xxl`).

---

## 4. Modern Visual Hierarchy Architecture (`HeroMetricDisplay`)

```kotlin
package com.couplefit.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.couplefit.ui.theme.AppSpacing

/**
 * Tier 1 Hero Glanceable Telemetry Display.
 * Uses 52sp tabular figures to prevent horizontal text jitter.
 */
@Composable
fun HeroGlanceMetric(
    userSteps: Int,
    partnerSteps: Int,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth().padding(vertical = AppSpacing.md)
    ) {
        // Tier 4: Contextual Label
        Text(
            text = "TODAY'S COMBINED STEPS",
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                letterSpacing = 0.8.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF8E95A3)
            )
        )
        
        // Tier 1: Primary Focal Counter (52sp ExtraBold)
        Text(
            text = "%,d".format(userSteps + partnerSteps),
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = 52.sp,
                lineHeight = 56.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-1.0).sp,
                color = Color.White
            )
        )

        // Tier 2: Bilateral Breakdown Pills
        Row(
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "You: %,d".format(userSteps),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00E5FF) // Electric Cyan
                )
            )
            Text(text = "•", color = Color(0xFF3F4452))
            Text(
                text = "Partner: %,d".format(partnerSteps),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF4568) // Vibrant Coral
                )
            )
        }
    }
}
```

---

## 5. Verification Checklist & QA Testing Gates

- [ ] **The Squint Test Gate**: Blur screen preview; verify the Dual Activity Rings and 52sp step counter dominate as the undisputed focal point.
- [ ] **1.5x Typography Scale Check**: Verify page headers (`24sp`+) are at least 1.5x larger than section titles (`16sp`), and section titles are at least 1.5x larger than metadata (`11sp`).
- [ ] **Single Primary CTA**: Ensure each screen presents only one high-emphasis primary button (e.g. `[Start Challenge]` or `[Link Accounts]`), with secondary actions styled as tonal or text buttons.
- [ ] **Thumb Zone Anchoring**: Verify primary actions sit within the lower 60% of the mobile viewport for comfortable one-handed reach.
