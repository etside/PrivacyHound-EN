package com.privacyhound.android.monitor

/** 简易模式下无法解析调用方包名时使用 */
const val UNKNOWN_CALLER_PACKAGE = "com.privacyhound.public_unknown"

/** 隐私报告等处：仅当能确定是「其他应用」的包名时才展示，避免误显示本应用或占位包名 */
fun shouldShowCallerPackageInReports(storedPackage: String, selfPackage: String): Boolean {
    if (storedPackage.isBlank()) return false
    if (storedPackage == UNKNOWN_CALLER_PACKAGE) return false
    if (storedPackage == selfPackage) return false
    return true
}
