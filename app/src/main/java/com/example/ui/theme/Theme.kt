package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = SageGreen80,
    secondary = SageGreenGrey80,
    tertiary = PeachRose80,
    background = SandSandDark,
    surface = SlateCardDark,
    onPrimary = Color(0xFF1E3524),
    onSecondary = Color(0xFF2C322F),
    onTertiary = Color(0xFF432A23),
    onBackground = Color(0xFFE2E3E0),
    onSurface = Color(0xFFE2E3E0),
    surfaceVariant = Color(0xFF2D312E)
)

private val LightColorScheme = lightColorScheme(
    primary = SageGreen40,
    secondary = SageGreenGrey40,
    tertiary = PeachRose40,
    background = SandSandLight,
    surface = SlateCardLight,
    onPrimary = Color(0xFFFFFFFF),
    onSecondary = Color(0xFFFFFFFF),
    onTertiary = Color(0xFFFFFFFF),
    onBackground = Color(0xFF1A1C1A),
    onSurface = Color(0xFF1A1C1A),
    surfaceVariant = Color(0xFFE2E4E1)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set to false to preserve organic therapeutic colors
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

