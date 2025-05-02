package com.example.petvitals.ui.screens.add_pet

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.petvitals.R
import com.example.petvitals.ui.components.CustomOutlinedTextField
import com.example.petvitals.ui.components.DropDownOption
import com.example.petvitals.ui.components.ScreenLayout
import com.example.petvitals.ui.screens.add_pet.AddPetViewModel.PastOrPresentSelectableDates
import com.example.petvitals.ui.theme.Dimen

@Composable
fun AddPetScreen(
    viewModel: AddPetViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    if (uiState.showModal) {
        DatePickerModal(
            onDateSelected = viewModel::onBirthDateMillisChange,
            onDismiss = { viewModel.onShowModalChange(false) }
        )
    }

    ScreenLayout(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(Dimen.spaceMediumLarge),
        horizontalAlignment = Alignment.Start
    ) {
        CustomOutlinedTextField(
            value = uiState.name,
            onValueChange = viewModel::onNameChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.name)) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        )

        DropDown(
            value = uiState.species,
            onValueChange = viewModel::onSpeciesChange,
            options = viewModel.getSpeciesList(),
            label = stringResource(R.string.species),
            modifier = Modifier.fillMaxWidth()
        )

        Card(
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier
                    .padding(Dimen.spaceMediumLarge)
                    .fillMaxWidth()
            ) {
                CheckboxWithLabel(
                    checked = uiState.isDateOfBirthApproximate,
                    onCheckedChange = viewModel::onDateOfBirthApproximateChange,
                    label = stringResource(R.string.date_of_birth_is_approximate)
                )
                if (uiState.isDateOfBirthApproximate) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Dimen.spaceMedium),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ValueDropDown(
                            value = uiState.selectedBirthMonth,
                            onValueChange = viewModel::onBirthMonthChange,
                            options = uiState.monthOptions,
                            label = stringResource(R.string.month),
                            modifier = Modifier.weight(1f)
                        )
                        CustomOutlinedTextField(
                            value = uiState.birthYear,
                            onValueChange = viewModel::onBirthYearChange,
                            label = { Text(stringResource(R.string.year)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    BirthDatePickerField(
                        value = viewModel.formatDateForDisplay (millis = uiState.birthDateMillis, context = context) ,
                        onClick = { viewModel.onShowModalChange(true) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Button(
            onClick = { viewModel.addPet() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.save))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropDown(
    value: String,
    onValueChange: (String) -> Unit,
    options: List<String>,
    label: String,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        CustomOutlinedTextField(
            value = value,
            onValueChange = { },
            label = { Text(text = label) },
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryEditable)
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            shape = MaterialTheme.shapes.large
        ) {
            options.forEach { selectionOption ->
                DropdownMenuItem(
                    text = { Text(selectionOption) },
                    onClick = {
                        onValueChange(selectionOption)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> ValueDropDown(
    value: T,
    onValueChange: (T) -> Unit,
    options: List<DropDownOption<T>>,
    label: String,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    val selectedOptionDisplay = remember(value, options) {
        options.find { it.value == value }!!.display
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        CustomOutlinedTextField(
            value = selectedOptionDisplay,
            onValueChange = { },
            label = { Text(text = label) },
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryEditable)
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            shape = MaterialTheme.shapes.large
        ) {
            options.forEach { selectionOption ->
                DropdownMenuItem(
                    text = { Text(selectionOption.display) },
                    onClick = {
                        onValueChange(selectionOption.value)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

@Composable
private fun CheckboxWithLabel(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = { onCheckedChange(!checked) },
                role = Role.Checkbox
            )
            .padding(vertical = Dimen.spaceSmall)
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerModal(
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        selectableDates = PastOrPresentSelectableDates
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onDateSelected(datePickerState.selectedDateMillis)
                onDismiss()
            }) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    ) {
        DatePicker(
            state = datePickerState,
            showModeToggle = false
        )
    }
}

@Composable
private fun BirthDatePickerField(
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    CustomOutlinedTextField(
        value = value,
        onValueChange = { },
        modifier = modifier.fillMaxWidth(),
        readOnly = true,
        label = { Text(stringResource(R.string.date_of_birth)) },
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = null
            )
        },
        interactionSource = remember { MutableInteractionSource() }
            .also { interactionSource ->
                LaunchedEffect(interactionSource) {
                    interactionSource.interactions.collect {
                        if (it is PressInteraction.Release) {
                            onClick()
                        }
                    }
                }
            }
    )
}