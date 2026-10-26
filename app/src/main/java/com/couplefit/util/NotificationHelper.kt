package com.couplefit.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.couplefit.MainActivity
import com.couplefit.R
import timber.log.Timber

object NotificationHelper {

    const val CHANNEL_NUDGES_ID = "channel_nudges"
    const val CHANNEL_NUDGES_NAME = "Partner Nudges"

    const val CHANNEL_MILESTONES_ID = "channel_milestones"
    const val CHANNEL_MILESTONES_NAME = "Milestones & Celebrations"

    private const val NOTIFICATION_ID_NUDGE = 1001
    private const val NOTIFICATION_ID_MILESTONE = 2001

    /**
     * Initializes notification channels for nudges and milestones on Android 8.0+ (API 26+).
     */
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            ?: return

        // 1. Channel: Partner Nudges (High Importance, Pulsing Red Light, Urgent Vibration)
        val nudgesChannel = NotificationChannel(
            CHANNEL_NUDGES_ID,
            CHANNEL_NUDGES_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Instant encouragement and cheer nudges sent by your partner."
            enableLights(true)
            lightColor = Color.parseColor("#FF2D55") // CoupleFit System Red/Pink
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 200, 100, 200, 100, 300)
            setShowBadge(true)
        }

        // 2. Channel: Milestones & Celebrations (High Importance, Cyan Light, Celebratory Vibration)
        val milestonesChannel = NotificationChannel(
            CHANNEL_MILESTONES_ID,
            CHANNEL_MILESTONES_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Celebrations when you or your partner close rings or crush daily fitness goals."
            enableLights(true)
            lightColor = Color.parseColor("#00F0FF") // Cyan Neon
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 150, 100, 150, 100, 150, 100, 400)
            setShowBadge(true)
        }

        notificationManager.createNotificationChannels(listOf(nudgesChannel, milestonesChannel))
        Timber.d("Created notification channels: $CHANNEL_NUDGES_ID, $CHANNEL_MILESTONES_ID")
    }

    /**
     * Shows a nudge notification sent by the partner.
     */
    fun showNudgeNotification(
        context: Context,
        title: String,
        body: String,
        nudgeId: String? = null
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_NOTIFICATION_TYPE", "NUDGE")
            putExtra("EXTRA_NUDGE_ID", nudgeId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            (System.currentTimeMillis() % 10000).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notification = NotificationCompat.Builder(context, CHANNEL_NUDGES_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setColor(Color.parseColor("#FF2D55"))
            .setAutoCancel(true)
            .setSound(soundUri)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .build()

        try {
            val notificationId = (System.currentTimeMillis() % 100000).toInt()
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (e: SecurityException) {
            Timber.e(e, "POST_NOTIFICATIONS permission not granted to display nudge notification")
        }
    }

    /**
     * Shows a milestone achievement notification (e.g., ring closed, 10k steps).
     */
    fun showMilestoneNotification(
        context: Context,
        title: String,
        body: String,
        milestoneKey: String? = null
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_NOTIFICATION_TYPE", "MILESTONE")
            putExtra("EXTRA_MILESTONE_KEY", milestoneKey)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            (System.currentTimeMillis() % 10000).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notification = NotificationCompat.Builder(context, CHANNEL_MILESTONES_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setColor(Color.parseColor("#00F0FF"))
            .setAutoCancel(true)
            .setSound(soundUri)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .build()

        try {
            val notificationId = (System.currentTimeMillis() % 100000).toInt()
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (e: SecurityException) {
            Timber.e(e, "POST_NOTIFICATIONS permission not granted to display milestone notification")
        }
    }
}
