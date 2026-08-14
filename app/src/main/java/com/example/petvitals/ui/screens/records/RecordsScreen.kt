@file:OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)

package com.example.petvitals.ui.screens.records

import android.text.format.DateFormat
import android.text.format.DateUtils
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import coil3.compose.AsyncImage
import com.example.petvitals.R
import com.example.petvitals.domain.models.Pet
import com.example.petvitals.domain.models.Record
import com.example.petvitals.domain.models.RecordOverview
import com.example.petvitals.domain.models.RecordType
import com.example.petvitals.ui.components.CenterAlignedTopBar
import com.example.petvitals.ui.components.ConfirmationDialog
import com.example.petvitals.ui.components.CustomMediumButton
import com.example.petvitals.ui.components.CustomSnackbarHost
import com.example.petvitals.ui.components.CustomTextField
import com.example.petvitals.ui.components.Loading
import com.example.petvitals.ui.components.ResetTopBarWhenNotScrollable
import com.example.petvitals.ui.components.SnackbarState
import com.example.petvitals.ui.components.SnackbarType
import com.example.petvitals.ui.components.rememberTopBarScrollBehavior
import com.example.petvitals.ui.components.showSnackbar
import com.example.petvitals.ui.theme.Dimen
import com.example.petvitals.ui.theme.PetVitalsTheme
import com.example.petvitals.ui.utils.ObserveAsEvents
import com.example.petvitals.ui.utils.decodeBase64ToImage
import java.util.Calendar
import java.util.Date

@Composable
fun RecordsScreen(
    onNavigateToAddEditRecord: (String?) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToPetProfile: (String) -> Unit,
    viewModel: RecordsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollBehavior = rememberTopBarScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }
    val resources = LocalResources.current
    var hasResumedOnce by rememberSaveable { mutableStateOf(false) }

    LifecycleResumeEffect(Unit) {
        if (hasResumedOnce) {
            viewModel.onAction(RecordsAction.OnRefresh)
        } else {
            hasResumedOnce = true
        }
        onPauseOrDispose { }
    }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is RecordsEvent.OnShowError -> snackbarHostState.showSnackbar(
                SnackbarState(
                    message = resources.getString(event.messageRes),
                    snackbarType = SnackbarType.ERROR
                )
            )
        }
    }

    RecordsScreenContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        scrollBehavior = scrollBehavior,
        onAction = viewModel::onAction,
        onNavigateToAddRecord = { onNavigateToAddEditRecord(null) },
        onNavigateToEditRecord = { onNavigateToAddEditRecord(it) },
        onNavigateToProfile = onNavigateToProfile,
        onNavigateToPetProfile = onNavigateToPetProfile
    )
}

