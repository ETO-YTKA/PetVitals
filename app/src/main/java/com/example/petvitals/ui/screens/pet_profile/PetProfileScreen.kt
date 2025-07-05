package com.example.petvitals.ui.screens.pet_profile

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.example.petvitals.AddEditMedication
import com.example.petvitals.PetProfile
import com.example.petvitals.R
import com.example.petvitals.data.repository.food.Food
import com.example.petvitals.data.repository.medication.Medication
import com.example.petvitals.data.repository.medication.MedicationStatus
import com.example.petvitals.data.repository.pet.Gender
import com.example.petvitals.data.repository.pet_permission.PermissionLevel
import com.example.petvitals.ui.components.ButtonWithIcon
import com.example.petvitals.ui.components.ConfirmationDialog
import com.example.petvitals.ui.components.CustomIconButton
import com.example.petvitals.ui.components.CustomOutlinedTextField
import com.example.petvitals.ui.components.Loading
import com.example.petvitals.ui.components.ScreenLayout
import com.example.petvitals.ui.components.TopBar
import com.example.petvitals.ui.theme.Dimen
import com.example.petvitals.utils.decodeBase64ToImage
import com.example.petvitals.utils.formatDateToString
import com.example.petvitals.utils.getMedicationStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetProfileScreen(
    petProfile: PetProfile,
    onNavigateToPets: () -> Unit,
    onNavigateToEditPet: (String) -> Unit,
    onNavigateToSharePet: (String) -> Unit,
    onNavigateToAddEditMedication: (AddEditMedication) -> Unit,
    onNavigateToAddEditFood: (String) -> Unit,
    viewModel: PetProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.getPetData(petProfile.petId)
    }

    ScreenLayout(
        horizontalAlignment = Alignment.Start,
        columnModifier = Modifier
            .padding(vertical = Dimen.spaceMedium)
            .fillMaxSize()
            .then(if (uiState.isLoading) Modifier else Modifier.verticalScroll(rememberScrollState())),
        verticalArrangement = Arrangement.spacedBy(Dimen.spaceMedium),
        topBar = {
            TopBar(
                title = { Text(stringResource(R.string.pet_profile)) },
                navigationIcon = {
                    CustomIconButton(
                        onClick = onNavigateToPets,
                        painter = painterResource(R.drawable.ic_arrow_back),
                        contentDescription = stringResource(R.string.back)
                    )
                },
                actions = {
                    if (uiState.permissionLevel != PermissionLevel.OWNER) return@TopBar

                    var isExpanded by remember { mutableStateOf(false) }
                    Box {
                        IconButton(
                            onClick = { isExpanded = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = stringResource(R.string.more_options)
                            )
                        }
                        DropdownMenu(
                            expanded = isExpanded,
                            onDismissRequest = { isExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.share)) },
                                onClick = {
                                    isExpanded = false
                                    onNavigateToSharePet(petProfile.petId)
                                },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_share),
                                        contentDescription = null
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.edit)) },
                                onClick = {
                                    isExpanded = false
                                    onNavigateToEditPet(petProfile.petId)
                                },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_edit),
                                        contentDescription = null
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.delete)) },
                                onClick = {
                                    isExpanded = false
                                    viewModel.toggleOnDeleteModal()
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Delete,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                },
                            )
                        }
                    }
                }
            )
        }
    ) {
        if (uiState.isLoading) {
            Loading()
        } else {

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
                    inEditMode = uiState.isHealthNoteInEditMode,
                    permissionLevel = uiState.permissionLevel
                )

                MedicationsList(
                    uiState = uiState,
                    onEditMedicationClick = { medication ->
                        onNavigateToAddEditMedication(AddEditMedication(
                            petId = uiState.pet.id,
                            medicationId = medication.id
                        )
                    ) },
                    onDeleteMedicationClick = viewModel::onDeleteMedicationClick,
                    navigateToAddMedication = { onNavigateToAddEditMedication(AddEditMedication(petId = uiState.pet.id)) }
                )
            }

            SectionCard(
                title = stringResource(R.string.nutrition),
                icon = painterResource(R.drawable.ic_pet_supplies)
            ) {
                Note(
                    title = stringResource(R.string.note),
                    content = uiState.pet.foodNote,
                    updatedContent = uiState.updatedFoodNote,
                    onValueChange = viewModel::onUpdatedFoodNoteChange,
                    onEditClick = viewModel::toggleFoodNoteEditMode,
                    onSaveClick = viewModel::onSaveFoodNoteClick,
                    inEditMode = uiState.isFoodNoteInEditMode,
                    permissionLevel = uiState.permissionLevel
                )

                FoodList(
                    uiState = uiState,
                    onEditFoodClick = viewModel::onEditFoodClick,
                    onDeleteFoodClick = viewModel::onDeleteFoodClick,
                    toggleFoodModal = viewModel::toggleFoodModal
                )
            }
        }
    }

    if (uiState.showAddFoodModal) {
        AddFoodBottomSheet(
            onDismiss = viewModel::toggleFoodModal,
            onFoodNameChange = viewModel::onFoodNameChange,
            onFoodPortionChange = viewModel::onFoodPortionChange,
            onFoodFrequencyChange = viewModel::onFoodFrequencyChange,
            onFoodNoteChange = viewModel::onFoodNoteChange,
            onSaveClick = viewModel::onSaveFoodClick,
            uiState = uiState
        )
    }

    if (uiState.showOnDeleteModal) {
        ConfirmationDialog(
            onDismissRequest = viewModel::toggleOnDeleteModal,
            onConfirmation = {
                viewModel.deletePet(uiState.pet.id)
                onNavigateToPets()
            },
            title = stringResource(R.string.delete_pet),
            text = stringResource(R.string.delete_pet_confirmation),
            confirmButtonText = stringResource(R.string.delete),
            dismissButtonText = stringResource(R.string.cancel),
            isConfirmButtonDestructive = true
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
    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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
    inEditMode: Boolean = false,
    permissionLevel: PermissionLevel
) {
    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = modifier
                .padding(Dimen.spaceMedium)
                .animateContentSize()
        ) {
            //Title and Edit Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                )

                if (permissionLevel != PermissionLevel.VIEWER) {
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
                val text = when {
                    permissionLevel == PermissionLevel.VIEWER -> stringResource(R.string.empty_note_for_viewer)
                    content.isNullOrEmpty() -> stringResource(R.string.empty_note_placeholder_for_editor)
                    else -> content
                }
                val color = when {
                    content.isNullOrEmpty() -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    else -> MaterialTheme.colorScheme.onSurface
                }

                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = color
                )
            }
        }
    }
}

