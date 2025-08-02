package com.example.petvitals.ui.screens.records

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.example.petvitals.R
import com.example.petvitals.data.repository.pet.Pet
import com.example.petvitals.data.repository.pet.PetSpecies
import com.example.petvitals.data.repository.pet_permission.PermissionLevel
import com.example.petvitals.data.repository.record.Record
import com.example.petvitals.data.repository.record.RecordType
import com.example.petvitals.ui.components.ScreenLayout
import com.example.petvitals.ui.components.TopBar
import com.example.petvitals.ui.theme.Dimen
import com.example.petvitals.utils.decodeBase64ToImage
import com.example.petvitals.utils.formatDateToStringLocale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordsScreen(
    onNavigateToAddEditRecord: (String?) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToPetProfile: (String) -> Unit,
    viewModel: RecordsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    ScreenLayout(
        topBar = {
            val keyboardController = LocalSoftwareKeyboardController.current
            val focusManager = LocalFocusManager.current

            RecordsTopAppBar(
                searchQuery = uiState.searchQuery,
                onSearchQueryChange = viewModel::onSearchQueryChange,
                onSearchTriggered = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                },
                isSelectionMode = uiState.selectionMode,
                onDeleteClick = viewModel::deleteSelectedRecords,
                onAddClick = { onNavigateToAddEditRecord(null) },
                onNavigateToProfile = onNavigateToProfile
            )
        }
    ) {
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { viewModel.getRecords() }
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = Dimen.spaceMedium),
                verticalArrangement = Arrangement.spacedBy(Dimen.spaceMedium)
            ) {
                item {
                    FilterBar(
                        allPets = uiState.allPetsForFiltering,
                        allTypes = uiState.allRecordTypesForFiltering,
                        selectedPetIds = uiState.selectedPetFilters,
                        selectedTypes = uiState.selectedTypeFilters,
                        onPetChipClicked = viewModel::onPetFilterChipClick,
                        onTypeChipClicked = viewModel::onTypeFilterChipClicked
                    )

                    HorizontalDivider(modifier = Modifier.padding(top = Dimen.spaceSmall))
                }

                items(
                    items = uiState.displayedRecords,
                    key = { entry ->
                        when (entry) {
                            is RecordsListEntry.Header -> "header-${entry.dateString}"
                            is RecordsListEntry.RecordItem -> entry.recordWithPets.record.id
                        }
                    }
                ) { entry ->

                    when (entry) {
                        is RecordsListEntry.Header -> {
                            Text(
                                text = entry.dateString,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        horizontal = Dimen.spaceMedium,
                                        vertical = Dimen.spaceSmall
                                    )
                            )
                        }
                        is RecordsListEntry.RecordItem -> {
                            val recordWithPets = entry.recordWithPets
                            RecordCard(
                                recordWithPets = recordWithPets,
                                selected = uiState.selectedRecords.contains(recordWithPets.record),
                                onEditClick = { record -> onNavigateToAddEditRecord(record.id) },
                                onDeleteClick = viewModel::deleteRecord,
                                onPetChipClick = { pet -> onNavigateToPetProfile(pet.id) },
                                modifier = Modifier
                                    .pointerInput(recordWithPets) {
                                        this.detectTapGestures(
                                            onLongPress = {
                                                if (!uiState.selectionMode) {
                                                    viewModel.selectRecord(recordWithPets.record)
                                                }
                                            },
                                            onTap = {
                                                if (uiState.selectionMode) {
                                                    viewModel.selectRecord(recordWithPets.record)
                                                }
                                            }
                                        )
                                    }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecordCard(
    recordWithPets: RecordWithPets,
    onEditClick: (Record) -> Unit,
    onDeleteClick: (Record) -> Unit,
    onPetChipClick: (Pet) -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false
) {
    val record = recordWithPets.record
    val pets = recordWithPets.pets

    OutlinedCard(
        modifier = modifier
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            ),
        border = when (selected) {
            true -> BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
            else -> BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        },
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                else MaterialTheme.colorScheme.surface
        )
    ) {
        Column {
            var isExpanded by remember { mutableStateOf(false) }

            //Title, type, more options, expand
            RecordCardMainContent(
                record = record,
                onEditClick = onEditClick,
                onDeleteClick = onDeleteClick,
                isExpanded = isExpanded,
                onExpandClick = { isExpanded = !isExpanded },
                modifier = Modifier
            )

            //Date, pets, description
            ExpandableContent(
                pets = pets,
                record = record,
                isExpanded = isExpanded,
                onPetChipClick = { pet -> onPetChipClick(pet) },
                modifier = Modifier
            )
        }
    }
}

@Composable
private fun RecordCardMainContent(
    record: Record,
    onEditClick: (Record) -> Unit,
    onDeleteClick: (Record) -> Unit,
    isExpanded: Boolean,
    onExpandClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(Dimen.spaceMedium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RecordMainInfo(
            record = record,
            modifier = Modifier.weight(1f)
        )

        ActionButtons(
            isExpanded = isExpanded,
            onExpandClick = onExpandClick,
            onEditClick = { onEditClick(record) },
            onDeleteClick = { onDeleteClick(record) },
            permissionLevel = record.currentUserPermission
        )
    }
}

@Composable
private fun RecordMainInfo(
    record: Record,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimen.spaceMedium)
    ) {
        //Indicator Icon
        Icon(
            painter = painterResource(id = record.type.iconResId),
            contentDescription = stringResource(record.type.titleResId),
            tint = record.type.color,
            modifier = Modifier
                .size(32.dp)
                .background(record.type.color.copy(alpha = 0.15f), CircleShape)
                .padding(Dimen.spaceSmall)
        )

        //Title, Type
        Column {
            Text(
                text = record.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stringResource(record.type.titleResId),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ActionButtons(
    permissionLevel: PermissionLevel,
    isExpanded: Boolean,
    onExpandClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        //More option DropDown
        Box {
            if (permissionLevel != PermissionLevel.VIEWER) {

                var showMenu by remember { mutableStateOf(false) }
                //more options
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Rounded.MoreVert,
                        contentDescription = stringResource(R.string.more_options)
                    )
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    //Dropdown items for Edit and Delete
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.edit)) },
                        onClick = {
                            showMenu = false
                            onEditClick()
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
                            onDeleteClick()
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
        //expand
        val rotationAngle by animateFloatAsState(
            targetValue = if (isExpanded) 180f else 0f,
            label = "ArrowRotation"
        )
        IconButton(onClick = onExpandClick) {
            Icon(
                Icons.Rounded.KeyboardArrowUp,
                stringResource(R.string.expand),
                modifier = Modifier.rotate(rotationAngle)
            )
        }
    }
}

@Composable
private fun ExpandableContent(
    record: Record,
    pets: List<Pet>,
    isExpanded: Boolean,
    onPetChipClick: (Pet) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isExpanded,
        enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
        exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top)
    ) {
        Column(
            modifier = modifier
                .padding(
                    start = Dimen.spaceMedium,
                    end = Dimen.spaceMedium,
                    bottom = Dimen.spaceMedium
                ),
            verticalArrangement = Arrangement.spacedBy(Dimen.spaceMedium)
        ) {
            HorizontalDivider()

            //Date
            Text(
                text = formatDateToStringLocale(record.date, "dd MMMM yyyy, HH:mm"),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )

            //Attached pets
            if (pets.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Dimen.spaceSmall),
                    verticalArrangement = Arrangement.spacedBy(Dimen.spaceSmall)
                ) {
                    pets.forEach { pet ->
                        PetChip(pet = pet, onClick = { onPetChipClick(pet) })
                    }
                }
            }

            //Description
            if (record.description.isNotBlank()) {
                Text(
                    text = record.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun PetChip(
    pet: Pet,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val fallbackRes = when (pet.species) {
        PetSpecies.DOG -> R.drawable.ic_dog
        PetSpecies.CAT -> R.drawable.ic_cat
    }
    val image = pet.avatar?.let { decodeBase64ToImage(it) }

    AssistChip(
        modifier = modifier,
        onClick = onClick,
        label = { Text(pet.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        leadingIcon = {
            AsyncImage(
                model = image,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                fallback = painterResource(fallbackRes),
                modifier = Modifier
                    .size(AssistChipDefaults.IconSize)
                    .then(if (image != null) Modifier.clip(CircleShape) else Modifier)
            )
        },
        border = AssistChipDefaults.assistChipBorder(
            enabled = true,
            borderColor = MaterialTheme.colorScheme.outline
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchAppBarField(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearchTriggered: () -> Unit,
    placeholderText: String,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        textStyle = MaterialTheme.typography.titleMedium,
        placeholder = { Text(placeholderText) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = stringResource(R.string.search)
            )
        },
        trailingIcon = {
            AnimatedVisibility(visible = query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = stringResource(R.string.clear_search)
                    )
                }
            }
        },
        singleLine = true,
        shape = MaterialTheme.shapes.extraLarge,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(
            onSearch = {
                onSearchTriggered()
                keyboardController?.hide()
                focusManager.clearFocus()
            }
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordsTopAppBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearchTriggered: () -> Unit,
    isSelectionMode: Boolean,
    onDeleteClick: () -> Unit,
    onAddClick: () -> Unit,
    onNavigateToProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopBar(
        modifier = modifier,
        title = {
            SearchAppBarField(
                query = searchQuery,
                onQueryChange = onSearchQueryChange,
                onSearchTriggered = onSearchTriggered,
                placeholderText = stringResource(R.string.search),
                modifier = Modifier.padding(vertical = Dimen.spaceSmall)
            )
        },
        navigationIcon = {
            IconButton(
                onClick = onNavigateToProfile
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_person),
                    contentDescription = stringResource(R.string.profile)
                )
            }
        },
        actions = {
            AnimatedContent(
                targetState = isSelectionMode,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(220, delayMillis = 90)) +
                            slideInVertically(initialOffsetY = { it / 2 }))
                        .togetherWith(
                            fadeOut(animationSpec = tween(90)) +
                                    slideOutVertically(targetOffsetY = { -it / 2 })
                        )
                },
                label = "TopBarActions"
            ) { inSelectionMode ->
                if (inSelectionMode) {
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            painter = painterResource(R.drawable.ic_delete_forever),
                            contentDescription = stringResource(R.string.delete),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                } else {
                    IconButton(onClick = onAddClick) {
                        Icon(
                            painter = painterResource(R.drawable.ic_note_add),
                            contentDescription = stringResource(R.string.create_record)
                        )
                    }
                }
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBar(
    allPets: List<Pet>,
    allTypes: List<RecordType>,
    selectedPetIds: Set<String>,
    selectedTypes: Set<RecordType>,
    onPetChipClicked: (String) -> Unit,
    onTypeChipClicked: (RecordType) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Dimen.spaceSmall)
    ) {
        // --- Pet Filters ---
        if (allPets.isNotEmpty()) {
            Text(
                text = stringResource(R.string.filter_by_pet),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(Dimen.spaceSmall)
            ) {
                items(allPets, key = { it.id }) { pet ->
                    val isSelected = selectedPetIds.contains(pet.id)
                    FilterChip(
                        selected = isSelected,
                        onClick = { onPetChipClicked(pet.id) },
                        label = { Text(pet.name) },
                        leadingIcon = {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Done,
                                    contentDescription = stringResource(R.string.selected_option),
                                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                                )
                            }
                        }
                    )
                }
            }
        }

        // --- Record Type Filters ---
        Text(
            text = stringResource(R.string.filter_by_type),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(Dimen.spaceSmall)
        ) {
            items(allTypes, key = { it.name }) { type ->
                val isSelected = selectedTypes.contains(type)
                FilterChip(
                    selected = isSelected,
                    onClick = { onTypeChipClicked(type) },
                    label = { Text(stringResource(id = type.titleResId)) },
                    leadingIcon = {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Done,
                                contentDescription = stringResource(R.string.selected_option),
                                modifier = Modifier.size(FilterChipDefaults.IconSize)
                            )
                        } else {
                            Icon(
                                painter = painterResource(id = type.iconResId),
                                contentDescription = null,
                                modifier = Modifier.size(FilterChipDefaults.IconSize),
                                tint = type.color
                            )
                        }
                    }
                )
            }
        }
    }
}