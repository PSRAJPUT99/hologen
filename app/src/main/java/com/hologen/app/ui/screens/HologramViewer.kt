package com.hologen.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.hologen.app.ui.theme.HologenColors

@Composable
fun HologramViewer(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    modelUri: String? = null
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(HologenColors.Background.primary),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(color = HologenColors.Accent.mint)
        } else {
            Text(
                text = "Hologram Stage Ready\n(Scan an object to load 3D model)",
                color = HologenColors.Text.secondary,
                textAlign = TextAlign.Center
            )
        }
    }
}