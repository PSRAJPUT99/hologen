package com.hologen.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.Color
import com.hologen.app.ui.theme.HologenColors
import com.hologen.app.ui.theme.HologenMetrics

@Composable
fun ScanScreen(modifier: Modifier = Modifier) {
    var draft by remember { mutableStateOf("") }
    var attachmentHint by remember { mutableStateOf("Send a photo or link to begin") }
    val messages = remember { mutableStateListOf<String>() }

    Column(modifier = modifier.fillMaxSize().background(HologenColors.Background.primary)) {
        HologramViewer(modifier = Modifier.weight(3f))
        Column(
            modifier = Modifier
                .weight(2f)
                .padding(horizontal = HologenMetrics.space16, vertical = HologenMetrics.space12),
            verticalArrangement = Arrangement.spacedBy(HologenMetrics.space12)
        ) {
            MessageList(messages = messages, modifier = Modifier.weight(1f))
            Composer(
                draft = draft,
                attachmentHint = attachmentHint,
                onDraftChange = { draft = it },
                onAttachment = { attachmentHint = it },
                onSend = {
                    if (draft.isNotBlank()) {
                        messages.add(draft.trim())
                        draft = ""
                        attachmentHint = "Send a photo or link to begin"
                    }
                }
            )
        }
    }
}

@Composable
private fun MessageList(messages: List<String>, modifier: Modifier) {
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
                        "Send a photo or link to begin",
                        style = MaterialTheme.typography.bodyMedium,
                        color = HologenColors.Text.secondary
                    )
                }
            }
        } else {
            items(messages) { message ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Text(
                        text = message,
                        modifier = Modifier
                            .clip(RoundedCornerShape(HologenMetrics.historyCardRadius))
                            .background(HologenColors.Background.cardSecondary)
                            .padding(horizontal = HologenMetrics.space12, vertical = HologenMetrics.space8),
                        style = MaterialTheme.typography.bodyMedium,
                        color = HologenColors.Text.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun Composer(
    draft: String,
    attachmentHint: String,
    onDraftChange: (String) -> Unit,
    onAttachment: (String) -> Unit,
    onSend: () -> Unit
) {
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
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = HologenColors.Text.primary),
            cursorBrush = SolidColor(HologenColors.Accent.mint),
            singleLine = true,
            decorationBox = { innerTextField ->
                Box {
                    if (draft.isEmpty()) {
                        Text(attachmentHint, style = MaterialTheme.typography.bodyMedium, color = HologenColors.Text.secondary)
                    }
                    innerTextField()
                }
            }
        )
        IconButton(onClick = { onAttachment("Add a photo to begin") }) {
            Icon(Icons.Outlined.PhotoCamera, contentDescription = "Attach camera photo", tint = HologenColors.Text.secondary)
        }
        IconButton(onClick = { onAttachment("Add a video to begin") }) {
            Icon(Icons.Outlined.Videocam, contentDescription = "Attach video", tint = HologenColors.Text.secondary)
        }
        IconButton(onClick = { onAttachment("Paste a link to begin") }) {
            Icon(Icons.Outlined.Link, contentDescription = "Attach link", tint = HologenColors.Text.secondary)
        }
        IconButton(
            onClick = onSend,
            enabled = draft.isNotBlank(),
            modifier = Modifier
                .clip(RoundedCornerShape(HologenMetrics.buttonRadius))
                .background(if (draft.isNotBlank()) HologenColors.Accent.mint else HologenColors.Background.cardSecondary)
        ) {
            Icon(Icons.Outlined.Send, contentDescription = "Send message", tint = HologenColors.Background.primary)
        }
    }
}
