package com.iturrizaga.fairplay.features.navigation

sealed class NavigationAction {
    data class Push(val route: AppRoute) : NavigationAction()
    data object Pop : NavigationAction()
    data object ClearStack : NavigationAction()
    data object DidNavigate : NavigationAction()
    data object DidPop : NavigationAction()
}
