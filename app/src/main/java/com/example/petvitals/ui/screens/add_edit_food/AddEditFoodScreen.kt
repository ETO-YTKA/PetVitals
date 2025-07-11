package com.example.petvitals.ui.screens.add_edit_food

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.petvitals.AddEditFood
import com.example.petvitals.R
import com.example.petvitals.ui.components.ButtonWithIcon
import com.example.petvitals.ui.components.CustomOutlinedTextField
import com.example.petvitals.ui.components.Loading
import com.example.petvitals.ui.components.ScreenLayout
import com.example.petvitals.ui.components.TopBar
import com.example.petvitals.ui.theme.Dimen

@Composable
fun AddEditFoodScreen(
    addEditFood: AddEditFood,
    onPopBackStack: () -> Unit,
    viewModel: AddEditFoodViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadInitialData(
            petId = addEditFood.petId,
            foodId = addEditFood.foodId
        )
    }

    ScreenLayout(
        verticalArrangement = Arrangement.spacedBy(Dimen.spaceLarge),
        horizontalAlignment = Alignment.Start,
        columnModifier = Modifier
            .padding(vertical = Dimen.spaceMedium)
            .fillMaxSize()
            .then(if (uiState.isLoading) Modifier else Modifier.verticalScroll(rememberScrollState())),
        topBar = {
            val title = when (addEditFood.foodId) {
                null -> stringResource(R.string.add_food)
                else -> stringResource(R.string.edit_food)
            }

            TopBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(
                        onClick = onPopBackStack
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) {

        if (uiState.isLoading) { Loading() } else {

            CustomOutlinedTextField(
                value = uiState.name,
                onValueChange = { viewModel.onNameChange(it) },
                label = { Text(stringResource(R.string.name)) },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_label),
                        contentDescription = null
                    )
                },
                isError = uiState.nameErrorMessage != null,
                supportingText = uiState.nameErrorMessage
            )

            CustomOutlinedTextField(
                value = uiState.portion,
                onValueChange = { viewModel.onPortionChange(it) },
                label = { Text(stringResource(R.string.portion)) },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_scale),
                        contentDescription = null
                    )
                },
                isError = uiState.portionErrorMessage != null,
                supportingText = uiState.portionErrorMessage
            )

            CustomOutlinedTextField(
                value = uiState.frequency,
                onValueChange = { viewModel.onFrequencyChange(it) },
                label = { Text(stringResource(R.string.frequency)) },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_history),
                        contentDescription = null
                    )
                },
                isError = uiState.frequencyErrorMessage != null,
                supportingText = uiState.frequencyErrorMessage
            )

            CustomOutlinedTextField(
                value = uiState.note,
                onValueChange = { viewModel.onNoteChange(it) },
                label = { Text(stringResource(R.string.note)) },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_sticky_note),
                        contentDescription = null
                    )
                },
                isError = uiState.noteErrorMessage != null,
                supportingText = uiState.noteErrorMessage
            )

            ButtonWithIcon(
                text = stringResource(R.string.save),
                onClick = { viewModel.save(onSuccess = onPopBackStack) },
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_save),
                        contentDescription = stringResource(R.string.save)
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}