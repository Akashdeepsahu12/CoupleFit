package com.couplefit.ui.theme

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

/**
 * Emil Kowalski sensory engineering haptic controller for CoupleFit (ENG-03, MIC-01).
 * Replaces generic OS vibrations with calibrated tactile waveforms.
 */
object CoupleFitHaptics {

    /**
     * Dual-pulse "Heartbeat Tick" (ENG-03, MIC-01)
     * Mimics an anatomical heartbeat rhythm:
     * - Pulse 1 ("Lub"): 12ms systolic pulse at ~40% amplitude (102/255)
     * - Gap: 80ms diastolic pause
     * - Pulse 2 ("Dub"): 18ms ventricular pulse at ~70% amplitude (178/255)
     */
    fun performHeartbeat(context: Context?, composeHaptic: HapticFeedback? = null) {
        if (context != null) {
            try {
                val vibrator = getVibrator(context)
                if (vibrator != null && vibrator.hasVibrator()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val timings = longArrayOf(0, 12, 80, 18)
                        val amplitudes = intArrayOf(0, 102, 0, 178)
                        val effect = if (vibrator.hasAmplitudeControl()) {
                            VibrationEffect.createWaveform(timings, amplitudes, -1)
                        } else {
                            VibrationEffect.createWaveform(timings, -1)
                        }
                        vibrator.vibrate(effect)
                        return
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(longArrayOf(0, 12, 80, 18), -1)
                        return
                    }
                }
            } catch (_: Throwable) {
                // Ignore and fall back to Compose haptic
            }
        }
        // Compose-level fallback if Vibrator is absent or mocked
        composeHaptic?.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }

    /**
     * Subtle mechanical tick (8ms light tap for tab change or button toggle)
     */
    fun performSubtleTick(context: Context?, composeHaptic: HapticFeedback? = null) {
        if (context != null) {
            try {
                val vibrator = getVibrator(context)
                if (vibrator != null && vibrator.hasVibrator()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
                        return
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(8, 80))
                        return
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(8)
                        return
                    }
                }
            } catch (_: Throwable) {
                // Ignore
            }
        }
        composeHaptic?.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }

    @Suppress("DEPRECATION")
    private fun getVibrator(context: Context): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }
}
