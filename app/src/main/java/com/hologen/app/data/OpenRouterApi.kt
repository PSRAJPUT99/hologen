package com.hologen.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

object OpenRouterApi {
    
    // Fetches all available models from OpenRouter
    suspend fun fetchModels(apiKey: String): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            if (apiKey.isBlank()) {
                return@withContext Result.failure(Exception("API Key is empty. Please add it in Settings."))
            }

            val url = URL("https://openrouter.ai/api/v1/models")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Authorization", "Bearer $apiKey")
            connection.setRequestProperty("Content-Type", "application/json")
            connection.connectTimeout = 10000 // 10 seconds

            val responseCode = connection.responseCode
            
            if (responseCode == 200) {
                val response = connection.inputStream.bufferedReader().readText()
                // Simple regex to extract model IDs from JSON response
                // Looks for "id": "provider/model-name"
                val regex = Regex(""""id"\s*:\s*"([^"]+)"""")
                val models = regex.findAll(response).map { it.groupValues[1] }.toList()
                
                // Filter out some noise if any, and sort
                Result.success(models.filter { it.contains("/") }.sorted())
            } else {
                val errorStream = connection.errorStream?.bufferedReader()?.readText() ?: "Unknown error"
                Result.failure(Exception("Failed to fetch models. Code: $responseCode. Check your API Key."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}