@Composable
internal fun RecordsScreenContent(
    uiState: RecordsUiState,
    snackbarHostState: SnackbarHostState,
    onAction: (RecordsAction) -> Unit,
    onNavigateToAddRecord: () -> Unit,
    onNavigateToEditRecord: (String) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToPetProfile: (String) -> Unit,
    modifier: Modifier = Modifier,
    scrollBehavior: androidx.compose.material3.TopAppBarScrollBehavior = rememberTopBarScrollBehavior()
) {
    val listState = rememberLazyListState()
    var recordPendingDeletion by remember { mutableStateOf<String?>(null) }

    ResetTopBarWhenNotScrollable(
        scrollBehavior = scrollBehavior,
        canScrollBackward = listState.canScrollBackward,
        canScrollForward = listState.canScrollForward,
        contentVisible = !uiState.isInitialLoading && uiState.errorMessageRes == null
    )

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { CustomSnackbarHost(snackbarHostState) },
        floatingActionButton = {
            val expanded by remember {
                derivedStateOf {
                    listState.firstVisibleItemIndex == 0 &&
                            listState.firstVisibleItemScrollOffset < 24
                }
            }
            ExtendedFloatingActionButton(
                onClick = onNavigateToAddRecord,
                expanded = expanded,
                modifier = Modifier.testTag("new-record-fab"),
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_note_add),
                        contentDescription = stringResource(R.string.new_record)
                    )
                },
                text = { Text(stringResource(R.string.new_record)) }
            )
        },
        topBar = {
            RecordsTopBar(
                onNavigateToProfile = onNavigateToProfile,
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .padding(paddingValues)
                .padding(horizontal = Dimen.Screen.horizontalPadding)
                .fillMaxSize()
        ) {
            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = { onAction(RecordsAction.OnRefresh) },
                modifier = Modifier.fillMaxSize()
            ) {
                when {
                    uiState.isInitialLoading -> Loading()

                    uiState.errorMessageRes != null -> RecordsStateMessage(
                        iconRes = R.drawable.ic_error,
                        title = stringResource(R.string.records_error_title),
                        message = stringResource(uiState.errorMessageRes),
                        actionLabel = stringResource(R.string.retry),
                        onAction = { onAction(RecordsAction.OnRefresh) }
                    )

                    uiState.records.isEmpty() -> RecordsStateMessage(
                        iconRes = R.drawable.ic_history,
                        title = stringResource(R.string.records_empty_title),
                        message = stringResource(R.string.records_empty_message),
                        actionLabel = stringResource(R.string.create_record),
                        onAction = onNavigateToAddRecord
                    )

                    else -> RecordsTimeline(
                        uiState = uiState,
                        listState = listState,
                        onAction = onAction,
                        onDeleteRequest = { recordPendingDeletion = it },
                        onNavigateToEditRecord = onNavigateToEditRecord,
                        onNavigateToPetProfile = onNavigateToPetProfile
                    )
                }
            }
        }
    }

    if (recordPendingDeletion != null) {
        ConfirmationDialog(
            onDismissRequest = { recordPendingDeletion = null },
            onConfirmation = {
                recordPendingDeletion?.let {
                    onAction(RecordsAction.OnDeleteRecordClick(it))
                }
            },
            title = stringResource(R.string.delete_record_title),
            text = stringResource(R.string.delete_record_confirmation),
            confirmButtonText = stringResource(R.string.delete),
            isConfirmButtonDestructive = true
        )
    }
}

@Composable
private fun RecordsTopBar(
    onNavigateToProfile: () -> Unit,
    scrollBehavior: androidx.compose.material3.TopAppBarScrollBehavior
) {
    CenterAlignedTopBar(
        scrollBehavior = scrollBehavior,
        title = {
            Text(
                text = stringResource(R.string.records),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigateToProfile) {
                Icon(
                    painter = painterResource(R.drawable.ic_person),
                    contentDescription = stringResource(R.string.profile)
                )
            }
        }
    )
}

