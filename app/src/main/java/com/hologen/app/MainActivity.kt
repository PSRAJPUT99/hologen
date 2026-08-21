package com.hologen.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.RotateRight
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hologen.app.ui.theme.HologenColors
import com.hologen.app.ui.theme.HologenMetrics
import com.hologen.app.ui.theme.HologenTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { HologenTheme { Surface { HologenApp() } } }
    }

    private data class Scan(
        val title: String,
        val subtitle: String,
        val color: androidx.compose.ui.graphics.Color
    )

    private enum class Destination(val label: String) { Scan("Scan"), History("History"), Settings("Settings") }

    @Composable
    fun HologenApp() {
        var selectedDestination by remember { mutableStateOf(Destination.Scan) }
        var selectedScan by remember { mutableIntStateOf(0) }
        var isScanning by remember { mutableStateOf(false) }
        var isRotating by remember { mutableStateOf(true) }
        var isSaved by remember { mutableStateOf(false) }

        val scans = remember {
            listOf(
                Scan("Fern study", "Today · 2 min ago", HologenColors.mintStrong),
                Scan("Ceramic form", "Yesterday · 4 min ago", HologenColors.mintDim),
                Scan("Sculptural light", "Monday · 11 min ago", HologenColors.quiet)
            )
        }

        Scaffold(
            containerColor = HologenColors.canvas,
            bottomBar = {
                NavigationBar(
                    modifier = Modifier.navigationBarsPadding(),
                    containerColor = HologenColors.surface,
                    tonalElevation = 0.dp
                ) {
                    Destination.entries.forEach { destination ->
                        NavigationBarItem(
                            selected = selectedDestination == destination,
                            onClick = { selectedDestination = destination },
                            icon = {
                                Icon(
                                    imageVector = when (destination) {
                                        Destination.Scan -> Icons.Outlined.Home
                                        Destination.History -> Icons.Outlined.BookmarkBorder
                                        Destination.Settings -> Icons.Outlined.PersonOutline
                                    },
                                    contentDescription = destination.label
                                )
                            },
                            label = { Text(destination.label) }
                        )
                    }
                    }
            }
        ) { padding ->
            when (selectedDestination) {
                Destination.Scan -> HomeScreen(
                    modifier = Modifier.padding(padding),
                    scan = scans[selectedScan],
                    scans = scans,
                    selectedScan = selectedScan,
                    isScanning = isScanning,
                    isRotating = isRotating,
                    isSaved = isSaved,
                    onScan = { isScanning = !isScanning },
                    onSelectScan = { selectedScan = it },
                    onRotate = { isRotating = !isRotating },
                    onReset = { selectedScan = 0; isRotating = true },
                    onSave = { isSaved = !isSaved },
                    onSettings = { selectedDestination = Destination.Settings },
                    onHistory = { selectedDestination = Destination.History }
                )
                Destination.History -> HistoryScreen(modifier = Modifier.padding(padding), scans = scans)
                Destination.Settings -> SettingsScreen(modifier = Modifier.padding(padding), isRotating = isRotating, onRotateChange = { isRotating = it }, isSaved = isSaved, onSavedChange = { isSaved = it })
            }
        }
    }

    @Composable
    private fun HomeScreen(
        modifier: Modifier,
        scan: Scan,
        scans: List<Scan>,
        selectedScan: Int,
        isScanning: Boolean,
        isRotating: Boolean,
        isSaved: Boolean,
        onScan: () -> Unit,
        onSelectScan: (Int) -> Unit,
        onRotate: () -> Unit,
        onReset: () -> Unit,
        onSave: () -> Unit,
        onSettings: () -> Unit,
        onHistory: () -> Unit
    ) {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = HologenMetrics.pagePadding,
                top = 24.dp,
                end = HologenMetrics.pagePadding,
                bottom = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(HologenMetrics.sectionGap)
        ) {
            item {
                Header(onSettings = onSettings)
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Good evening, Alex", style = MaterialTheme.typography.headlineSmall)
                    Text("Bring your ideas into focus.", style = MaterialTheme.typography.bodyMedium, color = HologenColors.quiet)
                }
            }
            item {
                HologramViewer(
                    scan = scan,
                    isScanning = isScanning,
                    isRotating = isRotating,
                    isSaved = isSaved,
                    onScan = onScan,
                    onRotate = onRotate,
                    onReset = onReset,
                    onSave = onSave
                )
            }
            item {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Recent scans", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                    Row(
                        modifier = Modifier.clickable(onClick = onHistory),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("See all", style = MaterialTheme.typography.labelLarge, color = HologenColors.mint)
                        Icon(Icons.Outlined.ArrowForward, contentDescription = "Open history", tint = HologenColors.mint, modifier = Modifier.size(18.dp))
                    }
                }
            }
            items(scans) { recent ->
                ScanCard(
                    scan = recent,
                    selected = scans.indexOf(recent) == selectedScan,
                    onClick = { onSelectScan(scans.indexOf(recent)) }
                )
            }
        }
    }

    @Composable
    private fun Header(onSettings: () -> Unit) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier.size(34.dp).clip(CircleShape).background(HologenColors.mint),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = HologenColors.ink, modifier = Modifier.size(19.dp))
                }
                Spacer(Modifier.width(10.dp))
                Text("hologen", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            }
            IconButton(onClick = onSettings) { Icon(Icons.Outlined.Settings, contentDescription = "Settings", tint = HologenColors.quiet) }
        }
    }

    @Composable
    private fun HologramViewer(
        scan: Scan,
        isScanning: Boolean,
        isRotating: Boolean,
        isSaved: Boolean,
        onScan: () -> Unit,
        onRotate: () -> Unit,
        onReset: () -> Unit,
        onSave: () -> Unit
    ) {
        val rotation by rememberInfiniteTransition(label = "hologram rotation").animateFloat(
            initialValue = 0f,
            targetValue = if (isRotating) 360f else 0f,
            animationSpec = infiniteRepeatable(tween(9000, easing = LinearEasing), RepeatMode.Restart),
            label = "rotation"
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(HologenMetrics.panelRadius))
                .shadow(HologenMetrics.panelElevation, RoundedCornerShape(HologenMetrics.panelRadius))
                .background(HologenColors.surface)
                .border(1.dp, HologenColors.line, RoundedCornerShape(HologenMetrics.panelRadius))
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Live hologram", style = MaterialTheme.typography.labelLarge, color = HologenColors.mint)
                    Text(scan.title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 4.dp))
                }
                IconButton(onClick = onSave) {
                    Icon(if (isSaved) Icons.Outlined.Check else Icons.Outlined.BookmarkBorder, contentDescription = "Save scan", tint = HologenColors.mint)
                }
            }
            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.22f)
                    .clip(RoundedCornerShape(HologenMetrics.smallRadius))
                    .background(HologenColors.ink),
                contentAlignment = Alignment.Center
            ) {
                HologramCanvas(modifier = Modifier.fillMaxSize().graphicsLayer(rotationZ = rotation), accent = scan.color)
                if (isScanning) {
                    Text("Scanning...", color = HologenColors.mint, style = MaterialTheme.typography.labelLarge)
                }
                Row(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ViewerControl(icon = Icons.Outlined.RotateRight, label = "Rotate", active = isRotating, onClick = onRotate)
                    ViewerControl(icon = Icons.Outlined.Refresh, label = "Reset", active = false, onClick = onReset)
                }
            }
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Ready to explore", style = MaterialTheme.typography.titleMedium)
                    Text("Place an object in view to begin", style = MaterialTheme.typography.bodyMedium, color = HologenColors.quiet)
                }
                FloatingActionButton(onClick = onScan, containerColor = HologenColors.mint, contentColor = HologenColors.ink) {
                    Icon(if (isScanning) Icons.Outlined.Check else Icons.Outlined.Add, contentDescription = if (isScanning) "Stop scan" else "Start scan")
                }
            }
        }
    }

    @Composable
    private fun HologramCanvas(modifier: Modifier, accent: androidx.compose.ui.graphics.Color) {
        Canvas(modifier = modifier) {
            val center = Offset(size.width / 2f, size.height / 2.1f)
            val scale = size.minDimension / 2.2f
            drawCircle(accent.copy(alpha = 0.07f), scale * 0.72f, center)
            drawOval(accent.copy(alpha = 0.24f), topLeft = Offset(center.x - scale * 0.71f, center.y + scale * 0.355f), size = Size(scale * 1.42f, scale * 0.25f), style = Stroke(2.dp.toPx()))
            drawOval(accent.copy(alpha = 0.14f), topLeft = Offset(center.x - scale * 0.9f, center.y + scale * 0.3f), size = Size(scale * 1.8f, scale * 0.36f), style = Stroke(1.dp.toPx()))
            repeat(6) { index ->
                val height = scale * (0.45f + index * 0.065f)
                val width = scale * (0.22f + (index % 2) * 0.09f)
                val left = center.x - width / 2f + (index - 2.5f) * scale * 0.055f
                drawLine(accent.copy(alpha = 0.18f), Offset(left, center.y + scale * 0.38f), Offset(left + width * 0.42f, center.y - height), width = 2.dp.toPx(), cap = StrokeCap.Round)
                drawLine(accent.copy(alpha = 0.28f), Offset(left + width * 0.42f, center.y - height), Offset(left + width, center.y + scale * 0.38f), width = 2.dp.toPx(), cap = StrokeCap.Round)
            }
            drawLine(accent.copy(alpha = 0.18f), Offset(center.x - scale, center.y + scale * 0.48f), Offset(center.x + scale, center.y + scale * 0.48f), width = 1.dp.toPx())
        }
    }

    @Composable
    private fun ViewerControl(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, active: Boolean, onClick: () -> Unit) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(HologenMetrics.smallRadius))
                .background(if (active) HologenColors.mint else HologenColors.raised)
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = label, tint = if (active) HologenColors.ink else HologenColors.paper, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(label, style = MaterialTheme.typography.labelLarge, color = if (active) HologenColors.ink else HologenColors.paper)
        }
    }

    @Composable
    private fun ScanCard(scan: Scan, selected: Boolean, onClick: () -> Unit) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(HologenMetrics.smallRadius))
                .shadow(HologenMetrics.cardElevation, RoundedCornerShape(HologenMetrics.smallRadius))
                .background(if (selected) HologenColors.raised else HologenColors.surface)
                .border(1.dp, if (selected) HologenColors.mintDim else HologenColors.line, RoundedCornerShape(HologenMetrics.smallRadius))
                .clickable(onClick = onClick)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(46.dp).clip(RoundedCornerShape(10.dp)).background(scan.color.copy(alpha = 0.18f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = scan.color, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(scan.title, style = MaterialTheme.typography.titleMedium)
                Text(scan.subtitle, style = MaterialTheme.typography.bodyMedium, color = HologenColors.quiet)
            }
            Icon(Icons.Outlined.ArrowForward, contentDescription = "Open ${scan.title}", tint = HologenColors.quiet, modifier = Modifier.size(18.dp))
        }
    }

    @Composable
    private fun HistoryScreen(modifier: Modifier, scans: List<Scan>) {
        var selected by remember { mutableIntStateOf(0) }
        LazyColumn(modifier = modifier.fillMaxSize(), contentPadding = androidx.compose.foundation.layout.PaddingValues(HologenMetrics.pagePadding), verticalArrangement = Arrangement.spacedBy(HologenMetrics.itemGap)) {
            item { Text("History", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(bottom = 10.dp)) }
            items(scans) { scan ->
                val index = scans.indexOf(scan)
                ScanCard(scan = scan, selected = index == selected, onClick = { selected = index })
            }
        }
    }

    @Composable
    private fun SettingsScreen(modifier: Modifier, isRotating: Boolean, onRotateChange: (Boolean) -> Unit, isSaved: Boolean, onSavedChange: (Boolean) -> Unit) {
        Column(modifier = modifier.fillMaxSize().padding(HologenMetrics.pagePadding), verticalArrangement = Arrangement.spacedBy(HologenMetrics.sectionGap)) {
            Text("Settings", style = MaterialTheme.typography.headlineSmall)
            Text("Viewer preferences", style = MaterialTheme.typography.bodyMedium, color = HologenColors.quiet)
            SettingRow("Auto-rotate holograms", "Keep the viewer moving while open", isRotating, onRotateChange)
            SettingRow("Save scans locally", "Keep your studies on this device", isSaved, onSavedChange)
        }
    }

    @Composable
    private fun SettingRow(title: String, description: String, enabled: Boolean, onEnabledChange: (Boolean) -> Unit) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(description, style = MaterialTheme.typography.bodyMedium, color = HologenColors.quiet)
            }
            Switch(checked = enabled, onCheckedChange = onEnabledChange)
        }
    }
}
