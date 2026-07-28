package com.privacyhound.android.monitor

/** Used when the caller package cannot be resolved in easy mode */
const val UNKNOWN_CALLER_PACKAGE = "com.privacyhound.public_unknown"

/** Privacy report etc.: only show when we can confirm it's another app's package, to avoid displaying this app or placeholder packages */
fun shouldShowCallerPackageInReports(storedPackage: String, selfPackage: String): Boolean {
    if (storedPackage.isBlank()) return false
    if (storedPackage == UNKNOWN_CALLER_PACKAGE) return false
    if (storedPackage == selfPackage) return false
    return true
}
