package com.example.petvitals.ui.screens.create_record

import android.icu.util.Calendar
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.example.petvitals.R
import com.example.petvitals.data.repository.pet.Pet
import com.example.petvitals.data.repository.pet.PetSpecies
import com.example.petvitals.ui.components.ButtonWithIcon
import com.example.petvitals.ui.components.CustomIconButton
import com.example.petvitals.ui.components.CustomOutlinedTextField
import com.example.petvitals.ui.components.ScreenLayout
import com.example.petvitals.ui.components.TopBar
import com.example.petvitals.ui.components.ValueDropDown
import com.example.petvitals.ui.theme.Dimen
import com.example.petvitals.utils.decodeBase64ToImage

@Composable
fun CreateRecordScreen(
    onPopBackStack: () -> Unit,
    onNavigateToRecords: () -> Unit,
    viewModel: CreateRecordViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    if (uiState.showDatePicker) {
        DatePickerModal(
            onDateSelected = viewModel::onDateChange,
            onDismiss = { viewModel.onShowDatePickerChange(false) }
        )
    }

    if (uiState.showBottomSheet) {
        BottomSheetModal(
            onDismissRequest = { viewModel.onShowBottomSheetChange(false) },
            onPetClick = viewModel::onPetSelected,
            pets = uiState.pets,
            selectedPets = uiState.selectedPets
        )
    }

    ScreenLayout(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        topBar = {
            TopBar(
                title = { Text(stringResource(R.string.create_record)) },
                navigationIcon = {
                    CustomIconButton(
                        onClick = onPopBackStack,
                        painter = painterResource(R.drawable.ic_arrow_back),
                        contentDescription = stringResource(R.string.back)
                    )
                }
            )
        },
        columnModifier = Modifier.verticalScroll(rememberScrollState()),
    ) {
        CustomOutlinedTextField(
            value = uiState.title,
            onValueChange = viewModel::onTitleChange,
            label = { Text(stringResource(R.string.title)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(Dimen.spaceMedium))

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
                onClick = { viewModel.onShowDatePickerChange(true) },
                label = stringResource(R.string.date),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(Dimen.spaceMedium))

        Column(
            Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.large)
                .padding(Dimen.spaceMedium),
        ) {
            ButtonWithIcon(
                onClick = { viewModel.onShowBottomSheetChange(true) },
                text = stringResource(R.string.attach_pet),
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_add),
                        contentDescription = null
                    )
                }
            )

            if (uiState.selectedPets.isNotEmpty()) {
                Spacer(modifier = Modifier.height(Dimen.spaceMedium))
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(Dimen.spaceSmall),
                verticalArrangement = Arrangement.spacedBy(Dimen.spaceSmall),
            ) {
                uiState.selectedPets.forEach { pet ->
                    PetCard(
                        pet = pet,
                        onClick = { viewModel.onPetSelected(pet) },
                        isSelected = true,
                    )
                }
            }
        }



        Spacer(modifier = Modifier.height(Dimen.spaceMedium))

        CustomOutlinedTextField(
            value = uiState.description,
            onValueChange = viewModel::onDescriptionChange,
            label = { Text(stringResource(R.string.description)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(Dimen.spaceLarge))

        ButtonWithIcon(
            text = stringResource(R.string.create_record),
            onClick = {
                viewModel.onCreateRecordClick()
                onNavigateToRecords()
            },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BottomSheetModal(
    onDismissRequest: () -> Unit,
    onPetClick: (Pet) -> Unit,
    pets: List<Pet>,
    selectedPets: List<Pet>
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest
    ) {
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
                .padding(horizontal = Dimen.spaceSmall),
            horizontalArrangement = Arrangement.spacedBy(Dimen.spaceSmall),
            verticalArrangement = Arrangement.spacedBy(Dimen.spaceSmall)
        ) {
            pets.forEach { pet ->
                PetCard(
                    pet = pet,
                    onClick = { onPetClick(pet) },
                    isSelected = selectedPets.contains(pet)
                )
            }
        }
    }
}

@Composable
private fun PetCard(
    pet: Pet,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false
) {
    Card(
        onClick = onClick,
        shape = CircleShape,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(Dimen.spaceMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val painterRes = if (pet.species == PetSpecies.CAT) R.drawable.ic_cat
                else R.drawable.ic_dog
            val image = pet.avatar?.let { remember { decodeBase64ToImage(pet.avatar) } }
            val imageModifier = Modifier
                .size(24.dp)
                .then(if (image != null) Modifier.clip(CircleShape) else Modifier)

            AsyncImage(
                model = image,
                contentDescription = pet.name,
                contentScale = ContentScale.Crop,
                fallback = painterResource(painterRes),
                modifier = imageModifier
            )

            Spacer(modifier = Modifier.width(Dimen.spaceMedium))

            Text(
                text = pet.name,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.width(Dimen.spaceMedium))

            Icon(
                painter = if (isSelected) painterResource(id = R.drawable.ic_remove) else painterResource(id = R.drawable.ic_add),
                contentDescription = null
            )
        }
    }
}