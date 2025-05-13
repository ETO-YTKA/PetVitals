package com.example.petvitals.ui.screens.records

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.petvitals.R
import com.example.petvitals.data.repository.record.RecordType
import com.example.petvitals.ui.components.ScreenLayout
import com.example.petvitals.ui.components.TopBarProfileSettings
import com.example.petvitals.ui.theme.Dimen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordsScreen(
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToCreateRecord: () -> Unit,
    viewModel: RecordsViewModel = hiltViewModel()
) {
    ScreenLayout(
        topBar = {
            TopBarProfileSettings(
                title = stringResource(R.string.records),
                onNavigateToSettings = onNavigateToSettings,
                onNavigateToUserProfile = onNavigateToProfile
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreateRecord,
                modifier = Modifier
                    .padding(Dimen.spaceHuge)
                    .size(Dimen.fabSize)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_note_add),
                    contentDescription = stringResource(R.string.create_record),
                    modifier = Modifier.size(Dimen.fabIconSize)
                )
            }
        }
    ) {
        val uiState by viewModel.uiState.collectAsState()
        val context = LocalContext.current

        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { viewModel.refreshRecords() }
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(uiState.records.size) { index ->
                    val record = uiState.records[index]
                    RecordCard(
                        title = record.title,
                        description = record.description,
                        type = record.type,
                        date = viewModel.formatDateForDisplay(record.date, context),
                        modifier = Modifier.clickable { }
                    )
                }
            }
        }
    }
}

@Composable
fun RecordCard(
    title: String,
    description: String,
    type: RecordType,
    date: String,
    modifier: Modifier = Modifier
) {
    var isDescriptionTruncated by remember { mutableStateOf(false) }
    val maxLinesCollapsed = 3

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.padding(vertical = Dimen.spaceMedium)
    ) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(10.dp)
                    .fillMaxHeight()
                    .background(type.color, RoundedCornerShape(topStart = 2.dp, bottomStart = 2.dp))
            )
            Spacer(modifier = Modifier.width(Dimen.spaceSmall))
            Column(
                modifier = modifier.padding(Dimen.spaceMedium)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = stringResource(type.titleResId),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                    Spacer(modifier = Modifier.width(Dimen.spaceMedium))

                    Text(
                        text = date,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.End,
                        maxLines = 1
                    )
                }
                Spacer(modifier = Modifier.height(Dimen.spaceLarge))

                var expanded by remember { mutableStateOf(false) }
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = if (expanded) Int.MAX_VALUE else maxLinesCollapsed,
                    overflow = TextOverflow.Ellipsis,
                    onTextLayout = { textLayoutResult: TextLayoutResult ->
                        isDescriptionTruncated = textLayoutResult.hasVisualOverflow
                    }
                )

//                if (isDescriptionTruncated || expanded) {
//                    Box(
//                        modifier = Modifier
//                            .align(Alignment.End)
//                            .clickable { expanded = !expanded }
//                    ) {
//                        Text(
//                            text = if (expanded) stringResource(R.string.show_less)
//                            else stringResource(R.string.read_more),
//                            style = MaterialTheme.typography.labelMedium,
//                            color = MaterialTheme.colorScheme.primary,
//                            modifier = Modifier.padding(
//                                top = Dimen.spaceMedium,
//                                end = Dimen.spaceMedium,
//                                bottom = Dimen.spaceSmall,
//                                start = Dimen.spaceMedium
//                            )
//                        )
//                    }
//                }
            }
        }
    }
}