package com.hologen.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.hologen.app.ui.theme.HologenColors

@Composable
fun WorkspaceScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(HologenColors.Background.primary),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Workspace coming soon",
            style = MaterialTheme.typography.bodyLarge,
            color = HologenColors.Text.secondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun HistoryScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(HologenColors.Background.primary),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "History coming soon",
            style = MaterialTheme.typography.bodyLarge,
            color = HologenColors.Text.secondary,
            textAlign = TextAlign.Center
        )
    }
}