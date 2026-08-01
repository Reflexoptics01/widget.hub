package com.reflex.widgethub.playlist

/** Local episode counter — never trust YouTube panel index for the user's EP number. */
fun nextEpisodeIndex(currentIndex: Int, direction: Int, totalCount: Int = 0): Int {
    val next = (currentIndex + direction).coerceAtLeast(1)
    return if (totalCount > 0) next.coerceAtMost(totalCount) else next
}
