package com.privacyhound.android.util

import android.app.ActivityManager
import android.content.Context

object ServiceRunning {

    @Suppress("DEPRECATION")
    fun isRunning(context: Context, serviceClass: Class<*>): Boolean {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return am.getRunningServices(64).any { it.service.className == serviceClass.name }
    }
}
