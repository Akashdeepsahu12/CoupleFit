package com.couplefit.ui.challenge

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.couplefit.data.local.CoupleChallengeEntity
import com.couplefit.ui.theme.AppSpacing
import com.couplefit.ui.theme.SystemGreen
import com.couplefit.ui.theme.SystemRed
import com.couplefit.ui.theme.iosGlass
import androidx.compose.ui.graphics.Color

/**
 * Compact Active Challenge Preview Card for Today Dashboard.
 * Implements Wireframe 1 specification from `card-sort-analysis.md` §7.2 (CSA-04).
 */
@Composable
fun ActiveChallengePreviewCard(
    challenge: CoupleChallengeEntity?,
    partnerName: String = "Partner",
    modifier: Modifier = Modifier,
    onViewAllChallenges: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = AppSpacing.xs)
            .iosGlass(shape = MaterialTheme.shapes.large)
            .semantics {
                contentDescription = "Active challenge preview card"
            },
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        border = null
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🏆", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ACTIVE CHALLENGE",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp
                    )
                }

                TextButton(
                    onClick = onViewAllChallenges,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "View Duels",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            if (challenge != null) {
                val diff = challenge.myProgress - challenge.partnerProgress
                val statusText = when {
                    diff > 0 -> "You're leading by $diff steps! 🔥"
                    diff < 0 -> "$partnerName is leading by ${-diff} steps! 💪"
                    else -> "Neck-and-neck at ${challenge.myProgress} steps! ⚡"
                }

                Text(
                    text = if (challenge.type == "STEPS_RACE") "Weekend Step Showdown" else "Active Streak Duel",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                val goal = challenge.goal.coerceAtLeast(1L)
                val ratio = (challenge.myProgress.toFloat() / goal.toFloat()).coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { ratio },
                    modifier = Modifier.fillMaxWidth().height(6.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surface
                )

                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = if (diff >= 0) SystemGreen else SystemRed
                )
            } else {
                Text(
                    text = "No active duel running right now.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Challenge $partnerName to a 24h step race!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
