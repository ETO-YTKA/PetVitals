package com.example.petvitals.ui.screens.sharepet

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.petvitals.R
import com.example.petvitals.domain.models.Member
import com.example.petvitals.domain.models.PermissionLevel
import com.example.petvitals.domain.models.PetInvite
import com.example.petvitals.ui.components.CenterAlignedTopBar
import com.example.petvitals.ui.components.CustomIconButton
import com.example.petvitals.ui.components.CustomSnackbarHost
import com.example.petvitals.ui.components.ErrorScreenContent
import com.example.petvitals.ui.components.Loading
import com.example.petvitals.ui.components.PopUpButton
import com.example.petvitals.ui.components.PopUpHost
import com.example.petvitals.ui.components.PopUpState
import com.example.petvitals.ui.components.PopUpType
import com.example.petvitals.ui.components.ResetTopBarWhenNotScrollable
import com.example.petvitals.ui.components.rememberTopBarScrollBehavior
import com.example.petvitals.ui.theme.Dimen
import com.example.petvitals.ui.theme.PetVitalsTheme

@Composable
fun SharePetScreen(
    petId: String,
    onPopBackStack: () -> Unit,
    viewModel: SharePetViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(petId) {
        viewModel.getInitialData(petId)
    }

    SharePetScreenContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onAction = viewModel::onAction,
        onPopBackStack = onPopBackStack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SharePetScreenContent(
    uiState: SharePetUiState,
    snackbarHostState: SnackbarHostState,
    onAction: (SharePetAction) -> Unit,
    onPopBackStack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = rememberTopBarScrollBehavior()
    val listState = rememberLazyListState()

    ResetTopBarWhenNotScrollable(
        scrollBehavior = scrollBehavior,
        canScrollBackward = listState.canScrollBackward,
        canScrollForward = listState.canScrollForward,
        contentVisible = !uiState.isLoading && uiState.permissionErrorMessageRes == null
    )

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { CustomSnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopBar(
                scrollBehavior = scrollBehavior,
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
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            when {
                uiState.isLoading -> Loading()

                uiState.permissionErrorMessageRes != null -> ErrorScreenContent(
                    message = stringResource(uiState.permissionErrorMessageRes)
                )

                else -> LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .widthIn(max = 640.dp)
                        .fillMaxWidth()
                        .padding(horizontal = Dimen.Screen.horizontalPadding),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    item {
                        AccessSection(
                            members = uiState.petMembers,
                            removingMemberId = uiState.removingMemberId,
                            onRemoveMember = { onAction(SharePetAction.OnRemoveMember(it)) }
                        )
                    }

                    uiState.membersErrorMessageRes?.let { messageRes ->
                        item {
                            InlineError(message = stringResource(messageRes))
                        }
                    }

                    item {
                        InviteComposer(
                            selectedPermission = uiState.selectedInvitePermission,
                            errorMessageRes = uiState.createInviteErrorMessageRes,
                            onPermissionSelected = {
                                onAction(SharePetAction.OnSelectInvitePermission(it))
                            },
                            onAction = onAction
                        )
                    }

                    item {
                        AnimatedVisibility(
                            visible = uiState.latestGeneratedCode != null,
                            enter = fadeIn(tween(200))
                                    + expandVertically(tween(200)),
                            exit = fadeOut(tween(160))
                                    + shrinkVertically(tween(160))
                        ) {
                            uiState.latestGeneratedCode?.let { code ->
                                GeneratedCodeSection(
                                    code = code,
                                    onAction = onAction
                                )
                            }
                        }
                    }

                    if (uiState.activeInvites.isNotEmpty() || uiState.invitesErrorMessageRes != null) {
                        item {
                            ActiveInvitesSection(
                                invites = uiState.activeInvites,
                                errorMessageRes = uiState.invitesErrorMessageRes,
                                onAction = onAction
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AccessSection(
    members: List<Member>,
    removingMemberId: String?,
    onRemoveMember: (String) -> Unit
) {
    val orderedMembers = remember(members) {
        members.sortedWith(
            compareBy<Member> { it.permissionLevel != PermissionLevel.OWNER }
                .thenBy { it.displayName.lowercase() }
        )
    }
    val count = orderedMembers.size

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionHeading(
            title = stringResource(R.string.who_has_access),
            supportingText = pluralStringResource(
                R.plurals.people_with_access_count,
                count,
                count
            )
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainerLow
        ) {
            if (orderedMembers.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_members_to_display),
                    modifier = Modifier.padding(20.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column {
                    orderedMembers.forEachIndexed { index, member ->
                        MemberRow(
                            member = member,
                            isRemoving = removingMemberId == member.userId,
                            onRemove = { onRemoveMember(member.userId) }
                        )
                        if (index != orderedMembers.lastIndex) {
                            HorizontalDivider(modifier = Modifier.padding(start = 72.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MemberRow(
    member: Member,
    isRemoving: Boolean,
    onRemove: (String) -> Unit
) {
    val isOwner = member.permissionLevel == PermissionLevel.OWNER
    val displayName = member.displayName.ifBlank { member.userId.take(12) }

    val showDeletePopUp = remember { mutableStateOf(false) }

    if (showDeletePopUp.value) {
        PopUpHost(
            popUpState = PopUpState(
                type = PopUpType.WARNING,
                title = stringResource(R.string.share_pet_remove_access_for, displayName),
                message = stringResource(
                    R.string.will_no_longer_be_able_to_view_this_pet_or_make_changes_to_it,
                    displayName
                ),
                primaryButton = PopUpButton(
                    text = stringResource(R.string.remove_access),
                    action = onRemove
                ),
                secondaryButton = PopUpButton(
                    text = stringResource(R.string.keep_access),
                    action = { showDeletePopUp.value = false }
                ),
            ),
            onAction = { showDeletePopUp.value = false },
            onDismiss = { showDeletePopUp.value = false }
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .padding(start = 16.dp, end = 4.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = if (isOwner) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceContainerHighest,
            contentColor = if (isOwner) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurfaceVariant
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(
                        if (isOwner) R.drawable.ic_lock else R.drawable.ic_person
                    ),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = displayName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            RolePill(permissionLevel = member.permissionLevel)
        }

        if (!isOwner) {
            IconButton(
                onClick = { showDeletePopUp.value = true },
                enabled = !isRemoving
            ) {
                if (isRemoving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        painter = painterResource(R.drawable.ic_close),
                        contentDescription = stringResource(R.string.remove_access_for, displayName),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun RolePill(permissionLevel: PermissionLevel) {
    val (containerColor, contentColor) = when (permissionLevel) {
        PermissionLevel.OWNER -> MaterialTheme.colorScheme.primaryContainer to
            MaterialTheme.colorScheme.onPrimaryContainer
        PermissionLevel.EDITOR -> MaterialTheme.colorScheme.secondaryContainer to
            MaterialTheme.colorScheme.onSecondaryContainer
        PermissionLevel.VIEWER -> MaterialTheme.colorScheme.surfaceContainerHighest to
            MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(shape = CircleShape, color = containerColor, contentColor = contentColor) {
        Text(
            text = stringResource(permissionLevel.nameResId),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1
        )
    }
}

@Composable
private fun InviteComposer(
    selectedPermission: PermissionLevel,
    errorMessageRes: Int?,
    onPermissionSelected: (PermissionLevel) -> Unit,
    onAction: (SharePetAction) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionHeading(
            title = stringResource(R.string.create_invite),
            supportingText = stringResource(R.string.invite_code_description)
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainerLow
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.permission_level),
                    style = MaterialTheme.typography.labelLarge
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(PermissionLevel.EDITOR, PermissionLevel.VIEWER).forEach { role ->
                        FilterChip(
                            selected = selectedPermission == role,
                            onClick = { onPermissionSelected(role) },
                            label = { Text(stringResource(role.nameResId)) },
                            leadingIcon = if (selectedPermission == role) {
                                {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_check),
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            } else null,
                            shape = CircleShape,
                            border = null,
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }

                Button(
                    onClick = { onAction(SharePetAction.OnCreateInviteCode) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                ) {
                    Icon(painterResource(R.drawable.ic_person_add), contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.generate_code))
                }
                if (errorMessageRes != null) {
                    InlineError(
                        message = stringResource(errorMessageRes)
                    )
                }
            }
        }
    }
}

@Composable
private fun GeneratedCodeSection(
    code: String,
    onAction: (SharePetAction) -> Unit
) {
    val context = LocalContext.current

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionHeading(
            title = stringResource(R.string.ready_to_share),
            supportingText = stringResource(R.string.invite_code_shown_once)
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = code,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TextButton(onClick = {
                        onAction(SharePetAction.OnCopyInviteCode(context, code))
                    }) {
                        Icon(painterResource(R.drawable.ic_content_copy), contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.copy_code))
                    }

                    TextButton(onClick = {
                        onAction(SharePetAction.OnShareInviteCode(context, code))
                    }) {
                        Icon(painterResource(R.drawable.ic_share), contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.share_code))
                    }
                }
            }
        }
    }
}

@Composable
private fun ActiveInvitesSection(
    invites: List<PetInvite>,
    errorMessageRes: Int?,
    onAction: (SharePetAction) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionHeading(
            title = stringResource(R.string.active_invites),
            supportingText = pluralStringResource(
                R.plurals.active_invites_count,
                invites.size,
                invites.size
            )
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainerLow
        ) {
            Column {
                if (errorMessageRes != null) {
                    InlineError(message = stringResource(errorMessageRes))
                }

                invites.forEachIndexed { index, invite ->
                    ActiveInviteRow(
                        invite = invite,
                        onAction = onAction
                    )

                    if (index != invites.lastIndex) {
                        HorizontalDivider(modifier = Modifier.padding(start = 16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ActiveInviteRow(
    invite: PetInvite,
    onAction: (SharePetAction) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .padding(start = 16.dp, end = 4.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        RolePill(invite.permissionLevel)

        Spacer(Modifier.weight(1f))

        IconButton(
            onClick = { onAction(SharePetAction.OnRevokeInviteCode(invite.codeHash)) },
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_close),
                contentDescription = stringResource(R.string.revoke_invite),
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun SectionHeading(title: String, supportingText: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = supportingText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun InlineError(message: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { liveRegion = LiveRegionMode.Polite },
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_error),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error
        )

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error
        )
    }
}

@PreviewLightDark
@Composable
private fun SharePetScreenContentPreview(
    @PreviewParameter(SharePetPreviewParameterProvider::class) uiState: SharePetUiState
) {
    PetVitalsTheme {
        SharePetScreenContent(
            uiState = uiState,
            snackbarHostState = SnackbarHostState(),
            onAction = {},
            onPopBackStack = {}
        )
    }
}