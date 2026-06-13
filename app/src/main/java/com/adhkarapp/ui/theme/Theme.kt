package com.adhkarapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = OliveGreen,
    onPrimary = White,
    primaryContainer = OliveGreenLight,
    onPrimaryContainer = OliveGreenDark,
    secondary = Gold,
    onSecondary = White,
    secondaryContainer = GoldLight,
    onSecondaryContainer = NearBlack,
    background = Cream,
    onBackground = NearBlack,
    surface = Cream,
    onSurface = NearBlack,
    surfaceVariant = OliveGreenLight,
    onSurfaceVariant = DarkGray,
    outline = LightGray,
    error = Red,
    onError = White
)

@Composable
fun AdhkarTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
