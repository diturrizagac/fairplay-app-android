package com.iturrizaga.fairplay.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.iturrizaga.fairplay.R
import com.iturrizaga.fairplay.core.models.Player
import com.iturrizaga.fairplay.core.models.displayName
import com.iturrizaga.fairplay.ui.components.DetailScaffold
import com.iturrizaga.fairplay.ui.components.StatItem
import com.iturrizaga.fairplay.ui.components.formatPerformance

@Composable
fun PlayerDetailScreen(player: Player?, onBack: () -> Unit) {
    if (player == null) return
    DetailScaffold(title = player.name, onBack = onBack) { padding ->
        PlayerStats(player, Modifier.padding(padding).padding(16.dp))
    }
}

@Composable
fun PlayerStats(player: Player, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(player.position.displayName(), style = MaterialTheme.typography.titleMedium)
        player.secondaryPosition?.let {
            Text(
                "${stringResource(R.string.player_secondary_position)}: ${it.displayName()}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Text(stringResource(R.string.player_stats_section), style = MaterialTheme.typography.titleSmall)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            StatItem(stringResource(R.string.player_performance), formatPerformance(player.performance))
            StatItem(stringResource(R.string.player_games), player.totalGames.toString())
            StatItem(stringResource(R.string.player_goals), player.goalsScored.toString())
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            StatItem(stringResource(R.string.player_wins), player.wins.toString())
            StatItem(stringResource(R.string.player_losses), player.losses.toString())
            StatItem(stringResource(R.string.player_age), player.age.toString())
        }
    }
}
