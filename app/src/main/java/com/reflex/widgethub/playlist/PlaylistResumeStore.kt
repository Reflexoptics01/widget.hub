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
    val thumbnailPath: String = ""
) {
    val hasVideo: Boolean get() = videoId.isNotBlank()
    val hasPlaylist: Boolean get() = playlistId.isNotBlank()

    fun nextUrl(): String? = playlistId.takeIf { it.isNotBlank() }?.let {
        "https://www.youtube.com/playlist?list=$it&index=${index + 1}"
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
        thumbnailPath = preferences.getString(KEY_THUMBNAIL, "").orEmpty()
    )

    fun saveShared(link: PlaylistLink, title: String, sharedUrl: String): PlaylistResumeState {
        val startingIndex = link.index.takeIf { it > 1 } ?: preferences.getInt(KEY_START_INDEX, 1)
        val state = PlaylistResumeState(
            url = sharedUrl,
            title = title.ifBlank { "Saved episode" },
            videoId = link.videoId,
            playlistId = link.playlistId.orEmpty(),
            index = startingIndex.coerceAtLeast(1),
            thumbnailPath = ""
        )
        save(state)
        return state
    }

    fun setStartingIndex(index: Int): PlaylistResumeState {
        val safeIndex = index.coerceAtLeast(1)
        preferences.edit().putInt(KEY_START_INDEX, safeIndex).apply()
        val current = load()
        if (!current.hasVideo) return current.copy(index = safeIndex)
        val updated = current.copy(index = safeIndex)
        save(updated)
        return updated
    }

    fun advance(): PlaylistResumeState {
        val current = load()
        if (!current.hasPlaylist) return current
        val updated = current.copy(index = current.index + 1, url = current.nextUrl().orEmpty())
        save(updated)
        return updated
    }

    fun clear() {
        val thumbnail = load().thumbnailPath
        if (thumbnail.isNotBlank()) runCatching { File(thumbnail).delete() }
        preferences.edit().clear().apply()
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
        const val KEY_THUMBNAIL = "thumbnail"
        const val THUMBNAIL_FILE = "playlist_resume_thumbnail.jpg"
    }
}
