package com.iturrizaga.fairplay.core.app

import com.iturrizaga.fairplay.core.localization.Language
import com.iturrizaga.fairplay.features.auth.AuthState
import com.iturrizaga.fairplay.features.matchcreation.MatchCreationState
import com.iturrizaga.fairplay.features.matchfinish.MatchFinishState
import com.iturrizaga.fairplay.features.matchregistration.MatchRegistrationState
import com.iturrizaga.fairplay.features.matches.MatchListState
import com.iturrizaga.fairplay.features.navigation.NavigationState
import com.iturrizaga.fairplay.features.onboarding.OnboardingState
import com.iturrizaga.fairplay.features.players.PlayerDetailState
import com.iturrizaga.fairplay.features.players.PlayerListState
import com.iturrizaga.fairplay.features.teamarrangement.TeamArrangementState

data class AppState(
    val navigation: NavigationState = NavigationState(),
    val auth: AuthState = AuthState(),
    val onboarding: OnboardingState = OnboardingState(),
    val playerList: PlayerListState = PlayerListState(),
    val playerDetail: PlayerDetailState = PlayerDetailState(),
    val matchList: MatchListState = MatchListState(),
    val matchRegistration: MatchRegistrationState = MatchRegistrationState(),
    val matchCreation: MatchCreationState = MatchCreationState(),
    val teamArrangement: TeamArrangementState = TeamArrangementState(),
    val matchFinish: MatchFinishState = MatchFinishState(),
    val language: Language = Language.system
)
