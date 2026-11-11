package com.iturrizaga.fairplay.core.network

import com.iturrizaga.fairplay.core.localization.Language
import com.iturrizaga.fairplay.core.models.Match
import com.iturrizaga.fairplay.core.models.MatchStatus
import com.iturrizaga.fairplay.core.models.MatchType
import com.iturrizaga.fairplay.core.models.Player
import com.iturrizaga.fairplay.core.models.PlayerPosition
import com.iturrizaga.fairplay.core.models.RegisteredPlayer
import com.iturrizaga.fairplay.core.models.Team
import com.iturrizaga.fairplay.core.models.TeamMember
import com.iturrizaga.fairplay.core.models.TeamResult
import com.iturrizaga.fairplay.features.auth.AuthUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/** One entry of POST /matches/{id}/finish. */
data class FinishTeamInput(
    val teamId: String,
    val score: Int,
    val playerGoals: Map<String, Int>
)

interface APIClient {
    var token: String?
    /** Emits whenever a request sent with a token is rejected with 401. */
    val unauthorizedEvents: Flow<Unit>

    // Auth
    suspend fun fetchCurrentUser(): AuthUser
    suspend fun signup(email: String, password: String, name: String): Pair<AuthUser, String>
    suspend fun login(email: String, password: String): Pair<AuthUser, String>
    suspend fun logout()

    // Players
    /** Returns the new player's id. */
    suspend fun createPlayerProfile(position: PlayerPosition, secondaryPosition: PlayerPosition?, birthDate: Long): String
    suspend fun fetchPlayers(): List<Player>

    // Matches
    suspend fun fetchMatches(): List<Match>
    suspend fun getMatch(matchId: String): Match
    suspend fun createMatch(name: String, date: Long, location: String, matchType: MatchType, numTeams: Int): Match
    suspend fun registerForMatch(matchId: String)
    suspend fun unregisterFromMatch(matchId: String)

    // Admin: match lifecycle
    suspend fun arrangeTeams(matchId: String): Match
    suspend fun finishMatch(matchId: String, teams: List<FinishTeamInput>): Match
}

class HttpAPIClient(environment: AppEnvironment, private val languageProvider: () -> Language = { Language.ENGLISH }) : APIClient {
    override var token: String? = null

    private val _unauthorizedEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    override val unauthorizedEvents: Flow<Unit> = _unauthorizedEvents.asSharedFlow()

    private val baseUrl = environment.baseUrl

    // MARK: - Auth

    override suspend fun fetchCurrentUser(): AuthUser = withContext(Dispatchers.IO) {
        getObject("/auth/me").toAuthUser()
    }

    override suspend fun signup(email: String, password: String, name: String): Pair<AuthUser, String> = withContext(Dispatchers.IO) {
        val body = JSONObject().apply {
            put("email", email)
            put("password", password)
            put("name", name)
        }
        val json = postObject("/auth/signup", body, authorized = false)
        Pair(json.getJSONObject("user").toAuthUser(), json.getString("access_token"))
    }

    override suspend fun login(email: String, password: String): Pair<AuthUser, String> = withContext(Dispatchers.IO) {
        val body = JSONObject().apply {
            put("email", email)
            put("password", password)
        }
        val json = postObject("/auth/login", body, authorized = false)
        Pair(json.getJSONObject("user").toAuthUser(), json.getString("access_token"))
    }

    override suspend fun logout(): Unit = withContext(Dispatchers.IO) {
        postVoid("/auth/logout")
    }

    // MARK: - Players

    override suspend fun createPlayerProfile(position: PlayerPosition, secondaryPosition: PlayerPosition?, birthDate: Long): String = withContext(Dispatchers.IO) {
        val body = JSONObject().apply {
            put("position", position.apiValue)
            put("secondary_position", secondaryPosition?.apiValue ?: JSONObject.NULL)
            put("birth_date", ApiDates.formatDate(birthDate))
        }
        postObject("/players/me", body).getString("id")
    }

    override suspend fun fetchPlayers(): List<Player> = withContext(Dispatchers.IO) {
        getArray("/players").objects().mapNotNull { it.toPlayer() }
    }

    // MARK: - Matches

    override suspend fun fetchMatches(): List<Match> = withContext(Dispatchers.IO) {
        getArray("/matches").objects().map { it.toMatch() }
    }

    override suspend fun getMatch(matchId: String): Match = withContext(Dispatchers.IO) {
        getObject("/matches/$matchId").toMatch()
    }

    override suspend fun createMatch(name: String, date: Long, location: String, matchType: MatchType, numTeams: Int): Match = withContext(Dispatchers.IO) {
        val body = JSONObject().apply {
            put("name", name)
            put("date", ApiDates.formatDateTime(date))
            put("location", location)
            put("match_type", matchType.apiValue)
            put("num_teams", numTeams)
        }
        postObject("/matches", body).toMatch()
    }

    override suspend fun registerForMatch(matchId: String): Unit = withContext(Dispatchers.IO) {
        postVoid("/matches/$matchId/register")
    }

    override suspend fun unregisterFromMatch(matchId: String): Unit = withContext(Dispatchers.IO) {
        deleteRequest("/matches/$matchId/register")
    }

    // MARK: - Admin: match lifecycle

    override suspend fun arrangeTeams(matchId: String): Match = withContext(Dispatchers.IO) {
        postObject("/matches/$matchId/teams", null).toMatch()
    }

