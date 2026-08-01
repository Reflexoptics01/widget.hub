package com.reflex.widgethub.playlist

/**
 * Pulls a playlist id from a full URL, share text, or a raw id like `PLxxxx`.
 * Vanced/YouTube often share `youtu.be/VIDEO?si=…` with no `list=` — the playlist
 * must then come from a separate playlist share or a pasted id.
 */
object PlaylistIdExtractor {
    private val listQuery = Regex("""(?:[?&]|/)list=([a-zA-Z0-9_-]+)""", RegexOption.IGNORE_CASE)
    private val rawId = Regex("""^(?:PL|UU|RD|OL|LL|WL)[a-zA-Z0-9_-]+$""", RegexOption.IGNORE_CASE)

    fun extract(input: String): String? {
        val trimmed = input.trim()
        if (trimmed.isBlank()) return null
        listQuery.find(trimmed)?.groupValues?.getOrNull(1)?.let { return it }
        if (rawId.matches(trimmed)) return trimmed
        return null
    }
}
