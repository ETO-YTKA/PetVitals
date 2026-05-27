package com.example.petvitals.ui.screens.sharepet

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petvitals.R
import com.example.petvitals.data.service.account.AccountService
import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.models.PermissionLevel
import com.example.petvitals.domain.models.PetPermission
import com.example.petvitals.domain.models.User
import com.example.petvitals.domain.repository.PetPermissionRepository
import com.example.petvitals.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

data class SharePetUiState(
    val isLoading: Boolean = false,

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
    private val petPermissionRepository: PetPermissionRepository,
    private val userRepository: UserRepository,
    private val accountService: AccountService,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _uiState = MutableStateFlow(SharePetUiState())
    val uiState = _uiState.asStateFlow()

    fun getPetPermissions(petId: String) {
        _uiState.update { state ->
            state.copy(isLoading = true)
        }

        viewModelScope.launch {
            val petPermissions = petPermissionRepository.getUsersByPetId(petId)
            val currentUserId = accountService.currentUserId ?: return@launch

            val userPermissions = petPermissions.mapNotNull { petPermission ->

                val result = userRepository.getUserById(petPermission.userId)

                when (result) {
                    is AppResult.Success -> {
                        if (result.data?.id == currentUserId) return@mapNotNull null

                        UserPermission(
                            user = result.data ?: return@mapNotNull null,
                            permissionLevel = petPermission.permissionLevel
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
        _uiState.update { state ->
            state.copy(isLoading = true)
        }

        val email = uiState.value.email
        val petId = uiState.value.petId

        viewModelScope.launch {
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
                    val petPermission = PetPermission(
                        userId = targetUser.id,
                        petId = petId,
                        permissionLevel = uiState.value.permissionLevel
                    )

                    try {
                        petPermissionRepository.savePetPermission(petPermission)
                        getPetPermissions(petId)

                        Toast.makeText(
                            context,
                            context.getString(R.string.shared_with_user),
                            Toast.LENGTH_SHORT
                        ).show()
                    } catch (e: Exception) {
                        Timber.d("onShareClick: $e")
                        _uiState.update { state ->
                            state.copy(shareErrorMessage = context.getString(R.string.something_went_wrong_error))
                        }
                    }
                }
            }
        }
    }

    fun onDeleteAccessClick(petId: String, userId: String) {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(isLoading = true)
            }

            petPermissionRepository.deletePetPermissionByUserPetIds(petId, userId)
            getPetPermissions(petId)
        }
    }
}