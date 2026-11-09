# Design System: CoupleFit (Android Mobile)
**Project Title:** CoupleFit — Privacy-First Fitness Companion for Couples  
**Platform:** Mobile (Android 14+, 9:19.5 aspect ratio, 390x844dp base viewport)  
**Standard:** Google Stitch Semantic Design Specification (Anti-Slop / High-Taste)

---

## 1. Visual Atmosphere & Aesthetic Philosophy
- **Vibe:** "Apple Fitness+ Deluxe meets High-Precision Telemetry HUD"
- **Atmosphere:** Deep midnight luxury, calm kinetic synchrony, high-trust intimacy.
- **Palette Baseline:** Pure Obsidian dark canvas (`#07080A`) layered with translucent optical liquid glass (`rgba(20, 23, 32, 0.65)`), directional specular rims, and dual luminous kinetic accents.
- **Density Score:** 6/10 (Balanced Telemetry — clear hierarchy without clinical clutter).
- **Motion Score:** 7/10 (Fluid physics, continuous spring momentum, staggered number pop-in).

---

## 2. Color Calibration & Identity Tokens
Every metric, gauge, and chart in CoupleFit enforces unmistakable bilateral partner distinction:

### Primary Brand Accents
- **User Primary (Electric Cyan):** `#00F5D4`  
  *Role:* User activity ring, personal step count, user spline waveform, active navigation indicator capsule.
- **Partner Primary (Vibrant Coral):** `#FF3366`  
  *Role:* Partner activity ring, partner step count, partner telemetry comparisons, affection nudges.
- **Sync Gold (Energy / Workouts):** `#FFB703`  
  *Role:* Active minutes, caloric burn badges, milestone completion stars.
- **Vitals Emerald (Heart / Rest):** `#10B981`  
  *Role:* Rest pulse, recovery health score, real-time partner online presence beacon.

### Dark Backgrounds & Glass Surfaces
- **Canvas Base (Deep Obsidian):** `#07080A` (Avoids clinical pure white and muddy gray).
- **Glass Card Surface (Dark Slate Glass):** `rgba(20, 24, 34, 0.70)` with 20px background blur.
- **Specular Rim Border (Diamond Cut):** 1px linear gradient `rgba(255, 255, 255, 0.24)` at top down to `rgba(255, 255, 255, 0.04)` at bottom.
- **Text Primary (Crisp Alabaster):** `#F8FAFC` (100% razor-sharp, zero container blur).
- **Text Muted (Cool Slate):** `#94A3B8` (For sub-labels, timestamps, and secondary captions).

---

## 3. Typographic Architecture
- **Display & Headlines:** `Cabinet Grotesk` or `Satoshi` (Bold, -0.02em tracking, commanding yet human).
- **Body & Captions:** `Inter` or `Geist` (Regular/Medium, 1.45 line height, comfortable glanceability).
- **Telemetry Readouts & Numbers:** `JetBrains Mono` or `Satoshi` with `fontFeatureSettings = "tnum"` (Tabular numbers to eliminate layout twitching as digits increment).

### Scale
- **Hero Tracker Digits:** 42sp, ExtraBold, Tabular.
- **Section Headers:** 21sp, Bold, Tracking -0.1sp.
- **Card Titles:** 17sp, SemiBold.
- **Body Text:** 14sp, Regular, Slate-300.
- **Micro Badges & Chips:** 11sp, Bold, All-Caps tracking +0.05em.

---

## 4. Component Behaviors & Geometry
- **Floating Liquid Glass Navigation Dock:**
  - 68dp height, 32dp continuous pill squircle, floating 16dp above bottom bezel.
  - Layered with 28px blur, 0.5dp specular rim border, and sliding active Cyan glowing capsule.
- **Bilateral Dual Rings:**
  - Concentric curves with 12 o'clock rounded cap markers and ambient luminescent track glow.
  - Outer ring: User (Cyan, 12dp stroke). Inner ring: Partner (Coral, 10dp stroke).
- **Contextual Micro-Pills (Medication / Status):**
  - Pill-shaped chips (36dp height, 16dp radius) with one-tap action triggers (`Log [✓]`).
- **Quick Nudge Row:**
  - 4 quick-reaction glass chips: `[👋 Poke]`, `[🔥 Keep Going!]`, `[❤️ Love]`, `[💧 Drink Water]`.
- **Modals & Dialogs:**
  - Anchored `ModalBottomSheet` (Thumb zone ergonomics) over a 75% dark scrim.
  - 2-step danger zone verification for unlinking (requires typing `"UNLINK"`).

---

## 5. Motion Philosophy & Spring Dynamics
- **Spring Parameters:** `stiffness = 750f, dampingRatio = 0.82f` (Snappy mechanical glass click, zero rubbery overshoot).
- **Pointer Down:** Zero-latency instantaneous depression (`scale = 0.975f`, 2.5% compression).
- **Number Transitions:** 500ms duration, 70ms stagger per character, 8dp upward translation, tabular numbers.
- **Accessibility:** Immediate static snapshot fallback when `prefers-reduced-motion` is enabled.

