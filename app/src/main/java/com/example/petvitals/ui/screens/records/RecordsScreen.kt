package com.example.petvitals.ui.screens.records

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.example.petvitals.R
import com.example.petvitals.data.repository.pet.Pet
import com.example.petvitals.data.repository.pet.PetSpecies
import com.example.petvitals.data.repository.record.Record
import com.example.petvitals.ui.components.CustomIconButton
import com.example.petvitals.ui.components.ScreenLayout
import com.example.petvitals.ui.components.TopBar
import com.example.petvitals.ui.theme.Dimen
import com.example.petvitals.utils.decodeBase64ToImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordsScreen(
    onNavigateToCreateRecord: () -> Unit,
    viewModel: RecordsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    ScreenLayout(
        topBar = {
            TopBar(
                title = {
                    TextField(
                        value = uiState.searchCond,
                        onValueChange = viewModel::onSearchCondChange,
                        placeholder = { Text(stringResource(R.string.search)) },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        ),
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_search),
                                contentDescription = null
                            )
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { viewModel.search() })
                    )
                },
                actions = {
                    AnimatedContent(targetState = uiState.selectionMode) { targetState ->
                        if (targetState) {
                            CustomIconButton(
                                onClick = viewModel::deleteSelectedRecords,
                                painter = painterResource(id = R.drawable.ic_delete_forever),
                                contentDescription = stringResource(R.string.delete)
                            )
                        } else {
                            CustomIconButton(
                                onClick = onNavigateToCreateRecord,
                                painter = painterResource(id = R.drawable.ic_note_add),
                                contentDescription = stringResource(R.string.create_record)
                            )
                        }
                    }
                }
            )
        }
    ) {
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { viewModel.getRecords() }
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(
                    count = uiState.recordWithPets.size,
                    key = { index -> uiState.recordWithPets[index].record.id }
                ) { index ->
                    val recordWithPets = uiState.recordWithPets[index]

                    RecordCard(
                        recordWithPets = recordWithPets,
                        recordDate = viewModel.formatDateForDisplay(recordWithPets.record.date),
                        selected = uiState.selectedRecords.contains(recordWithPets.record),
                        onEditClick = {},
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
    recordDate: String,
    onEditClick: (Record) -> Unit,
    onDeleteClick: (Record) -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false
) {
    val record = recordWithPets.record
    val pets = recordWithPets.pets

    Card(
        modifier = modifier
            .animateContentSize(
                animationSpec = spring(
                    stiffness = Spring.StiffnessHigh,
                    visibilityThreshold = IntSize.VisibilityThreshold
                ),
            )
            .padding(vertical = Dimen.spaceSmall)
    ) {
        Column(
            modifier = modifier.padding(Dimen.spaceMedium)
        ) {
            var isExpanded by remember { mutableStateOf(false) }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                //Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimen.spaceSmall),
                    modifier = Modifier.weight(1f)
                ) {
                    //Indicator
                    Box(
                        modifier = Modifier
                            .padding(end = Dimen.spaceMedium)
                            .size(30.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(
                                    color = record.type.color,
                                    shape = RoundedCornerShape(100)
                                )
                                .align(Alignment.Center)
                        )
                        if (selected) {
                            Box(
                                modifier = Modifier
                                    .size(17.dp)
                                    .align(Alignment.TopEnd)
                                    .background(
                                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                        shape = RoundedCornerShape(100)
                                    )
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_check),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .background(
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = RoundedCornerShape(100)
                                        )
                                        .size(14.dp)
                                )
                            }
                        }
                    }

                    //Title, type, date
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = record.title,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )

                        Text(
                            text = "${stringResource(record.type.titleResId)} • $recordDate",
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            modifier = Modifier.alpha(0.7f)
                        )
                    }


                    //Expand
                    if (pets.isNotEmpty() || record.description.isNotEmpty()) {

                        val rotationAngle by animateFloatAsState(
                            targetValue = if (isExpanded) 180f else 0f,
                            label = "ArrowRotation"
                        )
                        IconButton(
                            onClick = { isExpanded = !isExpanded }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.KeyboardArrowUp,
                                contentDescription = stringResource(R.string.expand),
                                modifier = Modifier.rotate(rotationAngle)
                            )
                        }
                    }

                    //More options
                    var showMenu by remember { mutableStateOf(false) }
                    Box {
                        IconButton(
                            onClick = { showMenu = true }
                        ) {
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
            }

            //Description
            AnimatedContent(
                targetState = isExpanded,
                transitionSpec = { // Caution AI slop
                    if (targetState) {
                        slideInVertically(
                            animationSpec = tween(
                                durationMillis = 300,
                                delayMillis = 50
                            ), // Customize duration/delay
                            initialOffsetY = { fullHeight -> -fullHeight } // Start fully off-screen above
                        ) + fadeIn(
                            animationSpec = tween(durationMillis = 200, delayMillis = 100)
                        ) togetherWith // Use 'togetherWith' to combine with the exit transition of old content
                                // Exit transition for the (empty) content that was there when collapsed
                                slideOutVertically(
                                    animationSpec = tween(durationMillis = 200),
                                    targetOffsetY = { fullHeight -> fullHeight / 2 } // Optional: slide out partially
                                ) + fadeOut(
                            animationSpec = tween(durationMillis = 150)
                        )
                    }
                    // Transition for when content is COLLAPSING (targetState is false)
                    else { // targetState is 'isExpanded' becoming false
                        // Slide in the (empty) content that will be there when collapsed
                        slideInVertically(
                            animationSpec = tween(durationMillis = 200, delayMillis = 50),
                            initialOffsetY = { fullHeight -> fullHeight / 2 } // Optional: slide in partially
                        ) + fadeIn(
                            animationSpec = tween(durationMillis = 150, delayMillis = 100)
                        ) togetherWith
                                // Slide up to the top and fade out for the existing expanded content
                                slideOutVertically(
                                    animationSpec = tween(durationMillis = 300),
                                    targetOffsetY = { fullHeight -> -fullHeight } // Slide fully off-screen above
                                ) + fadeOut(
                            animationSpec = tween(durationMillis = 200)
                        )
                    }
                }
            ) { targetState ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(Dimen.spaceMedium)
                ) {
                    if (targetState) {

                        if (pets.isNotEmpty()) {
                            FlowRow {
                                pets.forEach { pet ->
                                    PetCard(pet)
                                }
                            }
                        }

                        if (record.description.isNotEmpty()) {
                            Text(
                                text = record.description,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PetCard(
    pet: Pet,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline,
            shape = CircleShape
        ),
        shape = CircleShape,
        colors = CardDefaults.cardColors().copy(
            containerColor = MaterialTheme.colorScheme.background
        )
    ) {
        Row(
            modifier = Modifier.padding(Dimen.spaceSmall),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(Dimen.spaceSmall))

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
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
            )
        }
    }
}
