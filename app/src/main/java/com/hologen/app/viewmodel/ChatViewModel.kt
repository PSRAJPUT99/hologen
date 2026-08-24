package com.hologen.app.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.hologen.app.data.Attachment
import com.hologen.app.data.AttachmentType
import com.hologen.app.data.ChatMessage
import com.hologen.app.data.MessageSender
import com.hologen.app.data.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID
import android.util.Base64

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isProcessing: Boolean = false,
    val isTyping: Boolean = false,
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
            sender = MessageSender.USER
        )

        val currentMessages = _uiState.value.messages.toMutableList()
        currentMessages.add(userMessage)
        _uiState.value = _uiState.value.copy(
            messages = currentMessages, 
            isProcessing = true, 
            isTyping = true,
            error = null
        )

        // 2. Launch Coroutine to call OpenRouter API
        viewModelScope.launch {
            try {
                val apiKey = settingsRepository.apiKey.first()
                val model = settingsRepository.selectedModel.first() ?: "openai/gpt-4o-mini"

                if (apiKey.isNullOrBlank()) {
                    throw Exception("API Key missing! Please add it in Settings.")
                }

                // Check if there are image attachments
                val hasImages = attachments.any { it.type == AttachmentType.PHOTO }
                
                // Call the actual API (with or without vision)
                val fullResponse = if (hasImages) {
                    callOpenRouterVisionAPI(apiKey, model, text, attachments)
                } else {
                    callOpenRouterAPI(apiKey, model, text)
                }
                
                // 3. Stream the response word by word
                streamAIResponse(fullResponse)

            } catch (e: Exception) {
                // Handle Error
                val errorMessage = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    text = "Error: ${e.message ?: "Unknown error occurred"}",
                    attachments = emptyList(),
                    sender = MessageSender.AI
                )
                val updatedMessages = _uiState.value.messages.toMutableList()
                updatedMessages.add(errorMessage)
                _uiState.value = _uiState.value.copy(
                    messages = updatedMessages, 
                    isProcessing = false, 
                    isTyping = false,
                    error = e.message
                )
            }
        }
    }

    // Stream AI response word by word for professional effect
    private suspend fun streamAIResponse(fullResponse: String) {
        val words = fullResponse.split(" ")
        var currentText = ""
        
        // Create AI message with empty text
        val aiMessageId = UUID.randomUUID().toString()
        val aiMessage = ChatMessage(
            id = aiMessageId,
            text = "",
            attachments = emptyList(),
            sender = MessageSender.AI
        )
        
        val updatedMessages = _uiState.value.messages.toMutableList()
        updatedMessages.add(aiMessage)
        _uiState.value = _uiState.value.copy(messages = updatedMessages, isTyping = true)

        // Stream each word with delay
        words.forEachIndexed { index, word ->
            delay(50) // 50ms delay between words for natural typing effect
            currentText += if (index == 0) word else " $word"
            
            val messages = _uiState.value.messages.toMutableList()
            val aiMessageIndex = messages.indexOfFirst { it.id == aiMessageId }
            if (aiMessageIndex != -1) {
                messages[aiMessageIndex] = messages[aiMessageIndex].copy(text = currentText)
                _uiState.value = _uiState.value.copy(messages = messages)
            }
        }

        // Done typing
        _uiState.value = _uiState.value.copy(
            isProcessing = false, 
            isTyping = false
        )
    }

    // --- VISION API (For Images) ---
    private suspend fun callOpenRouterVisionAPI(
        apiKey: String, 
        model: String, 
        prompt: String,
        attachments: List<Attachment>
    ): String {
        return withContext(Dispatchers.IO) {
            val url = URL("https://openrouter.ai/api/v1/chat/completions")
            val connection = url.openConnection() as HttpURLConnection
            
            connection.requestMethod = "POST"
            connection.setRequestProperty("Authorization", "Bearer $apiKey")
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("HTTP-Referer", "https://github.com/hologen-app") 
            connection.setRequestProperty("X-Title", "Hologen App") 
            connection.doOutput = true
            connection.connectTimeout = 30000
            connection.readTimeout = 60000

            // Create JSON Body
            val jsonBody = JSONObject()
            jsonBody.put("model", model)
            jsonBody.put("max_tokens", 500)
            
            val messagesArray = JSONArray()
            
            // System Prompt for Hologen Vision Assistant
            val systemMessage = JSONObject()
            systemMessage.put("role", "system")
            systemMessage.put("content", 
                "You are Omi, the AI vision assistant for Hologen - a professional 3D hologram app. " +
                "When users send you images, identify the object clearly and describe its key features, parts, and technical specifications. " +
                "Be concise but informative. If asked to create a 3D model, confirm what object you see."
            )
            messagesArray.put(systemMessage)
            
            // User message with text and images
            val userMessageObj = JSONObject()
            userMessageObj.put("role", "user")
            
            val contentArray = JSONArray()
            
            // Add text prompt if exists
            if (prompt.isNotBlank()) {
                val textObj = JSONObject()
                textObj.put("type", "text")
                textObj.put("text", prompt)
                contentArray.put(textObj)
            }
            
            // Add images
            for (attachment in attachments) {
                if (attachment.type == AttachmentType.PHOTO) {
                    try {
                        val base64Image = imageToBase64(attachment.uri)
                        if (base64Image != null) {
                            val imageObj = JSONObject()
                            imageObj.put("type", "image_url")
                            val imageUrlObj = JSONObject()
                            imageUrlObj.put("url", "data:image/jpeg;base64,$base64Image")
                            imageObj.put("image_url", imageUrlObj)
                            contentArray.put(imageObj)
                        }
                    } catch (e: Exception) {
                        // Skip if image conversion fails
                    }
                }
            }
            
            userMessageObj.put("content", contentArray)
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

    // Convert image URI to Base64
    private fun imageToBase64(uri: Uri): String? {
        return try {
            val inputStream = javaClass.classLoader?.getResourceAsStream(uri.toString())
                ?: return null
            
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()
            android.util.Base64.encodeToString(byteArray, android.util.Base64.NO_WRAP)
        } catch (e: Exception) {
            null
        }
    }

    // --- TEXT API (For text-only) ---
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
            connection.connectTimeout = 30000
            connection.readTimeout = 60000

            // Create JSON Body with System Prompt for Professional Behavior
            val jsonBody = JSONObject()
            jsonBody.put("model", model)
            jsonBody.put("max_tokens", 500)
            
            // Add System Prompt for Professional Hologen Assistant
            val messagesArray = JSONArray()
            
            val systemMessage = JSONObject()
            systemMessage.put("role", "system")
            systemMessage.put("content", 
                "You are Omi, the AI assistant for Hologen - a professional 3D hologram and object visualization app. " +
                "Your role is to help users identify objects, understand their parts, and visualize them in 3D. " +
                "Be professional, concise, and helpful. When describing objects, focus on technical details and components. " +
                "Keep responses clear and structured."
            )
            messagesArray.put(systemMessage)
            
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
                val application = checkNotNull(extras[AndroidViewModelFactory.APPLICATION_KEY])
                return ChatViewModel(application) as T
            }
        }
    }
}