package com.example.petvitals.ui.screens.petprofile

import android.content.ContextWrapper
import com.example.petvitals.R
import com.example.petvitals.domain.AppResult
import com.example.petvitals.domain.error.FirestoreError
import com.example.petvitals.domain.models.Food
import com.example.petvitals.domain.models.Medication
import com.example.petvitals.domain.models.Member
import com.example.petvitals.domain.models.PermissionLevel
import com.example.petvitals.domain.models.Pet
import com.example.petvitals.domain.repository.FoodRepository
import com.example.petvitals.domain.repository.MedicationRepository
import com.example.petvitals.domain.repository.PetRepository
import com.example.petvitals.domain.usecase.GetPetPermissionUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PetProfileViewModelDeletionTest {

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
    fun deleteMedication_whenRepositoryFails_keepsMedication() = runTest(dispatcher) {
        val medicationRepository = FakeMedicationRepository(
            deleteResult = AppResult.Failure(FirestoreError.Network)
        )
        val viewModel = createViewModel(medicationRepository, PermissionLevel.OWNER)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.collect()
        }
        viewModel.onAction(PetProfileAction.LoadPet(PET_ID))
        advanceUntilIdle()

        viewModel.onAction(PetProfileAction.DeleteMedication(MEDICATION))
        advanceUntilIdle()

        assertEquals(listOf(MEDICATION), viewModel.uiState.value.medications)
        assertEquals(1, medicationRepository.deleteCalls)
    }

    @Test
    fun deleteMedication_whenRepositorySucceeds_removesMedication() = runTest(dispatcher) {
        val medicationRepository = FakeMedicationRepository(
            deleteResult = AppResult.Success(Unit)
        )
        val viewModel = createViewModel(medicationRepository, PermissionLevel.EDITOR)
        viewModel.onAction(PetProfileAction.LoadPet(PET_ID))
        advanceUntilIdle()

        viewModel.onAction(PetProfileAction.DeleteMedication(MEDICATION))
        advanceUntilIdle()

        assertEquals(emptyList<Medication>(), viewModel.uiState.value.medications)
        assertEquals(1, medicationRepository.deleteCalls)
    }

    @Test
    fun deleteMedication_asViewer_doesNotCallRepository() = runTest(dispatcher) {
        val medicationRepository = FakeMedicationRepository(
            deleteResult = AppResult.Success(Unit)
        )
        val viewModel = createViewModel(medicationRepository, PermissionLevel.VIEWER)
        viewModel.onAction(PetProfileAction.LoadPet(PET_ID))
        advanceUntilIdle()

        viewModel.onAction(PetProfileAction.DeleteMedication(MEDICATION))
        advanceUntilIdle()

        assertEquals(listOf(MEDICATION), viewModel.uiState.value.medications)
        assertEquals(0, medicationRepository.deleteCalls)
    }

    @Test
    fun loadPet_whenPetIsMissing_stopsLoadingAndShowsError() = runTest(dispatcher) {
        val viewModel = createViewModel(
            medicationRepository = FakeMedicationRepository(AppResult.Success(Unit)),
            permissionLevel = PermissionLevel.OWNER,
            petResult = AppResult.Success(null)
        )

        viewModel.onAction(PetProfileAction.LoadPet(PET_ID))
        advanceUntilIdle()

        assertEquals(false, viewModel.uiState.value.isLoading)
        assertEquals(R.string.pet_not_found_error, viewModel.uiState.value.loadErrorMessageRes)
    }

    @Test
    fun deleteFood_whenRepositoryFails_keepsFood() = runTest(dispatcher) {
        val foodRepository = FakeFoodRepository(
            food = listOf(FOOD),
            deleteResult = AppResult.Failure(FirestoreError.Network)
        )
        val viewModel = createViewModel(
            medicationRepository = FakeMedicationRepository(AppResult.Success(Unit)),
            permissionLevel = PermissionLevel.EDITOR,
            foodRepository = foodRepository
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.collect()
        }
        viewModel.onAction(PetProfileAction.LoadPet(PET_ID))
        advanceUntilIdle()

        viewModel.onAction(PetProfileAction.DeleteFood(FOOD))
        advanceUntilIdle()

        assertEquals(listOf(FOOD), viewModel.uiState.value.food)
        assertEquals(1, foodRepository.deleteCalls)
    }

    @Test
    fun deleteFood_whenRepositorySucceeds_removesFood() = runTest(dispatcher) {
        val foodRepository = FakeFoodRepository(food = listOf(FOOD))
        val viewModel = createViewModel(
            medicationRepository = FakeMedicationRepository(AppResult.Success(Unit)),
            permissionLevel = PermissionLevel.EDITOR,
            foodRepository = foodRepository
        )
        viewModel.onAction(PetProfileAction.LoadPet(PET_ID))
        advanceUntilIdle()

        viewModel.onAction(PetProfileAction.DeleteFood(FOOD))
        advanceUntilIdle()

        assertEquals(emptyList<Food>(), viewModel.uiState.value.food)
        assertEquals(1, foodRepository.deleteCalls)
    }

    @Test
    fun deleteFood_asViewer_doesNotCallRepository() = runTest(dispatcher) {
        val foodRepository = FakeFoodRepository(food = listOf(FOOD))
        val viewModel = createViewModel(
            medicationRepository = FakeMedicationRepository(AppResult.Success(Unit)),
            permissionLevel = PermissionLevel.VIEWER,
            foodRepository = foodRepository
        )
        viewModel.onAction(PetProfileAction.LoadPet(PET_ID))
        advanceUntilIdle()

        viewModel.onAction(PetProfileAction.DeleteFood(FOOD))
        advanceUntilIdle()

        assertEquals(listOf(FOOD), viewModel.uiState.value.food)
        assertEquals(0, foodRepository.deleteCalls)
    }

    @Test
    fun deleteMedication_forAnotherPet_doesNotCallRepository() = runTest(dispatcher) {
        val medicationRepository = FakeMedicationRepository(AppResult.Success(Unit))
        val viewModel = createViewModel(medicationRepository, PermissionLevel.OWNER)
        viewModel.onAction(PetProfileAction.LoadPet(PET_ID))
        advanceUntilIdle()

        viewModel.onAction(
            PetProfileAction.DeleteMedication(MEDICATION.copy(petId = "another-pet"))
        )
        advanceUntilIdle()

        assertEquals(0, medicationRepository.deleteCalls)
    }

    @Test
    fun deletePet_whenRepositorySucceeds_emitsPetDeleted() = runTest(dispatcher) {
        val events = mutableListOf<PetProfileEvent>()
        val viewModel = createViewModel(
            medicationRepository = FakeMedicationRepository(AppResult.Success(Unit)),
            permissionLevel = PermissionLevel.OWNER,
            petRepository = FakePetRepository(
                petResult = AppResult.Success(Pet(id = PET_ID)),
                deleteResult = AppResult.Success(Unit)
            )
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.collect(events::add)
        }
        viewModel.onAction(PetProfileAction.LoadPet(PET_ID))
        advanceUntilIdle()

        viewModel.onAction(PetProfileAction.DeletePet(PET_ID))
        advanceUntilIdle()

        assertTrue(events.contains(PetProfileEvent.PetDeleted))
    }

    @Test
    fun deletePet_whenRepositoryFails_emitsErrorAndStaysOnProfile() = runTest(dispatcher) {
        val events = mutableListOf<PetProfileEvent>()
        val viewModel = createViewModel(
            medicationRepository = FakeMedicationRepository(AppResult.Success(Unit)),
            permissionLevel = PermissionLevel.OWNER,
            petRepository = FakePetRepository(
                petResult = AppResult.Success(Pet(id = PET_ID)),
                deleteResult = AppResult.Failure(FirestoreError.Network)
            )
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.collect(events::add)
        }
        viewModel.onAction(PetProfileAction.LoadPet(PET_ID))
        advanceUntilIdle()

        viewModel.onAction(PetProfileAction.DeletePet(PET_ID))
        advanceUntilIdle()

        assertTrue(
            events.contains(
                PetProfileEvent.ShowSnackbar(
                    messageRes = R.string.network_error,
                    snackbarType = com.example.petvitals.ui.components.SnackbarType.ERROR
                )
            )
        )
        assertTrue(events.none { it is PetProfileEvent.PetDeleted })
    }

    @Test
    fun loadPet_whenPermissionIsDenied_doesNotFetchPet() = runTest(dispatcher) {
        val petRepository = FakePetRepository(AppResult.Success(Pet(id = PET_ID)))
        val viewModel = createViewModel(
            medicationRepository = FakeMedicationRepository(AppResult.Success(Unit)),
            permissionLevel = PermissionLevel.VIEWER,
            permissionResult = AppResult.Failure(FirestoreError.PermissionDenied),
            petRepository = petRepository
        )

        viewModel.onAction(PetProfileAction.LoadPet(PET_ID))
        advanceUntilIdle()

        assertEquals(0, petRepository.getPetCalls)
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(
            R.string.pet_profile_access_denied,
            viewModel.uiState.value.loadErrorMessageRes
        )
    }

    @Test
    fun loadPet_whenMedicationLoadFails_stopsLoadingWithError() = runTest(dispatcher) {
        val viewModel = createViewModel(
            medicationRepository = FakeMedicationRepository(
                deleteResult = AppResult.Success(Unit),
                getResult = AppResult.Failure(FirestoreError.Network)
            ),
            permissionLevel = PermissionLevel.OWNER
        )

        viewModel.onAction(PetProfileAction.LoadPet(PET_ID))
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(R.string.network_error, viewModel.uiState.value.loadErrorMessageRes)
    }

    private fun createViewModel(
        medicationRepository: FakeMedicationRepository,
        permissionLevel: PermissionLevel,
        petResult: AppResult<FirestoreError, Pet?> = AppResult.Success(Pet(id = PET_ID)),
        foodRepository: FakeFoodRepository = FakeFoodRepository(),
        petRepository: FakePetRepository = FakePetRepository(petResult),
        permissionResult: AppResult<FirestoreError, PermissionLevel> =
            AppResult.Success(permissionLevel)
    ): PetProfileViewModel {
        return PetProfileViewModel(
            petRepository = petRepository,
            medicationRepository = medicationRepository,
            foodRepository = foodRepository,
            getPetPermission = FakeGetPetPermissionUseCase(permissionResult),
            context = ContextWrapper(null)
        )
    }

    private class FakeMedicationRepository(
        private val deleteResult: AppResult<FirestoreError, Unit>,
        private val getResult: AppResult<FirestoreError, List<Medication>> =
            AppResult.Success(listOf(MEDICATION))
    ) : MedicationRepository {
        var deleteCalls = 0

        override suspend fun getMedications(
            petId: String
        ): AppResult<FirestoreError, List<Medication>> = getResult
        override suspend fun saveMedication(medication: Medication) = Unit
        override suspend fun deleteMedication(
            medication: Medication
        ): AppResult<FirestoreError, Unit> {
            deleteCalls++
            return deleteResult
        }
        override suspend fun getMedicationById(medicationId: String, petId: String): Medication? = null
    }

    private class FakeFoodRepository(
        private val food: List<Food> = emptyList(),
        private val deleteResult: AppResult<FirestoreError, Unit> = AppResult.Success(Unit)
    ) : FoodRepository {
        var deleteCalls = 0

        override suspend fun getAllFood(
            petId: String
        ): AppResult<FirestoreError, List<Food>> = AppResult.Success(food)
        override suspend fun getFoodById(petId: String, foodId: String): Food? = null
        override suspend fun saveFood(food: Food) = Unit
        override suspend fun deleteFood(food: Food): AppResult<FirestoreError, Unit> {
            deleteCalls++
            return deleteResult
        }
    }

    private class FakePetRepository(
        private val petResult: AppResult<FirestoreError, Pet?>,
        private val deleteResult: AppResult<FirestoreError, Unit> = AppResult.Success(Unit)
    ) : PetRepository {
        var getPetCalls = 0

        override suspend fun savePet(pet: Pet): AppResult<FirestoreError, Unit> =
            AppResult.Success(Unit)
        override suspend fun getPetById(petId: String): AppResult<FirestoreError, Pet?> {
            getPetCalls++
            return petResult
        }
        override suspend fun getCurrentUserPets(): AppResult<FirestoreError, List<Pet>> =
            AppResult.Success(emptyList())
        override suspend fun deletePet(petId: String): AppResult<FirestoreError, Unit> = deleteResult
        override suspend fun createPetWithOwner(
            pet: Pet,
            member: Member
        ): AppResult<FirestoreError, Unit> = AppResult.Success(Unit)
    }

    private class FakeGetPetPermissionUseCase(
        private val result: AppResult<FirestoreError, PermissionLevel>
    ) : GetPetPermissionUseCase {
        override suspend fun invoke(petId: String): AppResult<FirestoreError, PermissionLevel> =
            result
    }

    private companion object {
        const val PET_ID = "pet-id"
        val MEDICATION = Medication(id = "medication-id", petId = PET_ID)
        val FOOD = Food(id = "food-id", petId = PET_ID)
    }
}
