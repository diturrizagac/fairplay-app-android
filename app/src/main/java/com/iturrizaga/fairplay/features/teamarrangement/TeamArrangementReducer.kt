package com.iturrizaga.fairplay.features.teamarrangement

fun teamArrangementReducer(state: TeamArrangementState, action: TeamArrangementAction): TeamArrangementState = when (action) {
    is TeamArrangementAction.Reset          -> TeamArrangementState()
    is TeamArrangementAction.Load           -> TeamArrangementState(match = action.match)
    is TeamArrangementAction.Balance        -> state.copy(isBalancing = true, error = null)
    is TeamArrangementAction.BalanceSuccess -> state.copy(match = action.match, isBalancing = false)
    is TeamArrangementAction.BalanceFailure -> state.copy(isBalancing = false, error = action.error)
    is TeamArrangementAction.Close          -> TeamArrangementState()
}
