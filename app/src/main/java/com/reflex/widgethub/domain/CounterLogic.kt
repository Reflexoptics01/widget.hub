package com.reflex.widgethub.domain

fun increment(state: CounterState): CounterState = state.copy(
    currentCount = state.currentCount + 1,
    lifetimeTotal = state.lifetimeTotal + 1
)

fun resetCurrent(state: CounterState): CounterState = state.copy(currentCount = 0)

fun completedCycles(state: CounterState): Long {
    val safeGoal = state.goal.coerceAtLeast(1)
    return state.currentCount / safeGoal
}

fun normalizeGoal(goal: Long): Long = goal.coerceAtLeast(1)
