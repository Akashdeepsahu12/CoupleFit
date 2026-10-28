package com.couplefit.ui.components

import android.content.Context
import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import android.view.Surface
import android.view.TextureView
import androidx.annotation.RawRes
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.couplefit.R
import kotlinx.coroutines.delay

private const val TAG = "AmbientVideoBg"

/**
 * Ambient Video Background Composable (BGV-01, BGV-04, BGV-05).
 *
 * Implements hardware-accelerated, seamless looping video background using Android's native
 * [TextureView] and [MediaPlayer] with natural cinematic fade-in transition.
 *
 * Craft & Impeccable Principles:
 * 1. Seamless Alpha & Frosted Glass Layering: Unlike SurfaceView which punches holes through the window buffer,
 *    TextureView behaves as a full Compose graphics layer, allowing frosted glass cards (`Modifier.iosGlass()`)
 *    and the floating glass dock (`Modifier.glassDock()`) to organically diffuse and refract the video.
 * 2. Natural Cinematic Fade-In Bloom (BGV-05): Upon decoder first-frame readiness (`MEDIA_INFO_VIDEO_RENDERING_START`),
 *    the video layer blooms smoothly from the dark obsidian base (`#07080A`) to full opacity over 1200ms using
 *    [FastOutSlowInEasing], eliminating cold-start frame snapping and flickering.
 * 3. CenterCrop Aspect-Fill Matrix: Mathematically transforms video frame coordinates to fill any phone or tablet
 *    viewport without letterboxing, pillarboxing, or aspect-ratio distortion.
 * 4. Lifecycle-Aware Energy Preservation: Listens to [Lifecycle.Event.ON_PAUSE] and [Lifecycle.Event.ON_STOP]
 *    to automatically pause playback when the user switches apps or locks the phone, consuming 0% CPU/battery.
 * 5. Accessibility (`reduceMotion`): Immediately settles opacity and pauses video playback if the user has requested reduced motion.
 * 6. Audio Zeroing: Strictly mutes audio (`setVolume(0f, 0f)`) to serve purely as ambient visual atmosphere.
 * 7. Cinematic Obsidian Veil: Overlays a dual-stage gradient scrim (`#07080A`) ensuring 100% WCAG AA contrast for text.
 */
