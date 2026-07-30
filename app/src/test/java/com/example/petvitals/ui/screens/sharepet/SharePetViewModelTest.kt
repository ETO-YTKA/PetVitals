package com.example.petvitals.ui.screens.sharepet

import android.content.ContextWrapper
import com.example.petvitals.R
import com.example.petvitals.data.service.account.AccountService
import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.AccountError
import com.example.petvitals.domain.error.FirestoreError
import com.example.petvitals.domain.models.Member
import com.example.petvitals.domain.models.PermissionLevel
import com.example.petvitals.domain.models.User
import com.example.petvitals.domain.repository.PetMemberRepository
import com.example.petvitals.domain.repository.UserRepository
import com.example.petvitals.domain.usecase.GetPetPermissionUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SharePetViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun getPetPermissions_asViewer_deniesAccessWithoutLoadingMembers() = runTest(dispatcher) {
        val petMemberRepository = FakePetMemberRepository()
        val viewModel = SharePetViewModel(
            petMemberRepository = petMemberRepository,
            userRepository = FakeUserRepository(),
            accountService = FakeAccountService(),
            getPetPermission = FakeGetPetPermissionUseCase(PermissionLevel.VIEWER),
            context = ContextWrapper(null)
        )

        viewModel.getPetPermissions(PET_ID)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.hasOwnerPermission)
        assertEquals(
            R.string.pet_sharing_access_denied,
            viewModel.uiState.value.permissionErrorMessageRes
        )
        assertEquals(0, petMemberRepository.getMembersCalls)
    }

    @Test
    fun onShareClick_afterOwnerIsDemoted_rejectsMutation() = runTest(dispatcher) {
        val petMemberRepository = FakePetMemberRepository()
        val permissionUseCase = FakeGetPetPermissionUseCase(PermissionLevel.OWNER)
        val viewModel = SharePetViewModel(
            petMemberRepository = petMemberRepository,
            userRepository = FakeUserRepository(
                userByEmail = User(id = TARGET_USER_ID, email = TARGET_EMAIL)
            ),
            accountService = FakeAccountService(),
            getPetPermission = permissionUseCase,
            context = ContextWrapper(null)
        )
        viewModel.getPetPermissions(PET_ID)
        advanceUntilIdle()
        viewModel.onEmailChange(TARGET_EMAIL)
        permissionUseCase.permissionLevel = PermissionLevel.VIEWER

        viewModel.onShareClick()
        advanceUntilIdle()

        assertEquals(0, petMemberRepository.saveCalls)
        assertFalse(viewModel.uiState.value.hasOwnerPermission)
        assertEquals(
            R.string.pet_sharing_access_denied,
            viewModel.uiState.value.permissionErrorMessageRes
        )
    }

    private class FakeGetPetPermissionUseCase(
        var permissionLevel: PermissionLevel
    ) : GetPetPermissionUseCase {
        override suspend fun invoke(petId: String): AppResult<FirestoreError, PermissionLevel> =
            AppResult.Success(permissionLevel)
    }

    private class FakePetMemberRepository : PetMemberRepository {
        var getMembersCalls = 0
        var saveCalls = 0

        override suspend fun getPetMembers(
            petId: String
        ): AppResult<FirestoreError, List<Member>> {
            getMembersCalls++
            return AppResult.Success(emptyList())
        }
        override suspend fun getPetRole(
            petId: String,
            userId: String
        ): AppResult<FirestoreError, PermissionLevel?> = AppResult.Success(null)
        override suspend fun savePetMember(
            petId: String,
            member: Member
        ): AppResult<FirestoreError, Unit> {
            saveCalls++
            return AppResult.Success(Unit)
        }
        override suspend fun deletePetMember(
            petId: String,
            userId: String
        ): AppResult<FirestoreError, Unit> = AppResult.Success(Unit)
    }

    private class FakeUserRepository(
        private val userByEmail: User? = null
    ) : UserRepository {
        override suspend fun saveUser(user: User): AppResult<FirestoreError, Unit> =
            AppResult.Success(Unit)
        override suspend fun getUserById(userId: String): AppResult<FirestoreError, User?> =
            AppResult.Success(null)
        override suspend fun getUserByEmail(email: String): AppResult<FirestoreError, User?> =
            AppResult.Success(userByEmail)
        override suspend fun deleteUser(userId: String): AppResult<FirestoreError, Unit> =
            AppResult.Success(Unit)
    }

    private class FakeAccountService : AccountService {
        override val currentUser: Flow<User?> = emptyFlow()
        override val currentUserId: String = USER_ID
        override val isEmailVerified: Boolean = true
        override val currentUserEmail: String? = null

        override fun hasUser(): Boolean = true
        override suspend fun signIn(email: String, password: String): AppResult<AccountError, Unit> =
            AppResult.Success(Unit)
        override suspend fun signUp(email: String, password: String): AppResult<AccountError, String> =
            AppResult.Success(USER_ID)
        override fun logout() = Unit
        override suspend fun deleteAccount(): AppResult<AccountError, Unit> = AppResult.Success(Unit)
        override suspend fun sendVerificationEmail(): AppResult<AccountError, Unit> = AppResult.Success(Unit)
        override suspend fun sendPasswordResetEmail(
            email: String
        ): AppResult<AccountError, Unit> = AppResult.Success(Unit)
    }

    private companion object {
        const val PET_ID = "pet-id"
        const val USER_ID = "user-id"
        const val TARGET_USER_ID = "target-user-id"
        const val TARGET_EMAIL = "target@example.com"
    }
}
