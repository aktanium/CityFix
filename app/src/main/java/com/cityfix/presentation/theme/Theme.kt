package com.cityfix.presentation.theme

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

// Brand Colors
val PrimaryBlue = Color(0xFF1565C0)
val PrimaryBlueDark = Color(0xFF003C8F)
val SecondaryTeal = Color(0xFF00897B)
val TertiaryAmber = Color(0xFFF57C00)

val StatusNew = Color(0xFF1565C0)
val StatusInProgress = Color(0xFFF57C00)
val StatusResolved = Color(0xFF2E7D32)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6E4FF),
    onPrimaryContainer = Color(0xFF001847),
    secondary = SecondaryTeal,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFB2DFDB),
    onSecondaryContainer = Color(0xFF00201C),
    tertiary = TertiaryAmber,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFE0B2),
    onTertiaryContainer = Color(0xFF341100),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFF8F9FE),
    onBackground = Color(0xFF1A1C1E),
    surface = Color(0xFFF8F9FE),
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFE1E2EC),
    onSurfaceVariant = Color(0xFF44474F),
    outline = Color(0xFF74777F)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFA8C8FF),
    onPrimary = Color(0xFF003063),
    primaryContainer = Color(0xFF00468B),
    onPrimaryContainer = Color(0xFFD6E4FF),
    secondary = Color(0xFF80CBC4),
    onSecondary = Color(0xFF003731),
    secondaryContainer = Color(0xFF005048),
    onSecondaryContainer = Color(0xFFB2DFDB),
    tertiary = Color(0xFFFFCC80),
    onTertiary = Color(0xFF4A1C00),
    tertiaryContainer = Color(0xFF6A2C00),
    onTertiaryContainer = Color(0xFFFFE0B2),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF1A1C1E),
    onBackground = Color(0xFFE3E2E6),
    surface = Color(0xFF1A1C1E),
    onSurface = Color(0xFFE3E2E6),
    surfaceVariant = Color(0xFF44474F),
    onSurfaceVariant = Color(0xFFC4C6D0),
    outline = Color(0xFF8E9099)
)

@Composable
fun CityFixTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = CityFixTypography,
        content = content
    )
}