@Composable
private fun RecordsTimeline(
    uiState: RecordsUiState,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onAction: (RecordsAction) -> Unit,
    onDeleteRequest: (String) -> Unit,
    onNavigateToEditRecord: (String) -> Unit,
    onNavigateToPetProfile: (String) -> Unit
) {
    val hasActiveFilters = uiState.searchQuery.isNotBlank() ||
        uiState.selectedPetIds.isNotEmpty() ||
        uiState.selectedTypeFilters.isNotEmpty()
    val displayedRecords = remember(
        uiState.records,
        uiState.searchQuery,
        uiState.selectedPetIds,
        uiState.selectedTypeFilters
    ) {
        mapRecordEntries(
            records = uiState.records,
            query = uiState.searchQuery,
            selectedPetIds = uiState.selectedPetIds,
            selectedTypes = uiState.selectedTypeFilters
        )
    }
    val filterPets = remember(uiState.records) {
        uiState.records
            .flatMap { it.pets }
            .distinctBy { it.id }
            .sortedBy { it.name.lowercase() }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 104.dp)
    ) {
        item(key = "search-and-filters") {
            RecordsSearchAndFilters(
                query = uiState.searchQuery,
                allPets = filterPets,
                allTypes = RecordType.entries,
                selectedPetIds = uiState.selectedPetIds,
                selectedTypes = uiState.selectedTypeFilters,
                onAction = onAction,
                modifier = Modifier.padding(vertical = 12.dp)
            )
        }

        if (displayedRecords.isEmpty()) {
            item(key = "no-results") {
                RecordsStateMessage(
                    iconRes = R.drawable.ic_history,
                    title = stringResource(R.string.records_no_results_title),
                    message = stringResource(
                        if (hasActiveFilters) {
                            R.string.records_no_results_message
                        } else {
                            R.string.records_empty_message
                        }
                    ),
                    actionLabel = if (hasActiveFilters) {
                        stringResource(R.string.clear_filters)
                    } else {
                        null
                    },
                    onAction = if (hasActiveFilters) {
                        { onAction(RecordsAction.OnClearFilters) }
                    } else {
                        null
                    },
                    modifier = Modifier.fillParentMaxHeight(0.65f)
                )
            }
        } else {
            displayedRecords.forEach { entry ->
                when (entry) {
                    is RecordsListEntry.Header -> stickyHeader(
                        key = "header-${entry.date.time}",
                        contentType = "date-header"
                    ) {
                        DateHeader(entry.date)
                    }

                    is RecordsListEntry.RecordItem -> item(
                        key = entry.overview.record.id,
                        contentType = "record"
                    ) {
                        val overview = entry.overview
                        val record = overview.record

                        RecordTimelineRow(
                            overview = overview,
                            expanded = record.id in uiState.expandedRecordIds,
                            isDeleting = uiState.deletingRecordId == record.id,
                            onExpansionToggle = {
                                onAction(RecordsAction.OnRecordExpansionToggle(record.id))
                            },
                            onEdit = { onNavigateToEditRecord(record.id) },
                            onDelete = {
                                onDeleteRequest(record.id)
                            },
                            onPetClick = { onNavigateToPetProfile(it.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecordsSearchAndFilters(
    query: String,
    allPets: List<Pet>,
    allTypes: List<RecordType>,
    selectedPetIds: Set<String>,
    selectedTypes: Set<RecordType>,
    onAction: (RecordsAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        CustomTextField(
            value = query,
            onValueChange = { onAction(RecordsAction.OnSearchQueryChange(it)) },
            label = { Text(stringResource(R.string.records_search_label)) },
            placeholder = { Text(stringResource(R.string.records_search_placeholder)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null
                )
            },
            trailingIcon = if (query.isNotEmpty()) {
                {
                    IconButton(onClick = { onAction(RecordsAction.OnClearSearch) }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = stringResource(R.string.clear_search)
                        )
                    }
                }
            } else {
                null
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                }
            ),
            modifier = Modifier.fillMaxWidth()
        )

        if (allPets.isNotEmpty()) {
            RecordsFilterRow(
                items = allPets,
                key = { it.id },
                selected = { it.id in selectedPetIds },
                label = { it.name },
                testTag = { "pet-filter-${it.id}" },
                onClick = { onAction(RecordsAction.OnPetFilterToggle(it.id)) }
            )
        }

        RecordsFilterRow(
            items = allTypes,
            key = { it.name },
            selected = { it in selectedTypes },
            label = { stringResource(it.titleResId) },
            testTag = { "type-filter-${it.name}" },
            leadingIcon = { type, isSelected ->
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                    )
                } else {
                    Icon(
                        painter = painterResource(type.iconResId),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                    )
                }
            },
            onClick = { onAction(RecordsAction.OnTypeFilterToggle(it)) }
        )
    }
}

@Composable
private fun <T> RecordsFilterRow(
    items: List<T>,
    key: (T) -> Any,
    selected: (T) -> Boolean,
    label: @Composable (T) -> String,
    testTag: (T) -> String,
    onClick: (T) -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable ((T, Boolean) -> Unit)? = null
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(end = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items = items, key = key) { item ->
            val isSelected = selected(item)
            FilterChip(
                selected = isSelected,
                onClick = { onClick(item) },
                label = { Text(label(item), maxLines = 1) },
                leadingIcon = if (leadingIcon != null || isSelected) {
                    {
                        if (leadingIcon != null) {
                            leadingIcon(item, isSelected)
                        } else {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(FilterChipDefaults.IconSize)
                            )
                        }
                    }
                } else {
                    null
                },
                shape = CircleShape,
                border = null,
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    labelColor = MaterialTheme.colorScheme.onSurface,
                    iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.testTag(testTag(item))
            )
        }
    }
}

@Composable
private fun DateHeader(date: Date) {
    val now = remember { Date() }
    val context = LocalContext.current
    val dateText = when {
        isSameDay(date, now) -> stringResource(R.string.today)
        isYesterday(date, now) -> stringResource(R.string.yesterday)
        else -> DateUtils.formatDateTime(
            context,
            date.time,
            DateUtils.FORMAT_SHOW_DATE or
                DateUtils.FORMAT_SHOW_WEEKDAY or
                DateUtils.FORMAT_ABBREV_MONTH or
                if (
                    Calendar.getInstance().apply { time = date }[Calendar.YEAR] !=
                    Calendar.getInstance().apply { time = now }[Calendar.YEAR]
                ) {
                    DateUtils.FORMAT_SHOW_YEAR
                } else {
                    0
                }
        )
    }

    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = dateText,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 60.dp, end = 16.dp, top = 12.dp, bottom = 8.dp)
        )
    }
}

