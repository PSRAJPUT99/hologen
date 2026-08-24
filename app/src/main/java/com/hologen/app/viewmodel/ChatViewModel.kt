package com.hologen.app.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
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

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isProcessing: Boolean = false,
    val isTyping: Boolean = false,
    val error: String? = null
)

enum class AIProvider { GEMINI, OPENROUTER, OPENAI }

class ChatViewModel(private val application: Application) : ViewModel() {
    private val settingsRepository = SettingsRepository(application)
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    fun sendMessage(text: String, attachments: List<Attachment>) {
        if (text.isBlank() && attachments.isEmpty()) return

        val userMessage = ChatMessage(id = UUID.randomUUID().toString(), text = text, attachments = attachments, sender = MessageSender.USER)
        val currentMessages = _uiState.value.messages.toMutableList()
        currentMessages.add(userMessage)
        _uiState.value = _uiState.value.copy(messages = currentMessages, isProcessing = true, isTyping = true, error = null)

        viewModelScope.launch {
            try {
                val apiKey = settingsRepository.apiKey.first()
                val model = settingsRepository.selectedModel.first() ?: "openai/gpt-4o-mini"

                if (apiKey.isNullOrBlank()) throw Exception("API Key missing! Please add it in Settings.")

                val provider = detectProvider(apiKey)
                val hasImages = attachments.any { it.type == AttachmentType.PHOTO }
                
                val fullResponse = when (provider) {
                    AIProvider.GEMINI -> callGeminiAPI(apiKey, text, attachments)
                    AIProvider.OPENROUTER -> callOpenRouterAPI(apiKey, model, text, attachments)
                    AIProvider.OPENAI -> callOpenAIAPI(apiKey, model, text, attachments)
                }
                
                streamAIResponse(fullResponse)
            } catch (e: Exception) {
                val errorMessage = ChatMessage(id = UUID.randomUUID().toString(), text = "Error: ${e.message ?: "Unknown error"}", attachments = emptyList(), sender = MessageSender.AI)
                val updatedMessages = _uiState.value.messages.toMutableList()
                updatedMessages.add(errorMessage)
                _uiState.value = _uiState.value.copy(messages = updatedMessages, isProcessing = false, isTyping = false, error = e.message)
            }
        }
    }

    private fun detectProvider(apiKey: String): AIProvider {
        return when {
            apiKey.startsWith("AIza") -> AIProvider.GEMINI
            apiKey.startsWith("sk-or-v1") -> AIProvider.OPENROUTER
            apiKey.startsWith("sk-") -> AIProvider.OPENAI
            else -> AIProvider.OPENROUTER // Default fallback
        }
    }

    private suspend fun streamAIResponse(fullResponse: String) {
        val words = fullResponse.split(" ")
        var currentText = ""
        val aiMessageId = UUID.randomUUID().toString()
        val aiMessage = ChatMessage(id = aiMessageId, text = "", attachments = emptyList(), sender = MessageSender.AI)
        
        val updatedMessages = _uiState.value.messages.toMutableList()
        updatedMessages.add(aiMessage)
        _uiState.value = _uiState.value.copy(messages = updatedMessages, isTyping = true)

        words.forEachIndexed { index, word ->
            delay(40)
            currentText += if (index == 0) word else " $word"
            val messages = _uiState.value.messages.toMutableList()
            val aiMessageIndex = messages.indexOfFirst { it.id == aiMessageId }
            if (aiMessageIndex != -1) {
                messages[aiMessageIndex] = messages[aiMessageIndex].copy(text = currentText)
                _uiState.value = _uiState.value.copy(messages = messages)
            }
        }
        _uiState.value = _uiState.value.copy(isProcessing = false, isTyping = false)
    }

    // --- GEMINI API ---
    private suspend fun callGeminiAPI(apiKey: String, prompt: String, attachments: List<Attachment>): String {
        return withContext(Dispatchers.IO) {
            val url = URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true
            connection.connectTimeout = 30000; connection.readTimeout = 60000

            val jsonBody = JSONObject()
            val contentsArray = JSONArray()
            val contentObj = JSONObject()
            val partsArray = JSONArray()
            
            if (prompt.isNotBlank()) { val t = JSONObject(); t.put("text", prompt); partsArray.put(t) }
            
            for (att in attachments) {
                if (att.type == AttachmentType.PHOTO) {
                    val b64 = imageToBase64(att.uri)
                    if (b64 != null) {
                        val img = JSONObject()
                        img.put("inline_data", JSONObject().apply { put("mime_type", "image/jpeg"); put("data", b64) })
                        partsArray.put(img)
                    }
                }
            }
            contentObj.put("parts", partsArray); contentsArray.put(contentObj)
            jsonBody.put("contents", contentsArray)
            jsonBody.put("generationConfig", JSONObject().put("maxOutputTokens", 500))

            sendRequest(connection, jsonBody)
            parseGeminiResponse(connection)
        }
    }

