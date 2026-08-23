package com.hologen.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hologen.app.data.Attachment
import com.hologen.app.data.ChatMessage
import com.hologen.app.data.MessageSender
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

// Data classes for Chat State
data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isProcessing: Boolean = false
)

class ChatViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    fun sendMessage(text: String, attachments: List<Attachment>) {
        if (text.isBlank() && attachments.isEmpty()) return

        // 1. Create User Message
        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            text = text,
            sender = MessageSender.USER,
            attachments = attachments
        )

        // 2. Add to State and set processing to true
        _uiState.update { currentState ->
            currentState.copy(
                messages = currentState.messages + userMessage,
                isProcessing = true
            )
        }

        // 3. Simulate AI Response (Mock Backend)
        viewModelScope.launch {
            delay(1500) // 1.5 second ka delay taaki real lage
            
            val aiMessage = ChatMessage(
                id = UUID.randomUUID().toString(),
                text = "Received! Scanning object and researching parts...",
                sender = MessageSender.AI,
                attachments = emptyList()
            )

            _uiState.update { currentState ->
                currentState.copy(
                    messages = currentState.messages + aiMessage,
                    isProcessing = false
                )
            }
        }
    }

    // Factory for Compose
    companion object {
        val Factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return ChatViewModel() as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}