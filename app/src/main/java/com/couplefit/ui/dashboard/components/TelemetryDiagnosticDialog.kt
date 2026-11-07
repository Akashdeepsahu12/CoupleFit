package com.couplefit.ui.dashboard.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.couplefit.ui.theme.*

/**
 * Telemetry Diagnostic Dialog providing transparent insight into partner sync status
 * and an immediate, high-priority "Ping Partner Device 📡" action (FLOW-03).
 */
@Composable
fun TelemetryDiagnosticDialog(
    partnerName: String,
    lastUpdatedLabel: String,
    isStale: Boolean,
    onDismiss: () -> Unit,
    onPingPartner: () -> Unit
) {
    var pingSent by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .dialogMeasure()
            .iosGlass(
                shape = RoundedCornerShape(28.dp),
                tintColor = GlassPrimitives.SolidSlateModal,
                blurRadius = GlassPrimitives.BlurModal
            ),
        containerColor = Color.Transparent,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = if (isStale) "🟡 Partner Sync Diagnostics" else "🟢 Partner Telemetry Live",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (isStale)
                        "$partnerName's device has not synced recently ($lastUpdatedLabel). You are viewing cached offline Room SSOT records."
                    else
                        "$partnerName's telemetry is currently in live synchronization with Health Connect and Firestore.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "• Room SSOT cache remains available offline\n• Tap Ping to send high-priority sync wake-up to $partnerName",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (pingSent) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SystemGreen.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, SystemGreen.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = "✓ High-priority sync ping sent to $partnerName!",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = SystemGreen,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            FilledTonalButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onPingPartner()
                    pingSent = true
                }
            ) {
                Text("Ping Partner Device 📡", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
