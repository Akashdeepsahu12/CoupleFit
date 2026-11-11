package com.couplefit

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class CoupleFitApplication : Application(), Configuration.Provider {
    
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var remoteConfigManager: com.couplefit.util.RemoteConfigManager

    override fun onCreate() {
        super.onCreate()
        
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        
        // Initialize App Check with Play Integrity Provider
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance()
        )

        // Initialize Firebase Crashlytics & Analytics
        val crashlytics = com.google.firebase.crashlytics.FirebaseCrashlytics.getInstance()
        crashlytics.setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
        val analytics = com.google.firebase.analytics.FirebaseAnalytics.getInstance(this)
        analytics.setAnalyticsCollectionEnabled(true)
        analytics.logEvent("app_open_init", android.os.Bundle().apply {
            putString("version_name", BuildConfig.VERSION_NAME)
        })

        // Initialize Timber logging
        if (BuildConfig.DEBUG) {
            timber.log.Timber.plant(timber.log.Timber.DebugTree())
        }

        // Fetch Remote Config (Challenge goals & Feature flags)
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            remoteConfigManager.fetchAndActivate()
        }

        // Initialize Notification Channels for Nudges and Milestones
        com.couplefit.util.NotificationHelper.createNotificationChannels(this)

        // Schedule Low-Frequency (6-Hour) Battery-Optimized Background Synchronization Worker (WRK-03)
        // Background sync is throttled to minimum required; active telemetry is user-driven via foreground pull-to-refresh
        scheduleBackgroundSyncWorker()

        // Fetch and refresh FCM device token
        com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                timber.log.Timber.d("Initial FCM Token acquired: $token")
                val currentUid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                if (currentUid != null && token != null) {
                    com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        .collection("users").document(currentUid)
                        .set(
                            mapOf(
                                "fcmToken" to token,
                                "fcmTokenUpdatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                            ),
                            com.google.firebase.firestore.SetOptions.merge()
                        )
                }
            } else {
                timber.log.Timber.w(task.exception, "Fetching FCM registration token failed")
            }
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    private fun scheduleBackgroundSyncWorker() {
        try {
            val constraints = androidx.work.Constraints.Builder()
                .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()

            // Throttled to 6 hours to minimize background battery drain & network bandwidth (PWR-01)
            val syncRequest = androidx.work.PeriodicWorkRequestBuilder<com.couplefit.service.SyncWorker>(
                6, java.util.concurrent.TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .build()

            androidx.work.WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "CoupleFitSyncWorker",
                androidx.work.ExistingPeriodicWorkPolicy.UPDATE,
                syncRequest
            )
            timber.log.Timber.d("Enqueued unique periodic work 'CoupleFitSyncWorker' (6 hour low-power interval)")
        } catch (e: Exception) {
            timber.log.Timber.w(e, "Failed to schedule background SyncWorker")
        }
    }
}
