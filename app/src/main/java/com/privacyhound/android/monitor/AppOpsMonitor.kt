package com.privacyhound.android.monitor

import android.annotation.SuppressLint
import android.app.AppOpsManager
import android.content.Context
import android.os.Process
import com.privacyhound.android.util.PermissionUtils
import java.lang.reflect.Method
import java.util.concurrent.CopyOnWriteArrayList

interface AppOpsMonitorListener {
    fun onPackageOpChanged(packageName: String, appOpsOp: Int, active: Boolean)
}

/**
 * 封装 [AppOpsManager] 隐藏 API：在持有 [android.Manifest.permission.GET_APP_OPS_STATS] 时监听
 * 摄像头、麦克风、位置、通讯录读取与短信读取等 AppOps。
 */
class AppOpsMonitor(private val context: Context) {

    private val appOps: AppOpsManager =
        context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager

    private val listeners = CopyOnWriteArrayList<AppOpsMonitorListener>()
    private val watcherDelegates = CopyOnWriteArrayList<WatcherDelegate>()

    fun addListener(listener: AppOpsMonitorListener) {
        listeners.addIfAbsent(listener)
    }

    fun removeListener(listener: AppOpsMonitorListener) {
        listeners.remove(listener)
    }

    fun hasPrivilegedAccess(): Boolean = PermissionUtils.hasAppOpsStats(context)

    fun startWatching(): Boolean {
        if (!hasPrivilegedAccess()) return false
        stopWatchingInternal()
        val method = resolveStartWatchingMode() ?: return false
        val ops = HardwareOp.watchOps()

        for (op in ops) {
            val delegate = WatcherDelegate(op)
            watcherDelegates.add(delegate)
            try {
                method.invoke(appOps, op, null, delegate.listener)
            } catch (_: Throwable) {
                try {
                    method.invoke(appOps, op, context.packageName, delegate.listener)
                } catch (_: Throwable) {
                    watcherDelegates.remove(delegate)
                }
            }
        }
        return watcherDelegates.isNotEmpty()
    }

    fun stopWatching() {
        stopWatchingInternal()
    }

    private fun stopWatchingInternal() {
        val stop = resolveStopWatchingMode()
        if (stop != null) {
            for (d in watcherDelegates) {
                try {
                    stop.invoke(appOps, d.listener)
                } catch (_: Throwable) {
                    // ignore
                }
            }
        }
        watcherDelegates.clear()
    }

    private inner class WatcherDelegate(val op: Int) {
        val listener = AppOpsManager.OnOpChangedListener { opStr, pkg ->
            val resolvedPkg = pkg?.takeIf { it.isNotBlank() } ?: return@OnOpChangedListener
            val resolvedOp = HardwareOp.parseOpFromCallback(opStr) ?: op
            val uid = uidForPackage(resolvedPkg) ?: return@OnOpChangedListener
            val mode = checkOpNoThrow(resolvedOp, uid, resolvedPkg)
            dispatch(resolvedPkg, resolvedOp, isActiveMode(mode))
        }
    }

    private fun dispatch(packageName: String, appOpsOp: Int, active: Boolean) {
        for (l in listeners) {
            l.onPackageOpChanged(packageName, appOpsOp, active)
        }
    }

    private fun uidForPackage(packageName: String): Int? =
        runCatching {
            context.packageManager.getApplicationInfo(packageName, 0).uid
        }.getOrNull()

    @SuppressLint("PrivateApi")
    private fun checkOpNoThrow(op: Int, uid: Int, packageName: String): Int {
        return runCatching {
            val m: Method = AppOpsManager::class.java.getMethod(
                "checkOpNoThrow",
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType,
                String::class.java
            )
            m.invoke(appOps, op, uid, packageName) as Int
        }.getOrElse { AppOpsManager.MODE_IGNORED }
    }

    private fun isActiveMode(mode: Int): Boolean {
        return mode == AppOpsManager.MODE_FOREGROUND || mode == AppOpsManager.MODE_ALLOWED
    }

    private fun resolveStartWatchingMode(): Method? =
        runCatching {
            AppOpsManager::class.java.getMethod(
                "startWatchingMode",
                Int::class.javaPrimitiveType,
                String::class.java,
                AppOpsManager.OnOpChangedListener::class.java
            )
        }.getOrNull()

    private fun resolveStopWatchingMode(): Method? =
        runCatching {
            AppOpsManager::class.java.getMethod(
                "stopWatchingMode",
                AppOpsManager.OnOpChangedListener::class.java
            )
        }.getOrNull()
}
