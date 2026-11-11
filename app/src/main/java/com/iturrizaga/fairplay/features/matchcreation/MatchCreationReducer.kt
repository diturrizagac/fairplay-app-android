package com.iturrizaga.fairplay.features.matchcreation

fun matchCreationReducer(state: MatchCreationState, action: MatchCreationAction): MatchCreationState = when (action) {
    is MatchCreationAction.Reset         -> MatchCreationState()
    is MatchCreationAction.SetName       -> state.copy(name = action.name)
    is MatchCreationAction.SetDate       -> state.copy(date = action.date)
    is MatchCreationAction.SetLocation   -> state.copy(location = action.location)
    is MatchCreationAction.SetMatchType  -> state.copy(matchType = action.matchType)
    is MatchCreationAction.SetNumTeams   ->
        if (action.numTeams in MatchCreationState.allowedNumTeams) state.copy(numTeams = action.numTeams) else state
    is MatchCreationAction.Submit        -> state.copy(isSubmitting = true, error = null)
    is MatchCreationAction.SubmitSuccess -> MatchCreationState()
    is MatchCreationAction.SubmitFailure -> state.copy(isSubmitting = false, error = action.error)
    is MatchCreationAction.Cancel        -> MatchCreationState()
}
