package com.privacyhound.android.util

import android.content.Context
import android.content.pm.PackageManager

object AppNames {

    fun labelForPackage(context: Context, packageName: String): String {
        return runCatching {
            val pm = context.packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        }.getOrDefault(packageName)
    }
}
