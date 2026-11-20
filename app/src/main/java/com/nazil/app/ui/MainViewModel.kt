package com.nazil.app.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nazil.app.data.AppRepository
import com.nazil.app.data.AppSettings
import com.nazil.app.data.DownloadItem
import com.nazil.app.data.DownloadStatus
import com.nazil.app.data.FormatOption
import com.nazil.app.data.MediaKind
import com.nazil.app.data.Quality
import com.nazil.app.download.DownloadEngine
import com.nazil.app.download.DownloadProgress
import com.nazil.app.util.SiteDetector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.UUID

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = AppRepository(app)
    private val engine = DownloadEngine(app)

    val history: StateFlow<List<DownloadItem>> = repo.history

    val settings = repo.settings.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        AppSettings(),
    )

    var urlInput by mutableStateOf("")
        private set
    var fetchedJson by mutableStateOf<JSONObject?>(null)
        private set
    var availableFormats by mutableStateOf<List<FormatOption>>(emptyList())
        private set
    var isFetching by mutableStateOf(false)
        private set
    var message by mutableStateOf<String?>(null)
        private set
    var currentQuality by mutableStateOf(Quality.BEST)
    var customFormat by mutableStateOf<FormatOption?>(null)
        private set
    var showFormatPicker by mutableStateOf(false)
        private set
    var showLinkCard by mutableStateOf(true)
        private set

    /** Live stats derived from history. */
    val stats: StateFlow<DownloadStats> = history
        .map { items ->
            DownloadStats(
                total = items.size,
                active = items.count { it.isActive },
                completed = items.count { it.isDone },
                failed = items.count { it.status == DownloadStatus.FAILED },
                totalBytes = items.sumOf { it.totalBytes.coerceAtLeast(0) },
                lastActivityAt = items.maxOfOrNull { it.createdAt } ?: 0L,
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DownloadStats())

    /** Tracks active download jobs so we can cancel them. */
    private val activeJobs = mutableMapOf<String, Job>()

    init {
        viewModelScope.launch { repo.loadHistory() }
    }

    fun onUrlChange(value: String) {
        urlInput = value
        fetchedJson = null
        availableFormats = emptyList()
        customFormat = null
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
            runCatching {
                // Single yt-dlp call: --dump-json gives us formats, duration, uploader,
                // thumbnail, title — everything we need. Avoids a second getInfo() call.
                val json = engine.fetchInfoJson(url)
                fetchedJson = json
                availableFormats = engine.extractFormats(json)
            }.onFailure {
                message = "ما كدرت أجيب المعلومات، أكدر أنزّل مباشرة"
            }
            isFetching = false
        }
    }

    fun openFormatPicker() { showFormatPicker = true }
    fun closeFormatPicker() { showFormatPicker = false }

    fun selectCustomFormat(format: FormatOption) {
        customFormat = format
        currentQuality = Quality.CUSTOM
        showFormatPicker = false
    }

    fun clearCustomFormat() {
        customFormat = null
        if (currentQuality == Quality.CUSTOM) currentQuality = Quality.BEST
    }

    fun startDownload(quality: String = currentQuality) {
        val url = urlInput.trim()
        if (!SiteDetector.looksLikeUrl(url)) {
            message = "لصق رابط أول"
            return
        }
        val json = fetchedJson
        // Prefer the fetched JSON for metadata; fall back gracefully if fetch was skipped.
        val site = json?.optString("extractor_key")?.ifBlank { null }
            ?: json?.optString("extractor")?.ifBlank { null }
            ?: SiteDetector.detect(url)
        val ext = json?.optString("ext")?.ifBlank { null }?.lowercase()
        val duration = json?.optLong("duration", 0L) ?: 0L
        val uploader = json?.optString("uploader")?.ifBlank { null }
        val thumbnail = json?.optString("thumbnail")?.ifBlank { null }
        val title = json?.optString("title")?.ifBlank { null }
            ?: json?.optString("fulltitle")?.ifBlank { null }
            ?: "جارٍ التحميل…"

        val kind = when {
            quality == Quality.AUDIO -> MediaKind.AUDIO
            ext != null && ext in listOf("jpg", "jpeg", "png", "webp", "gif") -> MediaKind.IMAGE
            else -> MediaKind.VIDEO
        }

        val customFmt = customFormat
        val formatLabel = when {
            customFmt != null -> customFmt.label
            else -> com.nazil.app.data.Quality.label(quality)
        }

        val item = DownloadItem(
            id = UUID.randomUUID().toString(),
            url = url,
            title = title,
            thumbnail = thumbnail,
            uploader = uploader,
            durationSeconds = duration,
            site = site,
            kind = kind,
            formatLabel = formatLabel,
            formatId = customFmt?.formatId,
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
        val job = viewModelScope.launch {
            try {
                val settingsSnapshot = settings.value
                val path = engine.download(
                    item = item,
                    quality = quality,
                    sponsorBlock = settingsSnapshot.sponsorBlock,
                    subtitleLangs = settingsSnapshot.subtitleLangs,
                    downloadSubtitles = settingsSnapshot.downloadSubtitles,
                    customFormatId = item.formatId,
                ) { p: DownloadProgress ->
                    val now = System.currentTimeMillis()
                    if (now - lastUpdate > 200 || p.progress >= 0.999f) {
                        lastUpdate = now
                        latest = latest.copy(
                            status = if (p.status == "merging") DownloadStatus.MERGING
                            else if (p.progress >= 0.999f) DownloadStatus.COMPLETED
                            else DownloadStatus.DOWNLOADING,
                            progress = p.progress,
                            etaSeconds = p.etaSeconds,
                            speedBytesPerSec = p.speedBytes,
                            sizeBytes = p.sizeBytes,
                            totalBytes = p.totalBytes,
                            sponsorBlockApplied = settingsSnapshot.sponsorBlock,
                            withSubtitles = settingsSnapshot.downloadSubtitles,
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
                        speedBytesPerSec = 0L,
                        completedAt = System.currentTimeMillis(),
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
            } finally {
                activeJobs.remove(item.id)
            }
        }
        activeJobs[item.id] = job
    }

    fun cancel(item: DownloadItem) {
        engine.cancel(item.id)
        activeJobs[item.id]?.cancel()
        activeJobs.remove(item.id)
        viewModelScope.launch {
            repo.upsert(item.copy(status = DownloadStatus.CANCELED))
        }
    }

    fun delete(item: DownloadItem) {
        viewModelScope.launch {
            item.filePath?.let { p ->
                withContext(Dispatchers.IO) { runCatching { java.io.File(p).delete() } }
            }
            repo.remove(item.id)
        }
    }

    fun retry(item: DownloadItem) {
        viewModelScope.launch {
            val fresh = item.copy(
                status = DownloadStatus.QUEUED,
                progress = 0f,
                error = null,
                speedBytesPerSec = 0L,
                etaSeconds = -1,
                sizeBytes = 0L,
                totalBytes = 0L,
            )
            repo.upsert(fresh)
            urlInput = item.url
            // Pick the right quality for retry:
            //  - if the original download used a custom format_id, retry with CUSTOM
            //    and reuse the stored formatId on the item itself.
            //  - otherwise map the media kind to a sensible default quality.
            val q = if (item.formatId != null) Quality.CUSTOM else mapKindToQuality(item.kind)
            runDownload(fresh, q)
        }
    }

    private fun mapKindToQuality(kind: MediaKind): String = when (kind) {
        MediaKind.AUDIO -> Quality.AUDIO
        else -> Quality.BEST
    }

    fun clearCompleted() {
        viewModelScope.launch { repo.clearCompleted() }
    }

    fun clearFailed() {
        viewModelScope.launch { repo.clearFailed() }
    }

    fun clearAll() {
        viewModelScope.launch { repo.clearAll() }
    }

    fun setTheme(mode: String) {
        viewModelScope.launch { repo.updateTheme(mode) }
    }

    fun setDefaultQuality(q: String) {
        currentQuality = q
        if (q != Quality.CUSTOM) customFormat = null
        viewModelScope.launch { repo.updateQuality(q) }
    }

    fun setSponsorBlock(enabled: Boolean) {
        viewModelScope.launch { repo.updateSponsorBlock(enabled) }
    }

    fun setSubtitles(enabled: Boolean, langs: String) {
        viewModelScope.launch { repo.updateSubtitles(enabled, langs) }
    }

    fun setConcurrent(max: Int) {
        viewModelScope.launch { repo.updateConcurrent(max) }
    }

    fun setWifiOnly(enabled: Boolean) {
        viewModelScope.launch { repo.updateWifiOnly(enabled) }
    }

    fun setAutoRetry(enabled: Boolean) {
        viewModelScope.launch { repo.updateAutoRetry(enabled) }
    }

    fun consumeMessage() {
        message = null
    }

    fun usedSpace(): Long = engine.usedSpace()
    fun freeSpace(): Long = engine.freeSpace()
}

data class DownloadStats(
    val total: Int = 0,
    val active: Int = 0,
    val completed: Int = 0,
    val failed: Int = 0,
    val totalBytes: Long = 0L,
    val lastActivityAt: Long = 0L,
) {
    val successRate: Int
        get() {
            val done = completed + failed
            return if (done == 0) 0 else ((completed.toDouble() / done) * 100).toInt()
        }
}
