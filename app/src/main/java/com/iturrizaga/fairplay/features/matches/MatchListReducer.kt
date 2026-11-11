package com.iturrizaga.fairplay.features.matches

fun matchListReducer(state: MatchListState, action: MatchListAction): MatchListState = when (action) {
    is MatchListAction.Reset           -> MatchListState()
    is MatchListAction.OnTask          -> if (state.matches.isEmpty()) state.copy(isLoading = true, error = null) else state
    is MatchListAction.FetchSuccess    -> state.copy(matches = action.matches, isLoading = false)
    is MatchListAction.FetchFailure    -> state.copy(isLoading = false, error = action.error)
    is MatchListAction.MarkRegistered  -> state.copy(registeredMatchIds = state.registeredMatchIds + action.matchId)
    is MatchListAction.Unregister      -> state.copy(registeredMatchIds = state.registeredMatchIds - action.matchId)
    is MatchListAction.AddMatch        -> state.copy(matches = state.matches + action.match)
    is MatchListAction.ShowRegistration -> state
    is MatchListAction.ShowCreation    -> state
}
