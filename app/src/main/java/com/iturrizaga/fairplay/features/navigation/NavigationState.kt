package com.iturrizaga.fairplay.features.navigation

sealed class AppRoute {
    data object Signup : AppRoute()
    data object Onboarding : AppRoute()
    data object PlayerDetail : AppRoute()
    data object MatchRegistration : AppRoute()
    data object MatchCreation : AppRoute()
}

data class NavigationState(
    val stack: List<AppRoute> = emptyList(),
    val pendingPush: AppRoute? = null,
    val pendingPop: Boolean = false
)
