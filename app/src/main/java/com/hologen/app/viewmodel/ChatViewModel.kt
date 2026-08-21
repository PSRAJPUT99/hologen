package com.hologen.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hologen.app.data.Attachment
import com.hologen.app.data.ChatMessage
import com.hologen.app.data.ChatUiState
import com.hologen.app.data.MessageSender
import com.hologen.app.repository.ChatRepository
import com.hologen.app.repository.MockApiRepository
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatViewModel(
    private val repository: ChatRepository = MockApiRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    fun sendMessage(text: String, attachments: List<Attachment>) {
        val trimmedText = text.trim()
        if (trimmedText.isEmpty() && attachments.isEmpty()) return
        if (_uiState.value.isProcessing) return

        appendMessage(
            ChatMessage(
                id = UUID.randomUUID().toString(),
                text = trimmedText,
                sender = MessageSender.USER,
                attachments = attachments
            )
        )
        if (attachments.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, scanProgress = null) }
            runCatching {
                val scanId = repository.createScan(trimmedText, attachments)
                repository.streamScan(scanId).collect { progress ->
                    _uiState.update { it.copy(scanProgress = progress) }
                    appendMessage(
                        ChatMessage(
                            id = UUID.randomUUID().toString(),
                            text = progress.message,
                            sender = MessageSender.AI
                        )
                    )
                }
                appendMessage(
                    ChatMessage(
                        id = UUID.randomUUID().toString(),
                        text = "Your hologram is ready to explore.",
                        sender = MessageSender.AI
                    )
                )
            }.onFailure { error ->
                appendMessage(
                    ChatMessage(
                        id = UUID.randomUUID().toString(),
                        text = error.message ?: "We could not complete that scan.",
                        sender = MessageSender.AI
                    )
                )
            }
            _uiState.update { it.copy(isProcessing = false) }
        }
    }

    private fun appendMessage(message: ChatMessage) {
        _uiState.update { it.copy(messages = it.messages + message) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = ChatViewModel() as T
        }
    }
}