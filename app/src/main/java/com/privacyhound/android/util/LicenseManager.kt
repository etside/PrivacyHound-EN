package com.privacyhound.android.util

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import android.util.Base64

object LicenseManager {
    private const val PREFS_NAME = "premium_license"
    private const val KEY_TIER = "tier"
    private const val KEY_IDENTIFIER = "identifier"
    private const val KEY_EXPIRY = "expiry_timestamp"
    private const val KEY_PASSPHRASE = "passphrase"
    private const val KEY_ACTIVATED_AT = "activated_at"

    private const val SECRET_KEY = "eT_Premium_Secret_2024"

    const val TIER_FREE = "free"
    const val TIER_GOLD = "gold"
    const val TIER_PLATINUM = "platinum"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun generatePassphrase(identifier: String, tier: String, months: Int): String {
        val expiryTimestamp = System.currentTimeMillis() + (months.toLong() * 30L * 24L * 60L * 60L * 1000L)
        val payload = "$identifier|$tier|$expiryTimestamp"
        val hmac = hmacSha256(payload)
        val code = hmac.take(12).uppercase(Locale.US)
        val formatted = buildString {
            append("ETG-")
            for (i in code.indices) {
                if (i > 0 && i % 4 == 0) append("-")
                append(code[i])
            }
        }
        return formatted
    }

    fun verifyPassphrase(context: Context, passphrase: String, identifier: String): Boolean {
        val clean = passphrase.trim().uppercase(Locale.US)
        val parts = clean.split("-").filter { it.isNotEmpty() }
        val code = parts.joinToString("")
        if (code.length != 12) return false

        for (tier in listOf(TIER_PLATINUM, TIER_GOLD)) {
            for (months in 1..24) {
                val expiryTimestamp = System.currentTimeMillis() + (months.toLong() * 30L * 24L * 60L * 60L * 1000L)
                val payload = "$identifier|$tier|$expiryTimestamp"
                val expected = hmacSha256(payload).take(12).uppercase(Locale.US)
                val expectedFormatted = buildString {
                    append("ETG-")
                    for (i in expected.indices) {
                        if (i > 0 && i % 4 == 0) append("-")
                        append(expected[i])
                    }
                }
                if (clean == expectedFormatted) {
                    saveLicense(context, identifier, tier, expiryTimestamp, passphrase)
                    return true
                }
            }
        }
        return false
    }

    private fun hmacSha256(data: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(SECRET_KEY.toByteArray(), "HmacSHA256"))
        val hash = mac.doFinal(data.toByteArray())
        return Base64.encodeToString(hash, Base64.NO_WRAP).take(16)
    }

    private fun saveLicense(context: Context, identifier: String, tier: String, expiry: Long, passphrase: String) {
        prefs(context).edit().apply {
            putString(KEY_IDENTIFIER, identifier)
            putString(KEY_TIER, tier)
            putLong(KEY_EXPIRY, expiry)
            putString(KEY_PASSPHRASE, passphrase)
            putLong(KEY_ACTIVATED_AT, System.currentTimeMillis())
            apply()
        }
    }

    fun getTier(context: Context): String =
        prefs(context).getString(KEY_TIER, TIER_FREE) ?: TIER_FREE

    fun getIdentifier(context: Context): String =
        prefs(context).getString(KEY_IDENTIFIER, "") ?: ""

    fun getExpiry(context: Context): Long =
        prefs(context).getLong(KEY_EXPIRY, 0L)

    fun isExpired(context: Context): Boolean {
        val expiry = getExpiry(context)
        return expiry > 0 && System.currentTimeMillis() > expiry
    }

    fun isActive(context: Context): Boolean {
        val tier = getTier(context)
        return tier != TIER_FREE && !isExpired(context)
    }

    fun getExpiryFormatted(context: Context): String {
        val expiry = getExpiry(context)
        if (expiry == 0L) return "No active license"
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return "Expires: ${sdf.format(Date(expiry))}"
    }

    fun clearLicense(context: Context) {
        prefs(context).edit().clear().apply()
    }

    fun hasFeature(context: Context, feature: String): Boolean {
        val tier = getTier(context)
        if (!isActive(context)) return false
        return when (feature) {
            "camera", "mic" -> true
            "location" -> tier == TIER_GOLD || tier == TIER_PLATINUM
            "contacts", "sms" -> tier == TIER_PLATINUM
            "stats_full" -> tier == TIER_GOLD || tier == TIER_PLATINUM
            "export" -> tier == TIER_PLATINUM
            "unlimited_history" -> tier == TIER_PLATINUM
            "custom_overlay" -> tier == TIER_GOLD || tier == TIER_PLATINUM
            else -> false
        }
    }
}
