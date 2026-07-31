package com.example.petvitals.ui.screens.petprofile

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.example.petvitals.R
import com.example.petvitals.domain.getMedicationStatus
import com.example.petvitals.domain.models.Food
import com.example.petvitals.domain.models.Gender
import com.example.petvitals.domain.models.Medication
import com.example.petvitals.domain.models.MedicationStatus
import com.example.petvitals.domain.models.PermissionLevel
import com.example.petvitals.domain.models.Pet
import com.example.petvitals.domain.models.PetSpecies
import com.example.petvitals.domain.models.canDeletePet
import com.example.petvitals.domain.models.canManagePetCare
import com.example.petvitals.ui.components.CenterAlignedTopBar
import com.example.petvitals.ui.components.ConfirmationDialog
import com.example.petvitals.ui.components.CustomIconButton
import com.example.petvitals.ui.components.CustomSnackbarHost
import com.example.petvitals.ui.components.Loading
import com.example.petvitals.ui.components.ResetTopBarWhenNotScrollable
import com.example.petvitals.ui.components.SnackbarState
import com.example.petvitals.ui.components.rememberTopBarScrollBehavior
import com.example.petvitals.ui.components.showSnackbar
import com.example.petvitals.ui.navigation.AddEditFood
import com.example.petvitals.ui.navigation.AddEditMedication
import com.example.petvitals.ui.navigation.PetProfile
import com.example.petvitals.ui.theme.Dimen
import com.example.petvitals.ui.theme.LocalCustomColorsScheme
import com.example.petvitals.ui.theme.PetVitalsTheme
import com.example.petvitals.ui.utils.ObserveAsEvents
import com.example.petvitals.ui.utils.decodeBase64ToImage
import com.example.petvitals.ui.utils.formatDateToString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetProfileScreen(
    petProfile: PetProfile,
    onNavigateToPets: () -> Unit,
    onNavigateToEditPet: (String) -> Unit,
    onNavigateToSharePet: (String) -> Unit,
    onNavigateToAddEditMedication: (AddEditMedication) -> Unit,
    onNavigateToAddEditFood: (AddEditFood) -> Unit,
    viewModel: PetProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val resources = LocalResources.current

    LaunchedEffect(petProfile.petId) {
        viewModel.onAction(PetProfileAction.LoadPet(petProfile.petId))
    }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is PetProfileEvent.ShowSnackbar -> snackbarHostState.showSnackbar(
                SnackbarState(
                    message = resources.getString(event.messageRes),
                    snackbarType = event.snackbarType,
                    withDismissAction = event.withDismissAction
                )
            )
            PetProfileEvent.PetDeleted -> onNavigateToPets()
        }
    }

    PetProfileScreenContent(
        uiState = uiState,
        petId = petProfile.petId,
        onAction = viewModel::onAction,
        onNavigateToPets = onNavigateToPets,
        onNavigateToEditPet = onNavigateToEditPet,
        onNavigateToSharePet = onNavigateToSharePet,
        onNavigateToAddEditMedication = onNavigateToAddEditMedication,
        onNavigateToAddEditFood = onNavigateToAddEditFood,
        snackbarHostState = snackbarHostState
    )
}

