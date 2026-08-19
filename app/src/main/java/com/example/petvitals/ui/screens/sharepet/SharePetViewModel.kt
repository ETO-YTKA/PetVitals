package com.example.petvitals.ui.screens.sharepet

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PersistableBundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petvitals.R
import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.models.Member
import com.example.petvitals.domain.models.PermissionLevel
import com.example.petvitals.domain.models.canDeletePet
import com.example.petvitals.domain.repository.PetInviteRepository
import com.example.petvitals.domain.repository.PetMemberRepository
import com.example.petvitals.domain.usecase.CreateInviteCodeUseCase
import com.example.petvitals.domain.usecase.GetPetPermissionUseCase
import com.example.petvitals.ui.utils.toMessageRes
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class SharePetViewModel @Inject constructor(
    private val petMemberRepository: PetMemberRepository,
    private val petInviteRepository: PetInviteRepository,
    private val getPetPermission: GetPetPermissionUseCase,
    private val createInviteCode: CreateInviteCodeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SharePetUiState())
    val uiState = _uiState.asStateFlow()

    fun onAction(action: SharePetAction) {
        when (action) {
            is SharePetAction.OnSelectInvitePermission -> onSelectInvitePermissionLevel(
                action.permissionLevel
            )
            is SharePetAction.OnRemoveMember -> onRemoveMember(action.userId)
            is SharePetAction.OnCreateInviteCode -> onCreateInviteCode()
            is SharePetAction.OnRevokeInviteCode -> onRevokeInviteCode(action.codeId)
            is SharePetAction.OnCopyInviteCode -> onCopyInviteCode(action.context, action.code)
            is SharePetAction.OnShareInviteCode -> onShareInviteCode(action.context, action.code)
        }
    }

    fun getInitialData(petId: String) {
        _uiState.update { state ->
            state.copy(
                isLoading = true,
                petId = petId,
                petMembers = emptyList(),
                removingMemberId = null,
                membersErrorMessageRes = null,
                permissionErrorMessageRes = null
            )
        }

        viewModelScope.launch {
            val permissionResult = getPetPermission(petId)
            val isOwner = permissionResult is AppResult.Success &&
                permissionResult.data.canDeletePet

            if (!isOwner) {
                val errorMessageRes = when (permissionResult) {
                    is AppResult.Success -> R.string.pet_sharing_access_denied
                    is AppResult.Failure -> permissionResult.error.toMessageRes()
                }

                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        permissionErrorMessageRes = errorMessageRes
                    )
                }
                return@launch
            }

            when (val result = petMemberRepository.getPetMembers(petId)) {
                is AppResult.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            petMembers = result.data.sortedWith(
                                compareBy<Member> {
                                    it.permissionLevel != PermissionLevel.OWNER
                                }.thenBy { it.displayName.lowercase() }
                            )
                        )
                    }
                }
                is AppResult.Failure -> {
                    _uiState.update { state ->
                        state.copy(
                            membersErrorMessageRes = result.error.toMessageRes(),
                            isLoading = false
                        )
                    }
                }
            }

            when (val result = petInviteRepository.getCodes(petId)) {
                is AppResult.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            activeInvites = result.data,
                            isLoading = false
                        )
                    }
                }
                is AppResult.Failure -> {
                    _uiState.update { state ->
                        state.copy(
                            invitesErrorMessageRes = result.error.toMessageRes(),
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    private fun onSelectInvitePermissionLevel(permissionLevel: PermissionLevel) {
        if (permissionLevel == PermissionLevel.OWNER) return
        _uiState.update { state ->
            state.copy(selectedInvitePermission = permissionLevel)
        }
    }

    private fun onRemoveMember(userId: String) {
        val currentState = _uiState.value
        val member = currentState.petMembers.firstOrNull { it.userId == userId } ?: return
        if (member.permissionLevel == PermissionLevel.OWNER) return

        viewModelScope.launch {
            when (val result = petMemberRepository.deletePetMember(currentState.petId, userId)) {
                is AppResult.Failure -> {
                    _uiState.update { state ->
                        state.copy(
                            membersErrorMessageRes = result.error.toMessageRes()
                        )
                    }
                }
                is AppResult.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            petMembers = state.petMembers.filter { it.userId != userId }
                        )
                    }
                }
            }
        }
    }

    private fun onRevokeInviteCode(inviteId: String) {
        viewModelScope.launch {

            when (val result = petInviteRepository.revokeCode(inviteId)) {
                is AppResult.Failure -> {
                    _uiState.update { state ->
                        state.copy(
                            invitesErrorMessageRes = result.error.toMessageRes()
                        )
                    }
                }
                is AppResult.Success ->  {
                    _uiState.update { state ->
                        state.copy(
                            activeInvites = state.activeInvites.filter { it.codeHash != inviteId }
                        )
                    }
                }
            }
        }
    }

    private fun onCreateInviteCode() {
        viewModelScope.launch {
            val result = createInviteCode(
                uiState.value.petId,
                uiState.value.selectedInvitePermission
            )

            when (result) {
                is AppResult.Failure -> {
                    _uiState.update { state ->
                        state.copy(
                            createInviteErrorMessageRes = result.error.toMessageRes()
                        )
                    }
                }
                is AppResult.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            latestGeneratedCode = result.data.code,
                            activeInvites = state.activeInvites + result.data.petInvite
                        )
                    }
                }
            }
        }
    }

    private fun onCopyInviteCode(context: Context, code: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(context.getString(R.string.invite_code), code)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            clip.description.extras = PersistableBundle().apply {
                putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, true)
            }
        }
        clipboard.setPrimaryClip(clip)
    }

    private fun onShareInviteCode(context: Context, code: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, code)
        }
        context.startActivity(
            Intent.createChooser(intent, context.getString(R.string.share_invite_code))
        )
    }
}
