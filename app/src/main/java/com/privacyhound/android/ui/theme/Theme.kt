package com.privacyhound.android.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.privacyhound.android.util.PrefsManager

private val PremiumDarkColors = darkColorScheme(
    primary = GoldPrimary,
    onPrimary = PitchBlack,
    primaryContainer = GoldDark,
    onPrimaryContainer = TextWhite,
    secondary = GoldMid,
    onSecondary = PitchBlack,
    secondaryContainer = SurfaceCard,
    onSecondaryContainer = TextWhite,
    background = PitchBlack,
    onBackground = TextWhite,
    surface = Charcoal,
    onSurface = TextWhite,
    surfaceVariant = SurfaceDark,
    onSurfaceVariant = TextMuted,
    error = AlertRed,
    onError = TextWhite,
    outline = GoldSubtle,
    outlineVariant = SurfaceElevated
)

private val PremiumLightColors = lightColorScheme(
    primary = GoldDark,
    onPrimary = TextWhite,
    primaryContainer = Color(0xFFFFF8E1),
    onPrimaryContainer = PitchBlack,
    secondary = GoldMid,
    onSecondary = PitchBlack,
    background = Color(0xFFFAFAFA),
    onBackground = PitchBlack,
    surface = TextWhite,
    onSurface = PitchBlack,
    surfaceVariant = Color(0xFFF5F5F5),
    onSurfaceVariant = Color(0xFF666666),
    outline = GoldMuted
)

@Composable
fun PrivacyHoundTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { PrefsManager.getInstance(context) }
    val darkModePref by prefs.darkModeFlow.collectAsState()

    val systemDark = isSystemInDarkTheme()
    val dark = when (darkModePref) {
        "dark" -> true
        "light" -> false
        else -> systemDark
    }

    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        dark -> PremiumDarkColors
        else -> PremiumLightColors
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = PitchBlack.toArgb()
            window.navigationBarColor = PitchBlack.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = if (dark) PremiumDarkColors else colorScheme,
        typography = Typography,
        content = content
    )
}
