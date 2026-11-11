package com.iturrizaga.fairplay.features.auth

fun authReducer(state: AuthState, action: AuthAction): AuthState = when (action) {
    is AuthAction.Signup        -> state.copy(isLoading = true, error = null)
    is AuthAction.Login         -> state.copy(isLoading = true, error = null)
    is AuthAction.LoginSuccess  -> state.copy(isLoading = false, session = AppSession.AUTHENTICATED, currentUser = action.user, token = action.token)
    is AuthAction.LoginFailure  -> state.copy(isLoading = false, error = action.error)
    is AuthAction.Logout        -> AuthState()
    is AuthAction.ClearError    -> state.copy(error = null)
}
