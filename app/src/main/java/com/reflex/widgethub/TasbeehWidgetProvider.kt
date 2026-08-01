package com.reflex.widgethub

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.reflex.widgethub.data.CounterStore
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.reflex.widgethub.domain.increment
import com.reflex.widgethub.domain.resetCurrent
import com.reflex.widgethub.ui.expressiveProgress
import com.reflex.widgethub.ui.compactCountLabel
import com.reflex.widgethub.ui.hapticDurationMillis

class TasbeehWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        ids.forEach { updateWidget(context, manager, it) }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val store = CounterStore(context)
        when (intent.action) {
            ACTION_INCREMENT -> {
                val updated = store.update(::increment)
                vibrate(context, updated)
            }
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
            views.setTextViewText(R.id.widget_count, compactCountLabel(state))
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

        private fun vibrate(context: Context, state: com.reflex.widgethub.domain.CounterState) {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(VibratorManager::class.java)?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            } ?: return
            if (!vibrator.hasVibrator()) return
            val goal = state.goal.coerceAtLeast(1)
            val goalReached = state.currentCount > 0 && state.currentCount % goal == 0L
            val duration = hapticDurationMillis(goalReached)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val amplitude = if (goalReached) 255 else 160
                vibrator.vibrate(VibrationEffect.createOneShot(duration, amplitude))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(duration)
            }
        }
    }
}
