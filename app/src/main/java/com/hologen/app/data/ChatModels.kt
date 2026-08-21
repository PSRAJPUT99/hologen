package com.hologen.app.data

enum class AttachmentType {
    PHOTO,
    VIDEO,
    LINK
}

data class Attachment(
    val type: AttachmentType,
    val uri: String
)

enum class MessageSender {
    USER,
    AI
}

data class ChatMessage(
    val id: String,
    val text: String,
    val sender: MessageSender,
    val attachments: List<Attachment> = emptyList()
)

data class ScanProgress(
    val message: String,
    val percent: Int
)

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isProcessing: Boolean = false,
    val scanProgress: ScanProgress? = null
)