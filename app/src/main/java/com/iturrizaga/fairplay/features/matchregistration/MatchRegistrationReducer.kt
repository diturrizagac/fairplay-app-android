package com.iturrizaga.fairplay.features.matchregistration

fun matchRegistrationReducer(state: MatchRegistrationState, action: MatchRegistrationAction): MatchRegistrationState = when (action) {
    is MatchRegistrationAction.Reset          -> MatchRegistrationState()
    is MatchRegistrationAction.Show           -> state.copy(match = action.match, isConfirming = false, error = null)
    is MatchRegistrationAction.Confirm        -> state.copy(isConfirming = true, error = null)
    is MatchRegistrationAction.ConfirmSuccess -> MatchRegistrationState()
    is MatchRegistrationAction.ConfirmFailure -> state.copy(isConfirming = false, error = action.error)
    is MatchRegistrationAction.Cancel         -> MatchRegistrationState()
}
