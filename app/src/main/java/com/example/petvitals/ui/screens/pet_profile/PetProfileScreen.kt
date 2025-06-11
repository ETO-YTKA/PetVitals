package com.example.petvitals.ui.screens.pet_profile

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.example.petvitals.PetProfile
import com.example.petvitals.R
import com.example.petvitals.data.repository.pet.Gender
import com.example.petvitals.data.repository.pet.PetSpecies
import com.example.petvitals.ui.components.ButtonWithIcon
import com.example.petvitals.ui.components.CustomIconButton
import com.example.petvitals.ui.components.CustomOutlinedTextField
import com.example.petvitals.ui.components.DatePickerField
import com.example.petvitals.ui.components.DatePickerModal
import com.example.petvitals.ui.components.ScreenLayout
import com.example.petvitals.ui.components.TopBar
import com.example.petvitals.ui.theme.Dimen
import com.example.petvitals.utils.decodeBase64ToImage
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun PetProfileScreen(
    petProfile: PetProfile,
    onPopBackStack: () -> Unit,
    onNavigateToEditPet: (String) -> Unit,
    viewModel: PetProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.getPetData(petProfile.petId)
    }

    ScreenLayout(
        columnModifier = Modifier.verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(Dimen.spaceMedium),
        topBar = {
            TopBar(
                title = { Text(stringResource(R.string.pet_profile)) },
                navigationIcon = {
                    CustomIconButton(
                        onClick = onPopBackStack,
                        painter = painterResource(R.drawable.ic_arrow_back),
                        contentDescription = stringResource(R.string.back)
                    )
                },
                actions = {
                    CustomIconButton(
                        onClick = { onNavigateToEditPet(petProfile.petId) },
                        painter = painterResource(R.drawable.ic_edit),
                        contentDescription = stringResource(R.string.edit_pet)
                    )
                    CustomIconButton(
                        onClick = {
                            viewModel.deletePet(petProfile.petId)
                            onPopBackStack()
                        },
                        painter = painterResource(R.drawable.ic_delete_forever),
                        contentDescription = stringResource(R.string.delete_pet)
                    )
                }
            )
        }
    ) {
        Spacer(modifier = Modifier.height(0.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimen.spaceSmall)
        ) {
            ProfilePic(uiState)
            GeneralInfo(uiState)
        }

        SectionCard(
            title = stringResource(R.string.health),
            icon = painterResource(R.drawable.ic_health_and_safety),
        ) {
            CardItem(
                title = stringResource(R.string.date_of_birth),
                information = uiState.dob,
                infoIcon = painterResource(R.drawable.ic_cake)
            )

            Note(
                title = stringResource(R.string.note),
                content = uiState.pet.healthNote,
                updatedContent = uiState.updatedHealthNote,
                onValueChange = viewModel::onHealthNoteChange,
                onEditClick = viewModel::toggleHealthNoteEditMode,
                onSaveClick = viewModel::onSaveHealthNoteClick,
                inEditMode = uiState.isHealthNoteInEditMode
            )

            ButtonWithIcon(
                text = stringResource(R.string.add_medication),
                onClick = viewModel::toggleMedicationModal,
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_add),
                        contentDescription = stringResource(R.string.add_medication)
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (uiState.showMedicationModal) {
        MedicationBottomSheet(
            onDismiss = viewModel::toggleMedicationModal,
            onNameChange = viewModel::onMedicationNameChange,
            onDosageChange = viewModel::onMedicationDosageChange,
            onFrequencyChange = viewModel::onMedicationFrequencyChange,
            toggleStartDatePicker = viewModel::toggleStartDatePicker,
            toggleEndDatePicker = viewModel::toggleEndDatePicker,
            onNoteChange = viewModel::onMedicationNoteChange,
            onSaveClick = viewModel::toggleMedicationModal,
            uiState = uiState
        )
    }

    if (uiState.showStartDatePicker) {
        DatePickerModal(
            onDateSelected = viewModel::onMedicationStartDateChange,
            onDismiss = { viewModel.toggleStartDatePicker() }
        )
    }

    if (uiState.showEndDatePicker) {
        DatePickerModal(
            onDateSelected = viewModel::onMedicationEndDateChange,
            onDismiss = { viewModel.toggleEndDatePicker() }
        )
    }
}

@Composable
private fun SectionCard(
    title: String,
    modifier: Modifier = Modifier,
    icon: Painter? = null,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(Dimen.spaceMedium),
            verticalArrangement = Arrangement.spacedBy(Dimen.spaceMedium)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimen.spaceSmall)
            ) {
                icon?.let {
                    Icon(
                        painter = it,
                        contentDescription = null,
                    )
                }
                Text(text = title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            }
            content()
        }
    }
}

@Composable
private fun CardItem(
    title: String,
    information: String,
    modifier: Modifier = Modifier,
    infoIcon: Painter? = null,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(Dimen.spaceMedium),
            verticalArrangement = Arrangement.spacedBy(Dimen.spaceSmall)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )

            Row {
                infoIcon?.let {
                    Icon(
                        painter = it,
                        contentDescription = null,
                    )
                    Spacer(modifier = Modifier.width(Dimen.spaceSmall))
                }
                Text(text = information)
            }
        }
    }
}