private fun isSameDay(first: Date, second: Date): Boolean {
    val firstCalendar = Calendar.getInstance().apply { time = first }
    val secondCalendar = Calendar.getInstance().apply { time = second }
    return firstCalendar[Calendar.ERA] == secondCalendar[Calendar.ERA] &&
        firstCalendar[Calendar.YEAR] == secondCalendar[Calendar.YEAR] &&
        firstCalendar[Calendar.DAY_OF_YEAR] == secondCalendar[Calendar.DAY_OF_YEAR]
}

private fun isYesterday(date: Date, now: Date): Boolean {
    val yesterday = Calendar.getInstance().apply {
        time = now
        add(Calendar.DAY_OF_YEAR, -1)
    }
    return isSameDay(date, yesterday.time)
}

@Composable
private fun RecordTimelineRow(
    overview: RecordOverview,
    expanded: Boolean,
    isDeleting: Boolean,
    onExpansionToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onPetClick: (Pet) -> Unit,
    modifier: Modifier = Modifier
) {
    val record = overview.record
    val canManage = overview.canManage && !isDeleting
    val context = LocalContext.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .animateContentSize(animationSpec = tween(220)),
        verticalAlignment = Alignment.Top
    ) {
        FloatingTypeMedallion(
            record = record,
            modifier = Modifier.width(44.dp)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .clip(MaterialTheme.shapes.large)
                .clickable(
                    onClickLabel = stringResource(
                        if (expanded) R.string.collapse else R.string.expand
                    ),
                    onClick = onExpansionToggle
                )
                .padding(start = 8.dp, bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 7.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        text = record.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = stringResource(record.type.titleResId),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = DateFormat.getTimeFormat(context).format(
                                record.eventDate ?: record.createdAt
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (overview.pets.isNotEmpty()) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            overview.pets.forEach { pet ->
                                PetChip(
                                    pet = pet,
                                    onClick = { onPetClick(pet) }
                                )
                            }
                        }
                    }
                }

                RecordActionsMenu(
                    expanded = expanded,
                    canManage = canManage,
                    onExpansionToggle = onExpansionToggle,
                    onEdit = onEdit,
                    onDelete = onDelete
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(tween(160)) + expandVertically(tween(220)),
                exit = fadeOut(tween(120)) + shrinkVertically(tween(180))
            ) {
                Column(
                    modifier = Modifier.padding(top = 12.dp, end = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = if (record.description.isNullOrBlank()) {
                            stringResource(R.string.records_no_description)
                        } else {
                            record.description.orEmpty()
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (record.description.isNullOrBlank()) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )

                    if (canManage) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = onEdit) {
                                Text(stringResource(R.string.edit))
                            }
                            TextButton(onClick = onDelete) {
                                Text(
                                    text = stringResource(R.string.delete),
                                    color = MaterialTheme.colorScheme.error
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
private fun FloatingTypeMedallion(
    record: Record,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.testTag("record-type-gutter-${record.id}"),
        contentAlignment = Alignment.TopCenter
    ) {
        RecordTypeIcon(
            record = record,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(40.dp)
                .testTag("record-type-medallion-${record.id}")
        )
    }
}

@Composable
private fun RecordActionsMenu(
    expanded: Boolean,
    canManage: Boolean,
    onExpansionToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { showMenu = true }) {
            Icon(
                imageVector = Icons.Rounded.MoreVert,
                contentDescription = stringResource(R.string.more_options)
            )
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(
                text = {
                    Text(stringResource(if (expanded) R.string.collapse else R.string.expand))
                },
                onClick = {
                    showMenu = false
                    onExpansionToggle()
                }
            )

            if (canManage) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.edit)) },
                    leadingIcon = {
                        Icon(Icons.Outlined.Edit, contentDescription = null)
                    },
                    onClick = {
                        showMenu = false
                        onEdit()
                    }
                )
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(R.string.delete),
                            color = MaterialTheme.colorScheme.error
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    },
                    onClick = {
                        showMenu = false
                        onDelete()
                    }
                )
            }
        }
    }
}

