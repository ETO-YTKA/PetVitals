@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.petvitals.ui.screens.managerecord

import android.text.format.DateFormat
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.example.petvitals.R
import com.example.petvitals.domain.models.Pet
import com.example.petvitals.domain.models.PetSpecies
import com.example.petvitals.domain.models.RecordType
import com.example.petvitals.ui.components.CenterAlignedTopBar
import com.example.petvitals.ui.components.CustomMediumButton
import com.example.petvitals.ui.components.CustomSnackbarHost
import com.example.petvitals.ui.components.CustomTextField
import com.example.petvitals.ui.components.DatePickerField
import com.example.petvitals.ui.components.DatePickerModal
import com.example.petvitals.ui.components.DropDownOption
import com.example.petvitals.ui.components.Loading
import com.example.petvitals.ui.components.ResetTopBarWhenNotScrollable
import com.example.petvitals.ui.components.SnackbarState
import com.example.petvitals.ui.components.ValueDropDown
import com.example.petvitals.ui.components.rememberTopBarScrollBehavior
import com.example.petvitals.ui.components.showSnackbar
import com.example.petvitals.ui.navigation.AddEditRecord
import com.example.petvitals.ui.theme.Dimen
import com.example.petvitals.ui.theme.PetVitalsTheme
import com.example.petvitals.ui.utils.ObserveAsEvents
import com.example.petvitals.ui.utils.decodeBase64ToImage
import java.util.Calendar
import java.util.Date

@Composable
fun ManageRecordScreen(
    addEditRecord: AddEditRecord,
    onPopBackStack: () -> Unit,
    viewModel: ManageRecordViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val resources = LocalResources.current

    LaunchedEffect(addEditRecord) {
        viewModel.loadInitialData(addEditRecord.recordId)
    }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is ManageRecordEvent.OnShowSnackbar -> snackbarHostState.showSnackbar(
                SnackbarState(
                    message = resources.getString(event.messageRes),
                    snackbarType = event.snackbarType
                )
            )
        }
    }

    ManageRecordScreenContent(
        uiState = uiState,
        isEditing = addEditRecord.recordId != null,
        snackbarHostState = snackbarHostState,
        onAction = viewModel::onAction,
        onPopBackStack = onPopBackStack
    )
}

