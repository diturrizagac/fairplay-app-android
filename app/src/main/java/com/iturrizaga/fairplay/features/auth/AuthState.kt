package com.iturrizaga.fairplay.features.auth

data class AuthState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSessionExpired: Boolean = false,
    val currentUser: AuthUser? = null,
    val token: String? = null,
    val session: AppSession = AppSession.UNAUTHENTICATED
)

data class AuthUser(
    val id: String,
    val email: String,
    val name: String = "",
    val isAdmin: Boolean = false,
    val playerId: String? = null
)

enum class AppSession { RESTORING, AUTHENTICATED, UNAUTHENTICATED }
