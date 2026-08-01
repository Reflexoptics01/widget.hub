package com.reflex.widgethub.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class HapticFeedbackTest {
    @Test
    fun uses_short_feedback_for_normal_tap() {
        assertEquals(24L, hapticDurationMillis(goalReached = false))
    }

    @Test
    fun uses_long_feedback_for_goal_tap() {
        assertEquals(120L, hapticDurationMillis(goalReached = true))
    }

    @Test
    fun uses_medium_amplitude_for_normal_tap() {
        assertEquals(160, hapticAmplitude(goalReached = false))
    }

    @Test
    fun uses_max_amplitude_for_goal_tap() {
        assertEquals(255, hapticAmplitude(goalReached = true))
    }
}