    override suspend fun finishMatch(matchId: String, teams: List<FinishTeamInput>): Match = withContext(Dispatchers.IO) {
        val teamsJson = JSONArray()
        teams.forEach { team ->
            teamsJson.put(JSONObject().apply {
                put("team_id", team.teamId)
                put("score", team.score)
                put("player_goals", JSONObject(team.playerGoals.filterValues { it > 0 }))
            })
        }
        postObject("/matches/$matchId/finish", JSONObject().put("teams", teamsJson)).toMatch()
    }

    // MARK: - Parsing

    private fun JSONArray.objects(): List<JSONObject> = (0 until length()).map { getJSONObject(it) }

    private fun JSONObject.optStringOrNull(key: String): String? = if (isNull(key)) null else getString(key)

    private fun JSONObject.toAuthUser() = AuthUser(
        id       = getString("id"),
        email    = getString("email"),
        name     = optString("name", ""),
        isAdmin  = optBoolean("is_admin", false),
        playerId = optStringOrNull("player_id")
    )

    private fun JSONObject.toPlayer(): Player? {
        val position = PlayerPosition.fromApi(getString("position")) ?: return null
        return Player(
            id                = getString("id"),
            userId            = optStringOrNull("user_id"),
            name              = getString("name"),
            position          = position,
            secondaryPosition = PlayerPosition.fromApi(optStringOrNull("secondary_position")),
            wins              = getInt("wins"),
            losses            = getInt("losses"),
            totalGames        = getInt("total_games"),
            goalsScored       = getInt("goals_scored"),
            performance       = getDouble("performance"),
            age               = getInt("age"),
            photoUrl          = optStringOrNull("photo_url")
        )
    }

    private fun JSONObject.toMatch() = Match(
        id                = getString("id"),
        name              = getString("name"),
        date              = ApiDates.parseDateTime(getString("date")) ?: 0L,
        location          = getString("location"),
        matchType         = MatchType.fromApi(getString("match_type")),
        numTeams          = getInt("num_teams"),
        status            = MatchStatus.fromApi(getString("status")),
        finishedAt        = optStringOrNull("finished_at")?.let { ApiDates.parseDateTime(it) },
        maxPlayers        = getInt("max_players"),
        registeredCount   = getInt("registered_count"),
        registeredPlayers = optJSONArray("registered_players")?.objects()?.mapNotNull { it.toRegisteredPlayer() }.orEmpty(),
        teams             = optJSONArray("teams")?.objects()?.map { it.toTeam() }.orEmpty()
    )

    private fun JSONObject.toRegisteredPlayer(): RegisteredPlayer? {
        val position = PlayerPosition.fromApi(getString("position")) ?: return null
        return RegisteredPlayer(
            userId            = getString("user_id"),
            playerId          = getString("player_id"),
            name              = getString("name"),
            position          = position,
            secondaryPosition = PlayerPosition.fromApi(optStringOrNull("secondary_position")),
            performance       = getDouble("performance")
        )
    }

    private fun JSONObject.toTeam() = Team(
        id      = getString("id"),
        name    = getString("name"),
        score   = if (isNull("score")) null else getInt("score"),
        result  = TeamResult.fromApi(optStringOrNull("result")),
        members = getJSONArray("members").objects().map { member ->
            TeamMember(
                id               = member.getString("id"),
                playerId         = member.getString("player_id"),
                playerName       = member.getString("player_name"),
                assignedPosition = PlayerPosition.fromApi(member.getString("assigned_position")) ?: PlayerPosition.MIDFIELDER,
                goalsScored      = member.getInt("goals_scored")
            )
        }
    )

    // MARK: - HTTP

    private fun getObject(path: String): JSONObject = JSONObject(send("GET", path))

    private fun getArray(path: String): JSONArray = JSONArray(send("GET", path))

    private fun postObject(path: String, body: JSONObject?, authorized: Boolean = true): JSONObject =
        JSONObject(send("POST", path, body, authorized))

    private fun postVoid(path: String) {
        send("POST", path)
    }

    private fun deleteRequest(path: String) {
        send("DELETE", path)
    }

    private fun send(method: String, path: String, body: JSONObject? = null, authorized: Boolean = true): String {
        val conn = URL(baseUrl + path).openConnection() as HttpURLConnection
        try {
            conn.requestMethod = method
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("Accept", "application/json")
            conn.setRequestProperty("Accept-Language", languageProvider().code)
            if (authorized) token?.let { conn.setRequestProperty("Authorization", "Bearer $it") }
            conn.connectTimeout = 10_000
            conn.readTimeout = 10_000
            val bodyStr = body?.toString()
            NetworkLogger.logRequest(method, conn.url.toString(), conn.requestProperties.mapValues { it.value.joinToString() }, bodyStr)
            if (bodyStr != null) {
                conn.doOutput = true
                conn.outputStream.bufferedWriter().use { it.write(bodyStr) }
            }
            val code = conn.responseCode
            val response = (if (code in 200..299) conn.inputStream else conn.errorStream)
                ?.bufferedReader()?.readText() ?: ""
            NetworkLogger.logResponse(code, conn.url.toString(), response)
            validate(code, response, sentToken = authorized && token != null)
            return response
        } finally {
            conn.disconnect()
        }
    }

    private fun validate(code: Int, body: String, sentToken: Boolean) {
        if (code in 200..299) return
        when (code) {
            401 -> {
                if (sentToken) _unauthorizedEvents.tryEmit(Unit)
                throw APIError.Unauthorized()
            }
            else -> {
                val errorDesc = parseErrorDescription(body)
                if (errorDesc != null) throw APIError.Conflict(errorDesc)
                throw APIError.ServerError(code)
            }
        }
    }

    private fun parseErrorDescription(body: String): String? = try {
        JSONObject(body).optString("error_description").takeIf { it.isNotEmpty() }
    } catch (_: Exception) {
        null
    }
}
