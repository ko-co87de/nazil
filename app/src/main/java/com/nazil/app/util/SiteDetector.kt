package com.nazil.app.util

import java.net.URI

object SiteDetector {

    private val patterns = listOf(
        "youtu" to "YouTube",
        "instagram" to "Instagram",
        "tiktok" to "TikTok",
        "twitter" to "X (Twitter)",
        "x.com" to "X (Twitter)",
        "facebook" to "Facebook",
        "fb.watch" to "Facebook",
        "snapchat" to "Snapchat",
        "reddit" to "Reddit",
        "pinterest" to "Pinterest",
        "vimeo" to "Vimeo",
        "dailymotion" to "Dailymotion",
        "twitch" to "Twitch",
        "linkedin" to "LinkedIn",
        "t.me" to "Telegram",
        "telegram" to "Telegram",
        "soundcloud" to "SoundCloud",
        "bilibili" to "Bilibili",
        "imgur" to "Imgur",
        "flickr" to "Flickr",
    )

    fun detect(url: String): String {
        val host = hostOf(url)
        if (host.isBlank()) return "Unknown"
        patterns.forEach { (needle, name) ->
            if (host.contains(needle)) return name
        }
        return host.removePrefix("www.")
    }

    fun hostOf(url: String): String =
        runCatching { URI(url.trim()).host ?: "" }.getOrDefault("").lowercase()

    fun looksLikeUrl(text: String): Boolean {
        val t = text.trim()
        if (t.length < 8 || t.contains(' ')) return false
        return t.startsWith("http://") || t.startsWith("https://")
    }

    fun firstUrlIn(text: String): String? {
        val regex = Regex("""https?://\S+""")
        return regex.find(text)?.value
    }
}
