package com.hologen.app.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image // UI Component
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hologen.app.R
import com.hologen.app.data.Attachment
import com.hologen.app.data.AttachmentType
import com.hologen.app.data.ChatMessage
import com.hologen.app.data.MessageSender
import com.hologen.app.data.OpenRouterApi
import com.hologen.app.data.SettingsRepository
import com.hologen.app.ui.theme.HologenColors
import com.hologen.app.ui.theme.HologenMetrics
import com.hologen.app.viewmodel.ChatViewModel
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val chatViewModel: ChatViewModel = viewModel(factory = ChatViewModel.Factory)
    val settingsRepository = remember { SettingsRepository(context) }
    val scope = rememberCoroutineScope()
    
    val uiState by chatViewModel.uiState.collectAsStateWithLifecycle()
    var draft by remember { mutableStateOf("") }
    var attachments by remember { mutableStateOf<List<Attachment>>(emptyList()) }
    
    var showAttachmentSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    var cameraUri by remember { mutableStateOf<Uri?>(null) }

    var showModelSheet by remember { mutableStateOf(false) }
    val modelSheetState = rememberModalBottomSheetState()
    var availableModels by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoadingModels by remember { mutableStateOf(false) }
    var selectedModel by remember { mutableStateOf("Select Model") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        settingsRepository.selectedModel.collect { model ->
            if (model != null) selectedModel = model
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val currentUri = cameraUri
        if (success && currentUri != null) {
            attachments = attachments + Attachment(AttachmentType.PHOTO, currentUri.toString())
        }
        cameraUri = null 
    }

    val requestCameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            try {
                val photoFile = File.createTempFile("hologen_capture_", ".jpg", context.cacheDir)
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", photoFile)
                cameraUri = uri
                cameraLauncher.launch(uri)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { attachments = attachments + Attachment(AttachmentType.PHOTO, it.toString()) }
    }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { attachments = attachments + Attachment(AttachmentType.VIDEO, it.toString()) }
    }

    Column(
        modifier = modifier.fillMaxSize().background(HologenColors.Background.primary)
    ) {
        HologramViewer(modifier = Modifier.weight(3f), isLoading = uiState.isProcessing)

        Column(
            modifier = Modifier.weight(2f).padding(horizontal = HologenMetrics.space16, vertical = HologenMetrics.space12),
            verticalArrangement = Arrangement.spacedBy(HologenMetrics.space12)
        ) {
            MessageList(messages = uiState.messages, modifier = Modifier.weight(1f))

            if (attachments.isNotEmpty()) {
                AttachmentChips(attachments = attachments, onRemove = { attachmentToRemove ->
                    attachments = attachments.filter { it != attachmentToRemove }
                })
            }

            Composer(
                draft = draft,
                attachments = attachments,
                enabled = !uiState.isProcessing,
                onDraftChange = { draft = it },
                onShowAttachmentMenu = { showAttachmentSheet = true },
                onSend = {
                    chatViewModel.sendMessage(draft, attachments)
                    draft = ""
                    attachments = emptyList()
                }
            )
        }
    }

    if (showAttachmentSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAttachmentSheet = false },
            sheetState = sheetState,
            containerColor = HologenColors.Background.card
        ) {
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Add Attachment", style = MaterialTheme.typography.titleMedium, color = HologenColors.Text.primary, modifier = Modifier.padding(bottom = 24.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    AttachmentOption(Icons.Outlined.CameraAlt, "Camera") {
                        val isGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                        if (isGranted) {
                            try {
                                val photoFile = File.createTempFile("hologen_capture_", ".jpg", context.cacheDir)
                                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", photoFile)
                                cameraUri = uri
                                cameraLauncher.launch(uri)
                            } catch (e: Exception) { e.printStackTrace() }
                        } else {
                            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                        showAttachmentSheet = false
                    }
                    // FULLY QUALIFIED NAME TO AVOID CONFLICT
                    AttachmentOption(androidx.compose.material.icons.outlined.Image, "Photo") {
                        photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        showAttachmentSheet = false
                    }
                    AttachmentOption(Icons.Outlined.VideoLibrary, "Video") {
                        videoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
                        showAttachmentSheet = false
                    }
                    // FULLY QUALIFIED NAME TO AVOID CONFLICT
                    AttachmentOption(androidx.compose.material.icons.outlined.Link, "Link") {
                        attachments = attachments + Attachment(AttachmentType.LINK, "link_${System.currentTimeMillis()}")
                        showAttachmentSheet = false
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = HologenColors.Background.cardSecondary)
                
                Text(text = "AI Model", style = MaterialTheme.typography.titleSmall, color = HologenColors.Text.secondary, modifier = Modifier.align(Alignment.Start).padding(bottom = 8.dp))

                OutlinedButton(
                    onClick = {
                        showAttachmentSheet = false
                        showModelSheet = true
                        
                        if (availableModels.isEmpty() && !isLoadingModels) {
                            scope.launch {
                                isLoadingModels = true
                                errorMessage = null
                                settingsRepository.apiKey.collect { key ->
                                    if (key != null) {
                                        val result = OpenRouterApi.fetchModels(key)
                                        result.onSuccess { models ->
                                            availableModels = models
                                            isLoadingModels = false
                                        }
                                        result.onFailure { err ->
                                            errorMessage = err.message
                                            isLoadingModels = false
                                        }
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = HologenColors.Accent.mint)
                ) {
                    Icon(Icons.Outlined.SmartToy, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = if (isLoadingModels) "Fetching Models..." else selectedModel, modifier = Modifier.weight(1f), textAlign = TextAlign.Start)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (showModelSheet) {
        ModalBottomSheet(
            onDismissRequest = { showModelSheet = false },
            sheetState = modelSheetState,
            containerColor = HologenColors.Background.card
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Select AI Model", style = MaterialTheme.typography.titleLarge, color = HologenColors.Text.primary, modifier = Modifier.padding(bottom = 16.dp))

                if (isLoadingModels) {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = HologenColors.Accent.mint)
                    }
                } else if (errorMessage != null) {
                    Text(text = "Error: $errorMessage\n\nPlease check your API Key in Settings.", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
                } else if (availableModels.isEmpty()) {
                    Text(text = "No models found. Please save a valid API Key in Settings first.", color = HologenColors.Text.secondary, modifier = Modifier.padding(16.dp))
                } else {
                    LazyColumn(modifier = Modifier.height(400.dp)) {
                        items(availableModels) { model ->
                            ListItem(
                                headlineContent = { Text(model, style = MaterialTheme.typography.bodyMedium) },
                                modifier = Modifier.clickable {
                                    selectedModel = model
                                    scope.launch { settingsRepository.saveSelectedModel(model) }
                                    showModelSheet = false
                                }
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AttachmentOption(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(onClick = onClick).padding(8.dp)) {
        Icon(imageVector = icon, contentDescription = label, tint = HologenColors.Accent.mint, modifier = Modifier.size(32.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = HologenColors.Text.primary)
    }
}

@Composable
private fun AttachmentChips(attachments: List<Attachment>, onRemove: (Attachment) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(HologenMetrics.space8)) {
        items(attachments) { attachment ->
            Row(
                modifier = Modifier.clip(RoundedCornerShape(HologenMetrics.space8)).background(HologenColors.Background.cardSecondary).padding(horizontal = HologenMetrics.space8, vertical = HologenMetrics.space4),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // FULLY QUALIFIED NAMES HERE TOO
                val iconVector: ImageVector = when (attachment.type) {
                    AttachmentType.PHOTO -> androidx.compose.material.icons.outlined.Image
                    AttachmentType.VIDEO -> androidx.compose.material.icons.outlined.Videocam
                    AttachmentType.LINK -> androidx.compose.material.icons.outlined.Link
                }
                Icon(imageVector = iconVector, contentDescription = null, tint = HologenColors.Accent.mint, modifier = Modifier.size(HologenMetrics.space16))
                Spacer(modifier = Modifier.width(HologenMetrics.space4))
                Text(text = attachment.type.name.lowercase().replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.labelSmall, color = HologenColors.Text.primary)
                Spacer(modifier = Modifier.width(HologenMetrics.space4))
                IconButton(onClick = { onRemove(attachment) }, modifier = Modifier.size(HologenMetrics.space16)) {
                    Icon(Icons.Outlined.Close, contentDescription = "Remove", tint = HologenColors.Text.secondary, modifier = Modifier.size(HologenMetrics.space12))
                }
            }
        }
    }
}

@Composable
private fun MessageList(messages: List<ChatMessage>, modifier: Modifier) {
    LazyColumn(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(HologenMetrics.space8)) {
        if (messages.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(HologenMetrics.space16), contentAlignment = Alignment.Center) {
                    Text(text = stringResource(R.string.chat_empty_state), style = MaterialTheme.typography.bodyMedium, color = HologenColors.Text.secondary, textAlign = TextAlign.Center)
                }
            }
        } else {
            items(messages, key = { it.id }) { message -> MessageBubble(message) }
        }
    }
}

private fun getVideoThumbnail(context: Context, uri: Uri): Bitmap? {
    return try {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(context, uri)
        val bitmap = retriever.getFrameAtTime()
        retriever.release()
        bitmap
    } catch (e: Exception) { null }
}

@Composable
private fun MessageBubble(message: ChatMessage) {
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = if (message.sender == MessageSender.USER) Alignment.End else Alignment.Start) {
        if (message.attachments.isNotEmpty()) {
            message.attachments.forEach { attachment ->
                when (attachment.type) {
                    AttachmentType.PHOTO -> {
                        val bitmap = remember(attachment.uri) {
                            try { context.contentResolver.openInputStream(Uri.parse(attachment.uri))?.let { BitmapFactory.decodeStream(it)?.asImageBitmap() } } catch (e: Exception) { null }
                        }
                        if (bitmap != null) {
                            Image(bitmap = bitmap, contentDescription = "Attached photo", modifier = Modifier.size(120.dp).clip(RoundedCornerShape(16.dp)).padding(bottom = 4.dp))
                        }
                    }
                    AttachmentType.VIDEO -> {
                        val videoThumbnail = remember(attachment.uri) { getVideoThumbnail(context, Uri.parse(attachment.uri))?.asImageBitmap() }
                        if (videoThumbnail != null) {
                            Box {
                                Image(bitmap = videoThumbnail, contentDescription = "Attached video", modifier = Modifier.size(120.dp).clip(RoundedCornerShape(16.dp)).padding(bottom = 4.dp))
                                // FULLY QUALIFIED NAME
                                Icon(imageVector = androidx.compose.material.icons.outlined.Videocam, contentDescription = "Video", tint = HologenColors.Accent.mint, modifier = Modifier.align(Alignment.Center).size(32.dp))
                            }
                        }
                    }
                    AttachmentType.LINK -> {
                        // FULLY QUALIFIED NAME
                        Icon(imageVector = androidx.compose.material.icons.outlined.Link, contentDescription = "Link", tint = HologenColors.Accent.mint, modifier = Modifier.size(48.dp).padding(bottom = 4.dp))
                    }
                }
            }
        }
        if (message.text.isNotBlank()) {
            Text(
                text = message.text,
                modifier = Modifier
                    .clip(RoundedCornerShape(HologenMetrics.historyCardRadius))
                    .background(
                        if (message.sender == MessageSender.USER) HologenColors.Background.card
                        else HologenColors.Background.cardSecondary
                    )
                    .padding(horizontal = HologenMetrics.space12, vertical = HologenMetrics.space8),
                style = MaterialTheme.typography.bodyMedium,
                color = HologenColors.Text.primary
            )
        }
    }
}

@Composable
private fun Composer(
    draft: String,
    attachments: List<Attachment>,
    enabled: Boolean,
    onDraftChange: (String) -> Unit,
    onShowAttachmentMenu: () -> Unit,
    onSend: () -> Unit
) {
    val canSend = enabled && (draft.isNotBlank() || attachments.isNotEmpty())

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(HologenMetrics.buttonRadius))
            .background(HologenColors.Background.card)
            .padding(start = HologenMetrics.space16, end = HologenMetrics.space8, top = HologenMetrics.space8, bottom = HologenMetrics.space8),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicTextField(
            value = draft,
            onValueChange = onDraftChange,
            modifier = Modifier.weight(1f),
            enabled = enabled,
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = HologenColors.Text.primary),
            cursorBrush = SolidColor(HologenColors.Accent.mint),
            singleLine = true,
            decorationBox = { innerTextField ->
                Box {
                    if (draft.isEmpty()) {
                        Text(text = stringResource(R.string.chat_input_hint), style = MaterialTheme.typography.bodyMedium, color = HologenColors.Text.secondary)
                    }
                    innerTextField()
                }
            }
        )

        IconButton(onClick = onShowAttachmentMenu, enabled = enabled) {
            Icon(Icons.Outlined.Add, contentDescription = "Add Attachment", tint = HologenColors.Text.secondary)
        }

        IconButton(
            onClick = onSend,
            enabled = canSend,
            modifier = Modifier.clip(RoundedCornerShape(HologenMetrics.buttonRadius)).background(if (canSend) HologenColors.Accent.mint else HologenColors.Background.cardSecondary)
        ) {
            Icon(Icons.Outlined.Send, contentDescription = stringResource(R.string.send_message), tint = HologenColors.Background.primary)
        }
    }
}