package com.iturrizaga.fairplay.features.matchcreation

import com.iturrizaga.fairplay.core.models.MatchType

data class MatchCreationState(
    val name: String = "",
    val date: Long = System.currentTimeMillis() + ONE_DAY_MILLIS,
    val location: String = "",
    val matchType: MatchType = MatchType.FUTBOL_7,
    val numTeams: Int = 2,
    val isSubmitting: Boolean = false,
    val error: String? = null
) {
    val maxPlayers: Int get() = matchType.playersPerTeam * numTeams
    val canSubmit: Boolean get() = name.isNotBlank() && location.isNotBlank() && !isSubmitting

    companion object {
        /** The API only accepts 2 or 4 teams. */
        val allowedNumTeams = listOf(2, 4)
        private const val ONE_DAY_MILLIS = 24 * 60 * 60 * 1000L
    }
}
