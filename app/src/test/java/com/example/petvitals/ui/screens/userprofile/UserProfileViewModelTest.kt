package com.example.petvitals.ui.screens.userprofile

import android.content.ContextWrapper
import com.example.petvitals.R
import com.example.petvitals.data.service.account.AccountService
import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.AccountError
import com.example.petvitals.domain.error.FirestoreError
import com.example.petvitals.domain.models.User
import com.example.petvitals.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UserProfileViewModelTest {

    private val mainDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(mainDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun retryAfterFailure_clearsErrorAndShowsLoadedUser() = runTest(mainDispatcher) {
        val user = User(id = "user-id", username = "Taylor", email = "taylor@example.com")
        val repository = FakeUserRepository(
            ArrayDeque(
                listOf(
                    AppResult.Failure(FirestoreError.Network),
                    AppResult.Success(user)
                )
            )
        )
        val viewModel = createViewModel(repository = repository)

        advanceUntilIdle()
        assertEquals(R.string.network_error, viewModel.uiState.value.errorMessageRes)

        viewModel.onAction(UserProfileAction.Retry)
        assertTrue(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.errorMessageRes)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(user, viewModel.uiState.value.user)
        assertNull(viewModel.uiState.value.errorMessageRes)
    }

    @Test
    fun missingProfile_showsProfileNotFoundInsteadOfSessionExpired() = runTest(mainDispatcher) {
        val viewModel = createViewModel(
            repository = FakeUserRepository(
                ArrayDeque(listOf(AppResult.Success(null)))
            )
        )

        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.user)
        assertEquals(
            R.string.user_profile_not_found_error,
            viewModel.uiState.value.errorMessageRes
        )
    }

    @Test
    fun missingAuthenticatedUser_showsSessionExpiredWithoutRepositoryCall() =
        runTest(mainDispatcher) {
            val repository = FakeUserRepository()
            val viewModel = createViewModel(
                currentUserId = null,
                repository = repository
            )

            advanceUntilIdle()

            assertFalse(viewModel.uiState.value.isLoading)
            assertEquals(R.string.session_expired_error, viewModel.uiState.value.errorMessageRes)
            assertEquals(0, repository.getUserCalls)
        }

    private fun createViewModel(
        currentUserId: String? = "user-id",
        repository: FakeUserRepository
    ): UserProfileViewModel = UserProfileViewModel(
        accountService = FakeAccountService(currentUserId),
        userRepository = repository,
        context = ContextWrapper(null)
    )

    private class FakeAccountService(
        override val currentUserId: String?
    ) : AccountService {
        override val currentUser: Flow<User?> = flowOf(null)
        override val isEmailVerified: Boolean = false
        override val currentUserEmail: String? = null

        override fun hasUser(): Boolean = currentUserId != null
        override suspend fun signIn(
            email: String,
            password: String
        ): AppResult<AccountError, Unit> = AppResult.Success(Unit)

        override suspend fun signUp(
            email: String,
            password: String
        ): AppResult<AccountError, String> = AppResult.Success("user-id")

        override fun logout() = Unit
        override suspend fun deleteAccount(): AppResult<AccountError, Unit> =
            AppResult.Success(Unit)

        override suspend fun sendVerificationEmail(): AppResult<AccountError, Unit> =
            AppResult.Success(Unit)

        override suspend fun sendPasswordResetEmail(
            email: String
        ): AppResult<AccountError, Unit> = AppResult.Success(Unit)
    }

    private class FakeUserRepository(
        private val getUserResults: ArrayDeque<AppResult<FirestoreError, User?>> = ArrayDeque()
    ) : UserRepository {
        var getUserCalls = 0
            private set

        override suspend fun saveUser(user: User): AppResult<FirestoreError, Unit> =
            AppResult.Success(Unit)

        override suspend fun getUserById(userId: String): AppResult<FirestoreError, User?> {
            getUserCalls++
            return getUserResults.removeFirst()
        }

        override suspend fun getUserByEmail(
            email: String
        ): AppResult<FirestoreError, User?> = AppResult.Success(null)

        override suspend fun deleteUser(userId: String): AppResult<FirestoreError, Unit> =
            AppResult.Success(Unit)
    }
}
