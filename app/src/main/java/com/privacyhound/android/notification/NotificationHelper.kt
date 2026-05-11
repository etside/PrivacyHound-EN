package com.privacyhound.android.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.privacyhound.android.MainActivity
import com.privacyhound.android.R
import com.privacyhound.android.service.MonitorService

object NotificationHelper {

    const val CHANNEL_MONITOR_ID = "privacyhound_monitor"
    const val CHANNEL_ALERT_ID = "privacyhound_alert"

    const val NOTIFICATION_ID_MONITOR = 1001

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val monitor = NotificationChannel(
            CHANNEL_MONITOR_ID,
            context.getString(R.string.channel_monitor_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = context.getString(R.string.channel_monitor_desc)
            setShowBadge(false)
        }

        val alert = NotificationChannel(
            CHANNEL_ALERT_ID,
            context.getString(R.string.channel_alert_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.channel_alert_desc)
        }

        nm.createNotificationChannel(monitor)
        nm.createNotificationChannel(alert)
    }

    fun buildMonitorNotification(context: Context, preciseMode: Boolean): Notification {
        ensureChannels(context)
        val launch = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or pendingImmutable()
        )
        val stop = PendingIntent.getService(
            context,
            1,
            Intent(context, MonitorService::class.java).apply {
                action = MonitorService.ACTION_STOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or pendingImmutable()
        )

        val bodyRes = if (preciseMode) {
            R.string.monitor_notification_text_precise
        } else {
            R.string.monitor_notification_text_easy
        }

        return NotificationCompat.Builder(context, CHANNEL_MONITOR_ID)
            .setSmallIcon(R.drawable.ic_stat_shield)
            .setContentTitle(context.getString(R.string.monitor_notification_title))
            .setContentText(context.getString(bodyRes))
            .setContentIntent(launch)
            .setOngoing(true)
            .addAction(0, context.getString(R.string.action_stop_monitor), stop)
            .build()
    }

    fun showHardwareAlert(
        context: Context,
        appName: String,
        hardwareLabel: String,
        notificationId: Int
    ) {
        ensureChannels(context)
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val launch = PendingIntent.getActivity(
            context,
            notificationId,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(MainActivity.EXTRA_OPEN_HISTORY, true)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or pendingImmutable()
        )

        val text = context.getString(R.string.alert_notification_body, appName, hardwareLabel)
        val n = NotificationCompat.Builder(context, CHANNEL_ALERT_ID)
            .setSmallIcon(R.drawable.ic_stat_warning)
            .setContentTitle(context.getString(R.string.alert_notification_title))
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(launch)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        nm.notify(notificationId, n)
    }

    private fun pendingImmutable(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_IMMUTABLE
        } else {
            0
        }
    }
}
