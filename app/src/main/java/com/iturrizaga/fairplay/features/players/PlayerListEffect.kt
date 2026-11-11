package com.iturrizaga.fairplay.features.players

import com.iturrizaga.fairplay.core.network.APIClient

suspend fun playerListEffect(state: PlayerListState, action: PlayerListAction, client: APIClient): List<PlayerListAction> =
    when (action) {
        is PlayerListAction.OnTask -> if (state.players.isNotEmpty()) emptyList() else fetch(client)
        is PlayerListAction.Refresh -> fetch(client)
        else -> emptyList()
    }

private suspend fun fetch(client: APIClient): List<PlayerListAction> = try {
    listOf(PlayerListAction.FetchSuccess(client.fetchPlayers()))
} catch (e: Exception) {
    listOf(PlayerListAction.FetchFailure(e.message ?: "Error al cargar jugadores."))
}
