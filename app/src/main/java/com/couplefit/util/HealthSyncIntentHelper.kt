package com.couplefit.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.health.connect.client.HealthConnectClient

object HealthSyncIntentHelper {

    const val HEALTH_CONNECT_PACKAGE = "com.google.android.apps.healthdata"
    const val SAMSUNG_HEALTH_PACKAGE = "com.sec.android.app.shealth"
    const val GOOGLE_FIT_PACKAGE = "com.google.android.apps.fitness"

    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager ?: return true
        return pm.isIgnoringBatteryOptimizations(context.packageName)
    }

    @SuppressLint("BatteryLife")
    fun requestIgnoreBatteryOptimizations(context: Context) {
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:${context.packageName}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(intent) }.onFailure {
            runCatching {
                val fallbackIntent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallbackIntent)
            }
        }
    }

    fun openHealthConnectSettingsOrStore(context: Context) {
        val status = HealthConnectClient.getSdkStatus(context, HEALTH_CONNECT_PACKAGE)
        if (status == HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED ||
            status == HealthConnectClient.SDK_UNAVAILABLE
        ) {
            val playIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("market://details?id=$HEALTH_CONNECT_PACKAGE")
            ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
            runCatching { context.startActivity(playIntent) }.onFailure {
                context.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=$HEALTH_CONNECT_PACKAGE")
                    ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                )
            }
            return
        }

        val settingsAction = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            "android.health.connect.action.MANAGE_HEALTH_PERMISSIONS"
        } else {
            "androidx.health.ACTION_HEALTH_CONNECT_SETTINGS"
        }
        runCatching {
            context.startActivity(Intent(settingsAction).apply {
                putExtra(Intent.EXTRA_PACKAGE_NAME, context.packageName)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        }.onFailure {
            runCatching {
                context.startActivity(Intent("android.settings.HEALTH_CONNECT_SETTINGS").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
            }
        }
    }

    fun detectInstalledFeederAppName(context: Context): String? {
        val pm = context.packageManager
        return when {
            runCatching { pm.getPackageInfo(SAMSUNG_HEALTH_PACKAGE, 0) }.isSuccess -> "Samsung Health"
            runCatching { pm.getPackageInfo(GOOGLE_FIT_PACKAGE, 0) }.isSuccess -> "Google Fit"
            else -> null
        }
    }

    fun openPrimaryFeederApp(context: Context): Boolean {
        val pm = context.packageManager
        val targetPkg = listOf(SAMSUNG_HEALTH_PACKAGE, GOOGLE_FIT_PACKAGE).firstOrNull { pkg ->
            runCatching { pm.getPackageInfo(pkg, 0) }.isSuccess
        } ?: return false

        val launchIntent = pm.getLaunchIntentForPackage(targetPkg)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        } ?: return false

        return runCatching {
            context.startActivity(launchIntent)
            true
        }.getOrDefault(false)
    }
}
