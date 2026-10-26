package com.couplefit.ui.dashboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.couplefit.ui.theme.AppSpacing

@Composable
fun DashboardAdaptiveScreen(
    windowSizeClass: WindowSizeClass,
    isDarkTheme: Boolean,
    onToggleDarkTheme: () -> Unit,
    reduceMotion: Boolean = false,
    onOpenSettings: () -> Unit = {}
) {
    if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact) {
        // Phone portrait mode
        DashboardScreen(
            isDarkTheme = isDarkTheme,
            onToggleDarkTheme = onToggleDarkTheme,
            reduceMotion = reduceMotion,
            onOpenSettings = onOpenSettings
        )
    } else {
        // Tablet or Landscape mode: Two-pane layout
        Row(modifier = Modifier.fillMaxSize()) {
            // Pane 1: Dashboard (List/Overview)
            DashboardScreen(
                modifier = Modifier.weight(1.1f),
                isDarkTheme = isDarkTheme,
                onToggleDarkTheme = onToggleDarkTheme,
                reduceMotion = reduceMotion,
                onOpenSettings = onOpenSettings
            )
            
            // Pane 2: Detail Panel
            Box(
                modifier = Modifier
                    .weight(0.9f)
                    .fillMaxSize()
                    .padding(AppSpacing.xl),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Select a metric to see detailed history",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


