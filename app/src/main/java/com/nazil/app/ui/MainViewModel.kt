package com.nazil.app.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nazil.app.data.AppRepository
import com.nazil.app.data.DownloadItem
import com.nazil.app.data.DownloadStatus
import com.nazil.app.data.MediaKind
import com.nazil.app.data.Quality
import com.nazil.app.data.ThemeMode
import com.nazil.app.download.DownloadEngine
import com.nazil.app.util.SiteDetector
import com.yausername.youtubedl_android.mapper.VideoInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = AppRepository(app)
    private val engine = DownloadEngine(app)

    val history: StateFlow<List<DownloadItem>> = repo.history

    val settings = repo.settings.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        com.nazil.app.data.AppSettings(),
    )

    var urlInput by mutableStateOf("")
        private set
    var fetchedInfo by mutableStateOf<VideoInfo?>(null)
        private set
    var isFetching by mutableStateOf(false)
        private set
    var message by mutableStateOf<String?>(null)
        private set
    var currentQuality by mutableStateOf(Quality.BEST)

    init {
        viewModelScope.launch { repo.loadHistory() }
    }

    fun onUrlChange(value: String) {
        urlInput = value
        fetchedInfo = null
    }

    fun acceptSharedText(text: String?) {
        val url = text?.let { SiteDetector.firstUrlIn(it) ?: it.trim() } ?: return
        if (url.isNotBlank()) {
            urlInput = url
            fetchInfo()
        }
    }

    fun fetchInfo() {
        val url = urlInput.trim()
        if (!SiteDetector.looksLikeUrl(url)) {
            message = "الرابط ما يبدو صحيح"
            return
        }
        viewModelScope.launch {
            isFetching = true
            message = null
            runCatching { engine.fetchInfo(url) }
                .onSuccess { fetchedInfo = it }
                .onFailure { message = "ما كدرت أجيب المعلومات، أكدر أنزّل مباشرة" }
            isFetching = false
        }
    }

    fun startDownload(quality: String = currentQuality) {
        val url = urlInput.trim()
        if (!SiteDetector.looksLikeUrl(url)) {
            message = "لصق رابط أول"
            return
        }
        val info = fetchedInfo
        val site = info?.extractor ?: SiteDetector.detect(url)
        val ext = info?.ext?.lowercase()
        val kind = when {
            quality == Quality.AUDIO -> MediaKind.AUDIO
            ext != null && ext in listOf("jpg", "jpeg", "png", "webp", "gif") -> MediaKind.IMAGE
            else -> MediaKind.VIDEO
        }

        val item = DownloadItem(
            id = UUID.randomUUID().toString(),
            url = url,
            title = info?.title ?: info?.fulltitle ?: "جارٍ التحميل…",
            thumbnail = info?.thumbnail,
            site = site,
            kind = kind,
            formatLabel = Quality.label(quality),
            status = DownloadStatus.QUEUED,
        )

        viewModelScope.launch {
            repo.upsert(item)
            runDownload(item, quality)
        }
    }

    private suspend fun runDownload(item: DownloadItem, quality: String) {
        var lastUpdate = 0L
        var latest = item.copy(status = DownloadStatus.DOWNLOADING)

        repo.upsert(latest)
        try {
            val path = engine.download(item, quality) { progress, eta ->
                val now = System.currentTimeMillis()
                if (now - lastUpdate > 350 || progress >= 0.999f) {
                    lastUpdate = now
                    latest = latest.copy(
                        status = DownloadStatus.DOWNLOADING,
                        progress = progress.coerceIn(0f, 1f),
                        etaSeconds = eta,
                    )
                    viewModelScope.launch { repo.upsert(latest) }
                }
            }
            repo.upsert(
                latest.copy(
                    status = DownloadStatus.COMPLETED,
                    progress = 1f,
                    filePath = path,
                    etaSeconds = -1,
                ),
            )
            message = "✅ خلص التحميل"
        } catch (e: Exception) {
            repo.upsert(
                latest.copy(
                    status = DownloadStatus.FAILED,
                    error = e.message?.take(300) ?: "فشل التحميل",
                ),
            )
            message = "❌ فشل: ${e.message?.take(120) ?: "خطأ"}"
        }
    }

    fun cancel(item: DownloadItem) {
        engine.cancel(item.id)
        viewModelScope.launch {
            repo.upsert(item.copy(status = DownloadStatus.CANCELED))
        }
    }

    fun delete(item: DownloadItem) {
        viewModelScope.launch {
            item.filePath?.let { p -> withContext(Dispatchers.IO) { runCatching { java.io.File(p).delete() } } }
            repo.remove(item.id)
        }
    }

    fun clearCompleted() {
        viewModelScope.launch { repo.clearCompleted() }
    }

    fun setTheme(mode: String) {
        viewModelScope.launch { repo.updateTheme(mode) }
    }

    fun setDefaultQuality(q: String) {
        currentQuality = q
        viewModelScope.launch { repo.updateQuality(q) }
    }

    fun consumeMessage() {
        message = null
    }
}
