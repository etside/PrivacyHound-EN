package com.privacyhound.android.overlay

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.setPadding
import com.privacyhound.android.MainActivity
import com.privacyhound.android.R
import com.privacyhound.android.util.dp

/**
 * 顶部悬浮条提醒，约 5 秒后自动移除。
 */
class AlertOverlayService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private var attached: Pair<WindowManager, android.view.View>? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action != ACTION_SHOW) {
            stopSelf()
            return START_NOT_STICKY
        }

        val appName = intent.getStringExtra(EXTRA_APP_NAME).orEmpty()
        val hw = intent.getStringExtra(EXTRA_HARDWARE).orEmpty()
        val pkg = intent.getStringExtra(EXTRA_PACKAGE).orEmpty()

        handler.post {
            showCard(appName, hw, pkg)
        }

        handler.postDelayed({ stopSelfRemove() }, AUTO_DISMISS_MS)
        return START_NOT_STICKY
    }

    private fun showCard(appName: String, hw: String, pkg: String) {
        removeCard()

        val wm = getSystemService(WINDOW_SERVICE) as WindowManager
        val type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY

        val pad = 16.dp(this)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad)
            setBackgroundResource(R.drawable.bg_overlay_card)
        }

        val title = TextView(this).apply {
            text = getString(R.string.overlay_body, appName, hw)
            textSize = 15f
            setTextColor(getColor(R.color.overlay_text))
        }

        val actions = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.END
        }

        val detail = TextView(this).apply {
            text = getString(R.string.overlay_action_detail)
            textSize = 14f
            setTextColor(getColor(R.color.overlay_action))
            setPadding(0, pad / 2, pad, 0)
            setOnClickListener {
                startActivity(
                    Intent(this@AlertOverlayService, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        putExtra(MainActivity.EXTRA_OPEN_HISTORY, true)
                        putExtra(MainActivity.EXTRA_FOCUS_PACKAGE, pkg)
                    }
                )
                stopSelfRemove()
            }
        }

        val dismiss = TextView(this).apply {
            text = getString(R.string.overlay_action_dismiss)
            textSize = 14f
            setTextColor(getColor(R.color.overlay_muted))
            setPadding(pad, pad / 2, 0, 0)
            setOnClickListener { stopSelfRemove() }
        }

        actions.addView(detail)
        actions.addView(dismiss)
        root.addView(title)
        root.addView(actions)

        val lp = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = 24.dp(this@AlertOverlayService)
        }

        wm.addView(root, lp)
        attached = wm to root
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        removeCard()
        super.onDestroy()
    }

    private fun stopSelfRemove() {
        removeCard()
        stopSelf()
    }

    private fun removeCard() {
        val pair = attached ?: return
        runCatching { pair.first.removeView(pair.second) }
        attached = null
    }

    companion object {
        private const val ACTION_SHOW = "com.privacyhound.android.overlay.SHOW"
        const val EXTRA_APP_NAME = "app_name"
        const val EXTRA_HARDWARE = "hardware"
        const val EXTRA_PACKAGE = "package"

        private const val AUTO_DISMISS_MS = 5_000L

        fun show(context: Context, appName: String, hardwareLabel: String, packageName: String) {
            if (!com.privacyhound.android.util.PermissionUtils.canDrawOverlays(context)) return
            val i = Intent(context, AlertOverlayService::class.java).apply {
                action = ACTION_SHOW
                putExtra(EXTRA_APP_NAME, appName)
                putExtra(EXTRA_HARDWARE, hardwareLabel)
                putExtra(EXTRA_PACKAGE, packageName)
            }
            context.startService(i)
        }
    }
}
