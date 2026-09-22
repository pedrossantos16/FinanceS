package com.pedro.finances.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BlackYellowColorScheme = darkColorScheme(
    primary = YellowPrimary,
    onPrimary = Color.Black,
    primaryContainer = YellowVariant,
    onPrimaryContainer = Color.Black,
    secondary = YellowPrimary,
    onSecondary = Color.Black,
    background = Color.Black,
    onBackground = TextWhite,
    surface = DarkSurface,
    onSurface = TextWhite,
    surfaceVariant = CardBackground,
    onSurfaceVariant = TextGray
)

@Composable
fun FinanceSTheme(
    darkTheme: Boolean = true, // Always dark/black theme as requested
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = BlackYellowColorScheme,
        typography = Typography,
        content = content
    )
}
