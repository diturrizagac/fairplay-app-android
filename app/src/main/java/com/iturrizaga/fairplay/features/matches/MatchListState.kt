package com.iturrizaga.fairplay.features.matches

import com.iturrizaga.fairplay.core.models.Match

enum class MatchListFilter { UPCOMING, HISTORY }

data class MatchListState(
    val matches: List<Match> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val registeredMatchIds: Set<String> = emptySet(),
    val filter: MatchListFilter = MatchListFilter.UPCOMING,
    // Mirrored from AuthState by the mediator so the list can show admin
    // actions and registration status without reaching outside its scope.
    val currentUserId: String? = null,
    val isAdmin: Boolean = false
) {
    val visibleMatches: List<Match>
        get() = when (filter) {
            MatchListFilter.UPCOMING -> matches.filter { it.isUpcoming }.sortedBy { it.date }
            MatchListFilter.HISTORY -> matches.filter { !it.isUpcoming }.sortedByDescending { it.finishedAt ?: it.date }
        }

    /** Registration comes from each match's roster, so it survives restarts. */
    fun syncingRegistrations(updated: List<Match>): MatchListState {
        val userId = currentUserId ?: return this
        var ids = registeredMatchIds
        for (match in updated) {
            ids = if (match.registeredPlayers.any { it.userId == userId }) ids + match.id else ids - match.id
        }
        return copy(registeredMatchIds = ids)
    }
}
