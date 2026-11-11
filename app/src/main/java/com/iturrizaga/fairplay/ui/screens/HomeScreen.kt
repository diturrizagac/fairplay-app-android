package com.iturrizaga.fairplay.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.iturrizaga.fairplay.R

enum class HomeTab(val labelRes: Int, val icon: ImageVector) {
    MATCHES(R.string.tab_matches, Icons.Filled.SportsSoccer),
    PLAYERS(R.string.tab_players, Icons.Filled.Groups),
    PROFILE(R.string.tab_profile, Icons.Filled.Person)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    isAdmin: Boolean,
    onCreateMatch: () -> Unit,
    onRefreshMatches: () -> Unit,
    onRefreshPlayers: () -> Unit,
    matches: @Composable (Modifier) -> Unit,
    players: @Composable (Modifier) -> Unit,
    profile: @Composable (Modifier) -> Unit
) {
    var selected by rememberSaveable { mutableStateOf(HomeTab.MATCHES) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(selected.labelRes)) },
                actions = {
                    when (selected) {
                        HomeTab.MATCHES -> {
                            IconButton(onClick = onRefreshMatches) {
                                Icon(Icons.Filled.Refresh, contentDescription = stringResource(R.string.common_refresh))
                            }
                            if (isAdmin) {
                                IconButton(onClick = onCreateMatch) {
                                    Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.match_new))
                                }
                            }
                        }
                        HomeTab.PLAYERS -> IconButton(onClick = onRefreshPlayers) {
                            Icon(Icons.Filled.Refresh, contentDescription = stringResource(R.string.common_refresh))
                        }
                        HomeTab.PROFILE -> Unit
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                HomeTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = tab == selected,
                        onClick = { selected = tab },
                        icon = { Icon(tab.icon, contentDescription = null) },
                        label = { Text(stringResource(tab.labelRes)) }
                    )
                }
            }
        }
    ) { padding ->
        val modifier = Modifier.padding(padding)
        when (selected) {
            HomeTab.MATCHES -> matches(modifier)
            HomeTab.PLAYERS -> players(modifier)
            HomeTab.PROFILE -> profile(modifier)
        }
    }
}
