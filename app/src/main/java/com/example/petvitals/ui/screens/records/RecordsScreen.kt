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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.example.petvitals.ui.components.ScreenLayout
import com.example.petvitals.ui.components.TopBar
import com.example.petvitals.ui.theme.Dimen
import com.example.petvitals.utils.decodeBase64ToImage
import com.example.petvitals.utils.formatDateToStringLocale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordsScreen(
    onNavigateToAddEditRecord: (String?) -> Unit,
    viewModel: RecordsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    ScreenLayout(
        topBar = {
            RecordsTopAppBar(
                searchQuery = uiState.searchQuery,
                onSearchQueryChange = viewModel::onSearchQueryChange,
                onSearchTriggered = viewModel::getRecords,
                isSelectionMode = uiState.selectionMode,
                onDeleteClick = viewModel::deleteSelectedRecords,
                onAddClick = { onNavigateToAddEditRecord(null) }
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
                items(
                    items = uiState.recordsWithPets,
                    key = { it.record.id }
                ) { recordWithPets ->

                    RecordCard(
                        recordWithPets = recordWithPets,
                        selected = uiState.selectedRecords.contains(recordWithPets.record),
                        onEditClick = { record -> onNavigateToAddEditRecord(record.id) },
                        onDeleteClick = viewModel::deleteRecord,
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

@Composable
private fun RecordCard(
    recordWithPets: RecordWithPets,
    onEditClick: (Record) -> Unit,
    onDeleteClick: (Record) -> Unit,
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
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        ),
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Column {
            var isExpanded by remember { mutableStateOf(false) }

            //Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = Dimen.spaceMedium,
                        end = Dimen.spaceSmall,
                        top = Dimen.spaceMedium,
                        bottom = Dimen.spaceSmall
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                //Indicator and Title/Info Column
                Row(
                    modifier = Modifier.weight(1f),
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

                    //Title, Type, and Date
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

                //Action Icons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    //More options menu
                    if (record.currentUserPermission != PermissionLevel.VIEWER) {
                        var showMenu by remember { mutableStateOf(false) }
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(Icons.Rounded.MoreVert, stringResource(R.string.more_options))
                            }
                            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                //Dropdown items for Edit and Delete
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.edit)) },
                                    onClick = {
                                        showMenu = false
                                        onEditClick(record)
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
                                        onDeleteClick(record)
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

                    //Expand icon
                    if (pets.isNotEmpty() || record.description.isNotBlank()) {
                        val rotationAngle by animateFloatAsState(
                            targetValue = if (isExpanded) 180f else 0f,
                            label = "ArrowRotation"
                        )
                        IconButton(onClick = { isExpanded = !isExpanded }) {
                            Icon(
                                Icons.Rounded.KeyboardArrowUp,
                                stringResource(R.string.expand),
                                modifier = Modifier.rotate(rotationAngle)
                            )
                        }
                    }
                }
            }

            //Expandable content
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
                exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top)
            ) {
                Column(
                    modifier = Modifier
                        .padding(
                            start = Dimen.spaceMedium,
                            end = Dimen.spaceMedium,
                            bottom = Dimen.spaceMedium
                        ),
                    verticalArrangement = Arrangement.spacedBy(Dimen.spaceMedium)
                ) {
                    HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

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
                                PetChip(pet = pet)
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
    }
}

@Composable
fun PetChip(
    pet: Pet,
    modifier: Modifier = Modifier
) {
    val fallbackRes = when (pet.species) {
        PetSpecies.DOG -> R.drawable.ic_dog
        PetSpecies.CAT -> R.drawable.ic_cat
    }
    val image = pet.avatar?.let { decodeBase64ToImage(it) }

    AssistChip(
        modifier = modifier,
        onClick = {},
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
    onSearch: () -> Unit,
    placeholderText: String,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
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
                onSearch()
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
    modifier: Modifier = Modifier
) {
    TopBar(
        modifier = modifier,
        title = {
            SearchAppBarField(
                query = searchQuery,
                onQueryChange = onSearchQueryChange,
                onSearch = onSearchTriggered,
                placeholderText = stringResource(R.string.search)
            )
        },
        navigationIcon = {
            IconButton(
                onClick = {  }
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