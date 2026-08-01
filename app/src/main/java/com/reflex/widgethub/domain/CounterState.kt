package com.reflex.widgethub.domain

data class CounterState(
    val currentCount: Long = 0,
    val goal: Long = 33,
    val lifetimeTotal: Long = 0
)
