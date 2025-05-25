package com.example.petvitals.ui.screens.add_pet

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.example.petvitals.AddEditPet
import com.example.petvitals.R
import com.example.petvitals.ui.components.CustomIconButton
import com.example.petvitals.ui.components.CustomOutlinedTextField
import com.example.petvitals.ui.components.ScreenLayout
import com.example.petvitals.ui.components.TopBar
import com.example.petvitals.ui.components.ValueDropDown
import com.example.petvitals.ui.screens.add_pet.AddEditPetViewModel.PastOrPresentSelectableDates
import com.example.petvitals.ui.theme.Dimen

@Composable
fun AddEditPetScreen(
    addEditPet: AddEditPet,
    navigateToPets: () -> Unit,
    onPopBackStack: () -> Unit,
    viewModel: AddEditPetViewModel = hiltViewModel()

) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        addEditPet.petId?.let { petId ->
            viewModel.loadPetData(petId)
        }
    }

    if (uiState.showModal) {
        DatePickerModal(
            onDateSelected = viewModel::onBirthDateMillisChange,
            onDismiss = { viewModel.onShowModalChange(false) }
        )
    }

    ScreenLayout(
        columnModifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(Dimen.spaceMediumLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        topBar = {
            val addEditPetTitle = stringResource(if (addEditPet.petId == null) R.string.add_pet else R.string.edit_pet)
            TopBar(
                title = { Text(addEditPetTitle) },
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
        val imagePickerLauncher = rememberLauncherForActivityResult(PickVisualMedia()) { uri ->
            uri?.let { viewModel.onImageUriChange(it) }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val model = uiState.imageUri ?: uiState.petImageByteArray
            AsyncImage(
                model = model,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                fallback = painterResource(R.drawable.ic_add),
                modifier = Modifier
                    .size(Dimen.petIconSize)
                    .clip(RoundedCornerShape(100))
                    .background(MaterialTheme.colorScheme.tertiary)
                    .clickable(
                        onClick = {
                            imagePickerLauncher.launch(
                                PickVisualMediaRequest(PickVisualMedia.ImageOnly)
                            )
                        }
                    )
            )
            Text(text = stringResource(R.string.tap_to_select_photo))
        }


        CustomOutlinedTextField(
            value = uiState.name,
            onValueChange = viewModel::onNameChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.name)) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        )

        ValueDropDown(
            value = uiState.selectedSpecies,
            onValueChange = viewModel::onSpeciesChange,
            options = uiState.speciesOptions,
            label = stringResource(R.string.species),
            modifier = Modifier.fillMaxWidth()
        )

        Card(
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(Dimen.spaceMediumLarge)
                    .fillMaxWidth()
            ) {
                CheckboxWithLabel(
                    checked = uiState.isDateOfBirthApproximate,
                    onCheckedChange = viewModel::onDateOfBirthApproximateChange,
                    label = stringResource(R.string.approximate_date)
                )
                AnimatedContent(
                    targetState = uiState.isDateOfBirthApproximate,
                    transitionSpec = {
                        //Caution AI slop
                        if (targetState) {
                            (slideInHorizontally(animationSpec = tween(durationMillis = 400)) { fullWidth -> fullWidth / 4 } + fadeIn(animationSpec = tween(durationMillis = 200, delayMillis = 100)))
                                .togetherWith(slideOutHorizontally(animationSpec = tween(durationMillis = 400)) { fullWidth -> -fullWidth / 4 } + fadeOut(animationSpec = tween(durationMillis = 200)))
                        } else {
                            (slideInHorizontally(animationSpec = tween(durationMillis = 400)) { fullWidth -> -fullWidth / 4 } + fadeIn(animationSpec = tween(durationMillis = 200, delayMillis = 100)))
                                .togetherWith(slideOutHorizontally(animationSpec = tween(durationMillis = 400)) { fullWidth -> fullWidth / 4 } + fadeOut(animationSpec = tween(durationMillis = 200)))
                        }
                    },
                ) {
                    if (it) {
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
        }

        Button(
            onClick = {
                if (uiState.editMode) {
                    viewModel.updatePet(addEditPet.petId!!)
                } else {
                    viewModel.addPet()
                }
                navigateToPets()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.save))
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