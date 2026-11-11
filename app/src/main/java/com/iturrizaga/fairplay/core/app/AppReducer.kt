package com.iturrizaga.fairplay.core.app

import com.iturrizaga.fairplay.features.auth.authReducer
import com.iturrizaga.fairplay.features.matchcreation.matchCreationReducer
import com.iturrizaga.fairplay.features.matchfinish.matchFinishReducer
import com.iturrizaga.fairplay.features.matchregistration.matchRegistrationReducer
import com.iturrizaga.fairplay.features.matches.matchListReducer
import com.iturrizaga.fairplay.features.navigation.navigationReducer
import com.iturrizaga.fairplay.features.onboarding.onboardingReducer
import com.iturrizaga.fairplay.features.players.playerDetailReducer
import com.iturrizaga.fairplay.features.players.playerListReducer
import com.iturrizaga.fairplay.features.teamarrangement.teamArrangementReducer

fun appReducer(state: AppState, action: AppAction): AppState = when (action) {
    is AppAction.Navigation      -> state.copy(navigation      = navigationReducer(state.navigation, action.action))
    is AppAction.Auth            -> state.copy(auth            = authReducer(state.auth, action.action))
    is AppAction.Onboarding      -> state.copy(onboarding      = onboardingReducer(state.onboarding, action.action))
    is AppAction.PlayerList      -> state.copy(playerList      = playerListReducer(state.playerList, action.action))
    is AppAction.PlayerDetail    -> state.copy(playerDetail    = playerDetailReducer(state.playerDetail, action.action))
    is AppAction.MatchList       -> state.copy(matchList       = matchListReducer(state.matchList, action.action))
    is AppAction.MatchRegistration -> state.copy(matchRegistration = matchRegistrationReducer(state.matchRegistration, action.action))
    is AppAction.MatchCreation   -> state.copy(matchCreation   = matchCreationReducer(state.matchCreation, action.action))
    is AppAction.TeamArrangement -> state.copy(teamArrangement = teamArrangementReducer(state.teamArrangement, action.action))
    is AppAction.MatchFinish     -> state.copy(matchFinish     = matchFinishReducer(state.matchFinish, action.action))
    is AppAction.SetLanguage     -> state.copy(language        = action.language)
}
