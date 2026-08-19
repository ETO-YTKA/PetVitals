package com.example.petvitals.ui.screens.sharepet

import com.example.petvitals.R
import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.FirestoreError
import com.example.petvitals.domain.error.PetInviteError
import com.example.petvitals.domain.models.CreatedPetInvite
import com.example.petvitals.domain.models.Member
import com.example.petvitals.domain.models.PermissionLevel
import com.example.petvitals.domain.models.PetInvite
import com.example.petvitals.domain.models.User
import com.example.petvitals.domain.repository.PetInviteRepository
import com.example.petvitals.domain.repository.PetMemberRepository
import com.example.petvitals.domain.usecase.CreateInviteCodeUseCase
import com.example.petvitals.domain.usecase.GetPetPermissionUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
    fun getInitialData_asOwner_loadsSortedMembersAndInviteCodes() = runTest(dispatcher) {
        val memberRepository = FakePetMemberRepository(
            members = mutableListOf(EDITOR, OWNER, VIEWER)
        )
        val inviteRepository = FakePetInviteRepository(codes = listOf(EDITOR_INVITE))
        val viewModel = createViewModel(memberRepository, inviteRepository)

        viewModel.getInitialData(PET_ID)
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.permissionErrorMessageRes)
        assertEquals(listOf(OWNER, EDITOR, VIEWER), viewModel.uiState.value.petMembers)
        assertEquals(listOf(EDITOR_INVITE), viewModel.uiState.value.activeInvites)
        assertEquals(1, memberRepository.getMembersCalls)
        assertEquals(1, inviteRepository.getCodesCalls)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun getInitialData_asViewer_deniesAccessWithoutLoadingData() = runTest(dispatcher) {
        val memberRepository = FakePetMemberRepository()
        val inviteRepository = FakePetInviteRepository()
        val viewModel = createViewModel(
            memberRepository = memberRepository,
            inviteRepository = inviteRepository,
            permission = FakeGetPetPermissionUseCase(PermissionLevel.VIEWER)
        )

        viewModel.getInitialData(PET_ID)
        advanceUntilIdle()

        assertEquals(
            R.string.pet_sharing_access_denied,
            viewModel.uiState.value.permissionErrorMessageRes
        )
        assertEquals(0, memberRepository.getMembersCalls)
        assertEquals(0, inviteRepository.getCodesCalls)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun getInitialData_whenInviteLoadFails_preservesMembersAndShowsError() = runTest(dispatcher) {
        val viewModel = createViewModel(
            memberRepository = FakePetMemberRepository(mutableListOf(OWNER)),
            inviteRepository = FakePetInviteRepository(
                getCodesResult = AppResult.Failure(PetInviteError.Network)
            )
        )

        viewModel.getInitialData(PET_ID)
        advanceUntilIdle()

        assertEquals(listOf(OWNER), viewModel.uiState.value.petMembers)
        assertTrue(viewModel.uiState.value.activeInvites.isEmpty())
        assertEquals(R.string.network_error, viewModel.uiState.value.invitesErrorMessageRes)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun selectInvitePermission_acceptsEditorAndViewerButNotOwner() {
        val viewModel = createViewModel()

        viewModel.onAction(SharePetAction.OnSelectInvitePermission(PermissionLevel.EDITOR))
        assertEquals(PermissionLevel.EDITOR, viewModel.uiState.value.selectedInvitePermission)

        viewModel.onAction(SharePetAction.OnSelectInvitePermission(PermissionLevel.OWNER))
        assertEquals(PermissionLevel.EDITOR, viewModel.uiState.value.selectedInvitePermission)
    }

    @Test
    fun createInviteCode_onSuccess_showsRawCodeAndAddsInvite() = runTest(dispatcher) {
        val createInvite = FakeCreateInviteCodeUseCase(
            result = AppResult.Success(CreatedPetInvite(RAW_CODE, EDITOR_INVITE))
        )
        val viewModel = createViewModel(createInviteCode = createInvite)
        viewModel.getInitialData(PET_ID)
        advanceUntilIdle()
        viewModel.onAction(SharePetAction.OnSelectInvitePermission(PermissionLevel.EDITOR))

        viewModel.onAction(SharePetAction.OnCreateInviteCode)
        advanceUntilIdle()

        assertEquals(listOf(PET_ID to PermissionLevel.EDITOR), createInvite.calls)
        assertEquals(RAW_CODE, viewModel.uiState.value.latestGeneratedCode)
        assertEquals(listOf(EDITOR_INVITE), viewModel.uiState.value.activeInvites)
        assertNull(viewModel.uiState.value.createInviteErrorMessageRes)
    }

    @Test
    fun createInviteCode_onFailure_preservesCodesAndShowsError() = runTest(dispatcher) {
        val createInvite = FakeCreateInviteCodeUseCase(
            result = AppResult.Failure(PetInviteError.Network)
        )
        val inviteRepository = FakePetInviteRepository(codes = listOf(VIEWER_INVITE))
        val viewModel = createViewModel(
            inviteRepository = inviteRepository,
            createInviteCode = createInvite
        )
        viewModel.getInitialData(PET_ID)
        advanceUntilIdle()

        viewModel.onAction(SharePetAction.OnCreateInviteCode)
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.latestGeneratedCode)
        assertEquals(listOf(VIEWER_INVITE), viewModel.uiState.value.activeInvites)
        assertEquals(R.string.network_error, viewModel.uiState.value.createInviteErrorMessageRes)
    }

    @Test
    fun revokeInviteCode_onSuccess_removesOnlyRevokedInvite() = runTest(dispatcher) {
        val inviteRepository = FakePetInviteRepository(
            codes = listOf(EDITOR_INVITE, VIEWER_INVITE)
        )
        val viewModel = createViewModel(inviteRepository = inviteRepository)
        viewModel.getInitialData(PET_ID)
        advanceUntilIdle()

        viewModel.onAction(SharePetAction.OnRevokeInviteCode(EDITOR_INVITE.codeHash))
        advanceUntilIdle()

        assertEquals(listOf(EDITOR_INVITE.codeHash), inviteRepository.revokedCodeIds)
        assertEquals(listOf(VIEWER_INVITE), viewModel.uiState.value.activeInvites)
        assertNull(viewModel.uiState.value.invitesErrorMessageRes)
    }

    @Test
    fun revokeInviteCode_onFailure_preservesInvitesAndShowsError() = runTest(dispatcher) {
        val inviteRepository = FakePetInviteRepository(
            codes = listOf(EDITOR_INVITE),
            revokeResult = AppResult.Failure(PetInviteError.PermissionDenied)
        )
        val viewModel = createViewModel(inviteRepository = inviteRepository)
        viewModel.getInitialData(PET_ID)
        advanceUntilIdle()

        viewModel.onAction(SharePetAction.OnRevokeInviteCode(EDITOR_INVITE.codeHash))
        advanceUntilIdle()

        assertEquals(listOf(EDITOR_INVITE), viewModel.uiState.value.activeInvites)
        assertEquals(
            R.string.something_went_wrong_error,
            viewModel.uiState.value.invitesErrorMessageRes
        )
    }

    @Test
    fun removeOwner_isRejectedWithoutRepositoryMutation() = runTest(dispatcher) {
        val memberRepository = FakePetMemberRepository(mutableListOf(OWNER, EDITOR))
        val viewModel = createViewModel(memberRepository = memberRepository)
        viewModel.getInitialData(PET_ID)
        advanceUntilIdle()

        viewModel.onAction(SharePetAction.OnRemoveMember(OWNER.userId))
        advanceUntilIdle()

        assertEquals(0, memberRepository.deleteCalls)
        assertEquals(listOf(OWNER, EDITOR), viewModel.uiState.value.petMembers)
    }

    @Test
    fun removeNonOwner_afterOwnerLoad_removesAfterRepositorySuccess() = runTest(dispatcher) {
        val memberRepository = FakePetMemberRepository(mutableListOf(OWNER, EDITOR))
        val permission = FakeGetPetPermissionUseCase(PermissionLevel.OWNER)
        val viewModel = createViewModel(memberRepository = memberRepository, permission = permission)
        viewModel.getInitialData(PET_ID)
        advanceUntilIdle()

        viewModel.onAction(SharePetAction.OnRemoveMember(EDITOR.userId))
        advanceUntilIdle()

        assertEquals(listOf(EDITOR.userId), memberRepository.deletedUserIds)
        assertEquals(listOf(OWNER), viewModel.uiState.value.petMembers)
        assertNull(viewModel.uiState.value.removingMemberId)
    }

    private fun createViewModel(
        memberRepository: PetMemberRepository = FakePetMemberRepository(),
        inviteRepository: PetInviteRepository = FakePetInviteRepository(),
        permission: FakeGetPetPermissionUseCase = FakeGetPetPermissionUseCase(
            PermissionLevel.OWNER
        ),
        createInviteCode: CreateInviteCodeUseCase = FakeCreateInviteCodeUseCase()
    ) = SharePetViewModel(
        petMemberRepository = memberRepository,
        petInviteRepository = inviteRepository,
        getPetPermission = permission,
        createInviteCode = createInviteCode
    )

    private class FakeGetPetPermissionUseCase(
        var permissionLevel: PermissionLevel
    ) : GetPetPermissionUseCase {
        var calls = 0

        override suspend fun invoke(petId: String): AppResult<FirestoreError, PermissionLevel> {
            calls++
            return AppResult.Success(permissionLevel)
        }
    }

    private class FakePetMemberRepository(
        val members: MutableList<Member> = mutableListOf()
    ) : PetMemberRepository {
        var getMembersCalls = 0
        var deleteCalls = 0
        val deletedUserIds = mutableListOf<String>()

        override suspend fun getPetMembers(petId: String): AppResult<FirestoreError, List<Member>> {
            getMembersCalls++
            return AppResult.Success(members.toList())
        }

        override suspend fun getPetRole(
            petId: String,
            userId: String
        ): AppResult<FirestoreError, PermissionLevel?> = AppResult.Success(null)

        override suspend fun savePetMember(
            petId: String,
            member: Member
        ): AppResult<FirestoreError, Unit> = AppResult.Success(Unit)

        override suspend fun deletePetMember(
            petId: String,
            userId: String
        ): AppResult<FirestoreError, Unit> {
            deleteCalls++
            deletedUserIds += userId
            members.removeAll { it.userId == userId }
            return AppResult.Success(Unit)
        }
    }

    private class FakePetInviteRepository(
        codes: List<PetInvite> = emptyList(),
        private val getCodesResult: AppResult<PetInviteError, List<PetInvite>> =
            AppResult.Success(codes),
        private val revokeResult: AppResult<PetInviteError, Unit> = AppResult.Success(Unit)
    ) : PetInviteRepository {
        var getCodesCalls = 0
        val revokedCodeIds = mutableListOf<String>()

        override suspend fun createCode(invite: PetInvite): AppResult<PetInviteError, Unit> =
            AppResult.Success(Unit)

        override suspend fun redeemCode(
            rawCode: String,
            user: User
        ): AppResult<PetInviteError, Unit> = AppResult.Success(Unit)

        override suspend fun revokeCode(code: String): AppResult<PetInviteError, Unit> {
            revokedCodeIds += code
            return revokeResult
        }

        override suspend fun getCodes(
            petId: String
        ): AppResult<PetInviteError, List<PetInvite>> {
            getCodesCalls++
            return getCodesResult
        }
    }

    private class FakeCreateInviteCodeUseCase(
        private val result: AppResult<PetInviteError, CreatedPetInvite> = AppResult.Success(
            CreatedPetInvite(RAW_CODE, VIEWER_INVITE)
        )
    ) : CreateInviteCodeUseCase {
        val calls = mutableListOf<Pair<String, PermissionLevel>>()

        override suspend fun invoke(
            petId: String,
            permissionLevel: PermissionLevel
        ): AppResult<PetInviteError, CreatedPetInvite> {
            calls += petId to permissionLevel
            return result
        }
    }

    private companion object {
        const val PET_ID = "pet-id"
        const val RAW_CODE = "ABCD-EFGH-JKLM-NPQR"
        val OWNER = Member("owner-id", "Morgan", PermissionLevel.OWNER)
        val EDITOR = Member("editor-id", "Avery", PermissionLevel.EDITOR)
        val VIEWER = Member("viewer-id", "Riley", PermissionLevel.VIEWER)
        val EDITOR_INVITE = PetInvite("editor-hash", PET_ID, PermissionLevel.EDITOR)
        val VIEWER_INVITE = PetInvite("viewer-hash", PET_ID, PermissionLevel.VIEWER)
    }
}
