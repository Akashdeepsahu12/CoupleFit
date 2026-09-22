package com.couplefit

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CoupleFitApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialization code here
    }
}