@Composable
private fun Note(
    title: String,
    content: String?,
    updatedContent: String,
    onValueChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier,
    inEditMode: Boolean = false
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = modifier
                .padding(Dimen.spaceMedium)
                .animateContentSize()
        ) {
            //Title adn Edit Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                )

                IconButton(onClick = onEditClick) {
                    when (inEditMode) {
                        true -> {
                            Icon(
                                painter = painterResource(R.drawable.ic_close),
                                contentDescription = stringResource(R.string.cancel),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                        false -> {
                            Icon(
                                painter = painterResource(R.drawable.ic_edit),
                                contentDescription = stringResource(R.string.edit),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Content Area
            if (inEditMode) {
                CustomOutlinedTextField(
                    value = updatedContent,
                    onValueChange = onValueChange,
                    label = { Text(stringResource(R.string.edit_note_content)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 100.dp)
                )

                Spacer(modifier = Modifier.height(Dimen.spaceMedium))

                ButtonWithIcon(
                    text = stringResource(R.string.save),
                    onClick = onSaveClick,
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_check),
                            contentDescription = stringResource(R.string.save)
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(
                    text = if (content.isNullOrEmpty()) stringResource(R.string.empty_note_placeholder)
                        else content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (content.isNullOrEmpty()) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun ProfilePic(uiState: PetProfileUiState) {
    val painterRes = if (uiState.pet.species == PetSpecies.CAT) R.drawable.ic_cat
        else R.drawable.ic_dog
    val image = uiState.pet.avatar?.let { remember { decodeBase64ToImage(it) } }
    val imageModifier = Modifier
        .size(Dimen.petImageProfile)
        .then(if (image != null) Modifier.clip(CircleShape) else Modifier)

    AsyncImage(
        model = image,
        contentDescription = stringResource(R.string.pet_image),
        contentScale = ContentScale.Crop,
        fallback = painterResource(painterRes),
        modifier = imageModifier
    )
}

@Composable
private fun GeneralInfo(uiState: PetProfileUiState) {
    Row {
        Text(
            text = uiState.pet.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        uiState.pet.gender.let { gender ->
            @DrawableRes val painterRes = when (gender) {
                Gender.MALE -> R.drawable.ic_male
                Gender.FEMALE -> R.drawable.ic_female
            }
            @StringRes val contentDescription = when (gender) {
                Gender.MALE -> R.string.male
                Gender.FEMALE -> R.string.female
            }
            Icon(
                painter = painterResource(painterRes),
                contentDescription = stringResource(contentDescription),
            )
        }
    }

    Text(
        text = uiState.pet.breed,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.alpha(0.7f)
    )

    Text(
        text = uiState.age,
        style = MaterialTheme.typography.titleSmall,
        modifier = Modifier.alpha(0.7f)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MedicationBottomSheet(
    onDismiss: () -> Unit,
    uiState: PetProfileUiState,
    onNameChange: (String) -> Unit,
    onDosageChange: (String) -> Unit,
    onFrequencyChange: (String) -> Unit,
    toggleStartDatePicker: () -> Unit,
    toggleEndDatePicker: () -> Unit,
    onNoteChange: (String) -> Unit,
    onSaveClick: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(
            //skipPartiallyExpanded = true
        ),
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.padding(Dimen.spaceMedium),
            verticalArrangement = Arrangement.spacedBy(Dimen.spaceMedium)
        ) {
            CustomOutlinedTextField(
                value = uiState.medicationName,
                onValueChange = onNameChange,
                label = { Text(stringResource(R.string.name)) },
                modifier = Modifier.fillMaxWidth()
            )
            CustomOutlinedTextField(
                value = uiState.medicationDosage,
                onValueChange = onDosageChange,
                label = { Text(stringResource(R.string.dosage)) },
                modifier = Modifier.fillMaxWidth()
            )
            CustomOutlinedTextField(
                value = uiState.medicationFrequency,
                onValueChange = onFrequencyChange,
                label = { Text(stringResource(R.string.frequency)) },
                modifier = Modifier.fillMaxWidth()
            )
            DatePickerField(
                value = uiState.medicationStartDate?.let {
                    SimpleDateFormat(
                        "dd MMMM yyyy",
                        Locale.getDefault()
                    ).format(it ) } ?: stringResource(R.string.tap_to_select_date),
                onClick = toggleStartDatePicker,
                label = stringResource(R.string.start_date)
            )
            DatePickerField(
                value = uiState.medicationEndDate?.let {
                    SimpleDateFormat(
                        "dd MMMM yyyy",
                        Locale.getDefault()
                    ).format(it ) } ?: stringResource(R.string.tap_to_select_date),
                onClick = toggleEndDatePicker,
                label = stringResource(R.string.end_date)
            )
            CustomOutlinedTextField(
                value = uiState.medicationNote,
                onValueChange = onNoteChange,
                label = { Text(stringResource(R.string.note)) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(Dimen.spaceMedium))
            ButtonWithIcon(
                text = stringResource(R.string.save),
                onClick = onSaveClick,
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