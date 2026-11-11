package com.iturrizaga.fairplay.features.players

import com.iturrizaga.fairplay.core.models.Player

data class PlayerListState(
    val players: List<Player> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

data class PlayerDetailState(
    val player: Player? = null
)
