package com.example.petvitals.data.usecase

import com.example.petvitals.data.utils.InviteCodeGenerator
import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.FirestoreError
import com.example.petvitals.domain.models.Member
import com.example.petvitals.domain.models.PermissionLevel
import com.example.petvitals.domain.models.PetInvite
import com.example.petvitals.domain.repository.PetInviteRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class CreateInviteCodeUseCaseImplTest {

    @Test
    fun invoke_generatesHashedInviteAndReturnsRawCode() = runTest {
        val repository = FakePetInviteRepository()
        val generator = InviteCodeGenerator()
        val useCase = CreateInviteCodeUseCaseImpl(repository, generator)

        val result = useCase(PET_ID, PermissionLevel.EDITOR)

        assertTrue(result is AppResult.Success)
        val created = (result as AppResult.Success).data
        assertTrue(CODE_PATTERN.matches(created.code))
        assertEquals(PET_ID, created.petInvite.petId)
        assertEquals(PermissionLevel.EDITOR, created.petInvite.permissionLevel)
        assertEquals(64, created.petInvite.codeHash.length)
        assertEquals(listOf(created.petInvite), repository.createdInvites)
        assertEquals(
            generator.hash(generator.normalize(created.code)),
            created.petInvite.codeHash
        )
    }

    @Test
    fun invoke_whenRepositoryFails_propagatesFailure() = runTest {
        val repository = FakePetInviteRepository(
            createResult = AppResult.Failure(FirestoreError.Network)
        )
        val useCase = CreateInviteCodeUseCaseImpl(repository, InviteCodeGenerator())

        val result = useCase(PET_ID, PermissionLevel.VIEWER)

        assertTrue(result is AppResult.Failure)
        assertSame(FirestoreError.Network, (result as AppResult.Failure).error)
    }

    private class FakePetInviteRepository(
        private val createResult: AppResult<FirestoreError, Unit> = AppResult.Success(Unit)
    ) : PetInviteRepository {
        val createdInvites = mutableListOf<PetInvite>()

        override suspend fun createCode(invite: PetInvite): AppResult<FirestoreError, Unit> {
            createdInvites += invite
            return createResult
        }

        override suspend fun redeemCode(
            inviteId: String,
            member: Member
        ): AppResult<FirestoreError, Unit> = AppResult.Success(Unit)

        override suspend fun revokeCode(inviteId: String): AppResult<FirestoreError, Unit> =
            AppResult.Success(Unit)

        override suspend fun getCodes(
            petId: String
        ): AppResult<FirestoreError, List<PetInvite>> = AppResult.Success(emptyList())
    }

    private companion object {
        const val PET_ID = "pet-id"
        val CODE_PATTERN = Regex("^[0-9A-HJKMNP-TV-Z]{4}(-[0-9A-HJKMNP-TV-Z]{4}){3}$")
    }
}
