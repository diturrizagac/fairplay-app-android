package com.iturrizaga.fairplay.core.app

import com.iturrizaga.fairplay.features.auth.AppSession
import com.iturrizaga.fairplay.features.auth.AuthAction
import com.iturrizaga.fairplay.features.matchcreation.MatchCreationAction
import com.iturrizaga.fairplay.features.matches.MatchListAction
import com.iturrizaga.fairplay.features.matches.MatchListState
import com.iturrizaga.fairplay.core.models.MatchStatus
import com.iturrizaga.fairplay.features.matchfinish.MatchFinishAction
import com.iturrizaga.fairplay.features.matchfinish.MatchFinishState
import com.iturrizaga.fairplay.features.matchregistration.MatchRegistrationAction
import com.iturrizaga.fairplay.features.matchregistration.MatchRegistrationState
import com.iturrizaga.fairplay.features.teamarrangement.TeamArrangementAction
import com.iturrizaga.fairplay.features.teamarrangement.TeamArrangementState
import com.iturrizaga.fairplay.features.navigation.AppRoute
import com.iturrizaga.fairplay.features.navigation.NavigationAction
import com.iturrizaga.fairplay.features.onboarding.OnboardingAction
import com.iturrizaga.fairplay.features.players.PlayerDetailState
import com.iturrizaga.fairplay.features.players.PlayerListAction
import com.iturrizaga.fairplay.features.players.PlayerListState
import com.iturrizaga.fairplay.support.makeMatch
import com.iturrizaga.fairplay.support.makePlayer
import com.iturrizaga.fairplay.support.makeTeam
import com.iturrizaga.fairplay.support.makeUser
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Applies the action and every follow-up the mediator emits, like the Store does. */
private suspend fun dispatch(action: AppAction, state: AppState): AppState {
    var next = appReducer(state, action)
    for (followUp in makeMediator()(next, action)) {
        next = dispatch(followUp, next)
    }
    return next
}

class AppMediatorTest {
    @Test fun loginWithProfile_clearsNavigation() = runBlocking<Unit> {
        val start = AppState(navigation = appReducer(AppState(), AppAction.Navigation(NavigationAction.Push(AppRoute.Signup))).navigation)
        val state = dispatch(AppAction.Auth(AuthAction.LoginSuccess(makeUser(), "t")), start)
        assertEquals(AppSession.AUTHENTICATED, state.auth.session)
        assertTrue(state.navigation.stack.isEmpty())
    }

    @Test fun loginWithoutProfile_startsOnboarding() = runBlocking<Unit> {
        val user = makeUser(playerId = null)
        val state = dispatch(AppAction.Auth(AuthAction.LoginSuccess(user, "t")), AppState())
        assertEquals(listOf<AppRoute>(AppRoute.Onboarding), state.navigation.stack)
        assertEquals(user, state.onboarding.pendingUser)
        assertEquals("t", state.onboarding.pendingToken)
    }

    @Test fun loginSuccess_mirrorsUserIntoMatchList() = runBlocking<Unit> {
        val state = dispatch(AppAction.Auth(AuthAction.LoginSuccess(makeUser(id = "u9", isAdmin = true), "t")), AppState())
        assertEquals("u9", state.matchList.currentUserId)
        assertTrue(state.matchList.isAdmin)
    }

    @Test fun onboardingSuccess_completesLoginWithPlayerId() = runBlocking<Unit> {
        var state = dispatch(AppAction.Auth(AuthAction.LoginSuccess(makeUser(playerId = null), "t")), AppState())
        state = dispatch(AppAction.Onboarding(OnboardingAction.SubmitSuccess("new-player")), state)
        assertEquals("new-player", state.auth.currentUser?.playerId)
        assertTrue(state.navigation.stack.isEmpty())
    }

    @Test fun registrationConfirmed_updatesListAndPops() = runBlocking<Unit> {
        val match = makeMatch("m1")
        var state = dispatch(AppAction.MatchList(MatchListAction.ShowRegistration(match)), AppState(matchList = MatchListState(matches = listOf(match))))
        assertEquals(listOf<AppRoute>(AppRoute.MatchRegistration), state.navigation.stack)
        val joined = makeMatch("m1", registeredUserIds = listOf("u1"))
        state = dispatch(AppAction.MatchRegistration(MatchRegistrationAction.ConfirmSuccess(joined)), state)
        assertEquals(listOf(joined), state.matchList.matches)
        assertEquals(setOf("m1"), state.matchList.registeredMatchIds)
        assertTrue(state.navigation.stack.isEmpty())
    }

