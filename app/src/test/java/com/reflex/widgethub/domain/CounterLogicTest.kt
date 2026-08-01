package com.reflex.widgethub.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class CounterLogicTest {
    @Test
    fun increment_increases_current_and_lifetime_totals() {
        assertEquals(
            CounterState(33, 33, 101),
            increment(CounterState(32, 33, 100))
        )
    }

    @Test
    fun reset_clears_current_but_preserves_lifetime_total() {
        assertEquals(
            CounterState(0, 33, 102),
            resetCurrent(CounterState(34, 33, 102))
        )
    }

    @Test
    fun completed_cycles_are_full_goal_groups() {
        assertEquals(1, completedCycles(CounterState(34, 33, 500)))
        assertEquals(0, completedCycles(CounterState(32, 33, 500)))
    }
}
