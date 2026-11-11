package com.iturrizaga.fairplay.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.iturrizaga.fairplay.R
import com.iturrizaga.fairplay.core.localization.Language
import com.iturrizaga.fairplay.core.models.Player
import com.iturrizaga.fairplay.features.auth.AuthUser

@Composable
fun ProfileScreen(
    user: AuthUser?,
    player: Player?,
    language: Language,
    onLanguageChange: (Language) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(user?.name.orEmpty(), style = MaterialTheme.typography.headlineSmall)
        Text(user?.email.orEmpty(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (user?.isAdmin == true) {
            Text(stringResource(R.string.profile_admin_badge), color = MaterialTheme.colorScheme.primary)
        }
        HorizontalDivider()
        if (player != null) PlayerStats(player)
        else Text(stringResource(R.string.profile_no_stats), style = MaterialTheme.typography.bodyMedium)
        HorizontalDivider()
        Text(stringResource(R.string.language_title), style = MaterialTheme.typography.titleSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Language.entries.forEach { option ->
                FilterChip(
                    selected = option == language,
                    onClick = { onLanguageChange(option) },
                    label = { Text(option.displayName) }
                )
            }
        }
        OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
            Text(stringResource(R.string.profile_logout))
        }
    }
}
