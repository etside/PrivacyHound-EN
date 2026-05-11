package com.privacyhound.android.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.content.ContextCompat
import com.privacyhound.android.R
import com.privacyhound.android.XiaotianquanApp
import com.privacyhound.android.data.PrivacyEvent
import com.privacyhound.android.monitor.AppOpsMonitor
import com.privacyhound.android.monitor.AppOpsMonitorListener
import com.privacyhound.android.monitor.HardwareOp
import com.privacyhound.android.monitor.UNKNOWN_CALLER_PACKAGE
import com.privacyhound.android.monitor.PublicApiHardwareMonitor
import com.privacyhound.android.notification.NotificationHelper
import com.privacyhound.android.overlay.AlertOverlayService
import com.privacyhound.android.util.AppNames
import com.privacyhound.android.util.ForegroundChecker
import com.privacyhound.android.util.PermissionUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

class MonitorService : Service(), AppOpsMonitorListener {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(job + Dispatchers.Main.immediate)

    private lateinit var appOpsMonitor: AppOpsMonitor
    private var publicMonitor: PublicApiHardwareMonitor? = null

    /** 是否走 AppOps 精确监听（位置、通讯录与短信读取等）；失败或未授权时为 false，仅公开 API */
    private var useAppOps = false
    private val activeRows = ConcurrentHashMap<Key, Long>()

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.ensureChannels(this)
        appOpsMonitor = AppOpsMonitor(this)
        appOpsMonitor.addListener(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopMonitoring()
                stopSelf()
                return START_NOT_STICKY
            }
        }

        stopMonitoringInternals()

        val hasPrivilege = PermissionUtils.hasAppOpsStats(this)
        useAppOps = false
        var notificationShowsPrecise = false

        if (hasPrivilege) {
            val ok = appOpsMonitor.startWatching()
            if (ok) {
                useAppOps = true
                notificationShowsPrecise = true
            }
        }

        if (!useAppOps) {
            publicMonitor = PublicApiHardwareMonitor(this) { type, active, pkg ->
                onPublicHardwareEvent(type, active, pkg)
            }.also { it.start() }
        }

        val notification = NotificationHelper.buildMonitorNotification(this, notificationShowsPrecise)
        startForeground(NotificationHelper.NOTIFICATION_ID_MONITOR, notification)

        return START_STICKY
    }

    override fun onDestroy() {
        appOpsMonitor.removeListener(this)
        stopMonitoringInternals()
        job.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onPackageOpChanged(packageName: String, appOpsOp: Int, active: Boolean) {
        if (!useAppOps) return
        if (packageName == this.packageName) return

        val type = HardwareOp.typeForAppOpsOp(appOpsOp) ?: return
        handleTransition(packageName, type, active)
    }

    private fun onPublicHardwareEvent(type: Int, active: Boolean, packageName: String?) {
        val pkg = packageName ?: UNKNOWN_CALLER_PACKAGE
        handleTransition(pkg, type, active)
    }

    private fun handleTransition(packageName: String, type: Int, active: Boolean) {
        if (packageName == this.packageName) return

        val key = Key(packageName, type)
        val app = application as XiaotianquanApp

        scope.launch(Dispatchers.Default) {
            val repo = app.repository

            if (active) {
                if (activeRows.containsKey(key)) return@launch

                val appLabel = resolveAppLabel(packageName, type)
                val fg = if (packageName == UNKNOWN_CALLER_PACKAGE) {
                    false
                } else {
                    ForegroundChecker.isForeground(this@MonitorService, packageName)
                }

                val id = repo.insert(
                    PrivacyEvent(
                        packageName = packageName,
                        appName = appLabel,
                        opType = type,
                        startTime = System.currentTimeMillis(),
                        endTime = null,
                        isForeground = fg
                    )
                )
                activeRows[key] = id

                withContext(Dispatchers.Main) {
                    maybeAlert(appLabel, type, packageName)
                }
            } else {
                val id = activeRows.remove(key) ?: return@launch
                val row = repo.getById(id) ?: return@launch
                repo.update(row.copy(endTime = System.currentTimeMillis()))
            }
        }
    }

    private fun resolveAppLabel(packageName: String, opType: Int): String {
        if (packageName != UNKNOWN_CALLER_PACKAGE) {
            return AppNames.labelForPackage(this, packageName)
        }
        return when (opType) {
            HardwareOp.TYPE_MIC -> getString(R.string.public_unknown_mic)
            else -> getString(R.string.public_unknown_app)
        }
    }

    private fun maybeAlert(appLabel: String, type: Int, packageName: String) {
        val hw = hardwareLabelRes(type)
        val nid = (packageName.hashCode() xor type) and 0x7fff_ffff
        NotificationHelper.showHardwareAlert(this, appLabel, hw, nid)
        AlertOverlayService.show(this, appLabel, hw, packageName)
    }

    private fun hardwareLabelRes(type: Int): String = when (type) {
        HardwareOp.TYPE_MIC -> getString(R.string.hardware_mic)
        HardwareOp.TYPE_GPS -> getString(R.string.hardware_gps)
        HardwareOp.TYPE_CONTACTS_READ -> getString(R.string.hardware_contacts_read)
        HardwareOp.TYPE_SMS_READ -> getString(R.string.hardware_sms_read)
        else -> getString(R.string.hardware_camera)
    }

    private fun stopMonitoring() {
        stopMonitoringInternals()
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    private fun stopMonitoringInternals() {
        appOpsMonitor.stopWatching()
        publicMonitor?.stop()
        publicMonitor = null
        useAppOps = false
        val app = application as? XiaotianquanApp
        val now = System.currentTimeMillis()
        if (app != null) {
            runBlocking(Dispatchers.IO) {
                runCatching { app.repository.closeAllActiveAt(now) }
            }
        }
        activeRows.clear()
    }

    private data class Key(val packageName: String, val type: Int)

    companion object {
        const val ACTION_STOP = "com.privacyhound.android.action.STOP_MONITOR"

        fun start(context: Context) {
            ContextCompat.startForegroundService(
                context,
                Intent(context, MonitorService::class.java)
            )
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, MonitorService::class.java))
        }
    }
}