    // --- OPENROUTER API ---
    private suspend fun callOpenRouterAPI(apiKey: String, model: String, prompt: String, attachments: List<Attachment>): String {
        return withContext(Dispatchers.IO) {
            val url = URL("https://openrouter.ai/api/v1/chat/completions")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Authorization", "Bearer $apiKey")
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("HTTP-Referer", "https://github.com/hologen-app")
            connection.setRequestProperty("X-Title", "Hologen App")
            connection.doOutput = true
            connection.connectTimeout = 30000; connection.readTimeout = 60000

            val jsonBody = JSONObject()
            jsonBody.put("model", model)
            jsonBody.put("max_tokens", 500)
            
            val messagesArray = JSONArray()
            val sysMsg = JSONObject(); sysMsg.put("role", "system"); sysMsg.put("content", "You are Omi, a professional AI assistant for Hologen 3D app. Identify objects and describe technical parts concisely.")
            messagesArray.put(sysMsg)
            
            val userMsg = JSONObject(); userMsg.put("role", "user")
            
            val hasImages = attachments.any { it.type == AttachmentType.PHOTO }
            if (hasImages) {
                val contentArray = JSONArray()
                if (prompt.isNotBlank()) { val t = JSONObject(); t.put("type", "text"); t.put("text", prompt); contentArray.put(t) }
                for (att in attachments) {
                    if (att.type == AttachmentType.PHOTO) {
                        val b64 = imageToBase64(att.uri)
                        if (b64 != null) {
                            val img = JSONObject(); img.put("type", "image_url"); img.put("image_url", JSONObject().put("url", "data:image/jpeg;base64,$b64")); contentArray.put(img)
                        }
                    }
                }
                userMsg.put("content", contentArray)
            } else {
                userMsg.put("content", prompt)
            }
            messagesArray.put(userMsg)
            jsonBody.put("messages", messagesArray)

            sendRequest(connection, jsonBody)
            parseOpenRouterResponse(connection)
        }
    }

    // --- OPENAI API (Direct) ---
    private suspend fun callOpenAIAPI(apiKey: String, model: String, prompt: String, attachments: List<Attachment>): String {
        return withContext(Dispatchers.IO) {
            val url = URL("https://api.openai.com/v1/chat/completions")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Authorization", "Bearer $apiKey")
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true
            connection.connectTimeout = 30000; connection.readTimeout = 60000

            val jsonBody = JSONObject()
            jsonBody.put("model", model)
            jsonBody.put("max_tokens", 500)
            
            val messagesArray = JSONArray()
            val userMsg = JSONObject(); userMsg.put("role", "user")
            
            val hasImages = attachments.any { it.type == AttachmentType.PHOTO }
            if (hasImages) {
                val contentArray = JSONArray()
                if (prompt.isNotBlank()) { val t = JSONObject(); t.put("type", "text"); t.put("text", prompt); contentArray.put(t) }
                for (att in attachments) {
                    if (att.type == AttachmentType.PHOTO) {
                        val b64 = imageToBase64(att.uri)
                        if (b64 != null) {
                            val img = JSONObject(); img.put("type", "image_url"); img.put("image_url", JSONObject().put("url", "data:image/jpeg;base64,$b64")); contentArray.put(img)
                        }
                    }
                }
                userMsg.put("content", contentArray)
            } else {
                userMsg.put("content", prompt)
            }
            messagesArray.put(userMsg)
            jsonBody.put("messages", messagesArray)

            sendRequest(connection, jsonBody)
            parseOpenRouterResponse(connection) // Same JSON structure for choices
        }
    }

    // --- HELPERS ---
    private fun sendRequest(connection: HttpURLConnection, jsonBody: JSONObject) {
        val os = connection.outputStream
        OutputStreamWriter(os, "UTF-8").apply { write(jsonBody.toString()); flush(); close() }
        os.close()
    }

    private fun parseOpenRouterResponse(connection: HttpURLConnection): String {
        val code = connection.responseCode
        val stream = if (code == 200) connection.inputStream else connection.errorStream
        val res = stream?.bufferedReader()?.readText() ?: ""
        connection.disconnect()
        return if (code == 200) {
            try { JSONObject(res).getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content") } 
            catch (e: Exception) { "Failed to parse response." }
        } else { "API Error ($code): $res" }
    }

    private fun parseGeminiResponse(connection: HttpURLConnection): String {
        val code = connection.responseCode
        val stream = if (code == 200) connection.inputStream else connection.errorStream
        val res = stream?.bufferedReader()?.readText() ?: ""
        connection.disconnect()
        return if (code == 200) {
            try { JSONObject(res).getJSONArray("candidates").getJSONObject(0).getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text") }
            catch (e: Exception) { "Failed to parse Gemini response." }
        } else { "Gemini Error ($code): $res" }
    }

    private fun imageToBase64(uriString: String): String? {
        return try {
            val uri = Uri.parse(uriString)
            val inputStream = application.contentResolver.openInputStream(uri) ?: return null
            val bitmap = BitmapFactory.decodeStream(inputStream); inputStream.close()
            val bos = ByteArrayOutputStream(); bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos)
            Base64.encodeToString(bos.toByteArray(), Base64.NO_WRAP)
        } catch (e: Exception) { null }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                return ChatViewModel(checkNotNull(extras[AndroidViewModelFactory.APPLICATION_KEY])) as T
            }
        }
    }
}