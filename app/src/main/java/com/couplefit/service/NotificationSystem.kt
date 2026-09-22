package com.couplefit.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationSystem @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val ALERTS_CHANNEL_ID = "critical_alerts_channel"
        const val REMINDERS_CHANNEL_ID = "reminders_channel"
    }

    private val notificationManager = 
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createChannels()
    }

    private fun createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val alertsChannel = NotificationChannel(
                ALERTS_CHANNEL_ID,
                "Critical Health Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for high/low heart rate or irregular rhythms"
                enableVibration(true)
            }

            val remindersChannel = NotificationChannel(
                REMINDERS_CHANNEL_ID,
                "Partner Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Nudges and medication reminders"
            }

            notificationManager.createNotificationChannels(listOf(alertsChannel, remindersChannel))
        }
    }

    fun sendCriticalAlert(title: String, message: String) {
        val notification = NotificationCompat.Builder(context, ALERTS_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
    
    fun sendReminder(title: String, message: String) {
        val notification = NotificationCompat.Builder(context, REMINDERS_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}

