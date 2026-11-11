package com.iturrizaga.fairplay.support

import com.iturrizaga.fairplay.core.models.Match
import com.iturrizaga.fairplay.core.models.MatchStatus
import com.iturrizaga.fairplay.core.models.MatchType
import com.iturrizaga.fairplay.core.models.Player
import com.iturrizaga.fairplay.core.models.PlayerPosition
import com.iturrizaga.fairplay.core.models.RegisteredPlayer
import com.iturrizaga.fairplay.core.models.Team
import com.iturrizaga.fairplay.core.models.TeamMember
import com.iturrizaga.fairplay.core.network.APIClient
import com.iturrizaga.fairplay.core.network.FinishTeamInput
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
    var getMatchResult: Result<Match> = Result.failure(IllegalStateException("not stubbed"))
    var logoutCalled = false
    var unregisteredMatchId: String? = null
    var finishedWith: List<FinishTeamInput>? = null

    override suspend fun fetchCurrentUser(): AuthUser = currentUserResult.getOrThrow()
    override suspend fun signup(email: String, password: String, name: String): Pair<AuthUser, String> = unexpected()
    override suspend fun login(email: String, password: String): Pair<AuthUser, String> = unexpected()
    override suspend fun logout() { logoutCalled = true }
    override suspend fun createPlayerProfile(position: PlayerPosition, secondaryPosition: PlayerPosition?, birthDate: Long): String = unexpected()
    override suspend fun fetchPlayers(): List<Player> = unexpected()
    override suspend fun fetchMatches(): List<Match> = unexpected()
    override suspend fun getMatch(matchId: String): Match = getMatchResult.getOrThrow()
    override suspend fun createMatch(name: String, date: Long, location: String, matchType: MatchType, numTeams: Int): Match = unexpected()
    override suspend fun registerForMatch(matchId: String): Unit = unexpected()
    override suspend fun unregisterFromMatch(matchId: String) { unregisteredMatchId = matchId }
    override suspend fun arrangeTeams(matchId: String): Match = unexpected()
    override suspend fun finishMatch(matchId: String, teams: List<FinishTeamInput>): Match {
        finishedWith = teams
        return makeMatch(id = matchId, status = MatchStatus.FINISHED)
    }

    private fun unexpected(): Nothing = throw IllegalStateException("unexpected call")
}

fun makeUser(id: String = "u1", isAdmin: Boolean = false, playerId: String? = "p1") =
    AuthUser(id = id, email = "$id@example.com", name = id, isAdmin = isAdmin, playerId = playerId)

fun makeMatch(
    id: String = "m1",
    status: MatchStatus = MatchStatus.CREATED,
    date: Long = 1_000L,
    finishedAt: Long? = null,
    registeredUserIds: List<String> = emptyList(),
    teams: List<Team> = emptyList()
) = Match(
    id = id,
    name = "Match $id",
    date = date,
    location = "Cancha",
    matchType = MatchType.FUTBOL_7,
    numTeams = 2,
    status = status,
    finishedAt = finishedAt,
    maxPlayers = 14,
    registeredCount = registeredUserIds.size,
    registeredPlayers = registeredUserIds.map {
        RegisteredPlayer(userId = it, playerId = "p-$it", name = it, position = PlayerPosition.MIDFIELDER, secondaryPosition = null, performance = 50.0)
    },
    teams = teams
)

fun makeTeam(id: String, memberIds: List<String>) = Team(
    id = id,
    name = "Team $id",
    score = null,
    result = null,
    members = memberIds.map { TeamMember(id = "m-$it", playerId = it, playerName = it, assignedPosition = PlayerPosition.MIDFIELDER, goalsScored = 0) }
)

fun makePlayer(id: String = "p1") = Player(
    id = id,
    userId = "u-$id",
    name = "Messi",
    position = PlayerPosition.FORWARD,
    secondaryPosition = null,
    wins = 0,
    losses = 0,
    totalGames = 0,
    goalsScored = 0,
    performance = 50.0,
    age = 30
)
