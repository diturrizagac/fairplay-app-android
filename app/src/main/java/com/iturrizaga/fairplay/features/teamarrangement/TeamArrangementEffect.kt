package com.iturrizaga.fairplay.features.teamarrangement

import com.iturrizaga.fairplay.core.network.APIClient

suspend fun teamArrangementEffect(state: TeamArrangementState, action: TeamArrangementAction, client: APIClient): List<TeamArrangementAction> {
    if (action !is TeamArrangementAction.Balance) return emptyList()
    val matchId = state.match?.id ?: return emptyList()
    return try {
        listOf(TeamArrangementAction.BalanceSuccess(client.arrangeTeams(matchId)))
    } catch (e: Exception) {
        listOf(TeamArrangementAction.BalanceFailure(e.message ?: "Error al armar los equipos."))
    }
}
