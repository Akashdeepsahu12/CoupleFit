# Physical 3D Lens Refraction & Chromatic Dispersion Effect

This document isolates and strips **the exact 3D Lens Refraction and Chromatic Dispersion Engine** used in BitChord's liquid glass bottom navigation bar.

---

## 🔬 How The Refraction Effect Works

In physical optics, when light passes through a glass lens with curved bevels, two things happen:
1. **Refraction (Snell's Law / Lens Bending):** Light rays bend towards the surface normal. In this shader, a Signed Distance Field (`sdRoundedRect`) computes how close a pixel is to the rounded edge of the pill, and applies a spherical lens distortion curve (`circleMap(x) = 1.0 - sqrt(1.0 - x * x)`).
2. **Chromatic Dispersion (Prismatic Separation):** Different wavelengths of light bend by different amounts. In high-index optical glass, blue and red rays separate. The shader evaluates the background image at 7 distinct spectral offsets (`red`, `orange`, `yellow`, `green`, `cyan`, `blue`, and `purple`), yielding the characteristic rainbow shimmer along the edges of the navigation bar.

```
       Incoming Light Ray
              │
              ▼
   ┌──────────────────────┐   ◄── Glass Pill Boundary
   │      ╱  ╲   ╱        │
   │     ╱    ╲ ╱         │   ◄── Spherical Lens Distortion (circleMap)
   │    R   G   B         │   ◄── Chromatic Dispersion (Wavelength Splitting)
   └──────────────────────┘
              │
              ▼
       Refracted Output
```

---

## 📦 Requirements
- **Android 13+ (API 33, Tiramisu)**: AGSL (`android.graphics.RuntimeShader`) is an Android 13+ platform feature.
- **Android 12+ (API 31, S)**: `android.graphics.RenderEffect`.
- Compose BOM 2024.x+ / Kotlin 1.9+.

---

## 📄 File 1: `RefractionShaders.kt`

This contains the exact AGSL shaders stripped from BitChord's `com.music.bitchord.ui.components.backdrop.internal.Shaders.kt`.

```kotlin
package com.yourapp.ui.refraction

import org.intellij.lang.annotations.Language

/**
 * Signed Distance Field (SDF) and normal gradient math for a rounded rectangle.
 * Computes exact distance from any (x, y) coordinate to the pill's curved perimeter.
 */
@Language("AGSL")
internal const val RoundedRectSDF = """
float radiusAt(float2 coord, float4 radii) {
    if (coord.x >= 0.0) {
        return (coord.y <= 0.0) ? radii.y : radii.z;
    } else {
        return (coord.y <= 0.0) ? radii.x : radii.w;
    }
}

float sdRoundedRect(float2 coord, float2 halfSize, float radius) {
    float2 cornerCoord = abs(coord) - (halfSize - float2(radius));
    float outside = length(max(cornerCoord, 0.0)) - radius;
    float inside = min(max(cornerCoord.x, cornerCoord.y), 0.0);
    return outside + inside;
}

float2 gradSdRoundedRect(float2 coord, float2 halfSize, float radius) {
    float2 cornerCoord = abs(coord) - (halfSize - float2(radius));
    if (cornerCoord.x >= 0.0 || cornerCoord.y >= 0.0) {
        return sign(coord) * normalize(max(cornerCoord, 0.0));
    } else {
        float gradX = step(cornerCoord.y, cornerCoord.x);
        return sign(coord) * float2(gradX, 1.0 - gradX);
    }
}
"""

/**
 * Monochromatic Refraction Lens Shader:
 * Bends background rays through a spherical lens curve without color splitting.
 */
@Language("AGSL")
internal const val RoundedRectRefractionShaderString = """
uniform shader content;

uniform float2 size;
uniform float2 offset;
uniform float4 cornerRadii;
uniform float refractionHeight;
uniform float refractionAmount;
uniform float depthEffect;

$RoundedRectSDF

float circleMap(float x) {
    return 1.0 - sqrt(1.0 - x * x);
}

half4 main(float2 coord) {
    float2 halfSize = size * 0.5;
    float2 centeredCoord = (coord + offset) - halfSize;
    float radius = radiusAt(coord, cornerRadii);
    
    float sd = sdRoundedRect(centeredCoord, halfSize, radius);
    if (-sd >= refractionHeight) {
        return content.eval(coord);
    }
    sd = min(sd, 0.0);
    
    float d = circleMap(1.0 - -sd / refractionHeight) * refractionAmount;
    float gradRadius = min(radius * 1.5, min(halfSize.x, halfSize.y));
    float2 grad = normalize(gradSdRoundedRect(centeredCoord, halfSize, gradRadius) + depthEffect * normalize(centeredCoord));
    
    float2 refractedCoord = coord + d * grad;
    return content.eval(refractedCoord);
}
"""

/**
 * Prismatic Refraction Shader with 7-Wavelength Chromatic Dispersion:
 * Splits light into Red, Orange, Yellow, Green, Cyan, Blue, and Purple channels
 * along the perimeter of the pill for a hyper-realistic optical glass look.
 */
@Language("AGSL")
internal const val RoundedRectRefractionWithDispersionShaderString = """
uniform shader content;

uniform float2 size;
uniform float2 offset;
uniform float4 cornerRadii;
uniform float refractionHeight;
uniform float refractionAmount;
uniform float depthEffect;
uniform float chromaticAberration;

$RoundedRectSDF

float circleMap(float x) {
    return 1.0 - sqrt(1.0 - x * x);
}

half4 main(float2 coord) {
    float2 halfSize = size * 0.5;
    float2 centeredCoord = (coord + offset) - halfSize;
    float radius = radiusAt(coord, cornerRadii);
    
    float sd = sdRoundedRect(centeredCoord, halfSize, radius);
    if (-sd >= refractionHeight) {
        return content.eval(coord);
    }
    sd = min(sd, 0.0);
    
    float d = circleMap(1.0 - -sd / refractionHeight) * refractionAmount;
    float gradRadius = min(radius * 1.5, min(halfSize.x, halfSize.y));
    float2 grad = normalize(gradSdRoundedRect(centeredCoord, halfSize, gradRadius) + depthEffect * normalize(centeredCoord));
    
    float2 refractedCoord = coord + d * grad;
    float dispersionIntensity = chromaticAberration * ((centeredCoord.x * centeredCoord.y) / (halfSize.x * halfSize.y));
    float2 dispersedCoord = d * grad * dispersionIntensity;
    
    half4 color = half4(0.0);
    
    half4 red = content.eval(refractedCoord + dispersedCoord);
    color.r += red.r / 3.5;
    color.a += red.a / 7.0;
    
    half4 orange = content.eval(refractedCoord + dispersedCoord * (2.0 / 3.0));
    color.r += orange.r / 3.5;
    color.g += orange.g / 7.0;
    color.a += orange.a / 7.0;
    
    half4 yellow = content.eval(refractedCoord + dispersedCoord * (1.0 / 3.0));
    color.r += yellow.r / 3.5;
    color.g += yellow.g / 3.5;
    color.a += yellow.a / 7.0;
    
    half4 green = content.eval(refractedCoord);
    color.g += green.g / 3.5;
    color.a += green.a / 7.0;
    
    half4 cyan = content.eval(refractedCoord - dispersedCoord * (1.0 / 3.0));
    color.g += cyan.g / 3.5;
    color.b += cyan.b / 3.0;
    color.a += cyan.a / 7.0;
    
    half4 blue = content.eval(refractedCoord - dispersedCoord * (2.0 / 3.0));
    color.b += blue.b / 3.0;
    color.a += blue.a / 7.0;
    
    half4 purple = content.eval(refractedCoord - dispersedCoord);
    color.r += purple.r / 7.0;
    color.b += purple.b / 3.0;
    color.a += purple.a / 7.0;
    
    return color;
}
"""
```

---

## 📄 File 2: `RefractionLensEngine.kt`

This provides the Compose modifier that wires the AGSL refraction shader into the Android rendering pipeline.

```kotlin
package com.yourapp.ui.refraction

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.graphics.Shader
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.GlobalPositionAwareModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.requireDensity
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toIntSize
import androidx.compose.ui.util.fastCoerceAtLeast

val LocalRefractionBackdrop = staticCompositionLocalOf<RefractionBackdrop> {
    error("No RefractionBackdrop provided. Wrap your screen with rememberRefractionBackdrop!")
}

interface RefractionBackdrop {
    fun DrawScope.drawBackdrop(density: Density, coordinates: LayoutCoordinates?)
}

@Composable
fun rememberRefractionBackdrop(
    graphicsLayer: GraphicsLayer = rememberGraphicsLayer(),
    onDraw: ContentDrawScope.() -> Unit = { drawContent() }
): RefractionBackdropImpl {
    return remember(graphicsLayer, onDraw) {
        RefractionBackdropImpl(graphicsLayer, onDraw)
    }
}

@Stable
class RefractionBackdropImpl(
    val graphicsLayer: GraphicsLayer,
    internal val onDraw: ContentDrawScope.() -> Unit
) : RefractionBackdrop {
    internal var layerCoordinates: LayoutCoordinates? by mutableStateOf(null)

    override fun DrawScope.drawBackdrop(density: Density, coordinates: LayoutCoordinates?) {
        val coords = coordinates ?: return
        val lCoords = layerCoordinates ?: return
        val offset = try {
            lCoords.localPositionOf(coords)
        } catch (_: Exception) {
            coords.positionInWindow() - lCoords.positionInWindow()
        }
        drawContext.canvas.translate(-offset.x, -offset.y)
        drawLayer(graphicsLayer)
        drawContext.canvas.translate(offset.x, offset.y)
    }
}

/**
 * Modifier to attach to the root scrollable content so its frame is recorded for refraction.
 */
fun Modifier.recordForRefraction(backdrop: RefractionBackdropImpl): Modifier =
    this then RecordForRefractionElement(backdrop)

private class RecordForRefractionElement(val backdrop: RefractionBackdropImpl) :
    ModifierNodeElement<RecordForRefractionNode>() {
    override fun create(): RecordForRefractionNode = RecordForRefractionNode(backdrop)
    override fun update(node: RecordForRefractionNode) {
        if (node.backdrop != backdrop) {
            node.backdrop.layerCoordinates = null
            node.backdrop = backdrop
        }
    }
    override fun InspectorInfo.inspectableProperties() { name = "recordForRefraction" }
    override fun equals(other: Any?): Boolean = other is RecordForRefractionElement && backdrop == other.backdrop
    override fun hashCode(): Int = backdrop.hashCode()
}

private class RecordForRefractionNode(var backdrop: RefractionBackdropImpl) :
    DrawModifierNode, GlobalPositionAwareModifierNode, Modifier.Node() {

    override fun ContentDrawScope.draw() {
        if (size.width < 1f || size.height < 1f) {
            drawContent()
            return
        }
        val density = requireDensity()
        backdrop.graphicsLayer.record(size.toIntSize()) {
            val prevDensity = drawContext.density
            drawContext.density = density
            try {
                backdrop.onDraw(this@draw)
            } finally {
                drawContext.density = prevDensity
            }
        }
        drawLayer(backdrop.graphicsLayer)
    }

    override fun onGloballyPositioned(coordinates: LayoutCoordinates) {
        if (coordinates.isAttached) {
            backdrop.layerCoordinates = coordinates
        }
    }

    override fun onDetach() {
        backdrop.layerCoordinates = null
    }
}

/**
 * The physical refraction modifier:
 * Applies Blur + AGSL 3D Lens Refraction + Chromatic Dispersion + Edge Hairline.
 */
@Composable
fun Modifier.refractiveLens(
    shape: CornerBasedShape = RoundedCornerShape(percent = 50),
    blurRadius: Dp = 8.dp,
    refractionHeight: Dp = 24.dp,
    refractionAmount: Dp = 24.dp,
    chromaticAberration: Boolean = true,
    depthEffect: Boolean = true,
    surfaceTint: Color = Color.White.copy(alpha = 0.08f),
): Modifier {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        // Fallback for Android < 13
        return this
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f), shape)
            .border(0.5.dp, Color.White.copy(alpha = 0.2f), shape)
    }

    val backdrop = LocalRefractionBackdrop.current
    return this
        .clip(shape)
        then RefractionNodeElement(
            backdrop = backdrop,
            shape = shape,
            blurRadius = blurRadius,
            refractionHeight = refractionHeight,
            refractionAmount = refractionAmount,
            chromaticAberration = chromaticAberration,
            depthEffect = depthEffect,
            surfaceTint = surfaceTint
        )
        .border(0.5.dp, Color.White.copy(alpha = 0.22f), shape)
}

private class RefractionNodeElement(
    val backdrop: RefractionBackdrop,
    val shape: CornerBasedShape,
    val blurRadius: Dp,
    val refractionHeight: Dp,
    val refractionAmount: Dp,
    val chromaticAberration: Boolean,
    val depthEffect: Boolean,
    val surfaceTint: Color,
) : ModifierNodeElement<RefractionNode>() {
    override fun create(): RefractionNode = RefractionNode(
        backdrop, shape, blurRadius, refractionHeight, refractionAmount,
        chromaticAberration, depthEffect, surfaceTint
    )
    override fun update(node: RefractionNode) {
        node.backdrop = backdrop
        node.shape = shape
        node.blurRadius = blurRadius
        node.refractionHeight = refractionHeight
        node.refractionAmount = refractionAmount
        node.chromaticAberration = chromaticAberration
        node.depthEffect = depthEffect
        node.surfaceTint = surfaceTint
    }
    override fun InspectorInfo.inspectableProperties() { name = "refractiveLens" }
    override fun equals(other: Any?): Boolean = other is RefractionNodeElement && backdrop == other.backdrop
    override fun hashCode(): Int = backdrop.hashCode()
}

private class RefractionNode(
    var backdrop: RefractionBackdrop,
    var shape: CornerBasedShape,
    var blurRadius: Dp,
    var refractionHeight: Dp,
    var refractionAmount: Dp,
    var chromaticAberration: Boolean,
    var depthEffect: Boolean,
    var surfaceTint: Color,
) : DrawModifierNode, GlobalPositionAwareModifierNode, Modifier.Node() {

    private var layoutCoordinates: LayoutCoordinates? by mutableStateOf(null)
    private var graphicsLayer: GraphicsLayer? = null
    private val backdropScale = 0.33f // 1/3 resolution downscale for 120 FPS performance

    override fun ContentDrawScope.draw() {
        val coords = layoutCoordinates ?: run { drawContent(); return }
        val layer = graphicsLayer ?: obtainGraphicsContext().createGraphicsLayer().also { graphicsLayer = it }

        val density = this.density
        val blurPx = blurRadius.toPx() * backdropScale
        val refHeightPx = refractionHeight.toPx() * backdropScale
        val refAmountPx = refractionAmount.toPx() * backdropScale

        val scale = backdropScale
        val padding = blurPx

        layer.record(
            IntSize(
                ((size.width * scale).toInt() + padding.toInt() * 2).coerceAtLeast(1),
                ((size.height * scale).toInt() + padding.toInt() * 2).coerceAtLeast(1)
            )
        ) {
            val canvas = drawContext.canvas
            if (padding != 0f) canvas.translate(padding, padding)
            if (scale != 1f) canvas.scale(scale, scale)

            with(backdrop) {
                drawBackdrop(density = this@draw, coordinates = coords)
            }

            if (scale != 1f) canvas.scale(1f / scale, 1f / scale)
            if (padding != 0f) canvas.translate(-padding, -padding)
        }

        // ── 1. Create Gaussian Blur RenderEffect ──
        val blurEffect = android.graphics.RenderEffect.createBlurEffect(
            blurPx, blurPx, Shader.TileMode.CLAMP
        )

        // ── 2. Create AGSL Lens Refraction Shader ──
        val shader = RuntimeShader(
            if (chromaticAberration) RoundedRectRefractionWithDispersionShaderString
            else RoundedRectRefractionShaderString
        )
        val scaledWidth = size.width * backdropScale
        val scaledHeight = size.height * backdropScale
        shader.setFloatUniform("size", scaledWidth, scaledHeight)
        shader.setFloatUniform("offset", 0f, 0f)
        val r = scaledHeight / 2f
        shader.setFloatUniform("cornerRadii", r, r, r, r)
        shader.setFloatUniform("refractionHeight", refHeightPx)
        shader.setFloatUniform("refractionAmount", -refAmountPx)
        shader.setFloatUniform("depthEffect", if (depthEffect) 1f else 0f)
        if (chromaticAberration) {
            shader.setFloatUniform("chromaticAberration", 1f)
        }

        val lensEffect = android.graphics.RenderEffect.createRuntimeShaderEffect(shader, "content")

        // ── 3. Chain: Blur -> Lens Refraction ──
        val combinedEffect = android.graphics.RenderEffect.createChainEffect(lensEffect, blurEffect)
        layer.renderEffect = combinedEffect.asComposeRenderEffect()

        // Draw the refracted layer
        drawLayer(layer)

        // Draw surface tint overlay
        drawRect(color = surfaceTint)

        // Draw child content (icons, text)
        drawContent()
    }

    override fun onGloballyPositioned(coordinates: LayoutCoordinates) {
        if (coordinates.isAttached) layoutCoordinates = coordinates
    }

    override fun onDetach() {
        layoutCoordinates = null
        graphicsLayer?.let { obtainGraphicsContext().releaseGraphicsLayer(it) }
        graphicsLayer = null
    }

    private fun obtainGraphicsContext() = requireDensity().let {
        (this as androidx.compose.ui.node.DelegatableNode).requireGraphicsContext()
    }
}
```

---

## 📄 File 3: `RefractionBottomBar.kt`

The bottom navigation bar that uses `.refractiveLens(...)`.

```kotlin
package com.yourapp.ui.refraction

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

data class RefractionTab(val title: String, val icon: ImageVector)

@Composable
fun RefractionBottomBar(
    tabs: List<RefractionTab>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val pillShape = remember { RoundedCornerShape(percent = 50) }

    Box(
        modifier = modifier
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .fillMaxWidth()
            .height(64.dp)
            // ── APPLY THE 3D PHYSICAL REFRACTIVE LENS ──
            .refractiveLens(
                shape = pillShape,
                blurRadius = 8.dp,
                refractionHeight = 24.dp,    // Bevel thickness
                refractionAmount = 26.dp,    // Optical bending force
                chromaticAberration = true,  // Rainbow prismatic dispersion
                depthEffect = true,
                surfaceTint = Color.White.copy(alpha = 0.10f)
            )
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEachIndexed { index, tab ->
                val isSelected = index == selectedIndex
                val tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.55f)

                IconButton(
                    onClick = { onTabSelected(index) },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.title,
                        tint = tint,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
    }
}
```

---

## 📄 File 4: `RefractionDemoScreen.kt`

A test screen loaded with vibrant gradients, high-contrast text, and colorful album-style cards so the optical refraction and rainbow dispersion are intensely visible.

```kotlin
package com.yourapp.ui.refraction

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RefractionDemoScreen() {
    // 1. Create opaque window floor
    val windowBg = MaterialTheme.colorScheme.background
    val paintFloor: ContentDrawScope.() -> Unit = remember(windowBg) {
        {
            drawRect(windowBg)
            drawContent()
        }
    }

    // 2. Initialize the backdrop recorder
    val refractionBackdrop = rememberRefractionBackdrop(onDraw = paintFloor)
    var selectedTab by remember { mutableIntStateOf(0) }

    val tabs = remember {
        listOf(
            RefractionTab("Home", Icons.Rounded.Home),
            RefractionTab("Explore", Icons.Rounded.Explore),
            RefractionTab("Liked", Icons.Rounded.Favorite),
            RefractionTab("Profile", Icons.Rounded.Person),
        )
    }

    CompositionLocalProvider(LocalRefractionBackdrop provides refractionBackdrop) {
        Box(modifier = Modifier.fillMaxSize()) {

            // ── 3. Content recorded for refraction ──
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .recordForRefraction(refractionBackdrop)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 48.dp, bottom = 120.dp, start = 16.dp, end = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Text(
                            text = "Physical Lens Refraction",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Scroll down. Notice how the edges of the bar bend and split colors into rainbow channels.",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                    }

                    items(20) { index ->
                        val brush = when (index % 4) {
                            0 -> Brush.horizontalGradient(listOf(Color(0xFFFF0844), Color(0xFFFFB199)))
                            1 -> Brush.horizontalGradient(listOf(Color(0xFF00C6FB), Color(0xFF005BEA)))
                            2 -> Brush.horizontalGradient(listOf(Color(0xFFF355DA), Color(0xFF7000FF)))
                            else -> Brush.horizontalGradient(listOf(Color(0xFF11998E), Color(0xFF38EF7D)))
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(88.dp),
                            shape = RoundedCornerShape(16.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(brush)
                                    .padding(horizontal = 20.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = "Optical Target #$index",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // ── 4. Floating Refraction Bottom Bar ──
            RefractionBottomBar(
                tabs = tabs,
                selectedIndex = selectedTab,
                onTabSelected = { selectedTab = it },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}
```

---

## 🎛️ Refraction Parameters Tuning

In `.refractiveLens(...)`, you can customize:

| Parameter | Default | Effect |
|---|---|---|
| `refractionHeight` | `24.dp` | **Bevel Width**: Determines how deep into the pill the optical curvature extends from the edge. |
| `refractionAmount` | `26.dp` | **Lens Power**: The magnification / displacement strength. Higher values cause extreme bending; lower values produce subtle curvature. |
| `chromaticAberration` | `true` | **Rainbow Prism**: When `true`, separates RGB wavelengths along the corner normal vector. When `false`, uses monochromatic refraction. |
| `depthEffect` | `true` | **Spherical Dome**: Adds a radial lens center pull so the center of the bar has a subtle physical convex bulge. |
| `surfaceTint` | `White 10%` | **Translucency Tint**: Light frosted overlay to give body to the glass. |

