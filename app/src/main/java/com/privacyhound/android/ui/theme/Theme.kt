package com.privacyhound.android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF1565C0),
    onPrimary = Color.White,
    secondary = Color(0xFF00838F),
    background = Color(0xFFF7F9FC),
    surface = Color.White,
    onSurface = Color(0xFF1B1B1F)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF90CAF9),
    onPrimary = Color(0xFF0D2F63),
    secondary = Color(0xFF4DD0E1),
    background = Color(0xFF101418),
    surface = Color(0xFF1A2027),
    onSurface = Color(0xFFE6EAF0)
)

@Composable
fun PrivacyHoundTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        typography = Typography,
        content = content
    )
}
