package com.privacyhound.android.monitor

import android.content.Context
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.media.AudioRecordingConfiguration
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Process
import java.lang.reflect.Method

/**
 * 无需 root / ADB：摄像头通过 [CameraManager] 可用性；麦克风通过 [AudioManager] 录制会话。
 * 摄像头无法识别具体 App；麦克风在 Android 10+ 多数机型可通过 clientUid 解析包名。
 * 位置占用无可靠公开 API，简易模式下不监测位置（精确模式仍走 AppOps）。
 */
class PublicApiHardwareMonitor(
    private val context: Context,
    private val listener: Listener
) {

    fun interface Listener {
        fun onHardwareEvent(type: Int, active: Boolean, packageName: String?)
    }

    private val mainHandler = Handler(Looper.getMainLooper())
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private var cameraRefCount = 0
    private var micActive = false
    private var lastMicPackage: String? = null

    private val cameraCallback = object : CameraManager.AvailabilityCallback() {
        override fun onCameraUnavailable(cameraId: String) {
            if (cameraRefCount++ == 0) {
                listener.onHardwareEvent(HardwareOp.TYPE_CAMERA, true, null)
            }
        }

        override fun onCameraAvailable(cameraId: String) {
            cameraRefCount = (cameraRefCount - 1).coerceAtLeast(0)
            if (cameraRefCount == 0) {
                listener.onHardwareEvent(HardwareOp.TYPE_CAMERA, false, null)
            }
        }
    }

    private val pollMic = object : Runnable {
        override fun run() {
            applyMicConfigs(audioManager.activeRecordingConfigurations ?: emptyList())
            mainHandler.postDelayed(this, POLL_MS)
        }
    }

    private val audioRecordingCallback = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        object : AudioManager.AudioRecordingCallback() {
            override fun onRecordingConfigChanged(configs: MutableList<AudioRecordingConfiguration>) {
                applyMicConfigs(configs)
            }
        }
    } else {
        null
    }

    fun start() {
        cameraManager.registerAvailabilityCallback(cameraCallback, Handler(Looper.getMainLooper()))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && audioRecordingCallback != null) {
            audioManager.registerAudioRecordingCallback(audioRecordingCallback, mainHandler)
        }

        // 部分机型 System 回调不及时，轮询作为补充（与回调并行，逻辑幂等）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mainHandler.removeCallbacks(pollMic)
            mainHandler.post(pollMic)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            applyMicConfigs(audioManager.activeRecordingConfigurations ?: emptyList())
        }
    }

    fun stop() {
        cameraManager.unregisterAvailabilityCallback(cameraCallback)
        cameraRefCount = 0

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && audioRecordingCallback != null) {
            audioManager.unregisterAudioRecordingCallback(audioRecordingCallback)
        }
        mainHandler.removeCallbacks(pollMic)

        if (micActive) {
            val ended = lastMicPackage
            micActive = false
            lastMicPackage = null
            listener.onHardwareEvent(HardwareOp.TYPE_MIC, false, ended)
        }
    }

    private fun applyMicConfigs(configs: List<AudioRecordingConfiguration>) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return

        val myUid = Process.myUid()
        val others = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // null UID：系统未暴露 clientUid 时仍提示「有录制」（未知应用），避免微信/录音机等无任何提示。
            // 明确为本 UID 的会话排除。
            configs.filter { c ->
                val uid = recordingClientUidReflect(c)
                uid == null || uid != myUid
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configs
        } else {
            emptyList()
        }

        val pkg = resolveMicPackage(others)
        val active = others.isNotEmpty()

        when {
            active && !micActive -> {
                micActive = true
                lastMicPackage = pkg
                listener.onHardwareEvent(HardwareOp.TYPE_MIC, true, pkg)
            }
            !active && micActive -> {
                val ended = lastMicPackage
                micActive = false
                lastMicPackage = null
                listener.onHardwareEvent(HardwareOp.TYPE_MIC, false, ended)
            }
            active && micActive && pkg != lastMicPackage -> {
                listener.onHardwareEvent(HardwareOp.TYPE_MIC, false, lastMicPackage)
                lastMicPackage = pkg
                listener.onHardwareEvent(HardwareOp.TYPE_MIC, true, pkg)
            }
        }
    }

    private fun resolveMicPackage(configs: List<AudioRecordingConfiguration>): String? {
        if (configs.isEmpty()) return null
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return null
        val myUid = Process.myUid()
        val selfPkg = context.packageName
        for (c in configs) {
            val uid = recordingClientUidReflect(c) ?: continue
            if (uid == myUid) continue
            val pkgs = context.packageManager.getPackagesForUid(uid) ?: continue
            pkgs.firstOrNull { it != selfPkg }?.let { return it }
        }
        return null
    }

    companion object {
        private const val POLL_MS = 900L

        @Volatile
        private var clientUidMethod: Method? = null

        private fun resolveClientUidMethod(): Method? {
            val cls = AudioRecordingConfiguration::class.java
            return runCatching { cls.getMethod("getClientUid") }.getOrNull()
                ?: runCatching {
                    cls.getDeclaredMethod("getClientUid").apply { isAccessible = true }
                }.getOrNull()
        }

        private fun recordingClientUidReflect(config: AudioRecordingConfiguration): Int? {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return null
            val m = clientUidMethod ?: resolveClientUidMethod()?.also { clientUidMethod = it }
                ?: return null
            return try {
                m.invoke(config) as Int
            } catch (_: Throwable) {
                null
            }
        }
    }
}
