package com.reflex.widgethub.ui

import com.reflex.widgethub.domain.CounterState
import com.reflex.widgethub.domain.completedCycles

fun completedCycleLabel(state: CounterState): String? = completedCycles(state)
    .takeIf { it > 0 }
    ?.let { "+$it" }

fun cycleHeaderLabel(state: CounterState): String {
    val cycles = completedCycles(state)
    val noun = if (cycles == 1L) "CYCLE" else "CYCLES"
    return "GOAL ${state.goal.coerceAtLeast(1)} • +$cycles $noun"
}
