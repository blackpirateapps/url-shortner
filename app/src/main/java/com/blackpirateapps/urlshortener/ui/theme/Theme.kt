package com.blackpirateapps.urlshortener.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = IOSBlue,
    onPrimary = IOSSecondaryBackground,
    primaryContainer = IOSBlue.copy(alpha = 0.12f),
    onPrimaryContainer = IOSBlue,
    secondary = IOSGreen,
    onSecondary = IOSSecondaryBackground,
    background = IOSBackground,
    onBackground = IOSLabel,
    surface = IOSCardBackground,
    onSurface = IOSLabel,
    surfaceVariant = IOSGroupedBackground,
    onSurfaceVariant = IOSSecondaryLabel,
    outline = IOSSeparator,
    error = IOSRed,
    onError = IOSSecondaryBackground,
    tertiary = IOSOrange,
    onTertiary = IOSSecondaryBackground,
)

private val DarkColorScheme = darkColorScheme(
    primary = IOSBlueDark,
    onPrimary = IOSSecondaryBackgroundDark,
    primaryContainer = IOSBlueDark.copy(alpha = 0.18f),
    onPrimaryContainer = IOSBlueDark,
    secondary = IOSGreen,
    onSecondary = IOSSecondaryBackgroundDark,
    background = IOSBackgroundDark,
    onBackground = IOSLabelDark,
    surface = IOSCardBackgroundDark,
    onSurface = IOSLabelDark,
    surfaceVariant = IOSGroupedBackgroundDark,
    onSurfaceVariant = IOSSecondaryLabelDark,
    outline = IOSSeparatorDark,
    error = IOSRed,
    onError = IOSSecondaryBackgroundDark,
    tertiary = IOSOrange,
    onTertiary = IOSSecondaryBackgroundDark,
)

@Composable
fun BlackPirateUrlShortenerTheme(
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
        typography = IOSTypography,
        content = content
    )
}
