package com.iturrizaga.fairplay.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.iturrizaga.fairplay.R
import com.iturrizaga.fairplay.core.models.MatchType
import com.iturrizaga.fairplay.features.matchcreation.MatchCreationAction
import com.iturrizaga.fairplay.features.matchcreation.MatchCreationState
import com.iturrizaga.fairplay.ui.components.DetailScaffold
import com.iturrizaga.fairplay.ui.components.ErrorText
import com.iturrizaga.fairplay.ui.components.formatMatchDate
import java.util.Calendar

@Composable
fun MatchCreationScreen(state: MatchCreationState, onAction: (MatchCreationAction) -> Unit) {
    val context = LocalContext.current

    fun pickDateTime() {
        val calendar = Calendar.getInstance().apply { timeInMillis = state.date }
        DatePickerDialog(context, { _, year, month, day ->
            TimePickerDialog(context, { _, hour, minute ->
                calendar.set(year, month, day, hour, minute, 0)
                onAction(MatchCreationAction.SetDate(calendar.timeInMillis))
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).apply {
            datePicker.minDate = System.currentTimeMillis()
        }.show()
    }

    DetailScaffold(title = stringResource(R.string.match_creation_title), onBack = { onAction(MatchCreationAction.Cancel) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = state.name,
                onValueChange = { onAction(MatchCreationAction.SetName(it)) },
                label = { Text(stringResource(R.string.match_creation_name_section)) },
                placeholder = { Text(stringResource(R.string.match_creation_name_placeholder)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Text(stringResource(R.string.match_creation_datetime_section), style = MaterialTheme.typography.titleSmall)
            OutlinedButton(onClick = { pickDateTime() }, modifier = Modifier.fillMaxWidth()) {
                Text(formatMatchDate(state.date))
            }
            OutlinedTextField(
                value = state.location,
                onValueChange = { onAction(MatchCreationAction.SetLocation(it)) },
                label = { Text(stringResource(R.string.match_creation_location_section)) },
                placeholder = { Text(stringResource(R.string.match_creation_location_placeholder)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Text(stringResource(R.string.match_creation_type_section), style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MatchType.entries.forEach { type ->
                    FilterChip(
                        selected = state.matchType == type,
                        onClick = { onAction(MatchCreationAction.SetMatchType(type)) },
                        label = { Text(stringResource(type.nameRes)) }
                    )
                }
            }
            Text(stringResource(R.string.match_creation_teams_section), style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MatchCreationState.allowedNumTeams.forEach { count ->
                    FilterChip(
                        selected = state.numTeams == count,
                        onClick = { onAction(MatchCreationAction.SetNumTeams(count)) },
                        label = { Text(count.toString()) }
                    )
                }
            }
            Text(stringResource(R.string.match_creation_max_players, state.maxPlayers), style = MaterialTheme.typography.bodyMedium)
            ErrorText(state.error)
            Button(
                onClick = { onAction(MatchCreationAction.Submit) },
                enabled = state.canSubmit,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.isSubmitting) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                else Text(stringResource(R.string.match_creation_create_button))
            }
        }
    }
}
