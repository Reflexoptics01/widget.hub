package com.reflex.widgethub.playlist

import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

data class PlaylistEpisode(
    val videoId: String,
    val title: String,
    val index: Int
)

/**
 * Resolves the real next/previous playlist video via YouTube's Innertube `next`
 * endpoint so we never open a dummy `v=_` URL (which often shows Unavailable).
 */
object YoutubePlaylistNavigator {
    private const val ENDPOINT = "https://www.youtube.com/youtubei/v1/next?prettyPrint=false"
    private const val CLIENT_VERSION = "2.20240620.01.00"

    fun resolveNeighbor(
        playlistId: String,
        currentVideoId: String,
        direction: Int
    ): PlaylistEpisode? {
        if (playlistId.isBlank() || currentVideoId.isBlank()) return null
        val root = postNext(playlistId, currentVideoId) ?: return null
        val episodes = extractPanelEpisodes(root)
        if (episodes.isEmpty()) return null

        val currentPos = episodes.indexOfFirst { it.videoId == currentVideoId }
            .takeIf { it >= 0 }
            ?: episodes.indexOfFirst { it.selected }
                .takeIf { it >= 0 }
            ?: return null

        val targetPos = currentPos + direction
        if (targetPos !in episodes.indices) return null
        val target = episodes[targetPos]
        // Index from YouTube panel is unreliable vs the user's tracked EP number.
        // Caller should apply local currentIndex + direction.
        return PlaylistEpisode(
            videoId = target.videoId,
            title = target.title.ifBlank { "Episode" },
            index = 0
        )
    }

    private fun postNext(playlistId: String, videoId: String): JSONObject? {
        val body = JSONObject()
            .put(
                "context",
                JSONObject().put(
                    "client",
                    JSONObject()
                        .put("clientName", "WEB")
                        .put("clientVersion", CLIENT_VERSION)
                        .put("hl", "en")
                        .put("gl", "US")
                )
            )
            .put("videoId", videoId)
            .put("playlistId", playlistId)
            .toString()

        val connection = (URL(ENDPOINT).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 8000
            readTimeout = 10000
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 Chrome/124.0.0.0 Mobile Safari/537.36"
            )
        }
        return try {
            OutputStreamWriter(connection.outputStream).use { it.write(body) }
            if (connection.responseCode !in 200..299) return null
            val text = connection.inputStream.bufferedReader().use(BufferedReader::readText)
            JSONObject(text)
        } catch (_: Exception) {
            null
        } finally {
            connection.disconnect()
        }
    }

    private data class PanelItem(
        val videoId: String,
        val title: String,
        val index: Int,
        val selected: Boolean
    )

    private fun extractPanelEpisodes(root: JSONObject): List<PanelItem> {
        val found = mutableListOf<PanelItem>()
        walk(root) { obj ->
            val renderer = obj.optJSONObject("playlistPanelVideoRenderer") ?: return@walk
            val videoId = renderer.optJSONObject("navigationEndpoint")
                ?.optJSONObject("watchEndpoint")
                ?.optString("videoId")
                ?.takeIf { it.length == 11 }
                ?: renderer.optString("videoId").takeIf { it.length == 11 }
                ?: return@walk
            val title = renderer.optJSONObject("title")?.optString("simpleText").orEmpty()
                .ifBlank {
                    renderer.optJSONObject("title")
                        ?.optJSONObject("accessibility")
                        ?.optJSONObject("accessibilityData")
                        ?.optString("label")
                        .orEmpty()
                }
            val index = renderer.optString("index")
                .toIntOrNull()
                ?: renderer.optJSONObject("index")?.optString("simpleText")?.toIntOrNull()
                ?: 0
            val selected = renderer.optBoolean("selected", false)
            found += PanelItem(videoId, title, index, selected)
        }
        return found.distinctBy { it.videoId }
    }

    private fun walk(value: Any?, visit: (JSONObject) -> Unit) {
        when (value) {
            is JSONObject -> {
                visit(value)
                val keys = value.keys()
                while (keys.hasNext()) {
                    walk(value.opt(keys.next()), visit)
                }
            }
            is JSONArray -> {
                for (i in 0 until value.length()) walk(value.opt(i), visit)
            }
        }
    }
}