@Composable
private fun PetProfileScreenContent(
    uiState: PetProfileUiState,
    petId: String,
    onAction: (PetProfileAction) -> Unit,
    onNavigateToPets: () -> Unit,
    onNavigateToEditPet: (String) -> Unit,
    onNavigateToSharePet: (String) -> Unit,
    onNavigateToAddEditMedication: (AddEditMedication) -> Unit,
    onNavigateToAddEditFood: (AddEditFood) -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    if (uiState.isLoading) {
        Loading()
        return
    }

    val scrollBehavior = rememberTopBarScrollBehavior()
    val contentScrollState = rememberScrollState()
    val loadErrorMessageRes = uiState.loadErrorMessageRes

    ResetTopBarWhenNotScrollable(
        scrollBehavior = scrollBehavior,
        canScrollBackward = contentScrollState.canScrollBackward,
        canScrollForward = contentScrollState.canScrollForward,
        contentVisible = loadErrorMessageRes == null
    )

    if (loadErrorMessageRes != null) {
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                PetProfileTopBar(
                    onNavigateToPets = onNavigateToPets,
                    scrollBehavior = scrollBehavior
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(Dimen.Screen.horizontalPadding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(loadErrorMessageRes),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
        return
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = {
            CustomSnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            PetProfileTopBar(
                onNavigateToPets = onNavigateToPets,
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = Dimen.Screen.horizontalPadding)
                .fillMaxSize()
                .imePadding()
                .verticalScroll(contentScrollState)
        ) {
            PetProfileHeader(uiState = uiState)

            ProfileActions(
                permissionLevel = uiState.permissionLevel,
                onEditPetClick = { onNavigateToEditPet(petId) },
                onShareAccessClick = { onNavigateToSharePet(petId) }
            )

            PetDetailsSection(uiState = uiState)

            SectionCard(
                title = stringResource(R.string.health),
                actionButton = if (uiState.permissionLevel.canManagePetCare) {
                    {
                        TextButton(onClick = { onNavigateToAddEditMedication(AddEditMedication(petId = petId)) }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_add),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )

                            Text(stringResource(R.string.add_medication))
                        }
                    }
                } else null
            ) {
                Note(
                    noteType = PetNoteType.HEALTH,
                    title = stringResource(R.string.health_note),
                    content = uiState.pet.healthNote,
                    editorState = uiState.noteEditor,
                    editableEmptyText = stringResource(R.string.add_health_note_prompt),
                    editorPlaceholder = stringResource(R.string.health_note_placeholder),
                    editorSupportingText = stringResource(R.string.health_note_supporting_text),
                    onValueChange = { onAction(PetProfileAction.OnNoteChange(it)) },
                    onEditClick = { onAction(PetProfileAction.EditNote(PetNoteType.HEALTH)) },
                    onCancelClick = { onAction(PetProfileAction.CancelNoteEdit) },
                    onSaveClick = { onAction(PetProfileAction.SaveNote) },
                    permissionLevel = uiState.permissionLevel
                )

                MedicationsList(
                    uiState = uiState,
                    onEditMedicationClick = { medication ->
                        onNavigateToAddEditMedication(
                            AddEditMedication(
                                petId = uiState.pet.id,
                                medicationId = medication.id
                            )
                        )
                    },
                    onDeleteMedicationClick = { onAction(PetProfileAction.DeleteMedication(it)) },
                )
            }

            SectionCard(
                title = stringResource(R.string.nutrition),
                actionButton = if (uiState.permissionLevel.canManagePetCare) {
                    {
                        TextButton(onClick = { onNavigateToAddEditFood(AddEditFood(petId = petId)) }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_add),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )

                            Text(stringResource(R.string.add_food))
                        }
                    }
                } else null
            ) {
                Note(
                    noteType = PetNoteType.FOOD,
                    title = stringResource(R.string.food_note),
                    content = uiState.pet.foodNote,
                    editorState = uiState.noteEditor,
                    editableEmptyText = stringResource(R.string.add_food_note_prompt),
                    editorPlaceholder = stringResource(R.string.food_note_placeholder),
                    editorSupportingText = stringResource(R.string.food_note_supporting_text),
                    onValueChange = { onAction(PetProfileAction.OnNoteChange(it)) },
                    onEditClick = { onAction(PetProfileAction.EditNote(PetNoteType.FOOD)) },
                    onCancelClick = { onAction(PetProfileAction.CancelNoteEdit) },
                    onSaveClick = { onAction(PetProfileAction.SaveNote) },
                    permissionLevel = uiState.permissionLevel
                )

                FoodList(
                    uiState = uiState,
                    onEditFoodClick = { food ->
                        onNavigateToAddEditFood(
                            AddEditFood(
                                petId = uiState.pet.id,
                                foodId = food.id
                            )
                        )
                    },
                    onDeleteFoodClick = { onAction(PetProfileAction.DeleteFood(it)) },
                )
            }

            if (uiState.permissionLevel.canDeletePet) {
                TextButton(
                    onClick = { onAction(PetProfileAction.ToggleDeleteModal) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.delete_pet),
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        if (uiState.showOnDeleteModal) {
            ConfirmationDialog(
                onDismissRequest = { onAction(PetProfileAction.ToggleDeleteModal) },
                onConfirmation = {
                    onAction(PetProfileAction.DeletePet(uiState.pet.id))
                },
                title = stringResource(R.string.delete_pet),
                text = stringResource(R.string.delete_pet_confirmation),
                confirmButtonText = stringResource(R.string.delete),
                dismissButtonText = stringResource(R.string.cancel),
                isConfirmButtonDestructive = true
            )
        }
    }
}

@Composable
private fun PetProfileTopBar(
    onNavigateToPets: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior
) {
    CenterAlignedTopBar(
        scrollBehavior = scrollBehavior,
        title = { Text(stringResource(R.string.pet_profile)) },
        navigationIcon = {
            CustomIconButton(
                onClick = onNavigateToPets,
                painter = painterResource(R.drawable.ic_arrow_back),
                contentDescription = stringResource(R.string.back)
            )
        }
    )
}

@Composable
private fun SectionCard(
    title: String,
    modifier: Modifier = Modifier,
    actionButton: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.weight(1f))

            actionButton?.invoke()
        }

        content()
    }
}

