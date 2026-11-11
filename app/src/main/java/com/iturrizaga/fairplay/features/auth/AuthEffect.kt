package com.iturrizaga.fairplay.features.auth

import com.iturrizaga.fairplay.core.network.APIClient
import com.iturrizaga.fairplay.core.network.APIError
import com.iturrizaga.fairplay.core.session.SessionStore

suspend fun authEffect(
    state: AuthState,
    action: AuthAction,
    client: APIClient,
    sessionStore: SessionStore
): List<AuthAction> = when (action) {
    is AuthAction.OnTask -> {
        val token = if (state.session == AppSession.RESTORING) sessionStore.loadToken() else null
        when {
            state.session != AppSession.RESTORING -> emptyList()
            token == null -> listOf(AuthAction.RestoreFailure)
            else -> {
                client.token = token
                try {
                    listOf(AuthAction.LoginSuccess(client.fetchCurrentUser(), token))
                } catch (e: APIError.Unauthorized) {
                    sessionStore.clear()
                    client.token = null
                    listOf(AuthAction.RestoreFailure)
                } catch (e: Exception) {
                    // Offline or server down: keep the token so the next launch can retry.
                    listOf(AuthAction.RestoreFailure)
                }
            }
        }
    }
    is AuthAction.Signup -> {
        try {
            val (user, token) = client.signup(action.email, action.password, action.name)
            client.token = token
            listOf(AuthAction.LoginSuccess(user, token))
        } catch (e: Exception) {
            listOf(AuthAction.LoginFailure(e.message ?: "Error al registrarse."))
        }
    }
    is AuthAction.Login -> {
        try {
            val (user, token) = client.login(action.email, action.password)
            client.token = token
            listOf(AuthAction.LoginSuccess(user, token))
        } catch (e: Exception) {
            listOf(AuthAction.LoginFailure(e.message ?: "Error al iniciar sesión."))
        }
    }
    is AuthAction.LoginSuccess -> {
        client.token = action.token
        sessionStore.save(action.token)
        emptyList()
    }
    is AuthAction.Logout -> {
        try {
            client.logout()
        } catch (e: Exception) {
            // logout failures are non-critical; always clear local state
        }
        client.token = null
        sessionStore.clear()
        emptyList()
    }
    is AuthAction.SessionExpired -> {
        client.token = null
        sessionStore.clear()
        emptyList()
    }
    is AuthAction.RestoreFailure, is AuthAction.LoginFailure, is AuthAction.ClearError -> emptyList()
}
