package com.example.absensisiswa.ui.theme

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

private val LightColorScheme = lightColorScheme(
    primary = BrandGreen,
    onPrimary = Color.White,
    primaryContainer = BrandGreenContainer,
    onPrimaryContainer = BrandGreenDark,
    secondary = BrandMagenta,
    onSecondary = Color.White,
    secondaryContainer = BrandMagentaContainer,
    onSecondaryContainer = BrandMagentaDark,
    tertiary = BrandAccent,
    onTertiary = Color.White,
    background = BrandBackground,
    onBackground = BrandText,
    surface = BrandSurface,
    onSurface = BrandText,
    surfaceVariant = BrandSurfaceVariant,
    onSurfaceVariant = BrandTextSecondary,
    outline = BrandBorder,
    outlineVariant = BrandBorder,
    error = BrandError,
    onError = Color.White,
    errorContainer = BrandErrorBg,
    onErrorContainer = BrandError
)

private val DarkColorScheme = darkColorScheme(
    primary = BrandGreenLight,
    onPrimary = Color.White,
    primaryContainer = BrandGreenDark,
    onPrimaryContainer = BrandGreenContainer,
    secondary = BrandMagentaLight,
    onSecondary = Color.White,
    secondaryContainer = BrandMagentaDark,
    onSecondaryContainer = BrandMagentaContainer,
    tertiary = BrandAccentLight,
    onTertiary = Color.Black,
    background = Color(0xFF0F172A),
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF1E293B),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0xFF475569),
    error = Color(0xFFEF4444),
    onError = Color.White
)

@Composable
fun AbsensiSiswaQRTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = BrandGreenDark.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
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
