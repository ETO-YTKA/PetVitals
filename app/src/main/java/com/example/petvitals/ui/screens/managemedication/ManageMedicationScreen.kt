@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.example.petvitals.ui.screens.managemedication

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.petvitals.R
import com.example.petvitals.ui.components.CenterAlignedTopBar
import com.example.petvitals.ui.components.CustomMediumButton
import com.example.petvitals.ui.components.CustomSnackbarHost
import com.example.petvitals.ui.components.CustomTextField
import com.example.petvitals.ui.components.DatePickerField
import com.example.petvitals.ui.components.DatePickerModal
import com.example.petvitals.ui.components.Loading
import com.example.petvitals.ui.components.ResetTopBarWhenNotScrollable
import com.example.petvitals.ui.components.SnackbarState
import com.example.petvitals.ui.components.rememberTopBarScrollBehavior
import com.example.petvitals.ui.components.showSnackbar
import com.example.petvitals.ui.navigation.AddEditMedication
import com.example.petvitals.ui.theme.Dimen
import com.example.petvitals.ui.theme.PetVitalsTheme
import com.example.petvitals.ui.utils.ObserveAsEvents
import com.example.petvitals.ui.utils.formatDateToString
import java.util.Date

@Composable
fun ManageMedicationScreen(
    addEditMedication: AddEditMedication,
    onPopBackStack: () -> Unit,
    viewModel: ManageMedicationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val resources = LocalResources.current

    LaunchedEffect(addEditMedication) {
        viewModel.loadInitialData(
            petId = addEditMedication.petId,
            medicationId = addEditMedication.medicationId
        )
    }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is ManageMedicationEvent.OnShowSnackbar -> snackbarHostState.showSnackbar(
                SnackbarState(
                    message = resources.getString(event.messageRes),
                    snackbarType = event.snackbarType
                )
            )
        }
    }

    ManageMedicationScreenContent(
        uiState = uiState,
        isEditing = addEditMedication.medicationId != null,
        snackbarHostState = snackbarHostState,
        onAction = viewModel::onAction,
        onPopBackStack = onPopBackStack
    )
}

@Composable
private fun ManageMedicationScreenContent(
    uiState: ManageMedicationUiState,
    isEditing: Boolean,
    snackbarHostState: SnackbarHostState,
    onAction: (ManageMedicationAction) -> Unit,
    onPopBackStack: () -> Unit
) {
    val scrollBehavior = rememberTopBarScrollBehavior()
    val contentScrollState = rememberScrollState()

    ResetTopBarWhenNotScrollable(
        scrollBehavior = scrollBehavior,
        canScrollBackward = contentScrollState.canScrollBackward,
        canScrollForward = contentScrollState.canScrollForward,
        contentVisible = !uiState.isLoading && uiState.loadErrorMessageRes == null
    )

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { CustomSnackbarHost(hostState = snackbarHostState) },
        topBar = {
            CenterAlignedTopBar(
                scrollBehavior = scrollBehavior,
                title = {
                    Text(
                        stringResource(
                            if (isEditing) R.string.edit_medication else R.string.add_medication
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onPopBackStack) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = Dimen.Screen.horizontalPadding)
                .fillMaxSize()
                .then(
                    if (uiState.isLoading || uiState.loadErrorMessageRes != null) {
                        Modifier
                    } else {
                        Modifier.verticalScroll(contentScrollState)
                    }
                ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when {
                uiState.isLoading -> Loading()
                uiState.loadErrorMessageRes != null -> MedicationLoadError(
                    message = stringResource(uiState.loadErrorMessageRes),
                    onRetry = { onAction(ManageMedicationAction.OnRetryLoad) }
                )
                else -> MedicationForm(
                    uiState = uiState,
                    onAction = onAction,
                    onSave = {
                        onAction(ManageMedicationAction.OnSave(onSuccess = onPopBackStack))
                    }
                )
            }
        }
    }

    if (uiState.showStartDatePicker) {
        DatePickerModal(
            datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = uiState.startDate
            ),
            titleRes = R.string.select_start_date,
            onDateSelected = { onAction(ManageMedicationAction.OnStartDateChange(it)) },
            onDismiss = { onAction(ManageMedicationAction.OnStartDatePickerToggle) }
        )
    }

    if (uiState.showEndDatePicker) {
        DatePickerModal(
            datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = uiState.endDate
            ),
            titleRes = R.string.select_end_date,
            onDateSelected = { onAction(ManageMedicationAction.OnEndDateChange(it)) },
            onDismiss = { onAction(ManageMedicationAction.OnEndDatePickerToggle) }
        )
    }
}

@Composable
private fun MedicationLoadError(
    message: String,
    onRetry: () -> Unit
) {
    Text(
        text = message,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    CustomMediumButton(
        onClick = onRetry,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = stringResource(R.string.retry))
    }
}

