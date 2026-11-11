package com.iturrizaga.fairplay.features.matches

import com.iturrizaga.fairplay.core.network.APIClient

suspend fun matchListEffect(state: MatchListState, action: MatchListAction, client: APIClient): List<MatchListAction> {
    if (action !is MatchListAction.OnTask) return emptyList()
    if (state.matches.isNotEmpty()) return emptyList()
    return try {
        val matches = client.fetchMatches()
        listOf(MatchListAction.FetchSuccess(matches))
    } catch (e: Exception) {
        listOf(MatchListAction.FetchFailure(e.message ?: "Error al cargar los partidos."))
    }
}
