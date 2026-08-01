package com.reflex.widgethub.reminders

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.reflex.widgethub.MainActivity
import com.reflex.widgethub.R

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val type = intent.getStringExtra(EXTRA_TYPE)?.let { runCatching { ReminderType.valueOf(it) }.getOrNull() } ?: return
        val settings = ReminderStore(context).get(type)
        if (!settings.enabled) return
        createChannel(context)
        if (Build.VERSION.SDK_INT >= 33 && context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(if (type == ReminderType.TASBIH_FATIMA) "Tasbih-e-Fatima" else "Durood reminder")
            .setContentText(if (type == ReminderType.TASBIH_FATIMA) "Subhan Allah 33 • Alhamdulillah 33 • Allahu Akbar 34" else "Recite Durood 100 times")
            .setStyle(NotificationCompat.BigTextStyle().bigText(body(type)))
            .setAutoCancel(true)
            .setContentIntent(android.app.PendingIntent.getActivity(context, type.requestCode, Intent(context, MainActivity::class.java).putExtra(MainActivity.EXTRA_OPEN_REMINDER, true), android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE))
            .build()
        NotificationManagerCompat.from(context).notify(type.requestCode, notification)
        ReminderScheduler(context).schedule(type, settings)
    }

    private fun body(type: ReminderType): String = if (type == ReminderType.TASBIH_FATIMA) {
        "Before sleep: Subhan Allah 33 times, Alhamdulillah 33 times, Allahu Akbar 34 times."
    } else {
        "Allahumma salli wa sallim 'ala Muhammad — 100 times."
    }

    private fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= 26) {
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Daily reminders", NotificationManager.IMPORTANCE_DEFAULT)
            )
        }
    }

    companion object {
        const val EXTRA_TYPE = "reminder_type"
        const val CHANNEL_ID = "daily_dhikr_reminders"
    }
}
