package com.example.petvitals.data.usecase

import com.example.petvitals.data.service.account.AccountService
import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.AccountError
import com.example.petvitals.domain.error.FirestoreError
import com.example.petvitals.domain.error.PetInviteError
import com.example.petvitals.domain.models.PetInvite
import com.example.petvitals.domain.models.User
import com.example.petvitals.domain.repository.PetInviteRepository
import com.example.petvitals.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertSame
import org.junit.Test

class RedeemCodeUseCaseImplTest {

    @Test
    fun invoke_withoutAuthenticatedUser_returnsUnauthenticated() = runTest {
        val useCase = createUseCase(currentUserId = null)

        val result = useCase("ABCD1234EFGH5678")

        assertSame(PetInviteError.Unauthenticated, (result as AppResult.Failure).error)
    }

    @Test
    fun invoke_whenProfileLoadFails_mapsStorageFailure() = runTest {
        val useCase = createUseCase(
            userResult = AppResult.Failure(FirestoreError.Network)
        )

        val result = useCase("ABCD1234EFGH5678")

        assertSame(PetInviteError.Network, (result as AppResult.Failure).error)
    }

    @Test
    fun invoke_whenRepositoryRejectsInvite_preservesInviteError() = runTest {
        val useCase = createUseCase(
            repositoryResult = AppResult.Failure(PetInviteError.InviteUnavailable)
        )

        val result = useCase("ABCD1234EFGH5678")

        assertSame(PetInviteError.InviteUnavailable, (result as AppResult.Failure).error)
    }

    private fun createUseCase(
        currentUserId: String? = USER.id,
        userResult: AppResult<FirestoreError, User?> = AppResult.Success(USER),
        repositoryResult: AppResult<PetInviteError, Unit> = AppResult.Success(Unit)
    ) = RedeemCodeUseCaseImpl(
        petInviteRepository = FakePetInviteRepository(repositoryResult),
        userRepository = FakeUserRepository(userResult),
        accountService = FakeAccountService(currentUserId)
    )

    private class FakePetInviteRepository(
        private val result: AppResult<PetInviteError, Unit>
    ) : PetInviteRepository {
        override suspend fun createCode(invite: PetInvite): AppResult<PetInviteError, Unit> =
            AppResult.Success(Unit)

        override suspend fun redeemCode(
            rawCode: String,
            user: User
        ): AppResult<PetInviteError, Unit> = result

        override suspend fun revokeCode(code: String): AppResult<PetInviteError, Unit> =
            AppResult.Success(Unit)

        override suspend fun getCodes(
            petId: String
        ): AppResult<PetInviteError, List<PetInvite>> = AppResult.Success(emptyList())
    }

    private class FakeUserRepository(
        private val result: AppResult<FirestoreError, User?>
    ) : UserRepository {
        override suspend fun saveUser(user: User): AppResult<FirestoreError, Unit> =
            AppResult.Success(Unit)

        override suspend fun getUserById(userId: String): AppResult<FirestoreError, User?> =
            result

        override suspend fun getUserByEmail(email: String): AppResult<FirestoreError, User?> =
            AppResult.Success(null)

        override suspend fun deleteUser(userId: String): AppResult<FirestoreError, Unit> =
            AppResult.Success(Unit)
    }

    private class FakeAccountService(
        override val currentUserId: String?
    ) : AccountService {
        override val currentUser: Flow<User?> = flowOf(null)
        override val isEmailVerified: Boolean = true
        override val currentUserEmail: String? = null

        override fun hasUser(): Boolean = currentUserId != null

        override suspend fun signIn(
            email: String,
            password: String
        ): AppResult<AccountError, Unit> = AppResult.Success(Unit)

        override suspend fun signUp(
            email: String,
            password: String
        ): AppResult<AccountError, String> = AppResult.Success(USER.id)

        override fun logout() = Unit

        override suspend fun deleteAccount(): AppResult<AccountError, Unit> =
            AppResult.Success(Unit)

        override suspend fun sendVerificationEmail(): AppResult<AccountError, Unit> =
            AppResult.Success(Unit)

        override suspend fun sendPasswordResetEmail(
            email: String
        ): AppResult<AccountError, Unit> = AppResult.Success(Unit)
    }

    private companion object {
        val USER = User(id = "user-id", username = "Taylor", email = "taylor@example.com")
    }
}
