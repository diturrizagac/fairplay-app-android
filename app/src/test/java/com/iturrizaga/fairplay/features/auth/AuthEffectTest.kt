package com.iturrizaga.fairplay.features.auth

import com.iturrizaga.fairplay.core.network.APIError
import com.iturrizaga.fairplay.support.FakeAPIClient
import com.iturrizaga.fairplay.support.FakeSessionStore
import com.iturrizaga.fairplay.support.makeUser
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthEffectTest {
    private val restoring = AuthState(session = AppSession.RESTORING)

    @Test fun onTask_withoutStoredToken_fails() = runBlocking<Unit> {
        val actions = authEffect(restoring, AuthAction.OnTask, FakeAPIClient(), FakeSessionStore())
        assertEquals(listOf(AuthAction.RestoreFailure), actions)
    }

    @Test fun onTask_withValidToken_logsIn() = runBlocking<Unit> {
        val client = FakeAPIClient().apply { currentUserResult = Result.success(makeUser()) }
        val actions = authEffect(restoring, AuthAction.OnTask, client, FakeSessionStore("stored"))
        assertEquals(listOf(AuthAction.LoginSuccess(makeUser(), "stored")), actions)
        assertEquals("stored", client.token)
    }

    @Test fun onTask_withRejectedToken_clearsIt() = runBlocking<Unit> {
        val client = FakeAPIClient().apply { currentUserResult = Result.failure(APIError.Unauthorized()) }
        val store = FakeSessionStore("stale")
        val actions = authEffect(restoring, AuthAction.OnTask, client, store)
        assertEquals(listOf(AuthAction.RestoreFailure), actions)
        assertNull(store.token)
        assertNull(client.token)
    }

    @Test fun onTask_offline_keepsTokenForNextLaunch() = runBlocking<Unit> {
        val client = FakeAPIClient().apply { currentUserResult = Result.failure(APIError.ServerError(503)) }
        val store = FakeSessionStore("stored")
        val actions = authEffect(restoring, AuthAction.OnTask, client, store)
        assertEquals(listOf(AuthAction.RestoreFailure), actions)
        assertEquals("stored", store.token)
    }

    @Test fun onTask_whenNotRestoring_doesNothing() = runBlocking<Unit> {
        val actions = authEffect(
            AuthState(session = AppSession.AUTHENTICATED), AuthAction.OnTask, FakeAPIClient(), FakeSessionStore("stored")
        )
        assertTrue(actions.isEmpty())
    }

    @Test fun loginSuccess_persistsToken() = runBlocking<Unit> {
        val client = FakeAPIClient()
        val store = FakeSessionStore()
        authEffect(AuthState(), AuthAction.LoginSuccess(makeUser(), "new"), client, store)
        assertEquals("new", store.token)
        assertEquals("new", client.token)
    }

    @Test fun logout_clearsTokenEverywhere() = runBlocking<Unit> {
        val client = FakeAPIClient().apply { token = "t" }
        val store = FakeSessionStore("t")
        authEffect(AuthState(), AuthAction.Logout, client, store)
        assertTrue(client.logoutCalled)
        assertNull(client.token)
        assertNull(store.token)
    }

    @Test fun sessionExpired_clearsTokenWithoutCallingServer() = runBlocking<Unit> {
        val client = FakeAPIClient().apply { token = "t" }
        val store = FakeSessionStore("t")
        authEffect(AuthState(), AuthAction.SessionExpired, client, store)
        assertFalse(client.logoutCalled)
        assertNull(client.token)
        assertNull(store.token)
    }
}
