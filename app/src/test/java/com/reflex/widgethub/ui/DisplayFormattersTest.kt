package com.reflex.widgethub.ui

import com.reflex.widgethub.domain.CounterState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DisplayFormattersTest {
    @Test
    fun shows_completed_goal_cycle_as_plus_one() {
        assertEquals("+1", completedCycleLabel(CounterState(34, 33, 500)))
    }

    @Test
    fun hides_cycle_label_before_first_completed_goal() {
        assertNull(completedCycleLabel(CounterState(32, 33, 500)))
    }
}
