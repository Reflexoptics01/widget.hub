package com.reflex.widgethub.playlist

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.net.HttpURLConnection
import java.net.URL

class PlaylistResumeShareActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedText = intent.getStringExtra(android.content.Intent.EXTRA_TEXT).orEmpty()
        val link = PlaylistResumeParser.parse(sharedText)
        if (link == null) {
            toast("Not a YouTube video or playlist link")
            finish()
            return
        }

        val title = intent.getStringExtra(android.content.Intent.EXTRA_TITLE).orEmpty()
        val store = PlaylistResumeStore(this)
        val sharedUrl = extractUrl(sharedText)
        val saved = store.saveShared(link, title, sharedUrl)
        PlaylistResumeWidgetProvider.refreshAllWidgets(this)

        when {
            saved.hasPlaylist && saved.hasVideo ->
                toast("Saved episode ${saved.index} · Next enabled")
            saved.hasPlaylist && !saved.hasVideo ->
                toast("Playlist linked · set episode number in app")
            saved.hasVideo && !saved.hasPlaylist ->
                toast("Video saved · paste playlist URL in app for Next")
            else -> toast("Saved")
        }

        if (saved.videoId.isBlank()) {
            finish()
            return
        }
        Thread {
            downloadThumbnail(store, saved.videoId)
            runOnUiThread {
                PlaylistResumeWidgetProvider.refreshAllWidgets(this)
                finish()
            }
        }.start()
    }

    private fun downloadThumbnail(store: PlaylistResumeStore, videoId: String) {
        val connection = runCatching {
            (URL("https://i.ytimg.com/vi/$videoId/hqdefault.jpg").openConnection() as HttpURLConnection).apply {
                connectTimeout = 5000
                readTimeout = 5000
                requestMethod = "GET"
            }
        }.getOrNull() ?: return
        try {
            if (connection.responseCode in 200..299) {
                connection.inputStream.use { input ->
                    BitmapFactory.decodeStream(input)?.let(store::saveThumbnail)
                }
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun extractUrl(text: String): String = Regex("https?://[^\\s]+", RegexOption.IGNORE_CASE)
        .find(text)
        ?.value
        ?.trimEnd('.', ',', '!', ')', ']', '>')
        .orEmpty()

    private fun toast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
    }
}
