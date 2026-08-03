package com.example.petvitals.ui.screens.managefood

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.petvitals.R
import com.example.petvitals.ui.components.CenterAlignedTopBar
import com.example.petvitals.ui.components.CustomMediumButton
import com.example.petvitals.ui.components.CustomTextField
import com.example.petvitals.ui.components.Loading
import com.example.petvitals.ui.components.ResetTopBarWhenNotScrollable
import com.example.petvitals.ui.components.rememberTopBarScrollBehavior
import com.example.petvitals.ui.components.showSnackbar
import com.example.petvitals.ui.navigation.AddEditFood
import com.example.petvitals.ui.theme.Dimen
import com.example.petvitals.ui.theme.PetVitalsTheme
import com.example.petvitals.ui.utils.ObserveAsEvents

@Composable
fun ManageFoodScreen(
    addEditFood: AddEditFood,
    onPopBackStack: () -> Unit,
    viewModel: ManageFoodViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(addEditFood) {
        viewModel.loadInitialData(
            petId = addEditFood.petId,
            foodId = addEditFood.foodId
        )
    }

    ObserveAsEvents(viewModel.events) {
        when (it) {
            is ManageFoodEvent.OnShowSnackbar -> {
                snackbarHostState.showSnackbar(it.snackbarState)
            }
        }
    }

    ManageFoodScreenContent(
        uiState = uiState,
        isEditing = addEditFood.foodId != null,
        snackbarHostState = snackbarHostState,
        onAction = viewModel::onAction,
        onPopBackStack = onPopBackStack
    )
}

@Composable
private fun ManageFoodScreenContent(
    uiState: ManageFoodUiState,
    isEditing: Boolean,
    snackbarHostState: SnackbarHostState,
    onAction: (ManageFoodAction) -> Unit,
    onPopBackStack: () -> Unit
) {
    val scrollBehavior = rememberTopBarScrollBehavior()
    val contentScrollState = rememberScrollState()

    ResetTopBarWhenNotScrollable(
        scrollBehavior = scrollBehavior,
        canScrollBackward = contentScrollState.canScrollBackward,
        canScrollForward = contentScrollState.canScrollForward,
        contentVisible = !uiState.isLoading
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            val title = if (isEditing) {
                stringResource(R.string.edit_food)
            } else {
                stringResource(R.string.add_food)
            }

            CenterAlignedTopBar(
                scrollBehavior = scrollBehavior,
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
    ) { paddingValues ->
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = Dimen.Screen.horizontalPadding)
                .fillMaxSize()
                .verticalScroll(contentScrollState)
        ) {
            if (uiState.isLoading) {
                Loading()
            } else {

                CustomTextField(
                    value = uiState.name,
                    onValueChange = { onAction(ManageFoodAction.OnNameChange(it)) },
                    label = { Text(stringResource(R.string.name)) },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_label),
                            contentDescription = null
                        )
                    },
                    isError = uiState.nameErrorMessageRes != null,
                    supportingText = uiState.nameErrorMessageRes?.let { { Text(stringResource(it)) } }
                )

                CustomTextField(
                    value = uiState.portion,
                    onValueChange = { onAction(ManageFoodAction.OnPortionChange(it)) },
                    label = { Text(stringResource(R.string.portion)) },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_scale),
                            contentDescription = null
                        )
                    },
                    isError = uiState.portionErrorMessageRes != null,
                    supportingText = uiState.portionErrorMessageRes?.let { { Text(stringResource(it)) } }
                )

                CustomTextField(
                    value = uiState.frequency,
                    onValueChange = { onAction(ManageFoodAction.OnFrequencyChange(it)) },
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

                CustomTextField(
                    value = uiState.note,
                    onValueChange = { onAction(ManageFoodAction.OnNoteChange(it)) },
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
                    onClick = { onAction(ManageFoodAction.OnSave(onSuccess = onPopBackStack)) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.save),
                        fontSize = Dimen.FontSize.mediumButton
                    )
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun ManageFoodScreenContentPreview() {
    PetVitalsTheme {
        ManageFoodScreenContent(
            uiState = ManageFoodUiState(),
            isEditing = false,
            snackbarHostState = SnackbarHostState(),
            onAction = {},
            onPopBackStack = {}
        )
    }
}