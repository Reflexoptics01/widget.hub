package com.reflex.widgethub.playlist

import java.net.URI

object PlaylistResumeParser {
    private val urlPattern = Regex("https?://[^\\s]+", RegexOption.IGNORE_CASE)

    fun parse(sharedText: String): PlaylistLink? {
        val candidate = urlPattern.find(sharedText)?.value
            ?.trimEnd('.', ',', '!', ')', ']', '>')
            ?: return null
        val uri = runCatching { URI(candidate) }.getOrNull() ?: return null
        val host = uri.host?.lowercase() ?: return null
        val query = parseQuery(uri.rawQuery.orEmpty())
        val videoId = when {
            host == "youtu.be" -> uri.path.orEmpty().trim('/').takeIf { it.isNotBlank() }
            host.endsWith("youtube.com") -> query["v"]
            else -> null
        } ?: return null
        return PlaylistLink(
            videoId = videoId,
            playlistId = query["list"],
            index = query["index"]?.toIntOrNull()?.coerceAtLeast(1) ?: 1
        )
    }

    private fun parseQuery(rawQuery: String): Map<String, String> = rawQuery
        .split('&')
        .mapNotNull { item ->
            val parts = item.split('=', limit = 2)
            if (parts.size == 2) parts[0] to parts[1] else null
        }
        .toMap()
}
