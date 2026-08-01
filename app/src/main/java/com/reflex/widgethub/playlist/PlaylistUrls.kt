package com.reflex.widgethub.playlist

/**
 * YouTube playlist position is carried in the watch URL as:
 *   watch?v=VIDEO_ID&list=PLAYLIST_ID&index=N   (index is 1-based)
 *
 * Always open a real video id. Dummy `v=_` jump URLs are unreliable on
 * mobile/Vanced and often show "Video unavailable".
 */
object PlaylistUrls {
    fun watch(videoId: String, playlistId: String?, index: Int): String {
        val safeIndex = index.coerceAtLeast(1)
        if (playlistId.isNullOrBlank()) {
            return "https://www.youtube.com/watch?v=$videoId"
        }
        return "https://www.youtube.com/watch?v=$videoId&list=$playlistId&index=$safeIndex"
    }

    fun resumeUrl(state: PlaylistResumeState): String = when {
        state.videoId.isNotBlank() -> watch(
            videoId = state.videoId,
            playlistId = state.playlistId.takeIf { it.isNotBlank() },
            index = state.index
        )
        state.playlistId.isNotBlank() -> "https://www.youtube.com/playlist?list=${state.playlistId}"
        else -> state.url
    }

    fun openUrlForIndex(state: PlaylistResumeState, index: Int): String {
        val safeIndex = index.coerceAtLeast(1)
        return when {
            state.videoId.isNotBlank() ->
                watch(state.videoId, state.playlistId.takeIf { it.isNotBlank() }, safeIndex)
            state.playlistId.isNotBlank() ->
                "https://www.youtube.com/playlist?list=${state.playlistId}"
            else -> state.url
        }
    }
}
