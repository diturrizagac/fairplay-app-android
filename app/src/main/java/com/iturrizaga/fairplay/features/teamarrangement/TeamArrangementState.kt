package com.iturrizaga.fairplay.features.teamarrangement

import com.iturrizaga.fairplay.core.models.Match

data class TeamArrangementState(
    val match: Match? = null,
    val isBalancing: Boolean = false,
    val error: String? = null
)
