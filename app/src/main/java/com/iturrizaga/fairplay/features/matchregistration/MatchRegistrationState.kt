package com.iturrizaga.fairplay.features.matchregistration

import com.iturrizaga.fairplay.core.models.Match

data class MatchRegistrationState(
    val match: Match? = null,
    val isConfirming: Boolean = false,
    val error: String? = null
)
