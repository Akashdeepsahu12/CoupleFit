package com.couplefit.widget

import android.content.Context
import android.content.Intent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.unit.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.couplefit.MainActivity
import com.couplefit.data.local.HealthDailyAggregateEntity
import com.couplefit.data.local.PartnerDailyEntity
import dagger.hilt.android.EntryPointAccessors
import timber.log.Timber
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Jetpack Glance Home-Screen Widget showing user's rings and partner's rings (FLOW-01).
 *
 * CRITICAL ARCHITECTURAL CONTRACT:
 * - Reads ONLY from Room database (HealthDao), maintaining strict offline-first SSOT.
 * - Provides zero-tap Home Screen ambient accountability.
 * - Features an interactive, one-tap "Poke 👋" action button running PokeActionCallback
 *   without triggering app cold start.
 */
class CoupleFitAppWidget : GlanceAppWidget() {

    companion object {
        @Volatile
        private var lastPokeTimestamp: Long = 0L

        fun markPoked() {
            lastPokeTimestamp = System.currentTimeMillis()
        }

        fun isRecentlyPoked(): Boolean {
            return (System.currentTimeMillis() - lastPokeTimestamp) < 4_000L // 4s confirmation window
        }
    }

    private fun widgetColor(color: androidx.compose.ui.graphics.Color): ColorProvider =
        ColorProvider(color)

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Query local Room database for today's user and partner records
        val todayStr = try {
            LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        } catch (e: Exception) {
            "2026-10-21"
        }

        var myDaily: HealthDailyAggregateEntity? = null
        var partnerDaily: PartnerDailyEntity? = null

        try {
            val entryPoint = EntryPointAccessors.fromApplication(
                context.applicationContext,
                WidgetEntryPoint::class.java
            )
            val healthDao = entryPoint.healthDao()
            myDaily = healthDao.getDailyAggregate(todayStr)
            partnerDaily = healthDao.getPartnerDaily(todayStr)
        } catch (e: Exception) {
            Timber.w(e, "Glance widget could not retrieve Room data directly from EntryPoint")
        }

        val mySteps = myDaily?.totalSteps ?: 0L
        val myActiveMins = myDaily?.totalActiveMinutes ?: 0L
        val myCalories = myDaily?.totalCalories ?: 0L

        val pSteps = partnerDaily?.steps ?: 0L
        val pActiveMins = partnerDaily?.activeMinutes ?: 0L
        val pCalories = partnerDaily?.calories ?: 0L

        val recentlyPoked = isRecentlyPoked()

