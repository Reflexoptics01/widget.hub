package com.reflex.widgethub.ui

import com.reflex.widgethub.domain.CounterState
import kotlin.test.Test
import kotlin.test.assertEquals

class ExpressiveUiStateTest {
    @Test
    fun maps_counter_to_red_progress_and_completion_state() {
        assertEquals(50, expressiveProgress(CounterState(currentCount = 50, goal = 100)))
        assertEquals(false, isGoalPulse(CounterState(currentCount = 50, goal = 100)))
        assertEquals(true, isGoalPulse(CounterState(currentCount = 100, goal = 100)))
    }

    @Test
    fun progress_wraps_after_goal_cycle() {
        assertEquals(0, expressiveProgress(CounterState(currentCount = 100, goal = 100)))
        assertEquals(25, expressiveProgress(CounterState(currentCount = 125, goal = 100)))
    }
}
