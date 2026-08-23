package com.hologen.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ClearAll
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hologen.app.data.SettingsRepository
import com.hologen.app.ui.theme.HologenColors
import com.hologen.app.ui.theme.HologenMetrics
import kotlinx.coroutines.launch

// Popular OpenRouter Models
val AVAILABLE_MODELS = listOf(
    "openai/gpt-4o",
    "openai/gpt-4o-mini",
    "anthropic/claude-3.5-sonnet",
    "anthropic/claude-3-haiku",
    "meta-llama/llama-3-70b-instruct",
    "google/gemini-pro-1.5",
    "mistralai/mixtral-8x22b-instruct"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val repository = remember { SettingsRepository(context) }
    val scope = rememberCoroutineScope()
    
    val apiKey by repository.apiKey.collectAsStateWithLifecycle(initialValue = "")
    val selectedModel by repository.selectedModel.collectAsStateWithLifecycle(initialValue = "openai/gpt-4o")

    var inputApiKey by remember { mutableStateOf(apiKey ?: "") }
    var isModelDropdownExpanded by remember { mutableStateOf(false) }
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

        // --- SECTION 1: AI Configuration ---
        SettingsSectionHeader(title = "AI Configuration", icon = Icons.Outlined.SmartToy)
        Spacer(modifier = Modifier.height(16.dp))

        // API Key Input
        OutlinedTextField(
            value = inputApiKey,
            onValueChange = { inputApiKey = it },
            label = { Text("OpenRouter API Key") },
            placeholder = { Text("sk-or-v1-...") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = HologenColors.Accent.mint,
                focusedLabelColor = HologenColors.Accent.mint
            ),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Button(
            onClick = { 
                scope.launch {
                    repository.saveApiKey(inputApiKey)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = HologenColors.Accent.mint)
        ) {
            Text("Save API Key", color = HologenColors.Background.primary)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Model Selector
        Text(
            text = "Preferred Model",
            style = MaterialTheme.typography.titleMedium,
            color = HologenColors.Text.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { isModelDropdownExpanded = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = HologenColors.Text.primary)
            ) {
                Text(selectedModel ?: "Select Model", modifier = Modifier.weight(1f))
            }
            DropdownMenu(
                expanded = isModelDropdownExpanded,
                onDismissRequest = { isModelDropdownExpanded = false },
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                AVAILABLE_MODELS.forEach { model ->
                    DropdownMenuItem(
                        text = { Text(model) },
                        onClick = {
                            scope.launch {
                                repository.saveSelectedModel(model)
                            }
                            isModelDropdownExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- SECTION 2: Data Management ---
        SettingsSectionHeader(title = "Data Management", icon = Icons.Outlined.ClearAll)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = { showClearDialog = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Clear All Local Data")
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- SECTION 3: About ---
        SettingsSectionHeader(title = "About", icon = Icons.Outlined.Info)
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Hologen v1.0.0\nPowered by Omi AI Engine",
            style = MaterialTheme.typography.bodyMedium,
            color = HologenColors.Text.secondary
        )
    }

    // Clear Data Confirmation Dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear Data?") },
            text = { Text("This will reset your local settings and chat history. This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        repository.clearLocalData()
                        inputApiKey = ""
                    }
                    showClearDialog = false
                }) {
                    Text("Clear", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel")
                }
            }
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