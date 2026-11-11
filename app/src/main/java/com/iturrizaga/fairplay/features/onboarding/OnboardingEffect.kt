package com.iturrizaga.fairplay.features.onboarding

import com.iturrizaga.fairplay.core.network.APIClient

suspend fun onboardingEffect(state: OnboardingState, action: OnboardingAction, client: APIClient): List<OnboardingAction> {
    if (action !is OnboardingAction.Submit) return emptyList()
    val position = state.position ?: return listOf(OnboardingAction.SubmitFailure("Selecciona tu posición."))
    return try {
        val playerId = client.createPlayerProfile(position, state.secondaryPosition, state.birthDate)
        listOf(OnboardingAction.SubmitSuccess(playerId))
    } catch (e: Exception) {
        listOf(OnboardingAction.SubmitFailure(e.message ?: "Error al crear el perfil."))
    }
}
