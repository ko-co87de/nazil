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
    val site: String = "Unknown",
    val kind: MediaKind = MediaKind.VIDEO,
    val formatLabel: String = "Best",
    val filePath: String? = null,
    val status: DownloadStatus = DownloadStatus.QUEUED,
    val progress: Float = 0f,
    val speed: String? = null,
    val etaSeconds: Long = -1,
    val sizeBytes: Long = 0,
    val error: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
) {
    val isActive: Boolean
        get() = status in setOf(
            DownloadStatus.QUEUED,
            DownloadStatus.FETCHING,
            DownloadStatus.DOWNLOADING,
            DownloadStatus.MERGING,
        )

    val isDone: Boolean get() = status == DownloadStatus.COMPLETED
}

@Serializable
data class AppSettings(
    val themeMode: String = ThemeMode.SYSTEM,
    val defaultQuality: String = Quality.BEST,
)

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
