package com.iturrizaga.fairplay.features.matches

import com.iturrizaga.fairplay.core.models.Match

sealed class MatchListAction {
    data object Reset : MatchListAction()
    data object OnTask : MatchListAction()
    data object Refresh : MatchListAction()
    data class FetchSuccess(val matches: List<Match>) : MatchListAction()
    data class FetchFailure(val error: String) : MatchListAction()
    data class MarkRegistered(val matchId: String) : MatchListAction()
    data class Unregister(val matchId: String) : MatchListAction()
    data class UnregisterFailure(val matchId: String, val error: String) : MatchListAction()
    data class AddMatch(val match: Match) : MatchListAction()
    data class UpdateMatch(val match: Match) : MatchListAction()
    data class SetFilter(val filter: MatchListFilter) : MatchListAction()
    data class SetCurrentUser(val userId: String?, val isAdmin: Boolean) : MatchListAction()
    // Navigation triggers, handled by the mediator
    data class ShowRegistration(val match: Match) : MatchListAction()
    data object ShowCreation : MatchListAction()
    data class ShowArrangement(val match: Match) : MatchListAction()
    data class ShowFinish(val match: Match) : MatchListAction()
}
