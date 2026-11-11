package com.iturrizaga.fairplay.core.app

import com.iturrizaga.fairplay.core.store.Effect
import com.iturrizaga.fairplay.features.auth.AuthAction
import com.iturrizaga.fairplay.features.matchcreation.MatchCreationAction
import com.iturrizaga.fairplay.features.matchregistration.MatchRegistrationAction
import com.iturrizaga.fairplay.features.matches.MatchListAction
import com.iturrizaga.fairplay.features.navigation.AppRoute
import com.iturrizaga.fairplay.features.navigation.NavigationAction
import com.iturrizaga.fairplay.features.onboarding.OnboardingAction
import com.iturrizaga.fairplay.features.players.PlayerDetailAction
import com.iturrizaga.fairplay.features.players.PlayerListAction

fun makeMediator(): Effect<AppState, AppAction> = { _, action ->
    when {
        // Auth: login/signup success — go to onboarding if no player profile, else home
        action is AppAction.Auth && action.action is AuthAction.LoginSuccess -> {
            val success = action.action as AuthAction.LoginSuccess
            if (success.user.playerId == null) {
                listOf(
                    AppAction.Onboarding(OnboardingAction.Setup(success.user, success.token)),
                    AppAction.Navigation(NavigationAction.Push(AppRoute.Onboarding))
                )
            } else {
                listOf(AppAction.Navigation(NavigationAction.ClearStack))
            }
        }

        // Onboarding complete — go to home
        action is AppAction.Onboarding && action.action is OnboardingAction.SubmitSuccess -> {
            listOf(AppAction.Navigation(NavigationAction.ClearStack))
        }


        // Player detail
        action is AppAction.PlayerList && action.action is PlayerListAction.ShowDetail -> {
            val player = (action.action as PlayerListAction.ShowDetail).player
            listOf(
                AppAction.PlayerDetail(PlayerDetailAction.Load(player)),
                AppAction.Navigation(NavigationAction.Push(AppRoute.PlayerDetail))
            )
        }

        // Match registration
        action is AppAction.MatchList && action.action is MatchListAction.ShowRegistration -> {
            val match = (action.action as MatchListAction.ShowRegistration).match
            listOf(
                AppAction.MatchRegistration(MatchRegistrationAction.Show(match)),
                AppAction.Navigation(NavigationAction.Push(AppRoute.MatchRegistration))
            )
        }

        // Match creation
        action is AppAction.MatchList && action.action is MatchListAction.ShowCreation -> {
            listOf(AppAction.Navigation(NavigationAction.Push(AppRoute.MatchCreation)))
        }

        // Registration success
        action is AppAction.MatchRegistration && action.action is MatchRegistrationAction.ConfirmSuccess -> {
            val matchId = (action.action as MatchRegistrationAction.ConfirmSuccess).matchId
            listOf(
                AppAction.MatchList(MatchListAction.MarkRegistered(matchId)),
                AppAction.Navigation(NavigationAction.Pop)
            )
        }

        // Registration cancel
        action is AppAction.MatchRegistration && action.action is MatchRegistrationAction.Cancel -> {
            listOf(AppAction.Navigation(NavigationAction.Pop))
        }

        // Creation success
        action is AppAction.MatchCreation && action.action is MatchCreationAction.SubmitSuccess -> {
            val match = (action.action as MatchCreationAction.SubmitSuccess).match
            listOf(
                AppAction.MatchList(MatchListAction.AddMatch(match)),
                AppAction.Navigation(NavigationAction.Pop)
            )
        }

        else -> emptyList()
    }
}
