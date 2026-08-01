package com.reflex.widgethub.playlist

import kotlin.test.Test
import kotlin.test.assertEquals

class PlaylistUrlsTest {
    @Test
    fun builds_watch_url_with_playlist_context() {
        assertEquals(
            "https://www.youtube.com/watch?v=abc123xyz89&list=PLdemo&index=7",
            PlaylistUrls.watch("abc123xyz89", "PLdemo", 7)
        )
    }

    @Test
    fun builds_watch_url_without_playlist() {
        assertEquals(
            "https://www.youtube.com/watch?v=abc123xyz89",
            PlaylistUrls.watch("abc123xyz89", null, 3)
        )
    }

    @Test
    fun resume_prefers_known_video_id() {
        val state = PlaylistResumeState(
            videoId = "abc123xyz89",
            playlistId = "PLdemo",
            index = 4
        )
        assertEquals(
            "https://www.youtube.com/watch?v=abc123xyz89&list=PLdemo&index=4",
            PlaylistUrls.resumeUrl(state)
        )
    }

    @Test
    fun resume_without_video_opens_playlist_page() {
        val state = PlaylistResumeState(playlistId = "PLdemo", index = 4)
        assertEquals(
            "https://www.youtube.com/playlist?list=PLdemo",
            PlaylistUrls.resumeUrl(state)
        )
    }
}

class PlaylistResumeStateTest {
    @Test
    fun episode_label_includes_total_when_known() {
        val state = PlaylistResumeState(videoId = "abc", index = 3, totalCount = 40)
        assertEquals("3 / 40", state.episodeLabel())
        assertEquals("EPISODE", state.episodeCaption())
    }

    @Test
    fun episode_label_without_total() {
        val state = PlaylistResumeState(videoId = "abc", index = 12, totalCount = 0)
        assertEquals("12", state.episodeLabel())
    }
}

class PlaylistEpisodeIndexTest {
    @Test
    fun next_increments_local_counter() {
        assertEquals(201, nextEpisodeIndex(200, 1))
        assertEquals(199, nextEpisodeIndex(200, -1))
        assertEquals(1, nextEpisodeIndex(1, -1))
    }

    @Test
    fun next_respects_total_cap() {
        assertEquals(40, nextEpisodeIndex(40, 1, totalCount = 40))
    }
}
