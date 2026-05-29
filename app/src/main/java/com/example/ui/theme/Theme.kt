package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF818CF8), // Indigo 400
    secondary = Color(0xFF1E293B), // Slate 800
    tertiary = BoldRose500,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = Color.Black,
    onSecondary = Color(0xFFC7D2FE),
    onTertiary = Color.White,
    onBackground = Color(0xFFF8FAFC),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFFE2E8F0)
)

private val LightColorScheme = lightColorScheme(
    primary = BoldIndigo600,
    secondary = BoldIndigo50,
    tertiary = BoldRose500,
    background = LightBackground,
    surface = LightSurface,
    onPrimary = Color.White,
    onSecondary = BoldIndigo600,
    onTertiary = Color.White,
    onBackground = BoldSlate900,
    onSurface = BoldSlate900,
    surfaceVariant = BoldIndigo50,
    onSurfaceVariant = BoldIndigo600
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set to false to force our beautiful handcrafted Khata themes
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
