package com.iturrizaga.fairplay.features.onboarding

fun onboardingReducer(state: OnboardingState, action: OnboardingAction): OnboardingState = when (action) {
    is OnboardingAction.Reset -> OnboardingState()
    is OnboardingAction.Setup -> state.copy(
        pendingUser = action.user,
        pendingToken = action.token,
        position = null,
        secondaryPosition = null
    )
    is OnboardingAction.SetPosition -> state.copy(position = action.position)
    is OnboardingAction.SetSecondaryPosition -> state.copy(secondaryPosition = action.position)
    is OnboardingAction.SetBirthDate -> state.copy(birthDate = action.birthDate)
    is OnboardingAction.Submit -> state.copy(isLoading = true, error = null)
    is OnboardingAction.SubmitSuccess -> state.copy(isLoading = false)
    is OnboardingAction.SubmitFailure -> state.copy(isLoading = false, error = action.error)
}
