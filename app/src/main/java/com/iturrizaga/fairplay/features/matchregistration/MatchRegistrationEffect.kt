package com.iturrizaga.fairplay.features.matchregistration

import com.iturrizaga.fairplay.core.network.APIClient

suspend fun matchRegistrationEffect(state: MatchRegistrationState, action: MatchRegistrationAction, client: APIClient): List<MatchRegistrationAction> {
    if (action !is MatchRegistrationAction.Confirm) return emptyList()
    val matchId = state.match?.id ?: return emptyList()
    return try {
        client.registerForMatch(matchId)
        listOf(MatchRegistrationAction.ConfirmSuccess(matchId))
    } catch (e: Exception) {
        listOf(MatchRegistrationAction.ConfirmFailure(e.message ?: "Error al registrarse en el partido."))
    }
}
