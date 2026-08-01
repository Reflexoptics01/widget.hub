package com.reflex.widgethub.playlist

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PlaylistResumeParserTest {
    @Test
    fun parses_watch_url_video_playlist_and_index() {
        val link = PlaylistResumeParser.parse(
            "Watch this https://www.youtube.com/watch?v=abc123xyz89&list=PLdemo123&index=300"
        )

        assertEquals("abc123xyz89", link?.videoId)
        assertEquals("PLdemo123", link?.playlistId)
        assertEquals(300, link?.index)
        assertTrue(link?.indexFromUrl == true)
    }

    @Test
    fun parses_short_youtube_url_without_playlist() {
        val link = PlaylistResumeParser.parse("https://youtu.be/abc123xyz89?si=tracking")

        assertEquals("abc123xyz89", link?.videoId)
        assertNull(link?.playlistId)
        assertEquals(1, link?.index)
        assertFalse(link?.indexFromUrl == true)
    }

    @Test
    fun parses_playlist_page_without_video() {
        val link = PlaylistResumeParser.parse(
            "https://www.youtube.com/playlist?list=PLdemo123&si=abc"
        )

        assertEquals("", link?.videoId)
        assertEquals("PLdemo123", link?.playlistId)
    }

    @Test
    fun parses_youtu_be_with_playlist_and_index() {
        val link = PlaylistResumeParser.parse(
            "https://youtu.be/abc123xyz89?list=PLdemo123&index=12"
        )

        assertEquals("abc123xyz89", link?.videoId)
        assertEquals("PLdemo123", link?.playlistId)
        assertEquals(12, link?.index)
        assertTrue(link?.indexFromUrl == true)
    }

    @Test
    fun parses_shorts_url() {
        val link = PlaylistResumeParser.parse(
            "https://www.youtube.com/shorts/abc123xyz89?list=PLdemo123&index=4"
        )

        assertEquals("abc123xyz89", link?.videoId)
        assertEquals("PLdemo123", link?.playlistId)
        assertEquals(4, link?.index)
    }

    @Test
    fun parses_raw_playlist_id() {
        val link = PlaylistResumeParser.parse("PLdemo123XYZ")
        assertEquals("PLdemo123XYZ", link?.playlistId)
        assertEquals("", link?.videoId)
    }

    @Test
    fun rejects_text_without_a_youtube_video_url() {
        assertNull(PlaylistResumeParser.parse("not a video link"))
    }
}

class PlaylistIdExtractorTest {
    @Test
    fun extracts_from_playlist_url() {
        assertEquals(
            "PLdemo123",
            PlaylistIdExtractor.extract("https://youtube.com/playlist?list=PLdemo123&si=x")
        )
    }

    @Test
    fun extracts_raw_id() {
        assertEquals("PLdemo123", PlaylistIdExtractor.extract("PLdemo123"))
    }
}
