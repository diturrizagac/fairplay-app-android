package com.iturrizaga.fairplay.features.matchcreation

import com.iturrizaga.fairplay.core.network.APIClient

suspend fun matchCreationEffect(state: MatchCreationState, action: MatchCreationAction, client: APIClient): List<MatchCreationAction> {
    if (action !is MatchCreationAction.Submit) return emptyList()
    return try {
        val match = client.createMatch(state.name, state.date, state.location, state.maxPlayers)
        listOf(MatchCreationAction.SubmitSuccess(match))
    } catch (e: Exception) {
        listOf(MatchCreationAction.SubmitFailure(e.message ?: "Error al crear el partido."))
    }
}
