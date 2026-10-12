package com.nazil.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "nazil_settings")

class AppRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }
    private val historyFile = File(context.filesDir, "history.json")

    private val _history = MutableStateFlow<List<DownloadItem>>(emptyList())
    val history: StateFlow<List<DownloadItem>> = _history.asStateFlow()

    val settings: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            themeMode = prefs[KEY_THEME] ?: ThemeMode.SYSTEM,
            defaultQuality = prefs[KEY_QUALITY] ?: Quality.BEST,
        )
    }

    suspend fun updateTheme(mode: String) {
        context.dataStore.edit { it[KEY_THEME] = mode }
    }

    suspend fun updateQuality(quality: String) {
        context.dataStore.edit { it[KEY_QUALITY] = quality }
    }

    suspend fun currentQuality(): String =
        settings.map { it.defaultQuality }.first()

    suspend fun loadHistory() = withContext(Dispatchers.IO) {
        if (!historyFile.exists()) return@withContext
        runCatching {
            val text = historyFile.readText()
            if (text.isBlank()) return@runCatching
            _history.value = json.decodeFromString<List<DownloadItem>>(text)
                .sortedByDescending { it.createdAt }
        }
    }

    suspend fun upsert(item: DownloadItem) {
        val list = _history.value.toMutableList()
        val idx = list.indexOfFirst { it.id == item.id }
        if (idx >= 0) list[idx] = item else list.add(0, item)
        _history.value = list
        persist()
    }

    suspend fun remove(id: String) {
        _history.value = _history.value.filterNot { it.id == id }
        persist()
    }

    suspend fun clearCompleted() {
        _history.value = _history.value.filterNot { it.isDone || it.status == DownloadStatus.FAILED || it.status == DownloadStatus.CANCELED }
        persist()
    }

    private suspend fun persist() = withContext(Dispatchers.IO) {
        runCatching {
            historyFile.writeText(json.encodeToString(_history.value))
        }
    }

    companion object {
        private val KEY_THEME = stringPreferencesKey("theme_mode")
        private val KEY_QUALITY = stringPreferencesKey("default_quality")
    }
}
