package com.reflex.widgethub.playlist

import android.content.Context
import android.graphics.Bitmap
import java.io.File
import java.io.FileOutputStream

data class PlaylistResumeState(
    val url: String = "",
    val title: String = "",
    val videoId: String = "",
    val playlistId: String = "",
    val index: Int = 1,
    /** Optional episode total for the EP N / total label only (not a scrubber). */
    val totalCount: Int = 0,
    val thumbnailPath: String = ""
) {
    val hasVideo: Boolean get() = videoId.isNotBlank()
    val hasPlaylist: Boolean get() = playlistId.isNotBlank()
    val hasSaved: Boolean get() = hasVideo || hasPlaylist || url.isNotBlank()
    val canNavigatePlaylist: Boolean get() = hasPlaylist && hasVideo

    fun episodeLabel(): String = when {
        totalCount > 0 -> "$index / $totalCount"
        hasSaved -> "$index"
        else -> "—"
    }

    fun episodeCaption(): String = when {
        totalCount > 0 -> "EPISODE"
        hasSaved -> "EPISODE"
        else -> "READY"
    }
}

class PlaylistResumeStore(context: Context) {
    private val appContext = context.applicationContext
    private val preferences = appContext.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)

    fun load(): PlaylistResumeState = PlaylistResumeState(
        url = preferences.getString(KEY_URL, "").orEmpty(),
        title = preferences.getString(KEY_TITLE, "").orEmpty(),
        videoId = preferences.getString(KEY_VIDEO_ID, "").orEmpty(),
        playlistId = preferences.getString(KEY_PLAYLIST_ID, "").orEmpty(),
        index = preferences.getInt(KEY_INDEX, 1).coerceAtLeast(1),
        totalCount = preferences.getInt(KEY_TOTAL, 0).coerceAtLeast(0),
        thumbnailPath = preferences.getString(KEY_THUMBNAIL, "").orEmpty()
    )

    fun saveShared(link: PlaylistLink, title: String, sharedUrl: String): PlaylistResumeState {
        val existing = load()
        // Vanced often shares youtu.be without list= — keep a previously saved playlist id.
        val playlistId = link.playlistId?.takeIf { it.isNotBlank() } ?: existing.playlistId
        val videoId = link.videoId.ifBlank { existing.videoId }
        val index = when {
            link.indexFromUrl -> link.index
            existing.hasSaved && playlistId.isNotBlank() &&
                playlistId == (link.playlistId ?: existing.playlistId) -> existing.index
            videoId.isNotBlank() && videoId == existing.videoId -> existing.index
            else -> preferences.getInt(KEY_START_INDEX, 1).coerceAtLeast(1)
        }
        val url = when {
            videoId.isNotBlank() -> PlaylistUrls.watch(videoId, playlistId.takeIf { it.isNotBlank() }, index)
            playlistId.isNotBlank() -> "https://www.youtube.com/playlist?list=$playlistId"
            else -> sharedUrl
        }
        val state = PlaylistResumeState(
            url = url,
            title = title.ifBlank {
                when {
                    videoId.isNotBlank() -> "Episode $index"
                    playlistId.isNotBlank() -> "Playlist ready"
                    else -> "Saved"
                }
            },
            videoId = videoId,
            playlistId = playlistId,
            index = index,
            totalCount = existing.totalCount,
            thumbnailPath = if (videoId.isNotBlank() && videoId != existing.videoId) "" else existing.thumbnailPath
        )
        save(state)
        return state
    }

    fun setPlaylistId(raw: String): PlaylistResumeState {
        val playlistId = PlaylistIdExtractor.extract(raw).orEmpty()
        val current = load()
        val updated = current.copy(
            playlistId = playlistId,
            url = when {
                current.videoId.isNotBlank() ->
                    PlaylistUrls.watch(current.videoId, playlistId.takeIf { it.isNotBlank() }, current.index)
                playlistId.isNotBlank() ->
                    "https://www.youtube.com/playlist?list=$playlistId"
                else -> current.url
            }
        )
        save(updated)
        return updated
    }

    fun setCurrentIndex(index: Int): PlaylistResumeState {
        val safeIndex = index.coerceAtLeast(1)
        preferences.edit().putInt(KEY_START_INDEX, safeIndex).apply()
        val current = load()
        if (!current.hasSaved) {
            return current.copy(index = safeIndex)
        }
        val updated = current.copy(
            index = safeIndex,
            url = PlaylistUrls.openUrlForIndex(current, safeIndex)
        )
        save(updated)
        return updated
    }

    fun setTotalCount(total: Int): PlaylistResumeState {
        val safeTotal = total.coerceAtLeast(0)
        val current = load()
        val updated = current.copy(totalCount = safeTotal)
        save(updated)
        return updated
    }

    fun applyEpisode(episode: PlaylistEpisode): PlaylistResumeState {
        val current = load()
        val index = episode.index.coerceAtLeast(1)
        val updated = current.copy(
            videoId = episode.videoId,
            title = episode.title.ifBlank { "Episode $index" },
            index = index,
            url = PlaylistUrls.watch(
                episode.videoId,
                current.playlistId.takeIf { it.isNotBlank() },
                index
            ),
            thumbnailPath = ""
        )
        save(updated)
        return updated
    }

    fun clear() {
        val thumbnail = load().thumbnailPath
        if (thumbnail.isNotBlank()) runCatching { File(thumbnail).delete() }
        val startIndex = preferences.getInt(KEY_START_INDEX, 1)
        val total = preferences.getInt(KEY_TOTAL, 0)
        preferences.edit().clear().apply()
        preferences.edit()
            .putInt(KEY_START_INDEX, startIndex.coerceAtLeast(1))
            .putInt(KEY_TOTAL, total.coerceAtLeast(0))
            .apply()
    }

    fun saveThumbnail(bitmap: Bitmap) {
        val file = File(appContext.filesDir, THUMBNAIL_FILE)
        FileOutputStream(file).use { output -> bitmap.compress(Bitmap.CompressFormat.JPEG, 88, output) }
        preferences.edit().putString(KEY_THUMBNAIL, file.absolutePath).apply()
    }

    private fun save(state: PlaylistResumeState) {
        preferences.edit()
            .putString(KEY_URL, state.url)
            .putString(KEY_TITLE, state.title)
            .putString(KEY_VIDEO_ID, state.videoId)
            .putString(KEY_PLAYLIST_ID, state.playlistId)
            .putInt(KEY_INDEX, state.index)
            .putInt(KEY_TOTAL, state.totalCount)
            .putString(KEY_THUMBNAIL, state.thumbnailPath)
            .apply()
    }

    private companion object {
        const val PREFERENCES = "playlist_resume_v1"
        const val KEY_URL = "url"
        const val KEY_TITLE = "title"
        const val KEY_VIDEO_ID = "video_id"
        const val KEY_PLAYLIST_ID = "playlist_id"
        const val KEY_INDEX = "index"
        const val KEY_START_INDEX = "start_index"
        const val KEY_TOTAL = "total_count"
        const val KEY_THUMBNAIL = "thumbnail"
        const val THUMBNAIL_FILE = "playlist_resume_thumbnail.jpg"
    }
}
