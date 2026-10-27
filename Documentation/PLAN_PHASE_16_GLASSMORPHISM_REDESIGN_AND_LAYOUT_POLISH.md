# Phase 16 Plan: Glassmorphism Redesign, Ambient Aurora Illumination & Layout Polish 💎

**Objective**: Redesign CoupleFit's glassmorphic visual language to eliminate flat dark-slate murkiness, introduce luminous specular gradients, boost the ambient aurora back-illumination, elevate the floating navigation dock to Apple VisionOS polish, and resolve the Day-1 onboarding layout squeeze defect.

---

## 1. Defect & Aesthetic Catalog

| Defect ID | Severity | Category | File(s) | Description & Root Cause | Target Fix |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **`GLS-07` / `LAY-01`** | 🔴 Critical | Layout / Composition | `DashboardScreen.kt` | Day-1 onboarding card (`if (healthData.steps == 0L) Card(...)`) was nested inside a horizontal `Row` alongside `Text(syncText)`. In unconstrained Rows, `fillMaxWidth()` squeezed the card into a ~120px column causing text to break letter-by-letter. | Decouple sync status Row from Day-1 card. Make Day-1 card a full-width glass component directly in the parent `Column`. |
| **`GLS-08` / `VIS-07`** | 🔴 High | Visual / Design System | `GlassTokens.kt` | `iosGlass()` uses a flat solid color `Color(0x14FFFFFF)` (8% white). On OLED black `#000000`, this renders as flat dark gray `#141414` with no depth, sheen, or glass refraction. | Upgrade `iosGlass` with dual-stop specular surface gradients (`GlassGradients.CardSurfaceBrush`), rich translucent slate body, and diamond-cut specular rim borders. |
| **`GLS-09` / `VIS-08`** | 🟡 Medium | Visual Atmosphere | `GlassTokens.kt` | `AuroraTelemetryMesh` radial glow alphas are capped at `0.08f` (8%), making the backlight behind the activity dials virtually invisible. | Boost radial gradient alphas to `0.28f` with 2-tier diffusion plumes (`0.28f` -> `0.12f` -> `Transparent`) so radiant cyan and coral light illuminates through the frosted glass. |
| **`GLS-10` / `NAV-06`** | 🟡 Medium | Navigation Polish | `CoupleFitNavigationSuite.kt` & `GlassTokens.kt` | Floating navigation dock uses a flat dark container with weak contrast against scrolling content, low-contrast inactive icons, and generic indicator pills. | Redesign floating dock with dense dark-frosted glass gradient (`0xD91E2433` -> `0xEE12151F`), specular top rim, glowing electric cyan active indicator pill, and high-contrast labels (WCAG AA 5.2:1). |
| **`TYP-04` / `FLT-01`** | 🔵 Minor | Typography / Ergonomics | `DashboardScreen.kt` | View selection FilterChips in `DashboardScreen.kt` cause "Partner's Stats" to wrap into 2 lines on compact viewports. | Optimize chip labels (`"${healthData.partnerName}"` with `maxLines = 1`) and spacing so chips fit seamlessly across all phone viewports. |

---

## 2. Implementation Waves

### Wave 1: Core Glass Tokens & Specular Gradients (`GlassTokens.kt`)
- Define `GlassGradients` containing:
  - `CardSurfaceBrush`: Vertical gradient (`Color(0x24FFFFFF)` -> `Color(0x10FFFFFF)` -> `Color(0x05FFFFFF)`).
  - `CardBaseTint`: Translucent dark slate `Color(0x28161B26)`.
  - `DockSurfaceBrush`: Frosted dark glass gradient (`Color(0xD91E2433)` -> `Color(0xEE12151F)`).
  - `BorderSpecularRim`: 3-stop vertical rim brush (`0.0f` -> `0x60FFFFFF`, `0.25f` -> `0x24FFFFFF`, `1.0f` -> `0x08FFFFFF`).
- Update `Modifier.iosGlass()`, `Modifier.appleGlass()`, and `Modifier.glassDock()` to apply the dual-layer surface gradient brush and specular rim.
- Enhance `AuroraTelemetryMesh` with luminous dual radial aura plumes (`0.28f` core, `0.12f` halo).

### Wave 2: Layout Bug Fix & Dashboard Screen Polish (`DashboardScreen.kt`)
- Separate `syncText` Row from the Day-1 onboarding card.
- Re-architect Day-1 onboarding card as a full-width glass card with icon, title, description, and test action button.
- Optimize view selection FilterChips to prevent multi-line text wrapping on compact viewports.
- Ensure all cards on the dashboard use the upgraded glassmorphism.

### Wave 3: Floating Navigation Dock Polish (`CoupleFitNavigationSuite.kt`)
- Upgrade floating dock container to use `Modifier.glassDock()`, 68dp height, 32dp continuous pill shape, and specular rim border.
- Style active indicator capsules with `CoupleFitIdentityTokens.UserPrimary.copy(alpha = 0.22f)` and high-contrast labels.

### Wave 4: Test Suite & Deterministic Verification
- Run `./gradlew.bat testDebugUnitTest` and `./gradlew.bat compileDebugAndroidTestKotlin`.
- Update `NavigationComposeUiTest.kt` if necessary.

### Wave 5: Live Device ARTEMIS Verification on `emulator-5554`
- Install debug APK via `adb install -r`.
- Launch `com.couplefit`.
- Capture live screenshot proofs:
  1. `Today` Dashboard with Day-1 card and glowing activity gauges.
  2. Floating navigation dock visual proof.
  3. `Health Trends` screen with glassmorphic cards.

### Wave 6: Documentation Synchronization & Git Release
- Update `Fixinglist.md`, `triage.md`, `phases.md`, `INDEX.md`, and write release summary.
- Git commit with conventional commit message.
