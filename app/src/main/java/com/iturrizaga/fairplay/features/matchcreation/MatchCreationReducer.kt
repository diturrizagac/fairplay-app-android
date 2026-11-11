package com.iturrizaga.fairplay.features.matchcreation

fun matchCreationReducer(state: MatchCreationState, action: MatchCreationAction): MatchCreationState = when (action) {
    is MatchCreationAction.Reset         -> MatchCreationState()
    is MatchCreationAction.SetName       -> state.copy(name = action.name)
    is MatchCreationAction.SetDate       -> state.copy(date = action.date)
    is MatchCreationAction.SetLocation   -> state.copy(location = action.location)
    is MatchCreationAction.SetMaxPlayers -> state.copy(maxPlayers = action.count)
    is MatchCreationAction.Submit        -> state.copy(isSubmitting = true, error = null)
    is MatchCreationAction.SubmitSuccess -> MatchCreationState()
    is MatchCreationAction.SubmitFailure -> state.copy(isSubmitting = false, error = action.error)
}
