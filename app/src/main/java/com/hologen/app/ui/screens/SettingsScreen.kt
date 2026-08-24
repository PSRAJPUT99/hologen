package com.hologen.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ClearAll
import androidx.compose.material.icons.outlined.Info
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val repository = remember { SettingsRepository(context) }
    val scope = rememberCoroutineScope()
    
    val apiKey by repository.apiKey.collectAsStateWithLifecycle(initialValue = "")
    val selectedModel by repository.selectedModel.collectAsStateWithLifecycle(initialValue = "openai/gpt-4o-mini")

    var inputApiKey by remember { mutableStateOf(apiKey ?: "") }
    var inputModel by remember { mutableStateOf(selectedModel ?: "openai/gpt-4o-mini") }
    var showClearDialog by remember { mutableStateOf(false) }
    var saveButtonText by remember { mutableStateOf("Save API Key") }

    LaunchedEffect(apiKey) { if (apiKey != null && apiKey != inputApiKey) inputApiKey = apiKey!! }
    LaunchedEffect(selectedModel) { if (selectedModel != null && selectedModel != inputModel) inputModel = selectedModel!! }

    Column(
        modifier = modifier.fillMaxSize().background(HologenColors.Background.primary).verticalScroll(rememberScrollState()).padding(HologenMetrics.space16)
    ) {
        Text(text = "Settings", style = MaterialTheme.typography.headlineMedium, color = HologenColors.Text.primary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 24.dp))

        SettingsSectionHeader(title = "AI Configuration", icon = Icons.Outlined.SmartToy)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = inputApiKey,
            onValueChange = { inputApiKey = it },
            label = { Text("API Key (OpenRouter, Gemini, or OpenAI)") },
            placeholder = { Text("sk-or-v1-... or AIza...") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = HologenColors.Accent.mint, focusedLabelColor = HologenColors.Accent.mint),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        Button(
            onClick = { 
                if (inputApiKey.isNotBlank()) {
                    scope.launch {
                        repository.saveApiKey(inputApiKey)
                        saveButtonText = "Saved! ✓"
                        Toast.makeText(context, "API Key Saved Successfully!", Toast.LENGTH_SHORT).show()
                        delay(2000); saveButtonText = "Save API Key"
                    }
                } else { Toast.makeText(context, "Please enter a valid API Key", Toast.LENGTH_SHORT).show() }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = HologenColors.Accent.mint),
            enabled = inputApiKey.isNotBlank()
        ) { Text(saveButtonText, color = HologenColors.Background.primary) }

        Spacer(modifier = Modifier.height(24.dp))

        Text(text = "Model ID (Type exact model name)", style = MaterialTheme.typography.titleMedium, color = HologenColors.Text.primary, modifier = Modifier.padding(bottom = 8.dp))
        OutlinedTextField(
            value = inputModel,
            onValueChange = { inputModel = it },
            placeholder = { Text("e.g., openai/gpt-4o, gemini-1.5-flash, claude-3-opus") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = HologenColors.Accent.mint)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { 
                scope.launch {
                    repository.saveSelectedModel(inputModel)
                    Toast.makeText(context, "Model set to: $inputModel", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = HologenColors.Accent.mint)
        ) { Text("Save Model", color = HologenColors.Background.primary) }

        Spacer(modifier = Modifier.height(32.dp))
        SettingsSectionHeader(title = "Data Management", icon = Icons.Outlined.ClearAll)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(onClick = { showClearDialog = true }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text("Clear All Local Data") }

        Spacer(modifier = Modifier.height(32.dp))
        SettingsSectionHeader(title = "About", icon = Icons.Outlined.Info)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Hologen v1.0.0\nUniversal AI Engine", style = MaterialTheme.typography.bodyMedium, color = HologenColors.Text.secondary)
    }

    if (showClearDialog) {
        AlertDialog(onDismissRequest = { showClearDialog = false }, title = { Text("Clear Data?") }, text = { Text("This will reset your local settings and chat history.") },
            confirmButton = { TextButton(onClick = { scope.launch { repository.clearLocalData(); inputApiKey = ""; inputModel = "openai/gpt-4o-mini"; Toast.makeText(context, "Cleared", Toast.LENGTH_SHORT).show() }; showClearDialog = false }) { Text("Clear", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { showClearDialog = false }) { Text("Cancel") } })
    }
}

@Composable
private fun SettingsSectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Icon(imageVector = icon, contentDescription = null, tint = HologenColors.Accent.mint, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = title, style = MaterialTheme.typography.titleLarge, color = HologenColors.Text.primary, fontWeight = FontWeight.SemiBold)
    }
    HorizontalDivider(modifier = Modifier.padding(top = 8.dp), color = HologenColors.Background.cardSecondary)
}