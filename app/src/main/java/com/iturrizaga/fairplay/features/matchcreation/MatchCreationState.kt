package com.iturrizaga.fairplay.features.matchcreation

data class MatchCreationState(
    val name: String = "",
    val date: Long = System.currentTimeMillis(),
    val location: String = "",
    val maxPlayers: Int = 10,
    val isSubmitting: Boolean = false,
    val error: String? = null
)
