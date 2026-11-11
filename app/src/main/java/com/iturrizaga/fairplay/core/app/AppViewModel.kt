package com.iturrizaga.fairplay.core.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.iturrizaga.fairplay.core.localization.Language
import com.iturrizaga.fairplay.core.network.APIClient
import com.iturrizaga.fairplay.core.network.AppEnvironment
import com.iturrizaga.fairplay.core.network.HttpAPIClient
import com.iturrizaga.fairplay.core.session.KeystoreSessionStore
import com.iturrizaga.fairplay.core.session.SessionStore
import com.iturrizaga.fairplay.core.store.Store
import com.iturrizaga.fairplay.core.store.scopedEffect
import com.iturrizaga.fairplay.features.auth.AppSession
import com.iturrizaga.fairplay.features.auth.AuthAction
import com.iturrizaga.fairplay.features.auth.AuthState
import com.iturrizaga.fairplay.features.auth.authEffect
import com.iturrizaga.fairplay.features.matchcreation.matchCreationEffect
import com.iturrizaga.fairplay.features.matchregistration.matchRegistrationEffect
import com.iturrizaga.fairplay.features.matches.matchListEffect
import com.iturrizaga.fairplay.features.onboarding.onboardingEffect
import com.iturrizaga.fairplay.features.players.playerListEffect
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

private const val PREF_LANGUAGE = "app_language"

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("fairplay", android.content.Context.MODE_PRIVATE)

    private fun savedLanguage(): Language {
        val code = prefs.getString(PREF_LANGUAGE, null) ?: return Language.system
        return Language.fromCode(code)
    }

    private val client: APIClient by lazy {
        HttpAPIClient(AppEnvironment.current) { store.state.value.language }
    }

    private val sessionStore: SessionStore = KeystoreSessionStore(application)

    private val store = Store(
        initialState = AppState(
            auth = AuthState(session = AppSession.RESTORING),
            language = savedLanguage()
        ),
        reducer = ::appReducer,
        effects = listOf(
            scopedEffect(
                stateSelector  = { it.auth },
                actionExtractor = { (it as? AppAction.Auth)?.action },
                actionEmbedder  = { AppAction.Auth(it) },
                effect          = { state, action -> authEffect(state, action, client, sessionStore) }
            ),
            scopedEffect(
                stateSelector  = { it.onboarding },
                actionExtractor = { (it as? AppAction.Onboarding)?.action },
                actionEmbedder  = { AppAction.Onboarding(it) },
                effect          = { state, action -> onboardingEffect(state, action, client) }
            ),
            scopedEffect(
                stateSelector  = { it.playerList },
                actionExtractor = { (it as? AppAction.PlayerList)?.action },
                actionEmbedder  = { AppAction.PlayerList(it) },
                effect          = { state, action -> playerListEffect(state, action, client) }
            ),
            scopedEffect(
                stateSelector  = { it.matchList },
                actionExtractor = { (it as? AppAction.MatchList)?.action },
                actionEmbedder  = { AppAction.MatchList(it) },
                effect          = { state, action -> matchListEffect(state, action, client) }
            ),
            scopedEffect(
                stateSelector  = { it.matchRegistration },
                actionExtractor = { (it as? AppAction.MatchRegistration)?.action },
                actionEmbedder  = { AppAction.MatchRegistration(it) },
                effect          = { state, action -> matchRegistrationEffect(state, action, client) }
            ),
            scopedEffect(
                stateSelector  = { it.matchCreation },
                actionExtractor = { (it as? AppAction.MatchCreation)?.action },
                actionEmbedder  = { AppAction.MatchCreation(it) },
                effect          = { state, action -> matchCreationEffect(state, action, client) }
            ),
            makeMediator()
        )
    )

    val state: StateFlow<AppState> = store.state

    init {
        viewModelScope.launch {
            client.unauthorizedEvents.collect { send(AppAction.Auth(AuthAction.SessionExpired)) }
        }
    }

    fun send(action: AppAction) {
        if (action is AppAction.SetLanguage) {
            prefs.edit().putString(PREF_LANGUAGE, action.language.code).apply()
        }
        store.send(action)
    }

    override fun onCleared() {
        super.onCleared()
        store.dispose()
    }
}
