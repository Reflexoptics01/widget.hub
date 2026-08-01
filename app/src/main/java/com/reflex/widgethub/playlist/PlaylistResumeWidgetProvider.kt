package com.reflex.widgethub.playlist

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.RemoteViews
import com.reflex.widgethub.R

class PlaylistResumeWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        ids.forEach { updateWidget(context, manager, it) }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val store = PlaylistResumeStore(context)
        when (intent.action) {
            ACTION_RESUME -> openUrl(context, store.load().url)
            ACTION_NEXT -> {
                val next = store.advance()
                openUrl(context, next.url)
            }
            ACTION_CLEAR -> store.clear()
            else -> return
        }
        refreshAllWidgets(context)
    }

    companion object {
        const val ACTION_RESUME = "com.reflex.widgethub.playlist.RESUME"
        const val ACTION_NEXT = "com.reflex.widgethub.playlist.NEXT"
        const val ACTION_CLEAR = "com.reflex.widgethub.playlist.CLEAR"

        fun refreshAllWidgets(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, PlaylistResumeWidgetProvider::class.java)
            manager.getAppWidgetIds(component).forEach { updateWidget(context, manager, it) }
        }

        private fun updateWidget(context: Context, manager: AppWidgetManager, id: Int) {
            val state = PlaylistResumeStore(context).load()
            val views = RemoteViews(context.packageName, R.layout.widget_playlist_resume)
            views.setTextViewText(R.id.playlist_title, state.title.ifBlank { "SHARE A VIDEO TO START" })
            views.setTextViewText(R.id.playlist_index, if (state.hasVideo) "EP ${state.index}" else "READY")
            views.setImageViewResource(R.id.playlist_art, R.drawable.ic_tadabbur_icon)
            if (state.thumbnailPath.isNotBlank()) {
                runCatching { BitmapFactory.decodeFile(state.thumbnailPath) }
                    .getOrNull()
                    ?.let { views.setImageViewBitmap(R.id.playlist_art, it) }
            }
            views.setProgressBar(R.id.playlist_progress, 100, 0, true)
            views.setOnClickPendingIntent(R.id.playlist_root, broadcast(context, ACTION_RESUME, id))
            views.setOnClickPendingIntent(R.id.playlist_resume, broadcast(context, ACTION_RESUME, id))
            views.setOnClickPendingIntent(R.id.playlist_next, broadcast(context, ACTION_NEXT, id))
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
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            runCatching { context.startActivity(intent) }
        }
    }
}
