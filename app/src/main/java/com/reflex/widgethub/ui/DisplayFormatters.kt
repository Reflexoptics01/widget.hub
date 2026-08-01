package com.reflex.widgethub.ui

import com.reflex.widgethub.domain.CounterState
import com.reflex.widgethub.domain.completedCycles

fun completedCycleLabel(state: CounterState): String? = completedCycles(state)
    .takeIf { it > 0 }
    ?.let { "+$it" }

fun compactCountLabel(state: CounterState): String {
    val goal = state.goal.coerceAtLeast(1)
    if (state.currentCount <= goal) return state.currentCount.toString()
    val extra = state.currentCount - goal
    return "$goal${extra.toString().toSuperscript()}"
}

private fun String.toSuperscript(): String = map { character ->
    when (character) {
        '+' -> '⁺'
        '0' -> '⁰'
        '1' -> '¹'
        '2' -> '²'
        '3' -> '³'
        '4' -> '⁴'
        '5' -> '⁵'
        '6' -> '⁶'
        '7' -> '⁷'
        '8' -> '⁸'
        '9' -> '⁹'
        else -> character
    }
}.joinToString(separator = "", prefix = "⁺")
