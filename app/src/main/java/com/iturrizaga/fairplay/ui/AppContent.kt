package com.iturrizaga.fairplay.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.iturrizaga.fairplay.core.app.AppAction
import com.iturrizaga.fairplay.core.app.AppState
import com.iturrizaga.fairplay.core.app.AppViewModel
import com.iturrizaga.fairplay.features.auth.AuthAction
import com.iturrizaga.fairplay.features.navigation.AppRoute
import com.iturrizaga.fairplay.features.navigation.NavigationAction
import com.iturrizaga.fairplay.features.onboarding.OnboardingAction
import com.iturrizaga.fairplay.ui.screens.AuthScreen
import com.iturrizaga.fairplay.ui.screens.OnboardingScreen
import com.iturrizaga.fairplay.ui.screens.SignupScreen

@Composable
fun AppContent(viewModel: AppViewModel) {
    val state by viewModel.state.collectAsState()

    if (state.navigation.pendingPush != null) {
        viewModel.send(AppAction.Navigation(NavigationAction.DidNavigate))
    }

    if (state.navigation.pendingPop) {
        viewModel.send(AppAction.Navigation(NavigationAction.DidPop))
    }

    val currentRoute = state.navigation.stack.lastOrNull()
    if (currentRoute != null) {
        renderRoute(currentRoute, state, viewModel)
    } else if (state.auth.token == null) {
        AuthScreen(
            state = state.auth,
            currentLanguage = state.language,
            onLogin = { email, password ->
                viewModel.send(AppAction.Auth(AuthAction.Login(email, password)))
            },
            onSignupClick = {
                viewModel.send(AppAction.Navigation(NavigationAction.Push(AppRoute.Signup)))
            },
            onLanguageChange = { language ->
                viewModel.send(AppAction.SetLanguage(language))
            }
        )
    } else {
        // Main app content — will be implemented later
    }
}

@Composable
private fun renderRoute(
    route: AppRoute?,
    state: AppState,
    viewModel: AppViewModel
) {
    when (route) {
        AppRoute.Signup -> {
            SignupScreen(
                state = state.auth,
                onSignup = { email, password, name ->
                    viewModel.send(AppAction.Auth(AuthAction.Signup(email, password, name)))
                },
                onBackClick = {
                    viewModel.send(AppAction.Navigation(NavigationAction.Pop))
                },
                onLoginClick = {
                    viewModel.send(AppAction.Navigation(NavigationAction.Pop))
                }
            )
        }
        AppRoute.Onboarding -> {
            OnboardingScreen(
                state = state.onboarding,
                onAction = { action ->
                    viewModel.send(AppAction.Onboarding(action))
                }
            )
        }
        AppRoute.PlayerDetail,
        AppRoute.MatchRegistration,
        AppRoute.MatchCreation -> {
            // Route rendering will be implemented for other screens
        }
        null -> {}
    }
}
