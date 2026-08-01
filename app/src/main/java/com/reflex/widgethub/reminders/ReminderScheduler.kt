package com.reflex.widgethub.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

class ReminderScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)
    private val store = ReminderStore(context)

    fun schedule(type: ReminderType, settings: ReminderSettings = store.get(type)) {
        if (!settings.enabled) return cancel(type)
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, settings.hour)
            set(Calendar.MINUTE, settings.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }
        val pendingIntent = pendingIntent(type)
        if (Build.VERSION.SDK_INT >= 31 && !alarmManager.canScheduleExactAlarms()) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        } else if (Build.VERSION.SDK_INT >= 23) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        }
    }

    fun cancel(type: ReminderType) { alarmManager.cancel(pendingIntent(type)) }

    fun rescheduleEnabled() {
        ReminderType.entries.forEach { type ->
            val settings = store.get(type)
            if (settings.enabled) schedule(type, settings) else cancel(type)
        }
    }

    private fun pendingIntent(type: ReminderType): PendingIntent = PendingIntent.getBroadcast(
        context,
        type.requestCode,
        Intent(context, ReminderReceiver::class.java).putExtra(ReminderReceiver.EXTRA_TYPE, type.name),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
}
