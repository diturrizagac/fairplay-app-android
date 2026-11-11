package com.iturrizaga.fairplay.features.matchfinish

import com.iturrizaga.fairplay.core.models.Match

sealed class MatchFinishAction {
    data object Reset : MatchFinishAction()
    data class Load(val match: Match) : MatchFinishAction()
    data class SetScore(val teamId: String, val score: Int) : MatchFinishAction()
    data class SetGoals(val teamId: String, val playerId: String, val goals: Int) : MatchFinishAction()
    data object Submit : MatchFinishAction()
    data class SubmitSuccess(val match: Match) : MatchFinishAction()
    data class SubmitFailure(val error: String) : MatchFinishAction()
    data object Cancel : MatchFinishAction()
}
