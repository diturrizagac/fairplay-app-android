package com.iturrizaga.fairplay.core.models

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.iturrizaga.fairplay.R

data class Player(
    val id: String,
    val name: String,
    val position: PlayerPosition,
    val ranking: Int,
    val photoUrl: String? = null
)

enum class PlayerPosition(@StringRes val nameRes: Int) {
    GOALKEEPER(R.string.position_goalkeeper),
    DEFENDER(R.string.position_defender),
    MIDFIELDER(R.string.position_midfielder),
    FORWARD(R.string.position_forward)
}

@Composable
fun PlayerPosition.displayName(): String = stringResource(nameRes)

data class Match(
    val id: String,
    val name: String,
    val date: Long,         // epoch millis
    val location: String,
    val maxPlayers: Int,
    val registeredCount: Int
)
