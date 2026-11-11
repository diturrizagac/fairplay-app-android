package com.iturrizaga.fairplay.features.matches

fun matchListReducer(state: MatchListState, action: MatchListAction): MatchListState = when (action) {
    is MatchListAction.Reset             -> MatchListState()
    is MatchListAction.OnTask            -> if (state.matches.isEmpty()) state.copy(isLoading = true, error = null) else state
    is MatchListAction.Refresh           -> state.copy(isLoading = true, error = null)
    is MatchListAction.FetchSuccess      ->
        state.copy(matches = action.matches, isLoading = false, error = null).syncingRegistrations(action.matches)
    is MatchListAction.FetchFailure      -> state.copy(isLoading = false, error = action.error)
    is MatchListAction.MarkRegistered    -> state.copy(registeredMatchIds = state.registeredMatchIds + action.matchId)
    is MatchListAction.Unregister        -> state.copy(registeredMatchIds = state.registeredMatchIds - action.matchId)
    is MatchListAction.UnregisterFailure ->
        state.copy(registeredMatchIds = state.registeredMatchIds + action.matchId, error = action.error)
    is MatchListAction.AddMatch          -> state.copy(matches = state.matches + action.match)
    is MatchListAction.UpdateMatch       -> {
        val exists = state.matches.any { it.id == action.match.id }
        val matches = if (exists) state.matches.map { if (it.id == action.match.id) action.match else it }
                      else state.matches + action.match
        state.copy(matches = matches).syncingRegistrations(listOf(action.match))
    }
    is MatchListAction.SetFilter         -> state.copy(filter = action.filter)
    is MatchListAction.SetCurrentUser    ->
        if (state.currentUserId == action.userId) state.copy(isAdmin = action.isAdmin)
        else state.copy(currentUserId = action.userId, isAdmin = action.isAdmin, registeredMatchIds = emptySet())
            .syncingRegistrations(state.matches)
    is MatchListAction.ShowRegistration,
    is MatchListAction.ShowCreation,
    is MatchListAction.ShowArrangement,
    is MatchListAction.ShowFinish        -> state
}
