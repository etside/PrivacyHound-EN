package com.privacyhound.android.util

import android.app.ActivityManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Build

object ForegroundChecker {

    fun isForeground(context: Context, packageName: String): Boolean {
        if (PermissionUtils.hasUsageStatsPermission(context)) {
            val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val end = System.currentTimeMillis()
            val windowMs = 30_000L
            val usageEvents = usm.queryEvents(end - windowMs, end)
            val ev = UsageEvents.Event()
            var lastFg: String? = null
            while (usageEvents.hasNextEvent()) {
                usageEvents.getNextEvent(ev)
                if (ev.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                    lastFg = ev.packageName
                }
            }
            if (lastFg == packageName) return true
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return false
        }

        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        @Suppress("DEPRECATION")
        val procs = am.runningAppProcesses ?: return false
        for (p in procs) {
            if (p.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
                p.pkgList?.contains(packageName) == true
            ) {
                return true
            }
        }
        return false
    }
}
