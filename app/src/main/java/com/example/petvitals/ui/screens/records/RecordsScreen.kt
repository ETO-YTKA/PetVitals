package com.example.petvitals.ui.screens.records

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.petvitals.R
import com.example.petvitals.data.repository.record.RecordType
import com.example.petvitals.ui.components.ScreenLayout
import com.example.petvitals.ui.theme.Dimen

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
                onNavigateToCreateRecord = onNavigateToCreateRecord,
                onDeleteClick = { uiState.selectedRecords.forEach { viewModel.deleteRecord(it) } },
                selectionMode = uiState.selectionMode
            )
        }
    ) {
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { viewModel.refreshRecords() }
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(
                    count = uiState.records.size,
                    key = { index -> uiState.records[index].recordId.toString() }
                ) { index ->
                    val record = uiState.records[index]
                    RecordCard(
                        title = record.title,
                        description = record.description,
                        type = record.type,
                        date = viewModel.formatDateForDisplay(record.date),
                        selected = uiState.selectedRecords.contains(record),
                        modifier = Modifier
                            .pointerInput(record) {
                                this.detectTapGestures(
                                    onLongPress = {
                                        if (!uiState.selectionMode) {
                                            viewModel.selectRecord(record)
                                        }
                                    },
                                    onTap = {
                                        if (uiState.selectionMode) {
                                            viewModel.selectRecord(record)
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
    title: String,
    description: String,
    type: RecordType,
    date: String,
    modifier: Modifier = Modifier,
    selected: Boolean = false
) {
    Card(
        modifier = modifier
            .padding(vertical = Dimen.spaceSmall)
            .animateContentSize()
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimen.spaceSmall),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier.padding(end = Dimen.spaceMedium)
                    ) {
                        AnimatedContent(
                            targetState = selected,
                            transitionSpec = {
                                fadeIn(
                                    animationSpec = tween(300)
                                ) togetherWith fadeOut(animationSpec = tween(300))
                            }
                        ) { targetState ->

                            if (targetState) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_check),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .background(
                                            color = Color(0xff47df00),
                                            shape = RoundedCornerShape(100)
                                        )
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(
                                            color = type.color,
                                            shape = RoundedCornerShape(100)
                                        )
                                        .align(Alignment.Center)
                                )
                            }
                        }
                    }

                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, false)
                    )

                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = stringResource(type.titleResId),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.width(Dimen.spaceMedium))

                Text(
                    text = date,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1
                )

                if (description.isNotEmpty()) {
                    val rotationAngle by animateFloatAsState(
                        targetValue = if (isExpanded) 180f else 0f,
                        label = "ArrowRotation"
                    )
                    IconButton(
                        onClick = { isExpanded = !isExpanded }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.KeyboardArrowUp,
                            contentDescription = null,
                            modifier = Modifier.rotate(rotationAngle)
                        )
                    }
                }
            }
            if (isExpanded) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    onNavigateToCreateRecord: () -> Unit,
    onDeleteClick: () -> Unit,
    selectionMode: Boolean = false
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = stringResource(R.string.records),
                style = MaterialTheme.typography.titleLarge
            )
        },
        actions = {
            AnimatedContent(targetState = selectionMode, label = "TopBarActions") { targetState ->
                if (targetState) {
                    IconButton(
                        onClick = onDeleteClick
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_delete_forever),
                            contentDescription = stringResource(R.string.delete)
                        )
                    }
                } else {
                    IconButton(
                        onClick = onNavigateToCreateRecord
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_note_add),
                            contentDescription = stringResource(R.string.create_record)
                        )
                    }
                }
            }
        }
    )
}
