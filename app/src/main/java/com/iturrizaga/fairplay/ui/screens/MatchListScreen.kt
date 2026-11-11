package com.iturrizaga.fairplay.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.iturrizaga.fairplay.R
import com.iturrizaga.fairplay.core.models.Match
import com.iturrizaga.fairplay.core.models.MatchStatus
import com.iturrizaga.fairplay.features.matches.MatchListAction
import com.iturrizaga.fairplay.features.matches.MatchListFilter
import com.iturrizaga.fairplay.features.matches.MatchListState
import com.iturrizaga.fairplay.ui.components.ErrorText
import com.iturrizaga.fairplay.ui.components.LoadingState
import com.iturrizaga.fairplay.ui.components.MessageState
import com.iturrizaga.fairplay.ui.components.StatusChip
import com.iturrizaga.fairplay.ui.components.formatMatchDate

@Composable
fun MatchListScreen(
    state: MatchListState,
    onAction: (MatchListAction) -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) { onAction(MatchListAction.OnTask) }

    Column(modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = state.filter == MatchListFilter.UPCOMING,
                onClick = { onAction(MatchListAction.SetFilter(MatchListFilter.UPCOMING)) },
                label = { Text(stringResource(R.string.match_filter_upcoming)) }
            )
            FilterChip(
                selected = state.filter == MatchListFilter.HISTORY,
                onClick = { onAction(MatchListAction.SetFilter(MatchListFilter.HISTORY)) },
                label = { Text(stringResource(R.string.match_filter_history)) }
            )
        }
        ErrorText(state.error, Modifier.padding(horizontal = 16.dp))

        val matches = state.visibleMatches
        when {
            state.isLoading && state.matches.isEmpty() -> LoadingState()
            state.error != null && state.matches.isEmpty() -> MessageState(
                title = state.error,
                actionLabel = stringResource(R.string.match_retry),
                onAction = { onAction(MatchListAction.Refresh) }
            )
            matches.isEmpty() -> MessageState(
                title = stringResource(
                    if (state.filter == MatchListFilter.UPCOMING) R.string.match_empty_title else R.string.match_history_empty_title
                ),
                subtitle = if (state.filter == MatchListFilter.UPCOMING) stringResource(R.string.match_empty_subtitle) else null
            )
            else -> LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(matches, key = { it.id }) { match ->
                    MatchCard(
                        match = match,
                        isRegistered = match.id in state.registeredMatchIds,
                        isAdmin = state.isAdmin,
                        onAction = onAction
                    )
                }
            }
        }
    }
}

@Composable
private fun MatchCard(
    match: Match,
    isRegistered: Boolean,
    isAdmin: Boolean,
    onAction: (MatchListAction) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAction(MatchListAction.ShowRegistration(match)) }
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(match.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                StatusChip(match.status)
            }
            Text(formatMatchDate(match.date), style = MaterialTheme.typography.bodyMedium)
            Text(match.location, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                "${stringResource(match.matchType.nameRes)} · " +
                    stringResource(R.string.match_players_count, match.registeredCount, match.maxPlayers),
                style = MaterialTheme.typography.bodySmall
            )
            match.teams.takeIf { match.status == MatchStatus.FINISHED }?.let { teams ->
                Text(
                    teams.joinToString("  ·  ") { "${it.name} ${it.score ?: 0}" },
                    style = MaterialTheme.typography.titleSmall
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                PlayerActions(match, isRegistered, onAction)
                if (isAdmin) AdminActions(match, onAction)
            }
        }
    }
}

@Composable
private fun PlayerActions(match: Match, isRegistered: Boolean, onAction: (MatchListAction) -> Unit) {
    if (match.status != MatchStatus.CREATED) return
    when {
        isRegistered -> OutlinedButton(onClick = { onAction(MatchListAction.Unregister(match.id)) }) {
            Text(stringResource(R.string.match_unregister))
        }
        match.isFull -> Text(
            stringResource(R.string.match_full),
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(vertical = 12.dp)
        )
        else -> Button(onClick = { onAction(MatchListAction.ShowRegistration(match)) }) {
            Text(stringResource(R.string.match_register))
        }
    }
}

@Composable
private fun AdminActions(match: Match, onAction: (MatchListAction) -> Unit) {
    when (match.status) {
        MatchStatus.CREATED -> if (match.isFull) {
            TextButton(onClick = { onAction(MatchListAction.ShowArrangement(match)) }) {
                Text(stringResource(R.string.match_arrange_teams))
            }
        }
        MatchStatus.READY -> {
            TextButton(onClick = { onAction(MatchListAction.ShowArrangement(match)) }) {
                Text(stringResource(R.string.match_view_teams))
            }
            TextButton(onClick = { onAction(MatchListAction.ShowFinish(match)) }) {
                Text(stringResource(R.string.match_enter_result))
            }
        }
        MatchStatus.FINISHED -> Unit
    }
}
