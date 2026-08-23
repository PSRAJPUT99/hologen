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
import io.github.sceneview.SceneView
import io.github.sceneview.math.Position
import io.github.sceneview.model.Model

@Composable
fun HologramViewer(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    modelUri: String? = null // Future me real model load karne ke liye
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(HologenColors.Background.primary),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = HologenColors.Accent.mint
            )
        } else if (modelUri != null) {
            // Jab real model aayega, ye SceneView use hoga
            SceneView(
                modifier = Modifier.fillMaxSize(),
                model = Model.createInstance(modelUri), // SceneView 4.x API
                autoRotate = true,
                enablePan = true,
                enableZoom = true,
                environmentLighting = Position(0f, 1f, 0f)
            )
        } else {
            // Placeholder jab tak koi model generate nahi hua
            Text(
                text = "Hologram Stage Ready\nScan an object to begin",
                color = HologenColors.Text.secondary,
                textAlign = TextAlign.Center
            )
        }
    }
}
