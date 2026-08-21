package com.hologen.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object HologenColors {
    val ink = Color(0xFF0D1110)
    val canvas = Color(0xFF111615)
    val surface = Color(0xFF18201E)
    val raised = Color(0xFF202A27)
    val line = Color(0xFF2A3531)
    val mint = Color(0xFFA7D8C6)
    val mintStrong = Color(0xFF79BDA7)
    val mintDim = Color(0xFF6D9588)
    val paper = Color(0xFFE8F0EC)
    val quiet = Color(0xFF9AA9A3)
    val lightPrimary = Color(0xFF2F6B5E)
    val lightSecondary = Color(0xFF5B7C74)
    val lightCanvas = Color(0xFFF5F4F1)
    val lightPaper = Color(0xFF1A1C1B)
    val lightSurface = Color(0xFFF9F8F5)
}

private val LightColors = lightColorScheme(
    primary = HologenColors.lightPrimary,
    onPrimary = Color.White,
    secondary = HologenColors.lightSecondary,
    onSecondary = Color.White,
    background = HologenColors.lightCanvas,
    onBackground = HologenColors.lightPaper,
    surface = HologenColors.lightSurface,
    onSurface = HologenColors.lightPaper
)

private val DarkColors = darkColorScheme(
    primary = HologenColors.mint,
    onPrimary = HologenColors.ink,
    secondary = HologenColors.mintDim,
    onSecondary = HologenColors.ink,
    background = HologenColors.canvas,
    onBackground = HologenColors.paper,
    surface = HologenColors.surface,
    onSurface = HologenColors.paper,
    surfaceVariant = HologenColors.raised,
    onSurfaceVariant = HologenColors.quiet,
    outline = HologenColors.line
)

@Composable
fun HologenTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
