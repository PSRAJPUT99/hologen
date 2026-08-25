package com.hologen.app.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hologen.app.ui.theme.HologenColors
import com.hologen.app.ui.theme.HologenMetrics

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var highQualityHolograms by remember { mutableStateOf(false) }

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
            headlineContent = { Text("User Profile", color = HologenColors.Text.primary) },
            supportingContent = { Text("Guest User (Login to sync data)", color = HologenColors.Text.secondary) },
            modifier = Modifier.background(HologenColors.Background.card, MaterialTheme.shapes.medium)
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = { /* TODO: Implement Login/Signup later */ },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = HologenColors.Accent.mint)
        ) {
            Text("Sign In / Sign Up")
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- SECTION 2: Preferences ---
        SettingsSectionHeader(title = "Preferences", icon = Icons.Outlined.Bolt)
        Spacer(modifier = Modifier.height(16.dp))

        ListItem(
            headlineContent = { Text("Push Notifications", color = HologenColors.Text.primary) },
            trailingIcon = {
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = HologenColors.Accent.mint)
                )
            },
            modifier = Modifier.background(HologenColors.Background.card, MaterialTheme.shapes.medium)
        )
        Spacer(modifier = Modifier.height(8.dp))
        ListItem(
            headlineContent = { Text("High Quality Holograms", color = HologenColors.Text.primary) },
            supportingContent = { Text("Uses more data and battery for better 3D", color = HologenColors.Text.secondary) },
            trailingIcon = {
                Switch(
                    checked = highQualityHolograms,
                    onCheckedChange = { highQualityHolograms = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = HologenColors.Accent.mint)
                )
            },
            modifier = Modifier.background(HologenColors.Background.card, MaterialTheme.shapes.medium)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // --- SECTION 3: Data Management ---
        SettingsSectionHeader(title = "Data Management", icon = Icons.Outlined.ClearAll)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = { /* TODO: Clear data logic */ },
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
            color = HologenColors.Text.secondary
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