@Composable
private fun MedicationForm(
    uiState: ManageMedicationUiState,
    onAction: (ManageMedicationAction) -> Unit,
    onSave: () -> Unit
) {
    CustomTextField(
        value = uiState.name,
        onValueChange = { onAction(ManageMedicationAction.OnNameChange(it)) },
        label = { Text(stringResource(R.string.medication_name)) },
        modifier = Modifier.fillMaxWidth(),
        leadingIcon = {
            Icon(
                painter = painterResource(R.drawable.ic_medication),
                contentDescription = null
            )
        },
        isError = uiState.nameErrorMessageRes != null,
        supportingText = uiState.nameErrorMessageRes?.let { { Text(stringResource(it)) } }
    )

    CustomTextField(
        value = uiState.dosage,
        onValueChange = { onAction(ManageMedicationAction.OnDosageChange(it)) },
        label = { Text(stringResource(R.string.dosage)) },
        modifier = Modifier.fillMaxWidth(),
        leadingIcon = {
            Icon(
                painter = painterResource(R.drawable.ic_pill),
                contentDescription = null
            )
        },
        isError = uiState.dosageErrorMessageRes != null,
        supportingText = uiState.dosageErrorMessageRes?.let { { Text(stringResource(it)) } }
    )

    CustomTextField(
        value = uiState.frequency,
        onValueChange = { onAction(ManageMedicationAction.OnFrequencyChange(it)) },
        label = { Text(stringResource(R.string.frequency)) },
        modifier = Modifier.fillMaxWidth(),
        leadingIcon = {
            Icon(
                painter = painterResource(R.drawable.ic_history),
                contentDescription = null
            )
        },
        isError = uiState.frequencyErrorMessageRes != null,
        supportingText = uiState.frequencyErrorMessageRes?.let { { Text(stringResource(it)) } }
    )

    MedicationScheduleCard(uiState = uiState, onAction = onAction)

    CustomTextField(
        value = uiState.note,
        onValueChange = { onAction(ManageMedicationAction.OnNoteChange(it)) },
        label = { Text(stringResource(R.string.note)) },
        modifier = Modifier.fillMaxWidth(),
        leadingIcon = {
            Icon(
                painter = painterResource(R.drawable.ic_sticky_note),
                contentDescription = null
            )
        },
        isError = uiState.noteErrorMessageRes != null,
        supportingText = uiState.noteErrorMessageRes?.let { { Text(stringResource(it)) } }
    )

    CustomMediumButton(
        onClick = onSave,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = stringResource(R.string.save))
    }
}

