package com.privacyhound.android.monitor

import android.app.AppOpsManager

/** 与 [com.privacyhound.android.data.PrivacyEvent.opType] 对齐 */
object HardwareOp {
    const val TYPE_CAMERA = 0
    const val TYPE_MIC = 1
    const val TYPE_GPS = 2
    const val TYPE_CONTACTS_READ = 3
    const val TYPE_SMS_READ = 4

    /** AppOps 回调中的 op 字符串（部分机型 SDK 未暴露常量，与 AOSP 保持一致） */
    private const val OPSTR_READ_CONTACTS = "android:read_contacts"
    private const val OPSTR_READ_SMS = "android:read_sms"

    fun cameraOp(): Int =
        runCatching { AppOpsManager::class.java.getField("OP_CAMERA").getInt(null) }.getOrDefault(26)

    fun micOp(): Int =
        runCatching { AppOpsManager::class.java.getField("OP_RECORD_AUDIO").getInt(null) }.getOrDefault(27)

    fun fineLocOp(): Int =
        runCatching { AppOpsManager::class.java.getField("OP_FINE_LOCATION").getInt(null) }.getOrDefault(1)

    fun coarseLocOp(): Int =
        runCatching { AppOpsManager::class.java.getField("OP_COARSE_LOCATION").getInt(null) }.getOrDefault(0)

    fun readContactsOp(): Int =
        runCatching { AppOpsManager::class.java.getField("OP_READ_CONTACTS").getInt(null) }.getOrDefault(4)

    fun readSmsOp(): Int =
        runCatching { AppOpsManager::class.java.getField("OP_READ_SMS").getInt(null) }.getOrDefault(14)

    fun watchOps(): IntArray = intArrayOf(
        cameraOp(),
        micOp(),
        fineLocOp(),
        coarseLocOp(),
        readContactsOp(),
        readSmsOp()
    )

    fun typeForAppOpsOp(op: Int): Int? = when (op) {
        cameraOp() -> TYPE_CAMERA
        micOp() -> TYPE_MIC
        fineLocOp(), coarseLocOp() -> TYPE_GPS
        readContactsOp() -> TYPE_CONTACTS_READ
        readSmsOp() -> TYPE_SMS_READ
        else -> null
    }

    fun labelForType(type: Int): String = when (type) {
        TYPE_MIC -> "麦克风"
        TYPE_GPS -> "位置"
        TYPE_CONTACTS_READ -> "通讯录读取"
        TYPE_SMS_READ -> "短信读取"
        else -> "摄像头"
    }

    fun parseOpFromCallback(opStr: String?): Int? {
        if (opStr.isNullOrEmpty()) return null
        return when (opStr) {
            AppOpsManager.OPSTR_CAMERA -> cameraOp()
            AppOpsManager.OPSTR_RECORD_AUDIO -> micOp()
            AppOpsManager.OPSTR_FINE_LOCATION -> fineLocOp()
            AppOpsManager.OPSTR_COARSE_LOCATION -> coarseLocOp()
            OPSTR_READ_CONTACTS -> readContactsOp()
            OPSTR_READ_SMS -> readSmsOp()
            else -> null
        }
    }
}
