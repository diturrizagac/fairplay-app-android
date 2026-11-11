package com.iturrizaga.fairplay.core.app

import com.iturrizaga.fairplay.core.localization.Language
import com.iturrizaga.fairplay.features.auth.AuthAction
import com.iturrizaga.fairplay.features.matchcreation.MatchCreationAction
import com.iturrizaga.fairplay.features.matchfinish.MatchFinishAction
import com.iturrizaga.fairplay.features.matchregistration.MatchRegistrationAction
import com.iturrizaga.fairplay.features.matches.MatchListAction
import com.iturrizaga.fairplay.features.navigation.NavigationAction
import com.iturrizaga.fairplay.features.onboarding.OnboardingAction
import com.iturrizaga.fairplay.features.players.PlayerDetailAction
import com.iturrizaga.fairplay.features.players.PlayerListAction
import com.iturrizaga.fairplay.features.teamarrangement.TeamArrangementAction

sealed class AppAction {
    data class Navigation(val action: NavigationAction) : AppAction()
    data class Auth(val action: AuthAction) : AppAction()
    data class Onboarding(val action: OnboardingAction) : AppAction()
    data class PlayerList(val action: PlayerListAction) : AppAction()
    data class PlayerDetail(val action: PlayerDetailAction) : AppAction()
    data class MatchList(val action: MatchListAction) : AppAction()
    data class MatchRegistration(val action: MatchRegistrationAction) : AppAction()
    data class MatchCreation(val action: MatchCreationAction) : AppAction()
    data class TeamArrangement(val action: TeamArrangementAction) : AppAction()
    data class MatchFinish(val action: MatchFinishAction) : AppAction()
    data class SetLanguage(val language: Language) : AppAction()
}
