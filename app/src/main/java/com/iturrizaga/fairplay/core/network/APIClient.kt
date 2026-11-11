package com.iturrizaga.fairplay.core.network

import com.iturrizaga.fairplay.core.localization.Language
import com.iturrizaga.fairplay.core.models.Match
import com.iturrizaga.fairplay.core.models.Player
import com.iturrizaga.fairplay.core.models.PlayerPosition
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

interface APIClient {
    var token: String?
    /** Emits whenever a request sent with a token is rejected with 401. */
    val unauthorizedEvents: Flow<Unit>
    suspend fun fetchCurrentUser(): AuthUser
    suspend fun signup(email: String, password: String, name: String): Pair<AuthUser, String>
    suspend fun login(email: String, password: String): Pair<AuthUser, String>
    suspend fun logout()
    suspend fun createPlayerProfile(position: PlayerPosition, secondaryPosition: PlayerPosition?, birthDate: Long)
    suspend fun fetchPlayers(): List<Player>
    suspend fun fetchMatches(): List<Match>
    suspend fun createMatch(name: String, date: Long, location: String, maxPlayers: Int): Match
    suspend fun registerForMatch(matchId: String)
    suspend fun unregisterFromMatch(matchId: String)
}

class HttpAPIClient(environment: AppEnvironment, private val languageProvider: () -> Language = { Language.ENGLISH }) : APIClient {
    override var token: String? = null

    private val _unauthorizedEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    override val unauthorizedEvents: Flow<Unit> = _unauthorizedEvents.asSharedFlow()

    private val baseUrl = environment.baseUrl

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
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

    override suspend fun fetchCurrentUser(): AuthUser = withContext(Dispatchers.IO) {
        getObject("/auth/me").toAuthUser()
    }

