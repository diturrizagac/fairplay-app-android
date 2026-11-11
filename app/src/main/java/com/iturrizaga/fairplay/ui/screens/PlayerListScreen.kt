package com.iturrizaga.fairplay.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.iturrizaga.fairplay.R
import com.iturrizaga.fairplay.core.models.displayName
import com.iturrizaga.fairplay.features.players.PlayerListAction
import com.iturrizaga.fairplay.features.players.PlayerListState
import com.iturrizaga.fairplay.ui.components.LoadingState
import com.iturrizaga.fairplay.ui.components.MessageState
import com.iturrizaga.fairplay.ui.components.formatPerformance

@Composable
fun PlayerListScreen(
    state: PlayerListState,
    onAction: (PlayerListAction) -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) { onAction(PlayerListAction.OnTask) }

    when {
        state.isLoading && state.players.isEmpty() -> LoadingState(modifier)
        state.error != null && state.players.isEmpty() -> MessageState(
            title = state.error,
            modifier = modifier,
            actionLabel = stringResource(R.string.player_retry),
            onAction = { onAction(PlayerListAction.Refresh) }
        )
        state.players.isEmpty() -> MessageState(title = stringResource(R.string.player_empty), modifier = modifier)
        // Players come sorted by performance, so the index is the ranking.
        else -> LazyColumn(modifier.fillMaxSize()) {
            itemsIndexed(state.players, key = { _, player -> player.id }) { index, player ->
                ListItem(
                    modifier = Modifier.clickable { onAction(PlayerListAction.ShowDetail(player)) },
                    leadingContent = { Text("${index + 1}", style = MaterialTheme.typography.titleMedium) },
                    headlineContent = { Text(player.name) },
                    supportingContent = { Text(player.position.displayName()) },
                    trailingContent = { Text(formatPerformance(player.performance), style = MaterialTheme.typography.titleMedium) }
                )
                HorizontalDivider()
            }
        }
    }
}
