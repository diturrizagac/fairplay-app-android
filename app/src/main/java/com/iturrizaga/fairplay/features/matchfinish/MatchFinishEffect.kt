package com.iturrizaga.fairplay.features.matchfinish

import com.iturrizaga.fairplay.core.network.APIClient
import com.iturrizaga.fairplay.core.network.FinishTeamInput

suspend fun matchFinishEffect(state: MatchFinishState, action: MatchFinishAction, client: APIClient): List<MatchFinishAction> {
    if (action !is MatchFinishAction.Submit) return emptyList()
    val match = state.match ?: return emptyList()
    val teams = match.teams.map { team ->
        FinishTeamInput(teamId = team.id, score = state.score(team.id), playerGoals = state.goals[team.id].orEmpty())
    }
    return try {
        listOf(MatchFinishAction.SubmitSuccess(client.finishMatch(match.id, teams)))
    } catch (e: Exception) {
        listOf(MatchFinishAction.SubmitFailure(e.message ?: "Error al guardar el resultado."))
    }
}
