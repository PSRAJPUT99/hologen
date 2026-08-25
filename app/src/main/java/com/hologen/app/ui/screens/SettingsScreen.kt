package com.hologen.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.ClearAll
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hologen.app.ui.theme.HologenColors
import com.hologen.app.ui.theme.HologenMetrics

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var notificationsEnabled by remember { mutableStateOf(true) }
    var highQualityHolograms by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HologenColors.Background.primary)
            .verticalScroll(rememberScrollState())
            .padding(HologenMetrics.space16)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            color = HologenColors.Text.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // --- SECTION 1: Account ---
        SettingsSectionHeader(title = "Account", icon = Icons.Outlined.AccountCircle)
        Spacer(modifier = Modifier.height(16.dp))
        
        ListItem(
            headlineContent = { Text("User Profile", color = HologenColors.Text.primary, fontWeight = FontWeight.Medium) },
            supportingContent = { Text("Guest User (Sync enabled after login)", color = HologenColors.Text.secondary) },
            modifier = Modifier.background(HologenColors.Background.card, MaterialTheme.shapes.medium)
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = { 
                Toast.makeText(context, "Profile management coming soon!", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = HologenColors.Accent.mint)
        ) {
            Text("Manage Account")
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- SECTION 2: Preferences ---
        SettingsSectionHeader(title = "Preferences", icon = Icons.Outlined.Bolt)
        Spacer(modifier = Modifier.height(16.dp))

        ListItem(
            headlineContent = { Text("Push Notifications", color = HologenColors.Text.primary) },
            trailingContent = {
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = HologenColors.Accent.mint,
                        checkedTrackColor = HologenColors.Accent.mint.copy(alpha = 0.5f)
                    )
                )
            },
            modifier = Modifier.background(HologenColors.Background.card, MaterialTheme.shapes.medium)
        )
        Spacer(modifier = Modifier.height(8.dp))
        ListItem(
            headlineContent = { Text("High Quality Holograms", color = HologenColors.Text.primary) },
            supportingContent = { Text("Uses more data and battery for better 3D rendering", color = HologenColors.Text.secondary) },
            trailingContent = {
                Switch(
                    checked = highQualityHolograms,
                    onCheckedChange = { highQualityHolograms = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = HologenColors.Accent.mint,
                        checkedTrackColor = HologenColors.Accent.mint.copy(alpha = 0.5f)
                    )
                )
            },
            modifier = Modifier.background(HologenColors.Background.card, MaterialTheme.shapes.medium)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // --- SECTION 3: Data Management ---
        SettingsSectionHeader(title = "Data Management", icon = Icons.Outlined.ClearAll)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = { showClearDialog = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Clear Local Cache & Data")
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- SECTION 4: About ---
        SettingsSectionHeader(title = "About", icon = Icons.Outlined.Info)
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Hologen v1.0.0\nPowered by Hologen Spatial Engine",
            style = MaterialTheme.typography.bodyMedium,
            color = HologenColors.Text.secondary,
            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.5
        )
    }

    // Clear Data Confirmation Dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear Data?", color = HologenColors.Text.primary) },
            text = { 
                Text(
                    "This will reset your local settings and clear cached 3D models. This action cannot be undone.", 
                    color = HologenColors.Text.secondary 
                ) 
            },
            confirmButton = {
                TextButton(onClick = {
                    // TODO: Add actual clear data logic here later
                    Toast.makeText(context, "Cache cleared successfully!", Toast.LENGTH_SHORT).show()
                    showClearDialog = false
                }) {
                    Text("Clear", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel", color = HologenColors.Text.primary)
                }
            },
            containerColor = HologenColors.Background.card
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = HologenColors.Accent.mint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = HologenColors.Text.primary,
            fontWeight = FontWeight.SemiBold
        )
    }
    HorizontalDivider(
        modifier = Modifier.padding(top = 8.dp),
        color = HologenColors.Background.cardSecondary
    )
}