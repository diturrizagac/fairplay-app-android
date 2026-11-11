package com.iturrizaga.fairplay.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.iturrizaga.fairplay.R
import com.iturrizaga.fairplay.core.models.MatchStatus
import com.iturrizaga.fairplay.core.models.Team
import com.iturrizaga.fairplay.core.models.displayName
import com.iturrizaga.fairplay.features.teamarrangement.TeamArrangementAction
import com.iturrizaga.fairplay.features.teamarrangement.TeamArrangementState
import com.iturrizaga.fairplay.ui.components.DetailScaffold
import com.iturrizaga.fairplay.ui.components.ErrorText

@Composable
fun TeamArrangementScreen(state: TeamArrangementState, onAction: (TeamArrangementAction) -> Unit) {
    val match = state.match ?: return
    DetailScaffold(title = match.name, onBack = { onAction(TeamArrangementAction.Close) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(stringResource(R.string.team_arrangement_title), style = MaterialTheme.typography.titleLarge)
            if (match.teams.isEmpty()) {
                Text(stringResource(R.string.team_arrangement_empty))
                if (!match.isFull) {
                    Text(
                        stringResource(R.string.team_arrangement_needs_players, match.registeredCount, match.maxPlayers),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            match.teams.forEach { TeamCard(it) }
            ErrorText(state.error)
            if (match.status != MatchStatus.FINISHED) {
                Button(
                    onClick = { onAction(TeamArrangementAction.Balance) },
                    enabled = match.isFull && !state.isBalancing,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (state.isBalancing) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                    else Text(stringResource(if (match.teams.isEmpty()) R.string.team_arrangement_balance else R.string.team_arrangement_rebalance))
                }
            }
            OutlinedButton(onClick = { onAction(TeamArrangementAction.Close) }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.team_arrangement_done))
            }
        }
    }
}

@Composable
private fun TeamCard(team: Team) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(team.name, style = MaterialTheme.typography.titleMedium)
            team.members.forEach { member ->
                Row(Modifier.fillMaxWidth()) {
                    Text(member.playerName, modifier = Modifier.weight(1f))
                    Text(member.assignedPosition.displayName(), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