@Composable
private fun PetProfileHeader(
    uiState: PetProfileUiState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ProfilePic(
            image = uiState.pet.avatar?.let { remember { decodeBase64ToImage(it) } },
            petSpecies = uiState.pet.species
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = uiState.pet.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            uiState.age?.let {
                Text(
                    text = uiState.age,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        PermissionChip(permissionLevel = uiState.permissionLevel)
    }
}

@Composable
private fun PermissionChip(
    permissionLevel: PermissionLevel,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = 8.dp,
                vertical = 4.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_group),
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = permissionLevel.label(),
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
private fun ProfileActions(
    permissionLevel: PermissionLevel,
    onEditPetClick: () -> Unit,
    onShareAccessClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (permissionLevel != PermissionLevel.OWNER) return

    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Button(
            onClick = onEditPetClick,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_edit),
                contentDescription = null
            )

            Spacer(modifier = Modifier.width(4.dp))

            Text(stringResource(R.string.edit_pet))
        }

        FilledTonalButton(
            onClick = onShareAccessClick,
            modifier = modifier.weight(1f),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_share),
                contentDescription = null,
            )

            Spacer(modifier = Modifier.width(4.dp))

            Text(text = stringResource(R.string.share_access))
        }
    }
}

@Composable
private fun PetDetailsSection(
    uiState: PetProfileUiState,
    modifier: Modifier = Modifier
) {
    SectionCard(
        title = stringResource(R.string.details),
        modifier = modifier
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
        ) {
            Column(
                modifier  = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProfileDetailRow(
                    label = stringResource(R.string.species),
                    value = stringResource(uiState.pet.species.stringRes)
                )

                ProfileDetailRow(
                    label = stringResource(R.string.breed),
                    value = uiState.pet.breed?.takeIf { it.isNotBlank() }
                        ?: stringResource(R.string.unknown)
                )

                ProfileDetailRow(
                    label = stringResource(R.string.gender),
                    value = uiState.pet.gender?.label()
                        ?: stringResource(R.string.unknown)
                )

                ProfileDetailRow(
                    label = stringResource(R.string.date_of_birth),
                    value = uiState.dob
                        ?: stringResource(R.string.unknown)
                )
            }
        }
    }
}

