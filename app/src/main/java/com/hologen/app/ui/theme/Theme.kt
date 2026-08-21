package com.hologen.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object HologenColors {
    object Background {
        val primary = Color(0xFF0B1215)
        val card = Color(0xFF2E3A40)
        val cardSecondary = Color(0xFF243036)
    }

    object Accent {
        val mint = Color(0xFF9FE7C5)
        val glow = Color(0xFFB7F5D8)
    }

    object Text {
        val primary = Color(0xFFFFFFFF)
        val secondary = Color(0xFFAEB9BD)
    }

    object Tile {
        val add = Color(0xFFE6EEF0)
    }

    // Compatibility aliases for existing UI code while screens migrate to semantic tokens.
    val ink = Background.primary
    val canvas = Background.primary
    val surface = Background.card
    val raised = Background.cardSecondary
    val line = Text.secondary.copy(alpha = 0.2f)
    val mint = Accent.mint
    val mintStrong = Accent.mint
    val mintDim = Text.secondary
    val paper = Text.primary
    val quiet = Text.secondary
}

private val DarkColors = darkColorScheme(
    primary = HologenColors.Accent.mint,
    onPrimary = HologenColors.Background.primary,
    secondary = HologenColors.Accent.glow,
    onSecondary = HologenColors.Background.primary,
    background = HologenColors.Background.primary,
    onBackground = HologenColors.Text.primary,
    surface = HologenColors.Background.card,
    onSurface = HologenColors.Text.primary,
    surfaceVariant = HologenColors.Background.cardSecondary,
    onSurfaceVariant = HologenColors.Text.secondary,
    outline = HologenColors.Text.secondary.copy(alpha = 0.35f)
)

@Composable
fun HologenTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        typography = Typography,
        content = content
    )
}
