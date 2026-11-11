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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.iturrizaga.fairplay.R
import com.iturrizaga.fairplay.core.models.MatchStatus
import com.iturrizaga.fairplay.core.models.displayName
import com.iturrizaga.fairplay.features.matchregistration.MatchRegistrationAction
import com.iturrizaga.fairplay.features.matchregistration.MatchRegistrationState
import com.iturrizaga.fairplay.ui.components.DetailScaffold
import com.iturrizaga.fairplay.ui.components.ErrorText
import com.iturrizaga.fairplay.ui.components.StatusChip
import com.iturrizaga.fairplay.ui.components.formatMatchDate
import com.iturrizaga.fairplay.ui.components.formatPerformance

@Composable
fun MatchRegistrationScreen(
    state: MatchRegistrationState,
    isRegistered: Boolean,
    onAction: (MatchRegistrationAction) -> Unit
) {
    val match = state.match ?: return
    DetailScaffold(title = match.name, onBack = { onAction(MatchRegistrationAction.Cancel) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatusChip(match.status)
            Text(formatMatchDate(match.date), style = MaterialTheme.typography.titleMedium)
            Text(match.location, style = MaterialTheme.typography.bodyLarge)
            Text(stringResource(match.matchType.nameRes), style = MaterialTheme.typography.bodyMedium)

            HorizontalDivider()
            Text(
                stringResource(R.string.match_registration_roster, match.registeredCount, match.maxPlayers),
                style = MaterialTheme.typography.titleSmall
            )
            match.registeredPlayers.forEach { player ->
                Row(Modifier.fillMaxWidth()) {
                    Text(player.name, modifier = Modifier.weight(1f))
                    Text(
                        "${player.position.displayName()} · ${formatPerformance(player.performance)}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            HorizontalDivider()

            when {
                isRegistered -> Text(stringResource(R.string.match_registration_already), color = MaterialTheme.colorScheme.primary)
                match.status != MatchStatus.CREATED -> Text(stringResource(R.string.match_registration_closed))
                match.isFull -> Text(stringResource(R.string.match_full), color = MaterialTheme.colorScheme.error)
                else -> {
                    Text(stringResource(R.string.match_registration_subtitle), style = MaterialTheme.typography.bodyMedium)
                    ErrorText(state.error)
                    Button(
                        onClick = { onAction(MatchRegistrationAction.Confirm) },
                        enabled = !state.isConfirming,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (state.isConfirming) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                        else Text(stringResource(R.string.match_registration_confirm))
                    }
                    OutlinedButton(onClick = { onAction(MatchRegistrationAction.Cancel) }, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.match_registration_cancel))
                    }
                }
            }
        }
    }
}
