package com.couplefit.ui.dashboard.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

enum class SyncBlockerType {
    NONE,
    SDK_UPDATE_REQUIRED,
    PERMISSION_REVOKED,
    SHARING_DISABLED,
    EMPTY_HUB_FEEDER_UNLINKED,
    BATTERY_OPTIMIZED
}

data class SyncDiagnosticUiModel(
    val blockerType: SyncBlockerType = SyncBlockerType.NONE,
    val title: String = "",
    val description: String = "",
    val actionLabel: String = "",
    val usingHardwareSensorFallback: Boolean = false,
    val partnerDiagnosticNote: String? = null
)

@Composable
fun SyncDiagnosticBanner(
    diagnostic: SyncDiagnosticUiModel,
    onPrimaryActionClick: (SyncBlockerType) -> Unit,
    modifier: Modifier = Modifier
) {
    val isVisible = diagnostic.blockerType != SyncBlockerType.NONE ||
        diagnostic.partnerDiagnosticNote != null

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier
    ) {
        val cardShape = RoundedCornerShape(22.dp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .clip(cardShape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xEE1E293B),
                            Color(0xF00F172A)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0x9938BDF8),
                            Color(0x33FFFFFF)
                        )
                    ),
                    shape = cardShape
                )
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (diagnostic.blockerType != SyncBlockerType.NONE) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = diagnostic.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = diagnostic.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.90f)
                            )
                        }
                        if (diagnostic.actionLabel.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .padding(start = 12.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFF00E5FF).copy(alpha = 0.25f))
                                    .border(
                                        width = 1.dp,
                                        color = Color(0xFF00E5FF).copy(alpha = 0.85f),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .clickable { onPrimaryActionClick(diagnostic.blockerType) }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = diagnostic.actionLabel,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                diagnostic.partnerDiagnosticNote?.let { note ->
                    Text(
                        text = note,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFFFB74D),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
