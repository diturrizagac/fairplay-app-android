package com.iturrizaga.fairplay.features.matchregistration

import com.iturrizaga.fairplay.core.models.Match

sealed class MatchRegistrationAction {
    data object Reset : MatchRegistrationAction()
    data class Show(val match: Match) : MatchRegistrationAction()
    data object Confirm : MatchRegistrationAction()
    data class ConfirmSuccess(val match: Match) : MatchRegistrationAction()
    data class ConfirmFailure(val error: String) : MatchRegistrationAction()
    data object Cancel : MatchRegistrationAction()
}
