package com.reflex.widgethub.ui

import com.reflex.widgethub.domain.CounterState

fun expressiveProgress(state: CounterState): Int {
    val goal = state.goal.coerceAtLeast(1)
    return ((state.currentCount % goal) * 100 / goal).toInt()
}

fun isGoalPulse(state: CounterState): Boolean {
    val goal = state.goal.coerceAtLeast(1)
    return state.currentCount > 0 && state.currentCount % goal == 0L
}
