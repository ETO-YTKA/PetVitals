@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.petvitals.ui.screens.add_edit_medication

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.petvitals.AddEditMedication
import com.example.petvitals.R
import com.example.petvitals.ui.components.ButtonWithIcon
import com.example.petvitals.ui.components.CustomIconButton
import com.example.petvitals.ui.components.CustomOutlinedTextField
import com.example.petvitals.ui.components.DatePickerField
import com.example.petvitals.ui.components.DatePickerModal
import com.example.petvitals.ui.components.Loading
import com.example.petvitals.ui.components.ScreenLayout
import com.example.petvitals.ui.components.TopBar
import com.example.petvitals.ui.theme.Dimen
import com.example.petvitals.utils.formatDateToString
import java.util.Date

@Composable
fun AddEditMedicationScreen(
    addEditMedication: AddEditMedication,
    onPopBackStack: () -> Unit,
    viewModel: AddEditMedicationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadInitialData(addEditMedication)
    }

    ScreenLayout(
        verticalArrangement = Arrangement.spacedBy(Dimen.spaceLarge),
        horizontalAlignment = Alignment.Start,
        columnModifier = Modifier
            .padding(vertical = Dimen.spaceMedium)
            .fillMaxSize()
            .then(if (uiState.isLoading) Modifier else Modifier.verticalScroll(rememberScrollState())),
        topBar = {
            val titleText = stringResource(
                when (addEditMedication.medicationId) {
                    null -> R.string.add_medication
                    else -> R.string.edit_medication
                }
            )

            TopBar(
                title = { Text(titleText) },
                navigationIcon = {
                    CustomIconButton(
                        onClick = onPopBackStack,
                        painter = painterResource(R.drawable.ic_arrow_back),
                        contentDescription = stringResource(R.string.back)
                    )
                }
            )
        }
    ) {
        if (uiState.isLoading) { Loading() }

        CustomOutlinedTextField(
            value = uiState.medicationName,
            onValueChange = viewModel::onNameChange,
            label = { Text(stringResource(R.string.medication_name)) },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.ic_medication),
                    contentDescription = null
                )
            },
            isError = uiState.medicationNameErrorMessage != null,
            supportingText = if (uiState.medicationNameErrorMessage != null) {
                { Text(uiState.medicationNameErrorMessage!!) }
            } else null
        )

        CustomOutlinedTextField(
            value = uiState.medicationDosage,
            onValueChange = viewModel::onDosageChange,
            label = { Text(stringResource(R.string.dosage)) },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.ic_pill),
                    contentDescription = null
                )
            },
            isError = uiState.medicationDosageErrorMessage != null,
            supportingText = if (uiState.medicationDosageErrorMessage != null) {
                { Text(uiState.medicationDosageErrorMessage!!) }
            } else null
        )

        CustomOutlinedTextField(
            value = uiState.medicationFrequency,
            onValueChange = viewModel::onFrequencyChange,
            label = { Text(stringResource(R.string.frequency)) },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.ic_event_repeat),
                    contentDescription = null
                )
            },
            isError = uiState.medicationFrequencyErrorMessage != null,
            supportingText = if (uiState.medicationFrequencyErrorMessage != null) {
                { Text(uiState.medicationFrequencyErrorMessage!!) }
            } else null
        )

        MedicationScheduleCard(
            uiState = uiState,
            toggleStartDatePicker = viewModel::toggleStartDatePicker,
            toggleEndDatePicker = viewModel::toggleEndDatePicker,
            toggleRegularMedication = viewModel::toggleRegularMedication
        )

        CustomOutlinedTextField(
            value = uiState.medicationNote,
            onValueChange = viewModel::onNoteChange,
            label = { Text(stringResource(R.string.note)) },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.ic_sticky_note),
                    contentDescription = null
                )
            },
            isError = uiState.medicationNoteErrorMessage != null,
            supportingText = if (uiState.medicationNoteErrorMessage != null) {
                { Text(uiState.medicationNoteErrorMessage!!) }
            } else null
        )

        ButtonWithIcon(
            text = stringResource(R.string.save),
            onClick = { viewModel.onSaveClick(onSuccess = onPopBackStack) },
            icon = {
                Icon(
                    painter = painterResource(R.drawable.ic_save),
                    contentDescription = stringResource(R.string.save)
                )
            },
            modifier = Modifier.fillMaxWidth()
        )
    }

    if (uiState.showStartDatePicker) {
        DatePickerModal(
            onDateSelected = viewModel::onStartDateChange,
            onDismiss = { viewModel.toggleStartDatePicker() }
        )
    }

    if (uiState.showEndDatePicker) {
        DatePickerModal(
            onDateSelected = viewModel::onEndDateChange,
            onDismiss = { viewModel.toggleEndDatePicker() }
        )
    }
}

@Composable
private fun MedicationScheduleCard(
    uiState: AddEditMedicationUiState,
    toggleStartDatePicker: () -> Unit,
    toggleEndDatePicker: () -> Unit,
    toggleRegularMedication: (Boolean) -> Unit
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(Dimen.spaceMedium),
            verticalArrangement = Arrangement.spacedBy(Dimen.spaceLarge)
        ) {
            Text(
                text = stringResource(R.string.medication_schedule),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = uiState.isMedicationRegular,
                    onClick = { toggleRegularMedication(true) },
                    shape = SegmentedButtonDefaults.itemShape(0, 2),
                    label = { Text(stringResource(R.string.regularly)) }
                )
                SegmentedButton(
                    selected = !uiState.isMedicationRegular,
                    onClick = { toggleRegularMedication(false) },
                    shape = SegmentedButtonDefaults.itemShape(1, 2),
                    label = { Text(stringResource(R.string.specific_dates)) }
                )
            }

            AnimatedVisibility(
                visible = !uiState.isMedicationRegular,
                enter = fadeIn(animationSpec = tween(durationMillis = 200, delayMillis = 100)) +
                        expandVertically(
                            animationSpec = tween(durationMillis = 300),
                            expandFrom = Alignment.Top
                        ),
                exit = shrinkVertically(
                    animationSpec = tween(durationMillis = 300),
                    shrinkTowards = Alignment.Top
                ) + fadeOut(animationSpec = tween(durationMillis = 200))
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(Dimen.spaceMedium)
                ) {
                    //StartDate
                    DatePickerField(
                        value = uiState.medicationStartDate?.let { formatDateToString(Date(it)) }
                            ?: stringResource(R.string.tap_to_select_date),
                        onClick = toggleStartDatePicker,
                        label = stringResource(R.string.start_date),
                        isError = uiState.medicationStartDateErrorMessage != null,
                        supportingText = if (uiState.medicationStartDateErrorMessage != null) {
                            { Text(uiState.medicationStartDateErrorMessage) }
                        } else null
                    )

                    //EndDate
                    DatePickerField(
                        value = uiState.medicationEndDate?.let { formatDateToString(Date(it)) }
                            ?: stringResource(R.string.tap_to_select_date),
                        onClick = toggleEndDatePicker,
                        label = stringResource(R.string.end_date),
                        isError = uiState.medicationEndDateErrorMessage != null,
                        supportingText = if (uiState.medicationEndDateErrorMessage != null) {
                            { Text(uiState.medicationEndDateErrorMessage) }
                        } else null
                    )
                }
            }
        }
    }
}