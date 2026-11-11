package com.iturrizaga.fairplay.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.iturrizaga.fairplay.R
import com.iturrizaga.fairplay.core.app.AppAction
import com.iturrizaga.fairplay.core.app.AppState
import com.iturrizaga.fairplay.core.app.AppViewModel
import com.iturrizaga.fairplay.features.auth.AppSession
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

    LaunchedEffect(Unit) {
        viewModel.send(AppAction.Auth(AuthAction.OnTask))
    }

    if (state.navigation.pendingPush != null) {
        viewModel.send(AppAction.Navigation(NavigationAction.DidNavigate))
    }

    if (state.navigation.pendingPop) {
        viewModel.send(AppAction.Navigation(NavigationAction.DidPop))
    }

    val currentRoute = state.navigation.stack.lastOrNull()
    if (state.auth.session == AppSession.RESTORING) {
        LaunchScreen()
    } else if (currentRoute != null) {
        renderRoute(currentRoute, state, viewModel)
    } else if (state.auth.session == AppSession.UNAUTHENTICATED) {
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
        // Placeholder until the main tabs land; lets the session flow be exercised end to end.
        HomePlaceholder(
            email = state.auth.currentUser?.email.orEmpty(),
            onLogout = { viewModel.send(AppAction.Auth(AuthAction.Logout)) }
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

@Composable
private fun HomePlaceholder(email: String, onLogout: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = email, style = MaterialTheme.typography.titleMedium)
        OutlinedButton(onClick = onLogout) {
            Text(stringResource(R.string.profile_logout))
        }
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
