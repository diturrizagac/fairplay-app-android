package com.iturrizaga.fairplay.features.auth

import com.iturrizaga.fairplay.core.network.APIClient

suspend fun authEffect(state: AuthState, action: AuthAction, client: APIClient): List<AuthAction> = when (action) {
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
    is AuthAction.Logout -> {
        try {
            client.logout()
        } catch (e: Exception) {
            // logout failures are non-critical; always clear local state
        }
        client.token = null
        emptyList()
    }
    else -> emptyList()
}