@Composable
private fun ManageRecordScreenContent(
    uiState: ManageRecordUiState,
    isEditing: Boolean,
    snackbarHostState: SnackbarHostState,
    onAction: (ManageRecordAction) -> Unit,
    onPopBackStack: () -> Unit
) {
    val scrollBehavior = rememberTopBarScrollBehavior()
    val scrollState = rememberScrollState()

    ResetTopBarWhenNotScrollable(
        scrollBehavior = scrollBehavior,
        canScrollBackward = scrollState.canScrollBackward,
        canScrollForward = scrollState.canScrollForward,
        contentVisible = !uiState.isLoading && uiState.loadErrorMessageRes == null
    )

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { CustomSnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopBar(
                scrollBehavior = scrollBehavior,
                title = {
                    Text(
                        stringResource(
                            if (isEditing) R.string.edit_record else R.string.create_record
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
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(0.dp))
            when {
                uiState.isLoading -> Loading()
                uiState.loadErrorMessageRes != null -> RecordLoadError(
                    message = stringResource(uiState.loadErrorMessageRes),
                    onRetry = { onAction(ManageRecordAction.OnRetryLoad) }
                )
                else -> RecordForm(
                    uiState = uiState,
                    onAction = onAction,
                    onSave = {
                        onAction(
                            ManageRecordAction.OnSave(
                                fallbackTitle = it,
                                onSuccess = onPopBackStack
                            )
                        )
                    }
                )
            }
        }
    }

    RecordDialogs(uiState = uiState, onAction = onAction)
}

@Composable
private fun RecordLoadError(message: String, onRetry: () -> Unit) {
    Text(
        text = message,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    CustomMediumButton(onClick = onRetry, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.retry))
    }
}

@Composable
private fun RecordForm(
    uiState: ManageRecordUiState,
    onAction: (ManageRecordAction) -> Unit,
    onSave: (String) -> Unit
) {
    val context = LocalContext.current
    val typeOptions = RecordType.entries.map { type ->
        DropDownOption(stringResource(type.titleResId), type)
    }
    val selectedPets = uiState.availablePets.filter { it.id in uiState.selectedPetIds }
    val fallbackTitle = stringResource(uiState.selectedType.titleResId)
    val formattedDate = remember(uiState.eventDate, context) {
        val date = Date(uiState.eventDate)
        "${DateFormat.getMediumDateFormat(context).format(date)} " +
            DateFormat.getTimeFormat(context).format(date)
    }

    CustomTextField(
        value = uiState.title,
        onValueChange = { onAction(ManageRecordAction.OnTitleChange(it)) },
        label = { Text(stringResource(R.string.title)) },
        modifier = Modifier.fillMaxWidth(),
        isError = uiState.titleErrorMessageRes != null,
        supportingText = uiState.titleErrorMessageRes?.let { { Text(stringResource(it)) } },
        singleLine = true
    )

    ValueDropDown(
        value = uiState.selectedType,
        onValueChange = { onAction(ManageRecordAction.OnTypeChange(it)) },
        options = typeOptions,
        label = stringResource(R.string.type)
    )

    DatePickerField(
        value = formattedDate,
        onClick = { onAction(ManageRecordAction.OnDatePickerToggle) },
        label = stringResource(R.string.date),
        modifier = Modifier.fillMaxWidth()
    )

    AttachedPetsSection(
        pets = selectedPets,
        errorMessageRes = uiState.petSelectionErrorMessageRes,
        onPetClick = { onAction(ManageRecordAction.OnPetToggle(it.id)) },
        onOpenSelector = { onAction(ManageRecordAction.OnPetSelectorToggle) }
    )

    CustomTextField(
        value = uiState.description,
        onValueChange = { onAction(ManageRecordAction.OnDescriptionChange(it)) },
        label = { Text(stringResource(R.string.description)) },
        modifier = Modifier.fillMaxWidth(),
        isError = uiState.descriptionErrorMessageRes != null,
        supportingText = uiState.descriptionErrorMessageRes?.let {
            { Text(stringResource(it)) }
        },
        minLines = 3
    )

    if (uiState.hasConflict) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .semantics { liveRegion = LiveRegionMode.Polite },
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.errorContainer
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.record_changed_error),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                TextButton(onClick = { onAction(ManageRecordAction.OnReloadLatest) }) {
                    Text(stringResource(R.string.reload_latest))
                }
            }
        }
    }

    CustomMediumButton(
        onClick = { onSave(fallbackTitle) },
        enabled = !uiState.isSaving,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        Text(stringResource(R.string.save))
    }
}

