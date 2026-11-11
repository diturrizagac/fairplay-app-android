package com.iturrizaga.fairplay.features.teamarrangement

import com.iturrizaga.fairplay.core.models.Match

sealed class TeamArrangementAction {
    data object Reset : TeamArrangementAction()
    data class Load(val match: Match) : TeamArrangementAction()
    data object Balance : TeamArrangementAction()
    data class BalanceSuccess(val match: Match) : TeamArrangementAction()
    data class BalanceFailure(val error: String) : TeamArrangementAction()
    data object Close : TeamArrangementAction()
}
