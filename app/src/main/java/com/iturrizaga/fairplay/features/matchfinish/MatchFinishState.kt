package com.iturrizaga.fairplay.features.matchfinish

import com.iturrizaga.fairplay.core.models.Match

data class MatchFinishState(
    val match: Match? = null,
    /** Score per team id. */
    val scores: Map<String, Int> = emptyMap(),
    /** Goals per team id, then per player id. */
    val goals: Map<String, Map<String, Int>> = emptyMap(),
    val isSubmitting: Boolean = false,
    val error: String? = null
) {
    fun score(teamId: String): Int = scores[teamId] ?: 0
    fun goals(teamId: String, playerId: String): Int = goals[teamId]?.get(playerId) ?: 0
    fun goalsSum(teamId: String): Int = goals[teamId]?.values?.sum() ?: 0

    /** Same rule as the API: player goals can't exceed the team score (own goals make it lower). */
    val goalsWithinScores: Boolean
        get() = match?.teams?.all { goalsSum(it.id) <= score(it.id) } ?: false

    val canSubmit: Boolean get() = goalsWithinScores && !isSubmitting
}
