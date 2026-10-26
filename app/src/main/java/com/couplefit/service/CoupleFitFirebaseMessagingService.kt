package com.couplefit.service

import com.couplefit.data.local.CoupleFitPreferencesDataStore
import com.couplefit.util.NotificationHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class CoupleFitFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var dataStore: CoupleFitPreferencesDataStore

    @Inject
    lateinit var auth: FirebaseAuth

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Timber.d("FCM Registration Token refreshed: $token")

        serviceScope.launch {
            try {
                dataStore.saveFcmToken(token)
                val currentUid = auth.currentUser?.uid
                if (currentUid != null) {
                    dataStore.mirrorFcmTokenToFirestore(currentUid, token)
                }
            } catch (e: Exception) {
                Timber.e(e, "Error handling refreshed FCM token")
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Timber.d("FCM message received from: ${remoteMessage.from}")

        val data = remoteMessage.data
        val type = data["type"] ?: "NUDGE"
        val title = remoteMessage.notification?.title
            ?: data["title"]
            ?: if (type == "MILESTONE") "Partner Milestone Achieved!" else "CoupleFit Nudge! ❤️"
        val body = remoteMessage.notification?.body
            ?: data["body"]
            ?: data["message"]
            ?: if (type == "MILESTONE") "Your partner crushed a new fitness milestone!" else "Your partner sent you a nudge!"

        when (type) {
            "MILESTONE" -> {
                val milestoneKey = data["milestoneKey"] ?: data["milestone"]
                NotificationHelper.showMilestoneNotification(
                    context = applicationContext,
                    title = title,
                    body = body,
                    milestoneKey = milestoneKey
                )
            }
            "NUDGE" -> {
                val nudgeId = data["nudgeId"]
                NotificationHelper.showNudgeNotification(
                    context = applicationContext,
                    title = title,
                    body = body,
                    nudgeId = nudgeId
                )
            }
            else -> {
                NotificationHelper.showNudgeNotification(
                    context = applicationContext,
                    title = title,
                    body = body
                )
            }
        }
    }
}
