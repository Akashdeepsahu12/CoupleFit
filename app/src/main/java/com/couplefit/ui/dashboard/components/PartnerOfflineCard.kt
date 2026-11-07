package com.couplefit.ui.dashboard.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.couplefit.ui.components.NumberPopInCounter
import com.couplefit.ui.theme.*

/**
 * Partner Card: displays offline cached values from Room SSOT with last updated label (KRP-02).
 */
@Composable
fun PartnerOfflineCard(
    partnerName: String,
    partnerSteps: Long,
    partnerActiveMinutes: Long,
    partnerCalories: Long,
    partnerAvgHr: Double,
    lastUpdatedLabel: String,
    animTrigger: Any? = null,
    reduceMotion: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .iosGlass(shape = MaterialTheme.shapes.large),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = null
    ) {
        Column(modifier = Modifier.padding(AppSpacing.md)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "$partnerName's Telemetry",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Room SSOT cache • Available offline",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SystemOrange.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, SystemOrange.copy(alpha = 0.35f))
                ) {
                    Text(
                        text = lastUpdatedLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = SystemOrange,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.sm))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "👟",
                            style = MaterialTheme.typography.titleSmall
                        )
                        NumberPopInCounter(
                            value = partnerSteps,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = StepsAccent
                            ),
                            animTrigger = animTrigger,
                            reduceMotion = reduceMotion
                        )
                    }
                    Text(
                        text = "Steps",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                MetricPill("Active", "${partnerActiveMinutes}m", ExerciseAccent, glyph = "⏱️", animTrigger = animTrigger, reduceMotion = reduceMotion)
                MetricPill("Calories", "%,d kcal".format(partnerCalories), CaloriesAccent, glyph = "🔥", animTrigger = animTrigger, reduceMotion = reduceMotion)
                if (partnerAvgHr > 0.0) {
                    MetricPill("Avg HR", "${partnerAvgHr.toInt()} BPM", VitalsAccent, glyph = "❤️", animTrigger = animTrigger, reduceMotion = reduceMotion)
                }
            }
        }
    }
}