    override suspend fun createPlayerProfile(position: PlayerPosition, secondaryPosition: PlayerPosition?, birthDate: Long): Unit = withContext(Dispatchers.IO) {
        val body = JSONObject().apply {
            put("position", position.name.lowercase())
            put("secondary_position", secondaryPosition?.name?.lowercase())
            put("birth_date", SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }.format(Date(birthDate)))
        }
        postVoid("/players/me", body)
    }

    override suspend fun logout(): Unit = withContext(Dispatchers.IO) {
        postVoid("/auth/logout")
    }

    override suspend fun fetchPlayers(): List<Player> = withContext(Dispatchers.IO) {
        val array = getArray("/players")
        (0 until array.length()).mapNotNull { i ->
            val obj = array.getJSONObject(i)
            val position = when (obj.getString("position")) {
                "goalkeeper" -> PlayerPosition.GOALKEEPER
                "defender"   -> PlayerPosition.DEFENDER
                "midfielder" -> PlayerPosition.MIDFIELDER
                "forward"    -> PlayerPosition.FORWARD
                else         -> return@mapNotNull null
            }
            Player(
                id       = obj.getString("id"),
                name     = obj.getString("name"),
                position = position,
                ranking  = obj.getInt("ranking"),
                photoUrl = if (obj.isNull("photo_url")) null else obj.getString("photo_url")
            )
        }
    }

    override suspend fun fetchMatches(): List<Match> = withContext(Dispatchers.IO) {
        val array = getArray("/matches")
        (0 until array.length()).map { i -> array.getJSONObject(i).toMatch() }
    }

    override suspend fun createMatch(name: String, date: Long, location: String, maxPlayers: Int): Match = withContext(Dispatchers.IO) {
        val body = JSONObject().apply {
            put("name", name)
            put("date", formatDate(date))
            put("location", location)
            put("max_players", maxPlayers)
        }
        postObject("/matches", body).toMatch()
    }

    override suspend fun registerForMatch(matchId: String): Unit = withContext(Dispatchers.IO) {
        postVoid("/matches/$matchId/register")
    }

    override suspend fun unregisterFromMatch(matchId: String): Unit = withContext(Dispatchers.IO) {
        deleteRequest("/matches/$matchId/register")
    }

    // MARK: - Helpers

    private fun JSONObject.toAuthUser() = AuthUser(
        id       = getString("id"),
        email    = getString("email"),
        name     = optString("name", ""),
        isAdmin  = optBoolean("is_admin", false),
        playerId = if (isNull("player_id")) null else getString("player_id")
    )

    private fun JSONObject.toMatch() = Match(
        id              = getString("id"),
        name            = getString("name"),
        date            = parseDate(getString("date")),
        location        = getString("location"),
        maxPlayers      = getInt("max_players"),
        registeredCount = getInt("registered_count")
    )

    private fun getObject(path: String): JSONObject {
        val conn = openConnection("GET", path)
        NetworkLogger.logRequest(conn.requestMethod, conn.url.toString(), conn.requestProperties.mapValues { it.value.joinToString() }, null)
        val body = readBody(conn)
        conn.disconnect()
        return JSONObject(body)
    }

    private fun getArray(path: String): JSONArray {
        val conn = openConnection("GET", path)
        NetworkLogger.logRequest(conn.requestMethod, conn.url.toString(), conn.requestProperties.mapValues { it.value.joinToString() }, null)
        val body = readBody(conn)
        conn.disconnect()
        return JSONArray(body)
    }

    private fun postObject(path: String, body: JSONObject, authorized: Boolean = true): JSONObject {
        val conn = openConnection("POST", path, authorized = authorized, hasBody = true)
        val bodyStr = body.toString()
        conn.outputStream.bufferedWriter().use { it.write(bodyStr) }
        NetworkLogger.logRequest(conn.requestMethod, conn.url.toString(), conn.requestProperties.mapValues { it.value.joinToString() }, bodyStr)
        val responseBody = readBody(conn)
        conn.disconnect()
        return JSONObject(responseBody)
    }

    private fun postVoid(path: String) {
        val conn = openConnection("POST", path, hasBody = false)
        NetworkLogger.logRequest(conn.requestMethod, conn.url.toString(), conn.requestProperties.mapValues { it.value.joinToString() }, null)
        val code = conn.responseCode
        NetworkLogger.logResponse(code, conn.url.toString(), "")
        validate(code)
        conn.disconnect()
    }

    private fun postVoid(path: String, body: JSONObject) {
        val conn = openConnection("POST", path, hasBody = true)
        val bodyStr = body.toString()
        conn.outputStream.bufferedWriter().use { it.write(bodyStr) }
        NetworkLogger.logRequest(conn.requestMethod, conn.url.toString(), conn.requestProperties.mapValues { it.value.joinToString() }, bodyStr)
        val code = conn.responseCode
        NetworkLogger.logResponse(code, conn.url.toString(), "")
        validate(code)
        conn.disconnect()
    }

    private fun deleteRequest(path: String) {
        val conn = openConnection("DELETE", path)
        NetworkLogger.logRequest(conn.requestMethod, conn.url.toString(), conn.requestProperties.mapValues { it.value.joinToString() }, null)
        val code = conn.responseCode
        NetworkLogger.logResponse(code, conn.url.toString(), "")
        validate(code)
        conn.disconnect()
    }

    private fun openConnection(
        method: String,
        path: String,
        authorized: Boolean = true,
        hasBody: Boolean = false
    ): HttpURLConnection {
        val conn = URL(baseUrl + path).openConnection() as HttpURLConnection
        conn.requestMethod = method
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("Accept", "application/json")
        conn.setRequestProperty("Accept-Language", languageProvider().code)
        if (authorized) token?.let { conn.setRequestProperty("Authorization", "Bearer $it") }
        if (hasBody) conn.doOutput = true
        conn.connectTimeout = 10_000
        conn.readTimeout = 10_000
        return conn
    }

    private fun readBody(conn: HttpURLConnection): String {
        val code = conn.responseCode
        val body = (if (code in 200..299) conn.inputStream else conn.errorStream)
            ?.bufferedReader()?.readText() ?: ""
        NetworkLogger.logResponse(code, conn.url.toString(), body)
        validate(code, body)
        return body
    }

    private fun validate(code: Int) {
        validate(code, "")
    }

    private fun validate(code: Int, body: String) {
        if (code in 200..299) return
        val errorDesc = parseErrorDescription(body)
        when (code) {
            401 -> {
                if (token != null) _unauthorizedEvents.tryEmit(Unit)
                throw APIError.Unauthorized()
            }
            else -> {
                if (errorDesc != null) throw APIError.Conflict(errorDesc)
                throw APIError.ServerError(code)
            }
        }
    }

    private fun parseErrorDescription(body: String): String? {
        return try {
            val json = JSONObject(body)
            val httpStatus = json.optInt("http_status", -1)
            val errorCode = json.optString("error_code", null)
            val errorDescription = json.optString("error_description", null)
            if (httpStatus != -1 && errorCode != null && errorDescription != null) {
                errorDescription
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun parseDate(raw: String): Long {
        val clean = raw.replace("Z", "").replace(Regex("\\+\\d{2}:\\d{2}$"), "").take(19)
        return try { dateFormat.parse(clean)?.time ?: 0L } catch (_: Exception) { 0L }
    }

    private fun formatDate(epochMillis: Long): String = dateFormat.format(Date(epochMillis))
}
