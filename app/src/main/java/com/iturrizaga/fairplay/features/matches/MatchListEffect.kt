package com.iturrizaga.fairplay.features.matches

import com.iturrizaga.fairplay.core.network.APIClient

suspend fun matchListEffect(state: MatchListState, action: MatchListAction, client: APIClient): List<MatchListAction> =
    when (action) {
        is MatchListAction.OnTask -> if (state.matches.isNotEmpty()) emptyList() else fetch(client)
        is MatchListAction.Refresh -> fetch(client)
        is MatchListAction.Unregister -> try {
            client.unregisterFromMatch(action.matchId)
            listOfNotNull(runCatching { client.getMatch(action.matchId) }.getOrNull()?.let { MatchListAction.UpdateMatch(it) })
        } catch (e: Exception) {
            listOf(MatchListAction.UnregisterFailure(action.matchId, e.message ?: "Error al cancelar la inscripción."))
        }
        else -> emptyList()
    }

private suspend fun fetch(client: APIClient): List<MatchListAction> = try {
    listOf(MatchListAction.FetchSuccess(client.fetchMatches()))
} catch (e: Exception) {
    listOf(MatchListAction.FetchFailure(e.message ?: "Error al cargar los partidos."))
}
