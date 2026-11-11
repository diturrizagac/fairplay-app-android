package com.iturrizaga.fairplay.features.players

fun playerListReducer(state: PlayerListState, action: PlayerListAction): PlayerListState = when (action) {
    is PlayerListAction.Reset         -> PlayerListState()
    // The effect skips the fetch when players are already loaded, so only show a spinner when it runs.
    is PlayerListAction.OnTask        -> if (state.players.isEmpty()) state.copy(isLoading = true, error = null) else state
    is PlayerListAction.Refresh       -> state.copy(isLoading = true, error = null)
    is PlayerListAction.FetchSuccess  -> state.copy(players = action.players, isLoading = false, error = null)
    is PlayerListAction.FetchFailure  -> state.copy(error = action.error, isLoading = false)
    is PlayerListAction.ShowDetail    -> state
}

fun playerDetailReducer(state: PlayerDetailState, action: PlayerDetailAction): PlayerDetailState = when (action) {
    is PlayerDetailAction.Reset   -> PlayerDetailState()
    is PlayerDetailAction.Load    -> state.copy(player = action.player)
    is PlayerDetailAction.Dismiss -> state.copy(player = null)
}