        provideContent {
            GlanceTheme {
                val launchIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }

                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(androidx.compose.ui.graphics.Color(0xFF141416))
                        .cornerRadius(20.dp)
                        .padding(12.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(
                        modifier = GlanceModifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Header with Title and Interactive Poke Button (FLOW-01)
                        Row(
                            modifier = GlanceModifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "COUPLEFIT ACTIVITY",
                                style = TextStyle(
                                    color = widgetColor(androidx.compose.ui.graphics.Color(0xFF8E8E93)),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = GlanceModifier.clickable(actionStartActivity(launchIntent))
                            )
                            Spacer(modifier = GlanceModifier.defaultWeight())
                            
                            // Interactive Zero-Tap Poke Action Pill (FLOW-01)
                            Box(
                                modifier = GlanceModifier
                                    .background(
                                        if (recentlyPoked) widgetColor(androidx.compose.ui.graphics.Color(0xFFFF2D55).copy(alpha = 0.35f))
                                        else widgetColor(androidx.compose.ui.graphics.Color(0xFF2C2C2E))
                                    )
                                    .cornerRadius(12.dp)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .clickable(actionRunCallback<PokeActionCallback>())
                            ) {
                                Text(
                                    text = if (recentlyPoked) "Poked! 💖" else "Poke 👋",
                                    style = TextStyle(
                                        color = if (recentlyPoked) widgetColor(androidx.compose.ui.graphics.Color(0xFFFF375F))
                                                else widgetColor(androidx.compose.ui.graphics.Color(0xFF00E5FF)),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }

                        Spacer(modifier = GlanceModifier.height(8.dp))

                        // Side-by-Side Activity Gauges & Telemetry Cards (FLOW-01)
                        Row(
                            modifier = GlanceModifier.fillMaxWidth()
                                .clickable(actionStartActivity(launchIntent)),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Column 1: You
                            Column(
                                modifier = GlanceModifier.defaultWeight()
                                    .background(androidx.compose.ui.graphics.Color(0xFF1F1F24))
                                    .cornerRadius(12.dp)
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = "YOU",
                                    style = TextStyle(
                                        color = widgetColor(androidx.compose.ui.graphics.Color(0xFF00E5FF)),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Spacer(modifier = GlanceModifier.height(4.dp))
                                Text(
                                    text = "$mySteps",
                                    style = TextStyle(
                                        color = widgetColor(androidx.compose.ui.graphics.Color.White),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "Steps / 10k",
                                    style = TextStyle(
                                        color = widgetColor(androidx.compose.ui.graphics.Color(0xFFAAAAAA)),
                                        fontSize = 10.sp
                                    )
                                )
                                Spacer(modifier = GlanceModifier.height(4.dp))
                                // Bilateral Mini-Gauge (FLOW-01)
                                LinearProgressIndicator(
                                    progress = (mySteps.toFloat() / 10000f).coerceIn(0f, 1f),
                                    modifier = GlanceModifier.fillMaxWidth().height(4.dp),
                                    color = widgetColor(androidx.compose.ui.graphics.Color(0xFF00E5FF)),
                                    backgroundColor = widgetColor(androidx.compose.ui.graphics.Color(0xFF2C2C2E))
                                )
                                Spacer(modifier = GlanceModifier.height(4.dp))
                                Row {
                                    Text(
                                        text = "${myActiveMins}m",
                                        style = TextStyle(
                                            color = widgetColor(androidx.compose.ui.graphics.Color(0xFF32D74B)),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                    Spacer(modifier = GlanceModifier.width(6.dp))
                                    Text(
                                        text = "${myCalories}cal",
                                        style = TextStyle(
                                            color = widgetColor(androidx.compose.ui.graphics.Color(0xFFFF2D55)),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = GlanceModifier.width(8.dp))

                            // Column 2: Partner
                            Column(
                                modifier = GlanceModifier.defaultWeight()
                                    .background(androidx.compose.ui.graphics.Color(0xFF1F1F24))
                                    .cornerRadius(12.dp)
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = "PARTNER",
                                    style = TextStyle(
                                        color = widgetColor(androidx.compose.ui.graphics.Color(0xFFFF375F)),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Spacer(modifier = GlanceModifier.height(4.dp))
                                Text(
                                    text = "$pSteps",
                                    style = TextStyle(
                                        color = widgetColor(androidx.compose.ui.graphics.Color.White),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "Steps / 10k",
                                    style = TextStyle(
                                        color = widgetColor(androidx.compose.ui.graphics.Color(0xFFAAAAAA)),
                                        fontSize = 10.sp
                                    )
                                )
                                Spacer(modifier = GlanceModifier.height(4.dp))
                                // Bilateral Mini-Gauge (FLOW-01)
                                LinearProgressIndicator(
                                    progress = (pSteps.toFloat() / 10000f).coerceIn(0f, 1f),
                                    modifier = GlanceModifier.fillMaxWidth().height(4.dp),
                                    color = widgetColor(androidx.compose.ui.graphics.Color(0xFFFF375F)),
                                    backgroundColor = widgetColor(androidx.compose.ui.graphics.Color(0xFF2C2C2E))
                                )
                                Spacer(modifier = GlanceModifier.height(4.dp))
                                Row {
                                    Text(
                                        text = "${pActiveMins}m",
                                        style = TextStyle(
                                            color = widgetColor(androidx.compose.ui.graphics.Color(0xFF32D74B)),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                    Spacer(modifier = GlanceModifier.width(6.dp))
                                    Text(
                                        text = "${pCalories}cal",
                                        style = TextStyle(
                                            color = widgetColor(androidx.compose.ui.graphics.Color(0xFFFF2D55)),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
