package com.reflex.widgethub.reminders

data class ReminderSettings(
    val enabled: Boolean,
    val hour: Int,
    val minute: Int
) {
    fun normalized(): ReminderSettings = copy(
        hour = hour.coerceIn(0, 23),
        minute = minute.coerceIn(0, 59)
    )
}

fun defaultReminderSettings(type: ReminderType): ReminderSettings = when (type) {
    ReminderType.TASBIH_FATIMA -> ReminderSettings(false, 23, 0)
    ReminderType.DUROOD -> ReminderSettings(false, 0, 0)
}

class ReminderPreferencesState(
    private val values: Map<ReminderType, ReminderSettings> = ReminderType.entries.associateWith(::defaultReminderSettings)
) {
    fun get(type: ReminderType): ReminderSettings = values[type] ?: defaultReminderSettings(type)

    fun set(type: ReminderType, settings: ReminderSettings): ReminderPreferencesState =
        ReminderPreferencesState(values + (type to settings.normalized()))
}
