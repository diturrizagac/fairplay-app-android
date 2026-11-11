package com.iturrizaga.fairplay.core.models

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.iturrizaga.fairplay.R

enum class PlayerPosition(val apiValue: String, @StringRes val nameRes: Int) {
    GOALKEEPER("goalkeeper", R.string.position_goalkeeper),
    DEFENDER("defender", R.string.position_defender),
    MIDFIELDER("midfielder", R.string.position_midfielder),
    FORWARD("forward", R.string.position_forward);

    companion object {
        fun fromApi(value: String?): PlayerPosition? = entries.firstOrNull { it.apiValue == value }
    }
}

enum class MatchType(val apiValue: String, val playersPerTeam: Int, @StringRes val nameRes: Int) {
    FUTBOL_7("futbol_7", 7, R.string.match_type_f7),
    FUTBOL_11("futbol_11", 11, R.string.match_type_f11);

    companion object {
        fun fromApi(value: String): MatchType = entries.firstOrNull { it.apiValue == value } ?: FUTBOL_7
    }
}

enum class MatchStatus(val apiValue: String, @StringRes val nameRes: Int) {
    CREATED("created", R.string.match_status_created),
    READY("ready", R.string.match_status_ready),
    FINISHED("finished", R.string.match_status_finished);

    companion object {
        fun fromApi(value: String): MatchStatus = entries.firstOrNull { it.apiValue == value } ?: CREATED
    }
}

enum class TeamResult(val apiValue: String, @StringRes val nameRes: Int) {
    WIN("win", R.string.team_result_win),
    LOSS("loss", R.string.team_result_loss),
    DRAW("draw", R.string.team_result_draw);

    companion object {
        fun fromApi(value: String?): TeamResult? = entries.firstOrNull { it.apiValue == value }
    }
}

@Composable
fun PlayerPosition.displayName(): String = stringResource(nameRes)

data class Player(
    val id: String,
    val userId: String?,
    val name: String,
    val position: PlayerPosition,
    val secondaryPosition: PlayerPosition?,
    val wins: Int,
    val losses: Int,
    val totalGames: Int,
    val goalsScored: Int,
    val performance: Double,
    val age: Int,
    val photoUrl: String? = null
)

data class RegisteredPlayer(
    val userId: String,
    val playerId: String,
    val name: String,
    val position: PlayerPosition,
    val secondaryPosition: PlayerPosition?,
    val performance: Double
)

data class TeamMember(
    val id: String,
    val playerId: String,
    val playerName: String,
    val assignedPosition: PlayerPosition,
    val goalsScored: Int
)

data class Team(
    val id: String,
    val name: String,
    val score: Int?,
    val result: TeamResult?,
    val members: List<TeamMember>
)

data class Match(
    val id: String,
    val name: String,
    val date: Long,         // epoch millis
    val location: String,
    val matchType: MatchType,
    val numTeams: Int,
    val status: MatchStatus,
    val finishedAt: Long?,
    val maxPlayers: Int,
    val registeredCount: Int,
    val registeredPlayers: List<RegisteredPlayer> = emptyList(),
    val teams: List<Team> = emptyList()
) {
    val isUpcoming: Boolean get() = status != MatchStatus.FINISHED
    val isFull: Boolean get() = registeredCount >= maxPlayers
}
