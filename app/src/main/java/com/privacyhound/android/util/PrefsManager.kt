package com.privacyhound.android.util

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PrefsManager(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // ── Monitoring ───────────────────────────────────────────────

    var monitoringEnabled: Boolean
        get() = prefs.getBoolean(KEY_MONITORING_ENABLED, DEFAULT_MONITORING_ENABLED)
        set(value) = prefs.edit().putBoolean(KEY_MONITORING_ENABLED, value).apply()

    var overlayEnabled: Boolean
        get() = prefs.getBoolean(KEY_OVERLAY_ENABLED, DEFAULT_OVERLAY_ENABLED)
        set(value) = prefs.edit().putBoolean(KEY_OVERLAY_ENABLED, value).apply()

    var notificationsEnabled: Boolean
        get() = prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, DEFAULT_NOTIFICATIONS_ENABLED)
        set(value) = prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, value).apply()

    // ── Data ─────────────────────────────────────────────────────

    var pollingIntervalMs: Long
        get() = prefs.getLong(KEY_POLLING_INTERVAL_MS, DEFAULT_POLLING_INTERVAL_MS)
        set(value) = prefs.edit().putLong(KEY_POLLING_INTERVAL_MS, value).apply()

    var dataRetentionDays: Int
        get() = prefs.getInt(KEY_DATA_RETENTION_DAYS, DEFAULT_DATA_RETENTION_DAYS)
        set(value) = prefs.edit().putInt(KEY_DATA_RETENTION_DAYS, value).apply()

    // ── Alerts ───────────────────────────────────────────────────

    var vibrateOnAlert: Boolean
        get() = prefs.getBoolean(KEY_VIBRATE_ON_ALERT, DEFAULT_VIBRATE_ON_ALERT)
        set(value) = prefs.edit().putBoolean(KEY_VIBRATE_ON_ALERT, value).apply()

    var soundOnAlert: Boolean
        get() = prefs.getBoolean(KEY_SOUND_ON_ALERT, DEFAULT_SOUND_ON_ALERT)
        set(value) = prefs.edit().putBoolean(KEY_SOUND_ON_ALERT, value).apply()

    var showOverlayOnAlert: Boolean
        get() = prefs.getBoolean(KEY_SHOW_OVERLAY_ON_ALERT, DEFAULT_SHOW_OVERLAY_ON_ALERT)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_OVERLAY_ON_ALERT, value).apply()

    // ── Appearance ───────────────────────────────────────────────

    var darkMode: String
        get() = prefs.getString(KEY_DARK_MODE, DEFAULT_DARK_MODE) ?: DEFAULT_DARK_MODE
        set(value) {
            prefs.edit().putString(KEY_DARK_MODE, value).apply()
            _darkModeFlow.value = value
        }

    private val _darkModeFlow = MutableStateFlow(darkMode)
    val darkModeFlow: StateFlow<String> = _darkModeFlow

    // ── Helpers ──────────────────────────────────────────────────

    fun clearAll() {
        prefs.edit().clear().apply()
        _darkModeFlow.value = DEFAULT_DARK_MODE
    }

    companion object {
        private const val PREFS_NAME = "privacy_hound_prefs"

        private const val KEY_MONITORING_ENABLED = "monitoring_enabled"
        private const val KEY_OVERLAY_ENABLED = "overlay_enabled"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_POLLING_INTERVAL_MS = "polling_interval_ms"
        private const val KEY_DATA_RETENTION_DAYS = "data_retention_days"
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_VIBRATE_ON_ALERT = "vibrate_on_alert"
        private const val KEY_SOUND_ON_ALERT = "sound_on_alert"
        private const val KEY_SHOW_OVERLAY_ON_ALERT = "show_overlay_on_alert"

        private const val DEFAULT_MONITORING_ENABLED = true
        private const val DEFAULT_OVERLAY_ENABLED = true
        private const val DEFAULT_NOTIFICATIONS_ENABLED = true
        private const val DEFAULT_POLLING_INTERVAL_MS = 30000L
        private const val DEFAULT_DATA_RETENTION_DAYS = 30
        private const val DEFAULT_DARK_MODE = "system"
        private const val DEFAULT_VIBRATE_ON_ALERT = true
        private const val DEFAULT_SOUND_ON_ALERT = true
        private const val DEFAULT_SHOW_OVERLAY_ON_ALERT = true

        @Volatile
        private var INSTANCE: PrefsManager? = null

        fun getInstance(context: Context): PrefsManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PrefsManager(context).also { INSTANCE = it }
            }
        }
    }
}
