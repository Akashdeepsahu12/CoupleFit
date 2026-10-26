package com.couplefit.ui.healthconnect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.couplefit.ui.settings.PrivacyTransparencyScreen
import com.couplefit.ui.theme.CoupleFitTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PrivacyPolicyActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CoupleFitTheme {
                PrivacyTransparencyScreen(
                    onNavigateBack = { finish() }
                )
            }
        }
    }
}

