package com.hologen.app.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hologen.app.R
import com.hologen.app.data.Attachment
import com.hologen.app.data.AttachmentType
import com.hologen.app.data.ChatMessage
import com.hologen.app.data.MessageSender
import com.hologen.app.ui.theme.HologenColors
import com.hologen.app.ui.theme.HologenMetrics
import com.hologen.app.viewmodel.ChatViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val chatViewModel: ChatViewModel = viewModel(factory = ChatViewModel.Factory)
    val uiState by chatViewModel.uiState.collectAsStateWithLifecycle()
    var draft by remember { mutableStateOf("") }
    var attachments by remember { mutableStateOf<List<Attachment>>(emptyList()) }
    
    // State for the attachment bottom sheet
    var showAttachmentSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    // Create a URI for the camera photo
    var cameraPhotoUri by remember { mutableStateOf<Uri?>(null) }

    // Camera Launcher (Opens actual camera)
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && cameraPhotoUri != null) {
            attachments = attachments + Attachment(AttachmentType.PHOTO, cameraPhotoUri.toString())
        }
        cameraPhotoUri = null // Reset
    }

    // Photo Picker Launcher (Opens gallery for photos)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            attachments = attachments + Attachment(AttachmentType.PHOTO, it.toString())
        }
    }

    // Video Picker Launcher (Opens gallery for videos)
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            attachments = attachments + Attachment(AttachmentType.VIDEO, it.toString())
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HologenColors.Background.primary)
    ) {
        // Top Zone: Hologram Stage
        HologramViewer(
            modifier = Modifier.weight(3f),
            isLoading = uiState.isProcessing
        )

        // Bottom Zone: Chat Interface
        Column(
            modifier = Modifier
                .weight(2f)
                .padding(horizontal = HologenMetrics.space16, vertical = HologenMetrics.space12),
            verticalArrangement = Arrangement.spacedBy(HologenMetrics.space12)
        ) {
            MessageList(
                messages = uiState.messages,
                modifier = Modifier.weight(1f)
            )

            // Attachment Chips
            if (attachments.isNotEmpty()) {
                AttachmentChips(
                    attachments = attachments,
                    onRemove = { attachmentToRemove ->
                        attachments = attachments.filter { it != attachmentToRemove }
                    }
                )
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

    // Attachment Bottom Sheet (The "+" Menu)
    if (showAttachmentSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAttachmentSheet = false },
            sheetState = sheetState,
            containerColor = HologenColors.Background.card
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Add Attachment",
                    style = MaterialTheme.typography.titleMedium,
                    color = HologenColors.Text.primary,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // CAMERA - Opens actual camera
                    AttachmentOption(
                        icon = Icons.Outlined.CameraAlt,
                        label = "Camera",
                        onClick = {
                            // Create URI for camera photo
                            val photoFile = File.createTempFile(
                                "hologen_capture_",
                                ".jpg",
                                context.cacheDir
                            )
                            cameraPhotoUri = androidx.core.content.FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                photoFile
                            )
                            cameraLauncher.launch(cameraPhotoUri)
                            showAttachmentSheet = false
                        }
                    )
                    // PHOTO - Opens gallery for existing photos
                    AttachmentOption(
                        icon = Icons.Outlined.Image,
                        label = "Photo",
                        onClick = {
                            photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            showAttachmentSheet = false
                        }
                    )
                    // VIDEO - Opens gallery for videos
                    AttachmentOption(
                        icon = Icons.Outlined.VideoLibrary,
                        label = "Video",
                        onClick = {
                            videoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
                            showAttachmentSheet = false
                        }
                    )
                    // LINK
                    AttachmentOption(
                        icon = Icons.Outlined.Link,
                        label = "Link",
                        onClick = {
                            attachments = attachments + Attachment(AttachmentType.LINK, "link_${System.currentTimeMillis()}")
                            showAttachmentSheet = false
                        }
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

// Helper Composable for Attachment Options in the Sheet
@Composable
private fun AttachmentOption(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = HologenColors.Accent.mint,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = HologenColors.Text.primary
        )
    }
}

@Composable
private fun AttachmentChips(
    attachments: List<Attachment>,
    onRemove: (Attachment) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(HologenMetrics.space8)
    ) {
        items(attachments) { attachment ->
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(HologenMetrics.space8))
                    .background(HologenColors.Background.cardSecondary)
                    .padding(horizontal = HologenMetrics.space8, vertical = HologenMetrics.space4),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when (attachment.type) {
                        AttachmentType.PHOTO -> Icons.Outlined.Image
                        AttachmentType.VIDEO -> Icons.Outlined.Videocam
                        AttachmentType.LINK -> Icons.Outlined.Link
                    },
                    contentDescription = null,
                    tint = HologenColors.Accent.mint,
                    modifier = Modifier.size(HologenMetrics.space16)
                )
                Spacer(modifier = Modifier.width(HologenMetrics.space4))
                Text(
                    text = attachment.type.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelSmall,
                    color = HologenColors.Text.primary
                )
                Spacer(modifier = Modifier.width(HologenMetrics.space4))
                IconButton(
                    onClick = { onRemove(attachment) },
                    modifier = Modifier.size(HologenMetrics.space16)
                ) {
                    Icon(
                        Icons.Outlined.Close,
                        contentDescription = "Remove",
                        tint = HologenColors.Text.secondary,
                        modifier = Modifier.size(HologenMetrics.space12)
                    )
                }
            }
        }
    }
}

