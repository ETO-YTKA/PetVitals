package com.example.petvitals.data.usecase

import com.example.petvitals.data.service.account.AccountService
import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.AccountError
import com.example.petvitals.domain.error.FirestoreError
import com.example.petvitals.domain.models.Member
import com.example.petvitals.domain.models.PermissionLevel
import com.example.petvitals.domain.models.User
import com.example.petvitals.domain.repository.PetMemberRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class GetPetPermissionUseCaseImplTest {

    @Test
    fun invoke_whenMembershipExists_returnsPermission() = runBlocking {
        PermissionLevel.entries.forEach { permissionLevel ->
            val useCase = GetPetPermissionUseCaseImpl(
                petMemberRepository = FakePetMemberRepository(
                    result = AppResult.Success(permissionLevel)
                ),
                accountService = FakeAccountService(USER_ID)
            )

            assertEquals(
                AppResult.Success(permissionLevel),
                useCase(PET_ID)
            )
        }
    }

    @Test
    fun invoke_whenMembershipIsMissing_returnsPermissionDenied() = runBlocking {
        val useCase = GetPetPermissionUseCaseImpl(
            petMemberRepository = FakePetMemberRepository(AppResult.Success(null)),
            accountService = FakeAccountService(USER_ID)
        )

        assertEquals(
            AppResult.Failure(FirestoreError.PermissionDenied),
            useCase(PET_ID)
        )
    }

    @Test
    fun invoke_whenUserIsSignedOut_returnsUnauthenticated() = runBlocking {
        val repository = FakePetMemberRepository(AppResult.Success(PermissionLevel.OWNER))
        val useCase = GetPetPermissionUseCaseImpl(
            petMemberRepository = repository,
            accountService = FakeAccountService(null)
        )

        assertEquals(
            AppResult.Failure(FirestoreError.Unauthenticated),
            useCase(PET_ID)
        )
        assertEquals(0, repository.getRoleCalls)
    }

    @Test
    fun invoke_whenRepositoryFails_returnsFailure() = runBlocking {
        val useCase = GetPetPermissionUseCaseImpl(
            petMemberRepository = FakePetMemberRepository(
                AppResult.Failure(FirestoreError.Network)
            ),
            accountService = FakeAccountService(USER_ID)
        )

        assertEquals(
            AppResult.Failure(FirestoreError.Network),
            useCase(PET_ID)
        )
    }

    private class FakePetMemberRepository(
        private val result: AppResult<FirestoreError, PermissionLevel?>
    ) : PetMemberRepository {
        var getRoleCalls = 0

        override suspend fun getPetRole(
            petId: String,
            userId: String
        ): AppResult<FirestoreError, PermissionLevel?> {
            getRoleCalls++
            return result
        }

        override suspend fun getPetMembers(
            petId: String
        ): AppResult<FirestoreError, List<Member>> = AppResult.Success(emptyList())

        override suspend fun savePetMember(
            petId: String,
            member: Member
        ): AppResult<FirestoreError, Unit> = AppResult.Success(Unit)

        override suspend fun deletePetMember(
            petId: String,
            userId: String
        ): AppResult<FirestoreError, Unit> = AppResult.Success(Unit)
    }

    private class FakeAccountService(
        override val currentUserId: String?
    ) : AccountService {
        override val currentUser: Flow<User?> = emptyFlow()
        override val isEmailVerified: Boolean = true
        override val currentUserEmail: String? = null

        override fun hasUser(): Boolean = currentUserId != null
        override suspend fun signIn(email: String, password: String): AppResult<AccountError, Unit> =
            AppResult.Success(Unit)
        override suspend fun signUp(email: String, password: String): AppResult<AccountError, String> =
            AppResult.Success(currentUserId.orEmpty())
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
    }
}
