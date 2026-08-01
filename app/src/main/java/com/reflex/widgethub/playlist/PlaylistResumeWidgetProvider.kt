package com.reflex.widgethub.playlist

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.RemoteViews
import android.widget.Toast
import com.reflex.widgethub.R
import java.net.HttpURLConnection
import java.net.URL

class PlaylistResumeWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        ids.forEach { updateWidget(context, manager, it) }
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_NEXT, ACTION_PREV -> {
                val pending = goAsync()
                val direction = if (intent.action == ACTION_NEXT) 1 else -1
                Thread {
                    try {
                        navigate(context, direction)
                    } finally {
                        pending.finish()
                    }
                }.start()
            }
            ACTION_RESUME -> {
                super.onReceive(context, intent)
                val state = PlaylistResumeStore(context).load()
                if (!state.hasSaved) {
                    toast(context, "Share a video from Vanced first")
                    return
                }
                openUrl(context, PlaylistUrls.resumeUrl(state))
                refreshAllWidgets(context)
            }
            ACTION_CLEAR -> {
                super.onReceive(context, intent)
                PlaylistResumeStore(context).clear()
                refreshAllWidgets(context)
            }
            else -> super.onReceive(context, intent)
        }
    }

    private fun navigate(context: Context, direction: Int) {
        val store = PlaylistResumeStore(context)
        val current = store.load()
        if (!current.hasPlaylist) {
            toast(context, "Paste playlist URL in the app first")
            return
        }
        if (!current.hasVideo) {
            toast(context, "Share the current episode video once, then Next works")
            return
        }

        val episode = YoutubePlaylistNavigator.resolveNeighbor(
            playlistId = current.playlistId,
            currentVideoId = current.videoId,
            direction = direction
        )
        if (episode == null) {
            toast(
                context,
                if (direction > 0) "Couldn't load next episode (end or network)"
                else "Couldn't load previous episode"
            )
            return
        }

        val index = nextEpisodeIndex(current.index, direction, current.totalCount)
        val applied = store.applyEpisode(
            episode.copy(
                index = index,
                title = episode.title.ifBlank { "Episode $index" }
            )
        )
        openUrl(context, applied.url)
        downloadThumbnail(store, applied.videoId)
        refreshAllWidgets(context)
    }

    companion object {
        const val ACTION_RESUME = "com.reflex.widgethub.playlist.RESUME"
        const val ACTION_NEXT = "com.reflex.widgethub.playlist.NEXT"
        const val ACTION_PREV = "com.reflex.widgethub.playlist.PREV"
        const val ACTION_CLEAR = "com.reflex.widgethub.playlist.CLEAR"

        private val YOUTUBE_PACKAGES = listOf(
            "app.revanced.android.youtube",
            "com.vanced.android.youtube",
            "com.google.android.youtube"
        )

        private val mainHandler = Handler(Looper.getMainLooper())

        fun refreshAllWidgets(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, PlaylistResumeWidgetProvider::class.java)
            manager.getAppWidgetIds(component).forEach { updateWidget(context, manager, it) }
        }

        private fun updateWidget(context: Context, manager: AppWidgetManager, id: Int) {
            val state = PlaylistResumeStore(context).load()
            val views = RemoteViews(context.packageName, R.layout.widget_playlist_resume)

            if (state.hasSaved) {
                views.setTextViewText(R.id.playlist_caption, state.episodeCaption())
                views.setTextViewText(R.id.playlist_index, state.episodeLabel())
                views.setTextViewText(R.id.playlist_title, state.title.ifBlank { "Episode ${state.index}" })
                views.setTextViewText(
                    R.id.playlist_hint,
                    when {
                        state.canNavigatePlaylist -> "Next opens the real next episode"
                        state.hasPlaylist -> "Share this episode video to enable Next"
                        else -> "Paste playlist URL in app"
                    }
                )
                views.setViewVisibility(R.id.playlist_controls, View.VISIBLE)
                views.setViewVisibility(R.id.playlist_clear, View.VISIBLE)
            } else {
                views.setTextViewText(R.id.playlist_caption, context.getString(R.string.playlist_ready))
                views.setTextViewText(R.id.playlist_index, "—")
                views.setTextViewText(R.id.playlist_title, context.getString(R.string.playlist_empty_title))
                views.setTextViewText(R.id.playlist_hint, context.getString(R.string.playlist_empty_hint))
                views.setViewVisibility(R.id.playlist_controls, View.GONE)
                views.setViewVisibility(R.id.playlist_clear, View.GONE)
            }

            views.setImageViewResource(R.id.playlist_art, R.drawable.ic_tadabbur_icon)
            if (state.thumbnailPath.isNotBlank()) {
                runCatching { BitmapFactory.decodeFile(state.thumbnailPath) }
                    .getOrNull()
                    ?.let { views.setImageViewBitmap(R.id.playlist_art, it) }
            }

            views.setOnClickPendingIntent(R.id.playlist_root, broadcast(context, ACTION_RESUME, id))
            views.setOnClickPendingIntent(R.id.playlist_resume, broadcast(context, ACTION_RESUME, id))
            views.setOnClickPendingIntent(R.id.playlist_next, broadcast(context, ACTION_NEXT, id))
            views.setOnClickPendingIntent(R.id.playlist_prev, broadcast(context, ACTION_PREV, id))
            views.setOnClickPendingIntent(R.id.playlist_clear, broadcast(context, ACTION_CLEAR, id))
            manager.updateAppWidget(id, views)
        }

        private fun broadcast(context: Context, action: String, widgetId: Int): PendingIntent {
            val intent = Intent(context, PlaylistResumeWidgetProvider::class.java)
                .setAction(action)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            return PendingIntent.getBroadcast(
                context,
                action.hashCode() + widgetId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        private fun openUrl(context: Context, url: String) {
            if (url.isBlank()) return
            val uri = Uri.parse(url)
            val pm = context.packageManager
            for (pkg in YOUTUBE_PACKAGES) {
                if (!isInstalled(pm, pkg)) continue
                val intent = Intent(Intent.ACTION_VIEW, uri)
                    .setPackage(pkg)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                if (runCatching { context.startActivity(intent); true }.getOrDefault(false)) return
            }
            val fallback = Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            runCatching { context.startActivity(fallback) }
                .onFailure { toast(context, "No app can open this YouTube link") }
        }

        private fun downloadThumbnail(store: PlaylistResumeStore, videoId: String) {
            if (videoId.isBlank()) return
            runCatching {
                val connection = (URL("https://i.ytimg.com/vi/$videoId/hqdefault.jpg").openConnection() as HttpURLConnection).apply {
                    connectTimeout = 5000
                    readTimeout = 5000
                    requestMethod = "GET"
                }
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
        }

        private fun isInstalled(pm: PackageManager, packageName: String): Boolean =
            runCatching { pm.getPackageInfo(packageName, 0); true }.getOrDefault(false)

        private fun toast(context: Context, message: String) {
            mainHandler.post {
                Toast.makeText(context.applicationContext, message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
