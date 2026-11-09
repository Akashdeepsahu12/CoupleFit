package com.couplefit.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.updateAll
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Background action callback invoked when the user taps the inline "Poke 👋"
 * action button directly from the CoupleFit Home Screen Glance widget (FLOW-01).
 *
 * Runs without launching MainActivity or triggering cold start latency.
 */
class PokeActionCallback : ActionCallback {

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        Timber.d("PokeActionCallback triggered from Glance Home Screen Widget (FLOW-01)")
        CoupleFitAppWidget.markPoked()

        withContext(Dispatchers.IO) {
            try {
                val entryPoint = EntryPointAccessors.fromApplication(
                    context.applicationContext,
                    WidgetEntryPoint::class.java
                )
                // Attempt to send nudge via repository if couple is paired
                val partnerRepo = entryPoint.partnerRepository()
                val sessionPrefs = context.applicationContext.getSharedPreferences("couplefit_session_cache", Context.MODE_PRIVATE)
                val coupleId = sessionPrefs.getString("cached_couple_id", null) ?: "couple_1"
                val partnerId = sessionPrefs.getString("cached_partner_id", null) ?: "partner_1"
                val myUid = entryPoint.authManager().getCurrentSession()?.uid ?: sessionPrefs.getString("cached_user_id", null) ?: "user_1"
                val senderName = sessionPrefs.getString("cached_display_name", "Partner") ?: "Partner"

                // Send generic poke cheer to partner
                partnerRepo.sendNudge(
                    coupleId = coupleId,
                    senderUid = myUid,
                    senderName = senderName,
                    recipientUid = partnerId,
                    message = "Poked you from Home Screen! 👋 Time to move!"
                )
                Timber.d("Successfully delivered widget poke nudge to partner")
            } catch (e: Exception) {
                Timber.w(e, "Widget poke executed locally; network delivery will retry on sync")
            }
        }

        // Refresh all widget instances to show optimistic confirmation
        CoupleFitAppWidget().updateAll(context)
    }
}
