package com.reflex.widgethub.playlist

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PlaylistResumeParserTest {
    @Test
    fun parses_watch_url_video_playlist_and_index() {
        val link = PlaylistResumeParser.parse(
            "Watch this https://www.youtube.com/watch?v=abc123xyz89&list=PLdemo123&index=300"
        )

        assertEquals("abc123xyz89", link?.videoId)
        assertEquals("PLdemo123", link?.playlistId)
        assertEquals(300, link?.index)
    }

    @Test
    fun parses_short_youtube_url_without_playlist() {
        val link = PlaylistResumeParser.parse("https://youtu.be/abc123xyz89")

        assertEquals("abc123xyz89", link?.videoId)
        assertNull(link?.playlistId)
        assertEquals(1, link?.index)
    }

    @Test
    fun rejects_text_without_a_youtube_video_url() {
        assertNull(PlaylistResumeParser.parse("not a video link"))
    }
}
