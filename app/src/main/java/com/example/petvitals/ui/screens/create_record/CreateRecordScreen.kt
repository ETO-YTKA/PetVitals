package com.example.petvitals.ui.screens.create_record

import android.icu.util.Calendar
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.petvitals.R
import com.example.petvitals.ui.components.ButtonWithIcon
import com.example.petvitals.ui.components.CustomOutlinedTextField
import com.example.petvitals.ui.components.ScreenLayout
import com.example.petvitals.ui.components.TopBarBackButton
import com.example.petvitals.ui.components.ValueDropDown
import com.example.petvitals.ui.theme.Dimen

@Composable
fun CreateRecordScreen(
    onPopBackStack: () -> Unit,
    viewModel: CreateRecordViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    if (uiState.showModal) {
        DatePickerModal(
            onDateSelected = viewModel::onDateChange,
            onDismiss = { viewModel.onShowModalChange(false) }
        )
    }

    ScreenLayout(
        verticalArrangement = Arrangement.Top,
        topBar = {
            TopBarBackButton(
                title = stringResource(R.string.create_record),
                onPopBackStack = onPopBackStack
            )
        }
    ) {
        CustomOutlinedTextField(
            value = uiState.title,
            onValueChange = viewModel::onTitleChange,
            label = { Text(stringResource(R.string.title)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(Dimen.spaceLarge))

        Row {
            ValueDropDown(
                value = uiState.selectedType,
                onValueChange = viewModel::onTypeChange,
                options = uiState.typeOptions,
                label = stringResource(R.string.type),
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(Dimen.spaceMedium))
            DatePickerField(
                value = viewModel.formatDateForDisplay(millis = uiState.date, context = context),
                onClick = { viewModel.onShowModalChange(true) },
                label = stringResource(R.string.date),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(Dimen.spaceLarge))

        CustomOutlinedTextField(
            value = uiState.description,
            onValueChange = viewModel::onDescriptionChange,
            label = { Text(stringResource(R.string.description)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(Dimen.spaceHuge))

        ButtonWithIcon(
            text = stringResource(R.string.create_record),
            onClick = { viewModel.createRecord() },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_check),
                    contentDescription = null
                )
            }
        )
    }
}

@Composable
private fun DatePickerField(
    value: String,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    CustomOutlinedTextField(
        value = value,
        onValueChange = { },
        modifier = modifier,
        readOnly = true,
        label = { Text(label) },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerModal(
    onDateSelected: (Long?) -> Unit,
    initialSelectedDateMillis: Long = Calendar.getInstance().timeInMillis,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialSelectedDateMillis
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