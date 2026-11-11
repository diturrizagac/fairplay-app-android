package com.iturrizaga.fairplay.features.navigation

fun navigationReducer(state: NavigationState, action: NavigationAction): NavigationState = when (action) {
    is NavigationAction.Push -> {
        val newStack = state.stack + action.route
        state.copy(stack = newStack, pendingPush = action.route, pendingPop = false)
    }
    is NavigationAction.Pop -> {
        val newStack = if (state.stack.isNotEmpty()) state.stack.dropLast(1) else emptyList()
        state.copy(stack = newStack, pendingPop = true, pendingPush = null)
    }
    is NavigationAction.ClearStack -> state.copy(stack = emptyList(), pendingPop = false, pendingPush = null)
    is NavigationAction.DidNavigate -> state.copy(pendingPush = null)
    is NavigationAction.DidPop     -> state.copy(pendingPop = false)
}
