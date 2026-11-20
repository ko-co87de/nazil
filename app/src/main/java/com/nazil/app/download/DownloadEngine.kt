package com.nazil.app.download

import android.content.Context
import android.os.Environment
import com.nazil.app.data.DownloadItem
import com.nazil.app.data.FormatOption
import com.nazil.app.data.Quality
import com.nazil.app.data.formatBytes
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLException
import com.yausername.youtubedl_android.YoutubeDLRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File

/**
 * A snapshot of progress reported by yt-dlp during a download.
 *
 * @param progress   0f..1f
 * @param etaSeconds remaining seconds, or -1 if unknown
 * @param speedBytes speed in bytes per second, or 0 if unknown
 * @param sizeBytes  downloaded bytes so far
 * @param totalBytes total bytes, or 0 if unknown
 * @param status     yt-dlp phase: "downloading", "merging", "finished", etc.
 */
data class DownloadProgress(
    val progress: Float,
    val etaSeconds: Long,
    val speedBytes: Long,
    val sizeBytes: Long,
    val totalBytes: Long,
    val status: String,
)

class DownloadEngine(private val context: Context) {

    /**
     * Fetch info as a raw JSON map so we can extract formats list, subtitles, etc.
     * yt-dlp returns the info dict; we use --dump-json under the hood.
     * This is the single source of truth for both metadata and formats.
     */
    suspend fun fetchInfoJson(url: String): JSONObject = withContext(Dispatchers.IO) {
        val request = YoutubeDLRequest(url)
        request.addOption("--dump-json")
        request.addOption("--no-playlist")
        request.addOption("--no-warnings")
        val response = YoutubeDL.getInstance().execute(request, "info_${url.hashCode()}")
        if (response.exitCode != 0) {
            throw YoutubeDLException((response.err.ifBlank { response.out }).takeLast(400))
        }
        JSONObject(response.out.trim().lineSequence().last { it.isNotBlank() })
    }

    /** Extract a sorted list of available formats from yt-dlp info JSON. */
    fun extractFormats(info: JSONObject): List<FormatOption> {
        val arr = info.optJSONArray("formats") ?: return emptyList()
        val result = mutableListOf<FormatOption>()
        for (i in 0 until arr.length()) {
            val f = arr.optJSONObject(i) ?: continue
            val formatId = f.optString("format_id")
            if (formatId.isBlank()) continue
            val ext = f.optString("ext", "?")
            val vcodec = f.optString("vcodec").ifBlank { null }
            val acodec = f.optString("acodec").ifBlank { null }
            val height = f.optInt("height", 0)
            val fps = if (f.has("fps") && !f.isNull("fps")) f.optInt("fps") else null
            val tbr = if (f.has("tbr") && !f.isNull("tbr")) f.optDouble("tbr") else null
            val filesize = f.optLong("filesize").let { if (it > 0) it else f.optLong("filesize_approx") }
            val resolution = if (height > 0) "${height}p" else "audio"
            val isAudioOnly = vcodec == null || vcodec == "none"
            val isVideoOnly = acodec == null || acodec == "none"
            val note = f.optString("format_note").ifBlank { null }
            result.add(
                FormatOption(
                    formatId = formatId,
                    ext = ext,
                    resolution = resolution,
                    fps = fps,
                    vcodec = vcodec,
                    acodec = acodec,
                    filesize = filesize,
                    tbr = tbr,
                    isAudioOnly = isAudioOnly,
                    isVideoOnly = isVideoOnly,
                    note = note,
                ),
            )
        }
        // Prefer video+audio combined at higher resolution, then audio-only, then video-only.
        return result.sortedWith(
            compareByDescending<FormatOption> { !it.isAudioOnly && !it.isVideoOnly }
                .thenByDescending { it.filesize }
                .thenByDescending { it.tbr ?: 0.0 }
                .thenByDescending { it.resolution },
        )
    }

