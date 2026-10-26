package com.couplefit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.couplefit.ui.theme.AppSpacing
import com.couplefit.ui.theme.CoupleFitIcons
import com.couplefit.ui.theme.SystemRed

/**
 * Root-level Material 3 alert dialog informing the user of lost network connectivity.
 */
@Composable
fun NoInternetDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit
) {
    if (isVisible) {
        AlertDialog(
            onDismissRequest = onDismiss,
            icon = {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(SystemRed.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedWifiOffIcon(
                        modifier = Modifier.size(28.dp),
                        tint = SystemRed
                    )
                }
            },
            title = {
                Text(
                    text = "No Internet Connection",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = "Please check your Wi-Fi or mobile data. Background sync and live updates are paused until reconnected.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(AppSpacing.sm)
                ) {
                    Text(
                        text = "Dismiss",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            shape = RoundedCornerShape(AppSpacing.md),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        )
    }
}

