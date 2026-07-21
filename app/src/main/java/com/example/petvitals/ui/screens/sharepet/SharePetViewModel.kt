package com.example.petvitals.ui.screens.sharepet

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petvitals.R
import com.example.petvitals.data.service.account.AccountService
import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.models.Member
import com.example.petvitals.domain.models.PermissionLevel
import com.example.petvitals.domain.models.User
import com.example.petvitals.domain.models.canDeletePet
import com.example.petvitals.domain.repository.PetMemberRepository
import com.example.petvitals.domain.repository.UserRepository
import com.example.petvitals.domain.usecase.GetPetPermissionUseCase
import com.example.petvitals.ui.utils.toMessageRes
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SharePetUiState(
    val isLoading: Boolean = false,
    val hasOwnerPermission: Boolean = false,
    val permissionErrorMessageRes: Int? = null,

    val petId: String = "",
    val email: String = "",
    val permissionLevel: PermissionLevel = PermissionLevel.VIEWER,
    val userPermissions: List<UserPermission> = emptyList(),

    val shareErrorMessage: String? = null
)

data class UserPermission(
    val user: User,
    val permissionLevel: PermissionLevel
)

@HiltViewModel
class SharePetViewModel @Inject constructor(
    private val petMemberRepository: PetMemberRepository,
    private val userRepository: UserRepository,
    private val accountService: AccountService,
    private val getPetPermission: GetPetPermissionUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _uiState = MutableStateFlow(SharePetUiState())
    val uiState = _uiState.asStateFlow()

    fun getPetPermissions(petId: String) {
        _uiState.update { state ->
            state.copy(isLoading = true)
        }

        viewModelScope.launch {
            if (!verifyOwnerPermission(petId)) return@launch

            val petMembers = when (val result = petMemberRepository.getPetMembers(petId)) {
                is AppResult.Success -> result.data
                is AppResult.Failure -> {
                    _uiState.update { state ->
                        state.copy(
                            shareErrorMessage = context.getString(R.string.something_went_wrong_error),
                            isLoading = false
                        )
                    }
                    return@launch
                }
            }
            val currentUserId = accountService.currentUserId ?: run {
                _uiState.update { state ->
                    state.copy(
                        shareErrorMessage = context.getString(R.string.something_went_wrong_error),
                        isLoading = false
                    )
                }
                return@launch
            }

            val userPermissions = petMembers.mapNotNull { member ->

                val result = userRepository.getUserById(member.userId)

                when (result) {
                    is AppResult.Success -> {
                        if (result.data?.id == currentUserId) return@mapNotNull null

                        UserPermission(
                            user = result.data ?: return@mapNotNull null,
                            permissionLevel = member.permissionLevel
                        )
                    }
                    is AppResult.Failure -> {
                        null //TODO: Handle failure
                    }
                }
            }

            _uiState.update { state ->
                state.copy(
                    userPermissions = userPermissions,
                    petId = petId,
                    isLoading = false
                )
            }
        }
    }

    fun onEmailChange(value: String) {
        _uiState.update { state ->
            state.copy(
                email = value,
                shareErrorMessage = null
            )
        }
    }

    fun onPermissionLevelChange(value: PermissionLevel) {
        _uiState.update { state ->
            state.copy(permissionLevel = value)
        }
    }

    fun onShareClick() {
        if (!uiState.value.hasOwnerPermission) return

        _uiState.update { state ->
            state.copy(isLoading = true)
        }

        val email = uiState.value.email
        val petId = uiState.value.petId

        viewModelScope.launch {
            if (!verifyOwnerPermission(petId)) return@launch

            val targetUser = when (val result = userRepository.getUserByEmail(email)) {
                is AppResult.Success -> result.data
                is AppResult.Failure -> {
                    _uiState.update { state ->
                        state.copy(
                            shareErrorMessage = context.getString(R.string.something_went_wrong_error),
                            isLoading = false
                        )
                    }
                    return@launch
                }
            }

            val currentUserId = accountService.currentUserId
            if (currentUserId.isNullOrBlank()) {
                _uiState.update { state ->
                    state.copy(
                        shareErrorMessage = context.getString(R.string.something_went_wrong_error),
                        isLoading = false
                    )
                }
                return@launch
            }

            when {
                //User does not exist
                targetUser == null -> {
                    _uiState.update { state ->
                        state.copy(
                            shareErrorMessage = context.getString(R.string.user_does_not_exist_error),
                            isLoading = false
                        )
                    }
                }
                //Share with yourself
                targetUser.id == currentUserId -> {
                    _uiState.update { state ->
                        state.copy(
                            shareErrorMessage = context.getString(R.string.cannot_share_with_yourself_error),
                            isLoading = false
                        )
                    }
                }
                //Already shared with user
                uiState.value.userPermissions.any { it.user.email == email } -> {
                    _uiState.update { state ->
                        state.copy(
                            shareErrorMessage = context.getString(R.string.already_shared_with_user_error),
                            isLoading = false
                        )
                    }
                }
                //Share with user
                else -> {
                    val member = Member(
                        userId = targetUser.id,
                        permissionLevel = uiState.value.permissionLevel
                    )

                    when (petMemberRepository.savePetMember(petId, member)) {
                        is AppResult.Success -> {
                            getPetPermissions(petId)
                            Toast.makeText(
                                context,
                                context.getString(R.string.shared_with_user),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        is AppResult.Failure -> {
                            _uiState.update { state ->
                                state.copy(
                                    shareErrorMessage = context.getString(R.string.something_went_wrong_error),
                                    isLoading = false
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    fun onDeleteAccessClick(petId: String, userId: String) {
        if (!uiState.value.hasOwnerPermission || petId != uiState.value.petId) return

        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(isLoading = true)
            }

            if (!verifyOwnerPermission(petId)) return@launch

            when (petMemberRepository.deletePetMember(petId, userId)) {
                is AppResult.Success -> getPetPermissions(petId)
                is AppResult.Failure -> _uiState.update { state ->
                    state.copy(
                        shareErrorMessage = context.getString(R.string.something_went_wrong_error),
                        isLoading = false
                    )
                }
            }
        }
    }

    private suspend fun verifyOwnerPermission(petId: String): Boolean {
        val permissionResult = getPetPermission(petId)
        val isOwner = permissionResult is AppResult.Success &&
                permissionResult.data.canDeletePet
        if (isOwner) {
            _uiState.update { state ->
                state.copy(
                    hasOwnerPermission = true,
                    permissionErrorMessageRes = null
                )
            }
            return true
        }

        val errorMessageRes = when (permissionResult) {
            is AppResult.Success -> R.string.pet_sharing_access_denied
            is AppResult.Failure -> permissionResult.error.toMessageRes()
        }
        _uiState.update { state ->
            state.copy(
                isLoading = false,
                hasOwnerPermission = false,
                permissionErrorMessageRes = errorMessageRes
            )
        }
        return false
    }
}
