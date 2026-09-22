package com.couplefit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import com.couplefit.ui.theme.CoupleFitTheme
import com.couplefit.ui.dashboard.DashboardAdaptiveScreen

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val prefs = getSharedPreferences("couplefit_prefs", Context.MODE_PRIVATE)

        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            val systemDark = isSystemInDarkTheme()
            
            // Read saved preference, defaulting to system theme if not set
            var isDarkTheme by remember { 
                mutableStateOf(prefs.getBoolean("is_dark_theme", systemDark)) 
            }
            
            CoupleFitTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DashboardAdaptiveScreen(
                        windowSizeClass = windowSizeClass,
                        isDarkTheme = isDarkTheme,
                        onToggleDarkTheme = {
                            val newTheme = !isDarkTheme
                            isDarkTheme = newTheme
                            prefs.edit().putBoolean("is_dark_theme", newTheme).apply()
                        }
                    )
                }
            }
        }
    }
}
