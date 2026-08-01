package com.reflex.widgethub.reminders

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class ReminderSettingsTest {
    @Test
    fun defaults_are_disabled_and_tasbih_defaults_to_2300() {
        assertEquals(ReminderSettings(false, 23, 0), defaultReminderSettings(ReminderType.TASBIH_FATIMA))
        assertFalse(defaultReminderSettings(ReminderType.DUROOD).enabled)
    }

    @Test
    fun reminder_types_are_independent() {
        val state = ReminderPreferencesState()
            .set(ReminderType.TASBIH_FATIMA, ReminderSettings(true, 23, 0))
        assertEquals(ReminderSettings(true, 23, 0), state.get(ReminderType.TASBIH_FATIMA))
        assertEquals(ReminderSettings(false, 0, 0), state.get(ReminderType.DUROOD))
    }
}
