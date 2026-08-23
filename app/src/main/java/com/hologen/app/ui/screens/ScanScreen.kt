package com.hologen.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
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

@Composable
fun ScanScreen(modifier: Modifier = Modifier) {
    val chatViewModel: ChatViewModel = viewModel(factory = ChatViewModel.Factory)
    val uiState by chatViewModel.uiState.collectAsStateWithLifecycle()
    var draft by remember { mutableStateOf("") }
    var attachments by remember { mutableStateOf<List<Attachment>>(emptyList()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HologenColors.Background.primary)
    ) {
        // Top Zone: Hologram Viewer (60% space approx, weight 3f)
        HologramViewer(
            modifier = Modifier.weight(3f), 
            isLoading = uiState.isProcessing
        )
        
        // Bottom Zone: Chat Interface (40% space approx, weight 2f)
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
            Composer(
                draft = draft,
                attachments = attachments,
                enabled = !uiState.isProcessing,
                onDraftChange = { draft = it },
                onAttachment = { type ->
                    attachments = attachments + Attachment(type, "pending://${type.name.lowercase()}")
                },
                onSend = {
                    chatViewModel.sendMessage(draft, attachments)
                    draft = ""
                    attachments = emptyList()
                }
            )
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
                        color = HologenColors.Text.secondary
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

@Composable
private fun MessageBubble(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.sender == MessageSender.USER) Arrangement.End else Arrangement.Start
    ) {
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

@Composable
private fun Composer(
    draft: String,
    attachments: List<Attachment>,
    enabled: Boolean,
    onDraftChange: (String) -> Unit,
    onAttachment: (AttachmentType) -> Unit,
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
            modifier = Modifier.weight(1f), // <-- Yahan weight safe hai kyunki ye Row ke andar hai
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
        
        IconButton(onClick = { onAttachment(AttachmentType.PHOTO) }, enabled = enabled) {
            Icon(Icons.Outlined.PhotoCamera, contentDescription = stringResource(R.string.attach_photo), tint = HologenColors.Text.secondary)
        }
        IconButton(onClick = { onAttachment(AttachmentType.VIDEO) }, enabled = enabled) {
            Icon(Icons.Outlined.Videocam, contentDescription = stringResource(R.string.attach_video), tint = HologenColors.Text.secondary)
        }
        IconButton(onClick = { onAttachment(AttachmentType.LINK) }, enabled = enabled) {
            Icon(Icons.Outlined.Link, contentDescription = stringResource(R.string.attach_link), tint = HologenColors.Text.secondary)
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