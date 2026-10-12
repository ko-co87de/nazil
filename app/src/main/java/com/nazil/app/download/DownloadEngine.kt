package com.nazil.app.download

import android.content.Context
import android.os.Environment
import com.nazil.app.data.DownloadItem
import com.nazil.app.data.Quality
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLException
import com.yausername.youtubedl_android.YoutubeDLRequest
import com.yausername.youtubedl_android.mapper.VideoInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class DownloadEngine(private val context: Context) {

    suspend fun fetchInfo(url: String): VideoInfo = withContext(Dispatchers.IO) {
        YoutubeDL.getInstance().getInfo(url)
    }

    suspend fun download(
        item: DownloadItem,
        quality: String,
        onProgress: (Float, Long) -> Unit,
    ): String = withContext(Dispatchers.IO) {
        val outDir = downloadDir()
        val request = YoutubeDLRequest(item.url)
        request.addOption("-o", File(outDir, "%(title).150B [%(id)s].%(ext)s").absolutePath)
        request.addOption("--no-playlist")
        request.addOption("--no-mtime")
        request.addOption("--no-warnings")
        request.addOption("--print", "after_move:filepath")

        when (quality) {
            Quality.AUDIO -> {
                request.addOption("-x")
                request.addOption("--audio-format", "mp3")
                request.addOption("--audio-quality", "0")
            }
            Quality.BEST -> Unit // let yt-dlp choose the best
            else -> {
                val h = quality.toIntOrNull() ?: 720
                request.addOption(
                    "-f",
                    "bestvideo[height<=$h]+bestaudio/best[height<=$h]/best",
                )
            }
        }

        val response = YoutubeDL.getInstance().execute(request, item.id) { progress, eta, _ ->
            onProgress(progress, eta)
        }

        if (response.exitCode != 0) {
            val msg = response.err.ifBlank { response.out }
            throw YoutubeDLException(msg.takeLast(400))
        }

        resolveOutputPath(response.out, outDir)
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
