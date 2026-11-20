package com.nazil.app.data

import kotlinx.serialization.Serializable

@Serializable
enum class MediaKind { VIDEO, AUDIO, IMAGE }

@Serializable
enum class DownloadStatus { QUEUED, FETCHING, DOWNLOADING, MERGING, COMPLETED, FAILED, CANCELED }

@Serializable
data class DownloadItem(
    val id: String,
    val url: String,
    val title: String,
    val thumbnail: String? = null,
    val uploader: String? = null,
    val durationSeconds: Long = 0,
    val site: String = "Unknown",
    val kind: MediaKind = MediaKind.VIDEO,
    val formatLabel: String = "Best",
    val formatId: String? = null,
    val filePath: String? = null,
    val status: DownloadStatus = DownloadStatus.QUEUED,
    val progress: Float = 0f,
    val speedBytesPerSec: Long = 0L,
    val etaSeconds: Long = -1,
    val sizeBytes: Long = 0,
    val totalBytes: Long = 0,
    val error: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val withSubtitles: Boolean = false,
    val sponsorBlockApplied: Boolean = false,
) {
    val isActive: Boolean
        get() = status in setOf(
            DownloadStatus.QUEUED,
            DownloadStatus.FETCHING,
            DownloadStatus.DOWNLOADING,
            DownloadStatus.MERGING,
        )

    val isDone: Boolean get() = status == DownloadStatus.COMPLETED

    val progressPercent: Int get() = (progress * 100).toInt().coerceIn(0, 100)

    val displaySize: String
        get() = formatBytes(totalBytes.coerceAtLeast(sizeBytes))
}

@Serializable
data class AppSettings(
    val themeMode: String = ThemeMode.SYSTEM,
    val defaultQuality: String = Quality.BEST,
    val sponsorBlock: Boolean = true,
    val subtitleLangs: String = "ar,en",
    val downloadSubtitles: Boolean = false,
    val maxConcurrent: Int = 2,
    val downloadOverWifiOnly: Boolean = false,
    val autoRetry: Boolean = true,
    val thumbnailCache: Boolean = true,
)

@Serializable
data class FormatOption(
    val formatId: String,
    val ext: String,
    val resolution: String = "audio",
    val fps: Int? = null,
    val vcodec: String? = null,
    val acodec: String? = null,
    val filesize: Long = 0,
    val tbr: Double? = null,
    val isAudioOnly: Boolean = false,
    val isVideoOnly: Boolean = false,
    val note: String? = null,
) {
    val sizeLabel: String get() = formatBytes(filesize)
    val label: String
        get() = buildString {
            append(resolution)
            if (fps != null) append("@${fps}")
            append(" • $ext")
            if (tbr != null) append(" • ${tbr.toInt()}k")
        }
}

object ThemeMode {
    const val SYSTEM = "system"
    const val LIGHT = "light"
    const val DARK = "dark"
    val all = listOf(SYSTEM, LIGHT, DARK)
}

object Quality {
    const val BEST = "best"
    const val FHD = "1080"
    const val HD = "720"
    const val SD = "480"
    const val AUDIO = "audio"
    const val CUSTOM = "custom"
    val all = listOf(BEST, FHD, HD, SD, AUDIO)

    fun label(q: String): String = when (q) {
        BEST -> "أفضل جودة"
        FHD -> "1080p"
        HD -> "720p"
        SD -> "480p"
        AUDIO -> "صوت MP3"
        else -> q
    }
}

object MediaFilter {
    const val ALL = "all"
    const val VIDEO = "video"
    const val AUDIO = "audio"
    const val IMAGE = "image"
    const val COMPLETED = "completed"
    const val FAILED = "failed"

    val all = listOf(ALL, VIDEO, AUDIO, IMAGE, COMPLETED, FAILED)

    fun label(f: String): String = when (f) {
        ALL -> "الكل"
        VIDEO -> "فيديو"
        AUDIO -> "صوت"
        IMAGE -> "صور"
        COMPLETED -> "مكتمل"
        FAILED -> "فشل"
        else -> f
    }
}

fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "—"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    var size = bytes.toDouble()
    var idx = 0
    while (size >= 1024.0 && idx < units.lastIndex) {
        size /= 1024.0
        idx++
    }
    val fmt = if (idx == 0) "%.0f" else "%.1f"
    return "${fmt.format(size)} ${units[idx]}"
}

fun formatSpeed(bytesPerSec: Long): String {
    if (bytesPerSec <= 0) return "—"
    return "${formatBytes(bytesPerSec)}/ث"
}

fun formatDuration(seconds: Long): String {
    if (seconds <= 0) return "—"
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%d:%02d".format(m, s)
}

fun formatEta(seconds: Long): String {
    if (seconds <= 0) return "—"
    val m = seconds / 60
    val s = seconds % 60
    return if (m > 0) "${m}د ${s}ث" else "${s}ث"
}
