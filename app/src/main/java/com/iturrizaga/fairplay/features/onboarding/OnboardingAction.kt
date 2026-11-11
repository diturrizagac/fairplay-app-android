package com.iturrizaga.fairplay.features.onboarding

import com.iturrizaga.fairplay.features.auth.AuthUser
import com.iturrizaga.fairplay.core.models.PlayerPosition

sealed class OnboardingAction {
    data object Reset : OnboardingAction()
    data class Setup(val user: AuthUser, val token: String) : OnboardingAction()
    data class SetPosition(val position: PlayerPosition) : OnboardingAction()
    data class SetSecondaryPosition(val position: PlayerPosition?) : OnboardingAction()
    data class SetBirthDate(val birthDate: Long) : OnboardingAction()
    data object Submit : OnboardingAction()
    data class SubmitSuccess(val playerId: String) : OnboardingAction()
    data class SubmitFailure(val error: String) : OnboardingAction()
}