@Composable
private fun ProfilePic(uiState: PetProfileUiState) {
    val fallBackRes = uiState.pet.species.drawableRes
    val image = uiState.pet.avatar?.let { remember { decodeBase64ToImage(it) } }
    val imageModifier = Modifier
        .size(Dimen.petImageProfile)
        .then(if (image != null) Modifier.clip(CircleShape) else Modifier)

    AsyncImage(
        model = image,
        contentDescription = stringResource(R.string.pet_image),
        contentScale = ContentScale.Crop,
        fallback = painterResource(fallBackRes),
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

    if (uiState.pet.breed.isNotBlank()) {
        Text(
            text = uiState.pet.breed,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.alpha(0.7f)
        )
    }

    Text(
        text = uiState.age,
        style = MaterialTheme.typography.titleSmall,
        modifier = Modifier.alpha(0.7f)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFoodBottomSheet(
    uiState: PetProfileUiState,
    onDismiss: () -> Unit,
    onFoodNameChange: (String) -> Unit,
    onFoodPortionChange: (String) -> Unit,
    onFoodFrequencyChange: (String) -> Unit,
    onFoodNoteChange: (String) -> Unit,
    onSaveClick: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        ),
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .padding(Dimen.spaceMedium)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Dimen.spaceMedium)
        ) {
            val title = stringResource(
                when (uiState.foodId) {
                    null -> R.string.add_food
                    else -> R.string.edit_food
                }
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            CustomOutlinedTextField(
                value = uiState.foodName,
                onValueChange = onFoodNameChange,
                label = { Text(stringResource(R.string.food_name)) },
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.isFoodNameError,
                supportingText = {
                    if (uiState.isFoodNameError) {
                        Text(uiState.foodNameErrorMessage ?: "")
                    }
                }
            )

            CustomOutlinedTextField(
                value = uiState.foodPortion,
                onValueChange = onFoodPortionChange,
                label = { Text(stringResource(R.string.portion)) },
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.isFoodPortionError,
                supportingText = {
                    if (uiState.isFoodPortionError) {
                        Text(uiState.foodPortionErrorMessage ?: "")
                    }
                }
            )

            CustomOutlinedTextField(
                value = uiState.foodFrequency,
                onValueChange = onFoodFrequencyChange,
                label = { Text(stringResource(R.string.frequency)) },
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.isFoodFrequencyError,
                supportingText = {
                    if (uiState.isFoodFrequencyError) {
                        Text(uiState.foodFrequencyErrorMessage ?: "")
                    }
                }
            )

            CustomOutlinedTextField(
                value = uiState.foodNote,
                onValueChange = onFoodNoteChange,
                label = { Text(stringResource(R.string.note)) },
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.isFoodNoteError,
                supportingText = {
                    if (uiState.isFoodNoteError) {
                        Text(uiState.foodNoteErrorMessage ?: "")
                    }
                }
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


@Composable
private fun MedicationCard(
    medication: Medication,
    onEditClick: (Medication) -> Unit,
    onDeleteClick: (Medication) -> Unit,
    permissionLevel: PermissionLevel,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    val status = remember(medication.startDate, medication.endDate) {
        getMedicationStatus(medication.startDate, medication.endDate)
    }

    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            //Header
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left side: Medication Info and Status
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = medication.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${medication.dosage} - ${medication.frequency}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    MedicationStatusIndicator(status = status)
                }

                // Right side

                //Expand/Collapse Icon
                if (medication.startDate != null || medication.endDate != null || medication.note.isNotEmpty()) {
                    val rotationAngle by animateFloatAsState(
                        targetValue = if (isExpanded) 180f else 0f,
                        label = "CardArrowRotation"
                    )
                    IconButton(onClick = { isExpanded = !isExpanded }) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = if (isExpanded) stringResource(R.string.collapse) else stringResource(
                                R.string.expand
                            ),
                            modifier = Modifier.rotate(rotationAngle)
                        )
                    }
                }

                //More options
                if (permissionLevel != PermissionLevel.VIEWER) {
                    var showMenu by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = stringResource(R.string.more_options)
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.edit)) },
                                onClick = {
                                    showMenu = false
                                    onEditClick(medication)
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Outlined.Edit,
                                        contentDescription = null
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.delete)) },
                                onClick = {
                                    showMenu = false
                                    onDeleteClick(medication)
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Outlined.Delete,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            )
                        }
                    }
                }
            }

            if (isExpanded) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = DividerDefaults.Thickness,
                    color = DividerDefaults.color
                )

                //Dates
                if (medication.startDate != null || medication.endDate != null) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        medication.startDate?.let {
                            DateInfoRow(
                                label = stringResource(R.string.start_date),
                                dateString = formatDateToString(it)
                            )
                        }
                        medication.endDate?.let {
                            DateInfoRow(
                                label = stringResource(R.string.end_date),
                                dateString = formatDateToString(it)
                            )
                        }
                    }
                }

                //Note
                if (medication.note.isNotBlank()) {
                    if (medication.startDate != null || medication.endDate != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    Text(
                        text = medication.note,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun FoodCard(
    food: Food,
    onEditClick: (Food) -> Unit,
    onDeleteClick: (Food) -> Unit,
    permissionLevel: PermissionLevel,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            //Header
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                //Left side: Food Info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = food.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    //Display Portion and Frequency
                    Text(
                        text = "${food.portion} - ${food.frequency}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                //Right side: action icons

                // Expand/Collapse Icon Button
                if (food.note.isNotBlank()) {
                    val rotationAngle by animateFloatAsState(
                        targetValue = if (isExpanded) 180f else 0f,
                        label = "FoodCardArrowRotation"
                    )
                    IconButton(onClick = { isExpanded = !isExpanded }) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = if (isExpanded) stringResource(R.string.collapse) else stringResource(
                                R.string.expand
                            ),
                            modifier = Modifier.rotate(rotationAngle)
                        )
                    }
                }

                // More Options Menu
                if (permissionLevel != PermissionLevel.VIEWER) {
                    var showMenu by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = stringResource(R.string.more_options)
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.edit)) },
                                onClick = {
                                    showMenu = false
                                    onEditClick(food)
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Outlined.Edit,
                                        contentDescription = null
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.delete)) },
                                onClick = {
                                    showMenu = false
                                    onDeleteClick(food)
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Outlined.Delete,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // --- EXPANDED CONTENT (NOTE) ---
            if (isExpanded) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = DividerDefaults.Thickness,
                    color = DividerDefaults.color
                )
                Text(
                    text = food.note,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun MedicationStatusIndicator(status: MedicationStatus) {
    val (text, color) = when (status) {
        MedicationStatus.ONGOING -> stringResource(R.string.ongoing) to MaterialTheme.colorScheme.primary
        MedicationStatus.SCHEDULED -> stringResource(R.string.scheduled) to MaterialTheme.colorScheme.secondary
        MedicationStatus.COMPLETED -> stringResource(R.string.completed) to MaterialTheme.colorScheme.onSurfaceVariant
        MedicationStatus.REGULAR -> stringResource(R.string.regularly) to MaterialTheme.colorScheme.tertiary
    }

    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = color,
        modifier = Modifier.padding(top = 4.dp)
    )
}