@Composable
private fun MedicationScheduleCard(
    uiState: ManageMedicationUiState,
    onAction: (ManageMedicationAction) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.medication_schedule),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource(R.string.medication_schedule_helper),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            MedicationScheduleModeButtonGroup(
                isOngoing = uiState.isRegular,
                onModeChange = {
                    onAction(ManageMedicationAction.OnRegularChange(it))
                }
            )

            AnimatedVisibility(
                visible = uiState.isRegular,
                enter = fadeIn(
                    animationSpec = tween(160, easing = LinearOutSlowInEasing)
                ) + expandVertically(
                    animationSpec = tween(220, easing = LinearOutSlowInEasing),
                    expandFrom = Alignment.Top
                ),
                exit = shrinkVertically(
                    animationSpec = tween(180, easing = LinearOutSlowInEasing),
                    shrinkTowards = Alignment.Top
                ) + fadeOut(
                    animationSpec = tween(120, easing = LinearOutSlowInEasing)
                )
            ) {
                Text(
                    text = stringResource(R.string.medication_ongoing_helper),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(
                visible = !uiState.isRegular,
                enter = fadeIn(
                    animationSpec = tween(160, easing = LinearOutSlowInEasing)
                ) + expandVertically(
                    animationSpec = tween(220, easing = LinearOutSlowInEasing),
                    expandFrom = Alignment.Top
                ),
                exit = shrinkVertically(
                    animationSpec = tween(180, easing = LinearOutSlowInEasing),
                    shrinkTowards = Alignment.Top
                ) + fadeOut(
                    animationSpec = tween(120, easing = LinearOutSlowInEasing)
                )
            ) {
                val scheduleError = uiState.scheduleErrorMessageRes?.let { stringResource(it) }
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    DatePickerField(
                        value = uiState.startDate?.let { formatDateToString(Date(it)) }
                            ?: stringResource(R.string.tap_to_select_date),
                        onClick = {
                            onAction(ManageMedicationAction.OnStartDatePickerToggle)
                        },
                        label = stringResource(R.string.start_date),
                        isError = scheduleError != null
                    )

                    DatePickerField(
                        value = uiState.endDate?.let { formatDateToString(Date(it)) }
                            ?: stringResource(R.string.tap_to_select_date),
                        onClick = {
                            onAction(ManageMedicationAction.OnEndDatePickerToggle)
                        },
                        label = stringResource(R.string.medication_end_date_optional),
                        isError = scheduleError != null,
                        onClear = if (uiState.endDate != null) {
                            { onAction(ManageMedicationAction.OnEndDateChange(null)) }
                        } else {
                            null
                        },
                        clearContentDescription = stringResource(R.string.clear_end_date)
                    )

                    Text(
                        text = scheduleError
                            ?: stringResource(R.string.medication_end_date_helper),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (scheduleError != null) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun MedicationScheduleModeButtonGroup(
    isOngoing: Boolean,
    onModeChange: (Boolean) -> Unit
) {
    val options = listOf(
        true to stringResource(R.string.medication_schedule_ongoing),
        false to stringResource(R.string.medication_schedule_date_range)
    )
    val interactionSources = remember {
        List(options.size) { MutableInteractionSource() }
    }
    val contentPadding = ButtonDefaults.ContentPadding
    val layoutDirection = LocalLayoutDirection.current

    ButtonGroup(
        overflowIndicator = { menuState ->
            ButtonGroupDefaults.OverflowIndicator(menuState = menuState)
        },
        modifier = Modifier
            .fillMaxWidth()
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
    ) {
        options.forEachIndexed { index, (ongoing, label) ->
            val interactionSource = interactionSources[index]
            customItem(
                buttonGroupContent = {
                    ToggleButton(
                        checked = isOngoing == ongoing,
                        onCheckedChange = { checked ->
                            if (checked) onModeChange(ongoing)
                        },
                        shapes = if (index == 0) {
                            ButtonGroupDefaults.connectedLeadingButtonShapes()
                        } else {
                            ButtonGroupDefaults.connectedTrailingButtonShapes()
                        },
                        interactionSource = interactionSource,
                        contentPadding = contentPadding,
                        modifier = Modifier
                            .weight(1f)
                            .animateWidth(
                                interactionSource = interactionSource,
                                compressionLimit = contentPadding.calculateEndPadding(
                                    layoutDirection
                                )
                            )
                            .semantics { role = Role.RadioButton }
                    ) {
                        Text(
                            text = label,
                            maxLines = 1
                        )
                    }
                },
                menuContent = { menuState ->
                    DropdownMenuItem(
                        text = { Text(label) },
                        onClick = {
                            onModeChange(ongoing)
                            menuState.dismiss()
                        }
                    )
                }
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun ManageMedicationAddPreview() {
    PetVitalsTheme {
        ManageMedicationScreenContent(
            uiState = ManageMedicationUiState(),
            isEditing = false,
            snackbarHostState = remember { SnackbarHostState() },
            onAction = {},
            onPopBackStack = {}
        )
    }
}

@PreviewLightDark
@Composable
private fun ManageMedicationEditPreview() {
    PetVitalsTheme {
        ManageMedicationScreenContent(
            uiState = ManageMedicationUiState(
                petId = "pet-id",
                medicationId = "medication-id",
                name = "Antibiotic",
                dosage = "1 tablet",
                frequency = "Twice daily",
                startDate = 1_750_000_000_000L,
                endDate = 1_750_604_800_000L,
                note = "Give with food"
            ),
            isEditing = true,
            snackbarHostState = remember { SnackbarHostState() },
            onAction = {},
            onPopBackStack = {}
        )
    }
}

@PreviewLightDark
@Composable
private fun ManageMedicationErrorPreview() {
    PetVitalsTheme {
        ManageMedicationScreenContent(
            uiState = ManageMedicationUiState(
                medicationId = "medication-id",
                loadErrorMessageRes = R.string.medication_not_found_error
            ),
            isEditing = true,
            snackbarHostState = remember { SnackbarHostState() },
            onAction = {},
            onPopBackStack = {}
        )
    }
}

@PreviewLightDark
@Composable
private fun ManageMedicationOngoingPreview() {
    PetVitalsTheme {
        ManageMedicationScreenContent(
            uiState = ManageMedicationUiState(
                name = "Daily supplement",
                dosage = "1 tablet",
                frequency = "Once daily",
                isRegular = true
            ),
            isEditing = false,
            snackbarHostState = remember { SnackbarHostState() },
            onAction = {},
            onPopBackStack = {}
        )
    }
}

@PreviewLightDark
@Composable
private fun ManageMedicationDateErrorPreview() {
    PetVitalsTheme {
        ManageMedicationScreenContent(
            uiState = ManageMedicationUiState(
                name = "Antibiotic",
                dosage = "1 tablet",
                frequency = "Twice daily",
                scheduleErrorMessageRes =
                    R.string.medication_start_date_must_be_selected_error
            ),
            isEditing = false,
            snackbarHostState = remember { SnackbarHostState() },
            onAction = {},
            onPopBackStack = {}
        )
    }
}
