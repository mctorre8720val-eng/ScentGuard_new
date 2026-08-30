package com.example.scentguard.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = PremiumGreen,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF1E3A24), // Dark deep green
    onPrimaryContainer = PremiumGreen,
    secondary = Charcoal,
    onSecondary = Color.White,
    background = DarkBackground,
    onBackground = Color.White,
    surface = DarkSurface,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF2C2C2E),
    onSurfaceVariant = Color(0xFFA1A1A6),
    error = ErrorRed,
    outline = Color(0xFF3A3A3C)
)

private val LightColorScheme = lightColorScheme(
    primary = PremiumGreen,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE8F5E9), // Soft Mint
    onPrimaryContainer = Color(0xFF1B5E20),
    secondary = Color(0xFFF1F3F5),
    onSecondary = Color(0xFF1A1C1E),
    background = Color(0xFFF8F9FA),
    onBackground = Color(0xFF1A1C1E),
    surface = Color.White,
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFF2F2F7),
    onSurfaceVariant = Color(0xFF6C757D),
    error = ErrorRed,
    outline = Color(0xFFE5E5EA)
)

@Composable
fun ScentGuardTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
