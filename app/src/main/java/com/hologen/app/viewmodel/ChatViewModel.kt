package com.hologen.app.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.hologen.app.data.Attachment
import com.hologen.app.data.ChatMessage
import com.hologen.app.data.MessageSender
import com.hologen.app.data.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isProcessing: Boolean = false,
    val error: String? = null
)

class ChatViewModel(application: Application) : ViewModel() {

    private val settingsRepository = SettingsRepository(application)

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    fun sendMessage(text: String, attachments: List<Attachment>) {
        if (text.isBlank() && attachments.isEmpty()) return

        // 1. Add User Message to UI immediately
        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            text = text,
            attachments = attachments,
            sender = MessageSender.USER,
            timestamp = System.currentTimeMillis()
        )

        val currentMessages = _uiState.value.messages.toMutableList()
        currentMessages.add(userMessage)
        _uiState.value = _uiState.value.copy(messages = currentMessages, isProcessing = true, error = null)

        // 2. Launch Coroutine to call OpenRouter API
        viewModelScope.launch {
            try {
                val apiKey = settingsRepository.apiKey.first()
                val model = settingsRepository.selectedModel.first() ?: "openai/gpt-4o-mini"

                if (apiKey.isNullOrBlank()) {
                    throw Exception("API Key missing! Please add it in Settings.")
                }

                // Call the actual API
                val aiResponse = callOpenRouterAPI(apiKey, model, text)

                // 3. Add AI Response to UI
                val aiMessage = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    text = aiResponse,
                    attachments = emptyList(),
                    sender = MessageSender.AI,
                    timestamp = System.currentTimeMillis()
                )

                val updatedMessages = _uiState.value.messages.toMutableList()
                updatedMessages.add(aiMessage)
                _uiState.value = _uiState.value.copy(messages = updatedMessages, isProcessing = false)

            } catch (e: Exception) {
                // Handle Error
                val errorMessage = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    text = "Error: ${e.message ?: "Unknown error occurred"}",
                    attachments = emptyList(),
                    sender = MessageSender.AI,
                    timestamp = System.currentTimeMillis()
                )
                val updatedMessages = _uiState.value.messages.toMutableList()
                updatedMessages.add(errorMessage)
                _uiState.value = _uiState.value.copy(messages = updatedMessages, isProcessing = false, error = e.message)
            }
        }
    }

    // --- NETWORK LOGIC ---
    private suspend fun callOpenRouterAPI(apiKey: String, model: String, prompt: String): String {
        return withContext(Dispatchers.IO) {
            val url = URL("https://openrouter.ai/api/v1/chat/completions")
            val connection = url.openConnection() as HttpURLConnection
            
            connection.requestMethod = "POST"
            connection.setRequestProperty("Authorization", "Bearer $apiKey")
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("HTTP-Referer", "https://github.com/hologen-app") 
            connection.setRequestProperty("X-Title", "Hologen App") 
            connection.doOutput = true
            connection.connectTimeout = 30000 // 30 seconds
            connection.readTimeout = 60000 // 60 seconds

            // Create JSON Body
            val jsonBody = JSONObject()
            jsonBody.put("model", model)
            
            val messagesArray = JSONArray()
            val userMessageObj = JSONObject()
            userMessageObj.put("role", "user")
            userMessageObj.put("content", prompt)
            messagesArray.put(userMessageObj)
            
            jsonBody.put("messages", messagesArray)

            // Send Request
            val os = connection.outputStream
            val writer = OutputStreamWriter(os, "UTF-8")
            writer.write(jsonBody.toString())
            writer.flush()
            writer.close()
            os.close()

            // Read Response
            val responseCode = connection.responseCode
            val inputStream = if (responseCode == HttpURLConnection.HTTP_OK) {
                connection.inputStream
            } else {
                connection.errorStream
            }

            val responseString = inputStream?.bufferedReader()?.readText() ?: ""
            connection.disconnect()

            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Parse JSON Response
                try {
                    val jsonResponse = JSONObject(responseString)
                    val choices = jsonResponse.getJSONArray("choices")
                    if (choices.length() > 0) {
                        val messageObj = choices.getJSONObject(0).getJSONObject("message")
                        return@withContext messageObj.getString("content")
                    } else {
                        return@withContext "No response from AI."
                    }
                } catch (e: Exception) {
                    return@withContext "Failed to parse AI response: ${e.message}"
                }
            } else {
                return@withContext "API Error ($responseCode): $responseString"
            }
        }
    }

    // --- FACTORY ---
    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = extras[androidx.lifecycle.viewmodel.AndroidViewModelFactory.APPLICATION_KEY]!!
                return ChatViewModel(application) as T
            }
        }
    }
}