    @Test fun balanceSuccess_updatesListAndStaysOnScreen() = runBlocking<Unit> {
        val match = makeMatch("m1")
        var state = dispatch(AppAction.MatchList(MatchListAction.ShowArrangement(match)), AppState(matchList = MatchListState(matches = listOf(match))))
        val ready = makeMatch("m1", status = MatchStatus.READY, teams = listOf(makeTeam("a", listOf("p1"))))
        state = dispatch(AppAction.TeamArrangement(TeamArrangementAction.BalanceSuccess(ready)), state)
        assertEquals(listOf(ready), state.matchList.matches)
        assertEquals(ready, state.teamArrangement.match)
        assertEquals(listOf<AppRoute>(AppRoute.TeamArrangement), state.navigation.stack)
        state = dispatch(AppAction.TeamArrangement(TeamArrangementAction.Close), state)
        assertTrue(state.navigation.stack.isEmpty())
    }

    @Test fun finishSuccess_updatesListReloadsPlayersAndPops() = runBlocking<Unit> {
        val ready = makeMatch("m1", status = MatchStatus.READY, teams = listOf(makeTeam("a", listOf("p1"))))
        var state = dispatch(AppAction.MatchList(MatchListAction.ShowFinish(ready)), AppState(matchList = MatchListState(matches = listOf(ready))))
        assertEquals(ready, state.matchFinish.match)
        val finished = makeMatch("m1", status = MatchStatus.FINISHED)
        state = dispatch(AppAction.MatchFinish(MatchFinishAction.SubmitSuccess(finished)), state)
        assertEquals(listOf(finished), state.matchList.matches)
        assertTrue(state.playerList.isLoading)
        assertTrue(state.navigation.stack.isEmpty())
    }

    @Test fun logout_resetsEveryFeature() = runBlocking<Unit> {
        val state = dispatch(AppAction.Auth(AuthAction.Logout), loggedInStateWithData())
        assertInitialFeatureState(state)
        assertEquals(AppSession.UNAUTHENTICATED, state.auth.session)
        assertFalse(state.auth.isSessionExpired)
    }

    @Test fun sessionExpired_resetsEveryFeatureAndFlagsMessage() = runBlocking<Unit> {
        val state = dispatch(AppAction.Auth(AuthAction.SessionExpired), loggedInStateWithData())
        assertInitialFeatureState(state)
        assertEquals(AppSession.UNAUTHENTICATED, state.auth.session)
        assertTrue(state.auth.isSessionExpired)
    }

    private suspend fun loggedInStateWithData(): AppState {
        var state = dispatch(AppAction.Auth(AuthAction.LoginSuccess(makeUser(), "t")), AppState())
        state = dispatch(AppAction.MatchList(MatchListAction.FetchSuccess(listOf(makeMatch()))), state)
        state = dispatch(AppAction.MatchList(MatchListAction.MarkRegistered("m1")), state)
        state = dispatch(AppAction.PlayerList(PlayerListAction.FetchSuccess(listOf(makePlayer()))), state)
        state = dispatch(AppAction.PlayerList(PlayerListAction.ShowDetail(makePlayer())), state)
        state = dispatch(AppAction.MatchList(MatchListAction.ShowRegistration(makeMatch())), state)
        state = dispatch(AppAction.MatchCreation(MatchCreationAction.SetName("Jueves")), state)
        state = dispatch(AppAction.Onboarding(OnboardingAction.Setup(makeUser(playerId = null), "t")), state)
        state = dispatch(AppAction.MatchList(MatchListAction.ShowFinish(makeMatch(teams = listOf(makeTeam("a", listOf("p1")))))), state)
        state = dispatch(AppAction.TeamArrangement(TeamArrangementAction.Load(makeMatch())), state)
        assertTrue(state.navigation.stack.isNotEmpty())
        assertEquals(setOf("m1"), state.matchList.registeredMatchIds)
        return state
    }

    private fun assertInitialFeatureState(state: AppState) {
        assertTrue(state.navigation.stack.isEmpty())
        assertEquals(PlayerListState(), state.playerList)
        assertEquals(PlayerDetailState(), state.playerDetail)
        assertEquals(MatchListState(), state.matchList)
        assertEquals(MatchRegistrationState(), state.matchRegistration)
        assertEquals(TeamArrangementState(), state.teamArrangement)
        assertEquals(MatchFinishState(), state.matchFinish)
        // Onboarding and creation defaults include "now"-based dates, so compare the rest.
        assertNull(state.onboarding.pendingUser)
        assertNull(state.onboarding.pendingToken)
        assertEquals("", state.matchCreation.name)
    }
}