@Composable
private fun MessageList(messages: List<ChatMessage>, modifier: Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(HologenMetrics.space8)
    ) {
        if (messages.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(HologenMetrics.space16),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.chat_empty_state),
                        style = MaterialTheme.typography.bodyMedium,
                        color = HologenColors.Text.secondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(messages, key = { it.id }) { message ->
                MessageBubble(message)
            }
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
    } catch (e: Exception) {
        null
    }
}

@Composable
private fun MessageBubble(message: ChatMessage) {
    val context = LocalContext.current
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (message.sender == MessageSender.USER) Alignment.End else Alignment.Start
    ) {
        if (message.attachments.isNotEmpty()) {
            message.attachments.forEach { attachment ->
                when (attachment.type) {
                    AttachmentType.PHOTO -> {
                        val bitmap = remember(attachment.uri) {
                            try {
                                val inputStream = context.contentResolver.openInputStream(Uri.parse(attachment.uri))
                                BitmapFactory.decodeStream(inputStream)?.asImageBitmap()
                            } catch (e: Exception) { null }
                        }
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap,
                                contentDescription = "Attached photo",
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .padding(bottom = 4.dp)
                            )
                        }
                    }
                    AttachmentType.VIDEO -> {
                        val videoThumbnail = remember(attachment.uri) {
                            getVideoThumbnail(context, Uri.parse(attachment.uri))?.asImageBitmap()
                        }
                        if (videoThumbnail != null) {
                            Box {
                                Image(
                                    bitmap = videoThumbnail,
                                    contentDescription = "Attached video",
                                    modifier = Modifier
                                        .size(120.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .padding(bottom = 4.dp)
                                )
                                Icon(
                                    imageVector = Icons.Outlined.Videocam,
                                    contentDescription = "Video",
                                    tint = HologenColors.Accent.mint,
                                    modifier = Modifier.align(Alignment.Center).size(32.dp)
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier.size(120.dp).clip(RoundedCornerShape(16.dp)).background(HologenColors.Background.cardSecondary).padding(bottom = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Outlined.Videocam, contentDescription = "Video", tint = HologenColors.Accent.mint, modifier = Modifier.size(48.dp))
                            }
                        }
                    }
                    AttachmentType.LINK -> {
                        Icon(
                            imageVector = Icons.Outlined.Link,
                            contentDescription = "Link",
                            tint = HologenColors.Accent.mint,
                            modifier = Modifier.size(48.dp).padding(bottom = 4.dp)
                        )
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
            .padding(
                start = HologenMetrics.space16,
                end = HologenMetrics.space8,
                top = HologenMetrics.space8,
                bottom = HologenMetrics.space8
            ),
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
                        Text(
                            text = stringResource(R.string.chat_input_hint),
                            style = MaterialTheme.typography.bodyMedium,
                            color = HologenColors.Text.secondary
                        )
                    }
                    innerTextField()
                }
            }
        )

        // The NEW "+" Button
        IconButton(onClick = onShowAttachmentMenu, enabled = enabled) {
            Icon(Icons.Outlined.Add, contentDescription = "Add Attachment", tint = HologenColors.Text.secondary)
        }

        IconButton(
            onClick = onSend,
            enabled = canSend,
            modifier = Modifier
                .clip(RoundedCornerShape(HologenMetrics.buttonRadius))
                .background(if (canSend) HologenColors.Accent.mint else HologenColors.Background.cardSecondary)
        ) {
            Icon(Icons.Outlined.Send, contentDescription = stringResource(R.string.send_message), tint = HologenColors.Background.primary)
        }
    }
}