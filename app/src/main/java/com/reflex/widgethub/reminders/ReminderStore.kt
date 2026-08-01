package com.reflex.widgethub.reminders

import android.content.Context

class ReminderStore(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)

    fun get(type: ReminderType): ReminderSettings = ReminderSettings(
        enabled = preferences.getBoolean(key(type, "enabled"), false),
        hour = preferences.getInt(key(type, "hour"), defaultReminderSettings(type).hour),
        minute = preferences.getInt(key(type, "minute"), defaultReminderSettings(type).minute)
    ).normalized()

    fun set(type: ReminderType, settings: ReminderSettings) {
        val normalized = settings.normalized()
        preferences.edit()
            .putBoolean(key(type, "enabled"), normalized.enabled)
            .putInt(key(type, "hour"), normalized.hour)
            .putInt(key(type, "minute"), normalized.minute)
            .apply()
    }

    private fun key(type: ReminderType, field: String): String = "${type.name.lowercase()}_$field"

    private companion object { const val PREFERENCES = "daily_reminders_v1" }
}
