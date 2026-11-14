package com.couplefit.data.sensor

import android.content.Context
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class HardwareStepSensorManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val sensorManager: SensorManager? =
        context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val stepSensor: Sensor? =
        sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private val prefs: SharedPreferences =
        context.getSharedPreferences("couplefit_hw_step_baseline", Context.MODE_PRIVATE)

    val isHardwareSensorAvailable: Boolean
        get() = stepSensor != null

    /**
     * Reads cumulative boot steps from Sensor.TYPE_STEP_COUNTER and computes today's delta.
     * Used as a zero-friction fallback when Health Connect returns 0 steps ("Empty Hub").
     * Strictly read-only: never writes back to Health Connect (ADR-002).
     */
    suspend fun readTodayHardwareSteps(): Int {
        val manager = sensorManager ?: return 0
        val sensor = stepSensor ?: return 0

        val totalBootSteps = withTimeoutOrNull(1500L) {
            suspendCancellableCoroutine<Int?> { continuation ->
                val listener = object : SensorEventListener {
                    override fun onSensorChanged(event: SensorEvent?) {
                        val value = event?.values?.firstOrNull()?.toInt()
                        manager.unregisterListener(this)
                        if (continuation.isActive) continuation.resume(value)
                    }

                    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
                }

                val registered = manager.registerListener(
                    listener,
                    sensor,
                    SensorManager.SENSOR_DELAY_UI
                )
                if (!registered && continuation.isActive) {
                    continuation.resume(null)
                }
                continuation.invokeOnCancellation {
                    manager.unregisterListener(listener)
                }
            }
        } ?: return 0

        return computeDailyDeltaAndUpdateBaseline(
            todayKey = LocalDate.now().toString(),
            totalBootSteps = totalBootSteps,
            prefs = prefs
        )
    }

    companion object {
        internal fun computeDailyDeltaAndUpdateBaseline(
            todayKey: String,
            totalBootSteps: Int,
            prefs: SharedPreferences
        ): Int {
            val savedDate = prefs.getString("baseline_date", null)
            val savedBaseline = prefs.getInt("baseline_steps", -1)

            // Reset baseline on new calendar day or if device rebooted (totalBootSteps < savedBaseline)
            if (savedDate != todayKey || savedBaseline < 0 || totalBootSteps < savedBaseline) {
                prefs.edit()
                    .putString("baseline_date", todayKey)
                    .putInt("baseline_steps", totalBootSteps)
                    .apply()
                return 0
            }

            return (totalBootSteps - savedBaseline).coerceAtLeast(0)
        }
    }
}
