package com.iturrizaga.fairplay.features.matches

import com.iturrizaga.fairplay.core.models.Match

data class MatchListState(
    val matches: List<Match> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val registeredMatchIds: Set<String> = emptySet()
)
