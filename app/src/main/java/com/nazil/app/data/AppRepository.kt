package com.nazil.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
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
            sponsorBlock = prefs[KEY_SPONSOR_BLOCK] ?: true,
            subtitleLangs = prefs[KEY_SUBTITLE_LANGS] ?: "ar,en",
            downloadSubtitles = prefs[KEY_DOWNLOAD_SUBTITLES] ?: false,
            maxConcurrent = (prefs[KEY_MAX_CONCURRENT] ?: 2).coerceIn(1, 4),
            downloadOverWifiOnly = prefs[KEY_WIFI_ONLY] ?: false,
            autoRetry = prefs[KEY_AUTO_RETRY] ?: true,
            thumbnailCache = prefs[KEY_THUMB_CACHE] ?: true,
        )
    }

    suspend fun updateTheme(mode: String) {
        context.dataStore.edit { it[KEY_THEME] = mode }
    }

    suspend fun updateQuality(quality: String) {
        context.dataStore.edit { it[KEY_QUALITY] = quality }
    }

    suspend fun updateSponsorBlock(enabled: Boolean) {
        context.dataStore.edit { it[KEY_SPONSOR_BLOCK] = enabled }
    }

    suspend fun updateSubtitles(enabled: Boolean, langs: String) {
        context.dataStore.edit {
            it[KEY_DOWNLOAD_SUBTITLES] = enabled
            it[KEY_SUBTITLE_LANGS] = langs
        }
    }

    suspend fun updateConcurrent(max: Int) {
        context.dataStore.edit { it[KEY_MAX_CONCURRENT] = max.coerceIn(1, 4) }
    }

    suspend fun updateWifiOnly(enabled: Boolean) {
        context.dataStore.edit { it[KEY_WIFI_ONLY] = enabled }
    }

    suspend fun updateAutoRetry(enabled: Boolean) {
        context.dataStore.edit { it[KEY_AUTO_RETRY] = enabled }
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

    suspend fun clearAll() {
        _history.value = emptyList()
        persist()
    }

    suspend fun clearCompleted() {
        _history.value = _history.value.filterNot {
            it.isDone || it.status == DownloadStatus.FAILED || it.status == DownloadStatus.CANCELED
        }
        persist()
    }

    suspend fun clearFailed() {
        _history.value = _history.value.filterNot { it.status == DownloadStatus.FAILED }
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
        private val KEY_SPONSOR_BLOCK = booleanPreferencesKey("sponsor_block")
        private val KEY_SUBTITLE_LANGS = stringPreferencesKey("subtitle_langs")
        private val KEY_DOWNLOAD_SUBTITLES = booleanPreferencesKey("download_subtitles")
        private val KEY_MAX_CONCURRENT = intPreferencesKey("max_concurrent")
        private val KEY_WIFI_ONLY = booleanPreferencesKey("wifi_only")
        private val KEY_AUTO_RETRY = booleanPreferencesKey("auto_retry")
        private val KEY_THUMB_CACHE = booleanPreferencesKey("thumbnail_cache")
    }
}
