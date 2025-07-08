package com.example.petvitals.ui.screens.share_pet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.petvitals.R
import com.example.petvitals.data.repository.pet_permission.PermissionLevel
import com.example.petvitals.ui.components.ButtonWithIcon
import com.example.petvitals.ui.components.CustomIconButton
import com.example.petvitals.ui.components.CustomOutlinedTextField
import com.example.petvitals.ui.components.DropDownOption
import com.example.petvitals.ui.components.ScreenLayout
import com.example.petvitals.ui.components.TopBar
import com.example.petvitals.ui.components.ValueDropDown
import com.example.petvitals.ui.theme.Dimen

@Composable
fun SharePetScreen(
    petId: String,
    onPopBackStack: () -> Unit,
    viewModel: SharePetViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.getPetPermissions(petId)
    }

    ScreenLayout(
        horizontalAlignment = Alignment.Start,
        topBar = {
            TopBar(
                title = { Text(stringResource(R.string.pet_sharing)) },
                navigationIcon = {
                    CustomIconButton(
                        onClick = onPopBackStack,
                        painter = painterResource(R.drawable.ic_arrow_back),
                        contentDescription = stringResource(R.string.back)
                    )
                }
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(Dimen.spaceLarge),
            verticalArrangement = Arrangement.spacedBy(Dimen.spaceLarge)
        ) {
            //Who has access
            item {
                SectionHeader(
                    title = stringResource(R.string.who_has_access),
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_group),
                            contentDescription = null
                        )
                    }
                )
            }

            if (uiState.isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            } else if (uiState.userPermissions.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.only_you_have_access),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(
                    items = uiState.userPermissions,
                    key = { it.user.id }
                ) { userPermission ->
                    UserPermissionCard(
                        username = userPermission.user.email,
                        permissionLevel = userPermission.permissionLevel,
                        onDeleteClick = { viewModel.onDeleteAccessClick(petId, userPermission.user.id) }
                    )
                }
            }

            //Invite a new user
            item {
                SectionHeader(
                    title = stringResource(R.string.invite_new_user),
                    icon = { Icon(painter = painterResource(R.drawable.ic_person_add), contentDescription = null) },
                    modifier = Modifier.padding(top = Dimen.spaceMedium)
                )
            }

            item {
                InviteUserForm(
                    uiState = uiState,
                    onEmailChange = viewModel::onEmailChange,
                    onPermissionLevelChange = viewModel::onPermissionLevelChange,
                    onShareClick = viewModel::onShareClick
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimen.spaceSmall)
    ) {
        icon()
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun UserPermissionCard(
    username: String,
    permissionLevel: PermissionLevel,
    modifier: Modifier = Modifier,
    onDeleteClick: () -> Unit,
) {
    ListItem(
        modifier = modifier.clip(RoundedCornerShape(Dimen.spaceLarge)),
        headlineContent = {
            Text(
                text = username,
                style = MaterialTheme.typography.bodyLarge
            )
        },
        supportingContent = {
            Text(
                text = stringResource(permissionLevel.nameResId),
                style = MaterialTheme.typography.bodyMedium
            )
        },
        leadingContent = {
            Icon(
                painter = painterResource(R.drawable.ic_person),
                contentDescription = null,
            )
        },
        trailingContent = {
            IconButton(onClick = onDeleteClick) {
                Icon(
                    painter = painterResource(R.drawable.ic_close),
                    contentDescription = stringResource(R.string.remove_access),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        },
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    )
}

@Composable
private fun InviteUserForm(
    uiState: SharePetUiState,
    onEmailChange: (String) -> Unit,
    onPermissionLevelChange: (PermissionLevel) -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(Dimen.spaceLarge),
            verticalArrangement = Arrangement.spacedBy(Dimen.spaceSmall)
        ) {
            CustomOutlinedTextField(
                value = uiState.email,
                onValueChange = onEmailChange,
                label = { Text(stringResource(R.string.email)) },
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.shareErrorMessage != null,
                supportingText = uiState.shareErrorMessage
            )

            ValueDropDown(
                value = uiState.permissionLevel,
                onValueChange = onPermissionLevelChange,
                options = listOf(
                    DropDownOption(
                        display = stringResource(PermissionLevel.EDITOR.nameResId),
                        value = PermissionLevel.EDITOR
                    ),
                    DropDownOption(
                        display = stringResource(PermissionLevel.VIEWER.nameResId),
                        value = PermissionLevel.VIEWER
                    )
                ),
                label = stringResource(R.string.permission_level)
            )

            ButtonWithIcon(
                text = stringResource(R.string.pet_sharing_add),
                onClick = onShareClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading,
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_person_add),
                        contentDescription = null
                    )
                },
            )
        }
    }
}