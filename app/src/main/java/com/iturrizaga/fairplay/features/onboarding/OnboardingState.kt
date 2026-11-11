package com.iturrizaga.fairplay.features.onboarding

import com.iturrizaga.fairplay.features.auth.AuthUser
import com.iturrizaga.fairplay.core.models.PlayerPosition

data class OnboardingState(
    val pendingUser: AuthUser? = null,
    val pendingToken: String? = null,
    val position: PlayerPosition? = null,
    val secondaryPosition: PlayerPosition? = null,
    val birthDate: Long = defaultBirthDate(),
    val isLoading: Boolean = false,
    val error: String? = null
)

private fun defaultBirthDate(): Long {
    val cal = java.util.Calendar.getInstance()
    cal.add(java.util.Calendar.YEAR, -25)
    return cal.timeInMillis
}
