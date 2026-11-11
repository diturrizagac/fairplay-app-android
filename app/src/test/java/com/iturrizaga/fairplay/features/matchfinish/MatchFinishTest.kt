package com.iturrizaga.fairplay.features.matchfinish

import com.iturrizaga.fairplay.core.models.MatchStatus
import com.iturrizaga.fairplay.support.FakeAPIClient
import com.iturrizaga.fairplay.support.makeMatch
import com.iturrizaga.fairplay.support.makeTeam
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MatchFinishTest {
    private val match = makeMatch(
        status = MatchStatus.READY,
        teams = listOf(makeTeam("a", listOf("p1", "p2")), makeTeam("b", listOf("p3")))
    )

    private fun loaded() = matchFinishReducer(MatchFinishState(), MatchFinishAction.Load(match))

    @Test fun load_seedsZeroScoresAndGoals() {
        val state = loaded()
        assertEquals(mapOf("a" to 0, "b" to 0), state.scores)
        assertEquals(mapOf("a" to mapOf("p1" to 0, "p2" to 0), "b" to mapOf("p3" to 0)), state.goals)
        assertTrue(state.canSubmit)
    }

    @Test fun negativeValuesAreClampedToZero() {
        var state = matchFinishReducer(loaded(), MatchFinishAction.SetScore("a", -2))
        state = matchFinishReducer(state, MatchFinishAction.SetGoals("a", "p1", -1))
        assertEquals(0, state.score("a"))
        assertEquals(0, state.goals("a", "p1"))
    }

    @Test fun playerGoalsMayNotExceedTeamScore() {
        var state = matchFinishReducer(loaded(), MatchFinishAction.SetScore("a", 1))
        state = matchFinishReducer(state, MatchFinishAction.SetGoals("a", "p1", 1))
        assertTrue(state.goalsWithinScores)
        state = matchFinishReducer(state, MatchFinishAction.SetGoals("a", "p2", 1))
        assertFalse(state.goalsWithinScores)
        assertFalse(state.canSubmit)
    }

    @Test fun submitEffect_sendsScoresAndGoalsPerTeam() = runBlocking<Unit> {
        var state = matchFinishReducer(loaded(), MatchFinishAction.SetScore("a", 2))
        state = matchFinishReducer(state, MatchFinishAction.SetGoals("a", "p1", 2))
        state = matchFinishReducer(state, MatchFinishAction.SetScore("b", 1))
        val client = FakeAPIClient()
        val actions = matchFinishEffect(state, MatchFinishAction.Submit, client)
        val sent = client.finishedWith!!
        assertEquals(listOf("a" to 2, "b" to 1), sent.map { it.teamId to it.score })
        assertEquals(2, sent.first().playerGoals["p1"])
        assertTrue(actions.single() is MatchFinishAction.SubmitSuccess)
    }
}