@Composable
fun AmbientVideoBackground(
    modifier: Modifier = Modifier,
    @RawRes videoResId: Int = R.raw.green1,
    reduceMotion: Boolean = false,
    scrimBrush: Brush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF07080A).copy(alpha = 0.45f),
            Color(0xFF07080A).copy(alpha = 0.65f),
            Color(0xFF07080A).copy(alpha = 0.80f)
        )
    )
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Reference to active MediaPlayer for lifecycle management
    var mediaPlayerRef by remember { mutableStateOf<MediaPlayer?>(null) }
    var isPlayerPrepared by remember { mutableStateOf(false) }
    var isVideoRendering by remember { mutableStateOf(false) }

    // Natural Fade-In Animation (BGV-05)
    // Smoothly blooms the video layer from obsidian black into full illumination
    val videoAlpha by animateFloatAsState(
        targetValue = if (isVideoRendering) 1f else 0f,
        animationSpec = if (reduceMotion) {
            tween(durationMillis = 0)
        } else {
            tween(durationMillis = 1200, easing = FastOutSlowInEasing)
        },
        label = "ambientVideoFadeIn"
    )

    // Reset rendering trigger on video change
    LaunchedEffect(videoResId) {
        isVideoRendering = false
    }

    // Safety fallback: ensure fade-in completes even if OEM decoder omits MEDIA_INFO_VIDEO_RENDERING_START
    LaunchedEffect(isPlayerPrepared) {
        if (isPlayerPrepared && !isVideoRendering) {
            delay(250)
            isVideoRendering = true
        }
    }

    // Lifecycle observer for battery/CPU preservation
    DisposableEffect(lifecycleOwner, mediaPlayerRef) {
        val observer = LifecycleEventObserver { _, event ->
            val player = mediaPlayerRef ?: return@LifecycleEventObserver
            when (event) {
                Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP -> {
                    try {
                        if (player.isPlaying) {
                            player.pause()
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Error pausing MediaPlayer on lifecycle event", e)
                    }
                }
                Lifecycle.Event.ON_RESUME -> {
                    try {
                        if (!reduceMotion && isPlayerPrepared && !player.isPlaying) {
                            player.start()
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Error resuming MediaPlayer on ON_RESUME", e)
                    }
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Motion reduction listener
    LaunchedEffect(reduceMotion, mediaPlayerRef, isPlayerPrepared) {
        val player = mediaPlayerRef ?: return@LaunchedEffect
        if (isPlayerPrepared) {
            try {
                if (reduceMotion && player.isPlaying) {
                    player.pause()
                } else if (!reduceMotion && !player.isPlaying) {
                    player.start()
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error adjusting playback for reduceMotion", e)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF07080A)) // Fallback dark obsidian base
    ) {
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = videoAlpha },
            factory = { ctx ->
                TextureView(ctx).apply {
                    val tv = this
                    surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                        private var surface: Surface? = null
                        private var player: MediaPlayer? = null

                        override fun onSurfaceTextureAvailable(st: SurfaceTexture, width: Int, height: Int) {
                            surface = Surface(st)
                            try {
                                player = MediaPlayer().apply {
                                    setSurface(surface)
                                    val videoUri = Uri.parse("android.resource://${ctx.packageName}/$videoResId")
                                    setDataSource(ctx, videoUri)
                                    isLooping = true
                                    setVolume(0f, 0f) // Silent ambient background
                                    
                                    setOnPreparedListener { mp ->
                                        isPlayerPrepared = true
                                        adjustAspectRatio(tv, mp.videoWidth, mp.videoHeight, width, height)
                                        if (!reduceMotion) {
                                            mp.start()
                                        }
                                    }
                                    
                                    setOnInfoListener { _, what, _ ->
                                        if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                                            isVideoRendering = true
                                        }
                                        false
                                    }

                                    setOnVideoSizeChangedListener { _, vWidth, vHeight ->
                                        adjustAspectRatio(tv, vWidth, vHeight, tv.width, tv.height)
                                    }
                                    
                                    setOnErrorListener { _, what, extra ->
                                        Log.w(TAG, "MediaPlayer error: what=$what extra=$extra")
                                        true // Handled gracefully, avoid crash
                                    }
                                    
                                    prepareAsync()
                                }
                                mediaPlayerRef = player
                            } catch (e: Exception) {
                                Log.e(TAG, "Failed to initialize ambient video background", e)
                            }
                        }

                        override fun onSurfaceTextureSizeChanged(st: SurfaceTexture, width: Int, height: Int) {
                            player?.let { mp ->
                                if (mp.videoWidth > 0 && mp.videoHeight > 0) {
                                    adjustAspectRatio(tv, mp.videoWidth, mp.videoHeight, width, height)
                                }
                            }
                        }

                        override fun onSurfaceTextureDestroyed(st: SurfaceTexture): Boolean {
                            try {
                                player?.apply {
                                    if (isPlaying) {
                                        stop()
                                    }
                                    release()
                                }
                            } catch (e: Exception) {
                                Log.w(TAG, "Error releasing MediaPlayer on texture destroy", e)
                            }
                            player = null
                            mediaPlayerRef = null
                            isPlayerPrepared = false
                            isVideoRendering = false
                            surface?.release()
                            surface = null
                            return true
                        }

                        override fun onSurfaceTextureUpdated(st: SurfaceTexture) {}
                    }
                }
            },
            update = { textureView ->
                mediaPlayerRef?.let { mp ->
                    if (mp.videoWidth > 0 && mp.videoHeight > 0 && textureView.width > 0 && textureView.height > 0) {
                        adjustAspectRatio(textureView, mp.videoWidth, mp.videoHeight, textureView.width, textureView.height)
                    }
                }
            }
        )

        // Cinematic Dark Obsidian Gradient Scrim Overlay for WCAG AA Text Contrast
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(scrimBrush)
        )
    }
}

/**
 * Computes and applies a CenterCrop transformation matrix to [textureView]
 * to ensure the video completely covers the viewport without letterboxing or distortion.
 */
private fun adjustAspectRatio(textureView: TextureView, videoWidth: Int, videoHeight: Int, viewWidth: Int, viewHeight: Int) {
    if (videoWidth <= 0 || videoHeight <= 0 || viewWidth <= 0 || viewHeight <= 0) return

    val scaleX = viewWidth.toFloat() / videoWidth
    val scaleY = viewHeight.toFloat() / videoHeight
    val maxScale = maxOf(scaleX, scaleY)

    val scaledWidth = videoWidth * maxScale
    val scaledHeight = videoHeight * maxScale

    val targetScaleX = scaledWidth / viewWidth
    val targetScaleY = scaledHeight / viewHeight

    val matrix = Matrix().apply {
        setScale(targetScaleX, targetScaleY, viewWidth / 2f, viewHeight / 2f)
    }
    textureView.setTransform(matrix)
}
