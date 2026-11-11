package com.iturrizaga.fairplay.features.auth

sealed class AuthAction {
    data class Signup(val email: String, val password: String, val name: String) : AuthAction()
    data class Login(val email: String, val password: String) : AuthAction()
    data class LoginSuccess(val user: AuthUser, val token: String) : AuthAction()
    data class LoginFailure(val error: String) : AuthAction()
    data object Logout : AuthAction()
    data object ClearError : AuthAction()
}
