package com.iturrizaga.fairplay.features.matches

import com.iturrizaga.fairplay.core.models.Match

sealed class MatchListAction {
    data object OnTask : MatchListAction()
    data class FetchSuccess(val matches: List<Match>) : MatchListAction()
    data class FetchFailure(val error: String) : MatchListAction()
    data class MarkRegistered(val matchId: String) : MatchListAction()
    data class Unregister(val matchId: String) : MatchListAction()
    data class AddMatch(val match: Match) : MatchListAction()
    data class ShowRegistration(val match: Match) : MatchListAction()
    data object ShowCreation : MatchListAction()
}