@Composable
private fun ProfileDetailRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(0.42f),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
        )
        Text(
            text = value,
            modifier = Modifier.weight(0.58f),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun Gender.label(): String {
    return stringResource(
        when (this) {
            Gender.MALE -> R.string.male
            Gender.FEMALE -> R.string.female
        }
    )
}

@Composable
private fun PermissionLevel.label(): String {
    return stringResource(
        when (this) {
            PermissionLevel.OWNER -> R.string.permission_level_owner
            PermissionLevel.EDITOR -> R.string.permission_level_editor
            PermissionLevel.VIEWER -> R.string.permission_level_viewer
        }
    )
}

@Composable
private fun Note(
    noteType: PetNoteType,
    title: String,
    content: String?,
    editorState: NoteEditorState,
    editableEmptyText: String,
    editorPlaceholder: String,
    editorSupportingText: String,
    onValueChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier,
    permissionLevel: PermissionLevel
) {
    val isEditing = editorState.noteType == noteType
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(isEditing) {
        if (isEditing) focusRequester.requestFocus()
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = tween(
                    durationMillis = 200,
                    easing = LinearOutSlowInEasing
                )
            ),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    modifier = Modifier.size(36.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(R.drawable.ic_info),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    if (!isEditing) {
                        val hasContent = !content.isNullOrBlank()
                        Text(
                            text = when {
                                hasContent -> content
                                permissionLevel == PermissionLevel.VIEWER -> stringResource(
                                    R.string.empty_note_for_viewer
                                )
                                else -> editableEmptyText
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (hasContent) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }

                if (
                    permissionLevel != PermissionLevel.VIEWER &&
                    editorState.noteType == null
                ) {
                    IconButton(onClick = onEditClick) {
                        Icon(
                            painter = painterResource(R.drawable.ic_edit),
                            contentDescription = stringResource(R.string.edit_named_note, title),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (isEditing) {
                val errorMessage = editorState.errorMessageRes?.let { stringResource(it) }
                val supportingText = when {
                    errorMessage != null -> errorMessage
                    editorState.draft.isBlank() && !content.isNullOrBlank() -> stringResource(
                        R.string.empty_note_removal_supporting_text
                    )
                    else -> editorSupportingText
                }

                TextField(
                    value = editorState.draft,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    enabled = !editorState.isSaving,
                    label = { Text(title) },
                    placeholder = { Text(editorPlaceholder) },
                    supportingText = { Text(supportingText) },
                    isError = errorMessage != null,
                    minLines = 4,
                    maxLines = 8,
                    shape = MaterialTheme.shapes.large,
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onCancelClick,
                        enabled = !editorState.isSaving
                    ) {
                        Text(stringResource(R.string.cancel))
                    }

                    Button(
                        onClick = onSaveClick,
                        enabled = editorState.canSave
                    ) {
                        if (editorState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            stringResource(
                                if (editorState.isSaving) R.string.saving else R.string.save_note
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfilePic(image: ByteArray?, petSpecies: PetSpecies) {
    if (image == null) {
        Surface(
            modifier = Modifier.size(230.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(petSpecies.drawableRes),
                    contentDescription = null,
                    modifier = Modifier.size(200.dp)
                )
            }
        }
    } else {
        Surface(
            modifier = Modifier.size(220.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary
        ) {
            Box(contentAlignment = Alignment.Center) {
                AsyncImage(
                    model = image,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(200.dp)
                        .clip(RoundedCornerShape(100))
                )
            }
        }
    }

}

@Composable
fun MedicationsList(
    uiState: PetProfileUiState,
    onEditMedicationClick: (Medication) -> Unit,
    onDeleteMedicationClick: (Medication) -> Unit,
) {
    Column {
        for (medication in uiState.medications.sortedBy { it.startDate }) {
            key(medication.id) {
                MedicationCard(
                    medication = medication,
                    onEditClick = onEditMedicationClick,
                    onDeleteClick = onDeleteMedicationClick,
                    permissionLevel = uiState.permissionLevel,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
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
    val medicationStatus = getMedicationStatus(medication.startDate, medication.endDate)
    val hasDetails = medication.startDate != null ||
            medication.endDate != null ||
            medication.note.isNotBlank()
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = medication.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        MedicationStatusBadge(status = medicationStatus)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${medication.dosage} • ${medication.frequency}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                if (hasDetails) {
                    val rotationAngle by animateFloatAsState(
                        targetValue = if (isExpanded) 180f else 0f,
                        label = "MedicationDetailsArrow"
                    )
                    IconButton(onClick = { isExpanded = !isExpanded }) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = stringResource(
                                if (isExpanded) R.string.collapse else R.string.expand
                            ),
                            modifier = Modifier.rotate(rotationAngle)
                        )
                    }
                }

                if (permissionLevel.canManagePetCare) {
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
                HorizontalDivider()
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    medication.startDate?.let { startDate ->
                        DateInfoRow(
                            label = stringResource(R.string.start_date),
                            dateString = formatDateToString(startDate)
                        )
                    }
                    medication.endDate?.let { endDate ->
                        DateInfoRow(
                            label = stringResource(R.string.end_date),
                            dateString = formatDateToString(endDate)
                        )
                    }
                    if (medication.note.isNotBlank()) {
                        Text(
                            text = stringResource(R.string.note),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = medication.note,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MedicationStatusBadge(status: MedicationStatus) {

    val (text, color) = when (status) {
        MedicationStatus.ONGOING -> stringResource(R.string.ongoing) to MaterialTheme.colorScheme.primaryContainer
        MedicationStatus.SCHEDULED -> stringResource(R.string.scheduled) to MaterialTheme.colorScheme.secondaryContainer
        MedicationStatus.COMPLETED -> stringResource(R.string.completed) to LocalCustomColorsScheme.current.successContainer
        MedicationStatus.REGULAR -> stringResource(R.string.regularly) to MaterialTheme.colorScheme.tertiaryContainer
    }

    Surface(
        color = color,
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(4.dp)
        )
    }
}

@Composable
private fun DateInfoRow(label: String, dateString: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_date_range),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "$label: $dateString",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun FoodList(
    uiState: PetProfileUiState,
    onEditFoodClick: (Food) -> Unit,
    onDeleteFoodClick: (Food) -> Unit,
) {
    Column {
        for (food in uiState.food) {
            key(food.id) {
                FoodCard(
                    food = food,
                    onEditClick = onEditFoodClick,
                    onDeleteClick = onDeleteFoodClick,
                    permissionLevel = uiState.permissionLevel,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
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
    val hasDetails = food.note.isNotBlank()
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = food.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${food.portion} • ${food.frequency}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                if (hasDetails) {
                    val rotationAngle by animateFloatAsState(
                        targetValue = if (isExpanded) 180f else 0f,
                        label = "FoodDetailsArrow"
                    )
                    IconButton(onClick = { isExpanded = !isExpanded }) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = stringResource(
                                if (isExpanded) R.string.collapse else R.string.expand
                            ),
                            modifier = Modifier.rotate(rotationAngle)
                        )
                    }
                }

                if (permissionLevel.canManagePetCare) {
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

            if (isExpanded) {
                HorizontalDivider()
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.note),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = food.note,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@PreviewLightDark
@Preview(heightDp = 1200)
@Composable
fun PetProfileContentViewerPreview() {
    PetVitalsTheme {
        PetProfileScreenContent(
            uiState = PetProfileUiState(
                pet = Pet(
                    name = "PetName",
                    breed = "Breed",
                    gender = Gender.MALE,
                    species = PetSpecies.CAT,
                    dobYear = 2017,
                    dobMonth = 12,
                    dobDay = 12,
                    avatar = null,
                    healthNote = "Health note: Lorum ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
                    foodNote = "Food note: Lorum ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
                    currentUserPermission = PermissionLevel.VIEWER,
                ),
                dob = "12.12.2012",
                age = "12 years old",
                permissionLevel = PermissionLevel.VIEWER
            ),
            petId = "",
            onAction = {},
            onNavigateToPets = {},
            onNavigateToEditPet = {},
            onNavigateToSharePet = {},
            onNavigateToAddEditMedication = {},
            onNavigateToAddEditFood = {},
            snackbarHostState = SnackbarHostState()
        )
    }
}

@Preview(heightDp = 1200)
@Composable
fun PetProfileContentEditorPreview() {
    PetVitalsTheme {
        PetProfileScreenContent(
            uiState = PetProfileUiState(
                pet = Pet(
                    name = "PetName",
                    breed = "Breed",
                    gender = Gender.MALE,
                    species = PetSpecies.CAT,
                    dobYear = 2017,
                    dobMonth = 12,
                    dobDay = 12,
                    avatar = null,
                    healthNote = "Health note: Lorum ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
                    foodNote = "Food note: Lorum ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
                    currentUserPermission = PermissionLevel.EDITOR,
                ),
                dob = "12.12.2012",
                age = "12 years old",
                permissionLevel = PermissionLevel.EDITOR,
                medications = listOf(
                    Medication(
                        name = "Medication",
                        dosage = "Dosage",
                        frequency = "Frequency"
                    )
                )
            ),
            petId = "",
            onAction = {},
            onNavigateToPets = {},
            onNavigateToEditPet = {},
            onNavigateToSharePet = {},
            onNavigateToAddEditMedication = {},
            onNavigateToAddEditFood = {},
            snackbarHostState = SnackbarHostState()
        )
    }
}

@Preview(heightDp = 1200)
@Composable
fun PetProfileContentOwnerPreview() {
    PetVitalsTheme {
        PetProfileScreenContent(
            uiState = PetProfileUiState(
                pet = Pet(
                    name = "PetName",
                    breed = "Breed",
                    gender = Gender.MALE,
                    species = PetSpecies.CAT,
                    dobYear = 2017,
                    dobMonth = 12,
                    dobDay = 12,
                    avatar = null,
                    healthNote = "Health note: Lorum ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
                    foodNote = "Food note: Lorum ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
                    currentUserPermission = PermissionLevel.OWNER,
                ),
                dob = "12.12.2012",
                age = "12 years old",
                permissionLevel = PermissionLevel.OWNER
            ),
            petId = "",
            onAction = {},
            onNavigateToPets = {},
            onNavigateToEditPet = {},
            onNavigateToSharePet = {},
            onNavigateToAddEditMedication = {},
            onNavigateToAddEditFood = {},
            snackbarHostState = SnackbarHostState()
        )
    }
}
