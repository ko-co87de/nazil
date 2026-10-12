package com.nazil.app.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import java.io.File

object FileActions {

    private fun uriFor(context: Context, path: String): Uri {
        val file = File(path)
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
    }

    fun mimeFor(path: String): String {
        val ext = path.substringAfterLast('.', "").lowercase()
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)
            ?: when (ext) {
                "mp4", "mkv", "webm" -> "video/*"
                "mp3", "m4a", "opus", "ogg" -> "audio/*"
                "jpg", "jpeg", "png", "webp", "gif" -> "image/*"
                else -> "*/*"
            }
    }

    fun open(context: Context, path: String): Boolean {
        val file = File(path)
        if (!file.exists()) return false
        return runCatching {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uriFor(context, path), mimeFor(path))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        }.getOrDefault(false)
    }

    fun share(context: Context, path: String): Boolean {
        val file = File(path)
        if (!file.exists()) return false
        return runCatching {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeFor(path)
                putExtra(Intent.EXTRA_STREAM, uriFor(context, path))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "مشاركة").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            true
        }.getOrDefault(false)
    }
}
