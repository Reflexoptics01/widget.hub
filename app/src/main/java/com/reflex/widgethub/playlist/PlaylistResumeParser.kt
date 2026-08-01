package com.reflex.widgethub.playlist

import java.net.URI
import java.net.URLDecoder

object PlaylistResumeParser {
    private val urlPattern = Regex("https?://[^\\s]+", RegexOption.IGNORE_CASE)

    fun parse(sharedText: String): PlaylistLink? {
        val candidate = urlPattern.find(sharedText)?.value
            ?.trimEnd('.', ',', '!', ')', ']', '>')
            ?: return parsePlaylistIdOnly(sharedText)

        val uri = runCatching { URI(candidate) }.getOrNull() ?: return parsePlaylistIdOnly(sharedText)
        val host = uri.host?.lowercase() ?: return parsePlaylistIdOnly(sharedText)
        if (host != "youtu.be" && !host.endsWith("youtube.com") && host != "music.youtube.com") {
            return parsePlaylistIdOnly(sharedText)
        }

        val query = parseQuery(uri.rawQuery.orEmpty())
        val path = uri.path.orEmpty().trim('/')
        val segments = path.split('/').filter { it.isNotBlank() }
        val listFromUrl = query["list"]?.takeIf { it.isNotBlank() }
        val listFromText = PlaylistIdExtractor.extract(sharedText)
        val playlistId = listFromUrl ?: listFromText
        val indexRaw = query["index"]
        val index = indexRaw?.toIntOrNull()?.coerceAtLeast(1) ?: 1
        val indexFromUrl = !indexRaw.isNullOrBlank()

        // Playlist page: youtube.com/playlist?list=…
        if (segments.firstOrNull() == "playlist" && !playlistId.isNullOrBlank()) {
            return PlaylistLink(
                videoId = "",
                playlistId = playlistId,
                index = index,
                indexFromUrl = indexFromUrl
            )
        }

        val videoId = when {
            host == "youtu.be" -> segments.firstOrNull()?.substringBefore('?')
            segments.firstOrNull() in setOf("shorts", "live", "embed", "v") -> segments.getOrNull(1)
            else -> query["v"]
        }?.takeIf { it.isNotBlank() }

        if (videoId.isNullOrBlank() && playlistId.isNullOrBlank()) {
            return parsePlaylistIdOnly(sharedText)
        }

        if (videoId.isNullOrBlank()) {
            return PlaylistLink(
                videoId = "",
                playlistId = playlistId,
                index = index,
                indexFromUrl = indexFromUrl
            )
        }

        return PlaylistLink(
            videoId = videoId,
            playlistId = playlistId,
            index = index,
            indexFromUrl = indexFromUrl
        )
    }

    private fun parsePlaylistIdOnly(text: String): PlaylistLink? {
        val playlistId = PlaylistIdExtractor.extract(text) ?: return null
        return PlaylistLink(videoId = "", playlistId = playlistId, index = 1, indexFromUrl = false)
    }

    private fun parseQuery(rawQuery: String): Map<String, String> = rawQuery
        .split('&')
        .mapNotNull { item ->
            val parts = item.split('=', limit = 2)
            if (parts.isEmpty() || parts[0].isBlank()) return@mapNotNull null
            val key = decode(parts[0])
            val value = if (parts.size == 2) decode(parts[1]) else ""
            key to value
        }
        .toMap()

    private fun decode(value: String): String =
        runCatching { URLDecoder.decode(value, "UTF-8") }.getOrDefault(value)
}