@Composable
private fun AttachedPetsSection(
    pets: List<Pet>,
    errorMessageRes: Int?,
    onPetClick: (Pet) -> Unit,
    onOpenSelector: () -> Unit
) {
    val hasError = errorMessageRes != null
    val selectionSummary = if (pets.isEmpty()) {
        stringResource(R.string.no_pets_attached)
    } else {
        pluralStringResource(R.plurals.pets_attached_count, pets.size, pets.size)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = if (hasError) {
            BorderStroke(1.dp, MaterialTheme.colorScheme.error)
        } else {
            null
        }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = stringResource(R.string.pets),
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = selectionSummary,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                TextButton(
                    onClick = onOpenSelector,
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text(
                        stringResource(
                            if (pets.isEmpty()) R.string.add else R.string.change
                        )
                    )
                }
            }

            if (pets.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    pets.forEach { pet ->
                        AttachedPetPill(
                            pet = pet,
                            onRemove = { onPetClick(pet) }
                        )
                    }
                }
            }

            errorMessageRes?.let {
                Text(
                    text = stringResource(it),
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun RecordDialogs(
    uiState: ManageRecordUiState,
    onAction: (ManageRecordAction) -> Unit
) {
    if (uiState.showDatePicker) {
        DatePickerModal(
            datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = eventDateToPickerDate(uiState.eventDate)
            ),
            onDateSelected = { onAction(ManageRecordAction.OnDateChange(it)) },
            onDismiss = { onAction(ManageRecordAction.OnDatePickerToggle) }
        )
    }

    if (uiState.showTimePicker) {
        val context = LocalContext.current
        val calendar = remember(uiState.eventDate) {
            Calendar.getInstance().apply { timeInMillis = uiState.eventDate }
        }
        TimePickerModal(
            initialHour = calendar.get(Calendar.HOUR_OF_DAY),
            initialMinute = calendar.get(Calendar.MINUTE),
            is24Hour = DateFormat.is24HourFormat(context),
            onDismissRequest = { onAction(ManageRecordAction.OnTimePickerToggle) },
            onConfirm = { hour, minute ->
                onAction(ManageRecordAction.OnTimeChange(hour, minute))
            }
        )
    }

    if (uiState.showPetSelector) {
        ModalBottomSheet(
            onDismissRequest = { onAction(ManageRecordAction.OnPetSelectorToggle) }
        ) {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                uiState.availablePets.forEach { pet ->
                    PetChoiceChip(
                        pet = pet,
                        selected = pet.id in uiState.selectedPetIds,
                        onClick = { onAction(ManageRecordAction.OnPetToggle(pet.id)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TimePickerModal(
    initialHour: Int,
    initialMinute: Int,
    is24Hour: Boolean,
    onDismissRequest: () -> Unit,
    onConfirm: (Int, Int) -> Unit
) {
    val state = rememberTimePickerState(initialHour, initialMinute, is24Hour)
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    stringResource(R.string.select_time),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(20.dp))
                TimePicker(state = state, layoutType = TimePickerLayoutType.Vertical)
                Spacer(Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismissRequest) { Text(stringResource(R.string.cancel)) }
                    TextButton(onClick = { onConfirm(state.hour, state.minute) }) {
                        Text(stringResource(R.string.ok))
                    }
                }
            }
        }
    }
}

@Composable
private fun AttachedPetPill(
    pet: Pet,
    onRemove: () -> Unit
) {
    Surface(
        modifier = Modifier
            .heightIn(min = 48.dp)
            .widthIn(max = 220.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PetAvatar(pet = pet, modifier = Modifier.size(28.dp))

            Text(
                text = pet.name,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .widthIn(max = 120.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelLarge
            )

            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_close),
                    contentDescription = stringResource(R.string.remove_pet, pet.name),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun PetChoiceChip(
    pet: Pet,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(onClick = onClick, shape = CircleShape) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PetAvatar(pet = pet, modifier = Modifier.size(24.dp))

            Text(
                text = pet.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Icon(
                painter = painterResource(
                    if (selected) R.drawable.ic_remove
                    else R.drawable.ic_add
                ),
                contentDescription = stringResource(
                    if (selected) R.string.remove_pet else R.string.add_pet_to_record,
                    pet.name
                )
            )
        }
    }
}

@Composable
private fun PetAvatar(
    pet: Pet,
    modifier: Modifier = Modifier
) {
    val image = remember(pet.avatar) {
        pet.avatar?.let { encodedAvatar ->
            runCatching { decodeBase64ToImage(encodedAvatar) }.getOrNull()
        }
    }

    if (image == null) {
        Icon(
            painter = painterResource(pet.species.drawableRes),
            contentDescription = null,
            modifier = modifier.size(24.dp)
        )
    } else {
        AsyncImage(
            model = image,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier.clip(CircleShape)
        )
    }
}

@PreviewLightDark
@Composable
private fun ManageRecordScreenContentPreview() {
    val previewPets = listOf(
        Pet(id = "mochi", name = "Mochi", species = PetSpecies.CAT),
        Pet(
            id = "pepper",
            name = "Pepper with a very long name",
            species = PetSpecies.DOG
        )
    )

    PetVitalsTheme {
        ManageRecordScreenContent(
            uiState = ManageRecordUiState(
                availablePets = previewPets,
                selectedPetIds = previewPets.mapTo(linkedSetOf()) { it.id }
            ),
            isEditing = false,
            snackbarHostState = SnackbarHostState(),
            onAction = {},
            onPopBackStack = {}
        )
    }
}