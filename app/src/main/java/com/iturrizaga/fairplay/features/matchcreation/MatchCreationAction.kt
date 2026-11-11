package com.iturrizaga.fairplay.features.matchcreation

import com.iturrizaga.fairplay.core.models.Match

sealed class MatchCreationAction {
    data class SetName(val name: String) : MatchCreationAction()
    data class SetDate(val date: Long) : MatchCreationAction()
    data class SetLocation(val location: String) : MatchCreationAction()
    data class SetMaxPlayers(val count: Int) : MatchCreationAction()
    data object Submit : MatchCreationAction()
    data class SubmitSuccess(val match: Match) : MatchCreationAction()
    data class SubmitFailure(val error: String) : MatchCreationAction()
}
