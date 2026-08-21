package com.hologen.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFF2F6B5E),
    onPrimary = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
    secondary = androidx.compose.ui.graphics.Color(0xFF5B7C74),
    onSecondary = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
    background = androidx.compose.ui.graphics.Color(0xFFF5F4F1),
    onBackground = androidx.compose.ui.graphics.Color(0xFF1A1C1B),
    surface = androidx.compose.ui.graphics.Color(0xFFF9F8F5),
    onSurface = androidx.compose.ui.graphics.Color(0xFF1A1C1B)
)

private val DarkColors = darkColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFF8AD7C2),
    onPrimary = androidx.compose.ui.graphics.Color(0xFF0C1E1A),
    secondary = androidx.compose.ui.graphics.Color(0xFFB9DACE),
    onSecondary = androidx.compose.ui.graphics.Color(0xFF0C1E1A),
    background = androidx.compose.ui.graphics.Color(0xFF101312),
    onBackground = androidx.compose.ui.graphics.Color(0xFFEEF2F0),
    surface = androidx.compose.ui.graphics.Color(0xFF171C1A),
    onSurface = androidx.compose.ui.graphics.Color(0xFFEEF2F0)
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
