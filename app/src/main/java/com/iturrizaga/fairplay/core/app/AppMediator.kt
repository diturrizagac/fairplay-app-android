package com.iturrizaga.fairplay.core.app

import com.iturrizaga.fairplay.core.store.Effect
import com.iturrizaga.fairplay.features.auth.AuthAction
import com.iturrizaga.fairplay.features.matchcreation.MatchCreationAction
import com.iturrizaga.fairplay.features.matches.MatchListAction
import com.iturrizaga.fairplay.features.matchfinish.MatchFinishAction
import com.iturrizaga.fairplay.features.matchregistration.MatchRegistrationAction
import com.iturrizaga.fairplay.features.navigation.AppRoute
import com.iturrizaga.fairplay.features.navigation.NavigationAction
import com.iturrizaga.fairplay.features.onboarding.OnboardingAction
import com.iturrizaga.fairplay.features.players.PlayerDetailAction
import com.iturrizaga.fairplay.features.players.PlayerListAction
import com.iturrizaga.fairplay.features.teamarrangement.TeamArrangementAction

private fun push(route: AppRoute) = AppAction.Navigation(NavigationAction.Push(route))
private val pop = AppAction.Navigation(NavigationAction.Pop)

fun makeMediator(): Effect<AppState, AppAction> = { state, action ->
    when (action) {
        is AppAction.Auth -> authRoutes(action.action)
        is AppAction.Onboarding -> onboardingRoutes(state, action.action)
        is AppAction.MatchList -> matchListRoutes(action.action)
        is AppAction.MatchRegistration -> when (val a = action.action) {
            is MatchRegistrationAction.ConfirmSuccess -> listOf(
                AppAction.MatchList(MatchListAction.UpdateMatch(a.match)),
                AppAction.MatchList(MatchListAction.MarkRegistered(a.match.id)),
                pop
            )
            is MatchRegistrationAction.Cancel -> listOf(pop)
            else -> emptyList()
        }
        is AppAction.MatchCreation -> when (val a = action.action) {
            is MatchCreationAction.SubmitSuccess -> listOf(AppAction.MatchList(MatchListAction.AddMatch(a.match)), pop)
            is MatchCreationAction.Cancel -> listOf(pop)
            else -> emptyList()
        }
        is AppAction.TeamArrangement -> when (val a = action.action) {
            // Keep the list in sync so going back shows the READY status without a refresh.
            is TeamArrangementAction.BalanceSuccess -> listOf(AppAction.MatchList(MatchListAction.UpdateMatch(a.match)))
            is TeamArrangementAction.Close -> listOf(pop)
            else -> emptyList()
        }
        is AppAction.MatchFinish -> when (val a = action.action) {
            // Finishing changes every player's stats, so the player list must be reloaded.
            is MatchFinishAction.SubmitSuccess -> listOf(
                AppAction.MatchList(MatchListAction.UpdateMatch(a.match)),
                AppAction.PlayerList(PlayerListAction.Refresh),
                pop
            )
            is MatchFinishAction.Cancel -> listOf(pop)
            else -> emptyList()
        }
        is AppAction.PlayerList -> when (val a = action.action) {
            is PlayerListAction.ShowDetail -> listOf(
                AppAction.PlayerDetail(PlayerDetailAction.Load(a.player)),
                push(AppRoute.PlayerDetail)
            )
            else -> emptyList()
        }
        else -> emptyList()
    }
}

private fun authRoutes(action: AuthAction): List<AppAction> = when (action) {
    // Login/restore success: onboarding if there's no player profile yet, else home.
    is AuthAction.LoginSuccess -> {
        val mirrorUser = AppAction.MatchList(MatchListAction.SetCurrentUser(action.user.id, action.user.isAdmin))
        if (action.user.playerId == null) listOf(
            mirrorUser,
            AppAction.Onboarding(OnboardingAction.Setup(action.user, action.token)),
            push(AppRoute.Onboarding)
        ) else listOf(mirrorUser, AppAction.Navigation(NavigationAction.ClearStack))
    }
    // Back to initial state so the next account never sees this one's data.
    is AuthAction.Logout, is AuthAction.SessionExpired -> listOf(
        AppAction.Navigation(NavigationAction.ClearStack),
        AppAction.Onboarding(OnboardingAction.Reset),
        AppAction.PlayerList(PlayerListAction.Reset),
        AppAction.PlayerDetail(PlayerDetailAction.Reset),
        AppAction.MatchList(MatchListAction.Reset),
        AppAction.MatchRegistration(MatchRegistrationAction.Reset),
        AppAction.MatchCreation(MatchCreationAction.Reset),
        AppAction.TeamArrangement(TeamArrangementAction.Reset),
        AppAction.MatchFinish(MatchFinishAction.Reset)
    )
    else -> emptyList()
}

private fun onboardingRoutes(state: AppState, action: OnboardingAction): List<AppAction> = when (action) {
    // Profile created: complete the login with the new player id, which also clears navigation.
    is OnboardingAction.SubmitSuccess -> {
        val user = state.onboarding.pendingUser
        val token = state.onboarding.pendingToken
        if (user == null || token == null) listOf(AppAction.Navigation(NavigationAction.ClearStack))
        else listOf(AppAction.Auth(AuthAction.LoginSuccess(user.copy(playerId = action.playerId), token)))
    }
    else -> emptyList()
}

private fun matchListRoutes(action: MatchListAction): List<AppAction> = when (action) {
    is MatchListAction.ShowRegistration -> listOf(
        AppAction.MatchRegistration(MatchRegistrationAction.Show(action.match)),
        push(AppRoute.MatchRegistration)
    )
    is MatchListAction.ShowCreation -> listOf(push(AppRoute.MatchCreation))
    is MatchListAction.ShowArrangement -> listOf(
        AppAction.TeamArrangement(TeamArrangementAction.Load(action.match)),
        push(AppRoute.TeamArrangement)
    )
    is MatchListAction.ShowFinish -> listOf(
        AppAction.MatchFinish(MatchFinishAction.Load(action.match)),
        push(AppRoute.MatchFinish)
    )
    else -> emptyList()
}
