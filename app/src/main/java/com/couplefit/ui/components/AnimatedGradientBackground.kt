package com.couplefit.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.couplefit.ui.theme.StepsAccent
import com.couplefit.ui.theme.ExerciseAccent
import com.couplefit.ui.theme.VitalsAccent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.animation.Animatable

@Composable
fun AnimatedGradientBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val baseColor = MaterialTheme.colorScheme.background
    
    // Lifecycle-aware flag to completely pause animation when app is not visible
    val lifecycleOwner = LocalLifecycleOwner.current
    var isAppForeground by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            isAppForeground = event == Lifecycle.Event.ON_RESUME
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Use Animatable for manual control, allowing us to pause/resume natively
    val color1Anim = remember { Animatable(baseColor) }
    val color2Anim = remember { Animatable(baseColor) }
    val color3Anim = remember { Animatable(baseColor) }

    val alpha1 = if (isDark) 0.15f else 0.08f
    val alpha2 = if (isDark) 0.10f else 0.06f
    val alpha3 = if (isDark) 0.12f else 0.07f

    val target1 = StepsAccent.copy(alpha = alpha1).compositeOver(baseColor)
    val target2 = ExerciseAccent.copy(alpha = alpha2).compositeOver(baseColor)
    val target3 = VitalsAccent.copy(alpha = alpha3).compositeOver(baseColor)

    // Slower transitions (Option B) - 45s loops
    LaunchedEffect(isAppForeground, isDark) {
        if (isAppForeground) {
            // Start looping animations
            launch {
                while (true) {
                    color1Anim.animateTo(target1, tween(40000, easing = LinearEasing))
                    color1Anim.animateTo(baseColor, tween(40000, easing = LinearEasing))
                }
            }
            launch {
                delay(15000) // Offset
                while (true) {
                    color2Anim.animateTo(target2, tween(45000, easing = LinearEasing))
                    color2Anim.animateTo(baseColor, tween(45000, easing = LinearEasing))
                }
            }
            launch {
                delay(30000) // Offset
                while (true) {
                    color3Anim.animateTo(target3, tween(50000, easing = LinearEasing))
                    color3Anim.animateTo(baseColor, tween(50000, easing = LinearEasing))
                }
            }
        } else {
            // App went to background - freeze animations immediately
            color1Anim.stop()
            color2Anim.stop()
            color3Anim.stop()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(color1Anim.value, color2Anim.value, color3Anim.value)
                )
            )
    ) {
        content()
    }
}
