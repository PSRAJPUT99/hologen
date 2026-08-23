package com.hologen.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extension property to create DataStore instance
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "hologen_settings")

class SettingsRepository(private val context: Context) {

    companion object {
        val API_KEY = stringPreferencesKey("openrouter_api_key")
        val SELECTED_MODEL = stringPreferencesKey("selected_model")
    }

    // Read API Key
    val apiKey: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[API_KEY]
    }

    // Read Selected Model
    val selectedModel: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[SELECTED_MODEL]
    }

    // Save API Key
    suspend fun saveApiKey(key: String) {
        context.dataStore.edit { preferences ->
            preferences[API_KEY] = key
        }
    }

    // Save Selected Model
    suspend fun saveSelectedModel(model: String) {
        context.dataStore.edit { preferences ->
            preferences[SELECTED_MODEL] = model
        }
    }

    // Clear all local data (Chat history, settings, etc.)
    suspend fun clearLocalData() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}