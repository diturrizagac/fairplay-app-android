package com.iturrizaga.fairplay.features.auth

fun authReducer(state: AuthState, action: AuthAction): AuthState = when (action) {
    is AuthAction.OnTask         -> state
    is AuthAction.RestoreFailure -> state.copy(session = AppSession.UNAUTHENTICATED)
    is AuthAction.Signup         -> state.copy(isLoading = true, error = null, isSessionExpired = false)
    is AuthAction.Login          -> state.copy(isLoading = true, error = null, isSessionExpired = false)
    is AuthAction.LoginSuccess   -> state.copy(
        isLoading = false,
        isSessionExpired = false,
        session = AppSession.AUTHENTICATED,
        currentUser = action.user,
        token = action.token
    )
    is AuthAction.LoginFailure   -> state.copy(isLoading = false, error = action.error)
    is AuthAction.Logout         -> AuthState()
    is AuthAction.SessionExpired ->
        if (state.session == AppSession.AUTHENTICATED) AuthState(isSessionExpired = true) else state
    is AuthAction.ClearError     -> state.copy(error = null, isSessionExpired = false)
}
