package com.hologen.app.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import io.github.sceneview.SceneView
import io.github.sceneview.gesture.rememberCameraManipulator
import io.github.sceneview.rememberMaterialLoader
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberModelInstance
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.math.Size
import io.github.sceneview.rememberEngine
import com.hologen.app.ui.theme.HologenColors
import com.hologen.app.ui.theme.HologenMetrics

class HologramViewerController {
    var modelUrl by mutableStateOf<String?>(null)
        private set
    var autoRotate by mutableStateOf(true)
        private set
    var resetVersion by mutableIntStateOf(0)
        private set

    fun loadModel(url: String) {
        modelUrl = url
    }

    fun resetCamera() {
        resetVersion += 1
    }

    fun setRotation(autoRotate: Boolean) {
        this.autoRotate = autoRotate
    }
}

@Composable
fun rememberHologramViewerController(): HologramViewerController = remember { HologramViewerController() }

@Composable
fun HologramViewer(
    modifier: Modifier = Modifier,
    controller: HologramViewerController = rememberHologramViewerController(),
    isLoading: Boolean = false
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(HologenMetrics.space12)
            .clip(RoundedCornerShape(HologenMetrics.cardRadius))
            .background(HologramGlow)
            .border(
                HologenMetrics.thinBorder,
                HologenColors.Background.card,
                RoundedCornerShape(HologenMetrics.cardRadius)
            ),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.runtime.key(controller.resetVersion) {
            SceneContent(controller)
        }
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = HologenColors.Accent.mint
            )
        }
        IconButton(
            onClick = controller::resetCamera,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(HologenMetrics.space12)
                .clip(CircleShape)
                .background(HologenColors.Background.card)
        ) {
            Icon(Icons.Outlined.Refresh, contentDescription = "Reset camera", tint = HologenColors.Accent.mint)
        }
    }
}

@Composable
private fun SceneContent(controller: HologramViewerController) {
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val materialLoader = rememberMaterialLoader(engine)
    val cameraManipulator = rememberCameraManipulator(
        orbitHomePosition = Position(z = 2.6f),
        targetPosition = Position()
    )
    val modelMaterial = remember(materialLoader) {
        materialLoader.createColorInstance(
            color = HologenColors.Accent.mint.copy(alpha = 0.7f),
            metallic = 0.05f,
            roughness = 0.82f
        )
    }
    val rotation by rememberInfiniteTransition(label = "hologram rotation").animateFloat(
        initialValue = 0f,
        targetValue = if (controller.autoRotate) 360f else 0f,
        animationSpec = infiniteRepeatable(tween(12000, easing = LinearEasing), RepeatMode.Restart),
        label = "hologram rotation"
    )
    val modelInstance = controller.modelUrl?.let { rememberModelInstance(modelLoader, it) }

    SceneView(
        modifier = Modifier.fillMaxSize(),
        engine = engine,
        modelLoader = modelLoader,
        cameraManipulator = cameraManipulator,
        isOpaque = true
    ) {
        if (modelInstance != null) {
            ModelNode(
                modelInstance = modelInstance,
                scaleToUnits = 1.0f,
                autoAnimate = true,
                rotation = Rotation(y = rotation)
            )
        } else {
            CubeNode(
                size = Size(0.9f),
                materialInstance = modelMaterial,
                rotation = Rotation(y = rotation)
            )
        }
    }
}

private val HologramGlow = HologenColors.Accent.glow.copy(alpha = 0.08f)