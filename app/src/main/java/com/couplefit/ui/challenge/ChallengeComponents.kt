package com.couplefit.ui.challenge

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.couplefit.data.local.CoupleChallengeEntity
import com.couplefit.ui.theme.*

@Composable
fun SideBySideChallengeCard(
    challenge: CoupleChallengeEntity,
    partnerName: String = "Partner",
    modifier: Modifier = Modifier
) {
    val isStepsRace = challenge.type == "STEPS_RACE" || challenge.type == "TOTAL_STEPS"
    val unitLabel = if (isStepsRace) "steps" else "days"
    val title = if (isStepsRace) "🏃 Steps Race" else "🔥 Active Minutes Streak"

    val goal = challenge.goal.coerceAtLeast(1L)
    val myRatio = (challenge.myProgress.toFloat() / goal.toFloat()).coerceIn(0f, 1f)
    val partnerRatio = (challenge.partnerProgress.toFloat() / goal.toFloat()).coerceIn(0f, 1f)

    val animatedMyProgress by animateFloatAsState(
        targetValue = myRatio,
        animationSpec = tween(durationMillis = 1000),
        label = "myProgress"
    )
    val animatedPartnerProgress by animateFloatAsState(
        targetValue = partnerRatio,
        animationSpec = tween(durationMillis = 1000),
        label = "partnerProgress"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .iosGlass(shape = RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = null
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Title & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Goal: ${challenge.goal} $unitLabel • ${challenge.start} to ${challenge.end}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (challenge.status == "ACTIVE") Color(0x3332D74B) else Color(0x33FFD60A)
                ) {
                    Text(
                        text = if (challenge.status == "ACTIVE") "ACTIVE" else "COMPLETED",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (challenge.status == "ACTIVE") SystemGreen else Color(0xFFFFD60A),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Side-by-Side Progress Bars
            // 1. You
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "You",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = VitalsAccent
                    )
                    Text(
                        text = "${challenge.myProgress} / ${challenge.goal} $unitLabel (${(myRatio * 100).toInt()}%)",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(animatedMyProgress)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(VitalsAccent, Color(0xFF00E5FF))
                                )
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Partner
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = partnerName,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = StepsAccent
                    )
                    Text(
                        text = "${challenge.partnerProgress} / ${challenge.goal} $unitLabel (${(partnerRatio * 100).toInt()}%)",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(animatedPartnerProgress)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(StepsAccent, SystemRed)
                                )
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Standing / Winner Banner
            if (challenge.status == "COMPLETED") {
                val winnerText = when (challenge.winnerUserId) {
                    "me" -> "🏆 You won the challenge!"
                    "TIE" -> "🤝 It's a tie! Incredible teamwork!"
                    null -> "🏁 Challenge finished!"
                    else -> "🏆 $partnerName won the challenge!"
                }
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0x22FFD60A),
                    border = BorderStroke(1.dp, Color(0x66FFD60A))
                ) {
                    Text(
                        text = winnerText,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        ),
                        color = Color(0xFFFFD60A),
                        modifier = Modifier.padding(10.dp)
                    )
                }
            } else {
                val diff = challenge.myProgress - challenge.partnerProgress
                val leaderText = when {
                    diff > 0 -> "🏆 You're leading by $diff $unitLabel! 🔥"
                    diff < 0 -> "⚡ $partnerName is leading by ${-diff} $unitLabel! Keep pushing! 💪"
                    else -> "🤝 Tied neck-and-neck at ${challenge.myProgress} $unitLabel!"
                }
                Text(
                    text = leaderText,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = if (diff >= 0) SystemGreen else StepsAccent
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartChallengeDialog(
    onDismiss: () -> Unit,
    onStartChallenge: (type: String, goal: Long) -> Unit
) {
    var selectedType by remember { mutableStateOf("STEPS_RACE") }
    var selectedGoal by remember { mutableStateOf(50_000L) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = GlassPrimitives.SolidSlateModal,
        scrimColor = GlassPrimitives.TintDarkScrim75,
        shape = GlassComponentTokens.ModalCornerShape,
        modifier = Modifier
            .fillMaxWidth()
            .glassModal()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 520.dp)
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Start a Couple Challenge",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Compete with your partner over the next 7 days!",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Type Selectors
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedType == "STEPS_RACE",
                    onClick = {
                        selectedType = "STEPS_RACE"
                        selectedGoal = 50_000L
                    },
                    label = { Text("Steps Race") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = selectedType == "ACTIVE_MINUTES_STREAK",
                    onClick = {
                        selectedType = "ACTIVE_MINUTES_STREAK"
                        selectedGoal = 7L
                    },
                    label = { Text("Active Streak") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Goal Presets
            Text(
                text = if (selectedType == "STEPS_RACE") "Target Steps Goal:" else "Streak Days Goal:",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (selectedType == "STEPS_RACE") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(30_000L, 50_000L, 75_000L).forEach { g ->
                        FilterChip(
                            selected = selectedGoal == g,
                            onClick = { selectedGoal = g },
                            label = { Text("${g / 1000}k") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(5L, 7L, 14L).forEach { g ->
                        FilterChip(
                            selected = selectedGoal == g,
                            onClick = { selectedGoal = g },
                            label = { Text("$g Days") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    onStartChallenge(selectedType, selectedGoal)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SystemRed)
            ) {
                Text("Launch Challenge 🚀", fontWeight = FontWeight.Bold)
            }
        }
    }
}
