package com.iturrizaga.fairplay.features.matchfinish

fun matchFinishReducer(state: MatchFinishState, action: MatchFinishAction): MatchFinishState = when (action) {
    is MatchFinishAction.Reset         -> MatchFinishState()
    is MatchFinishAction.Load          -> MatchFinishState(
        match = action.match,
        scores = action.match.teams.associate { it.id to 0 },
        goals = action.match.teams.associate { team -> team.id to team.members.associate { it.playerId to 0 } }
    )
    is MatchFinishAction.SetScore      -> state.copy(scores = state.scores + (action.teamId to action.score.coerceAtLeast(0)))
    is MatchFinishAction.SetGoals      -> {
        val teamGoals = state.goals[action.teamId].orEmpty() + (action.playerId to action.goals.coerceAtLeast(0))
        state.copy(goals = state.goals + (action.teamId to teamGoals))
    }
    is MatchFinishAction.Submit        -> state.copy(isSubmitting = true, error = null)
    is MatchFinishAction.SubmitSuccess -> MatchFinishState()
    is MatchFinishAction.SubmitFailure -> state.copy(isSubmitting = false, error = action.error)
    is MatchFinishAction.Cancel        -> MatchFinishState()
}
