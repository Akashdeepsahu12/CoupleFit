package com.couplefit.ui.glass

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.graphics.Shader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.GlobalPositionAwareModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.requireDensity
import androidx.compose.ui.node.requireGraphicsContext
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toIntSize
import androidx.compose.ui.util.fastCoerceAtLeast
import org.intellij.lang.annotations.Language

// =========================================================================
// 1. PHYSICAL AGSL REFRACTION & CHROMATIC DISPERSION SHADERS (BitChord Engine)
// =========================================================================

@Language("AGSL")
private const val RoundedRectSDF = """
float radiusAt(float2 coord, float4 radii) {
    if (coord.x >= 0.0) {
        if (coord.y <= 0.0) return radii.y;
        else return radii.z;
    } else {
        if (coord.y <= 0.0) return radii.x;
        else return radii.w;
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

@Language("AGSL")
private const val RoundedRectRefractionShaderString = """
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

@Language("AGSL")
private const val RoundedRectRefractionWithDispersionShaderString = """
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

// =========================================================================
// 2. BACKDROP INTERFACE & LAYER RECORDER
// =========================================================================

interface Backdrop {
    val isCoordinatesDependent: Boolean
    fun DrawScope.drawBackdrop(
        density: Density,
        coordinates: LayoutCoordinates?,
        layerBlock: (GraphicsLayerScope.() -> Unit)? = null
    )
}

@Composable
fun rememberLayerBackdrop(
    graphicsLayer: GraphicsLayer = rememberGraphicsLayer(),
    onDraw: ContentDrawScope.() -> Unit = { drawContent() }
): LayerBackdrop {
    return remember(graphicsLayer, onDraw) {
        LayerBackdrop(graphicsLayer, onDraw)
    }
}

@Stable
class LayerBackdrop internal constructor(
    val graphicsLayer: GraphicsLayer,
    internal val onDraw: ContentDrawScope.() -> Unit
) : Backdrop {
    override val isCoordinatesDependent: Boolean = true
    internal var layerCoordinates: LayoutCoordinates? by mutableStateOf(null)

    override fun DrawScope.drawBackdrop(
        density: Density,
        coordinates: LayoutCoordinates?,
        layerBlock: (GraphicsLayerScope.() -> Unit)?
    ) {
        val coords = coordinates ?: return
        val lCoords = layerCoordinates ?: return
        val offset = try {
            lCoords.localPositionOf(coords, Offset.Zero)
        } catch (_: Exception) {
            coords.positionInWindow() - lCoords.positionInWindow()
        }
        drawContext.canvas.translate(-offset.x, -offset.y)
        drawLayer(graphicsLayer)
        drawContext.canvas.translate(offset.x, offset.y)
    }
}

fun Modifier.layerBackdrop(backdrop: LayerBackdrop): Modifier =
    this then LayerBackdropElement(backdrop)

private class LayerBackdropElement(val backdrop: LayerBackdrop) : ModifierNodeElement<LayerBackdropNode>() {
    override fun create(): LayerBackdropNode = LayerBackdropNode(backdrop)
    override fun update(node: LayerBackdropNode) {
        if (node.backdrop != backdrop) {
            node.backdrop.layerCoordinates = null
            node.backdrop = backdrop
        }
    }
    override fun InspectorInfo.inspectableProperties() { name = "layerBackdrop" }
    override fun equals(other: Any?): Boolean = other is LayerBackdropElement && backdrop == other.backdrop
    override fun hashCode(): Int = backdrop.hashCode()
}

private class LayerBackdropNode(var backdrop: LayerBackdrop) :
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

// =========================================================================
// 3. EFFECT SCOPE & DRAW BACKDROP MODIFIER
// =========================================================================

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private object BackdropShaderCache {
    val dispersionShader: RuntimeShader by lazy {
        RuntimeShader(RoundedRectRefractionWithDispersionShaderString)
    }
    val refractionShader: RuntimeShader by lazy {
        RuntimeShader(RoundedRectRefractionShaderString)
    }
}

interface BackdropEffectScope : Density {
    val size: Size
    val shape: Shape
    var padding: Float
    var renderEffect: android.graphics.RenderEffect?

    fun blur(radius: Float) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && radius > 0f) {
            val blur = android.graphics.RenderEffect.createBlurEffect(
                radius, radius, Shader.TileMode.CLAMP
            )
            renderEffect = renderEffect?.let { android.graphics.RenderEffect.createChainEffect(it, blur) } ?: blur
        }
    }

    fun colorControls(saturation: Float = 1f) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && saturation != 1f) {
            val cm = android.graphics.ColorMatrix().apply { setSaturation(saturation) }
            val colorFilter = android.graphics.RenderEffect.createColorFilterEffect(
                android.graphics.ColorMatrixColorFilter(cm)
            )
            renderEffect = renderEffect?.let { android.graphics.RenderEffect.createChainEffect(it, colorFilter) } ?: colorFilter
        }
    }

    fun lens(
        refractionHeight: Float,
        refractionAmount: Float,
        depthEffect: Boolean = false,
        chromaticAberration: Boolean = false
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        if (refractionHeight <= 0f || refractionAmount == 0f) return

        if (padding > 0f) {
            padding = (padding - refractionHeight).fastCoerceAtLeast(0f)
        }

        // AGSL-01: Use cached singleton RuntimeShader instance to prevent per-frame AGSL recompilation churn
        val shader = if (chromaticAberration) {
            BackdropShaderCache.dispersionShader
        } else {
            BackdropShaderCache.refractionShader
        }
        shader.setFloatUniform("size", size.width, size.height)
        shader.setFloatUniform("offset", 0f, 0f)
        val r = (size.height / 2f) // Pill corner radius
        shader.setFloatUniform("cornerRadii", r, r, r, r)
        shader.setFloatUniform("refractionHeight", refractionHeight)
        // Physical optics: SDF gradient points outward; negative refractionAmount bends rays inward (convex lens curvature)
        shader.setFloatUniform("refractionAmount", -kotlin.math.abs(refractionAmount))
        shader.setFloatUniform("depthEffect", if (depthEffect) 1f else 0f)
        if (chromaticAberration) {
            shader.setFloatUniform("chromaticAberration", 1f)
        }

        val lensEffect = android.graphics.RenderEffect.createRuntimeShaderEffect(shader, "content")
        // Correct Chain Effect Ordering: RenderEffect.createChainEffect(outer, inner)
        // blurEffect is evaluated first (inner), and lensEffect refracts the blurred result (outer)
        renderEffect = renderEffect?.let { existingEffect ->
            android.graphics.RenderEffect.createChainEffect(lensEffect, existingEffect)
        } ?: lensEffect
    }
}

fun Modifier.drawBackdrop(
    backdrop: Backdrop,
    shape: () -> Shape,
    effects: BackdropEffectScope.() -> Unit,
    onDrawSurface: (DrawScope.() -> Unit)? = null,
    backdropScale: Float = 1f
): Modifier = this then DrawBackdropElement(backdrop, shape, effects, onDrawSurface, backdropScale)

private class DrawBackdropElement(
    val backdrop: Backdrop,
    val shape: () -> Shape,
    val effects: BackdropEffectScope.() -> Unit,
    val onDrawSurface: (DrawScope.() -> Unit)?,
    val backdropScale: Float
) : ModifierNodeElement<DrawBackdropNode>() {
    override fun create(): DrawBackdropNode = DrawBackdropNode(backdrop, shape, effects, onDrawSurface, backdropScale)
    override fun update(node: DrawBackdropNode) {
        node.backdrop = backdrop
        node.shapeProvider = shape
        node.effects = effects
        node.onDrawSurface = onDrawSurface
        node.backdropScale = backdropScale
    }
    override fun InspectorInfo.inspectableProperties() { name = "drawBackdrop" }
    override fun equals(other: Any?): Boolean = other is DrawBackdropElement && backdrop == other.backdrop
    override fun hashCode(): Int = backdrop.hashCode()
}

private class DrawBackdropNode(
    var backdrop: Backdrop,
    var shapeProvider: () -> Shape,
    var effects: BackdropEffectScope.() -> Unit,
    var onDrawSurface: (DrawScope.() -> Unit)?,
    var backdropScale: Float
) : DrawModifierNode, GlobalPositionAwareModifierNode, Modifier.Node() {

    private var layoutCoordinates: LayoutCoordinates? by mutableStateOf(null)
    private var graphicsLayer: GraphicsLayer? = null

    override fun ContentDrawScope.draw() {
        val coords = layoutCoordinates ?: run { drawContent(); return }
        val layer = graphicsLayer ?: obtainGraphicsContext().createGraphicsLayer().also { graphicsLayer = it }

        val scope = object : BackdropEffectScope {
            override val density: Float get() = this@draw.density
            override val fontScale: Float get() = this@draw.fontScale
            override val size: Size get() = this@draw.size * backdropScale
            override val shape: Shape get() = shapeProvider()
            override var padding: Float = 0f
            override var renderEffect: android.graphics.RenderEffect? = null
        }
        scope.effects()

        val scale = backdropScale
        val padding = scope.padding

        val layerW = ((size.width * scale).toInt() + padding.toInt() * 2).coerceAtLeast(1)
        val layerH = ((size.height * scale).toInt() + padding.toInt() * 2).coerceAtLeast(1)

        layer.record(IntSize(layerW, layerH)) {
            val canvas = drawContext.canvas
            if (padding != 0f) canvas.translate(padding, padding)
            if (scale != 1f) canvas.scale(scale, scale)

            with(backdrop) {
                drawBackdrop(density = this@draw, coordinates = coords)
            }

            if (scale != 1f) canvas.scale(1f / scale, 1f / scale)
            if (padding != 0f) canvas.translate(-padding, -padding)
        }

        layer.renderEffect = scope.renderEffect?.asComposeRenderEffect()

        // 1. Draw the refracted backdrop
        if (scale != 1f || padding != 0f) {
            scale(1f / scale, 1f / scale, pivot = Offset.Zero) {
                if (padding != 0f) {
                    translate(-padding, -padding) {
                        drawLayer(layer)
                    }
                } else {
                    drawLayer(layer)
                }
            }
        } else {
            drawLayer(layer)
        }

        // 2. Draw tinted surface overlay
        onDrawSurface?.invoke(this)

        // 3. Draw child content (100% crisp, zero RenderEffect distortion!)
        drawContent()
    }

    override fun onGloballyPositioned(coordinates: LayoutCoordinates) {
        if (coordinates.isAttached) {
            layoutCoordinates = coordinates
        }
    }

    override fun onDetach() {
        layoutCoordinates = null
        graphicsLayer?.let { obtainGraphicsContext().releaseGraphicsLayer(it) }
        graphicsLayer = null
    }

    private fun obtainGraphicsContext() = (this as DelegatableNode).requireGraphicsContext()
}
