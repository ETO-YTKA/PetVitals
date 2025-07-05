package com.example.petvitals.ui.screens.add_edit_record

import android.icu.util.Calendar
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerLayoutType
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.example.petvitals.AddEditRecord
import com.example.petvitals.R
import com.example.petvitals.data.repository.pet.Pet
import com.example.petvitals.data.repository.pet.PetSpecies
import com.example.petvitals.ui.components.ButtonWithIcon
import com.example.petvitals.ui.components.CustomIconButton
import com.example.petvitals.ui.components.CustomOutlinedTextField
import com.example.petvitals.ui.components.DatePickerField
import com.example.petvitals.ui.components.ScreenLayout
import com.example.petvitals.ui.components.TopBar
import com.example.petvitals.ui.components.ValueDropDown
import com.example.petvitals.ui.theme.Dimen
import com.example.petvitals.utils.decodeBase64ToImage

@Composable
fun AddEditRecordScreen(
    addEditRecord: AddEditRecord,
    onPopBackStack: () -> Unit,
    onNavigateToRecords: () -> Unit,
    viewModel: AddEditRecordViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        addEditRecord.recordId?.let { id ->
            viewModel.loadRecordData(id)
        }
    }

    if (uiState.showDatePicker) {
        DatePickerModal(
            onDateSelected = viewModel::onDateChange,
            onDismiss = { viewModel.onShowDatePickerChange(false) }
        )
    }

    if (uiState.showTimePicker) {
        TimePickerModal(
            onDismissRequest = {
                viewModel.onShowTimePickerChange(false)
            },
            onConfirm = { hours, minutes ->
                viewModel.onTimeChange(hours, minutes)
            }
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
            val title = stringResource(
            if (addEditRecord.recordId == null) R.string.create_record
                    else R.string.edit_record
            )

            TopBar(
                title = { Text(title) },
                navigationIcon = {
                    CustomIconButton(
                        onClick = onPopBackStack,
                        painter = painterResource(R.drawable.ic_arrow_back),
                        contentDescription = stringResource(R.string.back)
                    )
                }
            )
        },
        columnModifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(vertical = Dimen.spaceMedium)
    ) {
        //Title
        CustomOutlinedTextField(
            value = uiState.title,
            onValueChange = viewModel::onTitleChange,
            label = { Text(stringResource(R.string.title)) },
            modifier = Modifier.fillMaxWidth(),
            isError = uiState.isTitleError,
            supportingText = {
                if (uiState.isTitleError) {
                    Text(uiState.titleErrorMessage ?: "")
                }
            }
        )

        //Type
        ValueDropDown(
            value = uiState.selectedType,
            onValueChange = viewModel::onTypeChange,
            options = uiState.typeOptions,
            label = stringResource(R.string.type),
            supportingText = {}
        )

        //Date
        DatePickerField(
            value = viewModel.formatDateForDisplay(date = uiState.date, context = context),
            onClick = { viewModel.onShowDatePickerChange(true) },
            label = stringResource(R.string.date),
            modifier = Modifier.fillMaxWidth(),
            supportingText = {}
        )

        Spacer(modifier = Modifier.height(Dimen.spaceSmall))

        //Attach pets
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
                },
                modifier = Modifier.fillMaxWidth()
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

        //Description
        CustomOutlinedTextField(
            value = uiState.description,
            onValueChange = viewModel::onDescriptionChange,
            label = { Text(stringResource(R.string.description)) },
            modifier = Modifier.fillMaxWidth(),
            isError = uiState.isDescriptionError,
            supportingText = {
                if (uiState.isDescriptionError) {
                    Text(uiState.descriptionErrorMessage ?: "")
                }
            }
        )

        Spacer(modifier = Modifier.height(Dimen.spaceSmall))

        //Save button
        ButtonWithIcon(
            text = stringResource(R.string.save),
            onClick = {
                viewModel.saveRecord(
                    recordId = addEditRecord.recordId,
                    onSuccess = onNavigateToRecords
                )
            },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_save),
                    contentDescription = null
                )
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
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
fun TimePickerModal(
    onDismissRequest: () -> Unit,
    onConfirm: (Int, Int) -> Unit
) {
    val currentTime = Calendar.getInstance()

    val timePickerState = rememberTimePickerState(
        initialHour = currentTime.get(Calendar.HOUR_OF_DAY),
        initialMinute = currentTime.get(Calendar.MINUTE),
        is24Hour = true,
    )

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.select_time),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp)
                )

                TimePicker(
                    state = timePickerState,
                    layoutType = TimePickerLayoutType.Vertical
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text(stringResource(R.string.cancel))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = {
                            onConfirm(timePickerState.hour, timePickerState.minute)
                            onDismissRequest()
                        }
                    ) {
                        Text(stringResource(R.string.ok))
                    }
                }
            }
        }
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