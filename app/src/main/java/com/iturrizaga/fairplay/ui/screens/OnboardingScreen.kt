package com.iturrizaga.fairplay.ui.screens

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.iturrizaga.fairplay.R
import com.iturrizaga.fairplay.core.models.PlayerPosition
import com.iturrizaga.fairplay.core.models.displayName
import com.iturrizaga.fairplay.features.onboarding.OnboardingAction
import com.iturrizaga.fairplay.features.onboarding.OnboardingState
import com.iturrizaga.fairplay.ui.theme.FairplayTheme
import java.text.SimpleDateFormat
import java.util.*

private const val PAGE_COUNT = 3

@Composable
fun OnboardingScreen(
    state: OnboardingState,
    onAction: (OnboardingAction) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentPage by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        AnimatedContent(
            targetState = currentPage,
            modifier = Modifier.weight(1f),
            label = "onboarding_page"
        ) { page ->
            when (page) {
                0 -> PositionPage(
                    title = stringResource(R.string.onboarding_position_title),
                    subtitle = stringResource(R.string.onboarding_position_subtitle),
                    selected = state.position,
                    allowNone = false,
                    onSelect = { onAction(OnboardingAction.SetPosition(it)) },
                    onNone = {}
                )
                1 -> PositionPage(
                    title = stringResource(R.string.onboarding_secondary_position_title),
                    subtitle = stringResource(R.string.onboarding_secondary_position_subtitle),
                    selected = state.secondaryPosition,
                    allowNone = true,
                    onSelect = { onAction(OnboardingAction.SetSecondaryPosition(it)) },
                    onNone = { onAction(OnboardingAction.SetSecondaryPosition(null)) }
                )
                else -> BirthDatePage(
                    birthDate = state.birthDate,
                    onDateSelected = { onAction(OnboardingAction.SetBirthDate(it)) }
                )
            }
        }

        BottomBar(
            currentPage = currentPage,
            pageCount = PAGE_COUNT,
            canAdvance = currentPage != 0 || state.position != null,
            isLoading = state.isLoading,
            error = state.error,
            onBack = { currentPage-- },
            onNext = { currentPage++ },
            onSubmit = { onAction(OnboardingAction.Submit) }
        )
    }
}

@Composable
private fun PositionPage(
    title: String,
    subtitle: String,
    selected: PlayerPosition?,
    allowNone: Boolean,
    onSelect: (PlayerPosition) -> Unit,
    onNone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(56.dp))

        Icon(
            imageVector = Icons.Filled.SportsSoccer,
            contentDescription = null,
            modifier = Modifier.size(52.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        val positions = PlayerPosition.entries
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            positions.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { position ->
                        PositionCard(
                            position = position,
                            isSelected = selected == position,
                            onTap = { onSelect(position) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        if (allowNone) {
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = onNone) {
                Text(
                    text = stringResource(R.string.onboarding_none),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (selected == null) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun PositionCard(
    position: PlayerPosition,
    isSelected: Boolean,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (isSelected) MaterialTheme.colorScheme.primary
                         else MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary
                       else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        modifier = modifier
            .height(88.dp)
            .clickable { onTap() }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = position.displayName(),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                ),
                color = contentColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun BirthDatePage(
    birthDate: Long,
    onDateSelected: (Long) -> Unit
) {
    val context = LocalContext.current
    val cal = Calendar.getInstance().apply { timeInMillis = birthDate }
    val formatted = remember(birthDate) {
        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(birthDate))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Cake,
            contentDescription = null,
            modifier = Modifier.size(52.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.onboarding_birth_date_title),
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.onboarding_birth_date_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(40.dp))

        OutlinedButton(
            onClick = {
                DatePickerDialog(
                    context,
                    { _: DatePicker, year, month, day ->
                        val selected = Calendar.getInstance().apply {
                            set(year, month, day, 0, 0, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        onDateSelected(selected.timeInMillis)
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
                ).apply {
                    val maxCal = Calendar.getInstance().apply { add(Calendar.YEAR, -13) }
                    datePicker.maxDate = maxCal.timeInMillis
                }.show()
            }
        ) {
            Text(text = formatted, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun BottomBar(
    currentPage: Int,
    pageCount: Int,
    canAdvance: Boolean,
    isLoading: Boolean,
    error: String?,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onSubmit: () -> Unit
) {
    Surface(tonalElevation = 4.dp) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Page indicators
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(pageCount) { index ->
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (index == currentPage) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                        modifier = Modifier
                            .height(8.dp)
                            .width(if (index == currentPage) 24.dp else 8.dp)
                    ) {}
                }
            }

            if (error != null) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (currentPage > 0) {
                    OutlinedButton(
                        onClick = onBack,
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Text(stringResource(R.string.onboarding_back))
                    }
                }

                if (currentPage < pageCount - 1) {
                    Button(
                        onClick = onNext,
                        enabled = canAdvance,
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Text(stringResource(R.string.onboarding_next))
                    }
                } else {
                    Button(
                        onClick = onSubmit,
                        enabled = !isLoading,
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text(stringResource(R.string.onboarding_finish))
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun OnboardingScreenPreview() {
    FairplayTheme {
        OnboardingScreen(
            state = OnboardingState(),
            onAction = {}
        )
    }
}
