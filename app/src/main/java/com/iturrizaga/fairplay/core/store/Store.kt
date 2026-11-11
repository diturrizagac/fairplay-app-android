package com.iturrizaga.fairplay.core.store

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

typealias Effect<State, Action> = suspend (State, Action) -> List<Action>

class Store<State, Action>(
    initialState: State,
    private val reducer: (State, Action) -> State,
    private val effects: List<Effect<State, Action>> = emptyList()
) {
    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<State> = _state.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    fun send(action: Action) {
        val newState = reducer(_state.value, action)
        _state.value = newState
        effects.forEach { effect ->
            scope.launch {
                effect(newState, action).forEach { send(it) }
            }
        }
    }

    fun dispose() = scope.cancel()
}

fun <AppState, LocalState, LocalAction, AppAction> scopedEffect(
    stateSelector: (AppState) -> LocalState,
    actionExtractor: (AppAction) -> LocalAction?,
    actionEmbedder: (LocalAction) -> AppAction,
    effect: Effect<LocalState, LocalAction>
): Effect<AppState, AppAction> = { state, action ->
    val localAction = actionExtractor(action)
    if (localAction == null) emptyList() else effect(stateSelector(state), localAction).map(actionEmbedder)
}
