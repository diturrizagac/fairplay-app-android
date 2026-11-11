package com.iturrizaga.fairplay.features.players

import com.iturrizaga.fairplay.core.network.APIClient

suspend fun playerListEffect(state: PlayerListState, action: PlayerListAction, client: APIClient): List<PlayerListAction> = when (action) {
    is PlayerListAction.OnTask -> {
        if (state.players.isNotEmpty()) emptyList()
        else {
            try {
                val players = client.fetchPlayers()
                listOf(PlayerListAction.FetchSuccess(players))
            } catch (e: Exception) {
                listOf(PlayerListAction.FetchFailure(e.message ?: "Error al cargar jugadores."))
            }
        }
    }
    else -> emptyList()
}
