package com.iturrizaga.fairplay.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.iturrizaga.fairplay.core.app.AppAction
import com.iturrizaga.fairplay.core.app.AppState
import com.iturrizaga.fairplay.core.app.AppViewModel
import com.iturrizaga.fairplay.features.auth.AppSession
import com.iturrizaga.fairplay.features.auth.AuthAction
import com.iturrizaga.fairplay.features.matchcreation.MatchCreationAction
import com.iturrizaga.fairplay.features.matches.MatchListAction
import com.iturrizaga.fairplay.features.matchfinish.MatchFinishAction
import com.iturrizaga.fairplay.features.matchregistration.MatchRegistrationAction
import com.iturrizaga.fairplay.features.navigation.AppRoute
import com.iturrizaga.fairplay.features.navigation.NavigationAction
import com.iturrizaga.fairplay.features.players.PlayerDetailAction
import com.iturrizaga.fairplay.features.players.PlayerListAction
import com.iturrizaga.fairplay.features.teamarrangement.TeamArrangementAction
import com.iturrizaga.fairplay.ui.screens.AuthScreen
import com.iturrizaga.fairplay.ui.screens.HomeScreen
import com.iturrizaga.fairplay.ui.screens.MatchCreationScreen
import com.iturrizaga.fairplay.ui.screens.MatchFinishScreen
import com.iturrizaga.fairplay.ui.screens.MatchListScreen
import com.iturrizaga.fairplay.ui.screens.MatchRegistrationScreen
import com.iturrizaga.fairplay.ui.screens.OnboardingScreen
import com.iturrizaga.fairplay.ui.screens.PlayerDetailScreen
import com.iturrizaga.fairplay.ui.screens.PlayerListScreen
import com.iturrizaga.fairplay.ui.screens.ProfileScreen
import com.iturrizaga.fairplay.ui.screens.SignupScreen
import com.iturrizaga.fairplay.ui.screens.TeamArrangementScreen

@Composable
fun AppContent(viewModel: AppViewModel) {
    val state by viewModel.state.collectAsState()
    val send = viewModel::send

    LaunchedEffect(Unit) {
        send(AppAction.Auth(AuthAction.OnTask))
    }

    if (state.navigation.pendingPush != null) {
        send(AppAction.Navigation(NavigationAction.DidNavigate))
    }

    if (state.navigation.pendingPop) {
        send(AppAction.Navigation(NavigationAction.DidPop))
    }

    val currentRoute = state.navigation.stack.lastOrNull()
    // Onboarding can't be skipped: the account has no player profile yet.
    BackHandler(enabled = currentRoute != null && currentRoute != AppRoute.Onboarding) {
        send(backAction(currentRoute))
    }

    when {
        state.auth.session == AppSession.RESTORING -> LaunchScreen()
        currentRoute != null -> RenderRoute(currentRoute, state, send)
        state.auth.session == AppSession.UNAUTHENTICATED -> AuthScreen(
            state = state.auth,
            currentLanguage = state.language,
            onLogin = { email, password -> send(AppAction.Auth(AuthAction.Login(email, password))) },
            onSignupClick = { send(AppAction.Navigation(NavigationAction.Push(AppRoute.Signup))) },
            onLanguageChange = { send(AppAction.SetLanguage(it)) }
        )
        else -> Home(state, send)
    }
}

/** Leaving a screen through its feature's own action lets the feature reset its state. */
private fun backAction(route: AppRoute?): AppAction = when (route) {
    AppRoute.MatchRegistration -> AppAction.MatchRegistration(MatchRegistrationAction.Cancel)
    AppRoute.MatchCreation -> AppAction.MatchCreation(MatchCreationAction.Cancel)
    AppRoute.TeamArrangement -> AppAction.TeamArrangement(TeamArrangementAction.Close)
    AppRoute.MatchFinish -> AppAction.MatchFinish(MatchFinishAction.Cancel)
    else -> AppAction.Navigation(NavigationAction.Pop)
}

@Composable
private fun Home(state: AppState, send: (AppAction) -> Unit) {
    val user = state.auth.currentUser
    HomeScreen(
        isAdmin = user?.isAdmin == true,
        onCreateMatch = { send(AppAction.MatchList(MatchListAction.ShowCreation)) },
        onRefreshMatches = { send(AppAction.MatchList(MatchListAction.Refresh)) },
        onRefreshPlayers = { send(AppAction.PlayerList(PlayerListAction.Refresh)) },
        matches = { modifier ->
            MatchListScreen(state.matchList, { send(AppAction.MatchList(it)) }, modifier)
        },
        players = { modifier ->
            PlayerListScreen(state.playerList, { send(AppAction.PlayerList(it)) }, modifier)
        },
        profile = { modifier ->
            // The player list is the source of the player's stats; load it if the tab wasn't opened yet.
            LaunchedEffect(Unit) { send(AppAction.PlayerList(PlayerListAction.OnTask)) }
            ProfileScreen(
                user = user,
                player = state.playerList.players.firstOrNull { it.id == user?.playerId },
                language = state.language,
                onLanguageChange = { send(AppAction.SetLanguage(it)) },
                onLogout = { send(AppAction.Auth(AuthAction.Logout)) },
                modifier = modifier
            )
        }
    )
}

@Composable
private fun RenderRoute(route: AppRoute, state: AppState, send: (AppAction) -> Unit) {
    when (route) {
        AppRoute.Signup -> SignupScreen(
            state = state.auth,
            onSignup = { email, password, name -> send(AppAction.Auth(AuthAction.Signup(email, password, name))) },
            onBackClick = { send(AppAction.Navigation(NavigationAction.Pop)) },
            onLoginClick = { send(AppAction.Navigation(NavigationAction.Pop)) }
        )
        AppRoute.Onboarding -> OnboardingScreen(
            state = state.onboarding,
            onAction = { send(AppAction.Onboarding(it)) }
        )
        AppRoute.PlayerDetail -> PlayerDetailScreen(
            player = state.playerDetail.player,
            onBack = {
                send(AppAction.PlayerDetail(PlayerDetailAction.Dismiss))
                send(AppAction.Navigation(NavigationAction.Pop))
            }
        )
        AppRoute.MatchRegistration -> MatchRegistrationScreen(
            state = state.matchRegistration,
            isRegistered = state.matchRegistration.match?.let { it.id in state.matchList.registeredMatchIds } == true,
            onAction = { send(AppAction.MatchRegistration(it)) }
        )
        AppRoute.MatchCreation -> MatchCreationScreen(
            state = state.matchCreation,
            onAction = { send(AppAction.MatchCreation(it)) }
        )
        AppRoute.TeamArrangement -> TeamArrangementScreen(
            state = state.teamArrangement,
            onAction = { send(AppAction.TeamArrangement(it)) }
        )
        AppRoute.MatchFinish -> MatchFinishScreen(
            state = state.matchFinish,
            onAction = { send(AppAction.MatchFinish(it)) }
        )
    }
}

@Composable
private fun LaunchScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
    }
}
