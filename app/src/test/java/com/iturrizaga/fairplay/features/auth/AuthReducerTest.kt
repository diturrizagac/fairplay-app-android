package com.iturrizaga.fairplay.features.auth

import com.iturrizaga.fairplay.support.makeUser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthReducerTest {
    @Test fun restoreFailure_showsLogin() {
        val state = authReducer(AuthState(session = AppSession.RESTORING), AuthAction.RestoreFailure)
        assertEquals(AppSession.UNAUTHENTICATED, state.session)
    }

    @Test fun loginSuccess_authenticates() {
        val user = makeUser()
        val state = authReducer(AuthState(isLoading = true, isSessionExpired = true), AuthAction.LoginSuccess(user, "t"))
        assertEquals(AppSession.AUTHENTICATED, state.session)
        assertEquals(user, state.currentUser)
        assertEquals("t", state.token)
        assertFalse(state.isLoading)
        assertFalse(state.isSessionExpired)
    }

    @Test fun logout_resetsState() {
        val state = authReducer(
            AuthState(currentUser = makeUser(), token = "t", session = AppSession.AUTHENTICATED),
            AuthAction.Logout
        )
        assertEquals(AuthState(), state)
    }

    @Test fun sessionExpired_logsOutAndFlagsMessage() {
        val state = authReducer(
            AuthState(currentUser = makeUser(), token = "t", session = AppSession.AUTHENTICATED),
            AuthAction.SessionExpired
        )
        assertEquals(AppSession.UNAUTHENTICATED, state.session)
        assertNull(state.currentUser)
        assertNull(state.token)
        assertTrue(state.isSessionExpired)
    }

    @Test fun sessionExpired_isIgnoredWhenNotAuthenticated() {
        val restoring = AuthState(session = AppSession.RESTORING)
        assertEquals(restoring, authReducer(restoring, AuthAction.SessionExpired))
    }

    @Test fun login_clearsPreviousErrors() {
        val state = authReducer(AuthState(error = "old", isSessionExpired = true), AuthAction.Login("a", "b"))
        assertTrue(state.isLoading)
        assertNull(state.error)
        assertFalse(state.isSessionExpired)
    }
}