@Composable
private fun RecordTypeIcon(
    record: Record,
    modifier: Modifier = Modifier
) {
    val (containerColor, contentColor) = when (record.type) {
        RecordType.VACCINATION -> MaterialTheme.colorScheme.tertiaryContainer to
            MaterialTheme.colorScheme.onTertiaryContainer
        RecordType.MEDICATION,
        RecordType.VET_VISIT -> MaterialTheme.colorScheme.primaryContainer to
            MaterialTheme.colorScheme.onPrimaryContainer
        RecordType.SYMPTOM,
        RecordType.GROOMING -> MaterialTheme.colorScheme.secondaryContainer to
            MaterialTheme.colorScheme.onSecondaryContainer
        RecordType.INCIDENT -> MaterialTheme.colorScheme.errorContainer to
            MaterialTheme.colorScheme.onErrorContainer
        RecordType.NOTE -> MaterialTheme.colorScheme.surfaceContainerHighest to
            MaterialTheme.colorScheme.onSurface
    }

    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = containerColor
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(record.type.iconResId),
                contentDescription = stringResource(record.type.titleResId),
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun PetChip(
    pet: Pet,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val image = pet.avatar?.let(::decodeBase64ToImage)

    AssistChip(
        modifier = modifier.testTag("record-pet-${pet.id}"),
        onClick = onClick,
        label = {
            Text(
                text = pet.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        leadingIcon = {
            AsyncImage(
                model = image,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                fallback = painterResource(pet.species.drawableRes),
                modifier = Modifier
                    .size(AssistChipDefaults.IconSize)
                    .then(if (image != null) Modifier.clip(CircleShape) else Modifier)
            )
        },
        shape = CircleShape,
        border = null,
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            leadingIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

@Composable
private fun RecordsStateMessage(
    iconRes: Int,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier
                        .padding(16.dp)
                        .size(32.dp)
                )
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            if (actionLabel != null && onAction != null) {
                Spacer(Modifier.height(4.dp))
                CustomMediumButton(onClick = onAction) {
                    Text(
                        text = actionLabel,
                        fontSize = Dimen.FontSize.mediumButton
                    )
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun RecordsPreview(
    @PreviewParameter(RecordsPreviewParameterProvider::class) state: RecordsUiState
) {
    val snackbarHostState = remember { SnackbarHostState() }

    PetVitalsTheme {
        Surface {
            RecordsScreenContent(
                uiState = state,
                snackbarHostState = snackbarHostState,
                onAction = {},
                onNavigateToAddRecord = {},
                onNavigateToEditRecord = {},
                onNavigateToProfile = {},
                onNavigateToPetProfile = {}
            )
        }
    }
}
