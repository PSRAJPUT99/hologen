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

enum class AIProvider { GEMINI, OPENAI, OPENROUTER, ANTHROPIC }

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

                val provider = detectProvider(model, apiKey)
                val hasImages = attachments.any { it.type == AttachmentType.PHOTO }
                
                val fullResponse = when (provider) {
                    AIProvider.GEMINI -> callGeminiAPI(apiKey, model, text, attachments)
                    AIProvider.OPENAI -> callOpenAIAPI(apiKey, model, text, attachments)
                    AIProvider.OPENROUTER -> callOpenRouterAPI(apiKey, model, text, attachments)
                    AIProvider.ANTHROPIC -> callAnthropicAPI(apiKey, model, text, attachments)
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

    // SMART DETECTION: Model ID aur API Key dono ko check karta hai
    private fun detectProvider(model: String, apiKey: String): AIProvider {
        val modelLower = model.lowercase()
        
        // Pehle Model ID check karo
        return when {
            modelLower.contains("gemini") -> AIProvider.GEMINI
            modelLower.contains("claude") -> AIProvider.ANTHROPIC
            modelLower.startsWith("gpt-") || modelLower.startsWith("o1-") -> AIProvider.OPENAI
            modelLower.contains("openai/") -> AIProvider.OPENAI
            modelLower.contains("anthropic/") -> AIProvider.ANTHROPIC
            modelLower.contains("meta-llama/") || modelLower.contains("mistral/") || modelLower.contains("google/") -> AIProvider.OPENROUTER
            // Fallback: API Key format check karo
            apiKey.startsWith("AIza") || apiKey.startsWith("AQ.") -> AIProvider.GEMINI
            apiKey.startsWith("sk-or-v1") -> AIProvider.OPENROUTER
            apiKey.startsWith("sk-") -> AIProvider.OPENAI
            else -> AIProvider.OPENROUTER // Default
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

    // --- GEMINI API (Supports ALL key formats: AIza, AQ, etc.) ---
    private suspend fun callGeminiAPI(apiKey: String, model: String, prompt: String, attachments: List<Attachment>): String {
        return withContext(Dispatchers.IO) {
            val modelName = if (model.contains("gemini")) model else "gemini-1.5-flash"
            val url = URL("https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true
            connection.connectTimeout = 30000
            connection.readTimeout = 60000

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
            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
            jsonBody.put("contents", contentsArray)
            jsonBody.put("generationConfig", JSONObject().put("maxOutputTokens", 500))

            val os = connection.outputStream
            OutputStreamWriter(os, "UTF-8").apply { write(jsonBody.toString()); flush(); close() }
            os.close()

            val code = connection.responseCode
            val stream = if (code == 200) connection.inputStream else connection.errorStream
            val res = stream?.bufferedReader()?.readText() ?: ""
            connection.disconnect()

            if (code == 200) {
                try {
                    return@withContext JSONObject(res).getJSONArray("candidates").getJSONObject(0)
                        .getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text")
                } catch (e: Exception) { return@withContext "Failed to parse Gemini response: ${e.message}" }
            } else {
                return@withContext "Gemini Error ($code): $res"
            }
        }
    }

    // --- OPENAI API ---
    private suspend fun callOpenAIAPI(apiKey: String, model: String, prompt: String, attachments: List<Attachment>): String {
        return withContext(Dispatchers.IO) {
            val url = URL("https://api.openai.com/v1/chat/completions")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Authorization", "Bearer $apiKey")
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true
            connection.connectTimeout = 30000
            connection.readTimeout = 60000

            val jsonBody = JSONObject()
            jsonBody.put("model", model.replace("openai/", ""))
            jsonBody.put("max_tokens", 500)
            
            val messagesArray = JSONArray()
            val sysMsg = JSONObject(); sysMsg.put("role", "system"); sysMsg.put("content", "You are Omi, AI assistant for Hologen 3D app.")
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

            val os = connection.outputStream
            OutputStreamWriter(os, "UTF-8").apply { write(jsonBody.toString()); flush(); close() }
            os.close()

            val code = connection.responseCode
            val stream = if (code == 200) connection.inputStream else connection.errorStream
            val res = stream?.bufferedReader()?.readText() ?: ""
            connection.disconnect()

            return@withContext if (code == 200) {
                try { JSONObject(res).getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content") }
                catch (e: Exception) { "Parse error: ${e.message}" }
            } else { "OpenAI Error ($code): $res" }
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
            connection.connectTimeout = 30000
            connection.readTimeout = 60000

            val jsonBody = JSONObject()
            jsonBody.put("model", model)
            jsonBody.put("max_tokens", 500)
            
            val messagesArray = JSONArray()
            val sysMsg = JSONObject(); sysMsg.put("role", "system"); sysMsg.put("content", "You are Omi, AI assistant for Hologen 3D app.")
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

            val os = connection.outputStream
            OutputStreamWriter(os, "UTF-8").apply { write(jsonBody.toString()); flush(); close() }
            os.close()

            val code = connection.responseCode
            val stream = if (code == 200) connection.inputStream else connection.errorStream
            val res = stream?.bufferedReader()?.readText() ?: ""
            connection.disconnect()

            return@withContext if (code == 200) {
                try { JSONObject(res).getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content") }
                catch (e: Exception) { "Parse error: ${e.message}" }
            } else { "OpenRouter Error ($code): $res" }
        }
    }

    // --- ANTHROPIC API (Claude) ---
    private suspend fun callAnthropicAPI(apiKey: String, model: String, prompt: String, attachments: List<Attachment>): String {
        return withContext(Dispatchers.IO) {
            val url = URL("https://api.anthropic.com/v1/messages")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("x-api-key", apiKey)
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("anthropic-version", "2023-06-01")
            connection.doOutput = true
            connection.connectTimeout = 30000
            connection.readTimeout = 60000

            val jsonBody = JSONObject()
            jsonBody.put("model", model.replace("anthropic/", ""))
            jsonBody.put("max_tokens", 500)
            
            val messagesArray = JSONArray()
            val userMsg = JSONObject(); userMsg.put("role", "user")
            
            val contentArray = JSONArray()
            if (prompt.isNotBlank()) { val t = JSONObject(); t.put("type", "text"); t.put("text", prompt); contentArray.put(t) }
            
            for (att in attachments) {
                if (att.type == AttachmentType.PHOTO) {
                    val b64 = imageToBase64(att.uri)
                    if (b64 != null) {
                        val img = JSONObject()
                        img.put("type", "image")
                        val source = JSONObject()
                        source.put("type", "base64")
                        source.put("media_type", "image/jpeg")
                        source.put("data", b64)
                        img.put("source", source)
                        contentArray.put(img)
                    }
                }
            }
            userMsg.put("content", contentArray)
            messagesArray.put(userMsg)
            jsonBody.put("messages", messagesArray)

            val os = connection.outputStream
            OutputStreamWriter(os, "UTF-8").apply { write(jsonBody.toString()); flush(); close() }
            os.close()

            val code = connection.responseCode
            val stream = if (code == 200) connection.inputStream else connection.errorStream
            val res = stream?.bufferedReader()?.readText() ?: ""
            connection.disconnect()

            return@withContext if (code == 200) {
                try { JSONObject(res).getJSONArray("content").getJSONObject(0).getString("text") }
                catch (e: Exception) { "Parse error: ${e.message}" }
            } else { "Anthropic Error ($code): $res" }
        }
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