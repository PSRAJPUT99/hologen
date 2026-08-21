package com.hologen.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.hologen.app.ui.theme.HologenColors
import com.hologen.app.ui.theme.HologenMetrics

@Composable
fun WorkspaceScreen(modifier: Modifier = Modifier) {
    PlaceholderScreen(modifier, "Projects grid coming soon")
}

@Composable
fun HistoryScreen(modifier: Modifier = Modifier) {
    PlaceholderScreen(modifier, "Recent scans timeline coming soon")
}

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    PlaceholderScreen(modifier, "Preferences coming soon")
}

@Composable
private fun PlaceholderScreen(modifier: Modifier, message: String) {
    Column(
        modifier = modifier.fillMaxSize().padding(HologenMetrics.space24),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = HologenColors.Text.secondary
        )
    }
}
