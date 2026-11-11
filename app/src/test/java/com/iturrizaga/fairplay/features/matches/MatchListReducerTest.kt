package com.iturrizaga.fairplay.features.matches

import com.iturrizaga.fairplay.core.models.MatchStatus
import com.iturrizaga.fairplay.support.FakeAPIClient
import com.iturrizaga.fairplay.support.makeMatch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MatchListReducerTest {
    @Test fun onTask_setsLoadingOnlyWhenEmpty() {
        assertTrue(matchListReducer(MatchListState(isLoading = false), MatchListAction.OnTask).isLoading)
        val loaded = MatchListState(matches = listOf(makeMatch()), isLoading = false)
        assertFalse(matchListReducer(loaded, MatchListAction.OnTask).isLoading)
    }

    @Test fun fetchSuccess_derivesRegistrationFromRoster() {
        val matches = listOf(
            makeMatch(id = "joined", registeredUserIds = listOf("me", "other")),
            makeMatch(id = "open", registeredUserIds = listOf("other"))
        )
        val state = matchListReducer(MatchListState(currentUserId = "me"), MatchListAction.FetchSuccess(matches))
        assertFalse(state.isLoading)
        assertEquals(setOf("joined"), state.registeredMatchIds)
    }

    @Test fun updateMatch_replacesById_andResyncsRegistration() {
        val start = MatchListState(matches = listOf(makeMatch("1")), registeredMatchIds = setOf("1"), currentUserId = "me")
        val updated = makeMatch("1", status = MatchStatus.READY, registeredUserIds = listOf("other"))
        val state = matchListReducer(start, MatchListAction.UpdateMatch(updated))
        assertEquals(listOf(updated), state.matches)
        assertTrue(state.registeredMatchIds.isEmpty())
    }

    @Test fun updateMatch_appendsUnknownMatch() {
        val state = matchListReducer(MatchListState(matches = listOf(makeMatch("1"))), MatchListAction.UpdateMatch(makeMatch("2")))
        assertEquals(listOf("1", "2"), state.matches.map { it.id })
    }

    @Test fun setCurrentUser_recomputesRegistrationForNewUser() {
        val start = MatchListState(
            matches = listOf(makeMatch("a", registeredUserIds = listOf("alice")), makeMatch("b", registeredUserIds = listOf("bob"))),
            registeredMatchIds = setOf("a"),
            currentUserId = "alice"
        )
        val state = matchListReducer(start, MatchListAction.SetCurrentUser("bob", isAdmin = true))
        assertEquals("bob", state.currentUserId)
        assertTrue(state.isAdmin)
        assertEquals(setOf("b"), state.registeredMatchIds)
    }

    @Test fun unregisterFailure_restoresRegistrationAndShowsError() {
        var state = matchListReducer(MatchListState(registeredMatchIds = setOf("1")), MatchListAction.Unregister("1"))
        assertTrue(state.registeredMatchIds.isEmpty())
        state = matchListReducer(state, MatchListAction.UnregisterFailure("1", "nope"))
        assertEquals(setOf("1"), state.registeredMatchIds)
        assertEquals("nope", state.error)
    }

    @Test fun visibleMatches_filtersAndSorts() {
        val early = makeMatch("early", date = 100)
        val late = makeMatch("late", status = MatchStatus.READY, date = 200)
        val oldFinish = makeMatch("old", status = MatchStatus.FINISHED, finishedAt = 300)
        val newFinish = makeMatch("new", status = MatchStatus.FINISHED, finishedAt = 400)
        val state = MatchListState(matches = listOf(late, newFinish, early, oldFinish))
        assertEquals(listOf("early", "late"), state.visibleMatches.map { it.id })
        assertEquals(listOf("new", "old"), state.copy(filter = MatchListFilter.HISTORY).visibleMatches.map { it.id })
    }

    @Test fun unregisterEffect_refreshesTheMatch() = runBlocking<Unit> {
        val fresh = makeMatch("1", registeredUserIds = emptyList())
        val client = FakeAPIClient().apply { getMatchResult = Result.success(fresh) }
        val actions = matchListEffect(MatchListState(), MatchListAction.Unregister("1"), client)
        assertEquals("1", client.unregisteredMatchId)
        assertEquals(listOf(MatchListAction.UpdateMatch(fresh)), actions)
    }
}
