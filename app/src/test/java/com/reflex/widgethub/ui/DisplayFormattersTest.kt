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

    @Test
    fun formats_exact_goal_without_raw_next_number() {
        assertEquals("33", compactCountLabel(CounterState(33, 33, 33)))
    }

    @Test
    fun formats_extra_taps_as_superscript_remainder() {
        assertEquals("33⁺¹", compactCountLabel(CounterState(34, 33, 34)))
        assertEquals("33⁺¹³", compactCountLabel(CounterState(46, 33, 46)))
    }
}
