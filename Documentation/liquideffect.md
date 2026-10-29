# Liquid Glass Navigation Bar — Android (Jetpack Compose)

> **For AI Agents:** This document is a complete, self-contained implementation guide.
> Follow every step in order. Do not skip any section. All code is copy-paste ready.

---

## Goal

Add a floating, frosted-glass bottom navigation bar to an existing Android (Jetpack Compose)
app using the [Haze](https://github.com/chrisbanes/haze) library by Chris Banes.

The result is an iOS-style translucent pill-shaped bottom bar that blurs the scrollable
content behind it in real time — identical in look to BitChord's `FloatingBottomBar`.

---

## Prerequisites

- Android project using **Jetpack Compose** (Compose BOM 2024.x or later)
- `minSdk` ≥ **21** (Haze gracefully degrades on older devices; best visual result at API 31+)
- Kotlin **1.9+**
- `compileSdk` **35** or later

---

## Step 1 — Add Dependencies

Open `app/build.gradle.kts` and add:

```kotlin
dependencies {
    // Haze — real-time blur / frosted glass
    implementation("dev.chrisbanes.haze:haze:1.3.1")
    implementation("dev.chrisbanes.haze:haze-materials:1.3.1") // ultraThin / regular presets

    // Make sure these Compose basics are present (adjust versions to your BOM)
    implementation(platform("androidx.compose:compose-bom:2024.09.00"))
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
}
```

Sync Gradle before continuing.

---

## Step 2 — Enable Edge-to-Edge in MainActivity

The glass bar floats over the system navigation area, so the app must draw edge-to-edge.

```kotlin
// MainActivity.kt
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Required: draw behind system bars
        enableEdgeToEdge()

        setContent {
            YourAppTheme {
                MainScreen()
            }
        }
    }
}
```

If `enableEdgeToEdge()` is not available (API < 29), add this instead:

```kotlin
WindowCompat.setDecorFitsSystemWindows(window, false)
```

---

## Step 3 — Create the Glass Navigation Bar Composable

Create a new file: `ui/components/GlassBottomNavBar.kt`

```kotlin
package com.yourapp.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials

data class NavTab(
    val icon: ImageVector,
    val label: String,
)

@OptIn(ExperimentalHazeMaterialsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun GlassBottomNavBar(
    tabs: List<NavTab>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    hazeState: HazeState,
    modifier: Modifier = Modifier,
) {
    // Pill shape — 50% corner = fully rounded ends
    val pillShape = remember { RoundedCornerShape(percent = 50) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            // Respect system navigation bar insets so the bar sits just above home indicator
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier
                .wrapContentWidth()
                .height(64.dp)
                // ① Real-time frosted glass blur sampled from hazeState
                .hazeEffect(
                    state = hazeState,
                    style = HazeMaterials.ultraThin(),
                )
                .clip(pillShape)
                // ② Specular hairline rim — the subtle white edge that reads as glass
                .border(
                    width = 0.5.dp,
                    color = Color.White.copy(alpha = 0.18f),
                    shape = pillShape,
                )
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            tabs.forEachIndexed { index, tab ->
                val selected = index == selectedIndex
                NavigationBarItem(
                    selected = selected,
                    onClick = { onTabSelected(index) },
                    icon = {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.label,
                        )
                    },
                    label = {
                        Text(
                            text = tab.label,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    },
                    // ③ Soft white pill for the selected tab indicator
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onSurface,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                        selectedTextColor = MaterialTheme.colorScheme.onSurface,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                        indicatorColor = Color.White.copy(alpha = 0.14f),
                    ),
                )
            }
        }
    }
}
```

---

## Step 4 — Create the Main Screen Scaffold

Create `ui/MainScreen.kt`. The `hazeSource` modifier on the scroll content and `hazeEffect`
on the bar must share the same `HazeState` instance — that is the link that makes the blur work.

```kotlin
package com.yourapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import com.yourapp.ui.components.GlassBottomNavBar
import com.yourapp.ui.components.NavTab

@Composable
fun MainScreen() {
    // Single shared instance — the source and the effect must reference the same object
    val hazeState = remember { HazeState() }
    var selectedTab by remember { mutableIntStateOf(0) }

    val tabs = remember {
        listOf(
            NavTab(icon = Icons.Rounded.Home,   label = "Home"),
            NavTab(icon = Icons.Rounded.Search, label = "Search"),
            NavTab(icon = Icons.Rounded.Person, label = "Library"),
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // ─── Scrollable content (the blur source) ──────────────────────────
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                // ① Tag this layer as the backdrop the glass bar samples from
                .hazeSource(state = hazeState),
            // Add bottom padding so the last item is not hidden behind the bar
            contentPadding = PaddingValues(bottom = 120.dp),
        ) {
            itemsIndexed((1..40).toList()) { _, item ->
                ListItem(
                    headlineContent = { Text("Item $item") },
                    supportingContent = { Text("Supporting text for item $item") },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
                HorizontalDivider(thickness = 0.5.dp)
            }
        }

        // ─── Floating glass bar ────────────────────────────────────────────
        GlassBottomNavBar(
            tabs = tabs,
            selectedIndex = selectedTab,
            onTabSelected = { selectedTab = it },
            hazeState = hazeState,          // ② Same instance as above
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}
```

---

## Step 5 — Wire MainScreen in MainActivity

```kotlin
// MainActivity.kt
setContent {
    YourAppTheme {
        MainScreen()
    }
}
```

---

## Step 6 — (Optional) Accessibility — Reduce Transparency

Some users enable "Remove animations" or have low-vision needs. Detect the system setting and
swap the glass for a solid surface:

```kotlin
import androidx.compose.ui.platform.LocalContext
import android.provider.Settings

@Composable
fun isReduceTransparencyEnabled(): Boolean {
    val context = LocalContext.current
    return Settings.Global.getFloat(
        context.contentResolver,
        Settings.Global.TRANSITION_ANIMATION_SCALE,
        1f
    ) == 0f
}
```

Then in `GlassBottomNavBar`, branch on it:

```kotlin
val reduceTransparency = isReduceTransparencyEnabled()

val barModifier = if (reduceTransparency) {
    Modifier
        .clip(pillShape)
        .background(MaterialTheme.colorScheme.surface)
        .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, pillShape)
} else {
    Modifier
        .hazeEffect(state = hazeState, style = HazeMaterials.ultraThin())
        .clip(pillShape)
        .border(0.5.dp, Color.White.copy(alpha = 0.18f), pillShape)
}
```

---

## Step 7 — (Optional) Dark Theme Tuning

Glass on a dark background looks best with a slightly lighter tint. Use `HazeMaterials.thin()`
instead of `ultraThin()`, or pass a tint color:

```kotlin
.hazeEffect(
    state = hazeState,
    style = HazeMaterials.thin(tint = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)),
)
```

---

## File Structure After Implementation

```
app/src/main/java/com/yourapp/
├── MainActivity.kt
└── ui/
    ├── MainScreen.kt
    └── components/
        └── GlassBottomNavBar.kt
```

---

## How It Works (Architecture Summary)

```
┌─────────────────────────────────────────────┐
│             Box (full screen)               │
│                                             │
│  ┌───────────────────────────────────────┐  │
│  │  LazyColumn                           │  │
│  │  .hazeSource(hazeState)  ◄────────┐  │  │
│  │  (scrollable content)              │  │  │
│  └───────────────────────────────────── │──┘  │
│                                         │     │
│  ┌──────────────────────────┐           │     │
│  │  GlassBottomNavBar       │           │     │
│  │  .hazeEffect(hazeState) ─┘  blurs ──┘     │
│  │  (floating pill bar)                       │
│  └────────────────────────────────────────┘  │
└─────────────────────────────────────────────┘
```

`HazeState` is the shared bridge. `hazeSource` registers the content layer as the
blur source; `hazeEffect` consumes it and renders the frosted glass surface on top.

---

## Troubleshooting

| Problem | Cause | Fix |
|---|---|---|
| Bar is opaque / no blur visible | `hazeSource` missing on scroll content | Add `.hazeSource(hazeState)` to the `LazyColumn` modifier |
| Glass looks white/flat | Using `HazeMaterials.ultraThin()` on a white background | Switch to `HazeMaterials.regular()` or add a darker tint |
| Content hidden behind bar | Missing `contentPadding` | Add `PaddingValues(bottom = 120.dp)` to `LazyColumn` |
| Bar clips to rectangle shape | `clip(pillShape)` is before `hazeEffect` — wrong order | Always apply `.hazeEffect(...)` first, then `.clip(shape)` |
| Crash on API < 21 | `minSdk` too low | Haze requires `minSdk 21`. Check your module's `build.gradle` |

---

## Step 8 — Add a Settings Toggle to Enable / Disable Liquid Glass

This is the exact pattern BitChord uses in
[`AppSettings.kt`](file:///d:/REA/app/src/main/java/com/music/bitchord/data/settings/AppSettings.kt)
and
[`SettingsSheet.kt`](file:///d:/REA/app/src/main/java/com/music/bitchord/ui/screens/SettingsSheet.kt).

---

### 8a — Create an App-Wide Settings Object

Create `data/AppSettings.kt`:

```kotlin
package com.yourapp.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object AppSettings {

    private lateinit var prefs: SharedPreferences

    // ---- Keys ----
    private const val KEY_LIQUID_GLASS = "liquid_glass"

    // ---- State flows (observed by Compose) ----
    private val _liquidGlass = MutableStateFlow(false)
    val liquidGlass = _liquidGlass.asStateFlow()

    /** Call once in Application.onCreate() before any composable reads state. */
    fun init(context: Context) {
        prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        _liquidGlass.value = prefs.getBoolean(KEY_LIQUID_GLASS, false)
    }

    /** Toggle persisted to SharedPreferences and reflected immediately in all observers. */
    fun setLiquidGlass(enabled: Boolean) {
        _liquidGlass.value = enabled
        prefs.edit().putBoolean(KEY_LIQUID_GLASS, enabled).apply()
    }
}
```

---

### 8b — Initialise Settings in Application

Create `MyApplication.kt` (register it in `AndroidManifest.xml`):

```kotlin
package com.yourapp

import android.app.Application
import com.yourapp.data.AppSettings

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppSettings.init(this)
    }
}
```

`AndroidManifest.xml` — add the `android:name` attribute to `<application>`:

```xml
<application
    android:name=".MyApplication"
    ... >
```

---

### 8c — Create a Reusable Settings Row Composable

Create `ui/components/SettingsToggleRow.kt`:

```kotlin
package com.yourapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    ListItem(
        modifier = modifier
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) MaterialTheme.colorScheme.primary
                       else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
            )
        },
        headlineContent = {
            Text(
                text = title,
                color = if (enabled) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
            )
        },
        supportingContent = {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
                    .copy(alpha = if (enabled) 1f else 0.38f),
            )
        },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled,
                colors = SwitchDefaults.colors(
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                ),
            )
        },
    )
}
```

---

### 8d — Create the Settings Screen

Create `ui/SettingsScreen.kt`:

```kotlin
package com.yourapp.ui

import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BlurCircular
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yourapp.data.AppSettings
import com.yourapp.ui.components.SettingsToggleRow

/** Requires Android 12 (API 31) — RenderEffect availability. */
private fun isLiquidGlassSupported(): Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val liquidGlass by AppSettings.liquidGlass.collectAsStateWithLifecycle()
    val supported = isLiquidGlassSupported()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Settings") })
        }
    ) { innerPadding ->
        LazyColumn(
            contentPadding = innerPadding,
            modifier = Modifier.fillMaxSize(),
        ) {

            // ── Section header ──────────────────────────────────────────────
            item {
                Text(
                    text = "Appearance",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                )
            }

            // ── Liquid Glass toggle ─────────────────────────────────────────
            item {
                SettingsToggleRow(
                    icon = Icons.Rounded.BlurCircular,
                    title = "Liquid Glass Navigation Bar",
                    subtitle = if (supported) {
                        "Frosted glass effect on the floating bottom navigation bar"
                    } else {
                        "Requires Android 12 or later"
                    },
                    checked = liquidGlass,
                    onCheckedChange = AppSettings::setLiquidGlass,
                    enabled = supported,
                )
            }

            // ── Visual separator ────────────────────────────────────────────
            item { HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp)) }

            // Add more settings rows here...
        }
    }
}
```

---

### 8e — Read the Setting in GlassBottomNavBar

Update `GlassBottomNavBar.kt` from **Step 3** to branch on the setting.
Replace the `Row` modifier block with:

```kotlin
// GlassBottomNavBar.kt — updated modifier logic

val liquidGlassEnabled by AppSettings.liquidGlass.collectAsStateWithLifecycle()
val supported = remember { Build.VERSION.SDK_INT >= Build.VERSION_CODES.S }
val useGlass = liquidGlassEnabled && supported

val barModifier = if (useGlass) {
    // ✅ Live frosted blur (Liquid Glass ON)
    Modifier
        .hazeEffect(
            state = hazeState,
            style = HazeMaterials.ultraThin(),
        )
        .clip(pillShape)
        .border(0.5.dp, Color.White.copy(alpha = 0.18f), pillShape)
} else {
    // ⬜ Solid surface fallback (Liquid Glass OFF or unsupported)
    Modifier
        .clip(pillShape)
        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
        .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, pillShape)
}

Row(
    modifier = Modifier
        .wrapContentWidth()
        .height(64.dp)
        .then(barModifier)         // ← apply the selected modifier
        .padding(horizontal = 8.dp),
    ...
)
```

---

### 8f — Complete Data Flow Diagram

```
User flips toggle in SettingsScreen
           │
           ▼
AppSettings.setLiquidGlass(true)
           │
           ├─► SharedPreferences.putBoolean("liquid_glass", true)  [persisted across restarts]
           │
           └─► _liquidGlass.value = true   [StateFlow emits]
                           │
                           ├─► SettingsScreen recomposes (Switch shows ON)
                           │
                           └─► GlassBottomNavBar recomposes
                                       │
                               useGlass = true
                                       │
                               ┌───────┴────────────┐
                               │  hazeEffect() blur  │  ◄─── hazeSource from LazyColumn
                               │  + glass rim border │
                               └────────────────────┘
```

---

## References

- Haze library: https://github.com/chrisbanes/haze
- BitChord source: `app/src/main/java/com/music/bitchord/ui/components/GlassNavBar.kt`
- BitChord glass engine: `app/src/main/java/com/music/bitchord/ui/components/LiquidGlass.kt`
- BitChord frosted top bar: `app/src/main/java/com/music/bitchord/ui/components/FrostedTopBar.kt`

