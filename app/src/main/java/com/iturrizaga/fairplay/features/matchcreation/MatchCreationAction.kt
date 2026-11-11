package com.iturrizaga.fairplay.features.matchcreation

import com.iturrizaga.fairplay.core.models.Match
import com.iturrizaga.fairplay.core.models.MatchType

sealed class MatchCreationAction {
    data object Reset : MatchCreationAction()
    data class SetName(val name: String) : MatchCreationAction()
    data class SetDate(val date: Long) : MatchCreationAction()
    data class SetLocation(val location: String) : MatchCreationAction()
    data class SetMatchType(val matchType: MatchType) : MatchCreationAction()
    data class SetNumTeams(val numTeams: Int) : MatchCreationAction()
    data object Submit : MatchCreationAction()
    data class SubmitSuccess(val match: Match) : MatchCreationAction()
    data class SubmitFailure(val error: String) : MatchCreationAction()
    data object Cancel : MatchCreationAction()
}