@Composable
private fun DateInfoRow(label: String, dateString: String) {

    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(R.drawable.ic_date_range),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$label: $dateString",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun MedicationsList(
    uiState: PetProfileUiState,
    onEditMedicationClick: (Medication) -> Unit,
    onDeleteMedicationClick: (Medication) -> Unit,
    navigateToAddMedication: () -> Unit
) {
    OutlinedCard(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(Dimen.spaceMedium)
        ) {
            var isExpanded by remember { mutableStateOf(false) }

            TitleWithExpandButton(
                title = stringResource(R.string.medications),
                isExpanded = isExpanded,
                onClick = { isExpanded = !isExpanded }
            )

            AnimatedContent(
                targetState = isExpanded,
            ) { isExpanded ->
                if (isExpanded) {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 300.dp)
                    ) {
                        items(
                            items = uiState.medications,
                            key = { it.id }
                        ) { medication ->
                            MedicationCard(
                                medication = medication,
                                onEditClick = onEditMedicationClick,
                                onDeleteClick = onDeleteMedicationClick,
                                permissionLevel = uiState.permissionLevel,
                                modifier = Modifier.padding(vertical = Dimen.spaceMedium)
                            )
                        }

                        if (uiState.permissionLevel != PermissionLevel.VIEWER) {
                            item {
                                ButtonWithIcon(
                                    text = stringResource(R.string.add_medication),
                                    onClick = navigateToAddMedication,
                                    icon = {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_add),
                                            contentDescription = null
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FoodList(
    uiState: PetProfileUiState,
    onEditFoodClick: (Food) -> Unit,
    onDeleteFoodClick: (Food) -> Unit,
    toggleFoodModal: () -> Unit
) {
    OutlinedCard(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(Dimen.spaceMedium)
        ) {
            var isExpanded by remember { mutableStateOf(false) }

            TitleWithExpandButton(
                title = stringResource(R.string.food),
                isExpanded = isExpanded,
                onClick = { isExpanded = !isExpanded }
            )

            AnimatedContent(
                targetState = isExpanded
            ) { isExpanded ->
                if (isExpanded) {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 300.dp)
                    ) {
                        items(
                            items = uiState.food,
                            key = { it.id }
                        ) { food ->
                            FoodCard(
                                food = food,
                                onEditClick = onEditFoodClick,
                                onDeleteClick = onDeleteFoodClick,
                                permissionLevel = uiState.permissionLevel,
                                modifier = Modifier.padding(vertical = Dimen.spaceMedium)
                            )
                        }

                        if (uiState.permissionLevel != PermissionLevel.VIEWER) {
                            item {
                                ButtonWithIcon(
                                    text = stringResource(R.string.add_food),
                                    onClick = toggleFoodModal,
                                    icon = {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_add),
                                            contentDescription = null
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TitleWithExpandButton(
    title: String,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    Row {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(vertical = Dimen.spaceMedium)
                .weight(1f)
        )

        val rotationAngle by animateFloatAsState(
            targetValue = if (isExpanded) 180f else 0f,
            label = "CardArrowRotation"
        )
        IconButton(
            onClick = onClick
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = stringResource(if (isExpanded) R.string.collapse else R.string.expand),
                modifier = Modifier.rotate(rotationAngle)
            )
        }
    }
}