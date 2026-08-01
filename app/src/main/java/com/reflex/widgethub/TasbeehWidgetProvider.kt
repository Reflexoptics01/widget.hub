package com.reflex.widgethub

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.reflex.widgethub.data.CounterStore
import com.reflex.widgethub.domain.CounterState
import com.reflex.widgethub.domain.increment
import com.reflex.widgethub.domain.resetCurrent
import com.reflex.widgethub.ui.expressiveProgress

class TasbeehWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        ids.forEach { updateWidget(context, manager, it) }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val store = CounterStore(context)
        when (intent.action) {
            ACTION_INCREMENT -> store.update(::increment)
            ACTION_RESET -> store.update(::resetCurrent)
            ACTION_OPEN_APP -> context.startActivity(Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            else -> return
        }
        refreshAllWidgets(context)
    }

    companion object {
        const val ACTION_INCREMENT = "com.reflex.widgethub.action.INCREMENT"
        const val ACTION_RESET = "com.reflex.widgethub.action.RESET"
        const val ACTION_OPEN_APP = "com.reflex.widgethub.action.OPEN_APP"

        fun refreshAllWidgets(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, TasbeehWidgetProvider::class.java)
            manager.getAppWidgetIds(component).forEach { updateWidget(context, manager, it) }
        }

        private fun updateWidget(context: Context, manager: AppWidgetManager, id: Int) {
            val state = CounterStore(context).load()
            val views = RemoteViews(context.packageName, R.layout.widget_tasbeeh)
            views.setTextViewText(R.id.widget_count, state.currentCount.toString())
            views.setTextViewText(R.id.widget_goal, "GOAL ${state.goal}")
            views.setProgressBar(R.id.widget_progress, 100, expressiveProgress(state), false)
            views.setOnClickPendingIntent(R.id.widget_count, broadcast(context, ACTION_INCREMENT, id))
            views.setOnClickPendingIntent(R.id.widget_progress, broadcast(context, ACTION_INCREMENT, id))
            views.setOnClickPendingIntent(R.id.widget_reset, broadcast(context, ACTION_RESET, id))
            views.setOnClickPendingIntent(R.id.widget_goal, broadcast(context, ACTION_OPEN_APP, id))
            manager.updateAppWidget(id, views)
        }

        private fun broadcast(context: Context, action: String, widgetId: Int): PendingIntent {
            val intent = Intent(context, TasbeehWidgetProvider::class.java).setAction(action).putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            return PendingIntent.getBroadcast(context, action.hashCode() + widgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }
    }
}
