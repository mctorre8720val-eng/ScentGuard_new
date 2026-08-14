package com.example.scentguard.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = DarkGreenAccent,
    onPrimary = SurfaceWhite,
    background = DarkBackground,
    onBackground = DarkText,
    surface = DarkSurface,
    onSurface = DarkText,
    surfaceVariant = DarkSurface,
    onSurfaceVariant = SubtitleText,
    error = ErrorRed,
    outline = DarkSurface
)

private val LightColorScheme = lightColorScheme(
    primary = PremiumGreen,
    onPrimary = SurfaceWhite,
    primaryContainer = GreenSoft,
    onPrimaryContainer = PremiumGreen,
    secondary = SecondaryAction,
    onSecondary = NeutralText,
    background = BaseGray,
    onBackground = NeutralText,
    surface = SurfaceWhite,
    onSurface = NeutralText,
    surfaceVariant = BaseGray,
    onSurfaceVariant = SubtitleText,
    error = ErrorRed,
    outline = SecondaryAction
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
