package com.iturrizaga.fairplay.features.matchcreation

import com.iturrizaga.fairplay.core.models.MatchType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MatchCreationReducerTest {
    @Test fun numTeams_onlyAcceptsTwoOrFour() {
        var state = matchCreationReducer(MatchCreationState(), MatchCreationAction.SetNumTeams(3))
        assertEquals(2, state.numTeams)
        state = matchCreationReducer(state, MatchCreationAction.SetNumTeams(4))
        assertEquals(4, state.numTeams)
    }

    @Test fun maxPlayers_followsTypeAndTeams() {
        var state = matchCreationReducer(MatchCreationState(), MatchCreationAction.SetMatchType(MatchType.FUTBOL_11))
        state = matchCreationReducer(state, MatchCreationAction.SetNumTeams(4))
        assertEquals(44, state.maxPlayers)
    }

    @Test fun canSubmit_requiresNameAndLocation() {
        var state = MatchCreationState()
        assertFalse(state.canSubmit)
        state = matchCreationReducer(state, MatchCreationAction.SetName("Jueves"))
        state = matchCreationReducer(state, MatchCreationAction.SetLocation("Cancha 1"))
        assertTrue(state.canSubmit)
    }

    @Test fun defaultDateIsInTheFuture() {
        assertTrue(MatchCreationState().date > System.currentTimeMillis())
    }
}
