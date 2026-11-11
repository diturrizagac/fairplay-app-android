package com.iturrizaga.fairplay.support

import com.iturrizaga.fairplay.core.models.Match
import com.iturrizaga.fairplay.core.models.Player
import com.iturrizaga.fairplay.core.models.PlayerPosition
import com.iturrizaga.fairplay.core.network.APIClient
import com.iturrizaga.fairplay.core.session.SessionStore
import com.iturrizaga.fairplay.features.auth.AuthUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class FakeSessionStore(var token: String? = null) : SessionStore {
    override fun loadToken(): String? = token
    override fun save(token: String) { this.token = token }
    override fun clear() { token = null }
}

class FakeAPIClient : APIClient {
    override var token: String? = null
    override val unauthorizedEvents: Flow<Unit> = emptyFlow()

    var currentUserResult: Result<AuthUser> = Result.failure(IllegalStateException("not stubbed"))
    var logoutCalled = false

    override suspend fun fetchCurrentUser(): AuthUser = currentUserResult.getOrThrow()
    override suspend fun signup(email: String, password: String, name: String): Pair<AuthUser, String> = unexpected()
    override suspend fun login(email: String, password: String): Pair<AuthUser, String> = unexpected()
    override suspend fun logout() { logoutCalled = true }
    override suspend fun createPlayerProfile(position: PlayerPosition, secondaryPosition: PlayerPosition?, birthDate: Long): Unit = unexpected()
    override suspend fun fetchPlayers(): List<Player> = unexpected()
    override suspend fun fetchMatches(): List<Match> = unexpected()
    override suspend fun createMatch(name: String, date: Long, location: String, maxPlayers: Int): Match = unexpected()
    override suspend fun registerForMatch(matchId: String): Unit = unexpected()
    override suspend fun unregisterFromMatch(matchId: String): Unit = unexpected()

    private fun unexpected(): Nothing = throw IllegalStateException("unexpected call")
}

fun makeUser(id: String = "u1", isAdmin: Boolean = false, playerId: String? = "p1") =
    AuthUser(id = id, email = "$id@example.com", name = id, isAdmin = isAdmin, playerId = playerId)

fun makeMatch(id: String = "m1") =
    Match(id = id, name = "Match $id", date = 0L, location = "Cancha", maxPlayers = 14, registeredCount = 0)

fun makePlayer(id: String = "p1") =
    Player(id = id, name = "Messi", position = PlayerPosition.FORWARD, ranking = 1)
