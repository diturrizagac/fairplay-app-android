package com.iturrizaga.fairplay.features.players

import com.iturrizaga.fairplay.core.models.Player

sealed class PlayerListAction {
    data object Reset : PlayerListAction()
    data object OnTask : PlayerListAction()
    data class FetchSuccess(val players: List<Player>) : PlayerListAction()
    data class FetchFailure(val error: String) : PlayerListAction()
    data class ShowDetail(val player: Player) : PlayerListAction()
}

sealed class PlayerDetailAction {
    data object Reset : PlayerDetailAction()
    data class Load(val player: Player) : PlayerDetailAction()
    data object Dismiss : PlayerDetailAction()
}
