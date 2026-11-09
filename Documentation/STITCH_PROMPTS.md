# Google Stitch UI Generation Prompts: CoupleFit

Use these enhanced, production-grade prompts in [**stitch.withgoogle.com**](https://stitch.withgoogle.com/) to generate the high-fidelity UI screens for the **CoupleFit** app.

---

## 📱 Prompt 1: Today Dashboard (Core Telemetry)

```markdown
A high-precision, intimate couple fitness dashboard for tracking bilateral daily health synchrony with Apple Fitness+ luxury and dark glassmorphic telemetry.

**DESIGN SYSTEM (REQUIRED):**
- Platform: Mobile App (Android, 390x844)
- Theme: Dark, Midnight Obsidian Glassmorphism
- Background: Deep Obsidian (#07080A) with subtle diffused ambient aurora glow behind cards
- Primary Accent: Electric Cyan (#00F5D4) for User metrics, rings, and active controls
- Secondary Accent: Vibrant Coral (#FF3366) for Partner metrics, rings, and affection nudges
- Tertiary Accent: Warm Amber Gold (#FFB703) for Active Calories and Energy Burn
- Surface: Frosted Glass Cards (rgba(20, 24, 34, 0.70)) with 20px blur and 1px diamond-cut specular border (rgba(255, 255, 255, 0.18))
- Text Primary: Crisp Alabaster (#F8FAFC)
- Text Muted: Cool Slate (#94A3B8)
- Typography: Display in Satoshi Bold, telemetry numbers in JetBrains Mono Tabular
- Bottom Nav: Floating Liquid Glass Dock (68dp height, 32dp pill squircle, 16dp bottom margin)

**Page Structure:**
1. **Top Header Bar:** 
   - Left: Small glowing avatar duo ("Alex & Sarah") with a tiny emerald badge showing "🟢 In Sync • Live ⚡"
   - Right: Sleek glass pill showing current date ("Today, Oct 24") and subtle 44x44dp settings icon button
2. **Contextual Medication Due Pill (Prominent Banner):**
   - Translucent glass pill banner with a violet-blue tint: "💊 Vitamin D & Omega-3 due now" with an inline one-tap "Log [✓]" button
3. **Hero Bilateral Dual Activity Rings Section:**
   - Prominent concentric circular progress dials centered in a frosted glass container:
     * Outer Ring (Electric Cyan, 12px stroke): User steps (8,420 / 10,000 steps, 84% filled)
     * Inner Ring (Vibrant Coral, 10px stroke): Partner steps (7,650 / 10,000 steps, 76% filled)
   - Centered inside the rings: Large tabular step readout ("8,420" in Electric Cyan) with subtext "You're +770 ahead"
4. **Ergonomic Quick Nudge Row (Thumb Zone):**
   - Horizontal row of 4 tactile frosted glass pill chips directly below the rings:
     * "[👋 Poke]" chip with subtle cyan hover tint
     * "[🔥 Keep Going!]" chip
     * "[❤️ Love]" chip with subtle coral tint
     * "[💧 Drink Water]" chip
5. **Today's Competition (Tug-of-War Split Bar):**
   - Compact glass card showing "Today's Daily Duel: 10k Steps Race"
   - Split horizontal balance bar: Left 52% filled in Electric Cyan, Right 48% filled in Vibrant Coral, with user and partner icons at each end
6. **Live Cardiovascular Pulse & ECG Waveform:**
   - Frosted glass card titled "Resting Heart Rate" with a red-coral live pulse dot and "72 BPM" hero readout
   - Smooth neon spline graph showing daytime curve from 06:00 to 21:00 with Peak (128 BPM) and Min (58 BPM) badge chips
7. **Floating Liquid Glass Navigation Dock:**
   - 4 icons evenly spaced: "Today" (Active with glowing Electric Cyan pill background), "Challenges" (Trophy icon), "Trends" (Chart icon), and "Couple Hub" (Heart avatar icon)
```

---

## 🏆 Prompt 2: Challenges & Mutual Duels Tab

```markdown
An exhilarating, gamified mutual challenge and fitness competition screen for couples, balancing friendly rivalry with celebratory romance.

**DESIGN SYSTEM (REQUIRED):**
- Platform: Mobile App (Android, 390x844)
- Theme: Dark, Midnight Obsidian Glassmorphism
- Background: Deep Obsidian (#07080A)
- Primary Accent: Electric Cyan (#00F5D4) for User progress
- Secondary Accent: Vibrant Coral (#FF3366) for Partner progress
- Trophy Gold Accent: Radiant Amber (#FFB703) for trophies, badges, and rewards
- Surface: Frosted Slate Glass (rgba(20, 24, 34, 0.70)) with 20px blur and specular gradient borders
- Text Primary: Crisp Alabaster (#F8FAFC)
- Text Muted: Cool Slate (#94A3B8)
- Navigation: Floating Liquid Glass Dock with "Challenges" tab active

**Page Structure:**
1. **Header & Sub-Navigation:**
   - Top Bar Title: "Couple Challenges" with small trophy icon
   - Segmented Glass Switcher: Connected pill row with "[Active Duels (2)]" (Active pill) | "[Trophy Vault (8)]"
2. **Active Hero Challenge Card ("Weekend 20k Steps Race"):**
   - Frosted glass container featuring:
     * Header: "⚡ Weekend 20k Steps Sprint • 1 Day Left"
     * Bilateral Progress Bars: User bar (14,200 / 20,000 in Electric Cyan) vs. Partner bar (13,850 / 20,000 in Vibrant Coral)
     * Milestone marker at 20k with a glowing gold star icon
     * Motivational banner: "Sarah is catching up fast! 🏃‍♀️"
     * Primary Action CTA: Frosted glass button "+ Log Workout Run"
3. **Secondary Challenge Card ("7-Day Active Streak"):**
   - Horizontal card showing a 7-day flame calendar:
     * Days M-T-W-T-F marked with glowing dual-colored checkmarks
     * Current streak: "🔥 5-Day Joint Streak! 2 days to win Sunday Date Night reward"
4. **Couple Bet & Reward Banner:**
   - Special romantic prize callout card: "Stakes: Loser cooks dinner on Sunday 🍷🍝" with a playful padlock icon
5. **Floating "New Duel +" Action Button:**
   - Prominent rounded pill button at bottom right (above dock) in Electric Cyan with black text: "+ Propose Challenge"
6. **Bottom Navigation Dock:**
   - Floating glass dock with "Challenges" icon highlighted
```

---

## 📈 Prompt 3: Health Trends & Telemetry Tab

```markdown
A sophisticated, deep-dive health and fitness trends screen designed for couples, featuring clinical-grade multi-metric spline charts and comparative telemetry perspectives.

**DESIGN SYSTEM (REQUIRED):**
- Platform: Mobile App (Android, 390x844)
- Theme: Dark, Obsidian Medical Telemetry HUD
- Background: Deep Obsidian (#07080A)
- Primary Accent: Electric Cyan (#00F5D4) for User graphs
- Secondary Accent: Vibrant Coral (#FF3366) for Partner graphs
- Surface: Frosted Dark Slate Glass (rgba(20, 24, 34, 0.70)) with 1px specular gradient rim
- Text Primary: Crisp Alabaster (#F8FAFC)
- Text Muted: Cool Slate (#94A3B8)
- Navigation: Floating Liquid Glass Dock with "Trends" tab active

**Page Structure:**
1. **Header & Timeframe Filter Bar:**
   - Title: "Health Trends"
   - Timeframe Selector: Sleek glass segmented row with "[Day]" | "[Week]" (Active) | "[Month]" | "[Year]"
2. **Telemetry Perspective Filter:**
   - Ergonomic 3-way toggle row: "[Both 👥]" (Active cyan/coral split glow) | "[You 👤]" | "[Partner 💖]"
3. **Primary Telemetry Triad Cards (3-Column Grid):**
   - Card 1: "Avg Steps" ➔ "9,240 / day" with "+12% vs last week" chip in emerald
   - Card 2: "Active Mins" ➔ "48 mins / day" with gold flame icon
   - Card 3: "Avg Heart Rate" ➔ "68 BPM" with mini ECG heartbeat icon
4. **Main Comparative Spline Graph Container:**
   - Wide frosted glass chart card: "Weekly Step Volume (You vs. Partner)"
   - Dual glowing Bézier curves plotted across Monday to Sunday:
     * Curve 1 (Cyan): User daily steps
     * Curve 2 (Coral): Partner daily steps
   - Interactive scrub cursor showing tooltip: "Thursday: You 10,400 • Sarah 11,200"
5. **Heart Rate Zone Distribution:**
   - Stacked horizontal bar chart showing % of time spent in: Rest (55%), Fat Burn (25%), Cardio (15%), Peak (5%)
6. **Sleep & Recovery Balance Pill:**
   - Summary card: "💤 Sleep Synchrony: You avg 7h 20m • Sarah avg 7h 45m"
7. **Bottom Navigation Dock:**
   - Floating glass dock with "Trends" icon highlighted
```

---

## 💖 Prompt 4: Couple Hub & Intimacy Dossier Tab

```markdown
A private, warm, and intimate couple hub celebrating relationship milestones, real-time shared connection status, privacy sharing controls, and partnership settings.

**DESIGN SYSTEM (REQUIRED):**
- Platform: Mobile App (Android, 390x844)
- Theme: Dark, Romantic Midnight Glassmorphism
- Background: Deep Obsidian (#07080A) with soft rose and cyan radial glows
- Primary Accent: Electric Cyan (#00F5D4)
- Secondary Accent: Vibrant Coral (#FF3366)
- Surface: Frosted Glass Cards (rgba(20, 24, 34, 0.70)) with soft 20px blur
- Text Primary: Crisp Alabaster (#F8FAFC)
- Text Muted: Cool Slate (#94A3B8)
- Navigation: Floating Liquid Glass Dock with "Couple Hub" tab active

**Page Structure:**
1. **Intimacy Header & Avatars:**
   - Center: Overlapping circular avatar badges of Alex & Sarah linked by an infinite glowing cyan-coral knot
   - Presence Status: "Sarah is currently online • Watch synced 2m ago ⚡"
   - Milestone Badge: "✨ 342 Days Paired & Crushing Goals Together"
2. **Quick Affection & Nudge Panel:**
   - Large interactive glass card titled "Send Instant Cheer":
     * Double-tap avatar ripple trigger
     * 3 prominent reaction buttons: "[💌 Thinking of You]", "[⚡ Step Up Challenge]", "[🥤 Water Reminder]"
3. **Partner Visibility & Sharing Transparency Card:**
   - Shield icon with title "Privacy & Metric Sharing Rules"
   - Toggles list showing what you share with Sarah:
     * "Steps & Distance" ➔ [Shared ✓]
     * "Active Workouts" ➔ [Shared ✓]
     * "Heart Rate & Vitals" ➔ [Aggregated Only ✓]
   - Subtext: "Zero raw ECG/PPG waveforms leave your phone. End-to-end encrypted."
4. **Couple Milestones & Memory Vault:**
   - Carousel of shared achievements:
     * "1,000,000 Lifetime Steps Together 🏅"
     * "First 10-Mile Hike (August 14) 🏔️"
5. **Account & Danger Zone (Safe Unlinking):**
   - Clean footer item: "Manage Partnership • Danger Zone" with subtle red warning icon (triggers 2-step type-UNLINK verification)
6. **Bottom Navigation Dock:**
   - Floating glass dock with "Couple Hub" icon highlighted
```

---

## 🔐 Prompt 5: Couple Pairing & 6-Digit Code Exchange (Onboarding)

```markdown
A seamless, frictionless onboarding and partner-pairing screen for connecting two phones using a secure, expiring 6-digit invite code.

**DESIGN SYSTEM (REQUIRED):**
- Platform: Mobile App (Android, 390x844)
- Theme: Dark, Midnight Obsidian Glassmorphism
- Background: Deep Obsidian (#07080A)
- Primary Accent: Electric Cyan (#00F5D4)
- Secondary Accent: Vibrant Coral (#FF3366)
- Surface: Centered Frosted Glass Card (rgba(20, 24, 34, 0.80))
- Text Primary: Crisp Alabaster (#F8FAFC)
- Text Muted: Cool Slate (#94A3B8)
- Inputs: Large 6-slot OTP input boxes with glowing cyan/coral focus borders

**Page Structure:**
1. **Header Branding:**
   - CoupleFit logo with intertwined dual rings (Cyan and Coral)
   - Headline: "Link With Your Partner"
   - Sub-headline: "Share daily fitness goals, nudges, and milestones in private synchrony."
2. **Tabbed Pairing Option:**
   - Connected segmented toggle: "[Share My Invite Code]" (Active) | "[Enter Partner's Code]"
3. **6-Digit Code Display Card:**
   - Large glowing digits in 6 discrete rounded glass boxes: `[ 7 ] [ 4 ] [ 2 ] [ 9 ] [ 1 ] [ 8 ]`
   - Countdown timer badge: "⏳ Code expires in 09:42 minutes"
   - Single-tap actions:
     * Primary Button: "Share Invite Link 📤" (Cyan pill button with black text)
     * Secondary Button: "Copy Code 📋" (Frosted glass button)
4. **Alternative Code Entry (When 'Enter Code' is selected):**
   - 6 individual text input slots with autofocus and auto-advance
   - "Paste from Clipboard" floating chip
   - Primary Action: "Connect Partner 🤝" (Becomes active once 6 digits are filled)
5. **Privacy Guarantee Callout:**
   - Minimal lock icon at bottom: "Private & Encrypted • Only daily aggregates are shared, never raw location or GPS data."
```

