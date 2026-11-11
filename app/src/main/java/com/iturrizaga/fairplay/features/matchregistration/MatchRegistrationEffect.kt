package com.iturrizaga.fairplay.features.matchregistration

import com.iturrizaga.fairplay.core.network.APIClient

suspend fun matchRegistrationEffect(state: MatchRegistrationState, action: MatchRegistrationAction, client: APIClient): List<MatchRegistrationAction> {
    if (action !is MatchRegistrationAction.Confirm) return emptyList()
    val current = state.match ?: return emptyList()
    return try {
        client.registerForMatch(current.id)
        // The registration already succeeded; a failed refresh just keeps the roster we had.
        val fresh = runCatching { client.getMatch(current.id) }.getOrNull() ?: current
        listOf(MatchRegistrationAction.ConfirmSuccess(fresh))
    } catch (e: Exception) {
        listOf(MatchRegistrationAction.ConfirmFailure(e.message ?: "Error al registrarse en el partido."))
    }
}
