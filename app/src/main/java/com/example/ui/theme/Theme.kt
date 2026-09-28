package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = DanaGold,
    onPrimary = Color(0xFF1B1B1B),
    primaryContainer = Color(0xFF332900),
    onPrimaryContainer = DanaGoldLight,
    secondary = AccentCyan,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF003644),
    onSecondaryContainer = Color(0xFFBCE9F5),
    tertiary = BullGreen,
    onTertiary = Color.Black,
    error = BearRed,
    onError = Color.White,
    background = DanaBgDark,
    onBackground = TextPrimaryDark,
    surface = DanaSurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = DanaSurfaceElevatedDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = DanaBorderDark
)

private val LightColorScheme = lightColorScheme(
    primary = DanaGoldDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFF1C2),
    onPrimaryContainer = Color(0xFF261A00),
    secondary = Color(0xFF0277BD),
    onSecondary = Color.White,
    tertiary = Color(0xFF008955),
    onTertiary = Color.White,
    error = Color(0xFFD32F2F),
    onError = Color.White,
    background = Color(0xFFF6F8FA),
    onBackground = Color(0xFF12161E),
    surface = Color.White,
    onSurface = Color(0xFF12161E),
    surfaceVariant = Color(0xFFEDF2F7),
    onSurfaceVariant = Color(0xFF5A6679),
    outline = Color(0xFFD4DDE8)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // BTC Dana default is professional dark trading theme
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
