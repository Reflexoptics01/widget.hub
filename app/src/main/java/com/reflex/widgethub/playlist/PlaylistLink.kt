package com.reflex.widgethub.playlist

data class PlaylistLink(
    val videoId: String,
    val playlistId: String?,
    val index: Int,
    /** True when the shared URL included an explicit `index=` query param. */
    val indexFromUrl: Boolean = false
)