    suspend fun download(
        item: DownloadItem,
        quality: String,
        sponsorBlock: Boolean = false,
        subtitleLangs: String = "",
        downloadSubtitles: Boolean = false,
        customFormatId: String? = null,
        onProgress: (DownloadProgress) -> Unit,
    ): String = withContext(Dispatchers.IO) {
        val outDir = downloadDir()
        val request = YoutubeDLRequest(item.url)
        request.addOption("-o", File(outDir, "%(title).150B [%(id)s].%(ext)s").absolutePath)
        request.addOption("--no-playlist")
        request.addOption("--no-mtime")
        request.addOption("--no-warnings")
        request.addOption("--print", "after_move:filepath")
        request.addOption("--newline") // ensure progress is flushed line-by-line

        if (sponsorBlock) {
            // yt-dlp supports SponsorBlock via --sponsorblock-mark/remove (uses sponsor.ajay.app API).
            // We mark sponsor segments (not remove) to keep the video intact but mark sections.
            // For "remove" we'd need a build with SponsorBlock; the lib includes it.
            runCatching {
                request.addOption("--sponsorblock-remove", "sponsor,selfpromo,interaction,intro,outro,preview")
            }
        }

        if (downloadSubtitles && subtitleLangs.isNotBlank()) {
            request.addOption("--write-subs")
            request.addOption("--write-auto-subs")
            request.addOption("--sub-langs", subtitleLangs)
            request.addOption("--embed-subs")
            request.addOption("--convert-subs", "srt")
        }

        when {
            customFormatId != null -> {
                request.addOption("-f", customFormatId)
                request.addOption("--merge-output-format", "mp4")
            }

            quality == Quality.AUDIO -> {
                request.addOption("-x")
                request.addOption("--audio-format", "mp3")
                request.addOption("--audio-quality", "0")
            }

            quality == Quality.BEST -> Unit // let yt-dlp choose the best

            else -> {
                val h = quality.toIntOrNull() ?: 720
                request.addOption(
                    "-f",
                    "bestvideo[height<=$h]+bestaudio/best[height<=$h]/best",
                )
            }
        }

        val response = YoutubeDL.getInstance().execute(request, item.id) { progress, eta, line ->
            val parsed = parseProgressLine(line, progress, eta)
            onProgress(parsed)
        }

        if (response.exitCode != 0) {
            val msg = response.err.ifBlank { response.out }
            throw YoutubeDLException(msg.takeLast(400))
        }

        resolveOutputPath(response.out, outDir)
    }

    /** Parse yt-dlp's progress JSON line for speed/size when available. */
    private fun parseProgressLine(line: String?, progress: Float, eta: Long): DownloadProgress {
        var speedBytes = 0L
        var sizeBytes = 0L
        var totalBytes = 0L
        var status = "downloading"
        if (!line.isNullOrBlank()) {
            runCatching {
                if (line.trimStart().startsWith("{")) {
                    val obj = JSONObject(line.trim())
                    speedBytes = obj.optLong("speed", 0L).coerceAtLeast(0L)
                    sizeBytes = obj.optLong("downloaded_bytes", 0L)
                    totalBytes = obj.optLong("total_bytes", obj.optLong("total_bytes_estimate", 0L))
                    status = obj.optString("_status", obj.optString("status", "downloading"))
                } else {
                    // yt-dlp plain-text progress like "[download]  12.3% of 50.00MiB at 2.50MiB/s ETA 00:10"
                    val speedMatch = Regex("at\\s+([\\d.]+)(\\w+)/s").find(line)
                    val sizeMatch = Regex("of\\s+([\\d.]+)(\\w+)").find(line)
                    speedBytes = parseRate(speedMatch)
                    val total = parseSize(sizeMatch)
                    if (total > 0) totalBytes = total
                }
            }
        }
        if (progress >= 0.999f) status = "finished"
        return DownloadProgress(
            progress = progress.coerceIn(0f, 1f),
            etaSeconds = eta,
            speedBytes = speedBytes,
            sizeBytes = sizeBytes,
            totalBytes = totalBytes,
            status = status,
        )
    }

    private fun parseRate(m: MatchResult?): Long {
        if (m == null) return 0L
        val value = m.groupValues[1].toDoubleOrNull() ?: return 0L
        val unit = m.groupValues[2]
        val mult = when (unit.lowercase()) {
            "kib", "kb", "k" -> 1024.0
            "mib", "mb", "m" -> 1024.0 * 1024
            "gib", "gb", "g" -> 1024.0 * 1024 * 1024
            else -> 1.0
        }
        return (value * mult).toLong()
    }

    private fun parseSize(m: MatchResult?): Long {
        if (m == null) return 0L
        val value = m.groupValues[1].toDoubleOrNull() ?: return 0L
        val unit = m.groupValues[2]
        val mult = when (unit.lowercase()) {
            "kib", "kb", "k" -> 1024.0
            "mib", "mb", "m" -> 1024.0 * 1024
            "gib", "gb", "g" -> 1024.0 * 1024 * 1024
            else -> 1.0
        }
        return (value * mult).toLong()
    }

    fun cancel(processId: String) {
        runCatching { YoutubeDL.getInstance().destroyProcessById(processId) }
    }

    fun downloadDir(): File {
        val base = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val dir = File(base, "Nazil")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    /** Total disk usage of the Nazil download folder, in bytes. */
    fun usedSpace(): Long = downloadDir().walkTopDown().filter { it.isFile }.sumOf { it.length() }

    /** Approximate free space on the Downloads volume, in bytes. */
    fun freeSpace(): Long = downloadDir().freeSpace

    /** Human-readable size of a path string. */
    fun sizeOf(path: String): String = formatBytes(File(path).length().coerceAtLeast(0))

    private fun resolveOutputPath(out: String, dir: File): String {
        out.lineSequence()
            .map { it.trim().trim('"') }
            .lastOrNull { it.startsWith("/") && File(it).exists() }
            ?.let { return it }

        // Fallback: newest file in the download directory
        return dir.listFiles()
            ?.maxByOrNull { it.lastModified() }
            ?.absolutePath
            ?: (dir.absolutePath + "/")
    }
}
