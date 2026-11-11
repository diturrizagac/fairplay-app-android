package com.iturrizaga.fairplay.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.iturrizaga.fairplay.R
import com.iturrizaga.fairplay.core.models.Team
import com.iturrizaga.fairplay.features.matchfinish.MatchFinishAction
import com.iturrizaga.fairplay.features.matchfinish.MatchFinishState
import com.iturrizaga.fairplay.ui.components.DetailScaffold
import com.iturrizaga.fairplay.ui.components.ErrorText

@Composable
fun MatchFinishScreen(state: MatchFinishState, onAction: (MatchFinishAction) -> Unit) {
    val match = state.match ?: return
    DetailScaffold(title = stringResource(R.string.match_finish_title), onBack = { onAction(MatchFinishAction.Cancel) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(match.name, style = MaterialTheme.typography.titleLarge)
            match.teams.forEach { team -> TeamResultCard(team, state, onAction) }
            if (!state.goalsWithinScores) ErrorText(stringResource(R.string.match_finish_goals_exceed))
            ErrorText(state.error)
            Button(
                onClick = { onAction(MatchFinishAction.Submit) },
                enabled = state.canSubmit,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.isSubmitting) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                else Text(stringResource(R.string.match_finish_submit))
            }
        }
    }
}

@Composable
private fun TeamResultCard(team: Team, state: MatchFinishState, onAction: (MatchFinishAction) -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(team.name, style = MaterialTheme.typography.titleMedium)
            Stepper(
                label = stringResource(R.string.match_finish_score),
                value = state.score(team.id),
                emphasized = true,
                onChange = { onAction(MatchFinishAction.SetScore(team.id, it)) }
            )
            Text(stringResource(R.string.match_finish_goals_hint), style = MaterialTheme.typography.labelMedium)
            team.members.forEach { member ->
                Stepper(
                    label = member.playerName,
                    value = state.goals(team.id, member.playerId),
                    onChange = { onAction(MatchFinishAction.SetGoals(team.id, member.playerId, it)) }
                )
            }
        }
    }
}

@Composable
private fun Stepper(label: String, value: Int, onChange: (Int) -> Unit, emphasized: Boolean = false) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            label,
            modifier = Modifier.weight(1f),
            style = if (emphasized) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium
        )
        IconButton(onClick = { onChange(value - 1) }, enabled = value > 0) {
            Icon(Icons.Filled.Remove, contentDescription = null)
        }
        Text(value.toString(), modifier = Modifier.width(32.dp), textAlign = TextAlign.Center)
        IconButton(onClick = { onChange(value + 1) }) {
            Icon(Icons.Filled.Add, contentDescription = null)
        }
    }
}
