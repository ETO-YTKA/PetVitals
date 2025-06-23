package com.example.petvitals.ui.screens.share_pet

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petvitals.R
import com.example.petvitals.data.repository.pet_permission.PermissionLevel
import com.example.petvitals.data.repository.pet_permission.PetPermission
import com.example.petvitals.data.repository.pet_permission.PetPermissionRepository
import com.example.petvitals.data.repository.user.User
import com.example.petvitals.data.repository.user.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
            val currentUser = userRepository.getCurrentUser()

            val userPermissions = petPermissions.mapNotNull { petPermission ->
                val user = userRepository.getUserById(petPermission.userId)

                if (user?.id == currentUser?.id) return@mapNotNull null

                user?.let {
                    UserPermission(
                        user = user,
                        permissionLevel = petPermission.permissionLevel
                    )
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
            val targetUser = userRepository.getUserByEmail(email)
            val currentUser = userRepository.getCurrentUser()

            when {
                targetUser == null -> {
                    _uiState.update { state ->
                        state.copy(
                            shareErrorMessage = context.getString(R.string.user_does_not_exist_error),
                            isLoading = false
                        )
                    }
                }
                //Share with yourself
                currentUser?.email == email -> {
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
                        Log.d("SharePetViewModel", "onShareClick: $e")
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