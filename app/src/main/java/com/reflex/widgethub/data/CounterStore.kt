package com.reflex.widgethub.data

import android.content.Context
import com.reflex.widgethub.domain.CounterState
import com.reflex.widgethub.domain.normalizeGoal

class CounterStore(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)

    fun load(): CounterState = CounterState(
        currentCount = preferences.getLong(KEY_CURRENT, 0),
        goal = normalizeGoal(preferences.getLong(KEY_GOAL, 33)),
        lifetimeTotal = preferences.getLong(KEY_LIFETIME, 0)
    )

    fun save(state: CounterState) {
        preferences.edit()
            .putLong(KEY_CURRENT, state.currentCount.coerceAtLeast(0))
            .putLong(KEY_GOAL, normalizeGoal(state.goal))
            .putLong(KEY_LIFETIME, state.lifetimeTotal.coerceAtLeast(0))
            .apply()
    }

    fun update(transform: (CounterState) -> CounterState): CounterState {
        val next = transform(load())
        save(next)
        return next
    }

    private companion object {
        const val PREFERENCES = "tasbeeh_counter_v2"
        const val KEY_CURRENT = "current_count"
        const val KEY_GOAL = "goal"
        const val KEY_LIFETIME = "lifetime_total"
    }
}
