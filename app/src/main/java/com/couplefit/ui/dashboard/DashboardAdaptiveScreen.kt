package com.couplefit.ui.dashboard

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DashboardAdaptiveScreen(
    windowSizeClass: WindowSizeClass,
    isDarkTheme: Boolean,
    onToggleDarkTheme: () -> Unit
) {
    if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact) {
        // Phone portrait mode
        DashboardScreen(
            isDarkTheme = isDarkTheme,
            onToggleDarkTheme = onToggleDarkTheme
        )
    } else {
        // Tablet or Landscape mode: Two-pane layout
        Row(modifier = Modifier.fillMaxSize()) {
            // Pane 1: Dashboard (List/Overview)
            DashboardScreen(
                modifier = Modifier.weight(1f),
                isDarkTheme = isDarkTheme,
                onToggleDarkTheme = onToggleDarkTheme
            )
            
            // Pane 2: Detail Panel (Placeholder for expanded data)
            Text(
                text = "Select a metric to see details",
                modifier = Modifier
                    .weight(1f)
                    .padding(32.dp)
            )
        }
    }
}


